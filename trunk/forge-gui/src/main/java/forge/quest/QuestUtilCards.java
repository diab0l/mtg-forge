/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Singletons;
import forge.card.BoosterGenerator;
import forge.card.BoosterSlots;
import forge.card.CardEdition;
import forge.card.CardEditionPredicates;
import forge.card.CardRarity;
import forge.card.FormatCollection;
import forge.card.ICardDatabase;
import forge.card.MagicColor;
import forge.card.SealedProductTemplate;
import forge.card.UnOpenedProduct;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.item.BoosterPack;
import forge.item.PaperCard;
import forge.item.FatPack;
import forge.item.IPaperCard;
import forge.item.InventoryItem;
import forge.item.OpenablePack;
import forge.item.PreconDeck;
import forge.item.TournamentPack;
import forge.quest.bazaar.QuestItemType;
import forge.quest.data.GameFormatQuest;
import forge.quest.data.QuestAssets;
import forge.quest.data.QuestPreferences;
import forge.quest.data.QuestPreferences.DifficultyPrefs;
import forge.quest.data.QuestPreferences.QPref;
import forge.util.Aggregates;
import forge.util.ItemPool;
import forge.util.ItemPoolView;
import forge.util.MyRandom;

/**
 * This is a helper class to execute operations on QuestData. It has been
 * created to decrease complexity of questData class
 */
public final class QuestUtilCards {
    private final QuestController qc;
    private final QuestPreferences qpref;
    private final QuestAssets qa;

    /**
     * Instantiates a new quest util cards.
     * 
     * @param qd
     *            the qd
     */
    public QuestUtilCards(final QuestController qd) {
        this.qc = qd;
        this.qa = qc.getAssets();
        this.qpref = Singletons.getModel().getQuestPreferences();
    }

    /**
     * Adds the basic lands (from random sets as limited by the format).
     *
     * @param nBasic the n basic
     * @param nSnow the n snow
     * @param usedFormat currently enforced game format, if any
     * @return the item pool view
     */
    public static ItemPoolView<PaperCard> generateBasicLands(final int nBasic, final int nSnow, final GameFormatQuest usedFormat) {
        final ICardDatabase db = Singletons.getMagicDb().getCommonCards();
        final ItemPool<PaperCard> pool = new ItemPool<PaperCard>(PaperCard.class);

        List<String> landCodes = new ArrayList<String>();
        List<String> snowLandCodes = new ArrayList<String>();

        if (usedFormat != null) {
            List<String> availableEditions = usedFormat.getAllowedSetCodes();

            for (String edCode : availableEditions) {
                CardEdition ed = Singletons.getMagicDb().getEditions().get(edCode);
                // Duel decks might have only 2 types of basic lands
                if (CardEditionPredicates.hasBasicLands.apply(ed)) {
                    landCodes.add(edCode);
                }
            }
            if (usedFormat.isSetLegal("ICE")) {
                snowLandCodes.add("ICE");
            }
            if (usedFormat.isSetLegal("CSP")) {
                snowLandCodes.add("CSP");
            }
        } else {
            Iterable<CardEdition> allEditions = Singletons.getMagicDb().getEditions();
            for (CardEdition edition : Iterables.filter(allEditions, CardEditionPredicates.hasBasicLands)) {
                landCodes.add(edition.getCode());
            }
            snowLandCodes.add("ICE");
            snowLandCodes.add("CSP");
        }

        String landCode = Aggregates.random(landCodes);
        if (null == landCode) {
            landCode = "M10";
        }

        for (String landName : MagicColor.Constant.BASIC_LANDS) {
            pool.add(db.getCard(landName, landCode), nBasic);
        }


        if (!snowLandCodes.isEmpty()) {
            String snowLandCode = Aggregates.random(snowLandCodes);
            for (String landName : MagicColor.Constant.SNOW_LANDS) {
                pool.add(db.getCard(landName, snowLandCode), nSnow);
            }
        }

        return pool;
    }

