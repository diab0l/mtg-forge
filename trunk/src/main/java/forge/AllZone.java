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
package forge;

import java.util.Arrays;
import java.util.List;

import net.slightlymagic.braids.util.UtilFunctions;
import forge.Constant.Zone;
import forge.card.cardfactory.CardFactoryInterface;
import forge.card.cardfactory.PreloadingCardFactory;
import forge.card.replacement.ReplacementHandler;
import forge.card.trigger.TriggerHandler;
import forge.deck.DeckManager;
import forge.game.limited.CardRatings;
import forge.gui.input.InputControl;
import forge.model.FGameState;
import forge.model.FMatchState;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.quest.data.QuestData;
import forge.quest.data.QuestEvent;
import forge.quest.data.QuestEventManager;

/**
 * Please use public getters and setters instead of direct field access.
 * <p/>
 * If you need a setter, by all means, add it.
 * 
 * @author Forge
 * @version $Id$
 */
public final class AllZone {
    // only for testing, should read decks from local directory
    // public static final IO IO = new IO("all-decks");

    /**
     * Do not instantiate.
     */
    private AllZone() {
        // blank
    }

    /** Global <code>questData</code>. */
    private static forge.quest.data.QuestData questData = null;

    /** Global <code>QuestChallenge</code>. */
    private static QuestEvent questEvent = null;

    /** Global <code>questEventManager</code>. */
    private static QuestEventManager questEventManager = null;

    /** Constant <code>NAME_CHANGER</code>. */
    private static final NameChanger NAME_CHANGER = new NameChanger();

    /** Constant <code>COLOR_CHANGER</code>. */
    private static final ColorChanger COLOR_CHANGER = new ColorChanger();

    // Phase is now a prerequisite for CardFactory
    /** Global <code>cardFactory</code>. */
    private static CardFactoryInterface cardFactory = null;

    /** Constant <code>inputControl</code>. */
    private static InputControl inputControl = null;

    /** */
    private static FMatchState matchState = new FMatchState();

    // initialized at Runtime since it has to be the last object constructed

    // shared between Input_Attack, Input_Block, Input_CombatDamage ,
    // InputState_Computer

    /** Constant <code>DECK_MGR</code>. */
    private static DeckManager deckManager;

    /** Constant <code>CARD_RATINGS</code>. */
    private static CardRatings cardRatings = new CardRatings();

