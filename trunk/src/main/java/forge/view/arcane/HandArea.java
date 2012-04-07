/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.view.arcane;

import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;

import forge.view.arcane.util.CardPanelMouseListener;


/**
 * <p>
 * HandArea class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class HandArea extends CardArea {
    /** Constant <code>serialVersionUID=7488132628637407745L</code>. */
    private static final long serialVersionUID = 7488132628637407745L;

    /**
     * <p>
     * Constructor for HandArea.
     * </p>
     * TODO Make compatible with WindowBuilder
     * 
     * @param scrollPane
     *            a {@link javax.swing.JScrollPane} object.
     */
    public HandArea(final JScrollPane scrollPane) {
        super(scrollPane);

        this.setDragEnabled(true);
        this.setVertical(true);

        this.addCardPanelMouseListener(new CardPanelMouseListener() {
            @Override
            public void mouseRightClicked(final CardPanel panel, final MouseEvent evt) {
            }

            @Override
            public void mouseOver(final CardPanel panel, final MouseEvent evt) {
            }

            @Override
            public void mouseOut(final CardPanel panel, final MouseEvent evt) {
            }

            @Override
            public void mouseMiddleClicked(final CardPanel panel, final MouseEvent evt) {
            }

            @Override
            public void mouseLeftClicked(final CardPanel panel, final MouseEvent evt) {

            }

            @Override
            public void mouseDragged(final CardPanel dragPanel, final int dragOffsetX, final int dragOffsetY,
                    final MouseEvent evt) {
            }

            @Override
            public void mouseDragStart(final CardPanel dragPanel, final MouseEvent evt) {
            }

            @Override
            public void mouseDragEnd(final CardPanel dragPanel, final MouseEvent evt) {
            }
        });
    }
}
