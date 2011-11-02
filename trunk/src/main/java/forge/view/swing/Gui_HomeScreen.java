package forge.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;

import net.slightlymagic.braids.util.UtilFunctions;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import forge.AllZone;
import forge.Command;
import forge.Constant;
import forge.GuiImportPicture;
import forge.GuiDisplay;
import forge.GuiDownloadPrices;
import forge.GuiDownloadSetPicturesLQ;
import forge.MyRandom;
import forge.PlayerType;
import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckGeneration;
import forge.deck.DeckManager;
import forge.error.BugzReporter;
import forge.error.ErrorViewer;
import forge.game.GameType;
import forge.game.limited.BoosterDraft_1;
import forge.game.limited.CardPoolLimitation;
import forge.game.limited.SealedDeck;
import forge.gui.GuiUtils;
import forge.gui.ListChooser;
import forge.gui.deckeditor.DeckEditorCommon;
import forge.gui.deckeditor.DeckEditorDraft;
import forge.item.CardPrinted;
import forge.item.ItemPool;
import forge.properties.ForgePreferences.CardSizeType;
import forge.properties.ForgePreferences.StackOffsetType;
import forge.properties.ForgeProps;
import forge.properties.NewConstants.Lang;
import forge.properties.NewConstants.Lang.OldGuiNewGame.NewGameText;
import forge.quest.gui.QuestOptions;

/**
 * The Class Gui_HomeScreen.
 */
public class Gui_HomeScreen {
    // Hack... WindowBuilder can't deal with path relative to the project folder
    // like "res/"
    // So... use a full path when debugging or designing with WindowBuilder
    // private String HomeScreenPath =
    // "/home/rob/ForgeSVN/ForgeSVN/res/images/ui/HomeScreen/";
    // And switch to relative path for distribution
    private final String homeScreenPath = "res/images/ui/HomeScreen/";

    private JFrame gHS;

    private final JLabel lblBackground = new JLabel();
    private final ImageIcon imgBackground = new ImageIcon(this.homeScreenPath + "default_600/Main.jpg");

    // Interactive Elements
    private final JLabel lblGameMode = new JLabel();
    private final ImageIcon imgMode = new ImageIcon(this.homeScreenPath + "default_600/btnMode_title.png");

    private final JButton cmdConstructed = new JButton();
    private final ImageIcon imgConstructedUp = new ImageIcon(this.homeScreenPath + "default_600/btnMode_constrUp.png");
    private final ImageIcon imgConstructedOver = new ImageIcon(this.homeScreenPath
            + "default_600/btnMode_constrOver.png");
    private final ImageIcon imgConstructedDown = new ImageIcon(this.homeScreenPath
            + "default_600/btnMode_constrDown.png");
    private final ImageIcon imgConstructedSel = new ImageIcon(this.homeScreenPath
            + "default_600/btnMode_constrToggle2.png");

    private final JButton cmdSealed = new JButton();
    private final ImageIcon imgSealedUp = new ImageIcon(this.homeScreenPath + "default_600/btnMode_sealedUp.png");
    private final ImageIcon imgSealedOver = new ImageIcon(this.homeScreenPath + "default_600/btnMode_sealedOver.png");
    private final ImageIcon imgSealedDown = new ImageIcon(this.homeScreenPath + "default_600/btnMode_sealedDown.png");
    private final ImageIcon imgSealedSel = new ImageIcon(this.homeScreenPath + "default_600/btnMode_sealedToggle2.png");

    private final JButton cmdDraft = new JButton();
    private final ImageIcon imgDraftUp = new ImageIcon(this.homeScreenPath + "default_600/btnMode_draftUp.png");
    private final ImageIcon imgDraftOver = new ImageIcon(this.homeScreenPath + "default_600/btnMode_draftOver.png");
    private final ImageIcon imgDraftDown = new ImageIcon(this.homeScreenPath + "default_600/btnMode_draftDown.png");
    private final ImageIcon imgDraftSel = new ImageIcon(this.homeScreenPath + "default_600/btnMode_draftToggle2.png");

    private final JButton cmdQuest = new JButton();
    private final ImageIcon imgQuestUp = new ImageIcon(this.homeScreenPath + "default_600/btnMode_questUp.png");
    private final ImageIcon imgQuestOver = new ImageIcon(this.homeScreenPath + "default_600/btnMode_questOver.png");
    private final ImageIcon imgQuestDown = new ImageIcon(this.homeScreenPath + "default_600/btnMode_questDown.png");
    private final ImageIcon imgQuestSel = new ImageIcon(this.homeScreenPath + "default_600/btnMode_questToggle2.png");

    private final JLabel lblLibrary = new JLabel();
    private final ImageIcon imgLibrary = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_title.png");

    private JButton cmdHumanDeck;
    private final ImageIcon imgHumanUp = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_humanUp.png");
    private final ImageIcon imgHumanOver = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_humanOver.png");
    private final ImageIcon imgHumanDown = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_humanDown.png");
    private final ImageIcon imgHumanSel = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_humanToggle2.png");

    private JButton cmdAIDeck;
    private final ImageIcon imgAIUp = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_aiUp.png");
    private final ImageIcon imgAIOver = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_aiOver.png");
    private final ImageIcon imgAIDown = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_aiDown.png");
    private final ImageIcon imgAISel = new ImageIcon(this.homeScreenPath + "default_600/btnLibr_aiToggle2.png");

    private final JButton cmdDeckEditor = new JButton();
    private final ImageIcon imgEditorUp = new ImageIcon(this.homeScreenPath + "default_600/btnDeck_editorUp.png");
    private final ImageIcon imgEditorOver = new ImageIcon(this.homeScreenPath + "default_600/btnDeck_editorOver.png");
    private final ImageIcon imgEditorDown = new ImageIcon(this.homeScreenPath + "default_600/btnDeck_editorDown.png");

    private final JButton cmdStart = new JButton();
    private final ImageIcon imgStartUp = new ImageIcon(this.homeScreenPath + "default_600/btnStart_Up.png");
    private final ImageIcon imgStartOver = new ImageIcon(this.homeScreenPath + "default_600/btnStart_Over.png");
    private final ImageIcon imgStartDown = new ImageIcon(this.homeScreenPath + "default_600/btnStart_Down.png");

