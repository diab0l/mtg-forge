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
package forge.quest.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.slightlymagic.maxmtg.Predicate;
import forge.AllZone;
import forge.Singletons;
import forge.deck.Deck;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.item.PreconDeck;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.data.item.QuestInventory;
import forge.quest.data.pet.QuestPetManager;
import forge.util.FolderMapView;
import forge.util.IFolderMap;
import forge.util.IFolderMapView;
import forge.util.MyRandom;

//when you create QuestDataOld and AFTER you copy the AI decks over
//you have to call one of these two methods below
//see Gui_QuestOptions for more details

/**
 * <p>
 * QuestData class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class QuestData {

    // This field holds the version of the Quest Data
    /** Constant <code>CURRENT_VERSION_NUMBER=2</code>. */
    public static final int CURRENT_VERSION_NUMBER = 2;

    // This field places the version number into QD instance,
    // but only when the object is created through the constructor
    // DO NOT RENAME THIS FIELD
    /** The version number. */
    private int versionNumber = QuestData.CURRENT_VERSION_NUMBER;

    /** The rank index. */
    private int rankIndex; // level

    /** The win. */
    private int win; // number of wins

    /** The lost. */
    private int lost;

    private int winstreakBest = 0;

    private int winstreakCurrent = 0;

    /** The credits. */
    private long credits; // this money is good for all modes

    /** The life. */
    private int life; // for fantasy mode, how much life bought at shop to start
                      // game
    // with
    /** The inventory. */
    private QuestInventory inventory = new QuestInventory(); // different
                                                             // gadgets

    /** The pet manager. */
    private final QuestPetManager petManager = new QuestPetManager(); // pets
                                                                      // that
                                                                      // start
                                                                      // match
    // with you

    // Diffuculty - they store both index and title
    /** The diff index. */
    private int diffIndex;

    /** The difficulty. */
    private String difficulty;

    /** */
    private String name = "";

    // Quest mode - there should be an enum :(
    /** The mode. */
    private String mode = "";

    /** The Constant FANTASY. */
    public static final String FANTASY = "Fantasy";

    /** The Constant CLASSIC. */
    public static final String CLASSIC = "Classic";

    // Decks collected by player
    /** The my decks. */
    private final HashMap<String, Deck> myDecks = new HashMap<String, Deck>();

    private transient IFolderMap<Deck> decks;

    // Cards associated with quest
    /** The card pool. */
    private ItemPool<CardPrinted> cardPool = new ItemPool<CardPrinted>(CardPrinted.class); // player's
    // belonging
    /** The shop list. */
    private ItemPool<InventoryItem> shopList = new ItemPool<InventoryItem>(InventoryItem.class); // the
    // current
    // shop
    // list
    /** The new card list. */
    private ItemPool<InventoryItem> newCardList = new ItemPool<InventoryItem>(InventoryItem.class); // cards
    // acquired
    // since
    // last
    // game-win/loss

    // Challenge history
    /** The challenges played. */
    private int challengesPlayed = 0;

    /** The available challenges. */
    private List<Integer> availableChallenges = new ArrayList<Integer>();

    /** The completed challenges. */
    private List<Integer> completedChallenges = new ArrayList<Integer>();

    // Challenges used to be called quests. During the renaming,
    // files could be corrupted. These fields ensure old files still work.
    // These fields should be phased out after a little while.
    // The old files, if played once, are updated automatically to the new
    // system.
    /** The quests played. */
    private int questsPlayed = -1;

    /** The available quests. */
    private List<Integer> availableQuests = null;

    /** The completed quests. */
    private List<Integer> completedQuests = null;

    // own randomizer seed
    private long randomSeed = 0;

    // Utility class to access cards, has access to private fields
    // Moved some methods there that otherwise would make this class even more
    // complex
    private transient QuestUtilCards myCards;

    // This is used by shop. Had no idea where else to place this
    private static transient IFolderMapView<PreconDeck> preconManager = new FolderMapView<PreconDeck>(new PreconReader(ForgeProps.getFile(NewConstants.Quest.PRECONS)));

    /** The Constant RANK_TITLES. */
    public static final String[] RANK_TITLES = new String[] { "Level 0 - Confused Wizard", "Level 1 - Mana Mage",
            "Level 2 - Death by Megrim", "Level 3 - Shattered the Competition", "Level 4 - Black Knighted",
            "Level 5 - Shockingly Good", "Level 6 - Regressed into Timmy", "Level 7 - Loves Blue Control",
            "Level 8 - Immobilized by Fear", "Level 9 - Lands = Friends", "Level 10 - Forging new paths",
            "Level 11 - Infect-o-tron", "Level 12 - Great Balls of Fire", "Level 13 - Artifact Schmartifact",
            "Level 14 - Mike Mulligan's The Name", "Level 15 - Fresh Air: Good For The Health",
            "Level 16 - In It For The Love", "Level 17 - Sticks, Stones, Bones", "Level 18 - Credits For Breakfast",
            "Level 19 - Millasaurus", "Level 20 - One-turn Wonder", "Teaching Gandalf a Lesson",
            "What Do You Do With The Other Hand?", "Freelance Sorcerer, Works Weekends",
            "Should We Hire Commentators?", "Saltblasted For Your Talent", "Serra Angel Is Your Girlfriend", };

    /** */
    public QuestData() {
        this("An Unknown Quest");
    }

    /**
     * <p>
     * Constructor for QuestData.
     * </p>
     * 
     * @param s0 &emsp; String name
     */
    public QuestData(String s0) {
        this.initTransients();
        this.setName(s0);

        QuestPreferences prefs = Singletons.getModel().getQuestPreferences();
        ItemPoolView<CardPrinted> lands = QuestUtilCards.generateBasicLands(prefs.getPreferenceInt(QPref.STARTING_BASIC_LANDS),
                prefs.getPreferenceInt(QPref.STARTING_BASIC_LANDS));
        this.getCardPool().addAll(lands);
        this.randomizeOpponents();
    }

    private void initTransients() {
        // These are helper classes that hold no data.
        this.decks = new QuestDeckMap(myDecks);
        this.myCards = new QuestUtilCards(this);


        // to avoid NPE some pools will be created here if they are null
        if (null == this.getNewCardList()) {
            this.setNewCardList(new ItemPool<InventoryItem>(InventoryItem.class));
        }
        if (null == this.getShopList()) {
            this.setShopList(new ItemPool<InventoryItem>(InventoryItem.class));
        }

    }

    /**
     * New game.
     * 
     * @param diff
     *            the diff
     * @param m0de
     *            the m0de
     * @param standardStart
     *            the standard start
     */
    public void newGame(final int diff, final String m0de, final boolean standardStart) {
        this.setDifficulty(diff);

        final Predicate<CardPrinted> filter = standardStart ? AllZone.getFormats().getStandard().getFilterPrinted()
                : CardPrinted.Predicates.Presets.IS_TRUE;

        this.myCards.setupNewGameCardPool(filter, diff);
        this.setCredits(Singletons.getModel().getQuestPreferences().getPreferenceInt(QPref.STARTING_CREDITS, diff));

        this.mode = m0de;
        this.life = this.mode.equals(QuestData.FANTASY) ? 15 : 20;
    }

    // All belongings
    /**
     * Gets the inventory.
     * 
     * @return the inventory
     */
    public QuestInventory getInventory() {
        return this.inventory;
    }

    /**
     * Gets the pet manager.
     * 
     * @return the pet manager
     */
    public QuestPetManager getPetManager() {
        return this.petManager;
    }

    // Cards - class uses data from here
    /**
     * Gets the cards.
     * 
     * @return the cards
     */
    public QuestUtilCards getCards() {
        return this.myCards;
    }

    // Challenge performance
    /**
     * Gets the challenges played.
     * 
     * @return the challenges played
     */
    public int getChallengesPlayed() {
        // This should be phased out after a while, when
        // old quest decks have been updated. (changes made 19-9-11)
        if (this.questsPlayed != -1) {
            this.challengesPlayed = this.questsPlayed;
            this.questsPlayed = -1;
        }

        return this.challengesPlayed;
    }

    /**
     * Adds the challenges played.
     */
    public void addChallengesPlayed() {
        this.challengesPlayed++;
    }

    /**
     * Gets the available challenges.
     * 
     * @return the available challenges
     */
    public List<Integer> getAvailableChallenges() {
        // This should be phased out after a while, when
        // old quest decks have been updated. (changes made 19-9-11)
        if (this.availableQuests != null) {
            this.availableChallenges = this.availableQuests;
            this.availableQuests = null;
        }

        return this.availableChallenges != null ? new ArrayList<Integer>(this.availableChallenges) : null;
    }

    /**
     * Sets the available challenges.
     * 
     * @param list
     *            the new available challenges
     */
    public void setAvailableChallenges(final List<Integer> list) {
        this.availableChallenges = list;
    }

    /**
     * Clear available challenges.
     */
    public void clearAvailableChallenges() {
        this.availableChallenges.clear();
    }

    /**
     * <p>
     * getCompletedChallenges.
     * </p>
     * Returns stored list of non-repeatable challenge IDs.
     * 
     * @return List<Integer>
     */
    public List<Integer> getCompletedChallenges() {
        // This should be phased out after a while, when
        // old quest decks have been updated. (changes made 19-9-11)
        // Also, poorly named - this should be "getLockedChalleneges" or
        // similar.
        if (this.completedQuests != null) {
            this.completedChallenges = this.completedQuests;
            this.completedQuests = null;
        }

        return this.completedChallenges != null ? new ArrayList<Integer>(this.completedChallenges) : null;
    }

    /**
     * <p>
     * addCompletedChallenge.
     * </p>
     * Add non-repeatable challenge ID to list.
     * 
     * @param i
     *            the i
     */

    // Poorly named - this should be "setLockedChalleneges" or similar.
    public void addCompletedChallenge(final int i) {
        this.completedChallenges.add(i);
    }

    // Wins & Losses
    /**
     * Gets the lost.
     * 
     * @return the lost
     */
    public int getLost() {
        return this.lost;
    }

    /**
     * Adds the lost.
     */
    public void addLost() {
        this.lost++;

        if (winstreakCurrent > winstreakBest) {
            winstreakBest = winstreakCurrent;
        }

        winstreakCurrent = 0;
    }

    /**
     * Gets the win.
     * 
     * @return the win
     */
    public int getWin() {
        return this.win;
    }

    /**
     * Adds the win.
     */
    public void addWin() { // changes getRank()
        this.win++;
        this.winstreakCurrent++;

        if (winstreakCurrent > winstreakBest) {
            winstreakBest = winstreakCurrent;
        }

        final int winsToLvlUp = Singletons.getModel().getQuestPreferences().getPreferenceInt(QPref.WINS_RANKUP, this.diffIndex);
        if ((this.win % winsToLvlUp) == 0) {
            this.rankIndex++;
        }
    }

    // Life (only fantasy)
    /**
     * Gets the life.
     * 
     * @return the life
     */
    public int getLife() {
        return this.isFantasy() ? this.life : 20;
    }

    /**
     * Adds n life to maximum.
     * @param n &emsp; int
     */
    public void addLife(final int n) {
        this.life += n;
    }

    /**
     * Removes n life from maximum.
     * @param n &emsp; int
     */
    public void removeLife(final int n) {
        this.life -= n;
    }

    // Credits
    /**
     * Adds the credits.
     * 
     * @param c
     *            the c
     */
    public void addCredits(final long c) {
        this.setCredits(this.getCredits() + c);
    }

    /**
     * Subtract credits.
     * 
     * @param c
     *            the c
     */
    public void subtractCredits(final long c) {
        this.setCredits(this.getCredits() > c ? this.getCredits() - c : 0);
    }

    /**
     * Gets the credits.
     * 
     * @return the credits
     */
    public long getCredits() {
        return this.credits;
    }

    // Quest mode
    /**
     * Checks if is fantasy.
     * 
     * @return true, if is fantasy
     */
    public boolean isFantasy() {
        return this.mode.equals(QuestData.FANTASY);
    }

    /**
     * Gets the mode.
     * 
     * @return the mode
     */
    public String getMode() {
        return this.mode == null ? "" : this.mode;
    }

    // Difficulty
    /**
     * Gets the difficulty.
     * 
     * @return the difficulty
     */
    public String getDifficulty() {
        return this.difficulty;
    }

    /**
     * Gets the difficulty index.
     * 
     * @return the difficulty index
     */
    public int getDifficultyIndex() {
        return this.diffIndex;
    }

    /**
     * Sets the difficulty.
     * 
     * @param i
     *            the new difficulty
     */
    public void setDifficulty(final int i) {
        this.diffIndex = i;
        this.difficulty = QuestPreferences.getDifficulty(i);
    }

    // Level, read-only ( note: it increments in addWin() )
    /**
     * Gets the level.
     * 
     * @return the level
     */
    public int getLevel() {
        return this.rankIndex;
    }

    /**
     * Gets the rank.
     * 
     * @return the rank
     */
    public String getRank() {
        if (this.rankIndex >= QuestData.RANK_TITLES.length) {
            this.rankIndex = QuestData.RANK_TITLES.length - 1;
        }
        return QuestData.RANK_TITLES[this.rankIndex];
    }

    /** @return int */
    public int getWinStreakBest() {
        return this.winstreakBest;
    }

    /** @return int */
    public int getWinStreakCurrent() {
        return this.winstreakCurrent;
    }
    // decks management


    // randomizer - related
    /**
     * Gets the random seed.
     * 
     * @return the random seed
     */
    public long getRandomSeed() {
        return this.randomSeed;
    }

    /**
     * This method should be called whenever the opponents should change.
     */
    public void randomizeOpponents() {
        this.randomSeed = MyRandom.getRandom().nextLong();
    }

    // SERIALIZATION - related things

    // This must be called by XML-serializer via reflection
    /**
     * Read resolve.
     * 
     * @return the object
     */
    public Object readResolve() {
        this.initTransients();
        return this;
    }

    /**
     * Save data.
     */
    public void saveData() {
        QuestDataIO.saveData(this);
    }

    /**
     * Gets the card pool.
     * 
     * @return the cardPool
     */
    public ItemPool<CardPrinted> getCardPool() {
        return this.cardPool;
    }


    /**
     * Gets the shop list.
     * 
     * @return the shopList
     */
    public ItemPool<InventoryItem> getShopList() {
        return this.shopList;
    }

    /**
     * Sets the shop list.
     * 
     * @param shopList0
     *            the shopList to set
     */
    public void setShopList(final ItemPool<InventoryItem> shopList0) {
        this.shopList = shopList0;
    }

    /**
     * Gets the new card list.
     * 
     * @return the newCardList
     */
    public ItemPool<InventoryItem> getNewCardList() {
        return this.newCardList;
    }

    /**
     * Sets the new card list.
     * 
     * @param newCardList0
     *            the newCardList to set
     */
    public void setNewCardList(final ItemPool<InventoryItem> newCardList0) {
        this.newCardList = newCardList0;
    }

    /**
     * Gets the my decks.
     * 
     * @return the myDecks
     */
    public IFolderMap<Deck> getMyDecks() {
        return this.decks;
    }


    /** @return QuestPreconManager */
    public static IFolderMapView<PreconDeck> getPrecons() {
        return preconManager;
    }

    /**
     * Sets the inventory.
     * 
     * @param inventory0
     *            the inventory to set
     */
    public void setInventory(final QuestInventory inventory0) {
        this.inventory = inventory0;
    }

    /**
     * Sets the credits.
     * 
     * @param credits0
     *            the credits to set
     */
    public void setCredits(final long credits0) {
        this.credits = credits0;
    }

    /**
     * Gets the version number.
     * 
     * @return the versionNumber
     */
    public int getVersionNumber() {
        return this.versionNumber;
    }

    /**
     * Sets the version number.
     * 
     * @param versionNumber0
     *            the versionNumber to set
     */
    public void setVersionNumber(final int versionNumber0) {
        this.versionNumber = versionNumber0;
    }

    /** @param s0 &emsp; {@link java.lang.String} */
    public void setName(String s0) {
        this.name = s0;
    }

    /** @return {@link java.lang.String} */
    public String getName() {
        return this.name;
    }
}
