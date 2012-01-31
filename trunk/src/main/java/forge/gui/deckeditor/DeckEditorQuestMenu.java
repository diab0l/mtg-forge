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
package forge.gui.deckeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.StringUtils;

import forge.Command;
import forge.Constant;
import forge.card.CardRules;
import forge.deck.Deck;
import forge.deck.DeckIO;
import forge.error.ErrorViewer;
import forge.game.GameType;
import forge.gui.GuiUtils;
import forge.gui.ListChooser;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.quest.data.QuestData;

//presumes AllZone.getQuestData() is not null
/**
 * <p>
 * Gui_Quest_DeckEditor_Menu class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class DeckEditorQuestMenu extends JMenuBar {
    /** Constant <code>serialVersionUID=-4052319220021158574L</code>. */
    private static final long serialVersionUID = -4052319220021158574L;

    /** Constant <code>deckEditorName="Deck Editor"</code>. */
    private static final String DECK_EDITOR_NAME = "Deck Editor";

    // used for import and export, try to made the gui user friendly
    /** Constant <code>previousDirectory</code>. */
    private static File previousDirectory = null;

    private final Command exitCommand;
    private final forge.quest.data.QuestData questData;
    private Deck currentDeck;

    // the class DeckDisplay is in the file "Gui_DeckEditor_Menu.java"
    private final DeckDisplay deckDisplay;

    /**
     * <p>
     * Constructor for Gui_Quest_DeckEditor_Menu.
     * </p>
     * 
     * @param q
     *            the q
     * @param d
     *            a {@link forge.gui.deckeditor.DeckDisplay} object.
     * @param exit
     *            a {@link forge.Command} object.
     */
    public DeckEditorQuestMenu(final QuestData q, final DeckDisplay d, final Command exit) {

        this.deckDisplay = d;
        this.questData = q;

        d.setTitle(DeckEditorQuestMenu.DECK_EDITOR_NAME);

        this.exitCommand = exit;

        this.setupMenu();
    }

    /**
     * <p>
     * addImportExport.
     * </p>
     * 
     * @param menu
     *            a {@link javax.swing.JMenu} object.
     * @param isHumanMenu
     *            a boolean.
     */
    private void addImportExport(final JMenu menu, final boolean isHumanMenu) {
        final JMenuItem import2 = new JMenuItem("Import");
        final JMenuItem export = new JMenuItem("Export");

        import2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent a) {
                DeckEditorQuestMenu.this.importDeck(); // importDeck(isHumanMenu);
            }
        }); // import

        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent a) {
                DeckEditorQuestMenu.this.exportDeck();
            }
        }); // export

        menu.add(import2);
        menu.add(export);

    } // addImportExport()

    /**
     * <p>
     * exportDeck.
     * </p>
     */
    private void exportDeck() {
        final File filename = this.getExportFilename();

        if (filename == null) {
            return;
        }

        // write is an Object variable because you might just
        // write one Deck object
        final Deck deck = this.cardPoolToDeck(this.deckDisplay.getBottom());

        deck.setName(filename.getName());

        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(deck);
            out.flush();
            out.close();
        } catch (final Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("Gui_Quest_DeckEditor_Menu : exportDeck() error, " + ex);
        }

        this.exportDeckText(this.getExportDeckText(deck), filename.getAbsolutePath());

    } // exportDeck()

    /**
     * <p>
     * exportDeckText.
     * </p>
     * 
     * @param deckText
     *            a {@link java.lang.String} object.
     * @param filename
     *            a {@link java.lang.String} object.
     */
    private void exportDeckText(final String deckText, String filename) {

        // remove ".deck" extension
        final int cut = filename.indexOf(".");
        filename = filename.substring(0, cut);

        try {
            final FileWriter writer = new FileWriter(filename + ".txt");
            writer.write(deckText);

            writer.flush();
            writer.close();
        } catch (final Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("Gui_Quest_DeckEditor_Menu : exportDeckText() error, " + ex.getMessage() + " : "
                    + Arrays.toString(ex.getStackTrace()));
        }
    } // exportDeckText()

    /**
     * <p>
     * getExportDeckText.
     * </p>
     * 
     * @param aDeck
     *            a {@link forge.deck.Deck} object.
     * @return a {@link java.lang.String} object.
     */
    private String getExportDeckText(final Deck aDeck) {
        // convert Deck into CardList
        final ItemPoolView<CardPrinted> all = aDeck.getMain();
        // sort by card name
        Collections.sort(all.getOrderedList(), TableSorter.BY_NAME_THEN_SET);

        final StringBuffer sb = new StringBuffer();
        final String newLine = "\r\n";

        sb.append(String.format("%d Total Cards%n%n", all.countAll()));

        // creatures

        sb.append(String.format("%d Creatures%n-------------%n",
                CardRules.Predicates.Presets.IS_CREATURE.aggregate(all, all.getFnToCard(), all.getFnToCount())));
        for (final Entry<CardPrinted, Integer> e : CardRules.Predicates.Presets.IS_CREATURE.select(all,
                all.getFnToCard())) {
            sb.append(String.format("%d x %s%n", e.getValue(), e.getKey().getName()));
        }

        // spells
        sb.append(String.format("%d Spells%n----------%n", CardRules.Predicates.Presets.IS_NON_CREATURE_SPELL
                .aggregate(all, all.getFnToCard(), all.getFnToCount())));
        for (final Entry<CardPrinted, Integer> e : CardRules.Predicates.Presets.IS_NON_CREATURE_SPELL.select(all,
                all.getFnToCard())) {
            sb.append(String.format("%d x %s%n", e.getValue(), e.getKey().getName()));
        }

        // lands
        sb.append(String.format("%d Land%n--------%n",
                CardRules.Predicates.Presets.IS_LAND.aggregate(all, all.getFnToCard(), all.getFnToCount())));
        for (final Entry<CardPrinted, Integer> e : CardRules.Predicates.Presets.IS_LAND.select(all, all.getFnToCard())) {
            sb.append(String.format("%d x %s%n", e.getValue(), e.getKey().getName()));
        }

        sb.append(newLine);

        return sb.toString();
    } // getExportDeckText

    /**
     * <p>
     * getFileFilter.
     * </p>
     * 
     * @return a {@link javax.swing.filechooser.FileFilter} object.
     */
    private FileFilter getFileFilter() {
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(".dck") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Deck File .dck";
            }
        };

        return filter;
    } // getFileFilter()

    /**
     * <p>
     * getExportFilename.
     * </p>
     * 
     * @return a {@link java.io.File} object.
     */
    private File getExportFilename() {
        // Object o = null; // unused

        final JFileChooser save = new JFileChooser(DeckEditorQuestMenu.previousDirectory);

        save.setDialogTitle("Export Deck Filename");
        save.setDialogType(JFileChooser.SAVE_DIALOG);
        save.addChoosableFileFilter(this.getFileFilter());
        save.setSelectedFile(new File(this.currentDeck.getName() + ".deck"));

        final int returnVal = save.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = save.getSelectedFile();
            final String check = file.getAbsolutePath();

            DeckEditorQuestMenu.previousDirectory = file.getParentFile();

            if (check.endsWith(".deck")) {
                return file;
            } else {
                return new File(check + ".deck");
            }
        }

        return null;
    } // getExportFilename()

    /**
     * <p>
     * importDeck.
     * </p>
     */
    private void importDeck() {
        final File file = this.getImportFilename();

        if (file == null) {
        } else if (file.getName().endsWith(".dck")) {
            try {
                final Deck newDeck = DeckIO.readDeck(file);
                this.questData.addDeck(newDeck);

                final ItemPool<CardPrinted> cardpool = ItemPool.createFrom(this.questData.getCards().getCardpool(),
                        CardPrinted.class);
                final ItemPool<CardPrinted> decklist = new ItemPool<CardPrinted>(CardPrinted.class);
                for (final Entry<CardPrinted, Integer> s : newDeck.getMain()) {
                    final CardPrinted cp = s.getKey();
                    decklist.add(cp, s.getValue());
                    cardpool.add(cp, s.getValue());
                    this.questData.getCards().getCardpool().add(cp, s.getValue());
                }
                this.deckDisplay.setDeck(cardpool, decklist, GameType.Quest);

            } catch (final Exception ex) {
                ErrorViewer.showError(ex);
                throw new RuntimeException("Gui_DeckEditor_Menu : importDeck() error, " + ex);
            }
        }

    } // importDeck()

    /**
     * <p>
     * getImportFilename.
     * </p>
     * 
     * @return a {@link java.io.File} object.
     */
    private File getImportFilename() {
        final JFileChooser chooser = new JFileChooser(DeckEditorQuestMenu.previousDirectory);

        chooser.addChoosableFileFilter(this.getFileFilter());
        final int returnVal = chooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            DeckEditorQuestMenu.previousDirectory = file.getParentFile();
            return file;
        }

        return null;
    } // openFileDialog()

    private final ActionListener addCardActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {

            // Provide a model here: all unique cards to be displayed by only
            // name (unlike default toString)
            final Iterable<CardPrinted> uniqueCards = CardDb.instance().getAllUniqueCards();
            final List<String> cards = new ArrayList<String>();
            for (final CardPrinted c : uniqueCards) {
                cards.add(c.getName());
            }
            Collections.sort(cards);

            // use standard forge's list selection dialog
            final ListChooser<String> c = new ListChooser<String>("Cheat - Add Card to Your Cardpool", 0, 1, cards);
            if (c.show()) {
                final String cardName = c.getSelectedValue();
                final DeckEditorQuest g = (DeckEditorQuest) DeckEditorQuestMenu.this.deckDisplay;
                g.addCheatCard(CardDb.instance().getCard(cardName));
            }
        }
    };

    private final ActionListener openDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            final String deckName = DeckEditorQuestMenu.this.getUserInputOpenDeck(DeckEditorQuestMenu.this.questData
                    .getDeckNames());

            // check if user selected "cancel"
            if (StringUtils.isBlank(deckName)) {
                return;
            }

            DeckEditorQuestMenu.this.setPlayerDeckName(deckName);
            final ItemPool<CardPrinted> cards = ItemPool.createFrom(DeckEditorQuestMenu.this.questData.getCards()
                    .getCardpool().getView(), CardPrinted.class);
            final ItemPoolView<CardPrinted> deck = DeckEditorQuestMenu.this.questData.getDeck(deckName).getMain();

            // show in pool all cards except ones used in deck
            cards.removeAll(deck);
            DeckEditorQuestMenu.this.deckDisplay.setDeck(cards, deck, GameType.Quest);
        }
    };

    private final ActionListener newDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            DeckEditorQuestMenu.this.deckDisplay.setItems(DeckEditorQuestMenu.this.questData.getCards().getCardpool()
                    .getView(), null, GameType.Quest);
            DeckEditorQuestMenu.this.setPlayerDeckName("");
        }
    };

    private final ActionListener renameDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            final String deckName = DeckEditorQuestMenu.this.getUserInputGetDeckName(DeckEditorQuestMenu.this.questData
                    .getDeckNames());

            // check if user cancels
            if (StringUtils.isBlank(deckName)) {
                return;
            }

            // is the current deck already saved and in QuestData?
            if (DeckEditorQuestMenu.this.questData.getDeckNames().contains(
                    DeckEditorQuestMenu.this.currentDeck.getName())) {
                DeckEditorQuestMenu.this.questData.removeDeck(DeckEditorQuestMenu.this.currentDeck.getName());
            }

            DeckEditorQuestMenu.this.currentDeck.setName(deckName);

            final Deck deck = DeckEditorQuestMenu.this.cardPoolToDeck(DeckEditorQuestMenu.this.deckDisplay.getBottom());
            deck.setName(deckName);
            DeckEditorQuestMenu.this.questData.addDeck(deck);

            DeckEditorQuestMenu.this.setPlayerDeckName(deckName);
        }
    };

    private final ActionListener saveDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            String name = DeckEditorQuestMenu.this.currentDeck.getName();

            // check to see if name is set
            if (name.equals("")) {
                name = DeckEditorQuestMenu.this.getUserInputGetDeckName(DeckEditorQuestMenu.this.questData
                        .getDeckNames());

                // check if user cancels
                if (name.equals("")) {
                    return;
                }
            }

            DeckEditorQuestMenu.this.setPlayerDeckName(name);

            final Deck deck = DeckEditorQuestMenu.this.cardPoolToDeck(DeckEditorQuestMenu.this.deckDisplay.getBottom());
            deck.setName(name);

            DeckEditorQuestMenu.this.questData.addDeck(deck);
        }
    };

    private final ActionListener copyDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            final String name = DeckEditorQuestMenu.this.getUserInputGetDeckName(DeckEditorQuestMenu.this.questData
                    .getDeckNames());

            // check if user cancels
            if (name.equals("")) {
                return;
            }

            DeckEditorQuestMenu.this.setPlayerDeckName(name);

            final Deck deck = DeckEditorQuestMenu.this.cardPoolToDeck(DeckEditorQuestMenu.this.deckDisplay.getBottom());
            deck.setName(name);

            DeckEditorQuestMenu.this.questData.addDeck(deck);
        }
    };

    private final ActionListener deleteDeckActionListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent a) {
            if (DeckEditorQuestMenu.this.currentDeck.getName().equals("")) {
                return;
            }

            final int check = JOptionPane.showConfirmDialog(null, "Do you really want to delete this deck?", "Delete",
                    JOptionPane.YES_NO_OPTION);
            if (check == JOptionPane.NO_OPTION) {
                return; // stop here
            }

            DeckEditorQuestMenu.this.questData.removeDeck(DeckEditorQuestMenu.this.currentDeck.getName());

            // show card pool
            DeckEditorQuestMenu.this.deckDisplay.setItems(DeckEditorQuestMenu.this.questData.getCards().getCardpool()
                    .getView(), null, GameType.Quest);

            DeckEditorQuestMenu.this.setPlayerDeckName("");
        }
    };

    // the usual menu options that will be used
    /**
     * <p>
     * setupMenu.
     * </p>
     */
    private void setupMenu() {
        final JMenuItem openDeck = new JMenuItem("Open");
        final JMenuItem newDeck = new JMenuItem("New");
        final JMenuItem rename = new JMenuItem("Rename");
        final JMenuItem save = new JMenuItem("Save");
        final JMenuItem copy = new JMenuItem("Copy");
        final JMenuItem delete = new JMenuItem("Delete");
        final JMenuItem exit = new JMenuItem("Exit");

        final JMenuItem addCard = new JMenuItem("Cheat - Add Card");

        addCard.addActionListener(this.addCardActionListener);
        openDeck.addActionListener(this.openDeckActionListener);
        newDeck.addActionListener(this.newDeckActionListener);
        rename.addActionListener(this.renameDeckActionListener);
        save.addActionListener(this.saveDeckActionListener);
        copy.addActionListener(this.copyDeckActionListener);
        delete.addActionListener(this.deleteDeckActionListener);

        // human
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent a) {
                DeckEditorQuestMenu.this.close();
            }
        });

        final JMenu deckMenu = new JMenu("Deck");
        deckMenu.add(openDeck);
        deckMenu.add(newDeck);
        deckMenu.add(rename);
        deckMenu.add(save);
        deckMenu.add(copy);

        if (Constant.Runtime.DEV_MODE[0]) {
            deckMenu.addSeparator();
            deckMenu.add(addCard);
        }

        deckMenu.addSeparator();
        this.addImportExport(deckMenu, true);

        deckMenu.addSeparator();
        deckMenu.add(delete);
        deckMenu.addSeparator();
        deckMenu.add(exit);

        this.add(deckMenu);

    }

    /**
     * <p>
     * convertCardPoolToDeck.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardPool} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck cardPoolToDeck(final ItemPoolView<InventoryItem> list) {
        // put CardPool into Deck main
        final Deck deck = new Deck(GameType.Sealed);
        deck.getMain().addAll(list);
        return deck;
    }

    // needs to be public because Gui_Quest_DeckEditor.show(Command) uses it
    /**
     * <p>
     * setHumanPlayer.
     * </p>
     * 
     * @param deckName
     *            a {@link java.lang.String} object.
     */
    public final void setPlayerDeckName(final String deckName) {
        // the gui uses this, Gui_Quest_DeckEditor
        this.currentDeck = new Deck(GameType.Sealed);
        this.currentDeck.setName(deckName);

        this.deckDisplay.setTitle(DeckEditorQuestMenu.DECK_EDITOR_NAME + " - " + deckName);
    }

    // only accepts numbers, letters or dashes up to 20 characters in length
    /**
     * <p>
     * cleanString.
     * </p>
     * 
     * @param in
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    private String cleanString(final String in) {
        final StringBuffer out = new StringBuffer();
        final char[] c = in.toCharArray();

        for (int i = 0; (i < c.length) && (i < 20); i++) {
            if (Character.isLetterOrDigit(c[i]) || (c[i] == '-') || (c[i] == '_') || (c[i] == ' ')) {
                out.append(c[i]);
            }
        }

        return out.toString();
    }

    // if user cancels, returns ""
    /**
     * <p>
     * getUserInput_GetDeckName.
     * </p>
     * 
     * @param nameList
     *            a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    private String getUserInputGetDeckName(final List<String> nameList) {
        final Object o = JOptionPane.showInputDialog(null, "", "Deck Name", JOptionPane.OK_CANCEL_OPTION);

        if (o == null) {
            return "";
        }

        final String deckName = this.cleanString(o.toString());

        if (nameList.contains(deckName) || deckName.equals("")) {
            JOptionPane.showMessageDialog(null, "Please pick another deck name, a deck currently has that name.");
            return this.getUserInputGetDeckName(nameList);
        }

        return deckName;
    } // getUserInput_GetDeckName()

    // if user cancels, it will return ""
    /**
     * <p>
     * getUserInput_OpenDeck.
     * </p>
     * 
     * @param deckNameList
     *            a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    private String getUserInputOpenDeck(final List<String> deckNameList) {
        final List<String> choices = deckNameList;
        if (choices.size() == 0) {
            JOptionPane.showMessageDialog(null, "No decks found", "Open Deck", JOptionPane.PLAIN_MESSAGE);
            return "";
        }

        // Object o = JOptionPane.showInputDialog(null, "Deck Name",
        // "Open Deck", JOptionPane.OK_CANCEL_OPTION, null,
        // choices.toArray(), choices.toArray()[0]);
        final Object o = GuiUtils.getChoiceOptional("Select Deck", choices.toArray());

        if (o == null) {
            return "";
        }

        return o.toString();
    } // getUserInput_OpenDeck()

    // used by Gui_Quest_DeckEditor
    /**
     * <p>
     * close.
     * </p>
     */
    public final void close() {
        this.exitCommand.execute();
    }

    // used by Gui_Quest_DeckEditor
    /**
     * <p>
     * getDeckName.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getDeckName() {
        return this.currentDeck.getName();
    }

}
