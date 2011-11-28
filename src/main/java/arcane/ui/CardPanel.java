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
package arcane.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import arcane.ui.ScaledImagePanel.MultipassType;
import arcane.ui.ScaledImagePanel.ScalingType;
import arcane.ui.util.GlowText;
import arcane.ui.util.ManaSymbols;
import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardContainer;
import forge.Counters;
import forge.ImageCache;
import forge.Singletons;

/**
 * <p>
 * CardPanel class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardPanel extends JPanel implements CardContainer {
    /** Constant <code>serialVersionUID=2361907095724263295L</code>. */
    private static final long serialVersionUID = 2361907095724263295L;
    /**
     * Constant <code>TAPPED_ANGLE=Math.PI / 2</code>.
     */
    public static final double TAPPED_ANGLE = Math.PI / 2;
    /**
     * Constant <code>ASPECT_RATIO=3.5f / 2.5f</code>.
     */
    public static final float ASPECT_RATIO = 3.5f / 2.5f;

    /**
     * Constant <code>dragAnimationPanel</code>.
     */
    private static CardPanel dragAnimationPanel;

    /** Constant <code>ROUNDED_CORNER_SIZE=0.1f</code>. */
    private static final float ROUNDED_CORNER_SIZE = 0.1f;
    /** Constant <code>SELECTED_BORDER_SIZE=0.01f</code>. */
    private static final float SELECTED_BORDER_SIZE = 0.01f;
    /** Constant <code>BLACK_BORDER_SIZE=0.03f</code>. */
    private static final float BLACK_BORDER_SIZE = 0.03f;
    /** Constant <code>TEXT_GLOW_SIZE=6</code>. */
    private static final int TEXT_GLOW_SIZE = 6;
    /** Constant <code>TEXT_GLOW_INTENSITY=3f</code>. */
    private static final float TEXT_GLOW_INTENSITY = 3f;
    /**
     * Constant
     * <code>rotCenterToTopCorner=1.0295630140987000315797369464196f</code>.
     */
    private static final float ROT_CENTER_TO_TOP_CORNER = 1.0295630140987000315797369464196f;
    /**
     * Constant
     * <code>rotCenterToBottomCorner=0.7071067811865475244008443621048f</code>.
     */
    private static final float ROT_CENTER_TO_BOTTOM_CORNER = 0.7071067811865475244008443621048f;

    /**
     * 
     */
    private Card gameCard;
    /**
     * 
     */
    private CardPanel attachedToPanel;
    /**
     * 
     */
    private List<CardPanel> attachedPanels = new ArrayList<CardPanel>();
    /**
     * 
     */
    private boolean tapped;
    /**
     * 
     */
    private double tappedAngle = 0;
    /**
     * 
     */
    private final ScaledImagePanel imagePanel;

    private final GlowText titleText;
    private final GlowText ptText;
    private final List<CardPanel> imageLoadListeners = new ArrayList<CardPanel>(2);
    private boolean displayEnabled = true;
    private boolean isAnimationPanel;
    private int cardXOffset, cardYOffset, cardWidth, cardHeight;
    private boolean isSelected;
    private boolean showCastingCost;

    /**
     * <p>
     * Constructor for CardPanel.
     * </p>
     * 
     * @param newGameCard
     *            a {@link forge.Card} object.
     */
    public CardPanel(final Card newGameCard) {
        this.setGameCard(newGameCard);

        this.setBackground(Color.black);
        this.setOpaque(false);

        this.titleText = new GlowText();
        this.titleText.setFont(this.getFont().deriveFont(Font.BOLD, 13f));
        this.titleText.setForeground(Color.white);
        this.titleText.setGlow(Color.black, CardPanel.TEXT_GLOW_SIZE, CardPanel.TEXT_GLOW_INTENSITY);
        this.titleText.setWrap(true);
        this.add(this.titleText);

        this.ptText = new GlowText();
        this.ptText.setFont(this.getFont().deriveFont(Font.BOLD, 13f));
        this.ptText.setForeground(Color.white);
        this.ptText.setGlow(Color.black, CardPanel.TEXT_GLOW_SIZE, CardPanel.TEXT_GLOW_INTENSITY);
        this.add(this.ptText);

        this.imagePanel = new ScaledImagePanel();
        this.add(this.imagePanel);
        this.imagePanel.setScaleLarger(true);
        this.imagePanel.setScalingType(ScalingType.nearestNeighbor);
        this.imagePanel.setScalingBlur(true);
        this.imagePanel.setScalingMultiPassType(MultipassType.none);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                CardPanel.this.setCard(CardPanel.this.getGameCard());
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                CardPanel.this.setCard(CardPanel.this.getGameCard());
            }
        });

        this.setCard(newGameCard);
    }

    /**
     * <p>
     * setImage.
     * </p>
     * 
     * @param srcImage
     *            a {@link java.awt.Image} object.
     * @param srcImageBlurred
     *            a {@link java.awt.Image} object.
     * @param srcImageBlurred
     *            a {@link java.awt.Image} object.
     */
    private void setImage(final Image srcImage, final Image srcImageBlurred) {
        synchronized (this.imagePanel) {
            this.imagePanel.setImage(srcImage, srcImageBlurred);
            this.repaint();
            for (final CardPanel cardPanel : this.imageLoadListeners) {
                cardPanel.setImage(srcImage, srcImageBlurred);
                cardPanel.repaint();
            }
            this.imageLoadListeners.clear();
        }
        this.doLayout();
    }

    /**
     * <p>
     * setImage.
     * </p>
     * 
     * @param panel
     *            a {@link arcane.ui.CardPanel} object.
     */
    public final void setImage(final CardPanel panel) {
        synchronized (panel.imagePanel) {
            if (panel.imagePanel.hasImage()) {
                this.setImage(panel.imagePanel.getSrcImage(), panel.imagePanel.getSrcImageBlurred());
            } else {
                panel.imageLoadListeners.add(this);
            }
        }
    }

    /**
     * <p>
     * setScalingType.
     * </p>
     * 
     * @param scalingType
     *            a {@link arcane.ui.ScaledImagePanel.ScalingType} object.
     */
    public final void setScalingType(final ScalingType scalingType) {
        this.imagePanel.setScalingType(scalingType);
    }

    /**
     * <p>
     * Setter for the field <code>displayEnabled</code>.
     * </p>
     * 
     * @param displayEnabled
     *            a boolean.
     */
    public final void setDisplayEnabled(final boolean displayEnabled) {
        this.displayEnabled = displayEnabled;
    }

    /**
     * <p>
     * isDisplayEnabled.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isDisplayEnabled() {
        return this.displayEnabled;
    }

    /**
     * <p>
     * setAnimationPanel.
     * </p>
     * 
     * @param isAnimationPanel
     *            a boolean.
     */
    public final void setAnimationPanel(final boolean isAnimationPanel) {
        this.isAnimationPanel = isAnimationPanel;
    }

    /**
     * <p>
     * setSelected.
     * </p>
     * 
     * @param isSelected
     *            a boolean.
     */
    public final void setSelected(final boolean isSelected) {
        this.isSelected = isSelected;
        this.repaint();
    }

    /**
     * <p>
     * Setter for the field <code>showCastingCost</code>.
     * </p>
     * 
     * @param showCastingCost
     *            a boolean.
     */
    public final void setShowCastingCost(final boolean showCastingCost) {
        this.showCastingCost = showCastingCost;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics g) {
        if (!this.displayEnabled) {
            return;
        }
        if (!this.isValid()) {
            super.validate();
        }
        Graphics2D g2d = (Graphics2D) g;
        if (this.getTappedAngle() > 0) {
            g2d = (Graphics2D) g2d.create();
            final float edgeOffset = this.cardWidth / 2f;
            g2d.rotate(this.getTappedAngle(), this.cardXOffset + edgeOffset, (this.cardYOffset + this.cardHeight)
                    - edgeOffset);
        }
        super.paint(g2d);
    }

    /** {@inheritDoc} */
    @Override
    protected final void paintComponent(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // + White borders for Core sets Unlimited - 9th +
        final int cornerSize = Math.max(4, Math.round(this.cardWidth * CardPanel.ROUNDED_CORNER_SIZE));

        if (this.getGameCard() != null) {
            if ((!this.getGameCard().getImageFilename().equals("none"))
                    && (!this.getGameCard().getName().equals("Morph"))) {
                if ((this.getGameCard().getCurSetCode().equals("2ED"))
                        || (this.getGameCard().getCurSetCode().equals("3ED"))
                        || (this.getGameCard().getCurSetCode().equals("4ED"))
                        || (this.getGameCard().getCurSetCode().equals("5ED"))
                        || (this.getGameCard().getCurSetCode().equals("6ED"))
                        || (this.getGameCard().getCurSetCode().equals("7ED"))
                        || (this.getGameCard().getCurSetCode().equals("8ED"))
                        || (this.getGameCard().getCurSetCode().equals("9ED"))
                        || (this.getGameCard().getCurSetCode().equals("CHR"))
                        || (this.getGameCard().getCurSetCode().equals("S99"))
                        || (this.getGameCard().getCurSetCode().equals("PTK"))
                        || (this.getGameCard().getCurSetCode().equals("S00"))) {
                    if (!this.isSelected) {
                        g2d.setColor(Color.black);
                        final int offset = this.isTapped() ? 1 : 0;
                        for (int i = 1, n = Math.max(1, Math.round(this.cardWidth
                                * CardPanel.SELECTED_BORDER_SIZE)); i <= n; i++) {
                            g2d.drawRoundRect(this.cardXOffset - i, (this.cardYOffset - i) + offset,
                                    (this.cardWidth + (i * 2)) - 1, (this.cardHeight + (i * 2)) - 1, cornerSize,
                                    cornerSize);
                        }
                    }
                    g2d.setColor(Color.white);
                } else {
                    g2d.setColor(Color.black);
                }
            }
        }
        // - White borders for Core sets Unlimited - 9th -

        g2d.fillRoundRect(this.cardXOffset, this.cardYOffset, this.cardWidth, this.cardHeight, cornerSize, cornerSize);
        if (this.isSelected) {
            g2d.setColor(Color.green);
            final int offset = this.isTapped() ? 1 : 0;
            for (int i = 1, n = Math.max(1, Math.round(this.cardWidth * CardPanel.SELECTED_BORDER_SIZE)); i <= n; i++) {
                g2d.drawRoundRect(this.cardXOffset - i, (this.cardYOffset - i) + offset,
                        (this.cardWidth + (i * 2)) - 1, (this.cardHeight + (i * 2)) - 1, cornerSize, cornerSize);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void paintChildren(final Graphics g) {
        super.paintChildren(g);

        final boolean canDrawOverCard = this.showCastingCost && !this.isAnimationPanel;

        if (!canDrawOverCard) {
            return;
        }

        int width = ManaSymbols.getWidth(this.getGameCard().getManaCost());
        if (this.cardWidth < 200) {
            ManaSymbols.draw(g, this.getGameCard().getManaCost(), (this.cardXOffset + (this.cardWidth / 2))
                    - (width / 2), this.cardYOffset + (this.cardHeight / 2));
        }

        final int counters = this.getCard().getNumberOfCounters();

        if (counters == 1) {
            ManaSymbols.drawSymbol("counters1", g, this.cardXOffset - 15, (this.cardYOffset + this.cardHeight)
                    - (this.cardHeight / 3) - 40);
        } else if (counters == 2) {
            ManaSymbols.drawSymbol("counters2", g, this.cardXOffset - 15, (this.cardYOffset + this.cardHeight)
                    - (this.cardHeight / 3) - 40);
        } else if (counters == 3) {
            ManaSymbols.drawSymbol("counters3", g, this.cardXOffset - 15, (this.cardYOffset + this.cardHeight)
                    - (this.cardHeight / 3) - 40);
        } else if (counters > 3) {
            ManaSymbols.drawSymbol("countersMulti", g, this.cardXOffset - 15, (this.cardYOffset + this.cardHeight)
                    - (this.cardHeight / 3) - 40);
        }

        // int yOff = (cardHeight/4) + 2;
        if (this.getCard().isAttacking()) {
            ManaSymbols.drawSymbol("attack", g, (this.cardXOffset + (this.cardWidth / 4)) - 16,
                    (this.cardYOffset + this.cardHeight) - (this.cardHeight / 8) - 16);
        } else if (this.getCard().isBlocking()) {
            ManaSymbols.drawSymbol("defend", g, (this.cardXOffset + (this.cardWidth / 4)) - 16,
                    (this.cardYOffset + this.cardHeight) - (this.cardHeight / 8) - 16);
        }

        if (this.getCard().isCreature() && this.getCard().hasSickness() && AllZoneUtil.isCardInPlay(this.getCard())) {
            ManaSymbols.drawSymbol("summonsick", g, (this.cardXOffset + (this.cardWidth / 2)) - 16,
                    (this.cardYOffset + this.cardHeight) - (this.cardHeight / 8) - 16);
        }

        if (this.getCard().isPhasedOut()) {
            ManaSymbols.drawSymbol("phasing", g, (this.cardXOffset + (this.cardWidth / 2)) - 16,
                    (this.cardYOffset + this.cardHeight) - (this.cardHeight / 8) - 16);
        }

        if (this.getCard() != null) {
            if (this.getGameCard().getFoil() > 0) {
                final String fl = String.format("foil%02d", this.getCard().getFoil());
                final int z = Math.round(this.cardWidth * CardPanel.BLACK_BORDER_SIZE);
                ManaSymbols.draw(g, fl, this.cardXOffset + z, this.cardYOffset + z, this.cardWidth - (2 * z),
                        this.cardHeight - (2 * z));
            }

            if (this.getCard().getName().equals("Mana Pool") && !this.isAnimationPanel) {

                if (AllZone.getHumanPlayer().getManaPool() != null) {
                    final String s = AllZone.getHumanPlayer().getManaPool().getManaList();
                    if (!s.equals("|||||||||||")) {

                        final String[] mList = s.split("\\|", 12);

                        int n = 0;
                        for (int i = 0; i < 2; i++) {
                            for (int j = 0; j < 6; j++) {
                                if (!mList[n].equals("")) {
                                    width = ManaSymbols.getWidth(mList[n]);
                                    ManaSymbols.draw(g, mList[n], (this.cardXOffset + ((i + 1) * (this.cardWidth / 3)))
                                            - (width / 2), this.cardYOffset + ((j + 1) * (this.cardHeight / 7)));
                                }

                                n++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>
     * doLayout.
     * </p>
     * 
     * @since 1.0.15
     */
    @Override
    public final void doLayout() {
        final int borderSize = Math.round(this.cardWidth * CardPanel.BLACK_BORDER_SIZE);
        this.imagePanel.setLocation(this.cardXOffset + borderSize, this.cardYOffset + borderSize);
        this.imagePanel.setSize(this.cardWidth - (borderSize * 2), this.cardHeight - (borderSize * 2));

        final int fontHeight = Math.round(this.cardHeight * (27f / 680));
        final boolean showText = !this.imagePanel.hasImage() || (!this.isAnimationPanel && (fontHeight < 12));
        this.titleText.setVisible(showText);
        this.ptText.setVisible(showText);

        final int titleX = Math.round(this.cardWidth * (20f / 480));
        final int titleY = Math.round(this.cardHeight * (9f / 680));
        this.titleText.setBounds(this.cardXOffset + titleX, this.cardYOffset + titleY, this.cardWidth - titleX,
                this.cardHeight);

        final Dimension ptSize = this.ptText.getPreferredSize();
        this.ptText.setSize(ptSize.width, ptSize.height);
        final int ptX = Math.round(this.cardWidth * (420f / 480)) - (ptSize.width / 2);
        final int ptY = Math.round(this.cardHeight * (675f / 680)) - ptSize.height;
        this.ptText.setLocation((this.cardXOffset + ptX) - (CardPanel.TEXT_GLOW_SIZE / 2), (this.cardYOffset + ptY)
                - (CardPanel.TEXT_GLOW_SIZE / 2));

        if (this.isAnimationPanel || (this.cardWidth < 200)) {
            this.imagePanel.setScalingType(ScalingType.nearestNeighbor);
        } else {
            this.imagePanel.setScalingType(ScalingType.bilinear);
        }
    }

    /**
     * <p>
     * toString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String toString() {
        return this.getGameCard().getName();
    }

    /**
     * <p>
     * setCardBounds.
     * </p>
     * 
     * @param x
     *            a int.
     * @param y
     *            a int.
     * @param width
     *            a int.
     * @param height
     *            a int.
     */
    public final void setCardBounds(final int x, final int y, int width, int height) {
        this.cardWidth = width;
        this.cardHeight = height;
        final int rotCenterX = Math.round(width / 2f);
        final int rotCenterY = height - rotCenterX;
        final int rotCenterToTopCorner = Math.round(width * CardPanel.ROT_CENTER_TO_TOP_CORNER);
        final int rotCenterToBottomCorner = Math.round(width * CardPanel.ROT_CENTER_TO_BOTTOM_CORNER);
        final int xOffset = rotCenterX - rotCenterToBottomCorner;
        final int yOffset = rotCenterY - rotCenterToTopCorner;
        this.cardXOffset = -xOffset;
        this.cardYOffset = -yOffset;
        width = -xOffset + rotCenterX + rotCenterToTopCorner;
        height = -yOffset + rotCenterY + rotCenterToBottomCorner;
        this.setBounds(x + xOffset, y + yOffset, width, height);
    }

    /**
     * <p>
     * repaint.
     * </p>
     */
    @Override
    public final void repaint() {
        final Rectangle b = this.getBounds();
        final JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane == null) {
            return;
        }
        final Point p = SwingUtilities.convertPoint(this.getParent(), b.x, b.y, rootPane);
        rootPane.repaint(p.x, p.y, b.width, b.height);
    }

    /**
     * <p>
     * getCardX.
     * </p>
     * 
     * @return a int.
     */
    public final int getCardX() {
        return this.getX() + this.cardXOffset;
    }

    /**
     * <p>
     * getCardY.
     * </p>
     * 
     * @return a int.
     */
    public final int getCardY() {
        return this.getY() + this.cardYOffset;
    }

    /**
     * <p>
     * Getter for the field <code>cardWidth</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getCardWidth() {
        return this.cardWidth;
    }

    /**
     * <p>
     * Getter for the field <code>cardHeight</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getCardHeight() {
        return this.cardHeight;
    }

    /**
     * <p>
     * getCardLocation.
     * </p>
     * 
     * @return a {@link java.awt.Point} object.
     */
    public final Point getCardLocation() {
        final Point p = this.getLocation();
        p.x += this.cardXOffset;
        p.y += this.cardYOffset;
        return p;
    }

    /**
     * <p>
     * setText.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     */
    public final void setText(final Card card) {
        if ((card == null) || !Singletons.getModel().getPreferences().isCardOverlay()) {
            return;
        }

        if (card.isFaceDown()) {
            this.titleText.setText("");
            this.showCastingCost = false;
        } else {
            this.titleText.setText(card.getName());
            this.showCastingCost = true;
        }

        if (card.isCreature() && card.isPlaneswalker()) {
            this.ptText.setText(card.getNetAttack() + "/" + card.getNetDefense() + " ("
                    + String.valueOf(card.getCounters(Counters.LOYALTY)) + ")");
        } else if (card.isCreature()) {
            this.ptText.setText(card.getNetAttack() + "/" + card.getNetDefense());
        } else if (card.isPlaneswalker()) {
            this.ptText.setText(String.valueOf(card.getCounters(Counters.LOYALTY)));
        } else {
            this.ptText.setText("");
        }
    }

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
    @Override
    public final Card getCard() {
        return this.getGameCard();
    }

    /** {@inheritDoc} */
    @Override
    public final void setCard(final Card card) {
        if ((this.getGameCard() != null) && this.getGameCard().equals(card) && this.isAnimationPanel
                && this.imagePanel.hasImage()) {
            return;
        }
        this.setGameCard(card);
        if (!this.isShowing()) {
            return;
        }
        final Insets i = this.getInsets();
        final Image image = card == null ? null : ImageCache.getImage(card, this.getWidth() - i.left - i.right,
                this.getHeight() - i.top - i.bottom);
        if ((this.getGameCard() != null) && Singletons.getModel().getPreferences().isCardOverlay()) {
            this.setText(this.getGameCard());
        }

        this.setImage(image, image);
    }

    /**
     * Gets the game card.
     * 
     * @return the gameCard
     */
    public final Card getGameCard() {
        return this.gameCard;
    }

    /**
     * Sets the game card.
     * 
     * @param gameCard
     *            the gameCard to set
     */
    public final void setGameCard(final Card gameCard) {
        this.gameCard = gameCard; // TODO: Add 0 to parameter's name.
    }

    /**
     * Gets the drag animation panel.
     * 
     * @return the dragAnimationPanel
     */
    public static CardPanel getDragAnimationPanel() {
        return CardPanel.dragAnimationPanel;
    }

    /**
     * Sets the drag animation panel.
     * 
     * @param dragAnimationPanel
     *            the dragAnimationPanel to set
     */
    public static void setDragAnimationPanel(final CardPanel dragAnimationPanel) {
        CardPanel.dragAnimationPanel = dragAnimationPanel; // TODO: Add 0 to
                                                           // parameter's name.
    }

    /**
     * Gets the attached to panel.
     * 
     * @return the attachedToPanel
     */
    public final CardPanel getAttachedToPanel() {
        return this.attachedToPanel;
    }

    /**
     * Sets the attached to panel.
     * 
     * @param attachedToPanel
     *            the attachedToPanel to set
     */
    public final void setAttachedToPanel(final CardPanel attachedToPanel) {
        this.attachedToPanel = attachedToPanel; // TODO: Add 0 to parameter's
                                                // name.
    }

    /**
     * Gets the attached panels.
     * 
     * @return the attachedPanels
     */
    public final List<CardPanel> getAttachedPanels() {
        return this.attachedPanels;
    }

    /**
     * Sets the attached panels.
     * 
     * @param attachedPanels
     *            the attachedPanels to set
     */
    public final void setAttachedPanels(final List<CardPanel> attachedPanels) {
        this.attachedPanels = attachedPanels; // TODO: Add 0 to parameter's
                                              // name.
    }

    /**
     * Checks if is tapped.
     * 
     * @return the tapped
     */
    public final boolean isTapped() {
        return this.tapped;
    }

    /**
     * Sets the tapped.
     * 
     * @param tapped
     *            the tapped to set
     */
    public final void setTapped(final boolean tapped) {
        this.tapped = tapped; // TODO: Add 0 to parameter's name.
    }

    /**
     * Gets the tapped angle.
     * 
     * @return the tappedAngle
     */
    public final double getTappedAngle() {
        return this.tappedAngle;
    }

    /**
     * Sets the tapped angle.
     * 
     * @param tappedAngle
     *            the tappedAngle to set
     */
    public final void setTappedAngle(final double tappedAngle) {
        this.tappedAngle = tappedAngle; // TODO: Add 0 to parameter's name.
    }
}
