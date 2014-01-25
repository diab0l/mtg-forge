package forge.gui.deckeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import forge.Singletons;
import forge.StaticData;
import forge.card.ColorSet;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckSection;
import forge.game.GameFormat;
import forge.game.GameType;
import forge.gui.deckchooser.GenerateThemeDeck;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.item.PreconDeck;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.quest.QuestEvent;
import forge.util.IHasName;
import forge.util.storage.IStorage;
import forge.util.storage.StorageImmediatelySerialized;

// Adding a generic to this class creates compile problems in ItemManager (that I can not fix)
public class DeckProxy implements InventoryItem {
    protected IHasName deck;
    protected final IStorage<? extends IHasName> storage;

    public static final Function<DeckProxy, String> FN_GET_NAME = new Function<DeckProxy, String>() {
        @Override
        public String apply(DeckProxy arg0) {
            return arg0.getName();
        }
    };

    // cached values
    protected ColorSet color;
    protected Iterable<GameFormat> formats;
    private int mainSize = Integer.MIN_VALUE;
    private int sbSize = Integer.MIN_VALUE;
    private final String path;
    private final Function<IHasName, Deck> fnGetDeck;
    private String edition;

    public DeckProxy(Deck deck, GameType type, IStorage<? extends IHasName> storage) {
        this(deck, type, "", storage, null);
    }

    public DeckProxy(IHasName deck, Function<IHasName, Deck> fnGetDeck, GameType type, IStorage<? extends IHasName> storage) {
        this(deck, type, "", storage, fnGetDeck);
    }
    
    private DeckProxy(IHasName deck, GameType type, String path, IStorage<? extends IHasName> storage, Function<IHasName, Deck> fnGetDeck) {
        this.deck = deck;
        this.storage = storage;
        this.path = path;
        this.fnGetDeck = fnGetDeck;
        // gametype could give us a hint whether the storage is updateable and enable choice of right editor for this deck
    }

    @Override
    public String getName() {
        return deck.getName();
    }

    @Override
    public String getItemType() {
        // Could distinguish decks depending on gametype
        return "Deck";
    }

    public Deck getDeck() {
        return deck instanceof Deck && fnGetDeck == null ? (Deck) deck : fnGetDeck.apply(deck);
    }

    public String getPath() {
        return path;
    }
    
    public String getEdition() {
        if ( null == edition ) {
            if ( deck instanceof PreconDeck )
                edition = ((PreconDeck) deck).getEdition();
            if ( !isGeneratedDeck() )
                edition = StaticData.instance().getEditions().getEarliestEditionWithAllCards(getDeck().getAllCardsInASinglePool()).getCode();
        }
        return edition;
    }

    @Override
    public String toString() {
        return path + "/" + deck.getName();
    }

    public void invalidateCache() {
        color = null;
        formats = null;
        edition = null;
        mainSize = Integer.MIN_VALUE;
        sbSize = Integer.MIN_VALUE;
    }

    public ColorSet getColor() {
        if (color == null) {
            color = getDeck().getColor();
        }
        return color;
    }

    public Iterable<GameFormat> getFormats() {
        if (formats == null) {
            formats = Singletons.getModel().getFormats().getAllFormatsOfDeck(getDeck());
        }
        return formats;
    }

    public int getMainSize() {
        if (mainSize == Integer.MIN_VALUE) {
            if (deck == null) {
                mainSize = -1;
            }
            else {
                mainSize = getDeck().getMain().countAll();
            }
        }
        return mainSize;
    }

    public int getSideSize() {
        if (sbSize == Integer.MIN_VALUE) {
            CardPool sb = getDeck().get(DeckSection.Sideboard);
            sbSize = sb == null ? -1 : sb.countAll();
            if (sbSize == 0) {
                sbSize = -1;
            }
        }
        return sbSize;
    }

    public boolean isGeneratedDeck() {
        return false;
    }

    // TODO: The methods below should not take the decks collections from singletons, instead they are supposed to use data passed in parameters
    public static Iterable<DeckProxy> getAllConstructedDecks(IStorage<Deck> storageRoot) {
        List<DeckProxy> result = new ArrayList<DeckProxy>();
        addDecksRecursivelly(result, "", storageRoot);
        return result;
    }