    /**
     * <p>
     * getHumanPlayer.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.Player} object.
     * @since 1.0.15
     */
    public static Player getHumanPlayer() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getHumanPlayer();
        }

        return null;
    }

    /**
     * <p>
     * getComputerPlayer.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.Player} object.
     * @since 1.0.15
     */
    public static Player getComputerPlayer() {
        return Singletons.getModel().getGameState().getComputerPlayer();
    }

    /**
     * get a list of all players participating in this game.
     * 
     * @return a list of all player participating in this game
     */
    public static List<Player> getPlayersInGame() {
        return Arrays.asList(Singletons.getModel().getGameState().getPlayers());
    }

    /**
     * <p>
     * getQuestData.
     * </p>
     * 
     * @return a {@link forge.quest.data.QuestData} object.
     * @since 1.0.15
     */
    public static forge.quest.data.QuestData getQuestData() {
        return AllZone.questData;
    }

    /**
     * <p>
     * setQuestData.
     * </p>
     * 
     * @param questData0
     *            a {@link forge.quest.data.QuestData} object.
     * @since 1.0.15
     */
    public static void setQuestData(final QuestData questData0) {
        AllZone.questData = questData0;
    }

    /**
     * <p>
     * getQuestEvent.
     * </p>
     * 
     * @return a {@link forge.quest.data.QuestEvent} object.
     * @since 1.0.15
     */
    public static QuestEvent getQuestEvent() {
        return AllZone.questEvent;
    }

    /**
     * <p>
     * setQuestEvent.
     * </p>
     * 
     * @param q
     *            a {@link forge.quest.data.QuestEvent} object.
     */
    public static void setQuestEvent(final QuestEvent q) {
        AllZone.questEvent = q;
    }

    /**
     * <p>
     * getQuestEventManager.
     * </p>
     * 
     * @return {@link forge.quest.data.QuestEventManager} object.
     * @since 1.0.15
     */
    public static QuestEventManager getQuestEventManager() {
        return AllZone.questEventManager;
    }

    /**
     * <p>
     * setQuestEventManager.
     * </p>
     * 
     * @param qem
     *            a {@link forge.quest.data.QuestEventManager} object
     */
    public static void setQuestEventManager(final QuestEventManager qem) {
        AllZone.questEventManager = qem;
    }

    /**
     * <p>
     * getNameChanger.
     * </p>
     * 
     * @return a {@link forge.NameChanger} object.
     * @since 1.0.15
     */
    public static NameChanger getNameChanger() {
        return AllZone.NAME_CHANGER;
    }

    /**
     * <p>
     * getEndOfTurn.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.EndOfTurn} object.
     * @since 1.0.15
     */
    public static EndOfTurn getEndOfTurn() {
        return Singletons.getModel().getGameState().getEndOfTurn();
    }

    /**
     * <p>
     * getEndOfCombat.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.EndOfCombat} object.
     * @since 1.0.15
     */
    public static forge.EndOfCombat getEndOfCombat() {
        return Singletons.getModel().getGameState().getEndOfCombat();
    }

    /**
     * <p>
     * getUpkeep.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.EndOfCombat} object.
     * @since 1.0.16
     */
    public static forge.Upkeep getUpkeep() {
        return Singletons.getModel().getGameState().getUpkeep();
    }

    /**
     * <p>
     * getUpkeep.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.Untap} object.
     * @since 1.2.0
     */
    public static forge.Untap getUntap() {
        return Singletons.getModel().getGameState().getUntap();
    }

    /**
     * <p>
     * getPhaseHandler.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.PhaseHandler} object; may be null.
     * @since 1.0.15
     */
    public static PhaseHandler getPhaseHandler() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getPhaseHandler();
        }

        return null;
    }

    /**
     * <p>
     * getGameLog.
     * </p>
     * 
     * @return a {@link forge.GameLog} object; may be null.
     * @since 1.2.0
     */
    public static GameLog getGameLog() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getGameLog();
        }

        return null;
    }

    /**
     * <p>
     * getCardFactory.
     * </p>
     * 
     * @return a {@link forge.card.cardfactory.CardFactoryInterface} object.
     * @since 1.0.15
     */
    public static CardFactoryInterface getCardFactory() {
        if (AllZone.cardFactory == null) {
            // setCardFactory(new
            // LazyCardFactory(ForgeProps.getFile(CARDSFOLDER)));
            AllZone.setCardFactory(new PreloadingCardFactory(ForgeProps.getFile(NewConstants.CARDSFOLDER)));
        }
        return AllZone.cardFactory;
    }

    /**
     * Setter for cardFactory.
     * 
     * @param factory
     *            the factory to set
     */
    public static void setCardFactory(final CardFactoryInterface factory) {
        UtilFunctions.checkNotNull("factory", factory);
        AllZone.cardFactory = factory;
    }

    /**
     * <p>
     * getStack.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.MagicStack} object.
     * @since 1.0.15
     */
    public static MagicStack getStack() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getStack();
        }

        return null;
    }

    /**
     * <p>
     * getInputControl.
     * </p>
     * 
     * @return a {@link forge.gui.input.InputControl} object.
     * @since 1.0.15
     */
    public static InputControl getInputControl() {
        return AllZone.inputControl;
    }

    /** @param i0 &emsp; {@link forge.gui.input.InputControl} */
    public static void setInputControl(InputControl i0) {
        AllZone.inputControl = i0;
    }

    /**
     * <p>
     * getGameAction.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.GameAction} object.
     * @since 1.0.15
     */
    public static GameAction getGameAction() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getGameAction();
        }

        return null;
    }

    /**
     * <p>
     * getStaticEffects.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.StaticEffects} object.
     * @since 1.0.15
     */
    public static StaticEffects getStaticEffects() {
        final FGameState gameState = Singletons.getModel().getGameState();

        if (gameState != null) {
            return gameState.getStaticEffects();
        }

        return null;
    }

    /**
     * <p>
     * getTriggerHandler.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.card.trigger.TriggerHandler} object.
     * @since 1.0.15
     */
    public static TriggerHandler getTriggerHandler() {
        return Singletons.getModel().getGameState().getTriggerHandler();
    }

    /**
     * Gets the replacement handler.
     *
     * @return the replacement handler
     */
    public static ReplacementHandler getReplacementHandler() {
        return Singletons.getModel().getGameState().getReplacementHandler();
    }

    /**
     * <p>
     * getCombat.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.Combat} object.
     * @since 1.0.15
     */
    public static Combat getCombat() {
        return Singletons.getModel().getGameState().getCombat();
    }

    /**
     * <p>
     * setCombat.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @param attackers
     *            a {@link forge.Combat} object.
     * @since 1.0.15
     */
    public static void setCombat(final Combat attackers) {
        Singletons.getModel().getGameState().setCombat(attackers);
    }

    /**
     * <p>
     * getStackZone.
     * </p>
     * 
     * Will eventually be marked deprecated.
     * 
     * @return a {@link forge.PlayerZone} object.
     * @since 1.0.15
     */
    public static PlayerZone getStackZone() {
        return Singletons.getModel().getGameState().getStackZone();
    }

    /**
     * <p>
     * getZone.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link forge.PlayerZone} object.
     */
    public static PlayerZone getZoneOf(final Card c) {
        final FGameState gameState = Singletons.getModel().getGameState();
        if (gameState == null) {
            return null;
        }

        if (gameState.getStackZone().contains(c)) {
            return gameState.getStackZone();
        }

        for (final Player p : gameState.getPlayers()) {
            for (final Zone z : Player.ALL_ZONES) {
                final PlayerZone pz = p.getZone(z);
                if (pz.contains(c)) {
                    return pz;
                }
            }
        }

        return null;
    }

    /**
     * 
     * isCardInZone.
     * 
     * @param c
     *            Card
     * @param zone
     *            Constant.Zone
     * @return boolean
     */
    public static boolean isCardInZone(final Card c, final Constant.Zone zone) {
        final FGameState gameState = Singletons.getModel().getGameState();
        if (gameState == null) {
            return false;
        }

        if (zone.equals(Constant.Zone.Stack)) {
            if (gameState.getStackZone().contains(c)) {
                return true;
            }
        } else {
            for (final Player p : gameState.getPlayers()) {
                if (p.getZone(zone).contains(c)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * <p>
     * resetZoneMoveTracking.
     * </p>
     */
    public static void resetZoneMoveTracking() {
        final FGameState gameState = Singletons.getModel().getGameState();
        if (gameState == null) {
            return;
        }
        for (final Player p : gameState.getPlayers()) {
            for (final Zone z : Player.ALL_ZONES) {
                p.getZone(z).resetCardsAddedThisTurn();
            }
        }
    }

    /**
     * <p>
     * getDeckManager.
     * </p>
     * 
     * @return dMgr
     */
    public static DeckManager getDeckManager() {
        if (AllZone.deckManager == null) {
            AllZone.deckManager = new DeckManager(ForgeProps.getFile(NewConstants.NEW_DECKS));
        }
        return AllZone.deckManager;
    }

    /**
     * Create and return the next timestamp.
     * 
     * Will eventually be marked deprecated.
     * 
     * @return the next timestamp
     */
    public static long getNextTimestamp() {
        return Singletons.getModel().getGameState().getNextTimestamp();
    }

    /**
     * <p>
     * Resets everything possible to set a new game.
     * </p>
     */
    public static void newGameCleanup() {
        Singletons.getModel().getGameState().newGameCleanup();

        Singletons.getControl().getMatchControl().showCombat("");
        Singletons.getModel().loadPrefs();
        AllZone.getInputControl().clearInput();
        AllZone.getColorChanger().reset();
        Singletons.getControl().getMatchControl().showStack();
    }

    /**
     * Getter for matchState.
     * 
     * @return the matchState
     */
    public static FMatchState getMatchState() {
        return AllZone.matchState;
    }

    /**
     * Getter for colorChanger.
     * 
     * @return the colorChanger
     */
    public static ColorChanger getColorChanger() {
        return AllZone.COLOR_CHANGER;
    }

    /**
     *  Gets the CardRatings object.
     * 
     * @return the CardRatings object
     */
    public static CardRatings getCardRatings() {
        return AllZone.cardRatings;
    }

} // AllZone
