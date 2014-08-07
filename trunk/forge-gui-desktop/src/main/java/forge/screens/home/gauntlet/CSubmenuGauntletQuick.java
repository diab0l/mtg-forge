package forge.screens.home.gauntlet;

import forge.UiCommand;
import forge.Singletons;
import forge.control.FControl;
import forge.deck.Deck;
import forge.deck.DeckgenUtil;
import forge.deck.DeckgenUtil.DeckTypes;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gauntlet.GauntletData;
import forge.gauntlet.GauntletIO;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.model.FModel;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        updateData();

        view.getGauntletLister().setSelectedIndex(0);
        view.getLstDecks().initialize();
    }


    private void updateData() {
        final File[] files = GauntletIO.getGauntletFilesUnlocked();
        final List<GauntletData> data = new ArrayList<GauntletData>();

        for (final File f : files) {
            data.add(GauntletIO.loadGauntlet(f));
        }

        view.getGauntletLister().setGauntlets(data);
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
        final File[] arrFiles = GauntletIO.getGauntletFilesQuick();
        final Set<String> setNames = new HashSet<String>();
        for (File f : arrFiles) { setNames.add(f.getName()); }

        int num = 1;
        while (setNames.contains(GauntletIO.PREFIX_QUICK + num + GauntletIO.SUFFIX_DATA)) { num++; }
        FModel.getGauntletData().setName(GauntletIO.PREFIX_QUICK + num);

        // Pull user deck
        final Deck userDeck = view.getLstDecks().getPlayer().getDeck(); 

        // Generate gauntlet decks
        final int numOpponents = view.getSliOpponents().getValue();
        final List<DeckTypes> lstDecktypes = new ArrayList<DeckTypes>();
        final List<String> lstEventNames = new ArrayList<String>();
        final List<Deck> lstGauntletDecks = new ArrayList<Deck>();
        Deck tempDeck;
        int randType;

        if (view.getBoxColorDecks().isSelected()) { lstDecktypes.add(DeckTypes.COLORS); }
        if (view.getBoxThemeDecks().isSelected()) { lstDecktypes.add(DeckTypes.THEMES); }
        if (view.getBoxUserDecks().isSelected()) { lstDecktypes.add(DeckTypes.CUSTOM); }
        if (view.getBoxQuestDecks().isSelected()) { lstDecktypes.add(DeckTypes.QUESTEVENTS); }

        for (int i = 0; i < numOpponents; i++) {
            randType = (int) Math.round(Math.random() * (lstDecktypes.size() - 1));
            if (lstDecktypes.get(randType).equals(DeckTypes.COLORS)) {
                tempDeck = DeckgenUtil.getRandomColorDeck(true);
                lstEventNames.add("Random colors deck");
            }
            else if (lstDecktypes.get(randType).equals(DeckTypes.CUSTOM)) {
                tempDeck =  DeckgenUtil.getRandomCustomDeck();
                lstEventNames.add(tempDeck.getName());
            }
            else {
                tempDeck =  DeckgenUtil.getRandomQuestDeck();
                lstEventNames.add(tempDeck.getName());
            }

            lstGauntletDecks.add(tempDeck);
        }

        final GauntletData gd = FModel.getGauntletData();
        gd.setDecks(lstGauntletDecks);
        gd.setEventNames(lstEventNames);
        gd.setUserDeck(userDeck);

        // Reset all variable fields to 0, stamps and saves automatically.
        gd.reset();

        final Deck aiDeck = gd.getDecks().get(gd.getCompleted());

        List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
        FControl fc = Singletons.getControl();
        starter.add(new RegisteredPlayer(gd.getUserDeck()).setPlayer(fc.getGuiPlayer()));
        starter.add(new RegisteredPlayer(aiDeck).setPlayer(fc.getAiPlayer()));
        
        fc.startMatch(GameType.Gauntlet, starter);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }
}
