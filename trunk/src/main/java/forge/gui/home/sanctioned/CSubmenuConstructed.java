package forge.gui.home.sanctioned;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import forge.Command;
import forge.FThreads;
import forge.Singletons;
import forge.control.Lobby;
import forge.game.GameType;
import forge.game.Match;
import forge.game.RegisteredPlayer;
import forge.game.player.LobbyPlayer;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;

/** 
 * Controls the constructed submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CSubmenuConstructed implements ICDoc {
    /** */
    SINGLETON_INSTANCE;
    private final VSubmenuConstructed view = VSubmenuConstructed.SINGLETON_INSTANCE;


    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { view.getBtnStart().requestFocusInWindow(); }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void initialize() {
        final ForgePreferences prefs = Singletons.getModel().getPreferences();
        view.getDcLeft().initialize();
        view.getDcRight().initialize();

        // Checkbox event handling
        view.getBtnStart().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                startGame(GameType.Constructed);
            }
        });

        // Checkbox event handling
        view.getCbSingletons().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                prefs.setPref(FPref.DECKGEN_SINGLETONS,
                        String.valueOf(view.getCbSingletons().isSelected()));
                prefs.save();
            }
        });

        view.getCbArtifacts().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                prefs.setPref(
                        FPref.DECKGEN_ARTIFACTS, String.valueOf(view.getCbArtifacts().isSelected()));
                prefs.save();
            }
        });

        view.getCbRemoveSmall().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                prefs.setPref(FPref.DECKGEN_NOSMALL, String.valueOf(view.getCbRemoveSmall().isSelected()));
                prefs.save();
            }
        });

        // Pre-select checkboxes
        view.getCbSingletons().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_SINGLETONS));
        view.getCbArtifacts().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
        view.getCbRemoveSmall().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_NOSMALL));
    }

    /**
     *
     * @param gameType
     */
    private void startGame(final GameType gameType) {
        RegisteredPlayer pscLeft = view.getDcLeft().getDeck();
        RegisteredPlayer pscRight = view.getDcRight().getDeck();
        
        if (pscLeft == null || pscRight == null) {
            JOptionPane.showMessageDialog(null, "Please specify an AI deck and Player deck first.");
            return; 
        }
        
        String humanDeckErrorMessage = gameType.getDecksFormat().getDeckConformanceProblem(pscRight.getOriginalDeck());
        if (null != humanDeckErrorMessage) {
            JOptionPane.showMessageDialog(null, "Right-side deck " + humanDeckErrorMessage, "Invalid deck", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String aiDeckErrorMessage = gameType.getDecksFormat().getDeckConformanceProblem(pscLeft.getOriginalDeck());
        if (null != aiDeckErrorMessage) {
            JOptionPane.showMessageDialog(null, "Left-side deck " + aiDeckErrorMessage, "Invalid deck", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Lobby lobby = Singletons.getControl().getLobby();
        LobbyPlayer rightPlayer = view.isRightPlayerAi() ? lobby.getAiPlayer() : lobby.getGuiPlayer();
        LobbyPlayer leftPlayer = view.isLeftPlayerAi() ? lobby.getAiPlayer() : lobby.getGuiPlayer();
        
        List<RegisteredPlayer> players = new ArrayList<RegisteredPlayer>();
        players.add(pscRight.setPlayer(rightPlayer));
        players.add(pscLeft.setPlayer(leftPlayer));
        final Match mc = new Match(gameType, players);
        
        SOverlayUtils.startGameOverlay();
        SOverlayUtils.showOverlay();

        FThreads.invokeInEdtLater(new Runnable(){
            @Override
            public void run() {
                Singletons.getControl().startGameWithUi(mc);
                SOverlayUtils.hideOverlay();
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }
}
