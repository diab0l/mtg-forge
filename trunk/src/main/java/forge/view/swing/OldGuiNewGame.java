package forge.view.swing;

import static net.slightlymagic.braids.util.UtilFunctions.safeToString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import arcane.ui.util.ManaSymbols;

import com.esotericsoftware.minlog.Log;

import forge.AllZone;
import forge.BoosterDraft_1;
import forge.CardList;
import forge.Command;
import forge.Constant;
import forge.Constant_StringArrayList;
import forge.FileUtil;
import forge.GUI_ImportPicture;
import forge.GuiDisplay4;
import forge.Gui_BoosterDraft;
import forge.Gui_DeckEditor;
import forge.Gui_DownloadPictures_LQ;
import forge.Gui_DownloadPrices;
import forge.Gui_DownloadSetPictures_LQ;
import forge.Gui_QuestOptions;
import forge.ImageCache;
import forge.MyRandom;
import forge.SealedDeck;
import forge.deck.Deck;
import forge.deck.DeckManager;
import forge.deck.generate.Generate2ColorDeck;
import forge.deck.generate.Generate3ColorDeck;
import forge.deck.generate.GenerateConstructedDeck;
import forge.deck.generate.GenerateConstructedMultiColorDeck;
import forge.deck.generate.GenerateThemeDeck;
import forge.error.BugzReporter;
import forge.error.ErrorViewer;
import forge.gui.GuiUtils;
import forge.gui.ListChooser;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.CardSizeType;
import forge.properties.ForgePreferences.StackOffsetType;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.properties.NewConstants.LANG.OldGuiNewGame.MENU_BAR.MENU;
import forge.properties.NewConstants.LANG.OldGuiNewGame.MENU_BAR.OPTIONS;

/*CHOPPIC*/

/**
 * <p>
 * OldGuiNewGame class.
 * </p>
 *
 * @author Forge
 * @version $Id$
 */
public class OldGuiNewGame extends JFrame implements NewConstants, NewConstants.LANG.OldGuiNewGame {
    /** Constant <code>serialVersionUID=-2437047615019135648L</code>. */
    private static final long serialVersionUID = -2437047615019135648L;

    // private final DeckManager deckManager = new
    // DeckManager(ForgeProps.getFile(NEW_DECKS));
    private final DeckManager deckManager = AllZone.getDeckManager();
    // with the new IO, there's no reason to use different instances
    private List<Deck> allDecks;
    /** Constant <code>editor</code>. */
    private static Gui_DeckEditor editor;

    private JLabel titleLabel = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private JComboBox humanComboBox = new JComboBox();
    private JComboBox computerComboBox = new JComboBox();
    private JButton deckEditorButton = new JButton();
    private JButton startButton = new JButton();
    private ButtonGroup buttonGroup1 = new ButtonGroup();
    private JRadioButton sealedRadioButton = new JRadioButton();
    private JRadioButton singleRadioButton = new JRadioButton();

    private JRadioButton draftRadioButton = new JRadioButton();

    /* CHOPPIC */
    private CustomPanel jPanel1 = new CustomPanel(10);
    private CustomPanel jPanel2 = new CustomPanel(10);
    private CustomPanel jPanel3 = new CustomPanel(10);
    /* CHOPPIC */

    // @SuppressWarnings("unused")
    // titledBorder2
    /** Constant <code>newGuiCheckBox</code> */
    // private static JCheckBox newGuiCheckBox = new JCheckBox("", true);
    /** Constant <code>smoothLandCheckBox</code>. */
    static JCheckBox smoothLandCheckBox = new JCheckBox("", false);
    /** Constant <code>devModeCheckBox</code>. */
    static JCheckBox devModeCheckBox = new JCheckBox("", true);

    static JCheckBox upldDrftCheckBox = new JCheckBox("", true);
    static JCheckBox foilRandomCheckBox = new JCheckBox("", true);

    // GenerateConstructedDeck.get2Colors() and GenerateSealedDeck.get2Colors()
    // use these two variables
    /** Constant <code>removeSmallCreatures</code>. */
    public static JCheckBoxMenuItem removeSmallCreatures = new JCheckBoxMenuItem(
            ForgeProps.getLocalized(MENU_BAR.OPTIONS.GENERATE.REMOVE_SMALL));
    /** Constant <code>removeArtifacts</code>. */
    public static JCheckBoxMenuItem removeArtifacts = new JCheckBoxMenuItem(
            ForgeProps.getLocalized(MENU_BAR.OPTIONS.GENERATE.REMOVE_ARTIFACTS));
    /** Constant <code>useLAFFonts</code>. */
    public static JCheckBoxMenuItem useLAFFonts = new JCheckBoxMenuItem(ForgeProps.getLocalized(MENU_BAR.OPTIONS.FONT));
    /** Constant <code>cardOverlay</code>. */
    public static JCheckBoxMenuItem cardOverlay = new JCheckBoxMenuItem(
            ForgeProps.getLocalized(MENU_BAR.OPTIONS.CARD_OVERLAY));
    /** Constant <code>cardScale</code>. */
    public static JCheckBoxMenuItem cardScale = new JCheckBoxMenuItem(
            ForgeProps.getLocalized(MENU_BAR.OPTIONS.CARD_SCALE));
    private JButton questButton = new JButton();

    private final Action LOOK_AND_FEEL_ACTION = new LookAndFeelAction(this);
    // private Action DOWNLOAD_ACTION = new DownloadAction();
    private final Action DOWNLOAD_ACTION_LQ = new DownloadActionLQ();
    private final Action DOWNLOAD_ACTION_SETLQ = new DownloadActionSetLQ();
    private final Action IMPORT_PICTURE = new ImportPictureAction();
    private final Action CARD_SIZES_ACTION = new CardSizesAction();
    private final Action CARD_STACK_ACTION = new CardStackAction();
    private final Action CARD_STACK_OFFSET_ACTION = new CardStackOffsetAction();
    private final Action ABOUT_ACTION = new AboutAction();
    private final Action HOW_TO_PLAY_ACTION = new HowToPlayAction();
    private final Action DNLD_PRICES_ACTION = new DownloadPriceAction();
    private final Action BUGZ_REPORTER_ACTION = new BugzReporterAction();

    /** Constant <code>preferences</code>. */
    public static ForgePreferences preferences;

