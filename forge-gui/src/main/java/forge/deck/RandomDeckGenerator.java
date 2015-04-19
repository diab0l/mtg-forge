package forge.deck;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import forge.game.GameType;
import forge.game.IHasGameType;
import forge.model.FModel;
import forge.quest.QuestController;
import forge.util.Aggregates;

public class RandomDeckGenerator extends DeckProxy implements Comparable<RandomDeckGenerator> {
    private enum RandomDeckType {
        Generated,
        User,
        Favorite
    }
    
    public static List<DeckProxy> getRandomDecks(IHasGameType lstDecks0, boolean isAi0) {
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();

        decks.add(new RandomDeckGenerator("Random Generated Deck", RandomDeckType.Generated, lstDecks0, isAi0));
        decks.add(new RandomDeckGenerator("Random User Deck", RandomDeckType.User, lstDecks0, isAi0));
        decks.add(new RandomDeckGenerator("Random Favorite Deck", RandomDeckType.Favorite, lstDecks0, isAi0));

        return decks;
    }

    private final String name;
    private final RandomDeckType type;
    private final IHasGameType lstDecks;
    private final boolean isAi;

    public RandomDeckGenerator(String name0, RandomDeckType type0, IHasGameType lstDecks0, boolean isAi0) {
        super();
        name = name0;
        type = type0;
        lstDecks = lstDecks0;
        isAi = isAi0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final RandomDeckGenerator d) {
        return d instanceof RandomDeckGenerator ? Integer.compare(type.ordinal(), ((RandomDeckGenerator)d).type.ordinal()) : 1;
    }

    @Override
    public Deck getDeck() {
        switch (type) {
        case Generated:
            return getGeneratedDeck();
        case User:
            return getUserDeck();
        default:
            return getFavoriteDeck();
        }
    }

    private Deck getGeneratedDeck() {
        switch (lstDecks.getGameType()) {
        case Commander:
            return DeckgenUtil.generateCommanderDeck(isAi, GameType.Commander);
        case TinyLeaders:
            return DeckgenUtil.generateCommanderDeck(isAi, GameType.TinyLeaders);
        case Archenemy:
            return DeckgenUtil.generateSchemeDeck();
        case Planechase:
            return DeckgenUtil.generatePlanarDeck();
        default:
            while (true) {
                switch (Aggregates.random(DeckType.values())) {
                case PRECONSTRUCTED_DECK:
                    return Aggregates.random(DeckProxy.getAllPreconstructedDecks(QuestController.getPrecons())).getDeck();
                case QUEST_OPPONENT_DECK:
                    return Aggregates.random(DeckProxy.getAllQuestEventAndChallenges()).getDeck();
                case COLOR_DECK:
                    List<String> colors = new ArrayList<String>();
                    int count = Aggregates.randomInt(1, 3);
                    for (int i = 1; i <= count; i++) {
                        colors.add("Random " + i);
                    }
                    return DeckgenUtil.buildColorDeck(colors, isAi);
                case THEME_DECK:
                    return Aggregates.random(DeckProxy.getAllThemeDecks()).getDeck();
                default:
                    continue;
                }
            }
        }
    }
    
    private Deck getUserDeck() {
        Iterable<Deck> decks;
        switch (lstDecks.getGameType()) {
        case Commander:
            decks = FModel.getDecks().getCommander();
        case TinyLeaders:
            decks = DeckFormat.TinyLeaders.getLegalDecks(FModel.getDecks().getCommander());
        case Archenemy:
            decks = FModel.getDecks().getScheme();
        case Planechase:
            decks = FModel.getDecks().getPlane();
        default:
            decks = FModel.getDecks().getConstructed();
        }
        if (Iterables.isEmpty(decks)) {
            return getGeneratedDeck(); //fall back to generated deck if no decks in filtered list
        }
        return Aggregates.random(decks);
    }

    private Deck getFavoriteDeck() {
        Iterable<DeckProxy> decks;
        switch (lstDecks.getGameType()) {
        case Commander:
            decks = DeckProxy.getAllCommanderDecks();
        case TinyLeaders:
            decks = Iterables.filter(DeckProxy.getAllCommanderDecks(), new Predicate<DeckProxy>() {
                @Override
                public boolean apply(DeckProxy deck) {
                    return DeckFormat.TinyLeaders.getDeckConformanceProblem(deck.getDeck()) == null;
                }
            });
        case Archenemy:
            decks = DeckProxy.getAllSchemeDecks();
        case Planechase:
            decks = DeckProxy.getAllPlanarDecks();
        default:
            decks = DeckProxy.getAllConstructedDecks();
        }
        decks = Iterables.filter(decks, new Predicate<DeckProxy>() {
            @Override
            public boolean apply(DeckProxy deck) {
                return deck.isFavoriteDeck();
            }
        });
        if (Iterables.isEmpty(decks)) {
            return getGeneratedDeck(); //fall back to generated deck if no favorite decks
        }
        return Aggregates.random(decks).getDeck();
    }

    @Override
    public boolean isGeneratedDeck() {
        return true;
    }
}
