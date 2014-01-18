package forge.gui.home.gauntlet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import forge.Command;
import forge.Singletons;
import forge.deck.Deck;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gauntlet.GauntletData;
import forge.gauntlet.GauntletIO;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.net.FServer;
import forge.net.Lobby;

/** 
 * Controls the "gauntlet contests" submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */

@SuppressWarnings("serial")
public enum CSubmenuGauntletContests implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private final VSubmenuGauntletContests view = VSubmenuGauntletContests.SINGLETON_INSTANCE;

    private final ActionListener actStartGame = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            startGame();
        }
    };

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
        view.getBtnStart().addActionListener(actStartGame);

        view.getLstDecks().initialize();
        updateData();

        view.getGauntletLister().setSelectedIndex(0);
    }


    private void updateData() {
        final File[] files = GauntletIO.getGauntletFilesLocked();
        final List<GauntletData> data = new ArrayList<GauntletData>();
        for (final File f : files) {
            data.add(GauntletIO.loadGauntlet(f));
        }

        view.getGauntletLister().setGauntlets(data);
        view.getGauntletLister().setSelectedIndex(0);
    }

    /** */
    private void startGame() {
        final GauntletData gd = view.getGauntletLister().getSelectedGauntlet();
        final Deck userDeck;

        if (gd.getUserDeck() != null) {
            userDeck = gd.getUserDeck();
        }
        else {
            userDeck = view.getLstDecks().getPlayer().getDeck();
            gd.setUserDeck(userDeck);
        }

        gd.stamp();
        Singletons.getModel().setGauntletData(gd);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        Deck aiDeck = gd.getDecks().get(gd.getCompleted());

        List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
        Lobby lobby = FServer.instance.getLobby();

        starter.add(new RegisteredPlayer(gd.getUserDeck()).setPlayer(lobby.getGuiPlayer()));
        starter.add(new RegisteredPlayer(aiDeck).setPlayer(lobby.getAiPlayer()));

        Singletons.getControl().startMatch(GameType.Gauntlet, starter);

    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return new Command() {
            @Override
            public void run() {
                updateData();
            }
        };
    }
}