    /**
     * <p>
     * addCards.
     * </p>
     * 
     * @param fSets
     *            the f sets
     * @return the array list
     */
    public List<PaperCard> generateQuestBooster(final Predicate<PaperCard> fSets) {
        UnOpenedProduct unopened = new UnOpenedProduct(getBoosterTemplate(), fSets);
        return unopened.get();
    }

    /**
     * Adds the all cards.
     * 
     * @param newCards
     *            the new cards
     */
    public void addAllCards(final Iterable<PaperCard> newCards) {
        for (final PaperCard card : newCards) {
            this.addSingleCard(card, 1);
        }
    }

    /**
     * Adds the single card.
     * 
     * @param card
     *            the card
     * @param qty
     *          quantity
     */
    public void addSingleCard(final PaperCard card, int qty) {
        this.qa.getCardPool().add(card, qty);

        // register card into that list so that it would appear as a new one.
        this.qa.getNewCardList().add(card, qty);
    }

    private static final Predicate<PaperCard> RARE_PREDICATE = IPaperCard.Predicates.Presets.IS_RARE_OR_MYTHIC;


    /**
     * A predicate that takes into account the Quest Format (if any).
     * @param source
     *  the predicate to be added to the format predicate.
     * @return the composite predicate.
     */
    public Predicate<PaperCard> applyFormatFilter(Predicate<PaperCard> source) {
       return qc.getFormat() == null ? source : Predicates.and(source, qc.getFormat().getFilterPrinted());
    }

    /**
     * Adds the random rare.
     * 
     * @return the card printed
     */
    public PaperCard addRandomRare() {

        final Predicate<PaperCard> myFilter = applyFormatFilter(QuestUtilCards.RARE_PREDICATE);

        final PaperCard card = Aggregates.random(Iterables.filter(Singletons.getMagicDb().getCommonCards().getAllCards(), myFilter));
        this.addSingleCard(card, 1);
        return card;
    }

    /**
     * Adds the random rare.
     * 
     * @param n
     *            the n
     * @return the list
     */
    public List<PaperCard> addRandomRare(final int n) {
        final Predicate<PaperCard> myFilter = applyFormatFilter(QuestUtilCards.RARE_PREDICATE);

        final List<PaperCard> newCards = Aggregates.random(Iterables.filter(Singletons.getMagicDb().getCommonCards().getAllCards(), myFilter), n);
        this.addAllCards(newCards);
        return newCards;
    }

    /**
     * Setup new game card pool.
     * 
     * @param filter
     *            the filter
     * @param idxDifficulty
     *            the idx difficulty
     * @param userPrefs
     *            user preferences
     */
    public void setupNewGameCardPool(final Predicate<PaperCard> filter, final int idxDifficulty, final StartingPoolPreferences userPrefs) {
        final int nC = this.qpref.getPrefInt(DifficultyPrefs.STARTING_COMMONS, idxDifficulty);
        final int nU = this.qpref.getPrefInt(DifficultyPrefs.STARTING_UNCOMMONS, idxDifficulty);
        final int nR = this.qpref.getPrefInt(DifficultyPrefs.STARTING_RARES, idxDifficulty);

        this.addAllCards(BoosterUtils.getQuestStarterDeck(filter, nC, nU, nR, userPrefs));
    }

    /**
     * Buy card.
     * 
     * @param card
     *            the card
     * @param qty
     *          quantity
     * @param value
     *            the value
     */
    public void buyCard(final PaperCard card, int qty, final int value) {
        int totalCost = qty * value;
        if (this.qa.getCredits() >= totalCost) {
            this.qa.setCredits(this.qa.getCredits() - totalCost);
            this.qa.getShopList().remove(card, qty);
            this.addSingleCard(card, qty);
        }
    }

