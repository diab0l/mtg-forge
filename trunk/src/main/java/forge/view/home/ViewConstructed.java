package forge.view.home;

import java.awt.BorderLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.control.home.ControlConstructed;
import forge.view.ViewHomeUI;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FList;
import forge.view.toolbox.FProgressBar;
import forge.view.toolbox.FScrollPane;
import forge.view.toolbox.FSkin;

/** 
 * Assembles swing components for "Constructed" mode menu.
 * 
 */
@SuppressWarnings("serial")
public class ViewConstructed extends JPanel {
    private final ViewHomeUI parentView;
    private final ControlConstructed control;
    private final JButton btnStart;
    private final FProgressBar barProgress;

    private FLabel btnHumanRandomTheme, btnHumanRandomDeck, btnAIRandomTheme, btnAIRandomDeck;
    private JList lstDecksAI;
    private FList lstColorsHuman, lstThemesHuman, lstDecksHuman, lstColorsAI, lstThemesAI;

    private final String multi = System.getProperty("os.name").equals("Mac OS X") ? "CMD" : "CTRL";

    private final String colorsToolTip = "Generate deck (Multi-select: " + multi + ")";
    private final String themeToolTip = "Generate deck with a theme";
    private final String decklistToolTip = "Load deck (Decklist: Double Click)";

    /**
     * Assembles swing components for "Constructed" mode menu.
     * 
     * @param v0 {@link forge.view.ViewHomeUI} parent view
     */
    public ViewConstructed(ViewHomeUI v0) {
        //========== Basic init stuff
        super();
        this.setOpaque(false);
        this.setLayout(new MigLayout("insets 0, gap 0, hidemode 2"));
        parentView = v0;

        populateHuman();

        populateAI();

        // Start button
        btnStart = new StartButton(parentView);
        this.add(btnStart, "gap 6% 0 2% 0, span 5 1, ax center, wrap");

        barProgress = new FProgressBar();
        barProgress.setVisible(false);
        this.add(barProgress, "w 150px!, h 30px!, gap 6% 0 2% 0, span 5 1, align center");

        // When all components have been added, add listeners.
        control = new ControlConstructed(this);
        control.updateDeckNames();
        control.addListeners();
    }

