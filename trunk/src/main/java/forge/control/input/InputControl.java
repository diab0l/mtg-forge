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
package forge.control.input;

import java.util.Stack;

import forge.game.GameState;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerController;
import forge.game.zone.MagicStack;
import forge.gui.match.controllers.CMessage;
import forge.util.MyObservable;

/**
 * <p>
 * InputControl class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class InputControl extends MyObservable implements java.io.Serializable {
    /** Constant <code>serialVersionUID=3955194449319994301L</code>. */
    private static final long serialVersionUID = 3955194449319994301L;

    private Input input;

    private final Stack<Input> inputStack = new Stack<Input>();
    private final Stack<Input> urgentInputStack = new Stack<Input>();

    private final transient GameState game;
    /**
     * TODO Write javadoc for Constructor.
     * 
     * @param fModel
     *            the f model
     */
    public InputControl(final GameState game0) {
        this.game = game0;
    }

    /**
     * <p>
     * Setter for the field <code>input</code>.
     * </p>
     * 
     * @param in
     *            a {@link forge.control.input.Input} object.
     */
    public final void setInput(final Input in) {
        boolean isInputEmpty = this.input == null || this.input instanceof InputPassPriority;
        //System.out.println(in.getClass().getName());
        if (!this.game.getStack().isResolving() && isInputEmpty) {
            this.input = in;
        } else {
            this.inputStack.add(in);
        }
        this.updateObservers();
    }

    /**
     * <p>
     * Setter for the field <code>input</code>.
     * </p>
     * 
     * @param in
     *            a {@link forge.control.input.Input} object.
     * @param bAddToResolving
     *            a boolean.
     */
    public final void setInputInterrupt(final Input in) {
        // Make this
        final Input old = this.input;
        this.urgentInputStack.add(old);
        this.changeInput(in);
    }

    /**
     * <p>
     * changeInput.
     * </p>
     * 
     * @param in
     *            a {@link forge.control.input.Input} object.
     */
    private void changeInput(final Input in) {
        this.input = in;
        this.updateObservers();
    }

    /**
     * <p>
     * Getter for the field <code>input</code>.
     * </p>
     * 
     * @return a {@link forge.control.input.Input} object.
     */
    public final Input getInput() {
        return this.input;
    }

    /**
     * <p>
     * clearInput.
     * </p>
     */
    public final void clearInput() {
        this.input = null;
        this.inputStack.clear();
    }


    /**
     * <p>
     * resetInput.
     * </p>
     * 
     * @param update
     *            a boolean.
     */
    public final void resetInput() { 
        this.input = null;
        this.updateObservers();
    }

    /**
     * <p>
     * updateInput.
     * </p>
     * 
     * @return a {@link forge.control.input.Input} object.
     */
    public final Input getActualInput() {
        if ( !game.hasMulliganned() )
            return new InputMulligan();

        final PhaseHandler handler = game.getPhaseHandler();
        final PhaseType phase = handler.getPhase();
        final Player playerTurn = handler.getPlayerTurn();
        final Player priority = handler.getPriorityPlayer();
        final MagicStack stack = game.getStack();

        // TODO this resolving portion needs more work, but fixes Death Cloud
        // issues
        if (this.urgentInputStack.size() > 0) {
            if (this.input != null) {
                return this.input;
            }

            // if an SA is resolving, only change input for something that is
            // part of the resolving SA
            this.changeInput(this.urgentInputStack.pop());
            return this.input;
        }

        if (stack.isResolving()) {
            return null;
        }

        if (this.input != null) {
            return this.input;
        }

        if (!this.inputStack.isEmpty()) { // incoming input to Control
            this.changeInput(this.inputStack.pop());
            return this.input;
        }

        if (handler.hasPhaseEffects()) {
            // Handle begin phase stuff, then start back from the top
            handler.handleBeginPhase();
            return this.getActualInput();
        }

        // If the Phase we're in doesn't allow for Priority, return null to move
        // to next phase
        if (!handler.isPlayerPriorityAllowed()) {
            return null;
        }

        if (priority == null)
            return null;
        PlayerController pc = priority.getController();
        
        // Special Inputs needed for the following phases:
        switch (phase) {
            case COMBAT_DECLARE_ATTACKERS:
                stack.freezeStack();
                if (playerTurn.isHuman() && !playerTurn.getController().mayAutoPass(phase)) {
                    game.getCombat().initiatePossibleDefenders(playerTurn.getOpponents());
                    return new InputAttack();
                }
                break;

            case COMBAT_DECLARE_BLOCKERS:
                stack.freezeStack();

                if (game.getCombat().isPlayerAttacked(priority)) {
                    return pc.getBlockInput();
                }

                // noone attacks you
                pc.passPriority();
                return null;

            case CLEANUP:
                // discard
                if (stack.isEmpty()) {
                    // resolve things like Madness
                    return pc.getCleanupInput();
                }
                break;
            default:
                break;
        }

        // *********************
        // Special phases handled above, everything else is handled simply by
        // priority

        boolean prioritySkip = pc.mayAutoPass(phase) || pc.isUiSetToSkipPhase(playerTurn, phase);
        if (this.game.getStack().isEmpty() && prioritySkip) {
            pc.passPriority();
            return null;
        } else
            pc.autoPassCancel(); // probably cancel, since something has happened

         return pc.getDefaultInput();
    } // getInput()

    public final void setNewInput(GameState game) {
        PhaseHandler ph = game.getPhaseHandler();

        final Input tmp = getActualInput();
        //String message = String.format("%s's %s, priority of %s [%sP] input is %s", ph.getPlayerTurn(), ph.getPhase(), ph.getPriorityPlayer(), ph.isPlayerPriorityAllowed() ? "+" : "-", tmp == null ? "null" : tmp.getClass().getSimpleName());
        //System.out.println(message);
        if (tmp != null) {
            //System.out.println(ph.getPlayerTurn() + "'s " + ph.getPhase() + ", priority of " + ph.getPriorityPlayer() + " @ input is " + tmp.getClass().getName() );
            CMessage.SINGLETON_INSTANCE.getInputControl().setInput(tmp);
        } else if (!ph.isPlayerPriorityAllowed()) {
            // System.out.println("cannot have priority, forced to pass");
            ph.getPriorityPlayer().getController().passPriority();
        }
    }

} // InputControl
