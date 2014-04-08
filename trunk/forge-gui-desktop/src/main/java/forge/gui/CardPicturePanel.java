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

import forge.ImageCache;
import forge.ImageKeys;
import forge.card.CardCharacteristicName;
import forge.game.card.Card;
import forge.item.InventoryItem;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.imaging.FImagePanel;
import forge.toolbox.imaging.FImageUtil;
import forge.toolbox.imaging.FImagePanel.AutoSizeImageMode;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays image associated with a card or inventory item.
 * 
 * @version $Id: CardPicturePanel.java 25265 2014-03-27 02:18:47Z drdev $
 * 
 */
public final class CardPicturePanel extends JPanel {
    /** Constant <code>serialVersionUID=-3160874016387273383L</code>. */
    private static final long serialVersionUID = -3160874016387273383L;

    private Object displayed;

    private final FImagePanel panel;
    private BufferedImage currentImage;
    private boolean mayShowCard;

    public CardPicturePanel() {
        super(new BorderLayout());

        this.panel = new FImagePanel();
        this.add(this.panel);
    }

    public void setCard(final InventoryItem cp) {
        this.displayed = cp;
        this.mayShowCard = true;
        this.setImage();
    }

    //@Override
    public void setCard(final Card c, boolean mayShowCard) {
        this.displayed = c;
        this.mayShowCard = mayShowCard;
        this.setImage();
    }

    public void setCardImage(CardCharacteristicName flipState) {
        BufferedImage image = FImageUtil.getImage((Card)displayed, flipState);
        if (image != null && image != this.currentImage) {
            this.currentImage = image;
            this.panel.setImage(image, getAutoSizeImageMode());
        }
    }

    public void setImage() {
        BufferedImage image = getImage();
        if (image != null && image != this.currentImage) {
            this.currentImage = image;
            this.panel.setImage(image, getAutoSizeImageMode());
        }
    }

    public BufferedImage getImage() {
        if (displayed instanceof InventoryItem) {
            InventoryItem item = (InventoryItem) displayed;
            return ImageCache.getOriginalImage(ImageKeys.getImageKey(item, false), true);
        }
        else if (displayed instanceof Card) {
            if (mayShowCard) {
                return FImageUtil.getImage((Card)displayed);
            }
            return ImageCache.getOriginalImage(ImageKeys.TOKEN_PREFIX + ImageKeys.MORPH_IMAGE, true);
        }
        return null;
    }

    private AutoSizeImageMode getAutoSizeImageMode() {
        return (isUIScaleLarger() ? AutoSizeImageMode.PANEL : AutoSizeImageMode.SOURCE);
    }

    private boolean isUIScaleLarger() {
        return FModel.getPreferences().getPrefBoolean(FPref.UI_SCALE_LARGER);
    }

}
