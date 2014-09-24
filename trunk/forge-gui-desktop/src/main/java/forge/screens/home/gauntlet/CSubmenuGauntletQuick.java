package forge.screens.home.gauntlet;

import forge.UiCommand;
import forge.deck.DeckType;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gauntlet.GauntletData;
import forge.gauntlet.GauntletUtil;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.match.MatchUtil;
import forge.player.GamePlayerUtil;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** 
 * Controls the "quick gauntlet" submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */

public enum CSubmenuGauntletQuick implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private final ActionListener actStartGame = new ActionListener() { @Override
        public void actionPerformed(ActionEvent arg0) { startGame(); } };

    private final VSubmenuGauntletQuick view = VSubmenuGauntletQuick.SINGLETON_INSTANCE;

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
    }

    private void startGame() {
        // Start game overlay
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        // Find appropriate filename for new save, create and set new save file.
        List<DeckType> allowedDeckTypes = new ArrayList<DeckType>();
        if (view.getBoxColorDecks().isSelected()) { allowedDeckTypes.add(DeckType.COLOR_DECK); }
        if (view.getBoxThemeDecks().isSelected()) { allowedDeckTypes.add(DeckType.THEME_DECK); }
        if (view.getBoxUserDecks().isSelected()) { allowedDeckTypes.add(DeckType.CUSTOM_DECK); }
        if (view.getBoxQuestDecks().isSelected()) { allowedDeckTypes.add(DeckType.QUEST_OPPONENT_DECK); }
        if (view.getBoxPreconDecks().isSelected()) { allowedDeckTypes.add(DeckType.PRECONSTRUCTED_DECK); }

        final GauntletData gd = GauntletUtil.createQuickGauntlet(view.getLstDecks().getPlayer().getDeck(), view.getSliOpponents().getValue(), allowedDeckTypes);

        List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
        starter.add(new RegisteredPlayer(gd.getUserDeck()).setPlayer(GamePlayerUtil.getGuiPlayer()));
        starter.add(new RegisteredPlayer(gd.getDecks().get(gd.getCompleted())).setPlayer(GamePlayerUtil.createAiPlayer()));

        MatchUtil.startMatch(GameType.Gauntlet, starter);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }
}
