package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import forge.Constant.Preferences;
import forge.Singletons;
import forge.control.FControl;
import forge.control.input.InputControl;
import forge.deck.Deck;
import forge.error.BugReporter;
import forge.game.ai.AiProfileUtil;
import forge.game.event.DuelOutcomeEvent;
import forge.game.player.AIPlayer;
import forge.game.player.HumanPlayer;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerStatistics;
import forge.game.player.PlayerType;
import forge.game.zone.ZoneType;
import forge.gui.InputProxy;
import forge.gui.framework.EDocID;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.VMatchUI;
import forge.gui.match.ViewWinLose;
import forge.gui.match.controllers.CDock;
import forge.gui.match.controllers.CLog;
import forge.gui.match.controllers.CMessage;
import forge.gui.match.controllers.CStack;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VAntes;
import forge.properties.ForgePreferences.FPref;
import forge.util.Aggregates;

/**
 * TODO: Write javadoc for this type.
 * 
 */

public class MatchController {

    private final Map<LobbyPlayer, PlayerStartConditions> players = new HashMap<LobbyPlayer, PlayerStartConditions>();
    private GameType gameType = GameType.Constructed;

    private int gamesPerMatch = 3;
    private int gamesToWinMatch = 2;

    private GameState currentGame = null;

    private final List<GameOutcome> gamesPlayed = new ArrayList<GameOutcome>();
    private final List<GameOutcome> gamesPlayedRo;

    private InputControl input;

    public MatchController() {
        gamesPlayedRo = Collections.unmodifiableList(gamesPlayed);
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

        GameOutcome result = new GameOutcome(reason, game.getRegisteredPlayers());
        result.setTurnsPlayed(game.getPhaseHandler().getTurn());
        gamesPlayed.add(result);

        // Play the win/lose sound
        game.getEvents().post(new DuelOutcomeEvent(result.getWinner()));
        game.getGameLog().add("Final", result.getWinner() + " won", 0);

        // add result entries to the game log
        LobbyPlayer human = Singletons.getControl().getPlayer().getLobbyPlayer();
        String title = result.isWinner(human) ? "You Win" : "You Lost";

        List<String> outcomes = new ArrayList<String>();
        for (Entry<LobbyPlayer, PlayerStatistics> p : result) {
            String whoHas = p.getKey().equals(human) ? "You have" : p.getKey().getName() + " has";
            String outcome = String.format("%s %s", whoHas, p.getValue().getOutcome().toString());
            outcomes.add(outcome);
            game.getGameLog().add("Final", outcome, 0);
        }
        
        int humanWins = getGamesWonBy(human);
        int humanLosses = getPlayedGames().size() - humanWins;
        String statsSummary = "Won: " + humanWins + ", Lost: " + humanLosses;
        game.getGameLog().add("Final", statsSummary, 0);


        ViewWinLose v = new ViewWinLose(this);
        v.setTitle(title);
        v.setOutcomes(outcomes);
        v.setStatsSummary(statsSummary);
    }
    