    /**
     * <p>
     * Constructor for OldGuiNewGame.
     * </p>
     */
    public OldGuiNewGame() {

        AllZone.setQuestData(null);
        allDecks = getDecks();
        Constant.Runtime.matchState.reset();

        if (Constant.Runtime.width[0] == 0) {
            Constant.Runtime.width[0] = 70;
        }

        if (Constant.Runtime.height[0] == 0) {
            Constant.Runtime.height[0] = 98;
        }

        if (Constant.Runtime.stackSize[0] == 0) {
            Constant.Runtime.stackSize[0] = 4;
        }

        if (Constant.Runtime.stackOffset[0] == 0) {
            Constant.Runtime.stackOffset[0] = 10;
        }

        try {
            jbInit();
        } catch (Exception ex) {
            ErrorViewer.showError(ex);
        }

        if (Constant.Runtime.GameType[0].equals(Constant.GameType.Constructed)) {
            singleRadioButton.setSelected(true);
            updateDeckComboBoxes();
        }
        if (Constant.Runtime.GameType[0].equals(Constant.GameType.Sealed)) {
            sealedRadioButton.setSelected(true);
            updateDeckComboBoxes();
        }
        if (Constant.Runtime.GameType[0].equals(Constant.GameType.Draft)) {
            draftRadioButton.setSelected(true);
            draftRadioButtonActionPerformed(null);
        }

        addListeners();

        this.setSize(550, 565);
        GuiUtils.centerFrame(this);

        setTitle(ForgeProps.getLocalized(LANG.PROGRAM_NAME));
        setupMenu();
        setVisible(true);

        ManaSymbols.loadImages();
        Log.WARN(); // set logging level to warn
        SwingUtilities.updateComponentTreeUI(this);
    }

    // init()

