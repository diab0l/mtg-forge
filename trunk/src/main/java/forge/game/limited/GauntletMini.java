/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game.limited;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import forge.FThreads;
import forge.Singletons;
import forge.deck.Deck;
import forge.game.GameType;
import forge.game.Match;
import forge.game.RegisteredPlayer;
import forge.gui.SOverlayUtils;
import forge.net.FServer;
import forge.net.Lobby;

/**
 * <p>
 * GauntletMini class.
 * </p>
 * 
 * @author Forge
 * @version $Id: GauntletMini.java $
 * @since 1.2.xx
 */
public class GauntletMini {

    private int rounds;
    private Deck humanDeck;
    private int currentRound;
    private int wins;
    private int losses;
    private boolean gauntletDraft; // Means: Draft game is in Gauntlet-mode, not a single match
    private GameType gauntletType;
    private List<RegisteredPlayer> aiOpponents = new ArrayList<RegisteredPlayer>();

    // private final String humanName;
    /**
     * TODO: Write javadoc for Constructor.
     */
    public void gauntletMini() {
        currentRound = 1;
        gauntletDraft = false;
        wins = 0;
        losses = 0;
        gauntletType = GameType.Sealed; // Assignable in launch();
    }

    /**
     * 
     * Set the number of rounds in the tournament.
     * 
     * @param gameRounds
     *          the number of rounds in the mini tournament
     */

    private void setRounds(int gameRounds) {
        rounds = gameRounds;
    }

    /**
     * 
     * Chooses the human deck for the tournament.
     * Note: The AI decks are connected to the human deck.
     * 
     * @param hDeck
     *          the human deck for this tournament
     */
    private void setHumanDeck(Deck hDeck) {
        humanDeck = hDeck;
    }

    /**
     * Resets the tournament.
     */
    public void resetCurrentRound() {
        wins = 0;
        losses = 0;
        currentRound = 1;
    }


    /**
     * Advances the tournament to the next round.
     */
    public void nextRound() {

        // System.out.println("Moving from round " + currentRound + " to round " +  currentRound + 1 + " of " + rounds);
        if (currentRound >= rounds) {
            currentRound = rounds - 1;
            return;
        }

        currentRound++;
        Singletons.getControl().endCurrentGame();
        startRound();
    }

    /**
     * 
     * Setup and launch the gauntlet.
     * Note: The AI decks are connected to the human deck.
     * 
     * @param gameRounds
     *          the number of rounds (opponent decks) in this tournament
     * @param hDeck
     *          the human deck for this tournament
     * @param gType
     *          game type (Sealed, Draft, Constructed...)
     */
    public void launch(int gameRounds, Deck hDeck, final GameType gType) {
        setHumanDeck(hDeck);
        setRounds(gameRounds);
        gauntletType = gType;
        List<Deck> aiDecks;
        if (gauntletType == GameType.Sealed) {
            aiDecks = Singletons.getModel().getDecks().getSealed().get(humanDeck.getName()).getAiDecks();
        }
        else if (gauntletType == GameType.Draft) {
            gauntletDraft = true;
            aiDecks = Singletons.getModel().getDecks().getDraft().get(humanDeck.getName()).getAiDecks();
        }
        else {
            throw new IllegalStateException("Cannot launch Gauntlet, game mode not implemented.");
        }
        aiOpponents.clear();
        for (int i = 0; i < Math.min(gameRounds, aiDecks.size()); i++) {

            aiOpponents.add(new RegisteredPlayer(aiDecks.get(i)));
        }

        resetCurrentRound();
        startRound();
    }

    /**
     * Starts the tournament.
     */
    private void startRound() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
        Lobby lobby = FServer.instance.getLobby();
        starter.add(RegisteredPlayer.fromDeck(humanDeck).setPlayer(lobby.getGuiPlayer()));
        starter.add(aiOpponents.get(currentRound - 1).setPlayer(lobby.getAiPlayer()));
        final Match mc = new Match(gauntletType, starter);
        FThreads.invokeInEdtLater(new Runnable(){
            @Override
            public void run() {
                Singletons.getControl().startGameWithUi(mc);
                SOverlayUtils.hideOverlay();
            }
        });
    }


    /**
     * Returns the total number of rounds in the tournament.
     * @return int, number of rounds in the tournament
     */
    public final int getRounds() {
        return rounds;
    }

    /**
     * Returns the number of the current round in the tournament.
     * @return int, number of rounds in the tournament
     */
    public final int getCurrentRound() {
        return currentRound;
    }

    /**
     * Adds a game win to the tournament statistics.
     */
    public void addWin() {
        wins++;
    }

    /**
     * Adds a game loss to the tournament statistics.
     */
    public void addLoss() {
        losses++;
    }

    /**
     * The total number of won games in this tournament.
     * @return int, number of wins
     */
    public final int getWins() {
        return wins;
    }

    /**
     * The total number of lost games in this tournament.
     * @return int, numer of losses
     */
    public final int getLosses() {
        return losses;
    }

    /**
     * Resets the gauntletDraft value.
     */
    public void resetGauntletDraft() {
        gauntletDraft = false;
    }

    /**
     * Draft mode status.
     * @return boolean, gauntletDraft
     */
    public final boolean isGauntletDraft() {
        return gauntletDraft;
    }

}

