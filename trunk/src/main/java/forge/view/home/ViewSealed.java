package forge.view.home;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.control.home.ControlSealed;
import forge.game.GameType;
import forge.view.ViewHomeUI;
import forge.view.toolbox.DeckLister;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FProgressBar;
import forge.view.toolbox.FScrollPane;
import forge.view.toolbox.FSkin;

/** 
 * Assembles swing components for "Sealed" mode menu.
 *
 */
@SuppressWarnings("serial")
public class ViewSealed extends JPanel {
    private final ViewHomeUI parentView;
    private final ControlSealed control;
    private DeckLister lstHumanDecks;
    private final JButton btnStart;
    private final FLabel btnBuild;
    private final FProgressBar barProgress;

    /**
     * Assembles swing components for "Sealed" mode menu.
     * 
     * @param v0 {@link forge.view.ViewHomeUI} parent view
     */
    public ViewSealed(ViewHomeUI v0) {
        super();
        this.setOpaque(false);
        this.setLayout(new MigLayout("insets 0, gap 0, hidemode 2, wrap"));
        parentView = v0;
        control = new ControlSealed(this);

        // Title
        JLabel lblTitle = new JLabel("Select a deck for yourself, or build a new one: ");
        lblTitle.setFont(FSkin.getBoldFont(14));
        lblTitle.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Deck lister
        lstHumanDecks = new DeckLister(GameType.Sealed, control.getExitCommand());

        // Build button
        btnBuild = new FLabel.Builder().opaque(true).hoverable(true).text("Build A New Deck").build();

        // Start button
        btnStart = new StartButton(parentView);
        barProgress = new FProgressBar();
        barProgress.setVisible(false);

        // Add components
        this.add(lblTitle, "w 100%!, gap 0 0 2% 2%");
        this.add(new FScrollPane(lstHumanDecks), "w 90%!, h 35%!, gap 5% 0 2% 2%");
        this.add(btnBuild, "w 50%!, h 5%!, gap 25% 0 0 0, wrap");
        this.add(btnStart, "ax center, gaptop 5%");
        this.add(barProgress, "w 150px!, h 30px!, align center");

        control.updateDeckLists();
        control.addListeners();
    }

    /** @return {@link forge.view.ViewHomeUI} */
    public ViewHomeUI getParentView() {
        return parentView;
    }

    /** @return {@link javax.swing.JList} */
    public DeckLister getLstHumanDecks() {
        return lstHumanDecks;
    }

    /** @return {@link forge.control.home.ControlSealed} */
    public ControlSealed getControl() {
        return control;
    }

    /** @return {@link forge.view.toolbox.FLabel} */
    public FLabel getBtnBuild() {
        return btnBuild;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return btnStart;
    }

    /** @return {@link forge.view.toolbox.FProgressBar} */
    public FProgressBar getBarProgress() {
        return barProgress;
    }
}
