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
package forge.planarconquest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import forge.deck.CardPool;
import forge.properties.ForgeConstants;
import forge.quest.io.QuestDataIO.DeckToXml;
import forge.quest.io.QuestDataIO.ItemPoolToXml;
import forge.util.FileUtil;
import forge.util.IgnoringXStream;
import forge.util.ItemPool;

public class ConquestDataIO {
    static {
        //ensure save directory exists if this class is used
        FileUtil.ensureDirectoryExists(ForgeConstants.CONQUEST_SAVE_DIR);
    }

    protected static XStream getSerializer(final boolean isIgnoring) {
        final XStream xStream = isIgnoring ? new IgnoringXStream() : new XStream();
        xStream.registerConverter(new ItemPoolToXml());
        xStream.registerConverter(new DeckToXml());
        xStream.autodetectAnnotations(true);
        xStream.alias("CardPool", ItemPool.class);
        xStream.alias("DeckSection", CardPool.class);
        return xStream;
    }

    public static ConquestData loadData(final File xmlSaveFile) {
        try {
            ConquestData data = null;

            final GZIPInputStream zin = new GZIPInputStream(new FileInputStream(xmlSaveFile));
            final StringBuilder xml = new StringBuilder();
            final char[] buf = new char[1024];
            final InputStreamReader reader = new InputStreamReader(zin);
            while (reader.ready()) {
                final int len = reader.read(buf);
                if (len == -1) {
                    break;
                } // when end of stream was reached
                xml.append(buf, 0, len);
            }

            zin.close();
            
            String bigXML = xml.toString();
            data = (ConquestData) ConquestDataIO.getSerializer(true).fromXML(bigXML);

            if (data.getVersionNumber() != ConquestData.CURRENT_VERSION_NUMBER) {
                try {
                    ConquestDataIO.updateSaveFile(data, bigXML, xmlSaveFile.getName().replace(".dat", ""));
                }
                catch (final Exception e) {
                    //BugReporter.reportException(e);
                    throw new RuntimeException(e);
                }
            }

            return data;
        }
        catch (final Exception ex) {
            //BugReporter.reportException(ex, "Error loading Conquest Data");
            throw new RuntimeException(ex);
        }
    }

    private static void updateSaveFile(final ConquestData newData, final String input, String filename) throws ParserConfigurationException, SAXException, IOException, IllegalAccessException, NoSuchFieldException {
        //final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(input));
        //final Document document = builder.parse(is);

        final int saveVersion = newData.getVersionNumber();
        switch (saveVersion) {
        // There should be a fall-through between the cases so that each
        // version's changes get applied progressively
        case 0:
            
        default:
            break;
        }

        // mark the QD as the latest version
        newData.setVersionNumber(ConquestData.CURRENT_VERSION_NUMBER);
    }

    public static synchronized void saveData(final ConquestData qd) {
        try {
            final XStream xStream = ConquestDataIO.getSerializer(false);

            final File f = new File(ForgeConstants.CONQUEST_SAVE_DIR, qd.getName());
            ConquestDataIO.savePacked(f + ".dat", xStream, qd);
            // ConquestDataIO.saveUnpacked(f + ".xml", xStream, qd);
        }
        catch (final Exception ex) {
            //BugReporter.reportException(ex, "Error saving Conquest Data.");
            throw new RuntimeException(ex);
        }
    }

    private static void savePacked(final String f, final XStream xStream, final ConquestData qd) throws IOException {
        final BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(f));
        final GZIPOutputStream zout = new GZIPOutputStream(bout);
        xStream.toXML(qd, zout);
        zout.flush();
        zout.close();
    }

    @SuppressWarnings("unused") // used only for debug purposes
    private static void saveUnpacked(final String f, final XStream xStream, final ConquestData qd) throws IOException {
        final BufferedOutputStream boutUnp = new BufferedOutputStream(new FileOutputStream(f));
        xStream.toXML(qd, boutUnp);
        boutUnp.flush();
        boutUnp.close();
    }
}
