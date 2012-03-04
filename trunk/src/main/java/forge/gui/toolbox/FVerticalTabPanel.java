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
package forge.gui.toolbox;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import net.miginfocom.swing.MigLayout;

/**
 * TODO: Write javadoc for this type.
 * 
 */
@SuppressWarnings("serial")
public class FVerticalTabPanel extends JPanel {
    private final CardLayout cards;
    private final JPanel pnlContent;
    private final List<VTab> allVTabs;
    private int w, h, active;
    private final Color activeColor, inactiveColor, hoverColor;

    private boolean tabsOnRightSide;

    /**
     * Constructor, will automatically place tabs on left side.
     * 
     * @param childPanels
     *            &emsp; JPanels to be placed in tabber
     * @wbp.parser.constructor
     */
    public FVerticalTabPanel(final List<JPanel> childPanels) {
        this(childPanels, false);
    }

    /**
     * Assembles vertical tab panel from list of child panels. Tooltip on tab is
     * same as tooltip on child panel. Title of tab is same as name of child
     * panel.
     * 
     * @param childPanels
     *            &emsp; JPanels to be placed in tabber
     * @param b
     *            &emsp; boolean, true if tabs are on right side, false for left
     *            side.
     */
    public FVerticalTabPanel(final List<JPanel> childPanels, boolean b) {
        // General inits and skin settings
        super();
        tabsOnRightSide = b;
        this.setLayout(new MigLayout("insets 0, gap 0, wrap 2"));
        this.setOpaque(false);
        final int size = childPanels.size();
        this.hoverColor = FSkin.getColor(FSkin.Colors.CLR_HOVER);
        this.activeColor = FSkin.getColor(FSkin.Colors.CLR_ACTIVE);
        this.inactiveColor = FSkin.getColor(FSkin.Colors.CLR_INACTIVE);

        // Width of tabs, in percent width of container
        final int pctTabW = 11;
        // Spacing above and below each tab
        final double pctSpacing = 0.5;
        //
        final int pctTabH = (int) ((100 - size * pctSpacing * 2) / size);

        // Content panel and card layout inits
        this.cards = new CardLayout();
        this.pnlContent = new FPanel();
        ((FPanel) pnlContent).setCornerDiameter(0);
        this.pnlContent.setLayout(this.cards);

        // If tabs are on the left side, content panel is added
        // immediately to define grid.
        if (tabsOnRightSide) {
            this.add(this.pnlContent, "span 1 " + (size + 1) + ", w " + (100 - pctTabW) + "%!, h 100%!");
            this.pnlContent.setBorder(new MatteBorder(0, 0, 0, 1, FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        }

        this.add(new JPanel(), "w 0, h 0!");

        // If tabs are on right side, content panel
        // must be added after spacer, which then defines the grid.
        if (!tabsOnRightSide) {
            this.add(this.pnlContent, "span 1 " + (size + 1) + ", w " + (100 - pctTabW) + "%!, h 100%!");
            this.pnlContent.setBorder(new MatteBorder(0, 1, 0, 0, FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        }

        // Add all tabs
        VTab tab;
        this.allVTabs = new ArrayList<VTab>();

        for (int i = 0; i < size; i++) {
            tab = new VTab(childPanels.get(i).getName(), i);
            tab.setToolTipText(childPanels.get(i).getToolTipText());

            if (i == 0) {
                tab.setBackground(this.activeColor);
                this.active = 0;
            } else {
                tab.setBackground(this.inactiveColor);
            }

            this.add(tab, "w " + pctTabW + "%!, h " + pctTabH + "%!,"
                    + " gap 0 0 " + pctSpacing + " " + pctSpacing + "%!, ay center");
            this.allVTabs.add(tab);

            // Add card to content panel
            this.pnlContent.add(childPanels.get(i), "CARD" + i);
        }
    }

    /**
     * Programatically flips tab layout to specified number (without needing a
     * mouse event).
     * 
     * @param index
     *            &emsp; Tab number, starting from 0
     */
    public void showTab(final int index) {
        if (index >= this.allVTabs.size()) {
            return;
        }

        this.allVTabs.get(this.active).setBackground(this.inactiveColor);
        this.active = index;
        this.cards.show(this.pnlContent, "CARD" + index);
        this.allVTabs.get(this.active).setBackground(this.activeColor);
    }

    /**
     * Gets the content panel.
     * 
     * @return JPanel
     */
    public JPanel getContentPanel() {
        return this.pnlContent;
    }

    /**
     * A single instance of a vertical tab, with paintComponent overridden to
     * provide vertical-ness. Also manages root level hover and click effects.
     * 
     */
    public class VTab extends JPanel {
        private String msg;
        private int id;

        // ID is used to retrieve this tab from the list of allVTabs.
        /**
         * Creates the actual clickable tab.
         * 
         * @param txt
         *            &emsp; String text in tab
         * @param i
         *            &emsp; int index
         */
        VTab(final String txt, final int i) {
            super();
            this.setLayout(new MigLayout("insets 0, gap 0"));
            this.setOpaque(false);
            this.msg = txt;
            this.id = i;

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(final MouseEvent e) {
                    if (VTab.this.id != FVerticalTabPanel.this.active) {
                        VTab.this.setBackground(FVerticalTabPanel.this.hoverColor);
                    }
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    if (VTab.this.id != FVerticalTabPanel.this.active) {
                        VTab.this.setBackground(FVerticalTabPanel.this.inactiveColor);
                    }
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                    FVerticalTabPanel.this.allVTabs.get(FVerticalTabPanel.this.active).setBackground(
                            FVerticalTabPanel.this.inactiveColor);
                    FVerticalTabPanel.this.active = VTab.this.id;
                    FVerticalTabPanel.this.cards.show(FVerticalTabPanel.this.pnlContent, "CARD" + VTab.this.id);
                    VTab.this.setBackground(FVerticalTabPanel.this.activeColor);
                }
            });

            // Resize adapter
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Careful with this font scale factor; the vertical tabs will be
                    // unreadable
                    // if small window, too big if large window.
                    setFont(FSkin.getFont((int) (h * 0.16)));
                }
            });
        }

