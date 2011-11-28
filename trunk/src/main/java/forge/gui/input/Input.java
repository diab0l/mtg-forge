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
package forge.gui.input;

import forge.AllZone;
import forge.Card;
import forge.Player;
import forge.PlayerZone;

/**
 * <p>
 * Abstract Input class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Input implements java.io.Serializable {
    /** Constant <code>serialVersionUID=-6539552513871194081L</code>. */
    private static final long serialVersionUID = -6539552513871194081L;

    private boolean isFree = false;

    // showMessage() is always the first method called
    /**
     * <p>
     * showMessage.
     * </p>
     */
    public void showMessage() {
        AllZone.getDisplay().showMessage("Blank Input");
    }

    /**
     * <p>
     * selectCard.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param zone
     *            a {@link forge.PlayerZone} object.
     */
    public void selectCard(final Card c, final PlayerZone zone) {
    }

    /**
     * <p>
     * selectPlayer.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     */
    public void selectPlayer(final Player player) {
    }

    /**
     * <p>
     * selectButtonOK.
     * </p>
     */
    public void selectButtonOK() {
    }

    /**
     * <p>
     * selectButtonCancel.
     * </p>
     */
    public void selectButtonCancel() {
    }

    // helper methods, since they are used alot
    // to be used by anything in CardFactory like SetTargetInput
    // NOT TO BE USED by Input_Main or any of the "regular" Inputs objects that
    // are not set using AllZone.getInputControl().setInput(Input)
    /**
     * <p>
     * stop.
     * </p>
     */
    public final void stop() {
        // clears a "temp" Input like Input_PayManaCost if there is one
        AllZone.getInputControl().resetInput();

        if (AllZone.getPhase().isNeedToNextPhase()) {
            // mulligan needs this to move onto next phase
            AllZone.getPhase().setNeedToNextPhase(false);
            AllZone.getPhase().nextPhase();
        }
    }

    // exits the "current" Input and sets the next Input
    /**
     * <p>
     * stopSetNext.
     * </p>
     * 
     * @param in
     *            a {@link forge.gui.input.Input} object.
     */
    public final void stopSetNext(final Input in) {
        this.stop();
        AllZone.getInputControl().setInput(in);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "blank";
    } // returns the Input name like "EmptyStack"

    /**
     * <p>
     * setFree.
     * </p>
     * 
     * @param isFree
     *            a boolean.
     */
    public void setFree(final boolean isFree) {
        this.isFree = isFree;
    }

    /**
     * <p>
     * isFree.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isFree() {
        return this.isFree;
    }
}
