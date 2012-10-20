package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.Singletons;
import forge.Constant.Preferences;
import forge.control.FControl;
import forge.control.input.InputControl;
import forge.control.input.InputMulligan;
import forge.deck.Deck;
import forge.error.ErrorViewer;
import forge.game.player.ComputerAIGeneral;
import forge.game.player.ComputerAIInput;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerType;
import forge.game.zone.ZoneType;
import forge.gui.GuiInput;
import forge.gui.framework.EDocID;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.VMatchUI;
import forge.gui.match.controllers.CLog;
import forge.gui.match.controllers.CMessage;
import forge.gui.match.controllers.CStack;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VAntes;
import forge.gui.toolbox.FLabel;
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
        if ( !game.isGameOver() )
            throw new RuntimeException("Game is not over yet.");

        GameOutcome result = new GameOutcome(reason, game.getPlayers());
        result.setTurnsPlayed(game.getPhaseHandler().getTurn());
        gamesPlayed.add(result);
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void startRound() {

        // Will this lose all the ordering?
        currentGame = Singletons.getModel().newGame(players.keySet());

        // Instantiate AI
        input = new InputControl(currentGame);

        
        Map<Player, PlayerStartConditions> startConditions = new HashMap<Player, PlayerStartConditions>();
        for (Player p : currentGame.getPlayers())
            startConditions.put(p, players.get(p.getLobbyPlayer()));

        try {
            CMatchUI.SINGLETON_INSTANCE.initMatch(currentGame.getPlayers(), Singletons.getControl().getPlayer());
            Singletons.getModel().getPreferences().actuateMatchPreferences();
            Singletons.getControl().changeState(FControl.MATCH_SCREEN);
            SDisplayUtil.showTab(EDocID.REPORT_LOG.getDoc());

            // set all observers
            GuiInput inputControl = CMessage.SINGLETON_INSTANCE.getInputControl();
            input.addObserver(inputControl);
            currentGame.getStack().addObserver(inputControl);
            currentGame.getPhaseHandler().addObserver(inputControl);
            currentGame.getGameLog().addObserver(CLog.SINGLETON_INSTANCE);
            currentGame.getStack().addObserver(CStack.SINGLETON_INSTANCE);
            // some observers are set in CMatchUI.initMatch

            final boolean canRandomFoil = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL) && gameType == GameType.Constructed;
            GameNew.newGame(startConditions, currentGame, canRandomFoil);

            Player computerPlayer = Aggregates.firstFieldEquals(currentGame.getPlayers(), Player.Accessors.FN_GET_TYPE, PlayerType.COMPUTER);
            input.setComputer(new ComputerAIInput(new ComputerAIGeneral(computerPlayer)));


            if (this.getPlayedGames().isEmpty()) { 
                // TODO restore this functionality!!!
                //VMatchUI.SINGLETON_INSTANCE.getViewDevMode().getDocument().setVisible(Preferences.DEV_MODE);
        
                for (final VField field : VMatchUI.SINGLETON_INSTANCE.getFieldViews()) {
                    ((FLabel) field.getLblHand()).setHoverable(Preferences.DEV_MODE);
                    ((FLabel) field.getLblLibrary()).setHoverable(Preferences.DEV_MODE);
                }
        
                VAntes.SINGLETON_INSTANCE.clearAnteCards();
                input.resetInput();
            }
            
            // per player observers were set in CMatchUI.SINGLETON_INSTANCE.initMatch

            CMessage.SINGLETON_INSTANCE.updateGameInfo(this);
            // Update observers
            currentGame.getStack().updateObservers();
            input.updateObservers();
            currentGame.getGameLog().updateObservers();

            
            for( Player p : currentGame.getPlayers() ) {
                p.updateObservers();
                p.getZone(ZoneType.Hand).updateObservers();
            }

            CMatchUI.SINGLETON_INSTANCE.setCard(Singletons.getControl().getPlayer().getCardsIn(ZoneType.Hand).get(0));
            input.setInput(new InputMulligan());            
            
        } catch (Exception e) {
            ErrorViewer.showError(e);
        }
        // bf.updateObservers();
        // player.updateObservers();
        // player.getZone(ZoneType.Hand).updateObservers();

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
            if (score >= gamesToWinMatch)
                return true;
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
            if (go.getWinner().equals(questPlayer)) {
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
        return cond == null ? null : cond.getDeck();
    }

    public final InputControl getInput() {
        return input;
    }
}
