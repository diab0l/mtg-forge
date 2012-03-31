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
import java.util.Observer;
import java.util.Stack;

import com.esotericsoftware.minlog.Log;

import forge.Constant.Zone;
import forge.card.trigger.TriggerType;

/**
 * <p>
 * Phase class.
 * </p>
 * 
 * @author Forge
 * @version $Id: PhaseHandler.java 13001 2012-01-08 12:25:25Z Sloth $
 */
public class PhaseHandler extends MyObservable implements java.io.Serializable {

    /** Constant <code>serialVersionUID=5207222278370963197L</code>. */
    private static final long serialVersionUID = 5207222278370963197L;

    private int phaseIndex;
    private int turn;

    // Please use getX, setX, and incrementX methods instead of directly
    // accessing the following:
    /** Constant <code>GameBegins=0</code>. */
    private static int gameBegins = 0;

    private final Stack<Player> extraTurns = new Stack<Player>();

    private int extraCombats;

    private int nCombatsThisTurn;
    private boolean bPreventCombatDamageThisTurn;

    private Player playerTurn = AllZone.getHumanPlayer();

    /**
     * <p>
     * isPlayerTurn.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    public final boolean isPlayerTurn(final Player player) {
        return this.playerTurn.isPlayer(player);
    }

    /**
     * <p>
     * Setter for the field <code>playerTurn</code>.
     * </p>
     * 
     * @param s
     *            a {@link forge.Player} object.
     */
    public final void setPlayerTurn(final Player s) {
        this.playerTurn = s;
    }

    /**
     * <p>
     * Getter for the field <code>playerTurn</code>.
     * </p>
     * 
     * @return a {@link forge.Player} object.
     */
    public final Player getPlayerTurn() {
        return this.playerTurn;
    }

    // priority player

    private Player pPlayerPriority = AllZone.getHumanPlayer();

    /**
     * <p>
     * getPriorityPlayer.
     * </p>
     * 
     * @return a {@link forge.Player} object.
     */
    public final Player getPriorityPlayer() {
        return this.pPlayerPriority;
    }

    /**
     * <p>
     * setPriorityPlayer.
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object.
     */
    public final void setPriorityPlayer(final Player p) {
        this.pPlayerPriority = p;
    }

    private Player pFirstPriority = AllZone.getHumanPlayer();

    /**
     * <p>
     * getFirstPriority.
     * </p>
     * 
     * @return a {@link forge.Player} object.
     */
    public final Player getFirstPriority() {
        return this.pFirstPriority;
    }

    /**
     * <p>
     * setFirstPriority.
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object.
     */
    public final void setFirstPriority(final Player p) {
        this.pFirstPriority = p;
    }

    /**
     * <p>
     * setPriority.
     * </p>
     * 
     * @param p
     *            a {@link forge.Player} object.
     */
    public final void setPriority(final Player p) {
        if (AllZone.getStack() != null) {
            AllZone.getStack().chooseOrderOfSimultaneousStackEntryAll();
        }

        this.pFirstPriority = p;
        this.pPlayerPriority = p;
    }

    /**
     * <p>
     * resetPriority.
     * </p>
     */
    public final void resetPriority() {
        this.setPriority(this.playerTurn);
    }

    private boolean bPhaseEffects = true;

    /**
     * <p>
     * doPhaseEffects.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean doPhaseEffects() {
        return this.bPhaseEffects;
    }

    /**
     * <p>
     * setPhaseEffects.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setPhaseEffects(final boolean b) {
        this.bPhaseEffects = b;
    }

    private boolean bSkipPhase = true;

    /**
     * <p>
     * doSkipPhase.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean doSkipPhase() {
        return this.bSkipPhase;
    }

    /**
     * <p>
     * setSkipPhase.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setSkipPhase(final boolean b) {
        this.bSkipPhase = b;
    }

    private boolean bCombat = false;

    /**
     * <p>
     * inCombat.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean inCombat() {
        return this.bCombat;
    }

    /**
     * <p>
     * setCombat.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCombat(final boolean b) {
        this.bCombat = b;
    }

    private boolean bRepeat = false;

    /**
     * <p>
     * repeatPhase.
     * </p>
     */
    public final void repeatPhase() {
        this.bRepeat = true;
    }

