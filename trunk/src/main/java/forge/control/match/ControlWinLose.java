package forge.control.match;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import forge.AllZone;
import forge.Card;
import forge.CardList;
import forge.Constant;
import forge.Constant.Zone;
import forge.Singletons;
import forge.control.FControl;
import forge.deck.Deck;
import forge.game.GameType;
import forge.gui.GuiUtils;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences.FPref;
import forge.view.match.ViewWinLose;

/** 
 * Default controller for a ViewWinLose object. This class can
 * be extended for various game modes to populate the custom
 * panel in the win/lose screen.
 *
 */
public class ControlWinLose {
    private final ViewWinLose view;

    /** @param v &emsp; ViewWinLose */
    public ControlWinLose(final ViewWinLose v) {
        this.view = v;
        addListeners();
    }

    /** */
    public void addListeners() {
        view.getBtnContinue().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionOnContinue();
            }
        });

        view.getBtnRestart().addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionOnRestart();
            }
        });

        view.getBtnQuit().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                actionOnQuit();
            }
        });
    }

    /** Action performed when "continue" button is pressed in default win/lose UI. */
    public void actionOnContinue() {
        GuiUtils.closeOverlay();
        startNextRound();
    }

    /** Action performed when "restart" button is pressed in default win/lose UI. */
    public void actionOnRestart() {
        AllZone.getMatchState().reset();
        GuiUtils.closeOverlay();
        startNextRound();
    }

    /** Action performed when "quit" button is pressed in default win/lose UI. */
    public void actionOnQuit() {
        AllZone.getMatchState().reset();
        Singletons.getModel().savePrefs();
        Singletons.getControl().getMatchControl().deinitMatch();
        Singletons.getControl().changeState(FControl.HOME_SCREEN);
        GuiUtils.closeOverlay();
    }

    /**
     * Either continues or restarts a current game. May be overridden for use
     * with other game modes.
     */
    public void startNextRound() {
        boolean isAnte = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE);
        GameType gameType = Constant.Runtime.getGameType();

        //This is called from QuestWinLoseHandler also.  If we're in a quest, this is already handled elsewhere
        if (isAnte && !gameType.equals(GameType.Quest)) {
            Deck hDeck = Constant.Runtime.HUMAN_DECK[0];
            Deck cDeck = Constant.Runtime.COMPUTER_DECK[0];
            if (AllZone.getMatchState().hasWonLastGame(AllZone.getHumanPlayer().getName())) {
                CardList compAntes = AllZone.getComputerPlayer().getCardsIn(Zone.Ante);

                //remove compy's ante cards form his deck
                for (Card c : compAntes) {
                    CardPrinted toRemove = CardDb.instance().getCard(c);
                    cDeck.getMain().remove(toRemove);
                }

                Constant.Runtime.COMPUTER_DECK[0] = cDeck;

                List<Card> o = GuiUtils.getChoicesOptional("Select cards to add to your deck", compAntes.toArray());
                if (null != o) {
                    for (Card c : o) {
                        hDeck.getMain().add(c);
                    }
                }

            } else { //compy won
                CardList humanAntes = AllZone.getHumanPlayer().getCardsIn(Zone.Ante);

                //remove compy's ante cards form his deck
                for (Card c : humanAntes) {
                    CardPrinted toRemove = CardDb.instance().getCard(c);
                    hDeck.getMain().remove(toRemove);
                }
                Constant.Runtime.HUMAN_DECK[0] = hDeck;
            }
        }
        Singletons.getModel().savePrefs();
        AllZone.getGameAction().newGame(Constant.Runtime.HUMAN_DECK[0], Constant.Runtime.COMPUTER_DECK[0]);
    }

    /**
     * <p>
     * populateCustomPanel.
     * </p>
     * May be overridden as required by controllers for various game modes
     * to show custom information in center panel. Default configuration is empty.
     * 
     * @return boolean, panel has contents or not.
     */
    public boolean populateCustomPanel() {
        return false;
    }

    /** @return ViewWinLose object this controller is in charge of */
    public ViewWinLose getView() {
        return this.view;
    }
}