    private final JButton cmdSettings = new JButton();
    private final ImageIcon imgSettingsUp = new ImageIcon(this.homeScreenPath
            + "default_600/btnSettings_unselected.png");
    private final ImageIcon imgSettingsOver = new ImageIcon(this.homeScreenPath + "default_600/btnSettings_hover.png");
    private final ImageIcon imgSettingsDown = new ImageIcon(this.homeScreenPath
            + "default_600/btnSettings_selected.png");

    private final JButton cmdUtilities = new JButton("");
    private final ImageIcon imgUtilitiesUp = new ImageIcon(this.homeScreenPath + "default_600/btnUtils_unselected.png");
    private final ImageIcon imgUtilitiesOver = new ImageIcon(this.homeScreenPath + "default_600/btnUtils_hover.png");
    private final ImageIcon imgUtilitiesDown = new ImageIcon(this.homeScreenPath + "default_600/btnUtils_selected.png");

    // Intro Panel
    private final JPanel pnlIntro = new JPanel();
    private final JLabel lblIntro = new JLabel();

    // Deck Panel
    private final JPanel pnlDecks = new JPanel();
    private final JLabel lblDecksHeader = new JLabel();
    private final JList lstDecks = new JList();
    private final JScrollPane scrDecks = new JScrollPane();
    private final JButton cmdDeckSelect = new JButton("Select Deck");

    // Settings Panel
    private final JPanel pnlSettings = new JPanel();
    private final JScrollPane scrSettings = new JScrollPane();
    private final JPanel pnlSettingsA = new JPanel();
    private final JCheckBox chkStackAiLand = new JCheckBox("Stack AI Land");
    private final JCheckBox chkUploadDraftData = new JCheckBox("Upload Draft Data");
    private final JCheckBox chkDeveloperMode = new JCheckBox("Developer Mode");
    private final JCheckBox chkFoil = new JCheckBox("Random Foiling");
    private final JCheckBox chkMana = new JCheckBox("Use Text and Mana Overlay");
    private final JButton cmdLAF = new JButton("Choose Look and Feel");
    private final JCheckBox chkLAF = new JCheckBox("Use Look and Feel Fonts");
    private final JButton cmdSize = new JButton("Choose Card Size");
    private final JCheckBox chkScale = new JCheckBox("Scale Card Image Larger");
    private final JButton cmdStack = new JButton("Choose Stack Offset");
    private final JCheckBox chkRemoveArtifacts = new JCheckBox("Remove Artifacts");
    private final JCheckBox chkRemoveSmall = new JCheckBox("Remove Small Creatures");

    // Utilities Panel
    private final JPanel pnlUtilities = new JPanel();
    private final JButton cmdDownloadLQSetPics = new JButton("Download LQ Set Pics");
    private final JButton cmdDownloadPrices = new JButton("Download Prices");
    private final JButton cmdImportPics = new JButton("Import Pictures");
    private final JButton cmdReportBug = new JButton("Report Bug");
    private final JButton cmdHowToPlay = new JButton("How To Play");

    // Local objects
    private final Color clrScrollBackground = new Color(222, 184, 135);

    private final DeckManager deckManager = AllZone.getDeckManager();
    private List<Deck> allDecks;
    private static DeckEditorCommon editor;

    private String playerSelected = "";
    private GameType gameTypeSelected = GameType.Constructed;
    private String humanDeckSelected = "";
    private String aiDeckSelected = "";