    /** The phase order. */
    private String[] phaseOrder = { Constant.Phase.UNTAP, Constant.Phase.UPKEEP, Constant.Phase.DRAW,
            Constant.Phase.MAIN1, Constant.Phase.COMBAT_BEGIN, Constant.Phase.COMBAT_DECLARE_ATTACKERS,
            Constant.Phase.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY, Constant.Phase.COMBAT_DECLARE_BLOCKERS,
            Constant.Phase.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY, Constant.Phase.COMBAT_FIRST_STRIKE_DAMAGE,
            Constant.Phase.COMBAT_DAMAGE, Constant.Phase.COMBAT_END, Constant.Phase.MAIN2, Constant.Phase.END_OF_TURN,
            Constant.Phase.CLEANUP };

    /**
     * <p>
     * Constructor for PhaseHandler.
     * </p>
     */
    public PhaseHandler() {
        this.reset();
    }

    /**
     * <p>
     * reset.
     * </p>
     */
    public final void reset() {
        this.turn = 1;
        this.playerTurn = AllZone.getHumanPlayer();
        this.resetPriority();
        this.bPhaseEffects = true;
        this.needToNextPhase = false;
        PhaseHandler.setGameBegins(0);
        this.phaseIndex = 0;
        this.extraTurns.clear();
        this.nCombatsThisTurn = 0;
        this.extraCombats = 0;
        this.bPreventCombatDamageThisTurn = false;
        this.bCombat = false;
        this.bRepeat = false;
        this.updateObservers();
    }

    /**
     * <p>
     * turnReset.
     * </p>
     */
    public final void turnReset() {
        this.playerTurn.setNumLandsPlayed(0);
    }