    /**
     * <p>
     * setupMenu.
     * </p>
     */
    private void setupMenu() {
        Action[] actions = {
                // Remove the option to download HQ pics since the HQ pics
                // server appears to be offline.
                // LOOK_AND_FEEL_ACTION, DNLD_PRICES_ACTION, DOWNLOAD_ACTION,
                // DOWNLOAD_ACTION_LQ, DOWNLOAD_ACTION_SETLQ, IMPORT_PICTURE,
                // CARD_SIZES_ACTION,
                LOOK_AND_FEEL_ACTION, DNLD_PRICES_ACTION, DOWNLOAD_ACTION_LQ, DOWNLOAD_ACTION_SETLQ, IMPORT_PICTURE,
                CARD_SIZES_ACTION, CARD_STACK_ACTION, CARD_STACK_OFFSET_ACTION, BUGZ_REPORTER_ACTION,
                ErrorViewer.ALL_THREADS_ACTION, ABOUT_ACTION };
        JMenu menu = new JMenu(ForgeProps.getLocalized(MENU.TITLE));
        for (Action a : actions) {
            menu.add(a);
            if (a.equals(LOOK_AND_FEEL_ACTION) || a.equals(IMPORT_PICTURE) || a.equals(CARD_STACK_OFFSET_ACTION)
                    || a.equals(ErrorViewer.ALL_THREADS_ACTION))
            {
                menu.addSeparator();
            }
        }

        // useLAFFonts.setSelected(false);

        // new stuff
        JMenu generatedDeck = new JMenu(ForgeProps.getLocalized(MENU_BAR.OPTIONS.GENERATE.TITLE));
        generatedDeck.add(removeSmallCreatures);
        generatedDeck.add(removeArtifacts);
        JMenu optionsMenu = new JMenu(ForgeProps.getLocalized(OPTIONS.TITLE));
        optionsMenu.add(generatedDeck);

        optionsMenu.add(useLAFFonts);
        optionsMenu.addSeparator();
        optionsMenu.add(cardOverlay);
        optionsMenu.add(cardScale);

        cardScale.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                ImageCache.scaleLargerThanOriginal = cardScale.isSelected();
            }
        });

        JMenu helpMenu = new JMenu(ForgeProps.getLocalized(MENU_BAR.HELP.TITLE));

        Action[] helpActions = {HOW_TO_PLAY_ACTION};
        for (Action a : helpActions) {
            helpMenu.add(a);
        }

        JMenuBar bar = new JMenuBar();
        bar.add(menu);
        bar.add(optionsMenu);
        bar.add(helpMenu);
        // bar.add(new MenuItem_HowToPlay());

        setJMenuBar(bar);
    }

    // returns, ArrayList of Deck objects
    /**
     * <p>
     * getDecks.
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    private List<Deck> getDecks() {
        List<Deck> list = new ArrayList<Deck>(deckManager.getDecks());

        Collections.sort(list);
        return list;
    }

    /**
     * <p>
     * addListeners.
     * </p>
     */
    private void addListeners() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent ev) {
                System.exit(0);
            }
        });

        questButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // close this windows
                // can't use this.dispose() because "this" object is an
                // ActionListener
                OldGuiNewGame.this.dispose();

                new Gui_QuestOptions();
            }
        });
    } // addListeners()

    /**
     * <p>
     * setupSealed.
     * </p>
     */
    private void setupSealed() {
        Deck deck = new Deck(Constant.GameType.Sealed);

        // ReadBoosterPack booster = new ReadBoosterPack();
        // CardList pack = booster.getBoosterPack5();

        ArrayList<String> sealedTypes = new ArrayList<String>();
        sealedTypes.add("Full Cardpool");
        sealedTypes.add("Block / Set");
        sealedTypes.add("Custom");

        String prompt = "Choose Sealed Deck Format:";
        Object o = GuiUtils.getChoice(prompt, sealedTypes.toArray());

        SealedDeck sd = null;

        if (o.toString().equals(sealedTypes.get(0))) {
            sd = new SealedDeck("Full");
        }

        else if (o.toString().equals(sealedTypes.get(1))) {
            sd = new SealedDeck("Block");
        }

        else if (o.toString().equals(sealedTypes.get(2))) {
            sd = new SealedDeck("Custom");
        }

        else {
            throw new IllegalStateException(
                    "choice <<" + safeToString(o) + ">> does not equal any of the sealedTypes.");
        }

        CardList sDeck = sd.getCardpool();

        if (sDeck.size() > 1) {

            for (int i = 0; i < sDeck.size(); i++) {
                deck.addSideboard(sDeck.get(i).getName() + "|" + sDeck.get(i).getCurSetCode());
            }

            for (int i = 0; i < Constant.Color.BasicLands.length; i++) {
                for (int j = 0; j < 18; j++) {
                    deck.addSideboard(Constant.Color.BasicLands[i] + "|" + sd.LandSetCode[0]);
                }
            }

            String sDeckName = JOptionPane.showInputDialog(null,
                    ForgeProps.getLocalized(NEW_GAME_TEXT.SAVE_SEALED_MSG),
                    ForgeProps.getLocalized(NEW_GAME_TEXT.SAVE_SEALED_TTL), JOptionPane.QUESTION_MESSAGE);
            deck.setName(sDeckName);
            deck.addMetaData("PlayerType", "Human");

            Constant.Runtime.HumanDeck[0] = deck;
            Constant.Runtime.GameType[0] = Constant.GameType.Sealed;

            Deck aiDeck = sd.buildAIDeck(sd.getCardpool());
            aiDeck.setName("AI_" + sDeckName);
            aiDeck.addMetaData("PlayerType", "AI");
            deckManager.addDeck(aiDeck);
            deckManager.writeAllDecks();
            deckManager.readAllDecks();
            updateDeckComboBoxes();

            deckEditorButtonActionPerformed(null);
            editor.customMenu.setCurrentGameType(Constant.GameType.Sealed);
            editor.customMenu.showSealedDeck(deck);

            Constant.Runtime.ComputerDeck[0] = aiDeck;
        } else {
            new OldGuiNewGame();
        }
    }

    /**
     * <p>
     * setupDraft.
     * </p>
     */
    private void setupDraft() {
        Gui_BoosterDraft draft = new Gui_BoosterDraft();

        // determine what kind of booster draft to run
        ArrayList<String> draftTypes = new ArrayList<String>();
        draftTypes.add("Full Cardpool");
        draftTypes.add("Block / Set");
        draftTypes.add("Custom");

        String prompt = "Choose Draft Format:";
        Object o = GuiUtils.getChoice(prompt, draftTypes.toArray());

        if (o.toString().equals(draftTypes.get(0))) {
            draft.showGui(new BoosterDraft_1("Full"));
        }

        else if (o.toString().equals(draftTypes.get(1))) {
            draft.showGui(new BoosterDraft_1("Block"));
        }

        else if (o.toString().equals(draftTypes.get(2))) {
            draft.showGui(new BoosterDraft_1("Custom"));
        }

    }

    /**
     * <p>
     * jbInit.
     * </p>
     *
     * @throws java.lang.Exception
     *             if any.
     */
    private void jbInit() throws Exception {

        titleLabel.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.NEW_GAME));
        titleLabel.setFont(new java.awt.Font("Dialog", 0, 26));

        /* CHOPPIC */
        titleLabel.setForeground(Color.WHITE);
        /* CHOPPIC */

        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        this.getContentPane().setLayout(new MigLayout("fill"));

        /*
         * Game Type Panel
         */

        /* jPanel2.setBorder(titledBorder1); */
        setCustomBorder(jPanel2, ForgeProps.getLocalized(NEW_GAME_TEXT.GAMETYPE));
        jPanel2.setLayout(new MigLayout("align center"));

        singleRadioButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.CONSTRUCTED_TEXT));
        singleRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                singleRadioButtonActionPerformed(e);
            }
        });

        // sealedRadioButton.setToolTipText("");
        sealedRadioButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.SEALED_TEXT));
        sealedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                sealedRadioButtonActionPerformed(e);
            }
        });

        // draftRadioButton.setToolTipText("");
        draftRadioButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.BOOSTER_TEXT));
        draftRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                draftRadioButtonActionPerformed(e);
            }
        });

        /*
         * Library Panel
         */

        /* jPanel1.setBorder(titledBorder2); */
        setCustomBorder(jPanel1, ForgeProps.getLocalized(NEW_GAME_TEXT.LIBRARY));
        jPanel1.setLayout(new MigLayout("align center"));

        jLabel2.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.YOURDECK));
        jLabel3.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.OPPONENT));

        humanComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                humanComboBoxActionPerformed(e);
            }
        });

        /*
         * Settings Panel
         */

        /* jPanel3.setBorder(titledBorder3); */
        setCustomBorder(jPanel3, ForgeProps.getLocalized(NEW_GAME_TEXT.SETTINGS));
        jPanel3.setLayout(new MigLayout("align center"));

        // newGuiCheckBox.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.NEW_GUI));
        smoothLandCheckBox.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.AI_LAND));

        devModeCheckBox.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.DEV_MODE));
        devModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Constant.Runtime.DevMode[0] = devModeCheckBox.isSelected();
                preferences.developerMode = Constant.Runtime.DevMode[0];
            }
        });

        upldDrftCheckBox.setText("Upload Draft Picks");

        upldDrftCheckBox.setToolTipText("Your picks and all other participants' picks will help the Forge AI"
                + " make better draft picks.");

        upldDrftCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Constant.Runtime.UpldDrft[0] = upldDrftCheckBox.isSelected();
                preferences.uploadDraftAI = Constant.Runtime.UpldDrft[0];
            }
        });

        foilRandomCheckBox.setText("Random Foiling");
        foilRandomCheckBox.setToolTipText("Approximately 1:20 cards will appear with foiling effects applied.");
        foilRandomCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Constant.Runtime.RndCFoil[0] = foilRandomCheckBox.isSelected();
                preferences.randCFoil = Constant.Runtime.RndCFoil[0];
            }
        });

        /*
         * Buttons
         */

        deckEditorButton.setToolTipText("");
        deckEditorButton.setFont(new java.awt.Font("Dialog", 0, 15));
        deckEditorButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.DECK_EDITOR));
        deckEditorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                deckEditorButtonActionPerformed(e);
            }
        });

        startButton.setFont(new java.awt.Font("Dialog", 0, 18));
        startButton.setHorizontalTextPosition(SwingConstants.LEADING);
        startButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.START_GAME));
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                startButtonActionPerformed(e);
            }
        });

        questButton.setFont(new java.awt.Font("Dialog", 0, 18));
        questButton.setText(ForgeProps.getLocalized(NEW_GAME_TEXT.QUEST_MODE));

        this.getContentPane().add(titleLabel, "align center, span 3, grow, wrap");

        this.getContentPane().add(jPanel2, "span 3, grow, wrap");
        jPanel2.add(singleRadioButton, "span 3, wrap");
        jPanel2.add(sealedRadioButton, "span 3, wrap");
        jPanel2.add(draftRadioButton, "span 3, wrap");
        updatePanelDisplay(jPanel2);

        this.getContentPane().add(jPanel1, "span 2, grow");
        jPanel1.add(jLabel2);
        jPanel1.add(humanComboBox, "sg combobox, wrap");
        jPanel1.add(jLabel3);
        jPanel1.add(computerComboBox, "sg combobox");
        updatePanelDisplay(jPanel1);

        this.getContentPane().add(deckEditorButton, "sg buttons, align 50% 50%, wrap");

        this.getContentPane().add(jPanel3, "span 2, grow");

        // jPanel3.add(newGuiCheckBox, "wrap");
        jPanel3.add(smoothLandCheckBox, "wrap");
        jPanel3.add(devModeCheckBox, "wrap");
        jPanel3.add(upldDrftCheckBox, "wrap");
        jPanel3.add(foilRandomCheckBox, "wrap");
        updatePanelDisplay(jPanel3);

        this.getContentPane().add(startButton, "sg buttons, align 50% 50%, split 2, flowy");
        this.getContentPane().add(questButton, "sg buttons, align 50% 50%");

        buttonGroup1.add(singleRadioButton);
        buttonGroup1.add(sealedRadioButton);
        buttonGroup1.add(draftRadioButton);

        /* CHOPPIC */
        /* Add background image */
        ((JPanel) getContentPane()).setOpaque(false);
        ImageIcon bkgd = new ImageIcon("res/images/ui/newgame_background.jpg");
        JLabel myLabel = new JLabel(bkgd);

        // Do not pass Integer.MIN_VALUE directly here; it must be packaged in an Integer
        // instance.  Otherwise, GUI components will not draw unless moused over.
        getLayeredPane().add(myLabel, Integer.valueOf(Integer.MIN_VALUE));

        myLabel.setBounds(0, 0, bkgd.getIconWidth(), bkgd.getIconHeight());
        /* CHOPPIC */

    }

    /* CHOPPIC */
    /* Update Panel Display */
    /**
     * <p>
     * updatePanelDisplay.
     * </p>
     *
     * @param panel
     *            a {@link javax.swing.JPanel} object.
     */
    final void updatePanelDisplay(final JPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JRadioButton) {
                ((JRadioButton) c).setOpaque(false);
            } else if (c instanceof JLabel) {
                ((JLabel) c).setOpaque(false);
            } else if (c instanceof JCheckBox) {
                ((JCheckBox) c).setOpaque(false);
            }
        }
        panel.setOpaque(false);
    }

    /**
     * <p>
     * setCustomBorder.
     * </p>
     *
     * @param panel
     *            a {@link javax.swing.JPanel} object.
     * @param title
     *            a {@link java.lang.String} object.
     */
    final void setCustomBorder(final JPanel panel, final String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title);
        tb.setTitlePosition(TitledBorder.ABOVE_TOP);
        tb.setTitleFont(new java.awt.Font("Dialog", 0, 12));
        tb.setTitleColor(Color.BLUE);
        panel.setBorder(tb);
    }

    /* CHOPPIC */

    /**
     * <p>
     * deckEditorButton_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    final void deckEditorButtonActionPerformed(final ActionEvent e) {
        if (editor == null) {

            editor = new Gui_DeckEditor();

            Command exit = new Command() {
                private static final long serialVersionUID = -9133358399503226853L;

                public void execute() {
                    new OldGuiNewGame();
                }
            };
            editor.show(exit);
            editor.setVisible(true);
        } // if

        // refresh decks:
        allDecks = getDecks();

        // TODO (TO have DOne) - this seems hacky. If someone knows how to do
        // this for real, feel free.
        // This make it so the second time you open the Deck Editor, typing a
        // card name and pressing enter will filter
        javax.swing.JRootPane rootPane = editor.getRootPane();
        rootPane.setDefaultButton(editor.filterButton);

        editor.setVisible(true);

        dispose();
    }

    /**
     * <p>
     * getRandomDeck.
     * </p>
     *
     * @param d
     *            an array of {@link forge.deck.Deck} objects.
     * @return a {@link forge.deck.Deck} object.
     */
    final Deck getRandomDeck(final Deck[] d) {
        // get a random number between 0 and d.length
        // int i = (int) (Math.random() * d.length);
        Random r = MyRandom.random;

        return d[r.nextInt(d.length)];
    }

    /**
     * <p>
     * startButton_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    final void startButtonActionPerformed(final ActionEvent e) {
        if (humanComboBox.getSelectedItem() == null || computerComboBox.getSelectedItem() == null) {
            return;
        }

        String human = humanComboBox.getSelectedItem().toString();

        String computer = null;
        if (computerComboBox.getSelectedItem() != null) {
            computer = computerComboBox.getSelectedItem().toString();
        }

        if (draftRadioButton.isSelected()) {
            if (human.equals("New Draft")) {
                dispose();

                setupDraft();

                return;

            } else {
                // load old draft
                Deck[] deck = deckManager.getDraftDeck(human);
                int index = Integer.parseInt(computer);

                Constant.Runtime.HumanDeck[0] = deck[0];
                Constant.Runtime.ComputerDeck[0] = deck[index];

                if (Constant.Runtime.ComputerDeck[0] == null) {
                    throw new IllegalStateException("OldGuiNewGame : startButton() error - computer deck is null");
                }
            } // else - load old draft
        } // if
        else if (sealedRadioButton.isSelected()) {
            if (human.equals("New Sealed")) {
                dispose();

                setupSealed();

                return;
            } else {
                Constant.Runtime.HumanDeck[0] = deckManager.getDeck(human);

            }

            if (!computer.equals("New Sealed")) {
                Constant.Runtime.ComputerDeck[0] = deckManager.getDeck(computer);
            }
        } else {
            // non-draft decks
            String format = Constant.Runtime.GameType[0];
            // boolean sealed = Constant.GameType.Sealed.equals(format);
            boolean constructed = Constant.GameType.Constructed.equals(format);

            boolean humanGenerate = human.equals("Generate Deck");
            boolean humanRandom = human.equals("Random");

            if (humanGenerate) {
                if (constructed) {
                    genDecks("H");
                }
                // else if(sealed)
                // Constant.Runtime.HumanDeck[0] = generateSealedDeck();
            } else if (humanRandom) {
                Constant.Runtime.HumanDeck[0] = getRandomDeck(getDecks(format));

                JOptionPane.showMessageDialog(null,
                        String.format("You are using deck: %s", Constant.Runtime.HumanDeck[0].getName()), "Deck Name",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                Constant.Runtime.HumanDeck[0] = deckManager.getDeck(human);
            }

            assert computer != null;
            boolean computerGenerate = computer.equals("Generate Deck");
            boolean computerRandom = computer.equals("Random");

            if (computerGenerate) {
                if (constructed) {
                    genDecks("C");
                } // Constant.Runtime.ComputerDeck[0] =
                  // generateConstructedDeck();
                  // else if(sealed)
                  // Constant.Runtime.ComputerDeck[0] = generateSealedDeck();
            } else if (computerRandom) {
                Constant.Runtime.ComputerDeck[0] = getRandomDeck(getDecks(format));

                JOptionPane.showMessageDialog(null,
                        String.format("The computer is using deck: %s", Constant.Runtime.ComputerDeck[0].getName()),
                        "Deck Name", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Constant.Runtime.ComputerDeck[0] = deckManager.getDeck(computer);
            }
        } // else

        // DO NOT CHANGE THIS ORDER, GuiDisplay needs to be created before cards
        // are added
        // Constant.Runtime.DevMode[0] = devModeCheckBox.isSelected();

        // if (newGuiCheckBox.isSelected())
        AllZone.setDisplay(new GuiDisplay4());
        // else AllZone.setDisplay(new GuiDisplay3());

        Constant.Runtime.Smooth[0] = smoothLandCheckBox.isSelected();

        AllZone.getGameAction().newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0]);
        AllZone.getDisplay().setVisible(true);

        dispose();
    } // startButton_actionPerformed()

    /*
     * private Deck generateSealedDeck() { GenerateSealedDeck gen = new
     * GenerateSealedDeck(); CardList name = gen.generateDeck(); Deck deck = new
     * Deck(Constant.GameType.Sealed);
     *
     * for(int i = 0; i < 40; i++) deck.addMain(name.get(i).getName()); return
     * deck; }
     */
    /**
     * <p>
     * genDecks.
     * </p>
     *
     * @param p
     *            a {@link java.lang.String} object.
     */
    private void genDecks(final String p) {
        Deck d = null;

        ArrayList<String> decks = new ArrayList<String>();
        decks.add("2-Color Deck (original)");
        decks.add("3-Color Deck (original)");
        decks.add("5-Color Deck (original)");
        decks.add("Semi-Random Theme Deck");
        decks.add("2-Color Deck (new)");
        decks.add("3-Color Deck (new)");

        String prompt = "Generate ";
        if (p.equals("H")) {
            prompt += "Human ";
        } else {
            prompt += "Computer ";
        }
        prompt += "Deck";

        Object o = GuiUtils.getChoice(prompt, decks.toArray());

        if (o.toString().equals(decks.get(0))) {
            d = generateConstructedDeck();
        } else if (o.toString().equals(decks.get(1))) {
            d = generateConstructed3ColorDeck();
        } else if (o.toString().equals(decks.get(2))) {
            d = generateConstructed5ColorDeck();
        } else if (o.toString().equals(decks.get(3))) {
            d = generateConstructedThemeDeck();
        } else if (o.toString().equals(decks.get(4))) {
            d = generate2ColorDeck(p);
        } else if (o.toString().equals(decks.get(5))) {
            d = generate3ColorDeck(p);
        }

        if (p.equals("H")) {
            Constant.Runtime.HumanDeck[0] = d;
        } else if (p.equals("C")) {
            Constant.Runtime.ComputerDeck[0] = d;
        }
    }

    /**
     * <p>
     * generateConstructedDeck.
     * </p>
     *
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generateConstructedDeck() {
        GenerateConstructedDeck gen = new GenerateConstructedDeck();
        CardList name = gen.generateDeck();
        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructed3ColorDeck.
     * </p>
     *
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generateConstructed3ColorDeck() {
        GenerateConstructedMultiColorDeck gen = new GenerateConstructedMultiColorDeck();
        CardList name = gen.generate3ColorDeck();
        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructed5ColorDeck.
     * </p>
     *
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generateConstructed5ColorDeck() {
        GenerateConstructedMultiColorDeck gen = new GenerateConstructedMultiColorDeck();
        CardList name = gen.generate5ColorDeck();
        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructedThemeDeck.
     * </p>
     *
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generateConstructedThemeDeck() {
        GenerateThemeDeck gen = new GenerateThemeDeck();
        ArrayList<String> tNames = gen.getThemeNames();
        tNames.add(0, "Random");
        Object o = GuiUtils.getChoice("Select a theme.", tNames.toArray());

        String stDeck;
        if (o.toString().equals("Random")) {
            Random r = MyRandom.random;
            stDeck = tNames.get(r.nextInt(tNames.size() - 1) + 1);
        } else {
            stDeck = o.toString();
        }

        CardList td = gen.getThemeDeck(stDeck, 60);
        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < td.size(); i++) {
            deck.addMain(td.get(i).getName());
        }

        return deck;
    }

    /**
     * <p>
     * generate2ColorDeck.
     * </p>
     *
     * @param p
     *            a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generate2ColorDeck(final String p) {
        Random r = MyRandom.random;

        ArrayList<String> colors = new ArrayList<String>();
        colors.add("Random");
        colors.add("white");
        colors.add("blue");
        colors.add("black");
        colors.add("red");
        colors.add("green");

        String c1;
        String c2;
        if (p.equals("H")) {
            c1 = GuiUtils.getChoice("Select first color.", colors.toArray()).toString();

            if (c1.equals("Random")) {
                c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c1);

            c2 = GuiUtils.getChoice("Select second color.", colors.toArray()).toString();

            if (c2.equals("Random")) {
                c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }
        } else {
            // if (p.equals("C"))
            c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c1);
            c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
        }
        Generate2ColorDeck gen = new Generate2ColorDeck(c1, c2);
        CardList d = gen.get2ColorDeck(60);

        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < d.size(); i++) {
            deck.addMain(d.get(i).getName());
        }

        return deck;

    }

    /**
     * <p>
     * generate3ColorDeck.
     * </p>
     *
     * @param p
     *            a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck generate3ColorDeck(final String p) {
        Random r = MyRandom.random;

        ArrayList<String> colors = new ArrayList<String>();
        colors.add("Random");
        colors.add("white");
        colors.add("blue");
        colors.add("black");
        colors.add("red");
        colors.add("green");

        String c1;
        String c2;
        String c3;
        if (p.equals("H")) {
            c1 = GuiUtils.getChoice("Select first color.", colors.toArray()).toString();

            if (c1.equals("Random")) {
                c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c1);

            c2 = GuiUtils.getChoice("Select second color.", colors.toArray()).toString();

            if (c2.equals("Random")) {
                c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c2);

            c3 = GuiUtils.getChoice("Select third color.", colors.toArray()).toString();
            if (c3.equals("Random")) {
                c3 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

        } else {
            // if (p.equals("C"))
            c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c1);
            c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c2);
            c3 = colors.get(r.nextInt(colors.size() - 1) + 1);
        }
        Generate3ColorDeck gen = new Generate3ColorDeck(c1, c2, c3);
        CardList d = gen.get3ColorDeck(60);

        Deck deck = new Deck(Constant.GameType.Constructed);

        for (int i = 0; i < d.size(); i++) {
            deck.addMain(d.get(i).getName());
        }

        return deck;

    }

    /**
     * <p>
     * singleRadioButton_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    final void singleRadioButtonActionPerformed(final ActionEvent e) {
        Constant.Runtime.GameType[0] = Constant.GameType.Constructed;
        updateDeckComboBoxes();
    }

    /**
     * <p>
     * sealedRadioButton_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    final void sealedRadioButtonActionPerformed(final ActionEvent e) {
        Constant.Runtime.GameType[0] = Constant.GameType.Sealed;
        updateDeckComboBoxes();
    }

    /**
     * <p>
     * updateDeckComboBoxes.
     * </p>
     */
    private void updateDeckComboBoxes() {
        humanComboBox.removeAllItems();
        computerComboBox.removeAllItems();

        if (Constant.GameType.Sealed.equals(Constant.Runtime.GameType[0])) {
            humanComboBox.addItem("New Sealed");
            computerComboBox.addItem("New Sealed");

            for (Deck allDeck : allDecks) {
                if (allDeck.getDeckType().equals(Constant.GameType.Sealed)) {
                    if (allDeck.getMetadata("PlayerType").equals("Human")) {
                        humanComboBox.addItem(allDeck.getName());
                    } else if (allDeck.getMetadata("PlayerType").equals("AI")) {
                        computerComboBox.addItem(allDeck.getName());
                    }
                }
            } // for
        } else if (Constant.GameType.Constructed.equals(Constant.Runtime.GameType[0])) {
            humanComboBox.addItem("Generate Deck");
            computerComboBox.addItem("Generate Deck");

            humanComboBox.addItem("Random");
            computerComboBox.addItem("Random");

            for (Deck allDeck : allDecks) {
                if (allDeck.getDeckType().equals(Constant.GameType.Constructed)) {
                    humanComboBox.addItem(allDeck.getName());
                    computerComboBox.addItem(allDeck.getName());
                }
            } // for
        }
        // not sure if the code below is useful or not
        // this will select the deck that you previously used

        // if(Constant.Runtime.HumanDeck[0] != null)
        // humanComboBox.setSelectedItem(Constant.Runtime.HumanDeck[0].getName());

    } /* updateComboBoxes() */

    /**
     * <p>
     * getDecks.
     * </p>
     *
     * @param gameType
     *            a {@link java.lang.String} object.
     * @return an array of {@link forge.deck.Deck} objects.
     */
    final Deck[] getDecks(final String gameType) {
        ArrayList<Deck> list = new ArrayList<Deck>();

        Deck d;
        for (Deck allDeck : allDecks) {
            d = allDeck;

            if (d.getDeckType().equals(gameType)) {
                list.add(d);
            }
        } // for

        // convert ArrayList to Deck[]
        Deck[] out = new Deck[list.size()];
        list.toArray(out);

        return out;
    } // getDecks()

    /**
     * <p>
     * draftRadioButton_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    final void draftRadioButtonActionPerformed(final ActionEvent e) {
        Constant.Runtime.GameType[0] = Constant.GameType.Draft;
        humanComboBox.removeAllItems();
        computerComboBox.removeAllItems();

        humanComboBox.addItem("New Draft");
        Object[] key = deckManager.getDraftDecks().keySet().toArray();
        Arrays.sort(key);

        for (Object aKey : key) {
            humanComboBox.addItem(aKey);
        }

        for (int i = 0; i < 7; i++) {
            computerComboBox.addItem("" + (i + 1));
        }
    }

    /**
     * <p>
     * humanComboBox_actionPerformed.
     * </p>
     *
     * @param e
     *            a {@link java.awt.event.ActionEvent} object.
     */
    void humanComboBoxActionPerformed(final ActionEvent e) {

    } /* draftRadioButton_actionPerformed() */

    /**
     *
     * @author dhudson
     *
     */
    public static class LookAndFeelAction extends AbstractAction {

        private static final long serialVersionUID = -4447498333866711215L;
        private Component c;

        /**
         *
         * @param component
         */
        public LookAndFeelAction(final Component component) {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.LF));
            this.c = component;
        }

        /**
         * @param e
         */
        public final void actionPerformed(final ActionEvent e) {
            LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
            HashMap<String, String> LAFMap = new HashMap<String, String>();
            for (LookAndFeelInfo anInfo : info) {
                LAFMap.put(anInfo.getName(), anInfo.getClassName());
            }

            // add Substance LAFs:
            LAFMap.put("Autumn", "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
            LAFMap.put("Business", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
            LAFMap.put("Business Black Steel",
                    "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
            LAFMap.put("Business Blue Steel",
                    "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
            LAFMap.put("Challenger Deep", "org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel");
            LAFMap.put("Creme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
            LAFMap.put("Creme Coffee", "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
            LAFMap.put("Dust", "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
            LAFMap.put("Dust Coffee", "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
            LAFMap.put("Emerald Dusk", "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel");
            LAFMap.put("Gemini", "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
            LAFMap.put("Graphite", "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
            LAFMap.put("Graphite Aqua", "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel");
            LAFMap.put("Graphite Glass", "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel");
            LAFMap.put("Magma", "org.pushingpixels.substance.api.skin.SubstanceMagmaLookAndFeel");
            LAFMap.put("Magellan", "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel");
            // LAFMap.put("Mariner",
            // "org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
            LAFMap.put("Mist Aqua", "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
            LAFMap.put("Mist Silver", "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
            LAFMap.put("Moderate", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
            LAFMap.put("Nebula", "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
            LAFMap.put("Nebula Brick Wall", "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
            // LAFMap.put("Office Black 2007",
            // "org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel");
            LAFMap.put("Office Blue 2007", "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel");
            LAFMap.put("Office Silver 2007",
                    "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
            LAFMap.put("Raven", "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel");
            LAFMap.put("Raven Graphite", "org.pushingpixels.substance.api.skin.SubstanceRavenGraphiteLookAndFeel");
            //LAFMap.put("Raven Graphite Glass",
            //"org.pushingpixels.substance.api.skin.SubstanceRavenGraphiteGlassLookAndFeel");
            LAFMap.put("Sahara", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
            LAFMap.put("Twilight", "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel");

            String[] keys = new String[LAFMap.size()];
            int count = 0;

            for (String s1 : LAFMap.keySet()) {
                keys[count++] = s1;
            }
            Arrays.sort(keys);

            ListChooser<String> ch = new ListChooser<String>("Choose one", 0, 1, keys);
            if (ch.show()) {
                try {
                    String name = ch.getSelectedValue();
                    int index = ch.getSelectedIndex();
                    if (index == -1) {
                        return;
                    }
                    // UIManager.setLookAndFeel(info[index].getClassName());
                    preferences.laf = LAFMap.get(name);
                    UIManager.setLookAndFeel(LAFMap.get(name));

                    SwingUtilities.updateComponentTreeUI(c);
                } catch (Exception ex) {
                    ErrorViewer.showError(ex);
                }
            }
        }
    }

/**
 *
 * @author dhudson
 *
 */
    public static class DownloadPriceAction extends AbstractAction {
        private static final long serialVersionUID = 929877827872974298L;

        /**
         *
         */
        public DownloadPriceAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.DOWNLOADPRICE));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            Gui_DownloadPrices gdp = new Gui_DownloadPrices();
            gdp.setVisible(true);
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class BugzReporterAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 6354047838575733085L;

        /**
         *
         */
        public BugzReporterAction() {
            super("Report Bug");
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            BugzReporter br = new BugzReporter();
            br.setVisible(true);
        }


    }

    /*
     * public static class DownloadAction extends AbstractAction {
     *
     * private static final long serialVersionUID = 6564425021778307101L;
     *
     * public DownloadAction() {
     * super(ForgeProps.getLocalized(MENU_BAR.MENU.DOWNLOAD)); }
     *
     * public void actionPerformed(ActionEvent e) {
     *
     * Gui_DownloadPictures.startDownload(null); } }
     */
    /**
     *
     * @author dhudson
     *
     */
    public static class DownloadActionLQ extends AbstractAction {

        private static final long serialVersionUID = -6234380664413874813L;

        /**
         *
         */
        public DownloadActionLQ() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.DOWNLOADLQ));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            Gui_DownloadPictures_LQ.startDownload(null);
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class DownloadActionSetLQ extends AbstractAction {
        private static final long serialVersionUID = 2947202546752930L;

        /**
         *
         */
        public DownloadActionSetLQ() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.DOWNLOADSETLQ));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            Gui_DownloadSetPictures_LQ.startDownload(null);
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class ImportPictureAction extends AbstractAction {

        private static final long serialVersionUID = 6893292814498031508L;

        /**
         *
         */
        public ImportPictureAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.IMPORTPICTURE));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            GUI_ImportPicture ip = new GUI_ImportPicture(null);
            ip.setVisible(true);
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class CardSizesAction extends AbstractAction {

        private static final long serialVersionUID = -2900235618450319571L;
        private static String[] keys = {"Tiny", "Smaller", "Small", "Medium", "Large", "Huge"};
        private static int[] widths = {36, 42, 63, 70, 93, 120};
        private static int[] heights = {50, 59, 88, 98, 130, 168};

        /**
         *
         */
        public CardSizesAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.CARD_SIZES));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            ListChooser<String> ch = new ListChooser<String>("Choose one", "Choose a new card size", 0, 1, keys);
            if (ch.show()) {
                try {
                    int index = ch.getSelectedIndex();
                    if (index == -1) {
                        return;
                    }
                    set(index);
                } catch (Exception ex) {
                    ErrorViewer.showError(ex);
                }
            }
        }

        /**
         *
         * @param index
         */
        public static void set(final int index) {
            preferences.cardSize = CardSizeType.valueOf(keys[index].toLowerCase());
            Constant.Runtime.width[0] = widths[index];
            Constant.Runtime.height[0] = heights[index];
        }

        /**
         *
         * @param s
         */
        public static void set(final CardSizeType s) {
            preferences.cardSize = s;
            int index = 0;
            for (String str : keys) {
                if (str.toLowerCase().equals(s.toString())) {
                    break;
                }
                index++;
            }
            Constant.Runtime.width[0] = widths[index];
            Constant.Runtime.height[0] = heights[index];
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class CardStackAction extends AbstractAction {

        private static final long serialVersionUID = -3770527681359311455L;
        private static String[] keys = {"3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        private static int[] values = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        /**
         *
         */
        public CardStackAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.CARD_STACK));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {

            ListChooser<String> ch = new ListChooser<String>("Choose one", "Choose the max size of a stack", 0, 1,
                    keys);

            if (ch.show()) {
                try {
                    int index = ch.getSelectedIndex();
                    if (index == -1) {
                        return;
                    }
                    set(index);

                } catch (Exception ex) {
                    ErrorViewer.showError(ex);
                }
            }
        }

        /**
         *
         * @param index
         */
        public static void set(final int index) {
            preferences.maxStackSize = values[index];
            Constant.Runtime.stackSize[0] = values[index];
        }

        /**
         *
         * @param val
         */
        public static void setVal(final int val) {
            preferences.maxStackSize = val;
            Constant.Runtime.stackSize[0] = val;
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class CardStackOffsetAction extends AbstractAction {

        private static final long serialVersionUID = 5021304777748833975L;
        private static String[] keys = {"Tiny", "Small", "Medium", "Large"};
        private static int[] offsets = {5, 7, 10, 15};

        /**
         *
         */
        public CardStackOffsetAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.CARD_STACK_OFFSET));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            ListChooser<String> ch = new ListChooser<String>("Choose one", "Choose a stack offset value", 0, 1, keys);
            if (ch.show()) {
                try {
                    int index = ch.getSelectedIndex();
                    if (index == -1) {
                        return;
                    }
                    set(index);

                } catch (Exception ex) {
                    ErrorViewer.showError(ex);
                }
            }
        }

        /**
         *
         * @param index
         */
        public static void set(final int index) {
            preferences.stackOffset = StackOffsetType.valueOf(keys[index].toLowerCase());
            Constant.Runtime.stackOffset[0] = offsets[index];
        }

        /**
         *
         * @param s
         */
        public static void set(final StackOffsetType s) {
            preferences.stackOffset = s;
            int index = 0;
            for (String str : keys) {
                if (str.toLowerCase().equals(s.toString())) {
                    break;
                }
                index++;
            }
            Constant.Runtime.stackOffset[0] = offsets[index];
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class HowToPlayAction extends AbstractAction {

        private static final long serialVersionUID = 5552000208438248428L;

        /**
         *
         */
        public HowToPlayAction() {
            super(ForgeProps.getLocalized(LANG.HowTo.TITLE));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            String text = ForgeProps.getLocalized(LANG.HowTo.MESSAGE);

            JTextArea area = new JTextArea(text, 25, 40);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setEditable(false);
            area.setOpaque(false);

            JOptionPane.showMessageDialog(null, new JScrollPane(area), ForgeProps.getLocalized(LANG.HowTo.TITLE),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     *
     * @author dhudson
     *
     */
    public static class AboutAction extends AbstractAction {

        private static final long serialVersionUID = 5492173304463396871L;

        /**
         *
         */
        public AboutAction() {
            super(ForgeProps.getLocalized(MENU_BAR.MENU.ABOUT));
        }

        /**
         *
         */
        public final void actionPerformed(final ActionEvent e) {
            JTextArea area = new JTextArea(12, 25);

            if (useLAFFonts.isSelected()) {
                Font f = new Font(area.getFont().getName(), Font.PLAIN, 13);
                area.setFont(f);
            }

            area.setText("The various people who have contributed to this project apologize with deep remorse"
                    + " for any bugs that you may have noticed.\n\nThe development team.\n\nOriginal author: Forge\n\n"
                    + "(Quest icons used created by Teekatas, from his Legendora set:\n"
                    + " http://raindropmemory.deviantart.com)");

            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setEditable(false);

            JPanel p = new JPanel();
            area.setBackground(p.getBackground());

            JOptionPane.showMessageDialog(null, area, "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * <p>
     * exit.
     * </p>
     *
     * @return a boolean.
     */
    public final boolean exit() {
        try {
            preferences.laf = UIManager.getLookAndFeel().getClass().getName();
            preferences.lafFonts = useLAFFonts.isSelected();
            // preferences.newGui = newGuiCheckBox.isSelected();
            preferences.stackAiLand = smoothLandCheckBox.isSelected();
            preferences.millingLossCondition = Constant.Runtime.Mill[0];
            preferences.developerMode = Constant.Runtime.DevMode[0];
            preferences.cardOverlay = cardOverlay.isSelected();
            preferences.scaleLargerThanOriginal = ImageCache.scaleLargerThanOriginal;
            preferences.uploadDraftAI = Constant.Runtime.UpldDrft[0];
            preferences.save();
        } catch (Exception ex) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Preferences could not be saved. Continue to close without saving ?", "Confirm Exit",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return false;
            }
        }

        setVisible(false);
        dispose();
        return true;
    }

    /** {@inheritDoc} */
    protected final void processWindowEvent(final WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING) {
            if (!exit()) {
                return;
            }
        }
        super.processWindowEvent(event);
    }

    /* CHOPPIC */
    /* Panel with rounded border and semi-transparent background */
    private static class CustomPanel extends JPanel {
        private static final long serialVersionUID = 774205995101881824L;
        private final int radius;

        CustomPanel(final int neoRadius) {
            this.radius = neoRadius;
        }

        /**
         *
         */
        public void paintComponent(final Graphics g) {
            Color bg = getBackground();
            g.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 180));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(new Color(0, 0, 0, 70));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    /**
     *
     */
    public static void loadDynamicGamedata() {
        if (!Constant.CardTypes.loaded[0]) {
            ArrayList<String> typeListFile = FileUtil.readFile("res/gamedata/TypeLists.txt");

            ArrayList<String> tList = null;

            Constant.CardTypes.cardTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.superTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.basicTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.landTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.creatureTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.instantTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.sorceryTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.enchantmentTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.artifactTypes[0] = new Constant_StringArrayList();
            Constant.CardTypes.walkerTypes[0] = new Constant_StringArrayList();

            if (typeListFile.size() > 0) {
                for (int i = 0; i < typeListFile.size(); i++) {
                    String s = typeListFile.get(i);

                    if (s.equals("[CardTypes]")) {
                        tList = Constant.CardTypes.cardTypes[0].list;
                    }

                    else if (s.equals("[SuperTypes]")) {
                        tList = Constant.CardTypes.superTypes[0].list;
                    }

                    else if (s.equals("[BasicTypes]")) {
                        tList = Constant.CardTypes.basicTypes[0].list;
                    }

                    else if (s.equals("[LandTypes]")) {
                        tList = Constant.CardTypes.landTypes[0].list;
                    }

                    else if (s.equals("[CreatureTypes]")) {
                        tList = Constant.CardTypes.creatureTypes[0].list;
                    }

                    else if (s.equals("[InstantTypes]")) {
                        tList = Constant.CardTypes.instantTypes[0].list;
                    }

                    else if (s.equals("[SorceryTypes]")) {
                        tList = Constant.CardTypes.sorceryTypes[0].list;
                    }

                    else if (s.equals("[EnchantmentTypes]")) {
                        tList = Constant.CardTypes.enchantmentTypes[0].list;
                    }

                    else if (s.equals("[ArtifactTypes]")) {
                        tList = Constant.CardTypes.artifactTypes[0].list;
                    }

                    else if (s.equals("[WalkerTypes]")) {
                        tList = Constant.CardTypes.walkerTypes[0].list;
                    }

                    else if (s.length() > 1) {
                        tList.add(s);
                    }
                }
            }
            Constant.CardTypes.loaded[0] = true;
            /*
             * if (Constant.Runtime.DevMode[0]) {
             * System.out.println(Constant.CardTypes.cardTypes[0].list);
             * System.out.println(Constant.CardTypes.superTypes[0].list);
             * System.out.println(Constant.CardTypes.basicTypes[0].list);
             * System.out.println(Constant.CardTypes.landTypes[0].list);
             * System.out.println(Constant.CardTypes.creatureTypes[0].list);
             * System.out.println(Constant.CardTypes.instantTypes[0].list);
             * System.out.println(Constant.CardTypes.sorceryTypes[0].list);
             * System.out.println(Constant.CardTypes.enchantmentTypes[0].list);
             * System.out.println(Constant.CardTypes.artifactTypes[0].list);
             * System.out.println(Constant.CardTypes.walkerTypes[0].list); }
             */
        }

        if (!Constant.Keywords.loaded[0]) {
            ArrayList<String> nskwListFile = FileUtil.readFile("res/gamedata/NonStackingKWList.txt");

            Constant.Keywords.NonStackingList[0] = new Constant_StringArrayList();

            if (nskwListFile.size() > 1) {
                for (int i = 0; i < nskwListFile.size(); i++) {
                    String s = nskwListFile.get(i);
                    if (s.length() > 1) {
                        Constant.Keywords.NonStackingList[0].list.add(s);
                    }
                }
            }
            Constant.Keywords.loaded[0] = true;
            if (Constant.Runtime.DevMode[0]) {
                System.out.println(Constant.Keywords.NonStackingList[0].list);
            }
        }

        /*
         * if (!Constant.Color.loaded[0]) { ArrayList<String> lcListFile =
         * FileUtil.readFile("res/gamedata/LandColorList");
         *
         * if (lcListFile.size() > 1) { for (int i=0; i<lcListFile.size(); i++)
         * { String s = lcListFile.get(i); if (s.length() > 1)
         * Constant.Color.LandColor[0].map.add(s); } }
         * Constant.Keywords.loaded[0] = true; if (Constant.Runtime.DevMode[0])
         * { System.out.println(Constant.Keywords.NonStackingList[0].list); } }
         */
    }

}
