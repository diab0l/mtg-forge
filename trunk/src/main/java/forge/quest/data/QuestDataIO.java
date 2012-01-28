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
package forge.quest.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import forge.error.ErrorViewer;
import forge.game.GameType;
import forge.item.BoosterPack;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.PreconDeck;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.quest.data.item.QuestInventory;

/**
 * <p>
 * QuestDataIO class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestDataIO {
    /**
     * <p>
     * Constructor for QuestDataIO.
     * </p>
     */
    public QuestDataIO() {
    }

    /**
     * <p>
     * loadData.
     * </p>
     * 
     * @return a {@link forge.quest.data.QuestData} object.
     */
    public static QuestData loadData() {
        try {
            // read file "questData"
            QuestData data = null;

            final File xmlSaveFile = ForgeProps.getFile(NewConstants.Quest.XMLDATA);
            if (!xmlSaveFile.exists()) {
                return new QuestData();
            }

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

            final IgnoringXStream xStream = new IgnoringXStream();
            xStream.registerConverter(new CardPoolToXml());
            xStream.registerConverter(new GameTypeToXml());
            xStream.alias("CardPool", ItemPool.class);
            data = (QuestData) xStream.fromXML(xml.toString());

            if (data.getVersionNumber() != QuestData.CURRENT_VERSION_NUMBER) {
                QuestDataIO.updateSaveFile(data, xml.toString());
            }

            zin.close();

            return data;
        } catch (final Exception ex) {
            ErrorViewer.showError(ex, "Error loading Quest Data");
            throw new RuntimeException(ex);
        }
    }

    /**
     * <p>
     * updateSaveFile.
     * </p>
     * 
     * @param newData
     *            a {@link forge.quest.data.QuestData} object.
     * @param input
     *            a {@link java.lang.String} object.
     */
    private static void updateSaveFile(final QuestData newData, final String input) {
        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(input));
            final Document document = builder.parse(is);

            switch (newData.getVersionNumber()) {
            // There should be a fall-through b/w the cases so that each
            // version's changes get applied progressively
            case 0:
                // First beta release with new file format,
                // inventory needs to be migrated
                newData.setInventory(new QuestInventory());
                NodeList elements = document.getElementsByTagName("estatesLevel");
                newData.getInventory().setItemLevel("Estates", Integer.parseInt(elements.item(0).getTextContent()));
                elements = document.getElementsByTagName("luckyCoinLevel");
                newData.getInventory().setItemLevel("Lucky Coin", Integer.parseInt(elements.item(0).getTextContent()));
                elements = document.getElementsByTagName("sleightOfHandLevel");
                newData.getInventory().setItemLevel("Sleight", Integer.parseInt(elements.item(0).getTextContent()));
                elements = document.getElementsByTagName("gearLevel");

                final int gearLevel = Integer.parseInt(elements.item(0).getTextContent());
                if (gearLevel >= 1) {
                    newData.getInventory().setItemLevel("Map", 1);
                }
                if (gearLevel == 2) {
                    newData.getInventory().setItemLevel("Zeppelin", 1);
                }
                // fall-through
            case 1:
                // nothing to do here, everything is managed by CardPoolToXml
                // deserializer
                break;
            default:
                break;
            }

            // mark the QD as the latest version
            newData.setVersionNumber(QuestData.CURRENT_VERSION_NUMBER);

        } catch (final Exception e) {
            forge.error.ErrorViewer.showError(e);
        }
    }

    /**
     * <p>
     * saveData.
     * </p>
     * 
     * @param qd
     *            a {@link forge.quest.data.QuestData} object.
     */
    public static void saveData(final QuestData qd) {
        try {
            final XStream xStream = new XStream();
            xStream.registerConverter(new CardPoolToXml());
            xStream.alias("CardPool", ItemPool.class);

            final File f = ForgeProps.getFile(NewConstants.Quest.XMLDATA);
            final BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(f));
            final GZIPOutputStream zout = new GZIPOutputStream(bout);
            xStream.toXML(qd, zout);
            zout.flush();
            zout.close();

            // BufferedOutputStream boutUnp = new BufferedOutputStream(new
            // FileOutputStream(f + ".xml"));
            // xStream.toXML(qd, boutUnp);
            // boutUnp.flush();
            // boutUnp.close();

        } catch (final Exception ex) {
            ErrorViewer.showError(ex, "Error saving Quest Data.");
            throw new RuntimeException(ex);
        }
    }

    /**
     * Xstream subclass that ignores fields that are present in the save but not
     * in the class. This one is intended to skip fields defined in Object class
     * (but are there any fields?)
     */
    private static class IgnoringXStream extends XStream {
        private final List<String> ignoredFields = new ArrayList<String>();

        @Override
        protected MapperWrapper wrapMapper(final MapperWrapper next) {
            return new MapperWrapper(next) {
                @Override
                public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") final Class definedIn,
                        final String fieldName) {
                    if (definedIn == Object.class) {
                        IgnoringXStream.this.ignoredFields.add(fieldName);
                        return false;
                    }
                    return super.shouldSerializeMember(definedIn, fieldName);
                }
            };
        }
    }

    private static class GameTypeToXml implements Converter {
        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(final Class clasz) {
            return clasz.equals(GameType.class);
        }

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
            // not used
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            final String value = reader.getValue();
            return GameType.smartValueOf(value);
        }

    }

    private static class CardPoolToXml implements Converter {
        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(final Class clasz) {
            return clasz.equals(ItemPool.class);
        }

        private void write(final CardPrinted cref, final Integer count, final HierarchicalStreamWriter writer) {
            writer.startNode("card");
            writer.addAttribute("c", cref.getName());
            writer.addAttribute("s", cref.getSet());
            if (cref.isFoil()) {
                writer.addAttribute("foil", "1");
            }
            if (cref.getArtIndex() > 0) {
                writer.addAttribute("i", Integer.toString(cref.getArtIndex()));
            }
            writer.addAttribute("n", count.toString());
            writer.endNode();
        }

        private void write(final BoosterPack booster, final Integer count, final HierarchicalStreamWriter writer) {
            writer.startNode("booster");
            writer.addAttribute("s", booster.getSet());
            writer.addAttribute("n", count.toString());
            writer.endNode();
        }

        private void write(final PreconDeck deck, final Integer count, final HierarchicalStreamWriter writer) {
            writer.startNode("precon");
            writer.addAttribute("s", deck.getName());
            writer.addAttribute("n", count.toString());
            writer.endNode();
        }        
        
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
            @SuppressWarnings("unchecked")
            final ItemPool<InventoryItem> pool = (ItemPool<InventoryItem>) source;
            for (final Entry<InventoryItem, Integer> e : pool) {
                final InventoryItem item = e.getKey();
                final Integer count = e.getValue();
                if (item instanceof CardPrinted) {
                    this.write((CardPrinted) item, count, writer);
                } else if (item instanceof BoosterPack) {
                    this.write((BoosterPack) item, count, writer);
                } else if (item instanceof PreconDeck) {
                    this.write((PreconDeck) item, count, writer);
                }
            }

        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            final ItemPool<InventoryItem> result = new ItemPool<InventoryItem>(InventoryItem.class);
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                final String sCnt = reader.getAttribute("n");
                final int cnt = StringUtils.isNumeric(sCnt) ? Integer.parseInt(sCnt) : 1;
                final String nodename = reader.getNodeName();

                if ("string".equals(nodename)) {
                    result.add(CardDb.instance().getCard(reader.getValue()));
                } else if ("card".equals(nodename)) { // new format
                    result.add(this.readCardPrinted(reader), cnt);
                } else if ("booster".equals(nodename)) {
                    result.add(this.readBooster(reader), cnt);
                } else if ("precon".equals(nodename)) {
                    PreconDeck toAdd = this.readPreconDeck(reader); 
                    if ( null != toAdd )
                        result.add(toAdd, cnt);
                }
                reader.moveUp();
            }
            return result;
        }

        private PreconDeck readPreconDeck(final HierarchicalStreamReader reader) {
            final String name = reader.getAttribute("s");
            for( PreconDeck d : QuestData.getPreconManager().getDecks() )
                if ( name.equalsIgnoreCase( d.getName() ) )
                    return d;

            return null;
        }        
        
        private BoosterPack readBooster(final HierarchicalStreamReader reader) {
            final String set = reader.getAttribute("s");
            return new BoosterPack(set);
        }

        private CardPrinted readCardPrinted(final HierarchicalStreamReader reader) {
            final String name = reader.getAttribute("c");
            final String set = reader.getAttribute("s");
            final String sIndex = reader.getAttribute("i");
            final short index = StringUtils.isNumeric(sIndex) ? Short.parseShort(sIndex) : 0;
            final boolean foil = "1".equals(reader.getAttribute("foil"));
            final CardPrinted card = CardDb.instance().getCard(name, set, index);
            return foil ? CardPrinted.makeFoiled(card) : card;
        }
    }
}
