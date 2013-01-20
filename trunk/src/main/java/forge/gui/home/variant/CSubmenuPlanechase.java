package forge.gui.home.variant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.google.common.collect.Iterables;

import forge.Command;
import forge.GameActionUtil;
import forge.Singletons;
import forge.control.FControl;
import forge.control.Lobby;
import forge.deck.Deck;
import forge.deck.DeckgenUtil;
import forge.game.GameType;
import forge.game.MatchController;
import forge.game.MatchStartHelper;
import forge.game.player.LobbyPlayer;
import forge.game.player.PlayerType;
import forge.gui.SOverlayUtils;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.controllers.CEditorScheme;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.FDeckChooser;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;

/** 
 * Controls the constructed submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CSubmenuPlanechase implements ICDoc {
    /** */
    SINGLETON_INSTANCE;
    private final VSubmenuPlanechase view = VSubmenuPlanechase.SINGLETON_INSTANCE;


    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void update() {
        // Nothing to see here...
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @Override
    public void initialize() {

        VSubmenuPlanechase.SINGLETON_INSTANCE.getLblEditor().setCommand(new Command() {
            @Override
            public void execute() {
                //TODO:Enter Planar deck editor here!
                //CDeckEditorUI.SINGLETON_INSTANCE.setCurrentEditorController(new CEditorScheme());
                //FControl.SINGLETON_INSTANCE.changeState(FControl.DECK_EDITOR_CONSTRUCTED);
            }
        });

        final ForgePreferences prefs = Singletons.getModel().getPreferences();
        for (FDeckChooser fdc : view.getDeckChoosers()) {
            fdc.initialize();
        }

        // Checkbox event handling
        view.getBtnStart().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                startGame();
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
                prefs.setPref(
                        FPref.DECKGEN_NOSMALL, String.valueOf(view.getCbRemoveSmall().isSelected()));
                prefs.save();
            }
        });

        // Pre-select checkboxes
        view.getCbSingletons().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_SINGLETONS));
        view.getCbArtifacts().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
        view.getCbRemoveSmall().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_NOSMALL));
    }


    /** @param lists0 &emsp; {@link java.util.List}<{@link javax.swing.JList}> */
    private void startGame() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            public Object doInBackground() {
                Random rnd = new Random();

                boolean usedDefaults = false;

                Lobby lobby = Singletons.getControl().getLobby();
                MatchStartHelper helper = new MatchStartHelper();
                List<Deck> playerDecks = new ArrayList<Deck>();
                for (int i = 0; i < view.getNumPlayers(); i++) {
                    Deck d = view.getDeckChoosers().get(i).getDeck();

                    if (d == null) {
                        //ERROR!
                        GameActionUtil.showInfoDialg("No deck selected for player " + (i + 1));
                        return null;
                    }
                    playerDecks.add(d);
                    
    
                    List<CardPrinted> planes = null;
                    Object obj = view.getPlanarDeckLists().get(i).getSelectedValue();
    
                    boolean useDefault = VSubmenuPlanechase.SINGLETON_INSTANCE.getCbUseDefaultPlanes().isSelected();
                    useDefault &= !playerDecks.get(i).getSideboard().isEmpty();
    
                    System.out.println(useDefault);
                    if (useDefault) {
    
                        planes = playerDecks.get(i).getSideboard().toFlatList();
                        System.out.println(planes.toString());
                        usedDefaults = true;
    
                    } else {
    
                        if (obj instanceof String) {
                            String sel = (String) obj;
                            if (sel.equals("Random")) {
    
                                planes = Iterables.get(view.getAllPlanarDecks(), rnd.nextInt(Iterables.size(view.getAllPlanarDecks()))).getSideboard().toFlatList();
                            } else {
    
                                //Generate
                                planes = DeckgenUtil.generateSchemeDeck().getSideboard().toFlatList();
                            }
                        } else {
                            planes = ((Deck) obj).getSideboard().toFlatList();
                        }
                    }
                    if (planes == null) {
                        //ERROR!
                        GameActionUtil.showInfoDialg("No planar deck selected for player" + (i+1) + "!");
                        return null;
                    }
    
                    if (usedDefaults) {
    
                        GameActionUtil.showInfoDialg("Player " + (i+1) + " will use a default planar deck.");
                    }
                    
                    LobbyPlayer player = lobby.findLocalPlayer(i == 0 ? PlayerType.HUMAN : PlayerType.COMPUTER);

                    helper.addPlanechasePlayer(player, playerDecks.get(i), planes);
                }
                
                MatchController mc = Singletons.getModel().getMatch();
                mc.initMatch(GameType.Planechase, helper.getPlayerMap());
                mc.startRound();

                return null;
            }

            @Override
            public void done() {
                SOverlayUtils.hideOverlay();
            }
        };
        worker.execute();
    }


    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }
}