    /**
     * Buy booster.
     * 
     * @param booster
     *            the booster
     * @param value
     *            the value
     */
    public void buyPack(final OpenablePack booster, final int value) {
        if (this.qa.getCredits() >= value) {
            this.qa.setCredits(this.qa.getCredits() - value);
            this.qa.getShopList().remove(booster);
            this.addAllCards(booster.getCards());
        }
    }

    /**
     * Buy precon deck.
     * 
     * @param precon
     *            the precon
     * @param value
     *            the value
     */
    public void buyPreconDeck(final PreconDeck precon, final int value) {
        if (this.qa.getCredits() >= value) {
            this.qa.setCredits(this.qa.getCredits() - value);
            this.qa.getShopList().remove(precon);
            this.addDeck(precon.getDeck());
        }
    }

    /**
     * Import an existing deck.
     * 
     * @param fromDeck
     *            Deck, deck to import
     */
    void addDeck(final Deck fromDeck) {
        if (fromDeck == null) {
            return;
        }
        this.qc.getMyDecks().add(fromDeck);
        this.addAllCards(fromDeck.getMain().toFlatList());
        if (fromDeck.has(DeckSection.Sideboard)) {
            this.addAllCards(fromDeck.get(DeckSection.Sideboard).toFlatList());
        }
    }

    /**
     * Sell card.
     * 
     * @param card
     *            the card
     * @param qty
     *          quantity
     * @param pricePerCard
     *            the price per card
     */
    public void sellCard(final PaperCard card, int qty, final int pricePerCard) {
        this.sellCard(card, qty, pricePerCard, true);
    }

    /**
     * lose card.
     * 
     * @param card
     *            the card
     * @param qty
     *          quantity
     */
    public void loseCard(final PaperCard card, int qty) {
        this.sellCard(card, qty, 0, false);
    }

    /**
     * Sell card.
     * 
     * @param card
     *            the card
     * @param price
     *            the price
     * @param addToShop
     *            true if this card should be added to the shop, false otherwise
     */
    private void sellCard(final PaperCard card, int qty, final int pricePerCard, final boolean addToShop) {
        if (pricePerCard > 0) {
            this.qa.setCredits(this.qa.getCredits() + (qty * pricePerCard));
        }
        this.qa.getCardPool().remove(card, qty);
        if (addToShop) {
            this.qa.getShopList().add(card, qty);
        }

        // remove card being sold from all decks
        final int leftInPool = this.qa.getCardPool().count(card);
        // remove sold cards from all decks:
        for (final Deck deck : this.qc.getMyDecks()) {
            int cntInMain = deck.getMain().count(card);
            int cntInSb = deck.has(DeckSection.Sideboard) ? deck.get(DeckSection.Sideboard).count(card) : 0;
            int nToRemoveFromThisDeck = cntInMain + cntInSb - leftInPool;
            if (nToRemoveFromThisDeck <= 0) {
                continue; // this is not the deck you are looking for
            }

            int nToRemoveFromSb = Math.min(cntInSb, nToRemoveFromThisDeck);
            if (nToRemoveFromSb > 0) {
                deck.get(DeckSection.Sideboard).remove(card, nToRemoveFromSb);
                nToRemoveFromThisDeck -= nToRemoveFromSb;
                if (0 >= nToRemoveFromThisDeck) {
                    continue; // done here
                }
            }

            deck.getMain().remove(card, nToRemoveFromThisDeck);
        }
    }

    /**
     * Clear shop list.
     */
    public void clearShopList() {
        if (null != this.qa.getShopList()) {
            this.qa.getShopList().clear();
        }
    }

    /**
     * Gets the sell mutliplier.
     * 
     * @return the sell mutliplier
     */
    public double getSellMultiplier() {
        double multi = 0.20 + (0.001 * this.qc.getAchievements().getWin());
        if (multi > 0.6) {
            multi = 0.6;
        }

        final int lvlEstates = this.qc.getMode() == QuestMode.Fantasy ? this.qa.getItemLevel(QuestItemType.ESTATES) : 0;
        switch (lvlEstates) {
        case 1:
            multi += 0.01;
            break;
        case 2:
            multi += 0.0175;
            break;
        case 3:
            multi += 0.025;
            break;
        default:
            break;
        }

        return multi;
    }

