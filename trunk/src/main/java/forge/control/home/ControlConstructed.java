package forge.control.home;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import forge.AllZone;
import forge.CardList;
import forge.Constant;
import forge.PlayerType;
import forge.deck.Deck;
import forge.deck.generate.Generate2ColorDeck;
import forge.deck.generate.Generate3ColorDeck;
import forge.deck.generate.Generate5ColorDeck;
import forge.deck.generate.GenerateThemeDeck;
import forge.game.GameType;
import forge.gui.GuiUtils;
import forge.item.CardPrinted;
import forge.view.GuiTopLevel;
import forge.view.home.ViewConstructed;

/** 
 * Controls logic and listeners for Constructed mode in home screen.
 *
 */
public class ControlConstructed {
    private ViewConstructed view;

    private JList currentHumanSelection = null;
    private JList currentAISelection = null;

    private Map<String, String> colorVals;
    private List<String> themeNames;
    private List<String> deckNames;

    /**
     * 
     * Controls logic and listeners for "constructed" mode in home screen.
     * 
     * @param v0 &emsp; ViewConstructed
     */
    public ControlConstructed(ViewConstructed v0) {
        this.view = v0;

        // Reference values for colors, needed for deck generation classes.
        // TODO enum me or get from another enum
        colorVals = new HashMap<String, String>();
        colorVals.put("Random", "AI");
        colorVals.put("Random 1", "AI");
        colorVals.put("Random 2", "AI");
        colorVals.put("Random 3", "AI");
        colorVals.put("Random 4", "AI");
        colorVals.put("Black", "black");
        colorVals.put("Blue", "blue");
        colorVals.put("Green", "green");
        colorVals.put("Red", "red");
        colorVals.put("White", "white");
    }

    /** @return ViewConstructed */
    public ViewConstructed getView() {
        return view;
    }

    /** */
    public void addListeners() {
        view.getLstColorsAI().getSelectionModel().addListSelectionListener(new AIColorsListener());
        view.getLstColorsAI().setSelectedIndices(new int[] {0, 1});
        view.getLstThemesAI().getSelectionModel().addListSelectionListener(new AIThemesListener());
        view.getLstDecksAI().getSelectionModel().addListSelectionListener(new AIDecksListener());
        view.getLstColorsHuman().getSelectionModel().addListSelectionListener(new HumanColorsListener());
        view.getLstThemesHuman().getSelectionModel().addListSelectionListener(new HumanThemesListener());
        view.getLstDecksHuman().getSelectionModel().addListSelectionListener(new HumanDecksListener());
        view.getLstColorsHuman().setSelectedIndices(new int[] {0, 1});
    }

  //========== LISTENERS