    private static void addDecksRecursivelly(List<DeckProxy> list, String path, IStorage<Deck> folder) {
        for (IStorage<Deck> f : folder.getFolders()) {
            String subPath = (StringUtils.isBlank(path) ? "" : path) + "/" + f.getName();
            addDecksRecursivelly(list, subPath, f);
        }

        for (Deck d : folder) {
            list.add(new DeckProxy(d, GameType.Constructed, path, folder, null));
        }
    }

    // Consider using a direct predicate to manage DeckProxies (not this tunnel to collection of paper cards)
    public static final Predicate<DeckProxy> createPredicate(final Predicate<PaperCard> cardPredicate) {
        return new Predicate<DeckProxy>() {
            @Override
            public boolean apply(DeckProxy input) {
                for (Entry<DeckSection, CardPool> deckEntry : input.getDeck()) {
                    switch (deckEntry.getKey()) {
                    case Main:
                    case Sideboard:
                    case Commander:
                        for (Entry<PaperCard, Integer> poolEntry : deckEntry.getValue()) {
                            if (!cardPredicate.apply(poolEntry.getKey())) {
                                return false; //all cards in deck must pass card predicate to pass deck predicate
                            }
                        }
                        break;
                    default:
                        break; //ignore other sections
                    }
                }
                return true;
            }
        };
    }
    
    public void reloadFromStorage() {
        if (storage != null) {
            deck = storage.get(getName());
        }
        invalidateCache();
    }
    
    @SuppressWarnings("unchecked")
    public void updateInStorage() {
        if (storage instanceof StorageImmediatelySerialized<?>) {
            ((StorageImmediatelySerialized<IHasName>)storage).add(deck);
        }
    }
    
    public void deleteFromStorage() { 
        if (storage instanceof StorageImmediatelySerialized<?>) {
            storage.delete(getName());
        }
    }    

    private static class ThemeDeckGenerator extends DeckProxy {
        private final String name;
        public ThemeDeckGenerator(String name0) {
            super(null, null, null);
            name = name0;
        }

        @Override
        public Deck getDeck() {
            final GenerateThemeDeck gen = new GenerateThemeDeck();
            final Deck deck = new Deck();
            gen.setSingleton(Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_SINGLETONS));
            gen.setUseArtifacts(Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
            deck.getMain().addAll(gen.getThemeDeck(this.getName(), 60));
            return deck;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isGeneratedDeck() {
            return true;
        }
    }

    public static Iterable<DeckProxy> getAllThemeDecks() {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        for (final String s : GenerateThemeDeck.getThemeNames()) {
            decks.add(new ThemeDeckGenerator(s));
        }
        return decks;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<DeckProxy> getAllPreconstructedDecks(IStorage<PreconDeck> iStorage) {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        for (final PreconDeck preconDeck : iStorage) {
            decks.add(new DeckProxy(preconDeck, (Function<IHasName, Deck>)(Object)PreconDeck.FN_GET_DECK, null, iStorage));
        }
        return decks;
    }

    public static Iterable<DeckProxy> getAllQuestEventAndChallenges() {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        QuestController quest = Singletons.getModel().getQuest();
        for (QuestEvent e : quest.getDuelsManager().getAllDuels()) {
            decks.add(new DeckProxy(e.getEventDeck(), null, null));
        }
        for (QuestEvent e : quest.getChallenges()) {
            decks.add(new DeckProxy(e.getEventDeck(), null, null));
        }
        return decks;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<DeckProxy> getAllSealedDecks(IStorage<DeckGroup> sealed) {
        final List<DeckProxy> humanDecks = new ArrayList<DeckProxy>();

        // Since AI decks are tied directly to the human choice,
        // they're just mapped in a parallel map and grabbed when the game starts.
        for (final DeckGroup d : sealed) {
            humanDecks.add(new DeckProxy(d, (Function<IHasName, Deck>)(Object)DeckGroup.FN_HUMAN_DECK, GameType.Sealed, sealed));
        }
        return humanDecks;
    }

    public static Iterable<DeckProxy> getAllQuestDecks(IStorage<Deck> storage) {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        if (storage != null) {
            for (final Deck preconDeck : storage) {
                decks.add(new DeckProxy(preconDeck, GameType.Quest, storage));
            }
        }
        return decks;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<DeckProxy> getDraftDecks(IStorage<DeckGroup> draft) {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        for (DeckGroup d : draft) {
            decks.add(new DeckProxy(d, ((Function<IHasName, Deck>)(Object)DeckGroup.FN_HUMAN_DECK), GameType.Draft, draft));
        }
        return decks;
    }
    
}