    /**
     * Gets the sell price limit.
     * 
     * @return the sell price limit
     */
    public int getSellPriceLimit() {
        return this.qc.getAchievements().getWin() <= 50 ? 1000 : Integer.MAX_VALUE;
    }

    /**
     * Generate cards in shop.
     */
    private final FormatCollection formats = Singletons.getMagicDb().getFormats();
    private final Predicate<CardEdition> filterExt = CardEditionPredicates.isLegalInFormat(this.formats.getExtended());

    /** The filter t2booster. */
    private final Predicate<CardEdition> filterT2booster = Predicates.and(CardEditionPredicates.CAN_MAKE_BOOSTER,
            CardEditionPredicates.isLegalInFormat(this.formats.getStandard()));

    /** The filter ext but t2. */
    private final Predicate<CardEdition> filterExtButT2 = Predicates.and(
            CardEditionPredicates.CAN_MAKE_BOOSTER,
            Predicates.and(this.filterExt,
                    Predicates.not(CardEditionPredicates.isLegalInFormat(this.formats.getStandard()))));

    /** The filter not ext. */
    private final Predicate<CardEdition> filterNotExt = Predicates.and(CardEditionPredicates.CAN_MAKE_BOOSTER,
            Predicates.not(this.filterExt));

    /**
     * Helper predicate for shops: is legal in quest format.
     * 
     * @param qFormat
     *            the quest format
     * @return the predicate
     */
    public static Predicate<CardEdition> isLegalInQuestFormat(final GameFormatQuest qFormat) {
        return GameFormatQuest.Predicates.isLegalInFormatQuest(qFormat);
    }

    /**
     * Generate boosters in shop.
     * 
     * @param count
     *            the count
     */
    private void generateBoostersInShop(final int count) {
        for (int i = 0; i < count; i++) {
            final int rollD100 = MyRandom.getRandom().nextInt(100);
            Predicate<CardEdition> filter = rollD100 < 40 ? this.filterT2booster
                    : (rollD100 < 75 ? this.filterExtButT2 : this.filterNotExt);
            if (qc.getFormat() != null) {
                filter = Predicates.and(CardEditionPredicates.CAN_MAKE_BOOSTER, isLegalInQuestFormat(qc.getFormat()));
            }
            Iterable<CardEdition> rightEditions = Iterables.filter(Singletons.getMagicDb().getEditions(), filter);
            this.qa.getShopList().add(BoosterPack.FN_FROM_SET.apply(Aggregates.random(rightEditions)));
        }
    }

    /**
     * Generate precons in shop.
     * 
     * @param count
     *            the count
     */
    private void generateTournamentsInShop(final int count) {
        Predicate<CardEdition> formatFilter = CardEditionPredicates.HAS_TOURNAMENT_PACK;
        if (qc.getFormat() != null) {
            formatFilter = Predicates.and(formatFilter, isLegalInQuestFormat(qc.getFormat()));
        }
        Iterable<CardEdition> rightEditions = Iterables.filter(Singletons.getMagicDb().getEditions(), formatFilter);
        this.qa.getShopList().addAllFlat(Aggregates.random(Iterables.transform(rightEditions, TournamentPack.FN_FROM_SET), count));
    }

    /**
     * Generate precons in shop.
     * 
     * @param count
     *            the count
     */
    private void generateFatPacksInShop(final int count) {
        Predicate<CardEdition> formatFilter = CardEditionPredicates.HAS_FAT_PACK;
        if (qc.getFormat() != null) {
            formatFilter = Predicates.and(formatFilter, isLegalInQuestFormat(qc.getFormat()));
        }
        Iterable<CardEdition> rightEditions = Iterables.filter(Singletons.getMagicDb().getEditions(), formatFilter);
        this.qa.getShopList().addAllFlat(Aggregates.random(Iterables.transform(rightEditions, FatPack.FN_FROM_SET), count));
    }