    /**
     * Launch the application.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    final Gui_HomeScreen window = new Gui_HomeScreen();
                    window.gHS.setVisible(true);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Gui_HomeScreen() {
        this.initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.gHS = new JFrame();
        this.gHS.setIconImage(Toolkit.getDefaultToolkit().getImage(this.homeScreenPath + "../favicon.png"));
        this.gHS.setTitle("Forge");
        this.gHS.setResizable(false);
        this.gHS.setBounds(100, 100, 605, 627);
        this.gHS.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gHS.getContentPane().setLayout(null);
        this.lblGameMode.setFocusable(false);
        this.lblGameMode.setOpaque(false);
        this.lblGameMode.setBorder(null);
        this.lblGameMode.setIcon(this.imgMode);
        this.lblGameMode.setBounds(10, 187, 205, 30);
        this.gHS.getContentPane().add(this.lblGameMode);
        this.cmdConstructed.setSelectedIcon(this.imgConstructedSel);
        this.cmdConstructed.setBorderPainted(false);
        this.cmdConstructed.setBorder(null);
        this.cmdConstructed.setPressedIcon(this.imgConstructedDown);
        this.cmdConstructed.setRolloverEnabled(true);
        this.cmdConstructed.setRolloverIcon(this.imgConstructedOver);
        this.cmdConstructed.setOpaque(false);
        this.cmdConstructed.setIcon(this.imgConstructedUp);
        this.cmdConstructed.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.cmdConstructed.setContentAreaFilled(false);
        this.cmdConstructed.setBounds(9, 217, 205, 26);
        this.cmdConstructed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.gameTypeSelected = GameType.Constructed;
                Gui_HomeScreen.this.showDecks();
                Gui_HomeScreen.this.doGameModeSelect();
            }
        });
        this.cmdConstructed.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdConstructed.setIcon(Gui_HomeScreen.this.imgConstructedOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdConstructed.setIcon(Gui_HomeScreen.this.imgConstructedUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdConstructed);
        this.cmdSealed.setRolloverIcon(this.imgSealedOver);
        this.cmdSealed.setPressedIcon(this.imgSealedDown);
        this.cmdSealed.setRolloverEnabled(true);
        this.cmdSealed.setSelectedIcon(this.imgSealedSel);
        this.cmdSealed.setOpaque(false);
        this.cmdSealed.setBorder(null);
        this.cmdSealed.setBorderPainted(false);
        this.cmdSealed.setIcon(this.imgSealedUp);
        this.cmdSealed.setFont(new Font("Dialog", Font.BOLD, 10));
        this.cmdSealed.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.cmdSealed.setContentAreaFilled(false);
        this.cmdSealed.setBounds(9, 243, 205, 26);
        this.cmdSealed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.gameTypeSelected = GameType.Sealed;
                Gui_HomeScreen.this.showDecks();
                Gui_HomeScreen.this.doGameModeSelect();
            }
        });
        this.cmdSealed.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdSealed.setIcon(Gui_HomeScreen.this.imgSealedOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdSealed.setIcon(Gui_HomeScreen.this.imgSealedUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdSealed);
        this.cmdDraft.setSelectedIcon(this.imgDraftSel);
        this.cmdDraft.setRolloverIcon(this.imgDraftOver);
        this.cmdDraft.setRolloverEnabled(true);
        this.cmdDraft.setPressedIcon(this.imgDraftDown);
        this.cmdDraft.setOpaque(false);
        this.cmdDraft.setBorder(null);
        this.cmdDraft.setBorderPainted(false);
        this.cmdDraft.setIcon(this.imgDraftUp);
        this.cmdDraft.setFont(new Font("Dialog", Font.BOLD, 10));
        this.cmdDraft.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.cmdDraft.setContentAreaFilled(false);
        this.cmdDraft.setBounds(9, 269, 205, 26);
        this.cmdDraft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.gameTypeSelected = GameType.Draft;
                Gui_HomeScreen.this.showDecks();
                Gui_HomeScreen.this.doGameModeSelect();
            }
        });
        this.cmdDraft.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdDraft.setIcon(Gui_HomeScreen.this.imgDraftOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdDraft.setIcon(Gui_HomeScreen.this.imgDraftUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdDraft);
        this.cmdQuest.setRolloverIcon(this.imgQuestOver);
        this.cmdQuest.setRolloverEnabled(true);
        this.cmdQuest.setSelectedIcon(this.imgQuestSel);
        this.cmdQuest.setPressedIcon(this.imgQuestDown);
        this.cmdQuest.setOpaque(false);
        this.cmdQuest.setBorder(null);
        this.cmdQuest.setBorderPainted(false);
        this.cmdQuest.setIcon(this.imgQuestUp);
        this.cmdQuest.setFont(new Font("Dialog", Font.BOLD, 10));
        this.cmdQuest.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.cmdQuest.setContentAreaFilled(false);
        this.cmdQuest.setBounds(9, 295, 205, 26);
        this.cmdQuest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.gameTypeSelected = GameType.Quest;
                Gui_HomeScreen.this.showDecks();
                Gui_HomeScreen.this.doGameModeSelect();
            }
        });
        this.cmdQuest.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdQuest.setIcon(Gui_HomeScreen.this.imgQuestOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdQuest.setIcon(Gui_HomeScreen.this.imgQuestUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdQuest);
        this.lblLibrary.setFocusable(false);
        this.lblLibrary.setIcon(this.imgLibrary);
        this.lblLibrary.setOpaque(false);
        this.lblLibrary.setBounds(10, 338, 205, 30);
        this.gHS.getContentPane().add(this.lblLibrary);
        this.cmdHumanDeck = new JButton("");
        this.cmdHumanDeck.setSelectedIcon(this.imgHumanSel);
        this.cmdHumanDeck.setRolloverIcon(this.imgHumanOver);
        this.cmdHumanDeck.setPressedIcon(this.imgHumanDown);
        this.cmdHumanDeck.setRolloverEnabled(true);
        this.cmdHumanDeck.setIcon(this.imgHumanUp);
        this.cmdHumanDeck.setOpaque(false);
        this.cmdHumanDeck.setContentAreaFilled(false);
        this.cmdHumanDeck.setBorder(null);
        this.cmdHumanDeck.setBorderPainted(false);
        this.cmdHumanDeck.setBounds(8, 368, 205, 26);
        this.cmdHumanDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.playerSelected = "Human";
                Gui_HomeScreen.this.showDecks();
            }
        });
        this.cmdHumanDeck.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdHumanDeck.setIcon(Gui_HomeScreen.this.imgHumanOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdHumanDeck.setIcon(Gui_HomeScreen.this.imgHumanUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdHumanDeck);
        this.cmdAIDeck = new JButton("");
        this.cmdAIDeck.setSelectedIcon(this.imgAISel);
        this.cmdAIDeck.setPressedIcon(this.imgAIDown);
        this.cmdAIDeck.setRolloverIcon(this.imgAIOver);
        this.cmdAIDeck.setRolloverEnabled(true);
        this.cmdAIDeck.setIcon(this.imgAIUp);
        this.cmdAIDeck.setOpaque(false);
        this.cmdAIDeck.setContentAreaFilled(false);
        this.cmdAIDeck.setBorder(null);
        this.cmdAIDeck.setBorderPainted(false);
        this.cmdAIDeck.setBounds(8, 394, 205, 26);
        this.cmdAIDeck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Gui_HomeScreen.this.playerSelected = "AI";
                Gui_HomeScreen.this.showDecks();

            }
        });
        this.cmdAIDeck.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdAIDeck.setIcon(Gui_HomeScreen.this.imgAIOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdAIDeck.setIcon(Gui_HomeScreen.this.imgAIUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdAIDeck);
        this.cmdDeckEditor.setFocusPainted(false);
        this.cmdDeckEditor.setPressedIcon(this.imgEditorDown);
        this.cmdDeckEditor.setRolloverIcon(this.imgEditorOver);
        this.cmdDeckEditor.setRolloverEnabled(true);
        this.cmdDeckEditor.setContentAreaFilled(false);
        this.cmdDeckEditor.setBorderPainted(false);
        this.cmdDeckEditor.setBorder(null);
        this.cmdDeckEditor.setOpaque(false);
        this.cmdDeckEditor.setIcon(this.imgEditorUp);
        this.cmdDeckEditor.setBounds(10, 436, 205, 30);
        this.cmdDeckEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.doShowEditor();
            }
        });
        this.cmdDeckEditor.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdDeckEditor.setIcon(Gui_HomeScreen.this.imgEditorOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdDeckEditor.setIcon(Gui_HomeScreen.this.imgEditorUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdDeckEditor);
        this.cmdStart.setPressedIcon(this.imgStartDown);
        this.cmdStart.setRolloverIcon(this.imgStartOver);
        this.cmdStart.setRolloverEnabled(true);
        this.cmdStart.setIcon(this.imgStartUp);
        this.cmdStart.setOpaque(false);
        this.cmdStart.setContentAreaFilled(false);
        this.cmdStart.setBorder(null);
        this.cmdStart.setBorderPainted(false);
        this.cmdStart.setBounds(10, 476, 205, 84);
        this.cmdStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.doStartGame();
            }
        });
        this.cmdStart.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdStart.setIcon(Gui_HomeScreen.this.imgStartDown);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdStart.setIcon(Gui_HomeScreen.this.imgStartUp);
            }
        });
        this.gHS.getContentPane().add(this.cmdStart);
        this.cmdSettings.setPressedIcon(this.imgSettingsDown);
        this.cmdSettings.setRolloverIcon(this.imgSettingsOver);
        this.cmdSettings.setRolloverEnabled(true);
        this.cmdSettings.setIcon(this.imgSettingsUp);
        this.cmdSettings.setOpaque(false);
        this.cmdSettings.setContentAreaFilled(false);
        this.cmdSettings.setBorder(null);
        this.cmdSettings.setBorderPainted(false);
        this.cmdSettings.setBounds(212, 10, 205, 50);
        this.cmdSettings.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdSettings.setIcon(Gui_HomeScreen.this.imgSettingsOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdSettings.setIcon(Gui_HomeScreen.this.imgSettingsUp);
            }
        });
        this.cmdSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.pnlIntro.setVisible(false);
                Gui_HomeScreen.this.pnlDecks.setVisible(false);
                Gui_HomeScreen.this.pnlUtilities.setVisible(false);

                Gui_HomeScreen.this.pnlSettings.setVisible(true);
            }
        });
        this.gHS.getContentPane().add(this.cmdSettings);
        this.cmdUtilities.setIcon(this.imgUtilitiesUp);
        this.cmdUtilities.setRolloverEnabled(true);
        this.cmdUtilities.setRolloverIcon(this.imgUtilitiesOver);
        this.cmdUtilities.setPressedIcon(this.imgUtilitiesDown);
        this.cmdUtilities.setOpaque(false);
        this.cmdUtilities.setContentAreaFilled(false);
        this.cmdUtilities.setBorder(null);
        this.cmdUtilities.setBorderPainted(false);
        this.cmdUtilities.setBounds(395, 10, 205, 50);
        this.cmdUtilities.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdUtilities.setIcon(Gui_HomeScreen.this.imgUtilitiesOver);
            }

            @Override
            public void focusLost(final FocusEvent arg0) {
                Gui_HomeScreen.this.cmdUtilities.setIcon(Gui_HomeScreen.this.imgUtilitiesUp);
            }
        });
        this.cmdUtilities.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.pnlIntro.setVisible(false);
                Gui_HomeScreen.this.pnlDecks.setVisible(false);
                Gui_HomeScreen.this.pnlSettings.setVisible(false);

                Gui_HomeScreen.this.pnlUtilities.setVisible(true);
            }
        });
        this.gHS.getContentPane().add(this.cmdUtilities);
        this.pnlIntro.setVisible(true);
        this.pnlIntro.setOpaque(false);
        this.pnlIntro.setBounds(245, 135, 325, 345);
        this.gHS.getContentPane().add(this.pnlIntro);
        this.pnlIntro.setLayout(null);
        this.lblIntro.setBounds(10, 10, 305, 300);
        this.lblIntro.setFont(new Font("", Font.BOLD, 12));
        this.lblIntro.setHorizontalAlignment(SwingConstants.LEFT);
        this.lblIntro.setOpaque(false);
        this.lblIntro.setFocusable(false);
        this.lblIntro
                .setText("<html>Forge is an open source implementation of Magic: the Gathering written in the Java programming language.<br><br>"
                        + "<list><li>Select a Game Mode on the left</li><li>Select a Player</li><li>"
                        + "Choose a deck from the list</li><li>Click Select Deck</li><li>Press Start "
                        + "to begin the game</li></list></html>");
        this.pnlIntro.add(this.lblIntro);
        this.pnlDecks.setVisible(false);
        this.pnlDecks.setOpaque(false);
        this.pnlDecks.setBounds(245, 135, 325, 345);
        this.gHS.getContentPane().add(this.pnlDecks);
        this.pnlDecks.setLayout(null);
        this.lblDecksHeader.setBounds(10, 10, 305, 34);
        this.pnlDecks.add(this.lblDecksHeader);
        this.lblDecksHeader.setFont(new Font("", Font.BOLD | Font.ITALIC, 14));
        this.lblDecksHeader.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblDecksHeader.setOpaque(false);
        this.lblDecksHeader.setFocusable(false);
        this.scrDecks.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrDecks.setOpaque(false);
        this.scrDecks.setBackground(this.clrScrollBackground);
        this.scrDecks.setBounds(10, 45, 305, 260);
        this.pnlDecks.add(this.scrDecks);
        this.lstDecks.setVisible(true);
        this.scrDecks.setViewportView(this.lstDecks);
        this.lstDecks.setBackground(this.clrScrollBackground);
        this.lstDecks.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        this.cmdDeckSelect.setBounds(112, 310, 100, 23);
        this.pnlDecks.add(this.cmdDeckSelect);
        this.cmdDeckSelect.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        this.cmdDeckSelect.setBackground(new Color(255, 222, 173));
        this.cmdDeckSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Gui_HomeScreen.this.doDeckSelect();
            }
        });
        this.pnlSettings.setVisible(false);
        this.pnlSettings.setOpaque(false);
        this.pnlSettings.setBounds(245, 135, 325, 345);
        this.pnlSettings.setLayout(null);
        this.gHS.getContentPane().add(this.pnlSettings);
        this.scrSettings.setPreferredSize(new Dimension(1, 3));
        this.scrSettings.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrSettings.setBackground(this.clrScrollBackground);
        this.scrSettings.setOpaque(false);
        this.scrSettings.setBounds(10, 12, 305, 320);
        this.pnlSettings.add(this.scrSettings);
        this.pnlSettingsA.setBackground(this.clrScrollBackground);
        this.pnlSettingsA.setLayout(new GridLayout(15, 1, 0, 0));
        this.scrSettings.setViewportView(this.pnlSettingsA);
        final JLabel lblBasic = new JLabel("<html><u>Basic Settings</u></html>");
        lblBasic.setHorizontalAlignment(SwingConstants.CENTER);
        this.pnlSettingsA.add(lblBasic);
        this.chkDeveloperMode.setOpaque(false);
        this.chkDeveloperMode.setBackground(this.clrScrollBackground);
        this.chkDeveloperMode.setSelected(Singletons.getModel().getPreferences().isDeveloperMode());
        this.chkDeveloperMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences()
                        .setDeveloperMode(Gui_HomeScreen.this.chkDeveloperMode.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkDeveloperMode);
        this.chkStackAiLand.setOpaque(false);
        this.chkStackAiLand.setBackground(this.clrScrollBackground);
        this.chkStackAiLand.setSelected(Singletons.getModel().getPreferences().isStackAiLand());
        this.chkStackAiLand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences().setStackAiLand(Gui_HomeScreen.this.chkStackAiLand.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkStackAiLand);
        this.chkUploadDraftData.setBackground(this.clrScrollBackground);
        this.chkUploadDraftData.setOpaque(false);
        this.chkUploadDraftData.setSelected(Singletons.getModel().getPreferences().isUploadDraftAI());
        this.chkUploadDraftData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences()
                        .setUploadDraftAI(Gui_HomeScreen.this.chkUploadDraftData.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkUploadDraftData);
        final JLabel lblGraphs = new JLabel("<html><u>Graphical Settings</u></html>");
        lblGraphs.setHorizontalAlignment(SwingConstants.CENTER);
        this.pnlSettingsA.add(lblGraphs);
        this.chkMana.setOpaque(false);
        this.chkMana.setBackground(this.clrScrollBackground);
        this.chkMana.setSelected(Singletons.getModel().getPreferences().isCardOverlay());
        this.chkMana.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences().setCardOverlay(Gui_HomeScreen.this.chkMana.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkMana);
        this.chkFoil.setOpaque(false);
        this.chkFoil.setBackground(this.clrScrollBackground);
        this.chkFoil.setSelected(Singletons.getModel().getPreferences().isRandCFoil());
        this.chkFoil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences().setRandCFoil(Gui_HomeScreen.this.chkFoil.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkFoil);
        // cmdLAF.setBorderPainted(false);
        // cmdLAF.setBorder(new BevelBorder(BevelBorder.RAISED, null,
        // null, null, null));
        this.cmdLAF.setOpaque(false);
        this.cmdLAF.setBackground(this.clrScrollBackground);
        this.cmdLAF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.doLAF();
            }
        });
        this.pnlSettingsA.add(this.cmdLAF);
        this.chkLAF.setOpaque(false);
        this.chkLAF.setBackground(this.clrScrollBackground);
        this.chkLAF.setSelected(Singletons.getModel().getPreferences().isLafFonts());
        this.chkLAF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences().setLafFonts(Gui_HomeScreen.this.chkLAF.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkLAF);
        this.cmdSize.setOpaque(false);
        this.cmdSize.setBackground(this.clrScrollBackground);
        this.cmdSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.doCardSize();
            }
        });
        this.pnlSettingsA.add(this.cmdSize);
        this.chkScale.setOpaque(false);
        this.chkScale.setBackground(this.clrScrollBackground);
        this.chkScale.setSelected(Singletons.getModel().getPreferences().isScaleLargerThanOriginal());
        this.chkScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences()
                        .setScaleLargerThanOriginal(Gui_HomeScreen.this.chkScale.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkScale);
        this.cmdStack.setOpaque(false);
        this.cmdStack.setBackground(this.clrScrollBackground);
        this.cmdStack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Gui_HomeScreen.this.doStackOffset();
            }
        });
        this.pnlSettingsA.add(this.cmdStack);
        final JLabel lblGenGraphs = new JLabel("<html><u>Deck Generation Settings</u></html>");
        lblGenGraphs.setHorizontalAlignment(SwingConstants.CENTER);
        this.pnlSettingsA.add(lblGenGraphs);
        this.chkRemoveArtifacts.setOpaque(false);
        this.chkRemoveArtifacts.setBackground(this.clrScrollBackground);
        this.chkRemoveArtifacts.setSelected(Singletons.getModel().getPreferences().isDeckGenRmvArtifacts());
        this.chkRemoveArtifacts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences()
                        .setDeckGenRmvArtifacts(Gui_HomeScreen.this.chkRemoveArtifacts.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkRemoveArtifacts);
        this.chkRemoveSmall.setOpaque(false);
        this.chkRemoveSmall.setBackground(this.clrScrollBackground);
        this.chkRemoveSmall.setSelected(Singletons.getModel().getPreferences().isDeckGenRmvSmall());
        this.chkRemoveSmall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                Singletons.getModel().getPreferences()
                        .setDeckGenRmvSmall(Gui_HomeScreen.this.chkRemoveSmall.isSelected());
            }
        });
        this.pnlSettingsA.add(this.chkRemoveSmall);
        this.pnlUtilities.setOpaque(false);
        this.pnlUtilities.setVisible(false);
        this.pnlUtilities.setBounds(245, 135, 325, 345);
        this.pnlUtilities.setLayout(new GridLayout(5, 1, 0, 0));
        this.gHS.getContentPane().add(this.pnlUtilities);
        this.cmdDownloadLQSetPics.setOpaque(false);
        this.cmdDownloadLQSetPics.setBackground(this.clrScrollBackground);
        this.cmdDownloadLQSetPics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new GuiDownloadSetPicturesLQ(null);
            }
        });
        this.pnlUtilities.add(this.cmdDownloadLQSetPics);
        this.cmdDownloadPrices.setOpaque(false);
        this.cmdDownloadPrices.setBackground(this.clrScrollBackground);
        this.cmdDownloadPrices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final GuiDownloadPrices gdp = new GuiDownloadPrices();
                gdp.setVisible(true);
            }
        });
        this.pnlUtilities.add(this.cmdDownloadPrices);
        this.cmdImportPics.setOpaque(false);
        this.cmdImportPics.setBackground(this.clrScrollBackground);
        this.cmdImportPics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final GuiImportPicture ip = new GuiImportPicture(null);
                ip.setVisible(true);
            }
        });
        this.pnlUtilities.add(this.cmdImportPics);
        this.cmdReportBug.setOpaque(false);
        this.cmdReportBug.setBackground(this.clrScrollBackground);
        this.cmdReportBug.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final BugzReporter br = new BugzReporter();
                br.setVisible(true);
            }
        });
        this.pnlUtilities.add(this.cmdReportBug);
        this.cmdHowToPlay.setOpaque(false);
        this.cmdHowToPlay.setBackground(this.clrScrollBackground);
        this.cmdHowToPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final String text = ForgeProps.getLocalized(Lang.HowTo.MESSAGE);

                final JTextArea area = new JTextArea(text, 25, 40);
                area.setWrapStyleWord(true);
                area.setLineWrap(true);
                area.setEditable(false);
                area.setOpaque(false);

                JOptionPane.showMessageDialog(null, new JScrollPane(area), ForgeProps.getLocalized(Lang.HowTo.TITLE),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        this.pnlUtilities.add(this.cmdHowToPlay);
        this.lblBackground.setIcon(this.imgBackground);
        this.lblBackground.setBounds(0, 0, 600, 600);
        this.gHS.getContentPane().add(this.lblBackground);
        this.gHS.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { this.lblBackground,
                this.cmdConstructed, this.cmdSealed, this.cmdDraft, this.cmdQuest, this.cmdHumanDeck, this.cmdAIDeck,
                this.cmdDeckEditor, this.cmdSettings, this.cmdStart, this.lstDecks, this.cmdDeckSelect }));

        GuiUtils.centerFrame(this.gHS);

        // non gui init stuff
        this.allDecks = new ArrayList<Deck>(this.deckManager.getDecks());
    }

    private void doGameModeSelect() {
        // simulate a radio button group, because JRadioButton wasn't
        // transparent on Roll-over
        this.cmdConstructed.setSelected(this.gameTypeSelected.equals(GameType.Constructed));
        this.cmdSealed.setSelected(this.gameTypeSelected.equals(GameType.Sealed));
        this.cmdDraft.setSelected(this.gameTypeSelected.equals(GameType.Draft));
        this.cmdQuest.setSelected(this.gameTypeSelected.equals(GameType.Quest));
    }

    private void doDeckSelect() {
        if (this.lstDecks.getSelectedIndex() != -1) {
            if (this.playerSelected.equals("Human")) {
                this.humanDeckSelected = this.lstDecks.getSelectedValue().toString();
                this.cmdHumanDeck.setSelected(true);
                this.cmdHumanDeck.setToolTipText(this.humanDeckSelected);
            } else if (this.playerSelected.equals("AI")) {
                this.aiDeckSelected = this.lstDecks.getSelectedValue().toString();
                this.cmdAIDeck.setSelected(true);
                this.cmdAIDeck.setToolTipText(this.aiDeckSelected);
            }

        }
    }

    private boolean doDeckLogic() {
        if (this.gameTypeSelected.equals(GameType.Constructed)) {
            if (this.humanDeckSelected.equals("Generate Deck")) {
                DeckGeneration.genDecks(PlayerType.HUMAN);

            } else if (this.humanDeckSelected.equals("Random Deck")) {
                final Deck rDeck = this.chooseRandomDeck();

                if (rDeck != null) {
                    final String msg = String.format("You are using deck: %s.",
                            Constant.Runtime.HUMAN_DECK[0].getName());
                    JOptionPane.showMessageDialog(null, msg, "Random Deck Name", JOptionPane.INFORMATION_MESSAGE);

                    Constant.Runtime.HUMAN_DECK[0] = rDeck;
                } else {
                    JOptionPane.showMessageDialog(null, "No decks available.", "Random Deck Name",
                            JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }

            } else {
                Constant.Runtime.HUMAN_DECK[0] = this.deckManager.getDeck(this.humanDeckSelected);
            }

            if (this.aiDeckSelected.equals("Generate Deck")) {
                DeckGeneration.genDecks(PlayerType.COMPUTER);

            } else if (this.aiDeckSelected.equals("Random Deck")) {
                final Deck rDeck = this.chooseRandomDeck();

                if (rDeck != null) {
                    final String msg = String.format("The computer is using deck: %s.",
                            Constant.Runtime.COMPUTER_DECK[0].getName());
                    JOptionPane.showMessageDialog(null, msg, "Random Deck Name", JOptionPane.INFORMATION_MESSAGE);

                    Constant.Runtime.COMPUTER_DECK[0] = rDeck;
                } else {
                    JOptionPane.showMessageDialog(null, "No decks available.", "Random Deck Name",
                            JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }

            } else {
                Constant.Runtime.COMPUTER_DECK[0] = this.deckManager.getDeck(this.aiDeckSelected);
            }

        } else if (this.gameTypeSelected.equals(GameType.Sealed)) {
            if (this.humanDeckSelected.equals("New Sealed")) {
                // NG2.dispose();

                this.launchSealed();

                return false;

            } else {
                if (!this.humanDeckSelected.equals("") && !this.aiDeckSelected.equals("")) {
                    Constant.Runtime.HUMAN_DECK[0] = this.deckManager.getDeck(this.humanDeckSelected);
                    Constant.Runtime.COMPUTER_DECK[0] = this.deckManager.getDeck(this.aiDeckSelected);
                }
            }
        } else if (this.gameTypeSelected.equals(GameType.Draft)) {
            if (this.humanDeckSelected.equals("NewDraft")) {
                // NG2.dispose();

                this.launchDraft();

                return false;
            } else {
                if (!this.humanDeckSelected.equals("") && !this.aiDeckSelected.equals("")) {
                    Constant.Runtime.HUMAN_DECK[0] = this.deckManager.getDraftDeck(this.humanDeckSelected)[0];

                    final String[] aiDeck = this.aiDeckSelected.split(" - ");
                    final int aiDeckNum = Integer.parseInt(aiDeck[1]);
                    final String aiDeckName = aiDeck[0];

                    Constant.Runtime.COMPUTER_DECK[0] = this.deckManager.getDraftDeck(aiDeckName)[aiDeckNum];
                }
            }
        }

        return true;
    }

    private void launchDraft() {
        final DeckEditorDraft draft = new DeckEditorDraft();

        // determine what kind of booster draft to run
        final ArrayList<String> draftTypes = new ArrayList<String>();
        draftTypes.add("Full Cardpool");
        draftTypes.add("Block / Set");
        draftTypes.add("Custom");

        final String prompt = "Choose Draft Format:";
        final Object o = GuiUtils.getChoice(prompt, draftTypes.toArray());

        if (o.toString().equals(draftTypes.get(0))) {
            draft.showGui(new BoosterDraft_1(CardPoolLimitation.Full));
        }

        else if (o.toString().equals(draftTypes.get(1))) {
            draft.showGui(new BoosterDraft_1(CardPoolLimitation.Block));
        }

        else if (o.toString().equals(draftTypes.get(2))) {
            draft.showGui(new BoosterDraft_1(CardPoolLimitation.Custom));
        }

    }

    private void launchSealed() {
        final String[] sealedTypes = { "Full Cardpool", "Block / Set", "Custom" };

        final String prompt = "Choose Sealed Deck Format:";
        final Object o = GuiUtils.getChoice(prompt, sealedTypes);

        SealedDeck sd = null;

        if (o.toString().equals(sealedTypes[0])) {
            sd = new SealedDeck("Full");
        }

        else if (o.toString().equals(sealedTypes[1])) {
            sd = new SealedDeck("Block");
        }

        else if (o.toString().equals(sealedTypes[2])) {
            sd = new SealedDeck("Custom");
        }

        else {
            throw new IllegalStateException("choice <<" + UtilFunctions.safeToString(o)
                    + ">> does not equal any of the sealedTypes.");
        }

        final ItemPool<CardPrinted> sDeck = sd.getCardpool();

        if (sDeck.countAll() > 1) {
            final Deck deck = new Deck(GameType.Sealed);

            deck.addSideboard(sDeck);

            for (final String element : Constant.Color.BASIC_LANDS) {
                for (int j = 0; j < 18; j++) {
                    deck.addSideboard(element + "|" + sd.getLandSetCode()[0]);
                }
            }

            final String sDeckName = JOptionPane.showInputDialog(null,
                    ForgeProps.getLocalized(NewGameText.SAVE_SEALED_MSG),
                    ForgeProps.getLocalized(NewGameText.SAVE_SEALED_TTL), JOptionPane.QUESTION_MESSAGE);
            deck.setName(sDeckName);
            deck.setPlayerType(PlayerType.HUMAN);

            this.humanDeckSelected = sDeckName;
            Constant.Runtime.HUMAN_DECK[0] = deck;
            this.aiDeckSelected = "AI_" + sDeckName;

            // Deck aiDeck = sd.buildAIDeck(sDeck.toForgeCardList());
            final Deck aiDeck = sd.buildAIDeck(sd.getCardpool().toForgeCardList()); // AI
            // will
            // use
            // different
            // cardpool

            aiDeck.setName("AI_" + sDeckName);
            aiDeck.setPlayerType(PlayerType.COMPUTER);
            this.deckManager.addDeck(aiDeck);
            DeckManager.writeDeck(aiDeck, DeckManager.makeFileName(aiDeck));
            Constant.Runtime.COMPUTER_DECK[0] = aiDeck;

            this.showDecks();

            // cmdDeckEditor.doClick();
            // editor.customMenu.setCurrentGameType(Constant.GameType.Sealed);
            // editor.customMenu.showSealedDeck(deck);
        }
    }

    private Deck chooseRandomDeck() {
        Deck ret = null;

        final ArrayList<Deck> subDecks = new ArrayList<Deck>();
        for (final Deck d : this.allDecks) {
            if (d.getDeckType().equals(GameType.Constructed) && !d.isCustomPool()) {
                subDecks.add(d);
            }
        }

        if (subDecks.size() > 0) {
            final int n = MyRandom.getRandom().nextInt(subDecks.size());
            ret = subDecks.get(n);

        } else {
            JOptionPane.showMessageDialog(null, "Not enough decks to choose from.", "Random Deck Name",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        return ret;
    }

    private void showDecks() {
        this.deckManager.readAllDecks();

        this.lblDecksHeader.setText("");
        this.pnlIntro.setVisible(false);
        this.pnlDecks.setVisible(false);
        this.pnlSettings.setVisible(false);
        this.pnlUtilities.setVisible(false);

        final DefaultListModel deckList = new DefaultListModel();
        this.lstDecks.setModel(deckList);

        if (this.gameTypeSelected.equals(GameType.Constructed)) {
            if (this.playerSelected.equals("Human")) {
                this.lblDecksHeader.setText("Your Constructed Decks");
            } else if (this.playerSelected.equals("AI")) {
                this.lblDecksHeader.setText("AI Constructed Decks");
            }

            if (!this.playerSelected.equals("")) {
                deckList.addElement("Generate Deck");
                deckList.addElement("Random Deck");

                for (final Deck aDeck : this.allDecks) {
                    if (aDeck.getDeckType().equals(GameType.Constructed) && !aDeck.isCustomPool()) {
                        deckList.addElement(aDeck.getName());
                    }
                }

            }

        } else if (this.gameTypeSelected.equals(GameType.Sealed)) {
            if (this.playerSelected.equals("Human")) {
                this.lblDecksHeader.setText("Your Sealed Decks");

                deckList.addElement("New Sealed");

                for (final Deck aDeck : this.allDecks) {
                    if (aDeck.getDeckType().equals(GameType.Sealed) && (aDeck.getPlayerType() == PlayerType.HUMAN)) {
                        deckList.addElement(aDeck.getName());
                    }
                }
            } else if (this.playerSelected.equals("AI")) {
                this.lblDecksHeader.setText("AI Sealed Decks");

                for (final Deck aDeck : this.allDecks) {
                    if (aDeck.getDeckType().equals(GameType.Sealed)
                            && aDeck.getPlayerType().equals(PlayerType.COMPUTER)) {
                        deckList.addElement(aDeck.getName());
                    }
                }
            }

        } else if (this.gameTypeSelected.equals(GameType.Draft)) {
            if (this.playerSelected.equals("Human")) {
                this.lblDecksHeader.setText("Your Draft Decks");

                deckList.addElement("New Draft");

                for (final String sKey : this.deckManager.getDraftDecks().keySet()) {
                    deckList.addElement(sKey);
                }

            } else if (this.playerSelected.equals("AI")) {
                this.lblDecksHeader.setText("AI Draft Decks");

                for (final String sKey : this.deckManager.getDraftDecks().keySet()) {
                    for (int i = 1; i <= 7; i++) {
                        deckList.addElement(sKey + " - " + i);
                    }
                }
            }

        } else if (this.cmdQuest.isSelected()) {
            this.lblDecksHeader.setText("");
            // lstDecks.setVisible(false);
            // cmdDeckSelect.setVisible(false);
        }

        if (!this.playerSelected.equals("") && !this.gameTypeSelected.equals(GameType.Quest)) {
            this.lstDecks.setModel(deckList);
            // lstDecks.setVisible(true);
            // scrDecks.setVisible(true);
            this.pnlDecks.setVisible(true);
            // cmdDeckSelect.setVisible(true);
        }

    }

    private void doShowEditor() {
        if (Gui_HomeScreen.editor == null) {

            Gui_HomeScreen.editor = new DeckEditorCommon(GameType.Constructed);

            final Command exit = new Command() {
                private static final long serialVersionUID = -9133358399503226853L;

                @Override
                public void execute() {
                    final String[] ng = { "" };
                    Gui_HomeScreen.main(ng);
                }
            };
            Gui_HomeScreen.editor.show(exit);
            Gui_HomeScreen.editor.setVisible(true);
        } // if

        // refresh decks:
        this.allDecks = new ArrayList<Deck>(this.deckManager.getDecks());

        // TO-DO (TO have DOne) - this seems hacky. If someone knows how to do
        // this for real, feel free.
        // This make it so the second time you open the Deck Editor, typing a
        // card name and pressing enter will filter
        // editor.getRootPane().setDefaultButton(editor.filterButton);

        Gui_HomeScreen.editor.setVisible(true);

        this.gHS.dispose();
    }

    private void doStartGame() {
        if (this.gameTypeSelected.equals(GameType.Quest)) {
            new QuestOptions();
        } else {
            if (this.humanDeckSelected.equals("") && this.aiDeckSelected.equals("")) {
                return;
            }

            if (!this.doDeckLogic()) {
                return;
            }

            AllZone.setDisplay(new GuiDisplay());
            AllZone.getGameAction().newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
            AllZone.getDisplay().setVisible(true);
        }

        Constant.Runtime.setGameType(this.gameTypeSelected);

        this.gHS.dispose();
    }

    private void doLAF() {
        final LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        final HashMap<String, String> lafMap = new HashMap<String, String>();
        for (final LookAndFeelInfo anInfo : info) {
            lafMap.put(anInfo.getName(), anInfo.getClassName());
        }

        // add Substance LAFs:
        lafMap.put("Autumn", "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
        lafMap.put("Business", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
        lafMap.put("Business Black Steel",
                "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
        lafMap.put("Business Blue Steel", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
        lafMap.put("Challenger Deep", "org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel");
        lafMap.put("Creme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
        lafMap.put("Creme Coffee", "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
        lafMap.put("Dust", "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
        lafMap.put("Dust Coffee", "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
        lafMap.put("Emerald Dusk", "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel");
        lafMap.put("Gemini", "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
        lafMap.put("Graphite", "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
        lafMap.put("Graphite Aqua", "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel");
        lafMap.put("Graphite Glass", "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel");
        lafMap.put("Magma", "org.pushingpixels.substance.api.skin.SubstanceMagmaLookAndFeel");
        lafMap.put("Magellan", "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel");
        lafMap.put("Mist Aqua", "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
        lafMap.put("Mist Silver", "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
        lafMap.put("Moderate", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
        lafMap.put("Nebula", "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
        lafMap.put("Nebula Brick Wall", "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
        lafMap.put("Office Blue 2007", "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel");
        lafMap.put("Office Silver 2007", "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
        lafMap.put("Raven", "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel");
        lafMap.put("Raven Graphite", "org.pushingpixels.substance.api.skin.SubstanceRavenGraphiteLookAndFeel");
        lafMap.put("Sahara", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
        lafMap.put("Twilight", "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel");

        final String[] keys = new String[lafMap.size()];
        int count = 0;

        for (final String s1 : lafMap.keySet()) {
            keys[count++] = s1;
        }
        Arrays.sort(keys);

        final ListChooser<String> ch = new ListChooser<String>("Choose one", 0, 1, keys);
        if (ch.show()) {
            try {
                final String name = ch.getSelectedValue();
                final int index = ch.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                // UIManager.setLookAndFeel(info[index].getClassName());
                Singletons.getModel().getPreferences().setLaf(lafMap.get(name));
                UIManager.setLookAndFeel(lafMap.get(name));

                // SwingUtilities.updateComponentTreeUI(NG2);
            } catch (final Exception ex) {
                ErrorViewer.showError(ex);
            }
        }
    }

    private void doCardSize() {
        final String[] keys = { "Tiny", "Smaller", "Small", "Medium", "Large(default)", "Huge" };
        final int[] widths = { 52, 80, 120, 200, 300, 400 };
        final int[] heights = { 50, 59, 88, 98, 130, 168 };

        final ListChooser<String> ch = new ListChooser<String>("Choose one", "Choose a new max card size", 0, 1, keys);
        if (ch.show()) {
            try {
                final int index = ch.getSelectedIndex();
                if (index == -1) {
                    return;
                }

                Singletons.getModel().getPreferences().setCardSize(CardSizeType.valueOf(keys[index].toLowerCase()));
                Constant.Runtime.WIDTH[0] = widths[index];
                Constant.Runtime.HEIGHT[0] = heights[index];

            } catch (final Exception ex) {
                ErrorViewer.showError(ex);
            }
        }
    }

    private void doStackOffset() {
        final String[] keys = { "Tiny", "Small", "Medium", "Large" };
        final int[] offsets = { 5, 7, 10, 15 };

        final ListChooser<String> ch = new ListChooser<String>("Choose one", "Choose a stack offset value", 0, 1, keys);
        if (ch.show()) {
            try {
                final int index = ch.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                Singletons.getModel().getPreferences()
                        .setStackOffset(StackOffsetType.valueOf(keys[index].toLowerCase()));
                Constant.Runtime.STACK_OFFSET[0] = offsets[index];

            } catch (final Exception ex) {
                ErrorViewer.showError(ex);
            }
        }
    }
}
