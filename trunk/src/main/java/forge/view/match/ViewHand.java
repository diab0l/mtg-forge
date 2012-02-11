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
package forge.view.match;

import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import arcane.ui.HandArea;
import forge.control.match.ControlHand;
import forge.view.toolbox.FPanel;

/**
 * VIEW - Swing components for user hand.
 * 
 */
@SuppressWarnings("serial")
public class ViewHand extends FPanel {
    private ControlHand control;
    private HandArea hand;
    private MatchTopLevel topLevel;

    /**
     * Swing components for user hand.
     * 
     * @param v0 &emsp; The MatchTopLevel parent.
     */
    public ViewHand(MatchTopLevel v0) {
        final JScrollPane scroller = new JScrollPane();
        ViewHand.this.hand = new HandArea(scroller);
        topLevel = v0;

        scroller.setViewportView(ViewHand.this.hand);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(null);
        ViewHand.this.hand.setOpaque(false);

        setLayout(new MigLayout("insets 0, gap 0"));
        add(scroller, "w 100%, h 100%!");

        // After all components are in place, instantiate controller.
        ViewHand.this.control = new ControlHand(this);
    }

    /**
     * Gets the controller.
     * 
     * @return ControlHand
     */
    public ControlHand getController() {
        return ViewHand.this.control;
    }

    /**
     * Gets the hand area.
     *
     * @return HandArea
     */
    public HandArea getHandArea() {
        return ViewHand.this.hand;
    }

    /** @return MatchTopLevel */
    public MatchTopLevel getTopLevel() {
        return this.topLevel;
    }
}
