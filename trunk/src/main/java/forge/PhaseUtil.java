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

import forge.Constant.Zone;
import forge.view.GuiTopLevel;
import forge.view.match.ViewField.PhaseLabel;
import forge.view.match.ViewTopLevel;

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
        final Player turn = AllZone.getPhaseHandler().getPlayerTurn();

        AllZone.getPhaseHandler().turnReset();
        AllZone.getGameInfo().notifyNextTurn();

        AllZone.getCombat().reset();
        AllZone.getCombat().setAttackingPlayer(turn);
        AllZone.getCombat().setDefendingPlayer(turn.getOpponent());

        // For tokens a player starts the game with they don't recover from Sum.
        // Sickness on first turn
        if (turn.getTurn() > 0) {
            final CardList list = turn.getCardsIncludePhasingIn(Zone.Battlefield);
            for (final Card c : list) {
                c.setSickness(false);
            }
        }
        turn.incrementTurn();

        AllZone.getGameAction().resetActivationsPerTurn();

        final CardList lands = AllZoneUtil.getPlayerLandsInPlay(turn).filter(CardListFilter.UNTAPPED);
        turn.setNumPowerSurgeLands(lands.size());

        // anything before this point happens regardless of whether the Untap
        // phase is skipped

        if (PhaseUtil.skipUntap(turn)) {
            AllZone.getPhaseHandler().setNeedToNextPhase(true);
            return;
        }

        AllZone.getUntap().executeUntil(turn);
        AllZone.getUntap().executeAt();

        // otherwise land seems to stay tapped when it is really untapped
        AllZone.getHumanPlayer().getZone(Zone.Battlefield).updateObservers();

        AllZone.getPhaseHandler().setNeedToNextPhase(true);
    }

    // ******* UPKEEP PHASE *****
    /**
     * <p>
     * handleUpkeep.
     * </p>
     */
    public static void handleUpkeep() {
        final Player turn = AllZone.getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipUpkeep()) {
            // Slowtrips all say "on the next turn's upkeep" if there is no
            // upkeep next turn, the trigger will never occur.
            turn.clearSlowtripList();
            turn.getOpponent().clearSlowtripList();
            AllZone.getPhaseHandler().setNeedToNextPhase(true);
            return;
        }

        AllZone.getUpkeep().executeUntil(turn);
        AllZone.getUpkeep().executeAt();
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

        final Player turn = AllZone.getPhaseHandler().getPlayerTurn();

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
        final Player playerTurn = AllZone.getPhaseHandler().getPlayerTurn();

        if (PhaseUtil.skipDraw(playerTurn)) {
            AllZone.getPhaseHandler().setNeedToNextPhase(true);
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
        if (AllZone.getPhaseHandler().getTurn() == 1) {
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
        CombatUtil.showCombat();
    }

    /**
     * <p>
     * handleDeclareAttackers.
     * </p>
     */
    public static void handleDeclareAttackers() {
        PhaseUtil.verifyCombat();
        final CardList list = new CardList();
        list.addAll(AllZone.getCombat().getAttackers());

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
        final CardList list = new CardList();
        list.addAll(AllZone.getCombat().getAttackers());
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
        AllZone.getTriggerHandler().runTrigger("AttackersDeclared", runParams);

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
                return !c.getCreatureBlockedThisCombat();
            }
        });

        final CardList attList = new CardList();
        attList.addAll(AllZone.getCombat().getAttackers());

        CombatUtil.checkDeclareBlockers(list);

        for (final Card a : attList) {
            final CardList blockList = AllZone.getCombat().getBlockers(a);
            for (final Card b : blockList) {
                CombatUtil.checkBlockedAttackers(a, b);
            }
        }

        AllZone.getStack().unfreezeStack();

        AllZone.getGameLog().add("Combat", CombatUtil.getCombatBlockForLog(), 1);
        CombatUtil.showCombat();
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
        final String phase = AllZone.getPhaseHandler().getPhase();
        return phase.equals(Constant.Phase.UNTAP) || phase.equals(Constant.Phase.UPKEEP)
                || phase.equals(Constant.Phase.DRAW) || phase.equals(Constant.Phase.MAIN1)
                || phase.equals(Constant.Phase.COMBAT_BEGIN);
    }

    /**
     * Retrieves and visually activates phase label for appropriate phase and
     * player.
     * 
     * @param s
     *            &emsp; Phase state
     */
    public static void visuallyActivatePhase(final String s) {
        PhaseLabel lbl = null;
        final Player p = AllZone.getPhaseHandler().getPlayerTurn();
        final ViewTopLevel t = ((GuiTopLevel) AllZone.getDisplay()).getController().getMatchController().getView();

        int i; // Index of field; computer is 0, human is 1
        if (p.isComputer()) {
            i = 0;
        } else {
            i = 1;
        }

        if (s.equals(Constant.Phase.UPKEEP)) {
            lbl = t.getFieldControllers().get(i).getView().getLblUpkeep();
        } else if (s.equals(Constant.Phase.DRAW)) {
            lbl = t.getFieldControllers().get(i).getView().getLblDraw();
        } else if (s.equals(Constant.Phase.MAIN1)) {
            lbl = t.getFieldControllers().get(i).getView().getLblMain1();
        } else if (s.equals(Constant.Phase.COMBAT_BEGIN)) {
            lbl = t.getFieldControllers().get(i).getView().getLblBeginCombat();
        } else if (s.equals(Constant.Phase.COMBAT_DECLARE_ATTACKERS)) {
            lbl = t.getFieldControllers().get(i).getView().getLblDeclareAttackers();
        } else if (s.equals(Constant.Phase.COMBAT_DECLARE_BLOCKERS)) {
            lbl = t.getFieldControllers().get(i).getView().getLblDeclareBlockers();
        } else if (s.equals(Constant.Phase.COMBAT_DAMAGE)) {
            lbl = t.getFieldControllers().get(i).getView().getLblCombatDamage();
        } else if (s.equals(Constant.Phase.COMBAT_FIRST_STRIKE_DAMAGE)) {
            lbl = t.getFieldControllers().get(i).getView().getLblFirstStrike();
        } else if (s.equals(Constant.Phase.COMBAT_END)) {
            lbl = t.getFieldControllers().get(i).getView().getLblEndCombat();
        } else if (s.equals(Constant.Phase.MAIN2)) {
            lbl = t.getFieldControllers().get(i).getView().getLblMain2();
        } else if (s.equals(Constant.Phase.END_OF_TURN)) {
            lbl = t.getFieldControllers().get(i).getView().getLblEndTurn();
        } else if (s.equals(Constant.Phase.CLEANUP)) {
            lbl = t.getFieldControllers().get(i).getView().getLblCleanup();
        } else {
            return;
        }

        t.getController().resetAllPhaseButtons();
            // Could be a potential recursion bug here, but I checked and hopefully there isn't.
            // Please remove this comments if > 1 week and no problems. Doublestrike 12-01-11
            t.repaint();
            // End potential recursion bug
        lbl.setActive(true);
    }
}
