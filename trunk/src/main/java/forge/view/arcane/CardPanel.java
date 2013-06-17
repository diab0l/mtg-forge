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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import forge.Card;
import forge.CardCharacteristicName;
import forge.CounterType;
import forge.ImageCache;
import forge.Singletons;
import forge.card.CardEdition;
import forge.card.mana.ManaCost;
import forge.gui.CardContainer;
import forge.gui.toolbox.CardFaceSymbols;
import forge.properties.ForgePreferences.FPref;
import forge.view.arcane.util.OutlinedLabel;

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

    private final OutlinedLabel titleText;
    private final OutlinedLabel ptText;
    private final OutlinedLabel damageText;
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

        this.titleText = new OutlinedLabel();
        this.titleText.setFont(this.getFont().deriveFont(Font.BOLD, 13f));
        this.titleText.setForeground(Color.white);
        this.titleText.setGlow(Color.black);
        this.titleText.setWrap(true);
        this.add(this.titleText);

        this.ptText = new OutlinedLabel();
        this.ptText.setFont(this.getFont().deriveFont(Font.BOLD, 13f));
        this.ptText.setForeground(Color.white);
        this.ptText.setGlow(Color.black);
        this.add(this.ptText);

        this.damageText = new OutlinedLabel();
        this.damageText.setFont(this.getFont().deriveFont(Font.BOLD, 15f));
        this.damageText.setForeground(new Color(160,0,0));
        this.damageText.setGlow(Color.white);
        this.add(this.damageText);
        
        
        this.imagePanel = new ScaledImagePanel();
        this.add(this.imagePanel);
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
    private void setImage(final BufferedImage srcImage) {
        synchronized (this.imagePanel) {
            this.imagePanel.setImage(srcImage);
            this.repaint();
            for (final CardPanel cardPanel : this.imageLoadListeners) {
                cardPanel.setImage(srcImage);
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
     *            a {@link forge.view.arcane.CardPanel} object.
     */
    public final void setImage(final CardPanel panel) {
        synchronized (panel.imagePanel) {
            if (panel.imagePanel.hasImage()) {
                this.setImage(panel.imagePanel.getSrcImage());
            } else {
                panel.imageLoadListeners.add(this);
            }
        }
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
     * isSelected.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isSelected() {
        return this.isSelected;
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

        final int cornerSize = Math.max(4, Math.round(this.cardWidth * CardPanel.ROUNDED_CORNER_SIZE));
        final int offset = this.isTapped() ? 1 : 0;

        // Magenta outline for when card was chosen to pay
        if (this.getCard().isUsedToPay()) {
            g2d.setColor(Color.magenta);
            final int n2 = Math.max(1, Math.round(2 * this.cardWidth * CardPanel.SELECTED_BORDER_SIZE));
            g2d.fillRoundRect(this.cardXOffset - n2, (this.cardYOffset - n2) + offset, this.cardWidth + (n2 * 2), this.cardHeight + (n2 * 2), cornerSize + n2, cornerSize + n2);
        }

        // Green outline for hover
        if( this.isSelected ) {
            g2d.setColor(Color.green);
            final int n = Math.max(1, Math.round(this.cardWidth * CardPanel.SELECTED_BORDER_SIZE));
            g2d.fillRoundRect(this.cardXOffset - n, (this.cardYOffset - n) + offset, this.cardWidth + (n * 2), this.cardHeight + (n * 2), cornerSize + n , cornerSize + n);
        }
        
        // Black fill - (will become outline for white bordered cards) 
        final int n = 0;
        g2d.setColor(Color.black);
        g2d.fillRoundRect(this.cardXOffset - n, (this.cardYOffset - n) + offset, this.cardWidth + (n * 2), this.cardHeight + (n * 2), cornerSize + n , cornerSize + n);
        
        // White border if card is known to have it.
        if (this.getGameCard() != null && !this.getGameCard().isFaceDown()) {
            CardEdition ed = Singletons.getModel().getEditions().get(this.getGameCard().getCurSetCode());
            if (ed != null && ed.isWhiteBorder()) {
                g2d.setColor(Color.white);
                int ins = 1;
                g2d.fillRoundRect(this.cardXOffset + ins, this.cardYOffset + ins, this.cardWidth - ins*2, this.cardHeight - ins*2, cornerSize-ins, cornerSize-ins);
            }
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param g
     * @param manaCost 
     */
    private void drawManaCost(final Graphics g, ManaCost cost, int deltaY ) {
        int width = CardFaceSymbols.getWidth(cost);
        int height = CardFaceSymbols.getHeight();
        CardFaceSymbols.draw(g, cost, (this.cardXOffset + (this.cardWidth / 2)) - (width / 2), deltaY + this.cardYOffset + (this.cardHeight / 2) - height/2);
    }

    /** {@inheritDoc} */
    @Override
    protected final void paintChildren(final Graphics g) {
        super.paintChildren(g);

        if (this.isAnimationPanel) {
            return;
        }

        if (this.showCastingCost && this.cardWidth < 200) {
            Card gameCard = this.getGameCard();
            boolean showSplitMana = gameCard.isSplitCard() && gameCard.getCurState() == CardCharacteristicName.Original;
            if ( !showSplitMana ) {
                drawManaCost(g, gameCard.getManaCost(), 0);
            } else {
                drawManaCost(g, gameCard.getRules().getMainPart().getManaCost(), +12);
                drawManaCost(g, gameCard.getRules().getOtherPart().getManaCost(), -12);
            }
        }

        Card card = this.getCard();
        if (card == null) {
            return;
        }

        int number = 0;
        for (final Integer i : card.getCounters().values()) {
            number += i.intValue();
        }

        final int counters = number;
        final int yCounters = (this.cardYOffset + this.cardHeight) - (this.cardHeight / 3) - 40;

        if (counters == 1) {
            CardFaceSymbols.drawSymbol("counters1", g, this.cardXOffset - 15, yCounters);
        } else if (counters == 2) {
            CardFaceSymbols.drawSymbol("counters2", g, this.cardXOffset - 15, yCounters);
        } else if (counters == 3) {
            CardFaceSymbols.drawSymbol("counters3", g, this.cardXOffset - 15, yCounters);
        } else if (counters > 3) {
            CardFaceSymbols.drawSymbol("countersMulti", g, this.cardXOffset - 15, yCounters);
        }

        final int combatXSymbols = (this.cardXOffset + (this.cardWidth / 4)) - 16;
        final int stateXSymbols = (this.cardXOffset + (this.cardWidth / 2)) - 16;
        final int ySymbols = (this.cardYOffset + this.cardHeight) - (this.cardHeight / 8) - 16; 
        // int yOff = (cardHeight/4) + 2;
        if (card.isAttacking()) {
            CardFaceSymbols.drawSymbol("attack", g, combatXSymbols, ySymbols);
        } else if (card.isBlocking()) {
            CardFaceSymbols.drawSymbol("defend", g, combatXSymbols, ySymbols);
        }

        if (card.isSick() && card.isInPlay()) {
            CardFaceSymbols.drawSymbol("summonsick", g, stateXSymbols, ySymbols);
        }

        if (card.isPhasedOut()) {
            CardFaceSymbols.drawSymbol("phasing", g, stateXSymbols, ySymbols);
        }

        if (card.isUsedToPay()) {
            CardFaceSymbols.drawSymbol("sacrifice", g, (this.cardXOffset + (this.cardWidth / 2)) - 20,
                    (this.cardYOffset + (this.cardHeight / 2)) - 20);
        }

        if (this.getGameCard().getFoil() > 0) {
            final String fl = String.format("foil%02d", card.getFoil());
            final int z = Math.round(this.cardWidth * CardPanel.BLACK_BORDER_SIZE);
            CardFaceSymbols.drawOther(g, fl, this.cardXOffset + z, this.cardYOffset + z, this.cardWidth - (2 * z),
                    this.cardHeight - (2 * z));
        }
    }

    @Override
    public final void doLayout() {
        final int borderSize = Math.round(this.cardWidth * CardPanel.BLACK_BORDER_SIZE);

        final Point imgPos = new Point(this.cardXOffset + borderSize, this.cardYOffset + borderSize);
        final Dimension imgSize = new Dimension(this.cardWidth - (borderSize * 2), this.cardHeight - (borderSize * 2));

        this.imagePanel.setLocation(imgPos);
        this.imagePanel.setSize(imgSize);

        final int fontHeight = Math.round(this.cardHeight * (27f / 680));
        final boolean showText = !this.imagePanel.hasImage() || (!this.isAnimationPanel && (fontHeight < 12));
        this.titleText.setVisible(showText);
        this.ptText.setVisible(showText);
        this.damageText.setVisible(showText);

        final int titleX = Math.round(imgSize.width * (24f / 480));
        final int titleY = Math.round(imgSize.height * (54f / 640)) - 15;
        final int titleH = Math.round(imgSize.height * (360f / 640));
        this.titleText.setBounds(imgPos.x + titleX, imgPos.y + titleY, imgSize.width - 2 * titleX, titleH - titleY);

        final int rightLine = Math.round(imgSize.width * (412f / 480)); 

        final Dimension ptSize = this.ptText.getPreferredSize();
        this.ptText.setSize(ptSize.width, ptSize.height);
        final int ptX = rightLine - ptSize.width/2;
        final int ptY = Math.round(imgSize.height * (650f / 680)) - 13;
        this.ptText.setLocation(imgPos.x + ptX, imgPos.y + ptY);

        final Dimension dmgSize = this.damageText.getPreferredSize();
        this.damageText.setSize(dmgSize.width, dmgSize.height);
        final int dmgX = rightLine - dmgSize.width / 2;
        final int dmgY =  ptY - 16;
        this.damageText.setLocation(imgPos.x + dmgX, imgPos.y + dmgY);
    }


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
        if ((card == null) || !Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_CARD_OVERLAY)) {
            return;
        }

        if (card.isFaceDown()) {
            this.titleText.setText("");
            this.showCastingCost = false;
        } else {
            this.titleText.setText(card.getName());
            this.showCastingCost = true;
        }

        String sPt = "";
        if (card.isCreature() && card.isPlaneswalker()) {
            sPt = String.format("%d/%d (%d)", card.getNetAttack(), card.getNetDefense(), card.getCounters(CounterType.LOYALTY));
        } else if (card.isCreature()) {
            sPt = String.format("%d/%d", card.getNetAttack(), card.getNetDefense());
        } else if (card.isPlaneswalker()) {
            int loyalty = card.getCounters(CounterType.LOYALTY);
            sPt = String.valueOf(loyalty == 0 ? card.getBaseLoyalty() : loyalty);
        }
        this.ptText.setText(sPt);
        
        int damage = card.getDamage();
        this.damageText.setText(damage > 0 ? "\u00BB " + String.valueOf(damage) + " \u00AB" : "");

    }

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
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

        final BufferedImage image = card == null ? null : ImageCache.getImage(card, imagePanel.getWidth(), imagePanel.getHeight());
        if ((this.getGameCard() != null) && Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_CARD_OVERLAY)) {
            this.setText(this.getGameCard());
        }

        this.setImage(image);
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
     * @param gameCard0
     *            the gameCard to set
     */
    public final void setGameCard(final Card gameCard0) {
        this.gameCard = gameCard0;
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
     * @param dragAnimationPanel0
     *            the dragAnimationPanel to set
     */
    public static void setDragAnimationPanel(final CardPanel dragAnimationPanel0) {
        CardPanel.dragAnimationPanel = dragAnimationPanel0;
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
     * @param attachedToPanel0
     *            the attachedToPanel to set
     */
    public final void setAttachedToPanel(final CardPanel attachedToPanel0) {
        this.attachedToPanel = attachedToPanel0;
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
     * @param attachedPanels0
     *            the attachedPanels to set
     */
    public final void setAttachedPanels(final List<CardPanel> attachedPanels0) {
        this.attachedPanels = attachedPanels0;
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
     * @param tapped0
     *            the tapped to set
     */
    public final void setTapped(final boolean tapped0) {
        this.tapped = tapped0;
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
     * @param tappedAngle0
     *            the tappedAngle to set
     */
    public final void setTappedAngle(final double tappedAngle0) {
        this.tappedAngle = tappedAngle0;
    }
}
