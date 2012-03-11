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
package forge.quest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import forge.AllZone;
import forge.quest.data.QuestAssets;
import forge.quest.data.QuestStallDefinition;
import forge.quest.data.item.IQuestStallPurchasable;
import forge.quest.data.item.QuestItemPassive;
import forge.quest.data.item.QuestItemType;
import forge.quest.data.pet.QuestPetAbstract;
import forge.util.IgnoringXStream;
import forge.util.XmlUtil;

/**
 * <p>
 * QuestStallManager class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestStallManager {
    private final File xmlFile;

    public QuestStallManager(File xmlFile0) {
        xmlFile = xmlFile0;
    }

    public void load() {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.parse(xmlFile);

            XStream xs = new IgnoringXStream();
            xs.autodetectAnnotations(true);

            NodeList xmlStalls = document.getElementsByTagName("stalls").item(0).getChildNodes();
            for (int iN = 0; iN < xmlStalls.getLength(); iN++) {
                Node n = xmlStalls.item(iN);
                if (n.getNodeType() != Node.ELEMENT_NODE) { continue; }

                Attr att = document.createAttribute("resolves-to");
                att.setValue(QuestStallDefinition.class.getCanonicalName());
                n.getAttributes().setNamedItem(att);
                QuestStallDefinition stall = (QuestStallDefinition) xs.fromXML(XmlUtil.nodeToString(n));
                stalls.put(stall.getName(), stall);
            }

            NodeList xmlQuestItems = document.getElementsByTagName("questItems").item(0).getChildNodes();
            for (int iN = 0; iN < xmlQuestItems.getLength(); iN++) {
                Node n = xmlQuestItems.item(iN);
                if (n.getNodeType() != Node.ELEMENT_NODE) { continue; }
                
                NamedNodeMap attrs = n.getAttributes();
                String sType = attrs.getNamedItem("itemType").getTextContent();
                String name = attrs.getNamedItem("name").getTextContent();
                QuestItemType qType = QuestItemType.smartValueOf(sType);
                Attr att = document.createAttribute("resolves-to");
                att.setValue(qType.getBazaarControllerClass().getCanonicalName());
                attrs.setNamedItem(att);
                QuestItemPassive ctrl = (QuestItemPassive) xs.fromXML(XmlUtil.nodeToString(n));
                items.put(name, ctrl);
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /** Constant <code>stalls</code>. */
    private final Map<String, QuestStallDefinition> stalls = new TreeMap<String, QuestStallDefinition>();
    /** Constant <code>items</code>. */
    private final Map<String, SortedSet<IQuestStallPurchasable>> itemsOnStalls = new TreeMap<String, SortedSet<IQuestStallPurchasable>>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, IQuestStallPurchasable> items = new TreeMap<String, IQuestStallPurchasable>();

    /**
     * <p>
     * getStall.
     * </p>
     * 
     * @param stallName
     *            a {@link java.lang.String} object.
     * @return a {@link forge.quest.data.QuestStallDefinition} object.
     */
    public QuestStallDefinition getStall(final String stallName) {
        if (stalls.isEmpty()) {
            load();
        }

        return stalls.get(stallName);
    }

    /**
     * Retrieves all creatures and items, iterates through them,
     * and maps to appropriate merchant.
     */
    public void buildItems() {
        final Map<String, IQuestStallPurchasable> itemSet = new HashMap<String, IQuestStallPurchasable>();

        final QuestAssets qA = AllZone.getQuest().getAssets();
        for (QuestPetAbstract i : qA.getPetManager().getPetsAndPlants()) { itemSet.put(i.getName(), i); }
        itemSet.putAll(items);

        itemsOnStalls.clear();

        for (QuestStallDefinition thisStall : stalls.values()) {
            TreeSet<IQuestStallPurchasable> set = new TreeSet<IQuestStallPurchasable>();

            for (String itemName : thisStall.getItems()) {
                IQuestStallPurchasable item = itemSet.get(itemName);
                set.add(item);
            }
            itemsOnStalls.put(thisStall.getName(), set);
        }
    }

    /**
     * Returns <i>purchasable</i> items available for a particular stall.
     * 
     * @param stallName &emsp; {@link java.lang.String}
     * @return {@link java.util.List}.
     */
    public List<IQuestStallPurchasable> getItems(final String stallName) {
        buildItems();

        final List<IQuestStallPurchasable> ret = new ArrayList<IQuestStallPurchasable>();

        QuestAssets qA = AllZone.getQuest().getAssets();
        for (final IQuestStallPurchasable purchasable : itemsOnStalls.get(stallName)) {
            if (purchasable.isAvailableForPurchase(qA)) {
                ret.add(purchasable);
            }
        }
        return ret;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public Set<String> getStallNames() {
        if (stalls.isEmpty()) {
            load();
        }
        return stalls.keySet();
    }

}