    /** Listeners for human lists, which pass control directly to regulation logic. */
    public class HumanColorsListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateHuman(view.getLstColorsHuman()); }
    }

    private class HumanThemesListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateHuman(view.getLstThemesHuman()); }
    }

    private class HumanDecksListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateHuman(view.getLstDecksHuman()); }
    }

    /** Listeners for AI lists, which pass control directly to regulation logic. */
    private class AIColorsListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateAI(view.getLstColorsAI()); }
    }

    private class AIThemesListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateAI(view.getLstThemesAI()); }
    }

    private class AIDecksListener implements ListSelectionListener {
        @Override
        public void valueChanged(final ListSelectionEvent e) { regulateAI(view.getLstDecksAI()); }
    }

    //========= REGULATION AND VALIDATION

    /** 
     * Checks lengths of selected values for human color lists
     * to see if a deck generator exists. Alert and visual reminder if fail.
     * 
     * @param human0 &emsp; String[] of color names
     * @return boolean
     */
    public boolean checkValidityOfHumanSelectedColors(String[] human0) {
        boolean result = true;

        if (human0.length == 1) {
            JOptionPane.showMessageDialog(null,
                    "Sorry, single color generated decks aren't supported yet."
                    + "\n\rPlease choose at least one more color for the human deck.",
                    "Human deck: 1 color", JOptionPane.ERROR_MESSAGE);
            view.remind(currentHumanSelection);
            result = false;
        }
        else if (human0.length == 4) {
            JOptionPane.showMessageDialog(null,
                    "Sorry, four color generated decks aren't supported yet."
                    + "\n\rPlease use 2, 3, or 5 colors for the human deck.",
                    "Human deck: 4 colors", JOptionPane.ERROR_MESSAGE);
            view.remind(currentHumanSelection);
            result = false;
        }
        else if (human0.length > 5) {
            JOptionPane.showMessageDialog(null,
                    "Human deck: maximum five colors!",
                    "Human deck: too many colors", JOptionPane.ERROR_MESSAGE);
            view.remind(currentHumanSelection);
            result = false;
        }

        return result;
    }

    /** 
     * Checks lengths of selected values for AI color lists
     * to see if a deck generator exists. Alert and visual reminder if fail.
     *
     * @param ai0 &emsp; String[] of color names
     * @return boolean
     */
    public boolean checkValidityOfAISelectedColors(String[] ai0) {
        boolean result = true;

        if (ai0.length == 1) {
            JOptionPane.showMessageDialog(null,
                    "Sorry, single color generated decks aren't supported yet."
                    + "\n\rPlease choose at least one more color for the AI deck.",
                    "AI deck: 1 color", JOptionPane.ERROR_MESSAGE);
            view.remind(currentAISelection);
            result = false;
        }
        else if (ai0.length == 4) {
            JOptionPane.showMessageDialog(null,
                    "Sorry, four color generated decks aren't supported yet."
                    + "\n\rPlease use 2, 3, or 5 colors for the AI deck.",
                    "AI deck: 4 colors", JOptionPane.ERROR_MESSAGE);
            view.remind(currentAISelection);
            result = false;
        }
        else if (ai0.length > 5) {
            JOptionPane.showMessageDialog(null,
                    "AI deck: maximum five colors!",
                    "AI deck: Too many colors", JOptionPane.ERROR_MESSAGE);
            view.remind(currentAISelection);
            result = false;
        }

        return result;
    }

    /** 
     * Regulates that only one of the three deck type JLists is in use at a time.
     *
     * @param lst0 &emsp; a JList that has been clicked
     */
    public void regulateHuman(JList lst0) {
        if (currentHumanSelection != null && lst0 != currentHumanSelection) {
            currentHumanSelection.clearSelection();
        }

        currentHumanSelection = lst0;

        // Random chooser for theme decks
        if (lst0.getName() != null && lst0.getName().equals("lstThemesHuman")
                && lst0.getSelectedIndex() == 0) {
            Random r = new Random();

            int i = 0;
            while (i == 0) { i = r.nextInt(themeNames.size()); }
            lst0.setSelectedIndex(i);
        }

        // Random chooser for pre-constructed decks
        if (lst0.getName() != null && lst0.getName().equals("lstDecksHuman")
                && lst0.getSelectedIndex() == 0) {
            Random r = new Random();

            int i = 0;
            while (i == 0) { i = r.nextInt(deckNames.size()); }
            lst0.setSelectedIndex(i);
        }

        // Toggle "view" button accordingly
        if (lst0.getName() != null && lst0.getName().equals("lstDecksHuman")) {
            view.getBtnHumanDeckList().setEnabled(true);
        }
        else {
            view.getBtnHumanDeckList().setEnabled(false);
        }
    }

    /** 
     * Regulates that only one of the three deck type JLists is in use at a time.
     * 
     * @param lst0 &emsp; a JList that has been clicked
     */
    public void regulateAI(JList lst0) {
        if (currentAISelection != null && lst0 != currentAISelection) {
            currentAISelection.clearSelection();
        }

        currentAISelection = lst0;

        // Random chooser for theme decks
        if (lst0.getName() != null && lst0.getName().equals("lstThemesAI")
                && lst0.getSelectedIndex() == 0) {
            Random r = new Random();

            int i = 0;
            while (i == 0) { i = r.nextInt(themeNames.size()); }
            lst0.setSelectedIndex(i);
        }

        // Random chooser for pre-constructed decks
        if (lst0.getName() != null && lst0.getName().equals("lstDecksAI")
                && lst0.getSelectedIndex() == 0) {
            Random r = new Random();

            int i = 0;
            while (i == 0) { i = r.nextInt(deckNames.size()); }
            lst0.setSelectedIndex(i);
        }

        // Toggle "view" button accordingly
        if (lst0.getName() != null && lst0.getName().equals("lstDecksAI")) {
            view.getBtnAIDeckList().setEnabled(true);
        }
        else {
            view.getBtnAIDeckList().setEnabled(false);
        }
    }

    //========= DECK GENERATION

    /** Generates human deck from current list selection(s). */
    private void generateHumanDecks(String[] human0) {
        CardList cards = null;

        // Human: Color-based deck generation
        if (currentHumanSelection.getName().equals("lstColorsHuman")) {
            // Replace "random" with "AI" for deck generation code
            for (int i = 0; i < human0.length; i++) {
                human0[i] = colorVals.get(human0[i]);
            }

            // 2, 3, and 5 colors.
            if (human0.length == 2) {
                Generate2ColorDeck gen = new Generate2ColorDeck(
                        human0[0], human0[1]);
                cards = gen.get2ColorDeck(60, PlayerType.HUMAN);
            }
            else if (human0.length == 3) {
                Generate3ColorDeck gen = new Generate3ColorDeck(
                        human0[0], human0[1], human0[2]);
                cards = gen.get3ColorDeck(60, PlayerType.HUMAN);
            }
            else {
                Generate5ColorDeck gen = new Generate5ColorDeck(
                        human0[0], human0[1], human0[2], human0[3], human0[4]);
                cards = gen.get5ColorDeck(60, PlayerType.HUMAN);
            }

            // After generating card lists, convert to deck and save.
            final Deck deck = new Deck(GameType.Constructed);

            for (int i = 0; i < cards.size(); i++) {
                deck.addMain(cards.get(i).getName());
            }

            Constant.Runtime.HUMAN_DECK[0] = deck;
        }

        // Human: theme deck generation
        else if (currentHumanSelection.getName().equals("lstThemesHuman")) {
            GenerateThemeDeck gen = new GenerateThemeDeck();
            cards = gen.getThemeDeck(human0[0], 60);

            // After generating card lists, convert to deck and save.
            final Deck deck = new Deck(GameType.Constructed);

            for (int i = 0; i < cards.size(); i++) {
                deck.addMain(cards.get(i).getName());
            }

            Constant.Runtime.HUMAN_DECK[0] = deck;
        }

        // Human: deck file
        else {
            Constant.Runtime.HUMAN_DECK[0] = AllZone.getDeckManager().getDeck(human0[0]);
        }
    }

    private void generateAIDecks(String[] ai0) {
        CardList cards = null;
        // AI: Color-based deck generation
        if (currentAISelection.getName().equals("lstColorsAI")) {
            // Replace "random" with "AI" for deck generation code
            for (int i = 0; i < ai0.length; i++) {
                ai0[i] = colorVals.get(ai0[i]);
            }

            // 2, 3, and 5 colors.
            if (ai0.length == 2) {
                Generate2ColorDeck gen = new Generate2ColorDeck(
                        ai0[0], ai0[1]);
                cards = gen.get2ColorDeck(60, PlayerType.COMPUTER);
            }
            else if (ai0.length == 3) {
                Generate3ColorDeck gen = new Generate3ColorDeck(
                        ai0[0], ai0[1], ai0[2]);
                cards = gen.get3ColorDeck(60, PlayerType.COMPUTER);
            }
            else {
                Generate5ColorDeck gen = new Generate5ColorDeck(
                        ai0[0], ai0[1], ai0[2], ai0[3], ai0[4]);
                cards = gen.get5ColorDeck(60, PlayerType.COMPUTER);
            }

            // After generating card lists, convert to deck and save.
            final Deck deck = new Deck(GameType.Constructed);

            for (int i = 0; i < cards.size(); i++) {
                deck.addMain(cards.get(i).getName());
            }

            Constant.Runtime.COMPUTER_DECK[0] = deck;
        }

        // AI: theme deck generation
        else if (currentAISelection.getName().equals("lstThemesAI")) {
            GenerateThemeDeck gen = new GenerateThemeDeck();
            cards = gen.getThemeDeck(ai0[0], 60);

            // After generating card lists, convert to deck and save.
            final Deck deck = new Deck(GameType.Constructed);

            for (int i = 0; i < cards.size(); i++) {
                deck.addMain(cards.get(i).getName());
            }

            Constant.Runtime.COMPUTER_DECK[0] = deck;
        }

        // AI: deck file
        else {
            Constant.Runtime.COMPUTER_DECK[0] = AllZone.getDeckManager().getDeck(ai0[0]);
        }
    }

    //========= OTHER
    /** Fired when start button is pressed; checks various conditions from lists and starts game. */
    public void start() {
        String[] humanSelected = GuiUtils.oa2sa(currentHumanSelection.getSelectedValues());
        String[] aiSelected = GuiUtils.oa2sa(currentAISelection.getSelectedValues());

        // Check color-based deck selection for appropriate length
        if (currentHumanSelection.getName().equals("lstColorsHuman")) {
            if (!checkValidityOfHumanSelectedColors(humanSelected)) { return; }
        }

        if (currentAISelection.getName().equals("lstColorsAI")) {
            if (!checkValidityOfAISelectedColors(aiSelected)) { return; }
        }

        // If deck selection is acceptable, start a new game.
        generateHumanDecks(humanSelected);
        generateAIDecks(aiSelected);

        GuiTopLevel g = ((GuiTopLevel) AllZone.getDisplay());

        g.getController().changeState(1);
        g.getController().getMatchController().initMatch();

        AllZone.getGameAction().newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
    }

    //========= LIST BOX VALUES
    /**
     * 
     * Array of color selections present in list boxes. Values
     * correspond to colorVals hash map.
     * 
     * @return Object[]
     */
    // Four randoms are included which should cover all possibilities.
    public Object[] getColorNames() {
        return new Object[] {"Random", "Random", "Random",
                "Random", "Black", "Blue", "Green", "Red", "White"};
    }

    /**
     * Array of theme names, usually used in list boxes.
     * 
     * @return Object[]
     */
    public Object[] getThemeNames() {
        themeNames = new ArrayList<String>();
        themeNames.add("Random");
        for (String s : GenerateThemeDeck.getThemeNames()) {
            themeNames.add(s);
        }
        // No theme decks?
        if (themeNames.size() == 1) { themeNames = new ArrayList<String>(); }

        return themeNames.toArray();
    }

    /** */
    public void updateDeckNames() {
        deckNames = new ArrayList<String>();
        deckNames.add(0, "Random");

        Collection<Deck> allDecks = AllZone.getDeckManager().getConstructedDecks();
        for (Deck d : allDecks) {
            deckNames.add(d.getName());
        }

        // No pre-constructed decks?
        if (deckNames.size() == 1) { deckNames = new ArrayList<String>(); }

        view.getLstDecksHuman().setListData(deckNames.toArray());
        view.getLstDecksAI().setListData(deckNames.toArray());
    }

    /**
     * View deck list.
     * 
     * @param player &emsp; String
     */
    public void viewDeckList(final String player) {
        new DeckListAction(player).actionPerformed(null);
    }

    /**
     * Receives click and programmatic requests for viewing card in a player's deck.
     * 
     */
    private class DeckListAction {
        public DeckListAction(final String playerIn) {
            player = playerIn;
        }

        private String player;

        public void actionPerformed(final ActionEvent e) {
            if (player.equals("Human")
                    && (currentHumanSelection.getName().equals("lstColorsHuman")
                    || currentHumanSelection.getName().equals("lstThemesHuman"))) {
                return;
            }
            if (player.equals("Computer")
                    && (currentAISelection.getName().equals("lstColorsAI")
                    || currentAISelection.getName().equals("lstThemesAI"))) {
                return;
            }

            Deck targetDeck = (player.equals("Human"))
                    ? AllZone.getDeckManager().getDeck(GuiUtils.oa2sa(currentHumanSelection.getSelectedValues())[0])
                    : AllZone.getDeckManager().getDeck(GuiUtils.oa2sa(currentAISelection.getSelectedValues())[0]);

            final HashMap<String, Integer> deckMap = new HashMap<String, Integer>();

            for (final Entry<CardPrinted, Integer> s : targetDeck.getMain()) {
                deckMap.put(s.getKey().getName(), s.getValue());
            }

            final String nl = System.getProperty("line.separator");
            final StringBuilder deckList = new StringBuilder();
            String dName = targetDeck.getName();

            if (dName == null) {
                dName = "";
            } else {
                deckList.append(dName + nl);
            }

            final ArrayList<String> dmKeys = new ArrayList<String>();
            for (final String s : deckMap.keySet()) {
                dmKeys.add(s);
            }

            Collections.sort(dmKeys);

            for (final String s : dmKeys) {
                deckList.append(deckMap.get(s) + " x " + s + nl);
            }

            int rcMsg = -1138;
            String ttl = player + "'s Decklist";
            if (!dName.equals("")) {
                ttl += " - " + dName;
            }

            final StringBuilder msg = new StringBuilder();
            if (deckMap.keySet().size() <= 32) {
                msg.append(deckList.toString() + nl);
            } else {
                msg.append("Decklist too long for dialog." + nl + nl);
            }

            msg.append("Copy Decklist to Clipboard?");

            rcMsg = JOptionPane.showConfirmDialog(null, msg, ttl, JOptionPane.OK_CANCEL_OPTION);

            if (rcMsg == JOptionPane.OK_OPTION) {
                final StringSelection ss = new StringSelection(deckList.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            }
        }
    } // End DeckListAction
}
