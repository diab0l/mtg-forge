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
package forge.gui.match.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.match.controllers.CDock;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.JLabelSkin;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FSkin.SkinImage;

/**
 * Assembles Swing components of button dock area.
 * 
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VDock implements IVDoc<CDock> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Dock");

    // Dock button instances
    private final JLabel btnConcede =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_CONCEDE), "Concede Game");
    private final JLabel btnSettings =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_SETTINGS), "Game Settings");
    private final JLabel btnEndTurn =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_ENDTURN), "End Turn");
    private final JLabel btnViewDeckList =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_DECKLIST), "View Deck List");
    private final JLabel btnRevertLayout =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_REVERTLAYOUT), "Revert Layout");
    private final JLabel btnOpenLayout =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_OPENLAYOUT), "Open Layout");
    private final JLabel btnSaveLayout =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_SAVELAYOUT), "Save Layout");
    private final JLabel btnAlphaStrike =
            new DockButton(FSkin.getIcon(FSkin.DockIcons.ICO_ALPHASTRIKE), "Alpha Strike");
    private final FLabel btnTargeting =
            new FLabel.Builder().icon(FSkin.getIcon(FSkin.DockIcons.ICO_ARCSOFF))
                .hoverable(true).iconInBackground(true).iconScaleFactor(1.0).build();

    private VDock() {
        btnTargeting.setPreferredSize(new Dimension(30, 30));
        btnTargeting.setMinimumSize(new Dimension(30, 30));
        btnTargeting.setMaximumSize(new Dimension(30, 30));
    }

    //========= Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#populate()
     */
    @Override
    public void populate() {
        final JPanel pnl = parentCell.getBody();
        // Mig layout does not support wrapping!
        // http://stackoverflow.com/questions/5715833/how-do-you-make-miglayout-behave-like-wrap-layout
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        pnl.add(btnConcede);
        //pnl.add(btnSettings);
        pnl.add(btnEndTurn);
        pnl.add(btnViewDeckList);
        pnl.add(btnRevertLayout);
        pnl.add(btnOpenLayout);
        pnl.add(btnSaveLayout);
        pnl.add(btnAlphaStrike);
        pnl.add(btnTargeting);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell()
     */
    @Override
    public void setParentCell(final DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.BUTTON_DOCK;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CDock getLayoutControl() {
        return CDock.SINGLETON_INSTANCE;
    }

    //========= Retrieval methods

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnConcede() {
        return btnConcede;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnSettings() {
        return btnSettings;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnEndTurn() {
        return btnEndTurn;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnViewDeckList() {
        return btnViewDeckList;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnRevertLayout() {
        return btnRevertLayout;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnOpenLayout() {
        return btnOpenLayout;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnSaveLayout() {
        return btnSaveLayout;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnAlphaStrike() {
        return btnAlphaStrike;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnTargeting() {
        return btnTargeting;
    }

    //========= Custom class handling
    /**
     * Buttons in Dock. JLabels are used to allow hover effects.
     */
    @SuppressWarnings("serial")
    private class DockButton extends JLabel {
        private final SkinImage img;
        private final JLabelSkin<DockButton> skin;
        private final SkinColor hoverBG = FSkin.getColor(FSkin.Colors.CLR_HOVER);
        private final Color defaultBG = new Color(0, 0, 0, 0);
        private final Color defaultBorderColor = new Color(0, 0, 0, 0);
        private int w, h;

        /**
         * Buttons in Dock. JLabels are used to allow hover effects.
         * 
         * @param i0
         *            &emsp; ImageIcon to show in button
         * @param s0
         *            &emsp; Tooltip string
         */
        public DockButton(final SkinImage i0, final String s0) {
            super();
            this.skin = FSkin.get(this);
            this.setToolTipText(s0);
            this.setOpaque(false);
            this.setBackground(this.defaultBG);
            this.img = i0;

            Dimension size = new Dimension(30, 30);
            this.setMinimumSize(size);
            this.setMaximumSize(size);
            this.setPreferredSize(size);

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(final MouseEvent e) {
                    DockButton.this.skin.setBackground(DockButton.this.hoverBG);
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    DockButton.this.setBackground(DockButton.this.defaultBG);
                }
            });
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        public void paintComponent(final Graphics g) {
            this.w = this.getWidth();
            this.h = this.getHeight();
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.w, this.h);
            if (skin.getBackground() == this.hoverBG) {
                skin.setGraphicsColor(g, FSkin.getColor(FSkin.Colors.CLR_BORDERS));
            }
            else {
                g.setColor(this.defaultBorderColor);
            }
            g.drawRect(0, 0, this.w - 1, this.h - 1);
            skin.drawImage(g, this.img, 0, 0, this.w, this.h);
            super.paintComponent(g);
        }
    }
}
