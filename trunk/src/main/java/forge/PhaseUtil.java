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

import java.util.HashMap;

import forge.PhaseType;
import forge.Constant.Zone;
import forge.card.trigger.TriggerType;
import forge.control.ControlMatchUI;
import forge.view.match.ViewField.PhaseLabel;

/**
 * <p>
 * PhaseUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class PhaseUtil {
    // ******* UNTAP PHASE *****
    /**
     * <p>
     * skipUntap.
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    private static boolean skipUntap(final Player p) {

        if (p.hasKeyword("Skip your next untap step.")) {
            p.removeKeyword("Skip your next untap step.");
            return true;
        }

        if (AllZoneUtil.isCardInPlay("Sands of Time") || AllZoneUtil.isCardInPlay("Stasis")) {
            return true;
        }

        if (p.skipNextUntap()) {
            p.setSkipNextUntap(false);
            return true;
        }

        return false;
    }

    /**
     * <p>
     * handleUntap.
     * </p>
     */
    public static void handleUntap() {
        final Player turn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        Singletons.getModel().getGameState().getPhaseHandler().turnReset();
        Singletons.getModel().getGameSummary().notifyNextTurn();

        AllZone.getCombat().reset();
        AllZone.getCombat().setAttackingPlayer(turn);
        AllZone.getCombat().setDefendingPlayer(turn.getOpponent());

        // For tokens a player starts the game with they don't recover from Sum.
        // Sickness on first turn
        if (Singletons.getModel().getGameState().getPhaseHandler().getTurn() > 0) {
            final CardList list = turn.getCardsIncludePhasingIn(Zone.Battlefield);
            for (final Card c : list) {
                c.setSickness(false);
            }
        }
        turn.incrementTurn();

        Singletons.getModel().getGameAction().resetActivationsPerTurn();

        final CardList lands = AllZoneUtil.getPlayerLandsInPlay(turn).filter(CardListFilter.UNTAPPED);
        turn.setNumPowerSurgeLands(lands.size());

        // anything before this point happens regardless of whether the Untap
        // phase is skipped

        if (PhaseUtil.skipUntap(turn)) {
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            return;
        }

        Singletons.getModel().getGameState().getUntap().executeUntil(turn);
        Singletons.getModel().getGameState().getUntap().executeAt();

        // otherwise land seems to stay tapped when it is really untapped
        AllZone.getHumanPlayer().getZone(Zone.Battlefield).updateObservers();

        Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
    }

    // ******* UPKEEP PHASE *****
    /**
     * <p>
     * handleUpkeep.
     * </p>
     */
    public static void handleUpkeep() {
        final Player turn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipUpkeep()) {
            // Slowtrips all say "on the next turn's upkeep" if there is no
            // upkeep next turn, the trigger will never occur.
            turn.clearSlowtripList();
            turn.getOpponent().clearSlowtripList();
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            return;
        }

        Singletons.getModel().getGameState().getUpkeep().executeUntil(turn);
        Singletons.getModel().getGameState().getUpkeep().executeAt();
    }

    /**
     * <p>
     * skipUpkeep.
     * </p>
     * 
     * @return a boolean.
     */
    public static boolean skipUpkeep() {
        if (AllZoneUtil.isCardInPlay("Eon Hub")) {
            return true;
        }

        final Player turn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        if ((turn.getCardsIn(Zone.Hand).size() == 0) && AllZoneUtil.isCardInPlay("Gibbering Descent", turn)) {
            return true;
        }

        return false;
    }

    // ******* DRAW PHASE *****
    /**
     * <p>
     * handleDraw.
     * </p>
     */
    public static void handleDraw() {
        final Player playerTurn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipDraw(playerTurn)) {
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            return;
        }

        playerTurn.drawCards(1, true);
    }

    /**
     * <p>
     * skipDraw.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    private static boolean skipDraw(final Player player) {
        // starting player skips his draw
        if (Singletons.getModel().getGameState().getPhaseHandler().getTurn() == 1) {
            return true;
        }

        if (player.hasKeyword("Skip your next draw step.")) {
            player.removeKeyword("Skip your next draw step.");
            return true;
        }

        if (player.hasKeyword("Skip your draw step.")) {
            return true;
        }

        return false;
    }

    // ********* Declare Attackers ***********

    /**
     * <p>
     * verifyCombat.
     * </p>
     */
    public static void verifyCombat() {
        AllZone.getCombat().verifyCreaturesInPlay();
    }

    /**
     * <p>
     * handleCombatBegin.
     * </p>
     */
    public static void handleCombatBegin() {
        final Player playerTurn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipCombat(playerTurn)) {
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            return;
        }
    }

    /**
     * <p>
     * handleCombatDeclareAttackers.
     * </p>
     */
    public static void handleCombatDeclareAttackers() {
        final Player playerTurn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipCombat(playerTurn)) {
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            playerTurn.removeKeyword("Skip your next combat phase.");
            return;
        }
    }

    /**
     * <p>
     * skipCombat.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    private static boolean skipCombat(final Player player) {

        if (player.hasKeyword("Skip your next combat phase.")) {
            return true;
        }

        if (player.hasKeyword("Skip your combat phase.")) {
            return true;
        }

        return false;
    }

    /**
     * <p>
     * handleDeclareAttackers.
     * </p>
     */
    public static void handleDeclareAttackers() {
        PhaseUtil.verifyCombat();
        final CardList list = AllZone.getCombat().getAttackerList();

        // TODO move propaganda to happen as the Attacker is Declared
        // Remove illegal Propaganda attacks first only for attacking the Player

        final int size = list.size();
        for (int i = 0; i < size; i++) {
            final Card c = list.get(i);
            final boolean last = (i == (size - 1));
            CombatUtil.checkPropagandaEffects(c, last);
        }
    }

    /**
     * <p>
     * handleAttackingTriggers.
     * </p>
     */
    public static void handleAttackingTriggers() {
        final CardList list = AllZone.getCombat().getAttackerList();
        AllZone.getStack().freezeStack();
        // Then run other Attacker bonuses
        // check for exalted:
        if (list.size() == 1) {
            final Player attackingPlayer = AllZone.getCombat().getAttackingPlayer();

            CardList exalted = attackingPlayer.getCardsIn(Zone.Battlefield);
            exalted = exalted.getKeyword("Exalted");

            if (exalted.size() > 0) {
                CombatUtil.executeExaltedAbility(list.get(0), exalted.size());
                // Make sure exalted effects get applied only once per combat
            }

        }

        AllZone.getGameLog().add("Combat", CombatUtil.getCombatAttackForLog(), 1);

        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Attackers", list);
        runParams.put("AttackingPlayer", AllZone.getCombat().getAttackingPlayer());
        AllZone.getTriggerHandler().runTrigger(TriggerType.AttackersDeclared, runParams);

        for (final Card c : list) {
            CombatUtil.checkDeclareAttackers(c);
        }
        AllZone.getStack().unfreezeStack();
    }

    /**
     * <p>
     * handleDeclareBlockers.
     * </p>
     */
    public static void handleDeclareBlockers() {
        PhaseUtil.verifyCombat();

        AllZone.getStack().freezeStack();

        AllZone.getCombat().setUnblocked();

        CardList list = new CardList();
        list.addAll(AllZone.getCombat().getAllBlockers());

        list = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return !c.getDamageHistory().getCreatureBlockedThisCombat();
            }
        });

        final CardList attList = AllZone.getCombat().getAttackerList();

        CombatUtil.checkDeclareBlockers(list);

        for (final Card a : attList) {
            final CardList blockList = AllZone.getCombat().getBlockers(a);
            for (final Card b : blockList) {
                CombatUtil.checkBlockedAttackers(a, b);
            }
        }

        AllZone.getStack().unfreezeStack();

        AllZone.getGameLog().add("Combat", CombatUtil.getCombatBlockForLog(), 1);
    }

    // ***** Combat Utility **********
    // TODO: the below functions should be removed and the code blocks that use
    // them should instead use SpellAbilityRestriction
    /**
     * <p>
     * isBeforeAttackersAreDeclared.
     * </p>
     * 
     * @return a boolean.
     */
    public static boolean isBeforeAttackersAreDeclared() {
        final PhaseType phase = Singletons.getModel().getGameState().getPhaseHandler().getPhase();
        return phase.equals(PhaseType.UNTAP) || phase.equals(PhaseType.UPKEEP)
                || phase.equals(PhaseType.DRAW) || phase.equals(PhaseType.MAIN1)
                || phase.equals(PhaseType.COMBAT_BEGIN);
    }

    /**
     * Retrieves and visually activates phase label for appropriate phase and
     * player.
     * 
     * @param s
     *            &emsp; Phase state
     */
    public static void visuallyActivatePhase(final PhaseType s) {
        PhaseLabel lbl = null;
        final Player p = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();
        final ControlMatchUI t = Singletons.getControl().getControlMatch();

        int i; // Index of field; computer is 0, human is 1
        if (p.isComputer()) {
            i = 0;
        } else {
            i = 1;
        }

        if (s.equals(PhaseType.UPKEEP)) {
            lbl = t.getFieldControls().get(i).getView().getLblUpkeep();
        } else if (s.equals(PhaseType.DRAW)) {
            lbl = t.getFieldControls().get(i).getView().getLblDraw();
        } else if (s.equals(PhaseType.MAIN1)) {
            lbl = t.getFieldControls().get(i).getView().getLblMain1();
        } else if (s.equals(PhaseType.COMBAT_BEGIN)) {
            lbl = t.getFieldControls().get(i).getView().getLblBeginCombat();
        } else if (s.equals(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
            lbl = t.getFieldControls().get(i).getView().getLblDeclareAttackers();
        } else if (s.equals(PhaseType.COMBAT_DECLARE_BLOCKERS)) {
            lbl = t.getFieldControls().get(i).getView().getLblDeclareBlockers();
        } else if (s.equals(PhaseType.COMBAT_DAMAGE)) {
            lbl = t.getFieldControls().get(i).getView().getLblCombatDamage();
        } else if (s.equals(PhaseType.COMBAT_FIRST_STRIKE_DAMAGE)) {
            lbl = t.getFieldControls().get(i).getView().getLblFirstStrike();
        } else if (s.equals(PhaseType.COMBAT_END)) {
            lbl = t.getFieldControls().get(i).getView().getLblEndCombat();
        } else if (s.equals(PhaseType.MAIN2)) {
            lbl = t.getFieldControls().get(i).getView().getLblMain2();
        } else if (s.equals(PhaseType.END_OF_TURN)) {
            lbl = t.getFieldControls().get(i).getView().getLblEndTurn();
        } else if (s.equals(PhaseType.CLEANUP)) {
            lbl = t.getFieldControls().get(i).getView().getLblCleanup();
        } else {
            return;
        }

        t.resetAllPhaseButtons();
        Singletons.getView().getViewMatch().repaint();
        lbl.setActive(true);
    }
}