    /**
     * <p>
     * handleBeginPhase.
     * </p>
     */
    public final void handleBeginPhase() {
        Singletons.getModel().getGameState().getPhaseHandler().setPhaseEffects(false);
        // Handle effects that happen at the beginning of phases
        final String phase = Singletons.getModel().getGameState().getPhaseHandler().getPhase();
        final Player turn = Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn();
        Singletons.getModel().getGameState().getPhaseHandler().setSkipPhase(true);
        Singletons.getModel().getGameAction().checkStateEffects();

        // UNTAP
        if (phase.equals(Constant.Phase.UNTAP)) {
            Singletons.getControl().getControlMatch().showStack();
            PhaseUtil.handleUntap();
        }
        // UPKEEP
        else if (phase.equals(Constant.Phase.UPKEEP)) {
            PhaseUtil.handleUpkeep();
        }
        // DRAW
        else if (phase.equals(Constant.Phase.DRAW)) {
            PhaseUtil.handleDraw();
        }
        // COMBAT_BEGIN
        else if (phase.equals(Constant.Phase.COMBAT_BEGIN)) {
            PhaseUtil.verifyCombat();
            PhaseUtil.handleCombatBegin();
        }
        // COMBAT_DECLARE_ATTACKERS
        else if (phase.equals(Constant.Phase.COMBAT_DECLARE_ATTACKERS)) {
            PhaseUtil.handleCombatDeclareAttackers();
        }
        // COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY
        else if (phase.equals(Constant.Phase.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)) {
            if (this.inCombat()) {
                PhaseUtil.handleDeclareAttackers();
                CombatUtil.showCombat();
            } else {
                Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            }
        }
        // COMBAT_DECLARE_BLOCKERS: we can skip AfterBlockers and AfterAttackers
        // if necessary
        else if (phase.equals(Constant.Phase.COMBAT_DECLARE_BLOCKERS)) {
            if (this.inCombat()) {
                PhaseUtil.verifyCombat();
                CombatUtil.showCombat();
            } else {
                Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            }
        }
        // COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY
        else if (phase.equals(Constant.Phase.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
            // After declare blockers are finished being declared mark them
            // blocked and trigger blocking things
            if (this.inCombat()) {
                PhaseUtil.handleDeclareBlockers();
                CombatUtil.showCombat();
            } else {
                Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            }
        }
        // COMBAT_FIRST_STRIKE_DAMAGE
        else if (phase.equals(Constant.Phase.COMBAT_FIRST_STRIKE_DAMAGE)) {
            if (!this.inCombat()) {
                Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            } else {
                AllZone.getCombat().verifyCreaturesInPlay();

                // no first strikers, skip this step
                if (!AllZone.getCombat().setAssignedFirstStrikeDamage()) {
                    Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
                } else {
                    Combat.dealAssignedDamage();
                    Singletons.getModel().getGameAction().checkStateEffects();
                    CombatUtil.showCombat();
                }
            }
        }
        // COMBAT_DAMAGE
        else if (phase.equals(Constant.Phase.COMBAT_DAMAGE)) {
            if (!this.inCombat()) {
                Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
            } else {
                AllZone.getCombat().verifyCreaturesInPlay();

                AllZone.getCombat().setAssignedDamage();
                Combat.dealAssignedDamage();
                Singletons.getModel().getGameAction().checkStateEffects();
                CombatUtil.showCombat();
            }
        }
        // COMBAT_END
        else if (phase.equals(Constant.Phase.COMBAT_END)) {
            // End Combat always happens
            AllZone.getEndOfCombat().executeUntil();
            AllZone.getEndOfCombat().executeAt();
            CombatUtil.showCombat();
            Singletons.getControl().getControlMatch().showStack();
        } else if (phase.equals(Constant.Phase.MAIN2)) {
            CombatUtil.showCombat();
            Singletons.getControl().getControlMatch().showStack();
        }
        // END_OF_TURN
        else if (phase.equals(Constant.Phase.END_OF_TURN)) {
            AllZone.getEndOfTurn().executeAt();
        }
        // CLEANUP
        else if (phase.equals(Constant.Phase.CLEANUP)) {
            Singletons.getModel().getGameState().getPhaseHandler().getPlayerTurn().clearAssignedDamage();

            // Reset Damage received map
            final CardList list = AllZoneUtil.getCardsIn(Zone.Battlefield);
            for (final Card c : list) {
                c.resetPreventNextDamage();
                c.resetReceivedDamageFromThisTurn();
                c.resetDealtDamageToThisTurn();
                c.getDamageHistory().setDealtDmgToHumanThisTurn(false);
                c.getDamageHistory().setDealtDmgToComputerThisTurn(false);
                c.getDamageHistory().setDealtCombatDmgToHumanThisTurn(false);
                c.getDamageHistory().setDealtCombatDmgToComputerThisTurn(false);
                c.setRegeneratedThisTurn(0);
                c.clearMustBlockCards();
                if (Singletons.getModel().getGameState().getPhaseHandler().isPlayerTurn(AllZone.getComputerPlayer())) {
                    c.getDamageHistory().setCreatureAttackedLastComputerTurn(c.getDamageHistory().getCreatureAttackedThisTurn());
                }
                if (Singletons.getModel().getGameState().getPhaseHandler().isPlayerTurn(AllZone.getHumanPlayer())) {
                    c.getDamageHistory().setCreatureAttackedLastHumanTurn(c.getDamageHistory().getCreatureAttackedThisTurn());
                }
                c.getDamageHistory().setCreatureAttackedThisTurn(false);
                c.getDamageHistory().setCreatureBlockedThisTurn(false);
                c.getDamageHistory().setCreatureGotBlockedThisTurn(false);
                c.clearBlockedByThisTurn();
                c.clearBlockedThisTurn();
            }
            AllZone.getHumanPlayer().resetPreventNextDamage();
            AllZone.getComputerPlayer().resetPreventNextDamage();

            AllZone.getEndOfTurn().executeUntil();
            final CardList cHand = AllZone.getComputerPlayer().getCardsIn(Zone.Hand);
            final CardList hHand = AllZone.getHumanPlayer().getCardsIn(Zone.Hand);
            for (final Card c : cHand) {
                c.setDrawnThisTurn(false);
            }
            for (final Card c : hHand) {
                c.setDrawnThisTurn(false);
            }
            AllZone.getHumanPlayer().resetNumDrawnThisTurn();
            AllZone.getComputerPlayer().resetNumDrawnThisTurn();
            AllZone.getHumanPlayer().setAttackedWithCreatureThisTurn(false);
            AllZone.getComputerPlayer().setAttackedWithCreatureThisTurn(false);
        }

        if (!Singletons.getModel().getGameState().getPhaseHandler().isNeedToNextPhase()) {
            // Run triggers if phase isn't being skipped
            final HashMap<String, Object> runParams = new HashMap<String, Object>();
            runParams.put("Phase", phase);
            runParams.put("Player", turn);
            AllZone.getTriggerHandler().runTrigger(TriggerType.Phase, runParams);

        }

        // This line fixes Combat Damage triggers not going off when they should
        AllZone.getStack().unfreezeStack();

        // UNTAP
        if (!phase.equals(Constant.Phase.UNTAP)) {
            // during untap
            this.resetPriority();
        }
    }

    /**
     * Checks if is prevent combat damage this turn.
     * 
     * @return true, if is prevent combat damage this turn
     */
    public final boolean isPreventCombatDamageThisTurn() {
        return this.bPreventCombatDamageThisTurn;
    }

    /**
     * <p>
     * nextPhase.
     * </p>
     */
    public final void nextPhase() {
        this.needToNextPhase = false;

        // If the Stack isn't empty why is nextPhase being called?
        if (AllZone.getStack().size() != 0) {
            Log.debug("Phase.nextPhase() is called, but Stack isn't empty.");
            return;
        }
        this.bPhaseEffects = true;
        if (!AllZoneUtil.isCardInPlay("Upwelling")) {
            AllZone.getHumanPlayer().getManaPool().clearPool();
            AllZone.getComputerPlayer().getManaPool().clearPool();
        }

        if (this.getPhase().equals(Constant.Phase.COMBAT_DECLARE_ATTACKERS)) {
            AllZone.getStack().unfreezeStack();
            this.nCombatsThisTurn++;
        } else if (this.getPhase().equals(Constant.Phase.UNTAP)) {
            this.nCombatsThisTurn = 0;
        }

        if (this.getPhase().equals(Constant.Phase.COMBAT_END)) {
            Singletons.getControl().getControlMatch().showStack();
            AllZone.getCombat().reset();
            this.resetAttackedThisCombat(this.getPlayerTurn());
            this.bCombat = false;
        }

        if (this.getPhaseOrder()[this.phaseIndex].equals(Constant.Phase.CLEANUP)) {
            this.bPreventCombatDamageThisTurn = false;
            if (!this.bRepeat) {
                Singletons.getModel().getGameState().getPhaseHandler().setPlayerTurn(this.handleNextTurn());
            }
        }

        if (this.is(Constant.Phase.COMBAT_DECLARE_BLOCKERS)) {
            AllZone.getStack().unfreezeStack();
        }

        if (this.is(Constant.Phase.COMBAT_END) && (this.extraCombats > 0)) {
            // TODO: ExtraCombat needs to be changed for other spell/abilities
            // that give extra combat
            // can do it like ExtraTurn stack ExtraPhases

            final Player player = this.getPlayerTurn();
            final Player opp = player.getOpponent();

            this.bCombat = true;
            this.extraCombats--;
            AllZone.getCombat().reset();
            AllZone.getCombat().setAttackingPlayer(player);
            AllZone.getCombat().setDefendingPlayer(opp);
            this.phaseIndex = this.findIndex(Constant.Phase.COMBAT_DECLARE_ATTACKERS);
        } else {
            if (!this.bRepeat) { // for when Cleanup needs to repeat itself
                this.phaseIndex++;
                this.phaseIndex %= this.getPhaseOrder().length;
            } else {
                this.bRepeat = false;
            }
        }

        AllZone.getGameLog().add("Phase", this.getPlayerTurn() + " " + this.getPhase(), 6);

        // **** Anything BELOW Here is actually in the next phase. Maybe move
        // this to handleBeginPhase
        if (this.getPhase().equals(Constant.Phase.UNTAP)) {
            this.turn++;
            AllZone.getGameLog().add("Turn", "Turn " + this.turn + " (" + this.getPlayerTurn() + ")", 0);
        }

        PhaseUtil.visuallyActivatePhase(this.getPhase());

        // When consecutively skipping phases (like in combat) this section
        // pushes through that block
        this.updateObservers();

        if ((Singletons.getModel().getGameState().getPhaseHandler() != null) && Singletons.getModel().getGameState().getPhaseHandler().isNeedToNextPhase()) {
            Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(false);
            Singletons.getModel().getGameState().getPhaseHandler().nextPhase();
        }
    }

    /**
     * <p>
     * handleNextTurn.
     * </p>
     * 
     * @return a {@link forge.Player} object.
     */
    private Player handleNextTurn() {
        final Player nextTurn = this.extraTurns.isEmpty() ? this.getPlayerTurn().getOpponent() : this.extraTurns.pop();

        AllZone.getStack().setCardsCastLastTurn();
        AllZone.getStack().clearCardsCastThisTurn();
        AllZone.resetZoneMoveTracking();
        AllZone.getComputerPlayer().resetProwl();
        AllZone.getHumanPlayer().resetProwl();
        AllZone.getComputerPlayer().setLifeLostThisTurn(0);
        AllZone.getHumanPlayer().setLifeLostThisTurn(0);

        return this.skipTurnTimeVault(nextTurn);
    }

    /**
     * <p>
     * skipTurnTimeVault.
     * </p>
     * 
     * @param turn
     *            a {@link forge.Player} object.
     * @return a {@link forge.Player} object.
     */
    private Player skipTurnTimeVault(Player turn) {
        // time vault:
        CardList vaults = turn.getCardsIn(Zone.Battlefield, "Time Vault");
        vaults = vaults.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.isTapped();
            }
        });

        if (vaults.size() > 0) {
            final Card crd = vaults.get(0);

            if (turn.isHuman()) {
                if (GameActionUtil.showYesNoDialog(crd, "Untap " + crd + "?")) {
                    crd.untap();
                    turn = this.extraTurns.isEmpty() ? turn.getOpponent() : this.extraTurns.pop();
                }
            } else {
                // TODO Should AI skip his turn for time vault?
            }
        }
        return turn;
    }

    /**
     * <p>
     * is.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     * @param player
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    public final synchronized boolean is(final String phase, final Player player) {
        return this.getPhase().equals(phase) && this.getPlayerTurn().isPlayer(player);
    }

    /**
     * <p>
     * is.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final synchronized boolean is(final String phase) {
        return (this.getPhase().equals(phase));
    }

    /**
     * <p>
     * isAfter.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean isAfter(final String phase) {
        return this.phaseIndex > this.findIndex(phase);
    }

    /**
     * <p>
     * isBefore.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean isBefore(final String phase) {
        return this.phaseIndex < this.findIndex(phase);
    }

    /**
     * <p>
     * findIndex.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    private int findIndex(final String phase) {
        for (int i = 0; i < this.getPhaseOrder().length; i++) {
            if (phase.equals(this.getPhaseOrder()[i])) {
                return i;
            }
        }
        throw new RuntimeException("Phase : findIndex() invalid argument, phase = " + phase);
    }

    /**
     * <p>
     * getPhase.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getPhase() {
        return this.getPhaseOrder()[this.phaseIndex];
    }

    /**
     * <p>
     * Getter for the field <code>turn</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getTurn() {
        return this.turn;
    }

    /**
     * <p>
     * getNextTurn.
     * </p>
     * 
     * @return a {@link forge.Player} object.
     */
    public final Player getNextTurn() {
        if (this.extraTurns.isEmpty()) {
            return this.getPlayerTurn().getOpponent();
        }

        return this.extraTurns.peek();
    }

    /**
     * <p>
     * isNextTurn.
     * </p>
     * 
     * @param pl
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    public final boolean isNextTurn(final Player pl) {
        final Player next = this.getNextTurn();
        return (pl.equals(next));
    }

    /**
     * <p>
     * addExtraTurn.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     */
    public final void addExtraTurn(final Player player) {
        // use a stack to handle extra turns, make sure the bottom of the stack
        // restores original turn order
        if (this.extraTurns.isEmpty()) {
            this.extraTurns.push(this.getPlayerTurn().getOpponent());
        }

        this.extraTurns.push(player);
    }

    /**
     * <p>
     * skipTurn.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     */
    public final void skipTurn(final Player player) {
        // skipping turn without having extras is equivalent to giving your
        // opponent an extra turn
        if (this.extraTurns.isEmpty()) {
            this.addExtraTurn(player.getOpponent());
        } else {
            final int pos = this.extraTurns.lastIndexOf(player);
            if (pos == -1) {
                this.addExtraTurn(player.getOpponent());
            } else {
                this.extraTurns.remove(pos);
            }
        }
    }

    /**
     * <p>
     * addExtraCombat.
     * </p>
     */
    public final void addExtraCombat() {
        // Extra combats can only happen
        this.extraCombats++;
    }

    /**
     * <p>
     * isFirstCombat.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isFirstCombat() {
        return (this.nCombatsThisTurn == 1);
    }

    /**
     * <p>
     * resetAttackedThisCombat.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     */
    public final void resetAttackedThisCombat(final Player player) {
        // resets the status of attacked/blocked this phase
        CardList list = player.getCardsIn(Zone.Battlefield);

        list = list.getType("Creature");

        for (int i = 0; i < list.size(); i++) {
            final Card c = list.get(i);
            if (c.getDamageHistory().getCreatureAttackedThisCombat()) {
                c.getDamageHistory().setCreatureAttackedThisCombat(false);
            }
            if (c.getDamageHistory().getCreatureBlockedThisCombat()) {
                c.getDamageHistory().setCreatureBlockedThisCombat(false);
            }

            if (c.getDamageHistory().getCreatureGotBlockedThisCombat()) {
                c.getDamageHistory().setCreatureGotBlockedThisCombat(false);
            }
        }
    }

    /**
     * <p>
     * passPriority.
     * </p>
     */
    public final void passPriority() {
        final Player actingPlayer = this.getPriorityPlayer();
        final Player lastToAct = this.getFirstPriority();

        // actingPlayer is the player who may act
        // the lastToAct is the player who gained Priority First in this segment
        // of Priority

        if (lastToAct.equals(actingPlayer)) {
            // pass the priority to other player
            this.setPriorityPlayer(actingPlayer.getOpponent());
            AllZone.getInputControl().resetInput();
            AllZone.getStack().chooseOrderOfSimultaneousStackEntryAll();
        } else {
            if (AllZone.getStack().size() == 0) {
                // end phase
                this.needToNextPhase = true;
                this.pPlayerPriority = this.getPlayerTurn(); // this needs to be
                                                             // set early
                // as we exit the phase
            } else {
                if (!AllZone.getStack().hasSimultaneousStackEntries()) {
                    AllZone.getStack().resolveStack();
                }
            }
            AllZone.getStack().chooseOrderOfSimultaneousStackEntryAll();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void addObserver(final Observer o) {
        super.addObserver(o);
    }

    /** The need to next phase. */
    private boolean needToNextPhase = false;

    /**
     * <p>
     * Setter for the field <code>needToNextPhase</code>.
     * </p>
     * 
     * @param needToNextPhase
     *            a boolean.
     */
    public final void setNeedToNextPhase(final boolean needToNextPhase) {
        this.needToNextPhase = needToNextPhase;
    }

    /**
     * <p>
     * isNeedToNextPhase.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isNeedToNextPhase() {
        return this.needToNextPhase;
    }

    // This should only be true four times! that is for the initial nextPhases
    // in MyObservable
    /** The need to next phase init. */
    private int needToNextPhaseInit = 0;

    /**
     * <p>
     * isNeedToNextPhaseInit.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isNeedToNextPhaseInit() {
        this.needToNextPhaseInit++;
        if (this.needToNextPhaseInit <= 4) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * canCastSorcery.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a boolean.
     */
    public static boolean canCastSorcery(final Player player) {
        return Singletons.getModel().getGameState().getPhaseHandler().isPlayerTurn(player)
                && (Singletons.getModel().getGameState().getPhaseHandler().getPhase().equals(Constant.Phase.MAIN2) || Singletons.getModel().getGameState().getPhaseHandler()
                        .getPhase().equals(Constant.Phase.MAIN1)) && (AllZone.getStack().size() == 0);
    }

    /**
     * <p>
     * buildActivateString.
     * </p>
     * 
     * @param startPhase
     *            a {@link java.lang.String} object.
     * @param endPhase
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public final String buildActivateString(final String startPhase, final String endPhase) {
        final StringBuilder sb = new StringBuilder();

        boolean add = false;
        for (int i = 0; i < this.getPhaseOrder().length; i++) {
            if (this.getPhaseOrder()[i].equals(startPhase)) {
                add = true;
            }

            if (add) {
                if (sb.length() != 0) {
                    sb.append(",");
                }
                sb.append(this.getPhaseOrder()[i]);
            }

            if (this.getPhaseOrder()[i].equals(endPhase)) {
                add = false;
            }
        }

        return sb.toString();
    }

    /**
     * <p>
     * setGameBegins.
     * </p>
     * 
     * @param gameBegins
     *            a int.
     */
    public static void setGameBegins(final int gameBegins) {
        PhaseHandler.gameBegins = gameBegins;
    }

    /**
     * <p>
     * getGameBegins.
     * </p>
     * 
     * @return a int.
     */
    public static int getGameBegins() {
        return PhaseHandler.gameBegins;
    }

    // this is a hack for the setup game state mode, do not use outside of
    // devSetupGameState code
    // as it avoids calling any of the phase effects that may be necessary in a
    // less enforced context
    /**
     * <p>
     * setDevPhaseState.
     * </p>
     * 
     * @param phaseID
     *            a {@link java.lang.String} object.
     */
    public final void setDevPhaseState(final String phaseID) {
        this.phaseIndex = this.findIndex(phaseID);
    }

    /**
     * Sets the phase state.
     *
     * @param phaseID the new phase state
     */
    public final void setPhaseState(final String phaseID) {
        this.phaseIndex = this.findIndex(phaseID);
        this.handleBeginPhase();
    }

    /**
     * 
     * TODO Write javadoc for this method.
     * 
     * @param b
     *            a boolean
     */
    public final void setPreventCombatDamageThisTurn(final boolean b) {
        this.bPreventCombatDamageThisTurn = true;
    }

    /**
     * Gets the phase order.
     * 
     * @return the phaseOrder
     */
    public String[] getPhaseOrder() {
        return this.phaseOrder;
    }

    /**
     * Sets the phase order.
     * 
     * @param phaseOrder0
     *            the phaseOrder to set
     */
    public void setPhaseOrder(final String[] phaseOrder0) {
        this.phaseOrder = phaseOrder0;
    }
}
