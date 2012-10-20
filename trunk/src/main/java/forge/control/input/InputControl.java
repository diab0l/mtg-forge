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
import forge.game.player.ComputerAIInput;
import forge.game.player.Player;
import forge.game.zone.MagicStack;
import forge.gui.match.CMatchUI;
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

    private final GameState game;
    private ComputerAIInput aiInput; // initialized at runtime to be the latest object created

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
    public final void resetInput() { resetInput(true); }
    public final void resetInput(final boolean update) {
        this.input = null;
        if (update) {
            this.updateObservers();
        }
    }

    /**
     * <p>
     * updateInput.
     * </p>
     * 
     * @return a {@link forge.control.input.Input} object.
     */
    public final Input getActualInput() {
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
        if (!handler.mayPlayerHavePriority()) {
            return null;
        }

        // Special Inputs needed for the following phases:
        if (phase == PhaseType.COMBAT_DECLARE_ATTACKERS) {
            stack.freezeStack();

            if (playerTurn.isHuman() && !handler.getAutoPass()) {
                game.getCombat().initiatePossibleDefenders(playerTurn.getOpponent());
                return new InputAttack();
            }
        } else if (phase == PhaseType.COMBAT_DECLARE_BLOCKERS) {
            stack.freezeStack();
            if (playerTurn.isHuman()) {
                this.aiInput.getComputer().declareBlockers();
                return null;
            } else {
                if (game.getCombat().getAttackers().isEmpty()) {
                    // no active attackers, skip the Blocking phase
                    handler.setPlayerMayHavePriority(false);
                    return null;
                } else {
                    handler.setAutoPass(false);
                    return new InputBlock();
                }
            }
        } else if (phase == PhaseType.CLEANUP) {
            // discard
            if (stack.isEmpty()) {
                // resolve things
                // like Madness
                return new InputCleanup();
            }
        }

        // *********************
        // Special phases handled above, everything else is handled simply by
        // priority
        if (priority == null) {
            return null;
        } else if (priority.isHuman()) {
            if (autoSkipHumanPriority(playerTurn, phase)) {
                handler.passPriority();
                return null;
            } else {
                return new InputPassPriority();
            }
        } else if (playerTurn.isComputer()) {
            return this.aiInput;
        } else {
            this.aiInput.getComputer().playSpellAbilities();
            return null;
        }
    } // getInput()

    private boolean autoSkipHumanPriority(Player turn, PhaseType phase) {
        PhaseHandler handler = game.getPhaseHandler();
        // Handler tells me if I should skip, and I reset the flag
        final boolean skip = handler.doSkipPhase();
        handler.setSkipPhase(false);

        // If the stack isn't empty, and skip is disallowed, stop auto passing
        if (!skip || !this.game.getStack().isEmpty()) {
            handler.setAutoPass(false);
            return false;
        }

        // Player is AutoPassing, or Player doesn't stop on this phase
        return handler.getAutoPass() || !CMatchUI.SINGLETON_INSTANCE.stopAtPhase(turn, phase);
    }

    /**
     * Sets the computer.
     * 
     * @param computerAIInput
     *            the new computer
     */
    public final void setComputer(final ComputerAIInput computerAIInput) {
        this.aiInput = computerAIInput;
    }
} // InputControl
