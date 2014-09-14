package forge.achievement;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import forge.game.player.Player;
import forge.properties.ForgeConstants;
import forge.util.FileUtil;
import forge.util.XmlUtil;

public abstract class AchievementCollection implements Iterable<Achievement> {
    private final Map<String, Achievement> achievements = new LinkedHashMap<String, Achievement>();
    private final String name, filename;

    static {
        FileUtil.ensureDirectoryExists(ForgeConstants.ACHIEVEMENTS_DIR);
    }

    protected AchievementCollection(String name0, String filename0) {
        name = name0;
        filename = filename0;
        buildAchievementList();
        load();
    }

    protected abstract void buildAchievementList();

    protected void add(String name, Achievement achievement) {
        achievements.put(name, achievement);
    }

    public void updateAll(Player player) {
        for (Achievement achievement : achievements.values()) {
            achievement.update(player);
        }
        save();
    }

    public void load() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.parse(filename);
            final NodeList nodes = document.getElementsByTagName("a");
            for (int i = 0; i < nodes.getLength(); i++) {
                final Element el = (Element)nodes.item(i);
                final Achievement achievement = achievements.get(el.getAttribute("name"));
                if (achievement != null) {
                    achievement.loadFromXml(el);
                }
            }
        }
        catch (FileNotFoundException e) {
            //ok if file not found
        }
        catch (MalformedURLException e) {
            //ok if file not found
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("achievements");
            document.appendChild(root);

            for (Entry<String, Achievement> entry : achievements.entrySet()) {
                Achievement achievement = entry.getValue();
                if (achievement.needSave()) {
                    Element el = document.createElement("a");
                    el.setAttribute("name", entry.getKey());
                    achievement.saveToXml(el);
                    root.appendChild(el);
                }
            }
            XmlUtil.saveDocument(document, filename);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return achievements.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Achievement> iterator() {
        return achievements.values().iterator();
    }

    @Override
    public String toString() {
        return name;
    }
}