    /**
     * Generate precons in shop.
     * 
     * @param count
     *            the count
     */
    private void generatePreconsInShop(final int count) {
        final List<PreconDeck> meetRequirements = new ArrayList<PreconDeck>();
        for (final PreconDeck deck : QuestController.getPrecons()) {
            if (deck.getRecommendedDeals().meetsRequiremnts(this.qc.getAchievements())
                    && (null == qc.getFormat() || qc.getFormat().isSetLegal(deck.getEdition()))) {
                meetRequirements.add(deck);
            }
        }
        this.qa.getShopList().addAllFlat(Aggregates.random(meetRequirements, count));
    }

    @SuppressWarnings("unchecked")
    private SealedProductTemplate getShopBoosterTemplate() {
        return new SealedProductTemplate(Lists.newArrayList(
            Pair.of(BoosterSlots.COMMON, this.qpref.getPrefInt(QPref.SHOP_SINGLES_COMMON)),
            Pair.of(BoosterSlots.UNCOMMON, this.qpref.getPrefInt(QPref.SHOP_SINGLES_UNCOMMON)),
            Pair.of(BoosterSlots.RARE_MYTHIC, this.qpref.getPrefInt(QPref.SHOP_SINGLES_RARE))
        ));
    }

    @SuppressWarnings("unchecked")
    private SealedProductTemplate getBoosterTemplate() {
        return new SealedProductTemplate(Lists.newArrayList(
            Pair.of(BoosterSlots.COMMON, this.qpref.getPrefInt(QPref.BOOSTER_COMMONS)),
            Pair.of(BoosterSlots.UNCOMMON, this.qpref.getPrefInt(QPref.BOOSTER_UNCOMMONS)),
            Pair.of(BoosterSlots.RARE_MYTHIC, this.qpref.getPrefInt(QPref.BOOSTER_RARES))
        ));
    }

    /**
     * Generate cards in shop.
     */
    private void generateCardsInShop() {
        // Preferences
        final int startPacks = this.qpref.getPrefInt(QPref.SHOP_STARTING_PACKS);
        final int winsForPack = this.qpref.getPrefInt(QPref.SHOP_WINS_FOR_ADDITIONAL_PACK);
        final int maxPacks = this.qpref.getPrefInt(QPref.SHOP_MAX_PACKS);

        int level = this.qc.getAchievements().getLevel();
        final int levelPacks = level > 0 ? startPacks / level : startPacks;
        final int winPacks = this.qc.getAchievements().getWin() / winsForPack;
        final int totalPacks = Math.min(levelPacks + winPacks, maxPacks);


        SealedProductTemplate tpl = getShopBoosterTemplate();
        UnOpenedProduct unopened = qc.getFormat() == null ?  new UnOpenedProduct(tpl) : new UnOpenedProduct(tpl, qc.getFormat().getFilterPrinted());

        for (int i = 0; i < totalPacks; i++) {
            this.qa.getShopList().addAllFlat(unopened.get());
        }

        this.generateBoostersInShop(totalPacks);
        this.generatePreconsInShop(totalPacks);
        this.generateTournamentsInShop(totalPacks);
        this.generateFatPacksInShop(totalPacks);
        int numberSnowLands = 5;
        if (qc.getFormat() != null && !qc.getFormat().hasSnowLands()) {
            numberSnowLands = 0;
        }
        this.qa.getShopList().addAll(QuestUtilCards.generateBasicLands(10, numberSnowLands, qc.getFormat()));
    }

    /**
     * Gets the cardpool.
     * 
     * @return the cardpool
     */
    public ItemPool<PaperCard> getCardpool() {
        return this.qa.getCardPool();
    }

    /**
     * Gets the shop list.
     * 
     * @return the shop list
     */
    public ItemPoolView<InventoryItem> getShopList() {
        if (this.qa.getShopList().isEmpty()) {
            this.generateCardsInShop();
        }
        return this.qa.getShopList();
    }

