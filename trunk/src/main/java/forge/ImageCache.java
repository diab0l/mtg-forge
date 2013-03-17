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
package forge;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.mortennobel.imagescaling.ResampleOp;

import forge.game.player.IHasIcon;
import forge.gui.toolbox.FSkin;
import forge.item.InventoryItem;
import forge.properties.ForgePreferences.FPref;
import forge.properties.NewConstants;

/**
 * This class stores ALL card images in a cache with soft values. this means
 * that the images may be collected when they are not needed any more, but will
 * be kept as long as possible.
 * <p/>
 * The keys are the following:
 * <ul>
 * <li>Keys start with the file name, extension is skipped</li>
 * <li>The key without suffix belongs to the unmodified image from the file</li>
 * </ul>
 * 
 * @author Forge
 * @version $Id$
 */
public class ImageCache {
    // short prefixes to save memory
    public static final String TOKEN_PREFIX          = "t:";
    public static final String ICON_PREFIX           = "i:";
    public static final String BOOSTER_PREFIX        = "b:";
    public static final String FATPACK_PREFIX        = "f:";
    public static final String PRECON_PREFIX         = "p:";
    public static final String TOURNAMENTPACK_PREFIX = "o:";
    
    static private final LoadingCache<String, BufferedImage> CACHE = CacheBuilder.newBuilder().softValues().build(new ImageLoader());

    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(Card card, int width, int height) {
        final String key;
        if (!card.canBeShownTo(Singletons.getControl().getPlayer()) || card.isFaceDown()) {
            key = TOKEN_PREFIX + NewConstants.CACHE_MORPH_IMAGE_FILE;
        } else {
            key = card.getImageKey();
        }
        return scaleImage(key, width, height);
    }

    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(InventoryItem ii, int width, int height) {
        return scaleImage(ii.getImageKey(), width, height);
    }
    
    /**
     * retrieve an icon from the cache.  returns the current skin's ICO_UNKNOWN if the icon image is not found
     * in the cache and cannot be loaded from disk.
     */
    public static ImageIcon getIcon(IHasIcon ihi) {
        BufferedImage i = scaleImage(ihi.getIconImageKey(), -1, -1);
        if (null == i) {
            return FSkin.getIcon(FSkin.InterfaceIcons.ICO_UNKNOWN);
        }
        return new ImageIcon(i);
    }

    private static BufferedImage scaleImage(String key, final int width, final int height) {
        if (StringUtils.isEmpty(key) || (3 > width && -1 != width) || (3 > height && -1 != height)) {
            // picture too small or key not defined; return a blank
            return null;
        }

        StringBuilder rsKey = new StringBuilder(key);
        rsKey.append("#").append(width).append('x').append(height);
        String resizedKey = rsKey.toString();

        final BufferedImage cached = CACHE.getIfPresent(resizedKey);
        if (null != cached) {
            return cached;
        }
        
        boolean mayEnlarge = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_SCALE_LARGER);
        BufferedImage original = getImage(key);
        if (null == original) {
            return null;
        }
        
        double scale = Math.min(
                -1 == width ? 1 : (double)width / original.getWidth(),
                -1 == height? 1 : (double)height / original.getHeight());
        if ((scale > 1) && !mayEnlarge) {
            scale = 1;
        }

        BufferedImage result;
        if (1 == scale) { 
            result = original;
        } else {
            int destWidth  = (int)(original.getWidth()  * scale);
            int destHeight = (int)(original.getHeight() * scale);
            ResampleOp resampler = new ResampleOp(destWidth, destHeight);
            
            result = resampler.filter(original, null);
            CACHE.put(resizedKey, result);
        }
        
        return result;
    }

    /**
     * Returns the Image corresponding to the key.
     */
    private static BufferedImage getImage(final String key) {
        try {
            return ImageCache.CACHE.get(key);
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof NullPointerException) {
                return null;
            }
            ex.printStackTrace();
            return null;
        } catch (final InvalidCacheLoadException ex) {
            // should be when a card legitimately has no image
            return null;
        }
    }
}