    /** Assembles Swing components in human area. */
    private void populateHuman() {
        lstColorsHuman = new FList();
        lstColorsHuman.setName("lstColorsHuman");

        lstThemesHuman = new FList();
        lstThemesHuman.setName("lstThemesHuman");
        lstThemesHuman.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        lstDecksHuman = new FList();
        lstDecksHuman.setName("lstDecksHuman");
        lstDecksHuman.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        JLabel lblHuman = new JLabel("Choose your deck:");
        lblHuman.setFont(FSkin.getBoldFont(16));
        lblHuman.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblHuman.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblColorInfo = new JLabel(colorsToolTip);
        lblColorInfo.setToolTipText(colorsToolTip);
        lblColorInfo.setFont(FSkin.getItalicFont(12));
        lblColorInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblColorInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblThemeInfo = new JLabel(themeToolTip);
        lblThemeInfo.setToolTipText(themeToolTip);
        lblThemeInfo.setFont(FSkin.getItalicFont(12));
        lblThemeInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblThemeInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblDecklistInfo = new JLabel(decklistToolTip);
        lblDecklistInfo.setToolTipText(decklistToolTip);
        lblDecklistInfo.setFont(FSkin.getItalicFont(12));
        lblDecklistInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblDecklistInfo.setHorizontalAlignment(SwingConstants.CENTER);

        // Random theme and pre-constructed buttons
        btnHumanRandomTheme = new FLabel.Builder().opaque(true)
                .hoverable(true).text("Random Theme Deck").build();
        btnHumanRandomDeck = new FLabel.Builder().opaque(true)
                .hoverable(true).text("Random Deck").build();

        // Add components to human area
        JPanel colorsContainer = new JPanel();
        colorsContainer.setOpaque(false);
        colorsContainer.setLayout(new MigLayout("insets 0, gap 0"));
        colorsContainer.add(lblColorInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        colorsContainer.add(new FScrollPane(lstColorsHuman), "w 100%!, h 87%!, wrap");

        JPanel themeContainer = new JPanel();
        themeContainer.setOpaque(false);
        themeContainer.setLayout(new MigLayout("insets 0, gap 0"));
        themeContainer.add(lblThemeInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        themeContainer.add(new FScrollPane(lstThemesHuman), "w 100%!, h 73%!, wrap");
        themeContainer.add(btnHumanRandomTheme, "w 100%!, h 12%!, gaptop 2.5%");

        JPanel decksContainer = new JPanel();
        decksContainer.setOpaque(false);
        decksContainer.setLayout(new MigLayout("insets 0, gap 0"));
        decksContainer.add(lblDecklistInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        decksContainer.add(new FScrollPane(lstDecksHuman), "w 100%!, h 73%!, wrap");
        decksContainer.add(btnHumanRandomDeck, "w 100%!, h 12%!, gaptop 2.5%");

        String listConstraints = "w 28%!, h 31%!";
        String orConstraints = "w 5%!, h 31%!";
        this.add(lblHuman, "w 94%!, h 5%!, gap 3% 0 2% 0, wrap, span 5 1");
        this.add(colorsContainer, listConstraints + ", gapleft 3%");
        this.add(new OrPanel(), orConstraints);
        this.add(themeContainer, listConstraints);
        this.add(new OrPanel(), orConstraints);
        this.add(decksContainer, listConstraints + ", wrap");
    }

    /** Assembles Swing components in AI area. */
    private void populateAI() {
        lstColorsAI = new FList();
        //
        lstColorsAI.setName("lstColorsAI");

        lstThemesAI = new FList();
        //
        lstThemesAI.setName("lstThemesAI");
        lstThemesAI.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        lstDecksAI = new FList();
        lstDecksAI.setName("lstDecksAI");
        lstDecksAI.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        JLabel lblAI = new JLabel("Choose a deck for the computer:");
        lblAI.setFont(FSkin.getBoldFont(16));
        lblAI.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblAI.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblColorInfo = new JLabel(colorsToolTip);
        lblColorInfo.setToolTipText(colorsToolTip);
        lblColorInfo.setFont(FSkin.getItalicFont(12));
        lblColorInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblColorInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblThemeInfo = new JLabel(themeToolTip);
        lblThemeInfo.setToolTipText(themeToolTip);
        lblThemeInfo.setFont(FSkin.getItalicFont(12));
        lblThemeInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblThemeInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblDecklistInfo = new JLabel(decklistToolTip);
        lblDecklistInfo.setToolTipText(decklistToolTip);
        lblDecklistInfo.setFont(FSkin.getItalicFont(12));
        lblDecklistInfo.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblDecklistInfo.setHorizontalAlignment(SwingConstants.CENTER);

        // Random theme and pre-constructed deck buttons
        btnAIRandomTheme = new FLabel.Builder().opaque(true)
                .hoverable(true).text("Random Theme Deck").build();
        btnAIRandomDeck = new FLabel.Builder().opaque(true)
                .hoverable(true).text("Random Deck").build();

        // Add components to AI area
        JPanel colorsContainer = new JPanel();
        colorsContainer.setOpaque(false);
        colorsContainer.setLayout(new MigLayout("insets 0, gap 0"));
        colorsContainer.add(lblColorInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        colorsContainer.add(new FScrollPane(lstColorsAI), "w 100%!, h 87%!, wrap");

        JPanel themeContainer = new JPanel();
        themeContainer.setOpaque(false);
        themeContainer.setLayout(new MigLayout("insets 0, gap 0"));
        themeContainer.add(lblThemeInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        themeContainer.add(new FScrollPane(lstThemesAI), "w 100%!, h 73%!, wrap");
        themeContainer.add(btnAIRandomTheme, "w 100%!, h 12%!, gaptop 2.5%");

        JPanel decksContainer = new JPanel();
        decksContainer.setOpaque(false);
        decksContainer.setLayout(new MigLayout("insets 0, gap 0"));
        decksContainer.add(lblDecklistInfo, "w 100%!, h 10%!, gapbottom 2%, wrap");
        decksContainer.add(new FScrollPane(lstDecksAI), "w 100%!, h 73%!, wrap");
        decksContainer.add(btnAIRandomDeck, "w 100%!, h 12%!, gaptop 2.5%");

        String listConstraints = "w 28%!, h 31%!";
        String orConstraints = "w 5%!, h 31%!";

        this.add(lblAI, "w 94%!, h 5%!, gap 3% 0 5% 0, span 5 1, wrap");
        this.add(colorsContainer, listConstraints + ", gapleft 3%");
        this.add(new OrPanel(), orConstraints);
        this.add(themeContainer, listConstraints);
        this.add(new OrPanel(), orConstraints);
        this.add(decksContainer, listConstraints + ", wrap");
    }

    //========= RETRIEVAL FUNCTIONS
    /** @return {@link forge.view.ViewHomeUI} */
    public ViewHomeUI getParentView() {
        return parentView;
    }

    /** @return {@link forge.control.home.ControlConstructed} */
    public ControlConstructed getController() {
        return control;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstColorsHuman() {
        return lstColorsHuman;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstThemesHuman() {
        return lstThemesHuman;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstDecksHuman() {
        return lstDecksHuman;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstColorsAI() {
        return lstColorsAI;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstThemesAI() {
        return lstThemesAI;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstDecksAI() {
        return lstDecksAI;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnHumanRandomTheme() {
        return btnHumanRandomTheme;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnAIRandomTheme() {
        return btnAIRandomTheme;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnHumanRandomDeck() {
        return btnHumanRandomDeck;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnAIRandomDeck() {
        return btnAIRandomDeck;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return btnStart;
    }

    /** @return {@link forge.view.toolbox.FProgressBar} */
    public FProgressBar getBarProgress() {
        return barProgress;
    }

    // For some reason, MigLayout has sizing problems with a JLabel next to a JList.
    // So, the "or" label must be nested in a panel.
    private class OrPanel extends JPanel {
        public OrPanel() {
            super();
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel lblOr = new JLabel("OR");
            lblOr.setHorizontalAlignment(SwingConstants.CENTER);
            lblOr.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
            add(lblOr, BorderLayout.CENTER);
        }
    }
}