    /**
     * TODO: Write javadoc for this method.
     */
    public void startRound() {

        // Deal with circular dependencies here
        input = new InputControl();
        currentGame = Singletons.getModel().newGame(players.keySet(),gameType, this);

        Map<Player, PlayerStartConditions> startConditions = new HashMap<Player, PlayerStartConditions>();
        for (Player p : currentGame.getPlayers()) {
            startConditions.put(p, players.get(p.getLobbyPlayer()));
        }

        // Set the current AI profile.
        for (Player p : currentGame.getPlayers()) {
            if ( !(p instanceof AIPlayer))
                continue;
            
            String currentAiProfile = Singletons.getModel().getPreferences().getPref(FPref.UI_CURRENT_AI_PROFILE);
            
            // TODO: implement specific AI profiles for quest mode.
            boolean wantRandomProfile = currentAiProfile.equals(AiProfileUtil.AI_PROFILE_RANDOM_DUEL) 
                    || (this.getPlayedGames().isEmpty() && currentAiProfile.equals(AiProfileUtil.AI_PROFILE_RANDOM_MATCH)); 
            
            String profileToSet = wantRandomProfile ? AiProfileUtil.getRandomProfile() : currentAiProfile;
            
            p.getLobbyPlayer().setAiProfile(profileToSet);
            System.out.println(String.format("AI profile %s was chosen for the lobby player %s.", p.getLobbyPlayer().getAiProfile(), p.getLobbyPlayer().getName()));
        }
        
        
        try {
            HumanPlayer localHuman = (HumanPlayer) Aggregates.firstFieldEquals(currentGame.getPlayers(), Player.Accessors.FN_GET_TYPE, PlayerType.HUMAN);
            FControl.SINGLETON_INSTANCE.setPlayer(localHuman);
            CMatchUI.SINGLETON_INSTANCE.initMatch(currentGame.getRegisteredPlayers(), localHuman);
            CDock.SINGLETON_INSTANCE.onGameStarts(currentGame, localHuman);
            Singletons.getModel().getPreferences().actuateMatchPreferences();
            Singletons.getControl().changeState(FControl.Screens.MATCH_SCREEN);
            SDisplayUtil.showTab(EDocID.REPORT_LOG.getDoc());

            InputProxy inputControl = CMessage.SINGLETON_INSTANCE.getInputControl();
            inputControl.setMatch(this);
            input.addObserver(inputControl);
            currentGame.getStack().addObserver(inputControl);
            currentGame.getPhaseHandler().addObserver(inputControl);
            
            currentGame.getGameLog().addObserver(CLog.SINGLETON_INSTANCE);
            currentGame.getStack().addObserver(CStack.SINGLETON_INSTANCE);
            // some observers are set in CMatchUI.initMatch

            final boolean canRandomFoil = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL) && gameType == GameType.Constructed;
            GameNew.newGame(this, startConditions, currentGame, canRandomFoil);
            
            getInput().clearInput();
            //getInput().setNewInput(currentGame);
            
            
//            Thread thGame = new GameInputUpdatesThread(this, currentGame);
//            thGame.setName("Game input updater");
//            thGame.start();

            // TODO restore this functionality!!!
            //VMatchUI.SINGLETON_INSTANCE.getViewDevMode().getDocument().setVisible(Preferences.DEV_MODE);
            for (final VField field : VMatchUI.SINGLETON_INSTANCE.getFieldViews()) {
                field.getLblLibrary().setHoverable(Preferences.DEV_MODE);
            }

            if (this.getPlayedGames().isEmpty()) {
                VAntes.SINGLETON_INSTANCE.clearAnteCards();
            }

            // per player observers were set in CMatchUI.SINGLETON_INSTANCE.initMatch

            CMessage.SINGLETON_INSTANCE.updateGameInfo(this);
            // Update observers
            currentGame.getGameLog().updateObservers();

            CMatchUI.SINGLETON_INSTANCE.setCard(Singletons.getControl().getPlayer().getCardsIn(ZoneType.Hand).get(0));
        } catch (Exception e) {
            BugReporter.reportException(e);
        }

    }

    /**
     * This should become constructor once.
     */
    public void initMatch(GameType type, Map<LobbyPlayer, PlayerStartConditions> map) {
        gamesPlayed.clear();
        players.clear();
        players.putAll(map);
        gameType = type;
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void replayRound() {
        gamesPlayed.remove(gamesPlayed.size() - 1);
        startRound();
    }

    public void replay() {
        gamesPlayed.clear();
        startRound();
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
            for (LobbyPlayer p : players.keySet()) {
                if (p.equals(winner)) {
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

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param lobbyPlayer
     * @return
     */
    public Deck getPlayersDeck(LobbyPlayer lobbyPlayer) {
        PlayerStartConditions cond = players.get(lobbyPlayer);
        return cond == null ? null : cond.getCurrentDeck();
    }

    public Deck getPlayersOriginalDeck(LobbyPlayer lobbyPlayer) {
        PlayerStartConditions cond = players.get(lobbyPlayer);
        return cond == null ? null : cond.getOriginalDeck();
    }    
    
    
    public Map<LobbyPlayer, PlayerStartConditions> getPlayers() {
        return players;
    }

    public final InputControl getInput() {
        return input;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public static int getPoisonCountersAmountToLose() {
        return 10;
    }
}