        @Override
        protected void paintComponent(final Graphics g) {
            //super.paintComponent(g);
            w = this.getWidth();
            h = this.getHeight();

            g.setColor(this.getBackground());
            g.fillRoundRect(0, 0, w, h, 10, 10);
            g.fillRect(11, 0, w, h);

            // Rotate, draw string, rotate back (to allow hover border to be
            // painted properly)
            final Graphics2D g2d = (Graphics2D) g;
            final AffineTransform at = g2d.getTransform();

            if (tabsOnRightSide) {
                at.rotate(Math.toRadians(90), 0, 0);
                g2d.setTransform(at);
                g2d.setColor(FSkin.getColor(FSkin.Colors.CLR_TEXT));
                g2d.drawString(this.msg, 5, -4);
            } else {
                at.rotate(Math.toRadians(-90), 0, 0);
                g2d.setTransform(at);
                g2d.setColor(FSkin.getColor(FSkin.Colors.CLR_TEXT));
                // Rotated, so follows: (this.msg, vertical coord, horizontal coord)
                g2d.drawString(this.msg, 8 - h, w - 6);
            }

            if (tabsOnRightSide) {
                at.rotate(Math.toRadians(-90), 0, 0);
            } else {
                at.rotate(Math.toRadians(90), 0, 0);
            }

            g2d.setTransform(at);
        }

        /**
         * @param txt0
         *            &emsp; String
         */
        public void setText(String txt0) {
            this.msg = txt0;
        }
    }

    /** @return List<VTab> */
    public List<VTab> getAllVTabs() {
        return allVTabs;
    }
}
