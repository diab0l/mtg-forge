/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2013  Forge Team
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

package forge.toolbox.imaging;

import forge.ImageCache;
import forge.card.CardCharacteristicName;
import forge.game.card.Card;
import forge.model.FModel;
import forge.properties.ForgePreferences;
import forge.toolbox.CardFaceSymbols;
import forge.toolbox.FSkin.SkinIcon;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * Common image-related routines specific to Forge images. 
 * 
 * @version $Id: FImageUtil.java 25265 2014-03-27 02:18:47Z drdev $
 * 
 */
public final class FImageUtil {  
    private FImageUtil() {}
    
    public static BufferedImage getImage(Card card, CardCharacteristicName state) {
        BufferedImage image = ImageCache.getOriginalImage(card.getImageKey(state), true);
        int foilIndex = card.getFoil();
        if (image != null && foilIndex > 0) { 
            image = getImageWithFoilEffect(image, foilIndex);
        }
        return image;
    }
    
    /**
     * Gets the image associated with a card.
     * <p>
     * Adds a random foil effect if enabled.
     * <p>
     * For double-sided cards, returns the front-side image.<br>
     * For flip cards, returns the un-flipped image. 
     */
    public static BufferedImage getImage(Card card) {
        BufferedImage image = ImageCache.getOriginalImage(card.getImageKey(), true);
        int foilIndex = card.getFoil();
        if (image != null && foilIndex > 0) { 
            image = getImageWithFoilEffect(image, foilIndex);
        }
        return image;
    }
        
    /**
     * Applies a foil effect to a card image.
     */
    private static BufferedImage getImageWithFoilEffect(BufferedImage plainImage, int foilIndex) {
        if (!FModel.getPreferences().getPrefBoolean(ForgePreferences.FPref.UI_OVERLAY_FOIL_EFFECT)) {
            return plainImage;
        }

        ColorModel cm = plainImage.getColorModel();
        BufferedImage foilImage = new BufferedImage(cm, plainImage.copyData(null), cm.isAlphaPremultiplied(), null);
        final String fl = String.format("foil%02d", foilIndex);
        CardFaceSymbols.drawOther(foilImage.getGraphics(), fl, 0, 0, foilImage.getWidth(), foilImage.getHeight());
        return foilImage;                
    }
    
    public static SkinIcon getMenuIcon(SkinIcon sourceIcon) {
        return sourceIcon.resize(16, 16);      
    }    
        
    /**
     * Gets the nearest rotation for a requested rotation.
     * <p>
     * For example, if {@code nearestRotation} is set to 90 degrees then
     * will return one of 0, 90, 180 or 270 degrees, whichever is nearest to
     * {@code requestedRotation}.
     * 
     */
    public static int getRotationToNearest(int requestedRotation, int nearestRotation) {
        // Ensure requested rotation falls within -360..0..360 degree range first.
        requestedRotation = requestedRotation - (360 * (requestedRotation / (int)360));
        return (int)(Math.rint((double) requestedRotation / nearestRotation) * nearestRotation);
    }        

    /**
     * Calculates the scale required to best fit contained into container 
     * whilst retaining the aspect ratio.
     */
    public static double getBestFitScale(int containedW, int containedH, int containerW, int containerH) {
        double scaleX = (double)containerW / containedW;
        double scaleY = (double)containerH / containedH;
        return Math.min(scaleX, scaleY);        
    }    

    /**
     * Calculates the scale required to best fit contained into container 
     * whilst retaining the aspect ratio.
     */
    public static double getBestFitScale(Dimension contained, Dimension container) {
        return getBestFitScale(contained.width, contained.height, container.width, container.height);
    }
}