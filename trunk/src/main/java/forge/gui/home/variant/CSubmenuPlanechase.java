package forge.gui.home.variant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicate;

import forge.Command;
import forge.FThreads;
import forge.Singletons;
import forge.control.FControl;
import forge.control.Lobby;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.deck.DeckgenUtil;
import forge.game.GameType;
import forge.game.MatchState;
import forge.game.PlayerStartConditions;
import forge.game.player.LobbyPlayer;
import forge.gui.GuiDialog;
import forge.gui.SOverlayUtils;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.controllers.CEditorVariant;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.FDeckChooser;
import forge.gui.toolbox.FList;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.util.Aggregates;

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
        // reinit deck lists and restore last selections (if any)
        for (FList deckList : view.getPlanarDeckLists()) {
            Vector<Object> listData = new Vector<Object>();
            listData.add("Random");
            listData.add("Generate");
            for (Deck planarDeck : Singletons.getModel().getDecks().getPlane()) {
                listData.add(planarDeck);
            }

            Object val = deckList.getSelectedValue();
            deckList.setListData(listData);
            if (null != val) {
                deckList.setSelectedValue(val, true);
            }
            
            if (-1 == deckList.getSelectedIndex()) {
                deckList.setSelectedIndex(0);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { view.getBtnStart().requestFocusInWindow(); }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.home.ICSubmenu#initialize()
     */
    @SuppressWarnings("serial")
    @Override
    public void initialize() {
        VSubmenuPlanechase.SINGLETON_INSTANCE.getLblEditor().setCommand(new Command() {
            @Override
            public void run() {
                Predicate<CardPrinted> predPlanes = new Predicate<CardPrinted>() {
                    @Override
                    public boolean apply(CardPrinted arg0) {
                        return arg0.getRules().getType().isPlane() || arg0.getRules().getType().isPhenomenon();
                    }
                };
                
                Singletons.getControl().changeState(FControl.Screens.DECK_EDITOR_CONSTRUCTED);
                CDeckEditorUI.SINGLETON_INSTANCE.setCurrentEditorController(
                        new CEditorVariant(Singletons.getModel().getDecks().getPlane(), predPlanes, EDocID.HOME_PLANECHASE));
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
        Lobby lobby = Singletons.getControl().getLobby();
        List<Pair<LobbyPlayer, PlayerStartConditions>> helper = new ArrayList<Pair<LobbyPlayer,PlayerStartConditions>>();
        List<Deck> playerDecks = new ArrayList<Deck>();
        for (int i = 0; i < view.getNumPlayers(); i++) {
            PlayerStartConditions d = view.getDeckChoosers().get(i).getDeck();

            if (d == null) {
                //ERROR!
                GuiDialog.message("No deck selected for player " + (i + 1));
                return;
            }
            playerDecks.add(d.getOriginalDeck());
            

            List<CardPrinted> planes = null;
            Object obj = view.getPlanarDeckLists().get(i).getSelectedValue();

            boolean useDefault = VSubmenuPlanechase.SINGLETON_INSTANCE.getCbUseDefaultPlanes().isSelected();
            useDefault &= playerDecks.get(i).has(DeckSection.Planes);

            System.out.println(useDefault);
            if (useDefault) {

                planes = playerDecks.get(i).get(DeckSection.Planes).toFlatList();
                System.out.println(planes.toString());

            } else {

                if (obj instanceof String) {
                    String sel = (String) obj;
                    if (sel.equals("Random")) {
                        if (view.getAllPlanarDecks().isEmpty()) {
                            //Generate if no constructed scheme decks are available
                            System.out.println("Generating planar deck - no others available");
                            planes = DeckgenUtil.generatePlanarDeck().toFlatList();
                        } else {
                            System.out.println("Using planar deck: " + Aggregates.random(view.getAllPlanarDecks()).getName());
                            planes = Aggregates.random(view.getAllPlanarDecks()).get(DeckSection.Planes).toFlatList();
                        }
                        
                    } else {

                        //Generate
                        planes = DeckgenUtil.generatePlanarDeck().toFlatList();
                    }
                } else {
                    planes = ((Deck) obj).get(DeckSection.Planes).toFlatList();
                }
            }
            if (planes == null) {
                //ERROR!
                GuiDialog.message("No planar deck selected for player" + (i+1) + "!");
                return;
            }

            if (useDefault) {

                GuiDialog.message("Player " + (i+1) + " will use a default planar deck.");
            }
            LobbyPlayer player = i == 0 ? lobby.getGuiPlayer() : lobby.getAiPlayer();
            helper.add(Pair.of(player, PlayerStartConditions.forPlanechase(playerDecks.get(i), planes)));
        }

        SOverlayUtils.startGameOverlay();
        SOverlayUtils.showOverlay();
                
        final MatchState mc = new MatchState(GameType.Planechase, helper);
        FThreads.invokeInEdtLater(new Runnable(){
            @Override
            public void run() {
                mc.startRound();
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
