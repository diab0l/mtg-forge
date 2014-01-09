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
package forge.gui.match;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import forge.Singletons;
import forge.game.card.Card;
import forge.game.combat.Combat;
import forge.gui.framework.FScreen;
import forge.gui.match.controllers.CDock;
import forge.gui.match.views.VField;
import forge.gui.toolbox.FSkin;
import forge.view.FView;
import forge.view.arcane.CardPanel;

/**
 * Semi-transparent overlay panel. Should be used with layered panes.
 * 
 */

@SuppressWarnings("serial")
public enum TargetingOverlay {
    /** */
    SINGLETON_INSTANCE;

    private final JPanel pnl = new OverlayPanel();
    private final List<CardPanel> cardPanels = new ArrayList<CardPanel>();
    private final List<Point[]> arcs = new ArrayList<Point[]>();

    private CardPanel activePanel = null;

    /**
     * Semi-transparent overlay panel. Should be used with layered panes.
     */
    private TargetingOverlay() {
        pnl.setOpaque(false);
        pnl.setVisible(false);
        pnl.setFocusTraversalKeysEnabled(false);
        FSkin.get(pnl).setBackground(FSkin.getColor(FSkin.Colors.CLR_ZEBRA));
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPanel() {
        return this.pnl;
    }

    // TODO - this is called every repaint, regardless if card
    // positions have changed or not.  Could perform better if
    // it checked for a state change.  Doublestrike 28-09-12
    private void assembleArcs(Combat combat) {
        //List<VField> fields = VMatchUI.SINGLETON_INSTANCE.getFieldViews();

        switch (CDock.SINGLETON_INSTANCE.getArcState()) {
            case 0:
                return;
            case 1:
                // Draw only hovered card
                activePanel = null;
                for (VField f : VMatchUI.SINGLETON_INSTANCE.getFieldViews()) {
                    cardPanels.addAll(f.getTabletop().getCardPanels());
                    List<CardPanel> cPanels = f.getTabletop().getCardPanels();
                    for (CardPanel c : cPanels) {
                        if (c.isSelected()) {
                            activePanel = c;
                            break;
                        }
                    }
                }
                if (activePanel == null) { return; }
                break;
            default:
                // Draw all
                for (VField f : VMatchUI.SINGLETON_INSTANCE.getFieldViews()) {
                    cardPanels.addAll(f.getTabletop().getCardPanels());
                }
        }

        //final Point docOffsets = FView.SINGLETON_INSTANCE.getLpnDocument().getLocationOnScreen();
        // Locations of arc endpoint, per card, with ID as primary key.
        final Map<Integer, Point> endpoints = new HashMap<Integer, Point>();

        Point cardLocOnScreen;
        Point locOnScreen = this.getPanel().getLocationOnScreen();

        for (CardPanel c : cardPanels) {
            if (c.isShowing()) {
	            cardLocOnScreen = c.getCardLocationOnScreen();
	            endpoints.put(c.getCard().getUniqueNumber(), new Point(
	                (int) (cardLocOnScreen.getX() - locOnScreen.getX() + c.getWidth() / 4),
	                (int) (cardLocOnScreen.getY() - locOnScreen.getY() + c.getHeight() / 4)
	            ));
            }
        }

        List<Card> temp = new ArrayList<Card>();

        if (CDock.SINGLETON_INSTANCE.getArcState() == 1) {
            // Only work with the active panel
            Card c = activePanel.getCard();

            Card enchanting = c.getEnchantingCard();
            Card equipping = c.getEquippingCard();
            Card fortifying = c.getFortifyingCard();
            List<Card> enchantedBy = c.getEnchantedBy();
            List<Card> equippedBy = c.getEquippedBy();
            List<Card> fortifiedBy = c.getFortifiedBy();
            Card paired = c.getPairedWith();

            if (null != enchanting) {
                if (!enchanting.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(enchanting.getUniqueNumber()),
                        endpoints.get(c.getUniqueNumber())
                    });
                }
            }

            if (null != equipping) {
                if (!equipping.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(equipping.getUniqueNumber()),
                        endpoints.get(c.getUniqueNumber())
                    });
                }
            }

            if (null != fortifying) {
                if (!fortifying.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(fortifying.getUniqueNumber()),
                        endpoints.get(c.getUniqueNumber())
                    });
                }
            }

            if (null != enchantedBy) {
                for (Card enc : enchantedBy) {
                    if (!enc.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(c.getUniqueNumber()),
                        endpoints.get(enc.getUniqueNumber())
                    });
                    }
                }
            }

            if (null != equippedBy) {
                for (Card eq : equippedBy) {
                    if (!eq.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(c.getUniqueNumber()),
                        endpoints.get(eq.getUniqueNumber())
                    });
                    }
                }
            }

            if (null != fortifiedBy) {
                for (Card eq : fortifiedBy) {
                    if (!eq.getController().equals(c.getController())) {
                    arcs.add(new Point[] {
                        endpoints.get(c.getUniqueNumber()),
                        endpoints.get(eq.getUniqueNumber())
                    });
                    }
                }
            }

            if (null != paired) {
                arcs.add(new Point[] {
                    endpoints.get(paired.getUniqueNumber()),
                    endpoints.get(c.getUniqueNumber())
                });
            }

            if ( null != combat ) { 
                for (Card attackingCard : combat.getAttackers()) {
                    temp = combat.getBlockers(attackingCard);
                    for (Card blockingCard : temp) {
                        if (!attackingCard.equals(c) && !blockingCard.equals(c)) { continue; }
                        arcs.add(new Point[] {
                            endpoints.get(attackingCard.getUniqueNumber()),
                            endpoints.get(blockingCard.getUniqueNumber())
                        });
                    }
                }
            }
        } else {
            // Work with all card panels currently visible

            // Global cards
            for (CardPanel c : cardPanels) {
                if (!c.isShowing()) {
                    continue;
                }
                Card card = c.getCard();

                // Enchantments
                Card enchanting = card.getEnchantingCard();
                if (enchanting != null) {
                    if (enchanting.getController().equals(card.getController())) {
                        continue;
                    }
                    arcs.add(new Point[]{
                                endpoints.get(enchanting.getUniqueNumber()),
                                endpoints.get(card.getUniqueNumber())
                            });
                }
            }

            // Combat cards
            if ( null != combat ) 
                for (Card attackingCard : combat.getAttackers()) {
                    temp = combat.getBlockers(attackingCard);
                    for (Card blockingCard : temp) {
                        arcs.add(new Point[]{
                            endpoints.get(attackingCard.getUniqueNumber()),
                            endpoints.get(blockingCard.getUniqueNumber())
                        });
                    }
                }
        }

        temp.clear();
        endpoints.clear();
    }

    private class OverlayPanel extends JPanel {
        /**
         * For some reason, the alpha channel background doesn't work properly on
         * Windows 7, so the paintComponent override is required for a
         * semi-transparent overlay.
         * 
         * @param g
         *            &emsp; Graphics object
         */

        // Arrow drawing code by the MAGE team, used with permission.
        private Area getArrow(float length, float bendPercent) {
            float p1x = 0, p1y = 0;
            float p2x = length, p2y = 0;
            float cx = length / 2, cy = length / 8f * bendPercent;

            int bodyWidth = 10;
            float headSize = 17;

            float adjSize, ex, ey, abs_e;
            adjSize = (float) (bodyWidth / 2 / Math.sqrt(2));
            ex = p2x - cx;
            ey = p2y - cy;
            abs_e = (float) Math.sqrt(ex * ex + ey * ey);
            ex /= abs_e;
            ey /= abs_e;
            GeneralPath bodyPath = new GeneralPath();
            bodyPath.moveTo(p2x + (ey - ex) * adjSize, p2y - (ex + ey) * adjSize);
            bodyPath.quadTo(cx, cy, p1x, p1y - bodyWidth / 2);
            bodyPath.lineTo(p1x, p1y + bodyWidth / 2);
            bodyPath.quadTo(cx, cy, p2x - (ey + ex) * adjSize, p2y + (ex - ey) * adjSize);
            bodyPath.closePath();

            adjSize = (float) (headSize / Math.sqrt(2));
            ex = p2x - cx;
            ey = p2y - cy;
            abs_e = (float) Math.sqrt(ex * ex + ey * ey);
            ex /= abs_e;
            ey /= abs_e;
            GeneralPath headPath = new GeneralPath();
            headPath.moveTo(p2x - (ey + ex) * adjSize, p2y + (ex - ey) * adjSize);
            headPath.lineTo(p2x, p2y);
            headPath.lineTo(p2x + (ey - ex) * adjSize, p2y - (ex + ey) * adjSize);
            headPath.closePath();

            Area area = new Area(headPath);
            area.add(new Area(bodyPath));
            return area;
        }

        private void drawArrow(Graphics2D g2d, int startX, int startY, int endX, int endY, FSkin.SkinColor skinColor) {
            float ex = endX - startX;
            float ey = endY - startY;
            if (ex == 0 && ey == 0) { return; }

            float length = (float) Math.sqrt(ex * ex + ey * ey);
            float bendPercent = (float) Math.asin(ey / length);

            if (endX > startX) {
                bendPercent = -bendPercent;
            }

            Area arrow = getArrow(length, bendPercent);
            AffineTransform af = g2d.getTransform();

            g2d.translate(startX, startY);
            g2d.rotate(Math.atan2(ey, ex));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            FSkin.setGraphicsColor(g2d, skinColor);
            g2d.fill(arrow);
            g2d.setColor(Color.BLACK);
            g2d.draw(arrow);

            g2d.setTransform(af);
        }

        @Override
        public void paintComponent(final Graphics g) {
            final Combat combat = Singletons.getControl().getObservedGame().getCombat(); // this will get deprecated too
            // No need for this except in match view
            if (Singletons.getControl().getCurrentScreen() != FScreen.MATCH_SCREEN) { return; }

            super.paintComponent(g);

            // 0 is off
            int overlaystate = CDock.SINGLETON_INSTANCE.getArcState();
            if (overlaystate == 0) { return; }

            // Arc drawing
            arcs.clear();
            cardPanels.clear();
            
            assembleArcs(combat);
            if (arcs.isEmpty()) { return; }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FSkin.SkinColor skinColor = FSkin.getColor(FSkin.Colors.CLR_ACTIVE);

            for (Point[] p : arcs) {
                if (p[0] == null || p[1] == null) {
                    continue;
                }

                int endX = (int) p[0].getX();
                int endY = (int) p[0].getY();
                int startX = (int) p[1].getX();
                int startY = (int) p[1].getY();

                drawArrow(g2d, startX, startY, endX, endY, skinColor);
            }

            FView.SINGLETON_INSTANCE.getFrame().repaint(); // repaint the match UI
        }

    }
}