    /**
     * Gets the new cards.
     * 
     * @return the new cards
     */
    public ItemPoolView<InventoryItem> getNewCards() {
        return this.qa.getNewCardList();
    }

    /**
     * Reset new list.
     */
    public void resetNewList() {
        this.qa.getNewCardList().clear();
    }

    public Function<Entry<InventoryItem, Integer>, Comparable<?>> getFnNewCompare() {
        return this.fnNewCompare;
    }

    public Function<Entry<InventoryItem, Integer>, Object> getFnNewGet() {
        return this.fnNewGet;
    }

    // These functions provide a way to sort and compare cards in a table
    // according to their new-ness
    // It might be a good idea to store them in a base class for both quest-mode
    // deck editors
    // Maybe we should consider doing so later
    /** The fn new compare. */
    private final Function<Entry<InventoryItem, Integer>, Comparable<?>> fnNewCompare =
            new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return QuestUtilCards.this.qa.getNewCardList().contains(from.getKey()) ? Integer.valueOf(1) : Integer
                    .valueOf(0);
        }
    };

    /** The fn new get. */
    private final Function<Entry<InventoryItem, Integer>, Object> fnNewGet =
            new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return QuestUtilCards.this.qa.getNewCardList().contains(from.getKey()) ? "NEW" : "";
        }
    };

    public Function<Entry<InventoryItem, Integer>, Comparable<?>> getFnOwnedCompare() {
        return this.fnOwnedCompare;
    }

    public Function<Entry<InventoryItem, Integer>, Object> getFnOwnedGet() {
        return this.fnOwnedGet;
    }

    public int getCompletionPercent(String edition) {
        // get all cards in the specified edition
        Predicate<PaperCard> filter = IPaperCard.Predicates.printedInSet(edition);
        Iterable<PaperCard> editionCards = Iterables.filter(Singletons.getMagicDb().getCommonCards().getAllCards(), filter);

        ItemPool<PaperCard> ownedCards = qa.getCardPool();
        // 100% means at least one of every basic land and at least 4 of every other card in the set
        int completeCards = 0;
        int numOwnedCards = 0;
        for (PaperCard card : editionCards) {
            final int target = CardRarity.BasicLand == card.getRarity() ? 1 : 4;

            completeCards += target;
            numOwnedCards += Math.min(target, ownedCards.count(card));
        }

        return (numOwnedCards * 100) / completeCards;
    }

    // These functions provide a way to sort and compare items in the spell shop according to how many are already owned
    private final Function<Entry<InventoryItem, Integer>, Comparable<?>> fnOwnedCompare =
            new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            InventoryItem i = from.getKey();
            if (i instanceof PaperCard) {
                return QuestUtilCards.this.qa.getCardPool().count((PaperCard) i);
            } else if (i instanceof PreconDeck) {
                PreconDeck pDeck = (PreconDeck) i;
                return Singletons.getModel().getQuest().getMyDecks().contains(pDeck.getName()) ? -1 : -2;
            } else if (i instanceof OpenablePack) {
                OpenablePack oPack = (OpenablePack) i;
                return getCompletionPercent(oPack.getEdition()) - 103;
            }
            return null;
        }
    };

    private final Function<Entry<InventoryItem, Integer>, Object> fnOwnedGet =
            new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            InventoryItem i = from.getKey();
            if (i instanceof PaperCard) {
                return QuestUtilCards.this.qa.getCardPool().count((PaperCard) i);
            } else if (i instanceof PreconDeck) {
                PreconDeck pDeck = (PreconDeck) i;
                return Singletons.getModel().getQuest().getMyDecks().contains(pDeck.getName()) ? "YES" : "NO";
            } else if (i instanceof OpenablePack) {
                OpenablePack oPack = (OpenablePack) i;
                return String.format("%d%%", getCompletionPercent(oPack.getEdition()));
            }
            return null;
        }
    };
}
