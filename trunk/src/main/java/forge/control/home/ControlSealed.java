package forge.control.home;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.slightlymagic.braids.util.UtilFunctions;

import forge.AllZone;
import forge.Constant;
import forge.PlayerType;
import forge.control.ControlAllUI;
import forge.deck.Deck;
import forge.deck.DeckManager;
import forge.game.GameType;
import forge.game.limited.SealedDeck;
import forge.gui.GuiUtils;
import forge.item.CardPrinted;
import forge.item.ItemPool;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.view.GuiTopLevel;
import forge.view.home.ViewSealed;

/** 
 * Controls behavior of swing components in "sealed" mode menu.
 *
 */
public class ControlSealed {
    private ViewSealed view;
    private DeckManager deckManager;

    /**
     * Controls behavior of swing components in "sealed" mode menu.
     * 
     * @param v0 &emsp; ViewSealed
     */
    public ControlSealed(ViewSealed v0) {
        view = v0;
        deckManager = AllZone.getDeckManager();
    }

    /** */
    public void start() {
        Deck human = view.getLstHumanDecks().getSelectedDeck();
        if (human == null) {
            JOptionPane.showMessageDialog(null,
                    "Please build and/or select a deck for yourself.",
                    "No deck", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ai = view.getLstAIDecks().getSelectedValue().toString();
        if (ai == null) {
            JOptionPane.showMessageDialog(null,
                    "Please build and/or select a deck for the computer.",
                    "No deck", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Constant.Runtime.HUMAN_DECK[0] = human;
        Constant.Runtime.COMPUTER_DECK[0] = deckManager.getDeck(ai);

        ControlAllUI c = ((GuiTopLevel) AllZone.getDisplay()).getController();
        c.changeState(1);
        c.getMatchController().initMatch();
        AllZone.getGameAction().newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
    }

    /** */
    public void updateDeckLists() {
        List<String> aiNames = new ArrayList<String>();
        List<Deck> humanDecks = new ArrayList<Deck>();

        for (Deck d : deckManager.getDecks()) {
            if (d.getDeckType().equals(GameType.Sealed)) {
                if (d.getPlayerType() == PlayerType.COMPUTER) {
                    aiNames.add(d.getName());
                }
                else {
                    humanDecks.add(d);
                }
            }
        }

        view.getLstHumanDecks().setDecks(humanDecks.toArray(new Deck[0]));
        view.getLstAIDecks().setListData(aiNames.toArray(new String[0]));
    }

    /** */
    public void setupSealed() {
        Deck deck = new Deck(GameType.Sealed);

        // ReadBoosterPack booster = new ReadBoosterPack();
        // CardList pack = booster.getBoosterPack5();

        ArrayList<String> sealedTypes = new ArrayList<String>();
        sealedTypes.add("Full Cardpool");
        sealedTypes.add("Block / Set");
        sealedTypes.add("Custom");

        final String prompt = "Choose Sealed Deck Format:";
        final Object o = GuiUtils.getChoice(prompt, sealedTypes.toArray());

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
            throw new IllegalStateException("choice <<" + UtilFunctions.safeToString(o)
                    + ">> does not equal any of the sealedTypes.");
        }

        final ItemPool<CardPrinted> sDeck = sd.getCardpool();

        deck.addSideboard(sDeck);

        for (final String element : Constant.Color.BASIC_LANDS) {
            for (int j = 0; j < 18; j++) {
                deck.addSideboard(element + "|" + sd.getLandSetCode()[0]);
            }
        }

        final String sDeckName = JOptionPane.showInputDialog(null,
                ForgeProps.getLocalized(NewConstants.Lang.OldGuiNewGame.NewGameText.SAVE_SEALED_MSG),
                ForgeProps.getLocalized(NewConstants.Lang.OldGuiNewGame.NewGameText.SAVE_SEALED_TTL),
                JOptionPane.QUESTION_MESSAGE);

        deck.setName(sDeckName);
        deck.setPlayerType(PlayerType.HUMAN);

        Constant.Runtime.HUMAN_DECK[0] = deck;
        Constant.Runtime.setGameType(GameType.Sealed);

        Deck aiDeck = sd.buildAIDeck(sDeck.toForgeCardList());
        //final Deck aiDeck = sd.buildAIDeck(sd.getCardpool().toForgeCardList());
        // AI will use different cardpool
        aiDeck.setName("AI_" + sDeckName);
        aiDeck.setPlayerType(PlayerType.COMPUTER);
        deckManager.addDeck(aiDeck);
        DeckManager.writeDeck(aiDeck, DeckManager.makeFileName(aiDeck));
        //this.updateDeckComboBoxes();

        view.getParentView().getUtilitiesController().showDeckEditor(GameType.Sealed, deck);

        Constant.Runtime.COMPUTER_DECK[0] = aiDeck;
    }
}
