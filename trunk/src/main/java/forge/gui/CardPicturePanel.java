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

package forge.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import forge.Card;
import forge.ImageCache;
import forge.gui.toolbox.CardFaceSymbols;
import forge.item.InventoryItem;
import forge.view.arcane.ScaledImagePanel;
import java.awt.image.ColorModel;

/**
 * The class CardPicturePanel. Shows the full-sized image in a label. if there's
 * no picture, the cardname is displayed instead.
 * 
 * @author Clemens Koza
 * @version V0.0 17.02.2010
 */
public final class CardPicturePanel extends JPanel {
    /** Constant <code>serialVersionUID=-3160874016387273383L</code>. */
    private static final long serialVersionUID = -3160874016387273383L;

    private Object displayed;

    private final ScaledImagePanel panel;
    private BufferedImage currentImage;

    public CardPicturePanel() {
        super(new BorderLayout());

        this.panel = new ScaledImagePanel();
        this.add(this.panel);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                CardPicturePanel.this.update();
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                CardPicturePanel.this.update();
            }
        });
    }

    public void update() {
        this.setImage();
    }

    public void setCard(final InventoryItem cp) {
        this.displayed = cp;
        update();
    }

    //@Override
    public void setCard(final Card c) {
        this.displayed = c;
        update();
    }

    public void setImage() {        
        BufferedImage image = getImage();
        if (image != null && image != this.currentImage) {
            this.currentImage = image;
            this.panel.setImage(image);
            this.panel.repaint();
        }        
    }
    
    public BufferedImage getImage() {

        final Insets i = this.getInsets();
        BufferedImage image = null;
        int foilIndex = 0;
        
        if (displayed instanceof InventoryItem) {
            image = ImageCache.getImage(
                        (InventoryItem)this.displayed, 
                        this.getWidth() - i.left - i.right, 
                        this.getHeight() - i.top - i.bottom);
        
        } else if ( displayed instanceof Card ) {
            image = ImageCache.getImage(
                        (Card)this.displayed, 
                        this.getWidth() - i.left - i.right - 2, 
                        this.getHeight() - i.top - i.bottom - 2);
            foilIndex = ((Card)this.displayed).getFoil();
        }

        if (image != null && foilIndex > 0) { 
            image = getFoiledImage(image, foilIndex);
        }
        
        return image;
    }
    
    private BufferedImage getFoiledImage(BufferedImage plainImage, int foilIndex) {
        ColorModel cm = plainImage.getColorModel();
        BufferedImage foilImage = new BufferedImage(cm, plainImage.copyData(null), cm.isAlphaPremultiplied(), null);
        final String fl = String.format("foil%02d", foilIndex);
        CardFaceSymbols.drawOther(foilImage.getGraphics(), fl, 0, 0, foilImage.getWidth(), foilImage.getHeight());
        return foilImage;                
    }
    
}
