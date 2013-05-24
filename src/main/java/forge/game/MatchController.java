package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import forge.Constant.Preferences;
import forge.FThreads;
import forge.Singletons;
import forge.control.FControl;
import forge.error.BugReporter;
import forge.game.event.DuelOutcomeEvent;
import forge.game.event.FlipCoinEvent;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerStatistics;
import forge.gui.framework.EDocID;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.VMatchUI;
import forge.gui.match.ViewWinLose;
import forge.gui.match.controllers.CCombat;
import forge.gui.match.controllers.CDock;
import forge.gui.match.controllers.CLog;
import forge.gui.match.controllers.CMessage;
import forge.gui.match.controllers.CStack;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VAntes;
import forge.properties.ForgePreferences.FPref;
import forge.util.MyRandom;

/**
 * TODO: Write javadoc for this type.
 * 
 */

public class MatchController {

    private final List<Pair<LobbyPlayer, PlayerStartConditions>> players;
    private final GameType gameType;

    private int gamesPerMatch = 3;
    private int gamesToWinMatch = 2;
    
    private boolean useAnte = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE);

    private GameState currentGame = null;

    private final List<GameOutcome> gamesPlayed = new ArrayList<GameOutcome>();
    private final List<GameOutcome> gamesPlayedRo;

    /**
     * This should become constructor once.
     */
    public MatchController(GameType type, List<Pair<LobbyPlayer, PlayerStartConditions>> players0) {
        gamesPlayedRo = Collections.unmodifiableList(gamesPlayed);
        players = Collections.unmodifiableList(Lists.newArrayList(players0));
        gameType = type;
    }
    
    public MatchController(GameType type, List<Pair<LobbyPlayer, PlayerStartConditions>> players0, Boolean forceAnte) {
        this(type, players0);
        if( forceAnte != null )
            this.useAnte = forceAnte.booleanValue();
    }

    /**
     * Gets the games played.
     * 
     * @return the games played
     */
    public final List<GameOutcome> getPlayedGames() {
        return this.gamesPlayedRo;
    }

    /** @return int */
    public int getGamesPerMatch() {
        return gamesPerMatch;
    }

    /** @return int */
    public int getGamesToWinMatch() {
        return gamesToWinMatch;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param reason
     * 
     * @param game
     */
    public void addGamePlayed(GameEndReason reason, GameState game) {
        if (!game.isGameOver()) {
            throw new RuntimeException("Game is not over yet.");
        }

        final GameOutcome result = new GameOutcome(reason, game.getRegisteredPlayers());
        result.setTurnsPlayed(game.getPhaseHandler().getTurn());
        gamesPlayed.add(result);

        // Play the win/lose sound
        game.getEvents().post(new DuelOutcomeEvent(result.getWinner()));
        game.getGameLog().add("Final", result.getWinner() + " won", 0);

        // add result entries to the game log
        final LobbyPlayer human = Singletons.getControl().getLobby().getGuiPlayer();
        

        final List<String> outcomes = new ArrayList<String>();
        for (Entry<LobbyPlayer, PlayerStatistics> p : result) {
            String whoHas = p.getKey().equals(human) ? "You have" : p.getKey().getName() + " has";
            String outcome = String.format("%s %s", whoHas, p.getValue().getOutcome().toString());
            outcomes.add(outcome);
            game.getGameLog().add("Final", outcome, 0);
        }
        
        int humanWins = getGamesWonBy(human);
        int humanLosses = getPlayedGames().size() - humanWins;
        final String statsSummary = "Won: " + humanWins + ", Lost: " + humanLosses;
        game.getGameLog().add("Final", statsSummary, 0);


        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            String title = result.getWinner().getName() + " Won!";
            ViewWinLose v = new ViewWinLose(MatchController.this);
            v.setTitle(title);
            v.setOutcomes(outcomes);
            v.setStatsSummary(statsSummary);
        } });
    }
    

    /**
     * TODO: Write javadoc for this method.
     */
    public void startRound() {

        currentGame = new GameState(players, gameType, this);
        
        try {
            attachUiToMatch(this, FControl.SINGLETON_INSTANCE.getLobby().getGuiPlayer());

            final boolean canRandomFoil = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL) && gameType == GameType.Constructed;
            GameNew.newGame(currentGame, canRandomFoil, this.useAnte);

            currentGame.setAge(GameAge.Mulligan);
        } catch (Exception e) {
            BugReporter.reportException(e);
        }

        final Player firstPlayer = determineFirstTurnPlayer(getLastGameOutcome(), currentGame);
        
        currentGame.getInputQueue().clearInput();
        if(currentGame.getType() == GameType.Planechase)
            firstPlayer.initPlane();

        //Set Field shown to current player.
        VField nextField = CMatchUI.SINGLETON_INSTANCE.getFieldViewFor(firstPlayer);
        SDisplayUtil.showTab(nextField);

        // Update observers
        currentGame.getGameLog().updateObservers();

        // This code was run from EDT.
        FThreads.invokeInNewThread( new Runnable() {
            @Override
            public void run() {
                currentGame.getAction().mulligan(firstPlayer);
            }
        });
    }

    public static void attachUiToMatch(MatchController match, LobbyPlayer humanLobbyPlayer) {
        FControl.SINGLETON_INSTANCE.setMatch(match);
        
        GameState game = match.getCurrentGame();
        game.getEvents().register(Singletons.getControl().getSoundSystem());
        
        Player localHuman = null;
        for(Player p : game.getPlayers()) {
            if ( p.getLobbyPlayer() != humanLobbyPlayer)
                continue;
            localHuman = p;
            break;
        }

        FControl.SINGLETON_INSTANCE.setPlayer(localHuman);

        // The UI controls should use these game data as models
        CMatchUI.SINGLETON_INSTANCE.initMatch(game.getRegisteredPlayers(), humanLobbyPlayer);
        CDock.SINGLETON_INSTANCE.setModel(game, humanLobbyPlayer);
        CStack.SINGLETON_INSTANCE.setModel(game.getStack(), humanLobbyPlayer);
        CLog.SINGLETON_INSTANCE.setModel(game.getGameLog());
        CCombat.SINGLETON_INSTANCE.setModel(game);
        CMessage.SINGLETON_INSTANCE.setModel(match);


        Singletons.getModel().getPreferences().actuateMatchPreferences();
        Singletons.getControl().changeState(FControl.Screens.MATCH_SCREEN);
        SDisplayUtil.showTab(EDocID.REPORT_LOG.getDoc());

        CMessage.SINGLETON_INSTANCE.getInputControl().setGame(game);

        // models shall notify controllers of changes
        
        game.getStack().addObserver(CStack.SINGLETON_INSTANCE);
        game.getGameLog().addObserver(CLog.SINGLETON_INSTANCE);
        // some observers were set in CMatchUI.initMatch

        // black magic still
        
        
        VAntes.SINGLETON_INSTANCE.setModel(game.getRegisteredPlayers());

        for (final VField field : VMatchUI.SINGLETON_INSTANCE.getFieldViews()) {
            field.getLblLibrary().setHoverable(Preferences.DEV_MODE);
        }

        // per player observers were set in CMatchUI.SINGLETON_INSTANCE.initMatch
    }


    public void clearGamesPlayed() {
        gamesPlayed.clear();
    }
    
    public void clearLastGame() {
        gamesPlayed.remove(gamesPlayed.size() - 1);
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public GameOutcome getLastGameOutcome() {
        return gamesPlayed.isEmpty() ? null : gamesPlayed.get(gamesPlayed.size() - 1);
    }

    public GameState getCurrentGame() {
        return currentGame;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public boolean isMatchOver() {
        int[] victories = new int[players.size()];
        for (GameOutcome go : gamesPlayed) {
            LobbyPlayer winner = go.getWinner();
            int i = 0;
            for (Pair<LobbyPlayer, PlayerStartConditions> p : players) {
                if (p.getLeft().equals(winner)) {
                    victories[i]++;
                    break; // can't have 2 winners per game
                }
                i++;
            }
        }

        for (int score : victories) {
            if (score >= gamesToWinMatch) {
                return true;
            }
        }
        return gamesPlayed.size() >= gamesPerMatch;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param questPlayer
     * @return
     */
    public int getGamesWonBy(LobbyPlayer questPlayer) {
        int sum = 0;
        for (GameOutcome go : gamesPlayed) {
            if (questPlayer.equals(go.getWinner())) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param questPlayer
     * @return
     */
    public boolean isWonBy(LobbyPlayer questPlayer) {
        return getGamesWonBy(questPlayer) >= gamesToWinMatch;
    }

    public List<Pair<LobbyPlayer, PlayerStartConditions>> getPlayers() {
        return players;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public static int getPoisonCountersAmountToLose() {
        return 10;
    }
    
    /**
     * TODO: Write javadoc for this method.
     * @param match
     * @param game
     */
    private Player determineFirstTurnPlayer(final GameOutcome lastGameOutcome, final GameState game) {
        // Only cut/coin toss if it's the first game of the match
        Player goesFirst = null;
        final String message;
        //Player humanPlayer = Singletons.getControl().getPlayer();
        boolean isFirstGame = lastGameOutcome == null;
        if (isFirstGame) {
            goesFirst = seeWhoPlaysFirstDice(game);
            message = goesFirst + " has won the coin toss.";    
        } else {
            for(Player p : game.getPlayers()) {
                if(!lastGameOutcome.isWinner(p.getLobbyPlayer())) { 
                    goesFirst = p;
                    break;
                }
            }
            message = goesFirst + " lost the last game.";
        }
        
        boolean willPlay = goesFirst.getController().getWillPlayOnFirstTurn(message);
        goesFirst = willPlay ? goesFirst : goesFirst.getOpponent();
        game.getPhaseHandler().setPlayerTurn(goesFirst);
        return goesFirst;
    }
    
    // decides who goes first when starting another game, used by newGame()
    /**
     * <p>
     * seeWhoPlaysFirstCoinToss.
     * </p>
     * @return 
     */
    private Player seeWhoPlaysFirstDice(final GameState game) {
        
        // Play the Flip Coin sound
        game.getEvents().post(new FlipCoinEvent());

        List<Player> allPlayers = game.getPlayers();
        return allPlayers.get(MyRandom.getRandom().nextInt(allPlayers.size()));
    }

}
