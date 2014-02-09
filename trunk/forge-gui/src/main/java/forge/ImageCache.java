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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.mortennobel.imagescaling.ResampleOp;
import forge.card.CardDb;
import forge.card.CardRules;
import forge.card.CardSplitType;
import forge.game.card.Card;
import forge.game.player.IHasIcon;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinIcon;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.properties.NewConstants;
import forge.util.Base64Coder;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

    private static final Set<String> _missingIconKeys = new HashSet<String>();
    private static final LoadingCache<String, BufferedImage> _CACHE = CacheBuilder.newBuilder().softValues().build(new ImageLoader());
    private static final BufferedImage _defaultImage;
    static {
        BufferedImage defImage = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream isNoCardJpg = cl.getResourceAsStream("no_card.jpg");
            defImage = ImageIO.read(isNoCardJpg);
        } catch (Exception e) {
            // resource not found; perhaps we're running straight from source
            try {
                defImage = ImageIO.read(new File("src/main/resources/no_card.jpg"));
            } catch (Exception ex) {
                System.err.println("could not load default card image");
            }
        } finally {
            _defaultImage = (null == defImage) ? new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB) : defImage; 
        }
    }
    
    public static void clear() {
        _CACHE.invalidateAll();
        _missingIconKeys.clear();
    }
    
    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(Card card, int width, int height) {
        final String key;
        if (!Singletons.getControl().mayShowCard(card) || card.isFaceDown()) {
            key = ImageKeys.TOKEN_PREFIX + ImageKeys.MORPH_IMAGE;
        } else {
            key = card.getImageKey();
        }
        return scaleImage(key, width, height, true);
    }
    
    /**
     * retrieve an image from the cache.  returns null if the image is not found in the cache
     * and cannot be loaded from disk.  pass -1 for width and/or height to avoid resizing in that dimension.
     */
    public static BufferedImage getImage(InventoryItem ii, int width, int height) {
        return scaleImage(ImageKeys.getImageKey(ii, false), width, height, true);
    }
    
    /**
     * retrieve an icon from the cache.  returns the current skin's ICO_UNKNOWN if the icon image is not found
     * in the cache and cannot be loaded from disk.
     */
    public static SkinIcon getIcon(IHasIcon ihi) {
        String imageKey = ihi.getIconImageKey();
        final BufferedImage i;
        if (_missingIconKeys.contains(imageKey) ||
                null == (i = scaleImage(ihi.getIconImageKey(), -1, -1, false))) {
            _missingIconKeys.add(imageKey);
            return FSkin.getIcon(FSkin.InterfaceIcons.ICO_UNKNOWN);
        }
        return new FSkin.UnskinnedIcon(i);
    }
    
    /**
     * This requests the original unscaled image from the cache for the given key.
     * If the image does not exist then it can return a default image if desired.
     * <p>
     * If the requested image is not present in the cache then it attempts to load
     * the image from file (slower) and then add it to the cache for fast future access. 
     * </p>
     */
    public static BufferedImage getOriginalImage(String imageKey, boolean useDefaultIfNotFound) {
        if (null == imageKey) { 
            return null;
        }
        
        boolean altState = imageKey.endsWith(ImageKeys.BACKFACE_POSTFIX);
        if(altState)
            imageKey = imageKey.substring(0, imageKey.length() - ImageKeys.BACKFACE_POSTFIX.length());
        if (imageKey.startsWith(ImageKeys.CARD_PREFIX)) {
            imageKey = getImageKey(getPaperCardFromImageKey(imageKey.substring(2)), altState, true);
            if (StringUtils.isBlank(imageKey)) { 
                return _defaultImage;
            }
        }
        
        // Load from file and add to cache if not found in cache initially. 
        BufferedImage original = getImage(imageKey);
        
        // No image file exists for the given key so optionally associate with
        // a default "not available" image and add to cache for given key.
        if (original == null) {
            if (useDefaultIfNotFound) { 
                original = _defaultImage;
                _CACHE.put(imageKey, _defaultImage);
            } else {
                original = null;
            }            
        }
                
        return original;
    }
    
    private static PaperCard getPaperCardFromImageKey(String key) {
        if( key == null )
            return null;

        PaperCard cp = StaticData.instance().getCommonCards().getCard(key);
        if ( cp == null )
            cp = StaticData.instance().getVariantCards().getCard(key);
        return cp;
    }

    private static BufferedImage scaleImage(String key, final int width, final int height, boolean useDefaultImage) {
        if (StringUtils.isEmpty(key) || (3 > width && -1 != width) || (3 > height && -1 != height)) {
            // picture too small or key not defined; return a blank
            return null;
        }

        String resizedKey = String.format("%s#%dx%d", key, width, height);

        final BufferedImage cached = _CACHE.getIfPresent(resizedKey);
        if (null != cached) {
            //System.out.println("found cached image: " + resizedKey);
            return cached;
        }
        
        BufferedImage original = getOriginalImage(key, useDefaultImage);
        if (original == null) { return null; }
        
        // Calculate the scale required to best fit the image into the requested
        // (width x height) dimensions whilst retaining aspect ratio.
        double scaleX = (-1 == width ? 1 : (double)width / original.getWidth());
        double scaleY = (-1 == height? 1 : (double)height / original.getHeight());
        double bestFitScale = Math.min(scaleX, scaleY);
        if ((bestFitScale > 1) && !mayEnlarge()) {
            bestFitScale = 1;
        }

        BufferedImage result;
        if (1 == bestFitScale) { 
            result = original;
        } else {
            
            int destWidth  = (int)(original.getWidth()  * bestFitScale);
            int destHeight = (int)(original.getHeight() * bestFitScale);
                         
            ResampleOp resampler = new ResampleOp(destWidth, destHeight);
            result = resampler.filter(original, null);
        }
        
        //System.out.println("caching image: " + resizedKey);
        _CACHE.put(resizedKey, result);
        return result;
    }
    
    private static boolean mayEnlarge() {
        return Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_SCALE_LARGER);        
    }

    /**
     * Returns the Image corresponding to the key.
     */
    private static BufferedImage getImage(final String key) {
        FThreads.assertExecutedByEdt(true);
        try {
            return ImageCache._CACHE.get(key);
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
    



    
    private static String getImageRelativePath(PaperCard cp, boolean backFace, boolean includeSet, boolean isDownloadUrl) {
        final String nameToUse = cp == null ? null : getNameToUse(cp, backFace);
        if ( null == nameToUse )
            return null;
        
        StringBuilder s = new StringBuilder();
        
        CardRules card = cp.getRules();
        String edition = cp.getEdition();
        s.append(ImageCache.toMWSFilename(nameToUse));
        
        final int cntPictures;
        final boolean hasManyPictures;
        final CardDb db =  !card.isVariant() ? Singletons.getMagicDb().getCommonCards() : Singletons.getMagicDb().getVariantCards();
        if (includeSet) {
            cntPictures = db.getPrintCount(card.getName(), edition); 
            hasManyPictures = cntPictures > 1;
        } else {
            // without set number of pictures equals number of urls provided in Svar:Picture
            String urls = card.getPictureUrl(backFace);
            cntPictures = StringUtils.countMatches(urls, "\\") + 1;

            // raise the art index limit to the maximum of the sets this card was printed in
            int maxCntPictures = db.getMaxPrintCount(card.getName());
            hasManyPictures = maxCntPictures > 1;
        }
        
        int artIdx = cp.getArtIndex() - 1;
        if (hasManyPictures) {
            if ( cntPictures <= artIdx ) // prevent overflow
                artIdx = cntPictures == 0 ? 0 : artIdx % cntPictures;
            s.append(artIdx + 1);
        }
        
        // for whatever reason, MWS-named plane cards don't have the ".full" infix
        if (!card.getType().isPlane() && !card.getType().isPhenomenon()) {
            s.append(".full");
        }
        
        final String fname;
        if (isDownloadUrl) {
            s.append(".jpg");
            fname = Base64Coder.encodeString(s.toString(), true);
        } else {
            fname = s.toString();
        }
        
        if (includeSet) {
            String editionAliased = isDownloadUrl ? Singletons.getMagicDb().getEditions().getCode2ByCode(edition) : getSetFolder(edition);
            return String.format("%s/%s", editionAliased, fname);
        } else {
            return fname;
        }
    }
    
    public static boolean hasBackFacePicture(PaperCard cp) {
        CardSplitType cst = cp.getRules().getSplitType();
        return cst == CardSplitType.Transform || cst == CardSplitType.Flip; 
    }
    
    public static String getSetFolder(String edition) {
        return  !NewConstants.CACHE_CARD_PICS_SUBDIR.containsKey(edition)
                ? Singletons.getMagicDb().getEditions().getCode2ByCode(edition) // by default 2-letter codes from MWS are used
                : NewConstants.CACHE_CARD_PICS_SUBDIR.get(edition); // may use custom paths though
    }

    private static String getNameToUse(PaperCard cp, boolean backFace) {
        final CardRules card = cp.getRules();
        if (backFace ) {
            if ( hasBackFacePicture(cp) ) 
                return card.getOtherPart().getName();
            else 
                return null;
        } else if(CardSplitType.Split == cp.getRules().getSplitType()) {
            return card.getMainPart().getName() + card.getOtherPart().getName();
        } else {
            return cp.getName();
        }
    }
    
    public static String getImageKey(PaperCard cp, boolean backFace, boolean includeSet) {
        return getImageRelativePath(cp, backFace, includeSet, false);
    }

    public static String getDownloadUrl(PaperCard cp, boolean backFace) {
        return getImageRelativePath(cp, backFace, true, true);
    }    
    
    public static String toMWSFilename(String in) {
        final StringBuffer out = new StringBuffer();
        char c;
        for (int i = 0; i < in.length(); i++) {
            c = in.charAt(i);
            if ((c == '"') || (c == '/') || (c == ':') || (c == '?')) {
                out.append("");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
