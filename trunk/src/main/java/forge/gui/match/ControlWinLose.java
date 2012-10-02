package forge.gui.match;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

import forge.AllZone;
import forge.Card;

import forge.Constant;
import forge.Singletons;
import forge.control.FControl;
import forge.deck.Deck;
import forge.game.GameNew;
import forge.game.GameType;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.SOverlayUtils;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences.FPref;

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
                ((JButton) e.getSource()).setEnabled(false);
            }
        });
    }

    /** Action performed when "continue" button is pressed in default win/lose UI. */
    public void actionOnContinue() {
        SOverlayUtils.hideOverlay();
        startNextRound();
    }

    /** Action performed when "restart" button is pressed in default win/lose UI. */
    public void actionOnRestart() {
        Singletons.getModel().getMatchState().reset();
        SOverlayUtils.hideOverlay();
        startNextRound();
    }

    /** Action performed when "quit" button is pressed in default win/lose UI. */
    public void actionOnQuit() {
        Singletons.getModel().getMatchState().reset();
        Singletons.getModel().savePrefs();
        Singletons.getControl().changeState(FControl.HOME_SCREEN);
        SOverlayUtils.hideOverlay();
    }

    /**
     * Either continues or restarts a current game. May be overridden for use
     * with other game modes.
     */
    public void startNextRound() {
        boolean isAnte = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE);
        GameType gameType = Constant.Runtime.getGameType();

        Deck hDeck = AllZone.getHumanPlayer().getDeck();
        Deck cDeck = AllZone.getComputerPlayer().getDeck();

        //This is called from QuestWinLoseHandler also.  If we're in a quest, this is already handled elsewhere
        if (isAnte && !gameType.equals(GameType.Quest)) {
            if (Singletons.getModel().getMatchState().hasWonLastGame(AllZone.getHumanPlayer().getName())) {
                List<Card> compAntes = AllZone.getComputerPlayer().getCardsIn(ZoneType.Ante);

                //remove compy's ante cards form his deck
                for (Card c : compAntes) {
                    CardPrinted toRemove = CardDb.instance().getCard(c);
                    cDeck.getMain().remove(toRemove);
                }

                List<Card> o = GuiChoose.noneOrMany("Select cards to add to your deck", compAntes);
                if (null != o) {
                    for (Card c : o) {
                        hDeck.getMain().add(c);
                    }
                }

            } else { //compy won
                List<Card> humanAntes = AllZone.getHumanPlayer().getCardsIn(ZoneType.Ante);

                //remove humans ante cards form his deck
                for (Card c : humanAntes) {
                    CardPrinted toRemove = CardDb.instance().getCard(c);
                    hDeck.getMain().remove(toRemove);
                }
                AllZone.getHumanPlayer().setDeck(hDeck);
            }
        }
        Singletons.getModel().savePrefs();
        GameNew.newGame(hDeck, cDeck);
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
