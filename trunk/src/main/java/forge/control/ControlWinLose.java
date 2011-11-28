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
package forge.control;

import forge.AllZone;
import forge.Constant;
import forge.view.swing.GuiHomeScreen;
import forge.view.swing.OldGuiNewGame;
import forge.view.toolbox.WinLoseFrame;

/**
 * <p>
 * ControlWinLose.
 * </p>
 * 
 * Superclass of custom mode handling for win/lose UI. Can add swing components
 * to custom center panel. Custom mode handling for quest, puzzle, etc. should
 * extend this class.
 * 
 */
public class ControlWinLose {

    /** The view. */
    private WinLoseFrame view;

    /**
     * <p>
     * actionOnQuit.
     * </p>
     * Action performed when "continue" button is pressed in default win/lose
     * UI.
     * 
     */
    public void actionOnContinue() {

    }

    /**
     * <p>
     * actionOnQuit.
     * </p>
     * Action performed when "quit" button is pressed in default win/lose UI.
     * 
     */
    public void actionOnQuit() {
        if (System.getenv("NG2") != null) {
            if (System.getenv("NG2").equalsIgnoreCase("true")) {
                final String[] argz = {};
                GuiHomeScreen.main(argz);
            } else {
                new OldGuiNewGame();
            }
        } else {
            new OldGuiNewGame();
        }
    }

    /**
     * <p>
     * actionOnRestart.
     * </p>
     * Action performed when "restart" button is pressed in default win/lose UI.
     * 
     */
    public void actionOnRestart() {

    }

    /**
     * <p>
     * startNextRound.
     * </p>
     * Either continues or restarts a current game. May be overridden for use
     * with other game modes.
     * 
     */
    public void startNextRound() {
        AllZone.getGameAction().newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
    }

    /**
     * <p>
     * populateCustomPanel.
     * </p>
     * May be overridden as required by various mode handlers to show custom
     * information in center panel. Default configuration is empty.
     * 
     * @return true, if successful
     */
    public boolean populateCustomPanel() {
        return false;
    }

    /**
     * <p>
     * setView.
     * </p>
     * Links win/lose swing frame to mode handler, mostly to allow direct
     * manipulation of custom center panel.
     * 
     * @param wlh
     *            the new view
     */
    public final void setView(final WinLoseFrame wlh) {
        this.view = wlh;
    }

    /**
     * Gets the view.
     * 
     * @return the view
     */
    public WinLoseFrame getView() {
        return this.view;
    }
}
