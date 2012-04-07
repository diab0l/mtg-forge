/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.Singletons;
import forge.deck.Deck;
import forge.item.CardPrinted;
import forge.item.PreconDeck;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.quest.bazaar.QuestBazaarManager;
import forge.quest.bazaar.QuestPetStorage;
import forge.quest.data.QuestAchievements;
import forge.quest.data.QuestAssets;
import forge.quest.data.QuestData;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.io.PreconReader;
import forge.util.IStorage;
import forge.util.IStorageView;
import forge.util.StorageView;
import forge.util.closures.Predicate;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public class QuestController {

    private QuestData model;
    // gadgets

    // Utility class to access cards, has access to private fields
    // Moved some methods there that otherwise would make this class even more
    // complex
    private QuestUtilCards myCards;

    private QuestEvent currentEvent;

    /** The decks. */
    transient IStorage<Deck> decks;

    // acquired
    // since
    // last
    // game-win/loss

    /** The available challenges. */
    private List<Integer> availableChallenges = new ArrayList<Integer>();

    /** The available quests. */
    private List<Integer> availableQuests = null;

    private QuestEventManager duelManager = null;
    private QuestEventManager challengesManager = null;

    private QuestBazaarManager bazaar = null;

    private QuestPetStorage pets = null;

    // This is used by shop. Had no idea where else to place this
    private static transient IStorageView<PreconDeck> preconManager = new StorageView<PreconDeck>(new PreconReader(
            ForgeProps.getFile(NewConstants.Quest.PRECONS)));

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

    public static final int MAX_PET_SLOTS = 2;

    private Map<Integer, String> selectedPets = new HashMap<Integer, String>();

    public void selectPet(Integer slot, String name) {
        selectedPets.put(slot, name);
    }

    public String getSelectedPet(Integer slot) {
        return selectedPets.get(slot);
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

    /**
     * Gets the my decks.
     * 
     * @return the myDecks
     */
    public IStorage<Deck> getMyDecks() {
        return this.decks;
    }

    /**
     * Gets the current event.
     *
     * @return the current event
     */
    public QuestEvent getCurrentEvent() {
        return this.currentEvent;
    }

    /**
     * Sets the current event.
     *
     * @param currentEvent the new current event
     */
    public void setCurrentEvent(final QuestEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * Gets the precons.
     * 
     * @return QuestPreconManager
     */
    public static IStorageView<PreconDeck> getPrecons() {
        return QuestController.preconManager;
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @param selectedQuest the selected quest
     */
    public void load(final QuestData selectedQuest) {
        this.model = selectedQuest;
        // These are helper classes that hold no data.
        this.decks = this.model == null ? null : this.model.getAssets().getDeckStorage();
        this.myCards = this.model == null ? null : new QuestUtilCards(this);
        this.currentEvent = null;

        this.getChallengesManager().randomizeOpponents();
        this.getDuelsManager().randomizeOpponents();
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void save() {
        if (this.model != null) {
            this.model.saveData();
        }
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return true, if is loaded
     */
    public boolean isLoaded() {
        return false;
    }

    /**
     * Clear available challenges.
     */
    public void clearAvailableChallenges() {
        this.availableChallenges.clear();
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
     * New game.
     *
     * @param name the name
     * @param diff the diff
     * @param mode the mode
     * @param startPool the start type
     * @param preconName the precon name
     */
    public void newGame(final String name, final int diff, final QuestMode mode, final QuestStartPool startPool,
            final String preconName) {

        this.load(new QuestData(name, diff, mode));

        final Predicate<CardPrinted> filter;
        switch (startPool) {
        case Precon:
            this.myCards.addPreconDeck(QuestController.preconManager.get(preconName));
            return;

        case Standard:
            filter = Singletons.getModel().getFormats().getStandard().getFilterPrinted();
            break;

        default: // Unrestricted
            filter = CardPrinted.Predicates.Presets.IS_TRUE;
            break;
        }

        this.getAssets().setCredits(
                Singletons.getModel().getQuestPreferences().getPreferenceInt(QPref.STARTING_CREDITS, diff));
        this.myCards.setupNewGameCardPool(filter, diff);
    }

    /**
     * Gets the rank.
     * 
     * @return the rank
     */
    public String getRank() {
        int level = this.model.getAchievements().getLevel();
        if (level >= QuestController.RANK_TITLES.length) {
            level = QuestController.RANK_TITLES.length - 1;
        }
        return QuestController.RANK_TITLES[level];
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return the assets
     */
    public QuestAssets getAssets() {
        return this.model == null ? null : this.model.getAssets();
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return the name
     */
    public String getName() {
        return this.model == null ? null : this.model.getName();
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return the achievements
     */
    public QuestAchievements getAchievements() {
        return this.model == null ? null : this.model.getAchievements();
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return the mode
     */
    public QuestMode getMode() {
        return this.model.getMode();
    }

    /**
     * Gets the bazaar.
     *
     * @return the bazaar
     */
    public final QuestBazaarManager getBazaar() {
        if (null == this.bazaar) {
            this.bazaar = new QuestBazaarManager(ForgeProps.getFile(NewConstants.Quest.BAZAAR));
        }
        return this.bazaar;
    }

    /**
     * Gets the event manager.
     *
     * @return the event manager
     */
    public QuestEventManager getDuelsManager() {
        if (this.duelManager == null) {
            this.duelManager = new QuestEventManager(ForgeProps.getFile(NewConstants.Quest.DUELS));
        }
        return this.duelManager;
    }

    public QuestEventManager getChallengesManager() {
        if (this.challengesManager == null) {
            this.challengesManager = new QuestEventManager(ForgeProps.getFile(NewConstants.Quest.CHALLENGES));
        }
        return this.challengesManager;
    }

    public QuestPetStorage getPetsStorage() {
        if (this.pets == null) {
            this.pets = new QuestPetStorage(ForgeProps.getFile(NewConstants.Quest.BAZAAR));
        }

        return this.pets;
    }

}
