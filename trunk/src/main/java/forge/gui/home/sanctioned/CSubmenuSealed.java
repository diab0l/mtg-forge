package forge.gui.home.sanctioned;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;

import forge.Command;
import forge.Constant;
import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.deck.DeckGroup;
import forge.game.GameNew;
import forge.game.limited.SealedDeck;
import forge.gui.GuiUtils;
import forge.gui.SOverlayUtils;
import forge.gui.deckeditor.DeckEditorBase;
import forge.gui.deckeditor.DeckEditorLimited;
import forge.gui.home.ICSubmenu;
import forge.gui.toolbox.FSkin;
import forge.item.CardPrinted;
import forge.item.ItemPool;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.util.TextUtil;

/** 
 * Controls the sealed submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
@SuppressWarnings("serial")
public enum CSubmenuSealed implements ICSubmenu {
    /** */
    SINGLETON_INSTANCE;

    private Map<String, Deck> aiDecks;

    private final Command cmdExit = new Command() {
        @Override
        public void execute() {
            update();
            SOverlayUtils.hideOverlay();
        }
    };

    private final Command cmdDeckSelect = new Command() {
        @Override
        public void execute() {
            VSubmenuSealed.SINGLETON_INSTANCE.getBtnStart().setEnabled(true);
        }
    };

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void initialize() {
        final VSubmenuSealed view = VSubmenuSealed.SINGLETON_INSTANCE;

        view.populate();
        CSubmenuSealed.SINGLETON_INSTANCE.update();

        VSubmenuSealed.SINGLETON_INSTANCE.getLstDecks().setExitCommand(cmdExit);
        VSubmenuSealed.SINGLETON_INSTANCE.getLstDecks().setSelectCommand(cmdDeckSelect);

        VSubmenuSealed.SINGLETON_INSTANCE.getBtnBuildDeck().addMouseListener(
                new MouseAdapter() { @Override
                    public void mousePressed(final MouseEvent e) { setupSealed(); } });

        VSubmenuSealed.SINGLETON_INSTANCE.getBtnStart().addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                startGame();
                            }
                        });
                    }
                });

        VSubmenuSealed.SINGLETON_INSTANCE.getBtnDirections().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                VSubmenuSealed.SINGLETON_INSTANCE.showDirections();
            }
            @Override
            public void mouseEntered(final MouseEvent e) {
                VSubmenuSealed.SINGLETON_INSTANCE.getBtnDirections().setForeground(FSkin.getColor(FSkin.Colors.CLR_HOVER));
            }
            @Override
            public void mouseExited(final MouseEvent e) {
                VSubmenuSealed.SINGLETON_INSTANCE.getBtnDirections().setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#getCommand()
     */
    @Override
    public Command getMenuCommand() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        final List<Deck> humanDecks = new ArrayList<Deck>();
        aiDecks = new HashMap<String, Deck>();

        // Since AI decks are tied directly to the human choice,
        // they're just mapped in a parallel map and grabbed when the game starts.
        for (final DeckGroup d : Singletons.getModel().getDecks().getSealed()) {
            aiDecks.put(d.getName(), d.getAiDecks().get(0));
            humanDecks.add(d.getHumanDeck());
        }

        VSubmenuSealed.SINGLETON_INSTANCE.getLstDecks().setDecks(humanDecks);
    }

    private void startGame() {
        final Deck human = VSubmenuSealed.SINGLETON_INSTANCE.getLstDecks().getSelectedDeck();

        if (human == null) {
            JOptionPane.showMessageDialog(null,
                    "Please build and/or select a deck for yourself.",
                    "No deck", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
                Constant.Runtime.HUMAN_DECK[0] = human;
                Constant.Runtime.COMPUTER_DECK[0] = Singletons.getModel().getDecks().getSealed().get(human.getName()).getAiDecks().get(0);

                GameNew.newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
                return null;
            }

            @Override
            public void done() {
                SOverlayUtils.hideOverlay();
            }
        };
        worker.execute();
    }

    /** */
    @SuppressWarnings("unchecked")
    private <T extends DeckBase> void setupSealed() {
        final ArrayList<String> sealedTypes = new ArrayList<String>();
        sealedTypes.add("Full Cardpool");
        sealedTypes.add("Block / Set");
        sealedTypes.add("Custom");

        final String prompt = "Choose Sealed Deck Format:";
        final Object o = GuiUtils.chooseOne(prompt, sealedTypes.toArray());

        SealedDeck sd = null;

        if (o.toString().equals(sealedTypes.get(0))) {
            sd = new SealedDeck("Full");
        }

        else if (o.toString().equals(sealedTypes.get(1))) {
            sd = new SealedDeck("Block");
        }

        else if (o.toString().equals(sealedTypes.get(2))) {
            sd = new SealedDeck("Custom");
        }
        else {
            throw new IllegalStateException("choice <<" + TextUtil.safeToString(o)
                    + ">> does not equal any of the sealedTypes.");
        }

        if (sd.getCardpool().isEmpty()) {
            return;
        }

        final String sDeckName = JOptionPane.showInputDialog(null,
                ForgeProps.getLocalized(NewConstants.Lang.OldGuiNewGame.NewGameText.SAVE_SEALED_MSG),
                ForgeProps.getLocalized(NewConstants.Lang.OldGuiNewGame.NewGameText.SAVE_SEALED_TTL),
                JOptionPane.QUESTION_MESSAGE);

        if (StringUtils.isBlank(sDeckName)) {
            return;
        }

        // May check for name uniqueness here

        final ItemPool<CardPrinted> sDeck = sd.getCardpool();

        final Deck deck = new Deck(sDeckName);
        deck.getSideboard().addAll(sDeck);

        for (final String element : Constant.Color.BASIC_LANDS) {
            deck.getSideboard().add(element, sd.getLandSetCode()[0], 18);
        }

        final DeckGroup sealed = new DeckGroup(sDeckName);
        sealed.setHumanDeck(deck);
        sealed.addAiDeck(sd.buildAIDeck(sDeck.toForgeCardList()));
        Singletons.getModel().getDecks().getSealed().add(sealed);

        final DeckEditorBase<?, T> editor = (DeckEditorBase<?, T>) new DeckEditorLimited(
                Singletons.getView().getFrame(),
                Singletons.getModel().getDecks().getSealed());

        editor.show(cmdExit);
        editor.getController().setModel((T) sealed);
        editor.setAlwaysOnTop(true);
        editor.setVisible(true);
    }
}
