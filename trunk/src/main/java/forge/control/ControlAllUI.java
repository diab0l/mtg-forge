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

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLayeredPane;

import forge.AllZone;
import forge.view.GuiTopLevel;
import forge.view.editor.EditorTopLevel;
import forge.view.home.HomeTopLevel;
import forge.view.match.ViewTopLevel;

/**
 * <p>
 * ControlAllUI.
 * </p>
 * Controls all Forge UI functionality inside one JFrame. This class switches
 * between various display states in that JFrame. Controllers are instantiated
 * separately by each state's top level view class.
 */
public class ControlAllUI {
    private final JLayeredPane display;
    private final GuiTopLevel view;
    private HomeTopLevel home = null;
    private ViewTopLevel match = null;
    private EditorTopLevel editor = null;

    /**
     * <p>
     * ControlAllUI.
     * </p>
     * Controls all Forge UI functionality inside one JFrame. This class
     * switches between various display states in that JFrame. Controllers are
     * instantiated separately by each state's top level view class.
     * 
     * @param v0 &emsp; GuiTopLevel
     */
    public ControlAllUI(GuiTopLevel v0) {
        this.view = v0;

        this.display = (JLayeredPane) this.view.getContentPane();
    }

    /**
     * <p>
     * changeState.
     * </p>
     * Switches between display states in top level JFrame.
     * 
     * @param i
     *            &emsp; State index: 0 for home, 1 for match, etc.
     */
    public void changeState(final int i) {
        this.home = null;
        this.match = null;
        this.editor = null;

        this.display.removeAll();
        this.view.addOverlay();

        // Fire up new state
        switch (i) {
        case 0: // Home screen
            this.home = new HomeTopLevel();
            //this.display.add(this.home);
            break;

        case 1: // Match screen
            this.match = new ViewTopLevel();
            this.display.add(this.match, JLayeredPane.DEFAULT_LAYER);

            view.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent evt) {
                    ViewTopLevel t = ((GuiTopLevel) AllZone.getDisplay()).getController().getMatchController().getView();
                    t.getDockController().concede();
                }
            });

            view.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(final ComponentEvent e) {
                    Component[] children;
                    children = ControlAllUI.this.display.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
                    children[0].setSize(ControlAllUI.this.display.getSize());

                    children = ControlAllUI.this.display.getComponentsInLayer(JLayeredPane.MODAL_LAYER);
                    children[0].setSize(ControlAllUI.this.display.getSize());
                }
            });

            Component[] children;
            children = ControlAllUI.this.display.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
            children[0].setSize(ControlAllUI.this.display.getSize());

            children = ControlAllUI.this.display.getComponentsInLayer(JLayeredPane.MODAL_LAYER);
            children[0].setSize(ControlAllUI.this.display.getSize());
            break;

        case 2: // Deck editor screen
            this.editor = new EditorTopLevel();
            this.display.add(this.editor);
            break;

        default:
            break;
        }
    }

    /**
     * Gets the match view.
     * 
     * @return ViewTopLevel
     */
    public ViewTopLevel getMatchView() {
        return this.match;
    }

    /**
     * Gets the match controller.
     * 
     * @return ControlMatchUI
     */
    public ControlMatchUI getMatchController() {
        return this.match.getController();
    }
}
