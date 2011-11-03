package forge;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.apache.commons.lang3.StringUtils;

import forge.card.CardSet;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

/**
 * <p>
 * Gui_DownloadSetPictures_LQ class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class GuiDownloadSetPicturesLQ extends GuiDownloader {

    private static final long serialVersionUID = -7890794857949935256L;

    /**
     * <p>
     * Constructor for Gui_DownloadSetPictures_LQ.
     * </p>
     * 
     * @param frame
     *            a {@link javax.swing.JFrame} object.
     */
    public GuiDownloadSetPicturesLQ(final JFrame frame) {
        super(frame);
    }

    /**
     * <p>
     * getNeededCards.
     * </p>
     * 
     * @return an array of {@link forge.GuiDownloader.DownloadObject} objects.
     */
    protected final DownloadObject[] getNeededImages() {
        // read token names and urls
        DownloadObject[] cardTokenLQ = readFileWithNames(NewConstants.TOKEN_IMAGES, ForgeProps.getFile(NewConstants.IMAGE_TOKEN));
        ArrayList<DownloadObject> cList = new ArrayList<DownloadObject>();

        File base = ForgeProps.getFile(NewConstants.IMAGE_BASE);
        String urlBase = "http://cardforge.org/fpics/";
        for (CardPrinted c : CardDb.instance().getAllCards()) {
            String setCode3 = c.getSet();
            if (StringUtils.isBlank(setCode3) || "???".equals(setCode3)) {
                continue; // we don't want cards from unknown sets
            }

            CardSet thisSet = SetUtils.getSetByCode(setCode3);
            String setCode2 = thisSet.getCode2();

            String imgFN = CardUtil.buildFilename(c);
            boolean foundSetImage = imgFN.contains(setCode3) || imgFN.contains(setCode2);

            if (!foundSetImage) {
                int artsCnt = c.getCard().getSetInfo(setCode3).getCopiesCount();
                String fn = CardUtil.buildIdealFilename(c.getName(), c.getArtIndex(), artsCnt);
                cList.add(new DownloadObject(fn, urlBase + setCode2 + "/" + Base64Coder.encodeString(fn, true), base
                        .getPath() + File.separator + setCode3));
            }
        }

        // add missing tokens to the list of things to download
        File file;
        File filebase = ForgeProps.getFile(NewConstants.IMAGE_TOKEN);
        for (int i = 0; i < cardTokenLQ.length; i++) {
            file = new File(filebase, cardTokenLQ[i].getName());
            if (!file.exists()) {
                cList.add(cardTokenLQ[i]);
            }
        }

        // return all card names and urls that are needed
        DownloadObject[] out = new DownloadObject[cList.size()];
        cList.toArray(out);

        return out;
    } // getNeededImages()

} // end class Gui_DownloadSetPictures_LQ
