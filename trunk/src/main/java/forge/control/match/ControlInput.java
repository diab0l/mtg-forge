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
package forge.control.match;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import forge.AllZone;
import forge.Constant;
import forge.GuiInput;
import forge.view.match.ViewInput;

/**
 * Child controller - handles operations related to input panel.
 * 
 */
public class ControlInput {
    private final ViewInput view;

    private final GuiInput inputControl;

    private ActionListener alCancel = null, alOK = null;

    /**
     * Child controller - handles operations related to input panel.
     * 
     * @param v
     *            &emsp; The Swing component for the input area
     */
    public ControlInput(final ViewInput v) {
        this.view = v;
        this.inputControl = new GuiInput();

        this.alOK = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                ControlInput.this.btnOKActionPerformed(evt);

                if (AllZone.getPhaseHandler().isNeedToNextPhase()) {
                    // moves to next turn
                    AllZone.getPhaseHandler().setNeedToNextPhase(false);
                    AllZone.getPhaseHandler().nextPhase();
                }
                ControlInput.this.view.getBtnOK().requestFocusInWindow();
            }
        };

        this.alCancel = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                ControlInput.this.btnCancelActionPerformed(evt);
                ControlInput.this.view.getBtnOK().requestFocusInWindow();
            }
        };
    }

    /** Adds listeners to input area. */
    public void addListeners() {
        this.view.getBtnCancel().removeActionListener(alCancel);
        this.view.getBtnCancel().addActionListener(alCancel);

        this.view.getBtnOK().removeActionListener(alOK);
        this.view.getBtnOK().addActionListener(alOK);
    }

    /**
     * <p>
     * btnCancelActionPerformed.
     * </p>
     * Triggers current cancel action from whichever input controller is being
     * used.
     * 
     * @param evt
     *            a {@link java.awt.event.ActionEvent} object.
     */
    private void btnCancelActionPerformed(final ActionEvent evt) {
        this.inputControl.selectButtonCancel();
    }

    /**
     * <p>
     * btnOKActionPerformed.
     * </p>
     * Triggers current OK action from whichever input controller is being used.
     * 
     * @param evt
     *            a {@link java.awt.event.ActionEvent} object.
     */
    private void btnOKActionPerformed(final ActionEvent evt) {
        this.inputControl.selectButtonOK();
    }

    /**
     * Gets the input control.
     * 
     * @return GuiInput
     */
    public GuiInput getInputControl() {
        return this.inputControl;
    }

    /** @return ViewInput */
    public ViewInput getView() {
        return view;
    }

    /** Updates count label in input area. */
    public void updateGameCount() {
        view.getLblGames().setText("<html>Game #"
                + (AllZone.getMatchState().getGamesPlayedCount() + 1)
                + " of " + AllZone.getMatchState().getGamesPerMatch()
                + "<br>" + Constant.Runtime.getGameType().toString() + " mode</html>");
    }

    /** Flashes animation on input panel if play is currently waiting on input. */
    public void remind() {
        view.remind();
    }
}
