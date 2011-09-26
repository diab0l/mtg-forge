package forge.deck;


import forge.FileUtil;
import forge.PlayerType;
import forge.error.ErrorViewer;
import forge.game.GameType;
import forge.gui.deckeditor.TableSorter;
import forge.item.CardPrinted;
import forge.item.ItemPoolView;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.StringUtils;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;


//reads and writeDeck Deck objects
/**
 * <p>DeckManager class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class DeckManager {
    /** Constant <code>BDKFileFilter</code> */
    private static FilenameFilter BDKFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".bdk");
        }
    };

    /** Constant <code>DCKFileFilter</code> */
    public static final FilenameFilter DCKFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".dck");
        }
    };
    
    public static final FileFilter dckFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".dck") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Simple Deck File .dck";
        }
    };

    private static final String NAME = "Name";
    private static final String DECK_TYPE = "Deck Type";
    private static final String COMMENT = "Comment";
    private static final String PLAYER = "Player";
    private static final String CSTM_POOL = "Custom Pool";

    private File deckDir;
    Map<String, Deck> deckMap;
    Map<String, Deck[]> draftMap;

    /**
     * <p>Constructor for DeckManager.</p>
     *
     * @param deckDir a {@link java.io.File} object.
     */
    public DeckManager(File deckDir) {
        if (deckDir == null) {
            throw new IllegalArgumentException("No deck directory specified");
        }
        try {
            this.deckDir = deckDir;

            if (deckDir.isFile()) {
                throw new IOException("Not a directory");
            } else {
                deckDir.mkdirs();
                if (!deckDir.isDirectory()) {
                    throw new IOException("Directory can't be created");
                }
                this.deckMap = new HashMap<String, Deck>();
                this.draftMap = new HashMap<String, Deck[]>();
                readAllDecks();
            }
        } catch (IOException ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("DeckManager : writeDeck() error, " + ex.getMessage());
        }
    }


    /**
     * <p>isUnique.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isUnique(String deckName) {
        return !deckMap.containsKey(deckName);
    }

    /**
     * <p>isUniqueDraft.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isUniqueDraft(String deckName) {
        return !draftMap.keySet().contains(deckName);
    }

    /**
     * <p>getDeck.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    public Deck getDeck(String deckName) {
        return deckMap.get(deckName);
    }


    /**
     * <p>addDeck.</p>
     *
     * @param deck a {@link forge.deck.Deck} object.
     */
    public void addDeck(Deck deck) {
        if (deck.getDeckType().equals(GameType.Draft)) {
            throw new RuntimeException(
                    "DeckManager : addDeck() error, deck type is Draft");
        }

        deckMap.put(deck.getName(), deck);
    }

    /**
     * <p>deleteDeck.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     */
    public void deleteDeck(String deckName) {
        deckMap.remove(deckName);
    }

    /**
     * <p>getDraftDeck.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     * @return an array of {@link forge.deck.Deck} objects.
     */
    public Deck[] getDraftDeck(String deckName) {
        if (!draftMap.containsKey(deckName)) {
            throw new RuntimeException(
                    "DeckManager : getDraftDeck() error, deck name not found - " + deckName);
        }

        return draftMap.get(deckName);
    }

    /**
     * <p>addDraftDeck.</p>
     *
     * @param deck an array of {@link forge.deck.Deck} objects.
     */
    public void addDraftDeck(Deck[] deck) {
        checkDraftDeck(deck);

        draftMap.put(deck[0].toString(), deck);
    }

    /**
     * <p>deleteDraftDeck.</p>
     *
     * @param deckName a {@link java.lang.String} object.
     */
    public void deleteDraftDeck(String deckName) {
        if (!draftMap.containsKey(deckName)) {
            throw new RuntimeException(
                    "DeckManager : deleteDraftDeck() error, deck name not found - " + deckName);
        }

        draftMap.remove(deckName);
        // delete from disk as well
        File f = makeFileName(deckName, GameType.Draft);
        f.delete();
    }

    /**
     * <p>checkDraftDeck.</p>
     *
     * @param deck an array of {@link forge.deck.Deck} objects.
     */
    private void checkDraftDeck(Deck[] deck) {
        if (deck == null || deck.length != 8 || deck[0].getName().equals("")
                || (!deck[0].getDeckType().equals(GameType.Draft))) {
            throw new RuntimeException("DeckManager : checkDraftDeck() error, invalid deck");
        }
    }


    public Collection<Deck> getDecks() {
        return deckMap.values();
    }


    public Map<String, Deck[]> getDraftDecks() {
        return new HashMap<String, Deck[]>(draftMap);
    }
    

    public ArrayList<String> getDeckNames(final GameType deckType) {
        ArrayList<String> list = new ArrayList<String>();

        //only get decks according to the OldGuiNewGame screen option
        if (deckType.equals(GameType.Draft)) {
            for (String s : getDraftDecks().keySet()) {
                list.add(s);
            }
        } else {
            for (Deck deck : getDecks()) {
                if (deckType.equals(deck.getDeckType())) {
                    list.add(deck.toString());
                }
            }
        }

        Collections.sort(list);
        return list;
    }    

    /**
     * <p>readAllDecks.</p>
     */
    public void readAllDecks() {
        deckMap.clear();
        draftMap.clear();

        File[] files;

        List<String> decksThatFailedToLoad = new ArrayList<String>();
        files = deckDir.listFiles(DCKFileFilter);
        for (File file : files) {
            try {
                Deck newDeck = readDeck(file);
                deckMap.put(newDeck.getName(), newDeck);
            } catch (NoSuchElementException ex) {
                String message = String.format("%s failed to load because ---- %s", file.getName(), ex.getMessage());
                decksThatFailedToLoad.add(message);
            }
        }

        if (!decksThatFailedToLoad.isEmpty()) {
            JOptionPane.showMessageDialog(null, StringUtils.join(decksThatFailedToLoad, System.getProperty("line.separator")),
                "Some of your decks were not loaded.", JOptionPane.WARNING_MESSAGE);
        }

        files = deckDir.listFiles(BDKFileFilter);
        for (File file : files) {
            Deck[] d = new Deck[8];

            boolean gotError = false;
            for (int i = 0; i < d.length; i++) {
                d[i] = readDeck(new File(file, i + ".dck"));
                if(d[i] == null) {
                    gotError = true;
                    break;
                }
            }

            if (!gotError)
            {
                draftMap.put(d[0].getName(), d);
            }
        }
    }

    /**
     * <p>readDeck.</p>
     *
     * @param deckFile a {@link java.io.File} object.
     * @return a {@link forge.deck.Deck} object.
     */
    public static Deck readDeck(File deckFile) {
        
        List<String> lines = FileUtil.readFile(deckFile);
        if (lines.isEmpty()) { return null; }


        Deck d = new Deck();

        String firstLine = lines.get(0); 
        if (!firstLine.startsWith("[") || firstLine.equalsIgnoreCase("[general]")) {
            readDeckOldMetadata(lines.iterator(), d);
        } else {
            readDeckMetadata(findSection(lines, "metadata"), d);
        }
        d.setMain(readCardList(findSection(lines, "main")));
        d.setSideboard(readCardList(findSection(lines, "sideboard")));

        return d;
    }
    
    private static Iterator<String> findSection(final Iterable<String> lines, final String sectionName)
    {
        Iterator<String> lineIterator = lines.iterator();
        String toSearch = String.format("[%s]", sectionName);
        while(lineIterator.hasNext()) {
            if (toSearch.equalsIgnoreCase(lineIterator.next())) { break; }
        }
        
        return lineIterator;
    }
    
    private static void readDeckMetadata(final Iterator<String> lineIterator, final Deck d)
    {
        while (lineIterator.hasNext()) {
            String line = lineIterator.next();
            if( line.startsWith("[") ) { break; }
            
            String[] linedata = line.split("=", 2);
            String field = linedata[0].toLowerCase();
            String value = "";
            
            if (linedata.length > 1)
                value = linedata[1];
            
            if (NAME.equalsIgnoreCase(field)) {
                d.setName(value);
                
            } else if (COMMENT.equalsIgnoreCase(field)) {
                d.setComment(value);
                
            } else if (DECK_TYPE.equalsIgnoreCase(field)) {
                d.setDeckType(GameType.smartValueOf(value));
                
            } else if (CSTM_POOL.equalsIgnoreCase(field)) {
                d.setCustomPool(value.equalsIgnoreCase("true"));
                
            } else if (PLAYER.equalsIgnoreCase(field)) {
                if ("human".equalsIgnoreCase(value)) {
                    d.setPlayerType(PlayerType.HUMAN);
                    
                } else {
                    d.setPlayerType(PlayerType.COMPUTER);
                }
            }
        }
    }

    /**
     * <p>readDeckOld.</p>
     *
     * @param iterator a {@link java.util.ListIterator} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private static void readDeckOldMetadata(final Iterator<String> iterator, Deck d) {

        String line;
        //readDeck name
        String name = iterator.next();

        //readDeck comments
        String comment = null;
        while (iterator.hasNext() && (line = iterator.next()) != null && !line.equals("[general]")) {
            if (comment == null) {
                comment = line;
            } else {
                comment += "\n" + line;
            }
        }

        //readDeck deck type
        
        GameType deckType = iterator.hasNext() ? GameType.smartValueOf(iterator.next()) : GameType.Constructed; 

        d.setName(name);
        d.setComment(comment);
        d.setDeckType(deckType);
    }

    // Precondition: iterator should point at the first line of cards list
    private static List<String> readCardList(final Iterator<String> lineIterator) {
        List<String> result = new ArrayList<String>();
        Pattern p = Pattern.compile("((\\d+)\\s+)?(.*?)");

        while (lineIterator.hasNext()) {
            String line = lineIterator.next();
            if (line.startsWith("[")) { break; } // there comes another section

            Matcher m = p.matcher(line.trim());
            m.matches();
            String sCnt = m.group(2);
            String cardName = m.group(3);
            if (StringUtils.isBlank(cardName)) { continue; }

            int count = sCnt == null ? 1 : parseInt(sCnt);
            for (int i = 0; i < count; i++) {
                result.add(cardName);
            }
        }
        return result;
    }

    private static String deriveFileName(String deckName) {
        //skips all but the listed characters
        return deckName.replaceAll("[^-_$#@.{[()]} a-zA-Z0-9]", "");
    }

    //only accepts numbers, letters or dashes up to 20 characters in length
    public static String cleanDeckName(String in)
    {
        char[] c = in.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length && i < 20; i++) {
            if (Character.isLetterOrDigit(c[i]) || c[i] == '-') {
                sb.append(c[i]);
            }
        }
        return sb.toString();
    }

    public static File makeFileName(String deckName, GameType deckType)
    {
        File path = ForgeProps.getFile(NewConstants.NEW_DECKS);
        if (deckType == GameType.Draft)
            return new File(path, deriveFileName(deckName) + ".bdk");
        else
            return new File(path, deriveFileName(deckName) + ".dck");
    }
    
    public static File makeFileName(Deck deck)
    {
        return makeFileName(deck.getName(), deck.getDeckType());
    }

    /**
     * <p>writeAllDecks.</p>
     */
    public static void writeDraftDecks(Deck[] drafts) {
        try {
            File f = makeFileName(drafts[0]);
            f.mkdir();
            for (int i = 0; i < drafts.length; i++) {
                BufferedWriter out = new BufferedWriter(new FileWriter(new File(f, i + ".dck")));
                writeDeck(drafts[i], out);
                out.close();
            }


        } catch (IOException ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("DeckManager : writeDeck() error, " + ex.getMessage());
        }
    }

    /**
     * <p>writeDeck.</p>
     *
     * @param d a {@link forge.deck.Deck} object.
     * @param out a {@link java.io.BufferedWriter} object.
     * @throws java.io.IOException if any.
     */
    private static void writeDeck(final Deck d, final BufferedWriter out) throws IOException {
        out.write(format("[metadata]%n"));

        out.write(format("%s=%s%n", NAME, d.getName().replaceAll("\n", "")));
        out.write(format("%s=%s%n", DECK_TYPE, d.getDeckType()));
        // these are optional
        if (d.getComment() != null) { out.write(format("%s=%s%n", COMMENT, d.getComment().replaceAll("\n", ""))); }
        if (d.getPlayerType() != null) { out.write(format("%s=%s%n", PLAYER, d.getPlayerType())); }
        
        if (d.isCustomPool()) { out.write(format("%s=%s%n", CSTM_POOL, "true")); }

        out.write(format("%s%n", "[main]"));
        writeCardPool(d.getMain(), out);

        out.write(format("%s%n", "[sideboard]"));
        writeCardPool(d.getSideboard(), out);
    }

    private static void writeCardPool(final ItemPoolView<CardPrinted> pool, final BufferedWriter out) throws IOException
    {
        List<Entry<CardPrinted, Integer>> main2sort = pool.getOrderedList();
        Collections.sort(main2sort, TableSorter.byNameThenSet);
        for (Entry<CardPrinted, Integer> e : main2sort) {
            CardPrinted card = e.getKey();
            boolean hasBadSetInfo = "???".equals(card.getSet()) || StringUtils.isBlank(card.getSet());
            if (hasBadSetInfo) {
                out.write(format("%d %s%n", e.getValue(), card.getName()));
            } else {
                out.write(format("%d %s|%s%n", e.getValue(), card.getName(), card.getSet()));
            }
        }
    }


    /**
     * <p>writeDeck.</p>
     *
     * @param d a {@link forge.deck.Deck} object.
     * @param f a {@link java.io.File} object.
     */
    public static void writeDeck(Deck d, File f) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writeDeck(d, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
