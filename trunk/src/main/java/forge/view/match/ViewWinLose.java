package forge.view.match;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.AllZone;
import forge.Constant;
import forge.PhaseHandler;
import forge.Player;
import forge.Singletons;
import forge.control.match.ControlWinLose;
import forge.game.GameType;
import forge.model.FMatchState;
import forge.properties.ForgeProps;
import forge.properties.NewConstants.Lang.GuiWinLose.WinLoseText;
import forge.quest.gui.QuestWinLoseHandler;
import forge.view.toolbox.FButton;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FOverlay;
import forge.view.toolbox.FScrollPane;
import forge.view.toolbox.FSkin;
import forge.view.toolbox.FTextArea;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class ViewWinLose {
    private final FButton btnContinue, btnRestart, btnQuit;
    private final JPanel pnlCustom;
    private final FTextArea txtLog;
    private final FSkin skin;

    /** */
    public ViewWinLose() {
        final FOverlay overlay = AllZone.getOverlay();
        final FMatchState matchState = AllZone.getMatchState();

        final JPanel pnlLeft = new JPanel();
        final JPanel pnlRight = new JPanel();

        final JLabel lblTitle = new JLabel("WinLoseFrame > lblTitle needs updating.");
        final JLabel lblStats = new JLabel("WinLoseFrame > lblStats needs updating.");
        final JScrollPane scrCustom = new JScrollPane();
        pnlCustom = new JPanel();

        btnContinue = new FButton();
        btnRestart = new FButton();
        btnQuit = new FButton();

        skin = Singletons.getView().getSkin();

        // Control of the win/lose is handled differently for various game modes.
        ControlWinLose control;
        if (Constant.Runtime.getGameType() == GameType.Quest) {
            control = new QuestWinLoseHandler(this);
        }
        else {
            control = new ControlWinLose(this);
        }

        pnlLeft.setOpaque(false);
        pnlRight.setOpaque(false);
        pnlCustom.setOpaque(false);
        scrCustom.setOpaque(false);
        scrCustom.setBorder(null);
        scrCustom.getVerticalScrollBar().setUnitIncrement(16);
        scrCustom.getViewport().setOpaque(false);
        scrCustom.getViewport().add(pnlCustom);

        lblTitle.setForeground(Color.white);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(skin.getFont().deriveFont(Font.BOLD, 30));

        lblStats.setForeground(Color.white);
        lblStats.setHorizontalAlignment(SwingConstants.CENTER);
        lblStats.setFont(skin.getFont().deriveFont(Font.PLAIN, 26));

        btnContinue.setText(ForgeProps.getLocalized(WinLoseText.CONTINUE));
        btnContinue.setFont(skin.getFont(22));
        btnRestart.setText(ForgeProps.getLocalized(WinLoseText.RESTART));
        btnRestart.setFont(skin.getFont(22));
        btnQuit.setText(ForgeProps.getLocalized(WinLoseText.QUIT));
        btnQuit.setFont(skin.getFont(22));

        // End game and set state of "continue" button
        PhaseHandler.setGameBegins(0);

        if (matchState.isMatchOver()) {
            this.getBtnContinue().setEnabled(false);
            this.getBtnQuit().grabFocus();
        }

        // Show Wins and Loses
        final Player human = AllZone.getHumanPlayer();
        final int humanWins = matchState.countGamesWonBy(human.getName());
        final int humanLosses = matchState.getGamesPlayedCount() - humanWins;

        lblStats.setText(ForgeProps.getLocalized(WinLoseText.WON) + humanWins
                + ForgeProps.getLocalized(WinLoseText.LOST) + humanLosses);

        // Show "You Won" or "You Lost"
        if (matchState.hasWonLastGame(human.getName())) {
            lblTitle.setText(ForgeProps.getLocalized(WinLoseText.WIN));
        } else {
            lblTitle.setText(ForgeProps.getLocalized(WinLoseText.LOSE));
        }

        // Assemble game log scroller.
        txtLog = new FTextArea();
        txtLog.setBorder(null);
        txtLog.setText(AllZone.getGameLog().getLogText());
        txtLog.setFont(skin.getFont(14));

        // Add all components accordingly.
        overlay.removeAll();
        overlay.setLayout(new MigLayout("insets 0, w 100%!, h 100%!"));
        pnlLeft.setLayout(new MigLayout("insets 0, wrap, align center"));
        pnlRight.setLayout(new MigLayout("insets 0, wrap"));
        pnlCustom.setLayout(new MigLayout("insets 0, wrap, align center"));

        final boolean customIsPopulated = control.populateCustomPanel();
        if (customIsPopulated) {
            overlay.add(pnlLeft, "w 40%!, h 100%!");
            overlay.add(pnlRight, "w 60%!, h 100%!");
            pnlRight.add(scrCustom, "w 100%!, h 100%!");
        }
        else {
            overlay.add(pnlLeft, "w 100%!, h 100%!");
        }

        pnlLeft.add(lblTitle, "w 90%!, h 50px!, gap 5% 0 20px 0");
        pnlLeft.add(lblStats, "w 90%!, h 50px!, gap 5% 0 20px 0");

        // A container must be made to ensure proper centering.
        final JPanel pnlButtons = new JPanel(new MigLayout("insets 0, wrap, ax center"));
        pnlButtons.setOpaque(false);

        final String constraints = "w 300px!, h 50px!, gap 0 0 20px 0";
        pnlButtons.add(btnContinue, constraints);
        pnlButtons.add(btnRestart, constraints);
        pnlButtons.add(btnQuit, constraints);
        pnlLeft.add(pnlButtons, "w 100%!");

        final JPanel pnlLog = new JPanel(new MigLayout("insets 0, wrap, ax center"));
        pnlLog.setOpaque(false);
        final FLabel lblLog = new FLabel("Game Log", SwingConstants.CENTER);
        lblLog.setFontScaleFactor(0.8);
        lblLog.setFontStyle(Font.BOLD);
        pnlLog.add(lblLog, "w 300px!, h 28px!, gap 0 0 20px 0");
        pnlLog.add(new FScrollPane(txtLog), "w 300px!, h 100px!, gap 0 0 10px 0");
        pnlLeft.add(pnlLog, "w 100%!");

        overlay.showOverlay();
    }

    /** @return {@link forge.view.toolbox.FButton} */
    public FButton getBtnContinue() {
        return this.btnContinue;
    }

    /** @return {@link forge.view.toolbox.FButton} */
    public FButton getBtnRestart() {
        return this.btnRestart;
    }

    /** @return {@link forge.view.toolbox.FButton} */
    public FButton getBtnQuit() {
        return this.btnQuit;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlCustom() {
        return this.pnlCustom;
    }
}
