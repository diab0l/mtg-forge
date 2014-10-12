package forge.match;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import forge.LobbyPlayer;
import forge.ai.LobbyPlayerAi;
import forge.card.CardStateName;
import forge.control.FControlGameEventHandler;
import forge.control.FControlGamePlayback;
import forge.control.WatchLocalGame;
import forge.events.IUiEventVisitor;
import forge.events.UiEvent;
import forge.events.UiEventAttackerDeclared;
import forge.events.UiEventBlockerAssigned;
import forge.game.Game;
import forge.game.GameEntityView;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.GameView;
import forge.game.Match;
import forge.game.card.Card;
import forge.game.card.CardView;
import forge.game.card.CounterType;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerView;
import forge.game.player.RegisteredPlayer;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;
import forge.match.input.InputPlaybackControl;
import forge.match.input.InputQueue;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.player.LobbyPlayerHuman;
import forge.player.PlayerControllerHuman;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.sound.MusicPlaylist;
import forge.sound.SoundSystem;
import forge.util.GuiDisplayUtil;
import forge.util.NameGenerator;
import forge.util.gui.SOptionPane;

public class MatchUtil {
    private static IMatchController controller;
    private static Game game;
    private static Player currentPlayer;
    private static final List<PlayerControllerHuman> humanControllers = new ArrayList<PlayerControllerHuman>();
    private static int humanCount;
    private static final EventBus uiEvents;
    private static FControlGamePlayback playbackControl;
    private static final MatchUiEventVisitor visitor = new MatchUiEventVisitor();

    static {
        uiEvents = new EventBus("ui events");
        uiEvents.register(SoundSystem.instance);
        uiEvents.register(visitor);
    }

    public static IMatchController getController() {
        return controller;
    }
    public static void setController(IMatchController controller0) {
        controller = controller0;
    }

    public static void startMatch(GameType gameType, List<RegisteredPlayer> players) {
        startMatch(gameType, null, players);
    }
    public static void startMatch(GameType gameType, Set<GameType> appliedVariants, List<RegisteredPlayer> players) {
        boolean useRandomFoil = FModel.getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL);
        for (RegisteredPlayer rp : players) {
            rp.setRandomFoil(useRandomFoil);
        }

        GameRules rules = new GameRules(gameType);
        if (appliedVariants != null && !appliedVariants.isEmpty()) {
            rules.setAppliedVariants(appliedVariants);
        }
        rules.setPlayForAnte(FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE));
        rules.setMatchAnteRarity(FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE_MATCH_RARITY));
        rules.setManaBurn(FModel.getPreferences().getPrefBoolean(FPref.UI_MANABURN));
        rules.canCloneUseTargetsImage = FModel.getPreferences().getPrefBoolean(FPref.UI_CLONE_MODE_SOURCE);

        controller.startNewMatch(new Match(rules, players));
    }

    public static void continueMatch() {
        final Match match = game.getMatch();
        endCurrentGame();
        startGame(match);
    }

    public static void restartMatch() {
        final Match match = game.getMatch();
        endCurrentGame();
        match.clearGamesPlayed();
        startGame(match);
    }

    public static void startGame(final Match match) {
        if (!controller.resetForNewGame()) { return; }

        SoundSystem.instance.setBackgroundMusic(MusicPlaylist.MATCH);

        game = match.createGame();

        if (game.getRules().getGameType() == GameType.Quest) {
            QuestController qc = FModel.getQuest();
            // Reset new list when the Match round starts, not when each game starts
            if (game.getMatch().getPlayedGames().isEmpty()) {
                qc.getCards().resetNewList();
            }
            game.subscribeToEvents(qc); // this one listens to player's mulligans ATM
        }

        game.subscribeToEvents(SoundSystem.instance);

        final String[] indices = FModel.getPreferences().getPref(FPref.UI_AVATARS).split(",");

        // Instantiate all required field slots (user at 0)
        final List<Player> sortedPlayers = new ArrayList<Player>(game.getRegisteredPlayers());
        Collections.sort(sortedPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                int v1 = p1.getController() instanceof PlayerControllerHuman ? 0 : 1;
                int v2 = p2.getController() instanceof PlayerControllerHuman ? 0 : 1;
                return Integer.compare(v1, v2);
            }
        });

        int i = 0;
        int avatarIndex = 0;
        humanCount = 0;
        for (Player p : sortedPlayers) {
            if (i < indices.length) {
                avatarIndex = Integer.parseInt(indices[i]);
                i++;
            }
            p.getLobbyPlayer().setAvatarIndex(avatarIndex);

            if (p.getController() instanceof PlayerControllerHuman) {
                final PlayerControllerHuman humanController = (PlayerControllerHuman) p.getController();
                if (humanCount == 0) {
                    currentPlayer = p;
                    game.subscribeToEvents(new FControlGameEventHandler(humanController));
                }
                humanControllers.add(humanController);
                humanCount++;
            }
        }

        if (humanCount == 0) { //watch game but do not participate
            currentPlayer = sortedPlayers.get(0);
            PlayerControllerHuman humanController = new WatchLocalGame(game, currentPlayer, currentPlayer.getLobbyPlayer());
            game.subscribeToEvents(new FControlGameEventHandler(humanController));
            humanControllers.add(humanController);
        }
        else if (humanCount == sortedPlayers.size() && controller.hotSeatMode()) {
            //if there are no AI's, allow all players to see all cards (hotseat mode).
            for (Player p : sortedPlayers) {
                ((PlayerControllerHuman) p.getController()).setMayLookAtAllCards(true);
            }
        }

        controller.openView(sortedPlayers);

        if (humanCount == 0) {
            playbackControl = new FControlGamePlayback(getHumanController());
            playbackControl.setGame(game);
            game.subscribeToEvents(playbackControl);
        }

        // It's important to run match in a different thread to allow GUI inputs to be invoked from inside game. 
        // Game is set on pause while gui player takes decisions
        game.getAction().invoke(new Runnable() {
            @Override
            public void run() {
                //prompt user for player one name if needed
                if (StringUtils.isBlank(FModel.getPreferences().getPref(FPref.PLAYER_NAME))) {
                    boolean isPlayerOneHuman = match.getPlayers().get(0).getPlayer() instanceof LobbyPlayerHuman;
                    boolean isPlayerTwoComputer = match.getPlayers().get(1).getPlayer() instanceof LobbyPlayerAi;
                    if (isPlayerOneHuman && isPlayerTwoComputer) {
                        GamePlayerUtil.setPlayerName();
                    }
                }
                match.startGame(game);
            }
        });
    }

    public static Game getGame() {
        return game;
    }
    public static GameView getGameView() {
        return game == null ? null : game.getView();
    }

    public static PlayerControllerHuman getHumanController() {
        return getHumanController(currentPlayer);
    }
    public static PlayerControllerHuman getHumanController(Player player) {
        switch (humanControllers.size()) {
        case 1:
            return humanControllers.get(0);
        case 0:
            return null;
        default:
            return humanControllers.get(player.getId());
        }
    }

    public static int getHumanCount() {
        return humanCount;
    }

    public static PlayerControllerHuman getOtherHumanController() {
        //return other game view besides current game view
        if (humanControllers.size() < 2) {
            return null;
        }
        PlayerControllerHuman humanController = getHumanController();
        if (humanController == humanControllers.get(0)) {
            return humanControllers.get(1);
        }
        return humanControllers.get(0);
    }

    public static InputQueue getInputQueue() {
        PlayerControllerHuman humanController = getHumanController();
        if (humanController != null) {
            return humanController.getInputQueue();
        }
        return null;
    }

    public static void endCurrentTurn() {
        getHumanController().passPriorityUntilEndOfTurn();
    }

    public static Player getCurrentPlayer() {
        return currentPlayer;
    }
    public static void setCurrentPlayer(Player currentPlayer0) {
        if (currentPlayer == currentPlayer0) { return; }
        currentPlayer = currentPlayer0;
        if (humanControllers.size() > 1) {
            //TODO: ensure card views updated when current player changes to account for changes in card visibility
        }
    }

    public static void alphaStrike() {
        getHumanController().alphaStrike();
    }

    public static Map<CardView, Integer> getDamageToAssign(final CardView attacker, final List<CardView> blockers, final int damage, final GameEntityView defender, final boolean overrideOrder) {
        if (damage <= 0) {
            return new HashMap<CardView, Integer>();
        }

        // If the first blocker can absorb all of the damage, don't show the Assign Damage dialog
        CardView firstBlocker = blockers.get(0);
        if (!overrideOrder && !attacker.getCurrentState().hasDeathtouch() && firstBlocker.getLethalDamage() >= damage) {
            Map<CardView, Integer> res = new HashMap<CardView, Integer>();
            res.put(firstBlocker, damage);
            return res;
        }

        return controller.assignDamage(attacker, blockers, damage, defender, overrideOrder);
    }

    private static Set<PlayerView> highlightedPlayers = new HashSet<PlayerView>();
    public static void setHighlighted(PlayerView pv, boolean b) {
        if (b) {
            highlightedPlayers.add(pv);
        }
        else {
            highlightedPlayers.remove(pv);
        }
    }

    public static boolean isHighlighted(PlayerView player) {
        return highlightedPlayers.contains(player);
    }

    private static Set<CardView> highlightedCards = new HashSet<CardView>();
    // used to highlight cards in UI
    public static void setUsedToPay(CardView card, boolean value) {
        boolean hasChanged = value ? highlightedCards.add(card) : highlightedCards.remove(card);
        if (hasChanged) { // since we are in UI thread, may redraw the card right now
            controller.updateSingleCard(card);
        }
    }

    public static boolean isUsedToPay(CardView card) {
        return highlightedCards.contains(card);
    }

    public static void updateCards(Iterable<CardView> cardsToUpdate) {
        for (CardView c : cardsToUpdate) {
            controller.updateSingleCard(c);
        }
    }

    /** Concede game, bring up WinLose UI. */
    public static void concede() {
        String userPrompt =
                "This will end the current game and you will not be able to resume.\n\n" +
                        "Concede anyway?";
        if (SOptionPane.showConfirmDialog(userPrompt, "Concede Game?", "Concede", "Cancel")) {
            if (humanCount == 0) { // no human? then all players surrender!
                for (Player p : game.getPlayers()) {
                    p.concede();
                }
            }
            else {
                getCurrentPlayer().concede();
            }

            Player priorityPlayer = game.getPhaseHandler().getPriorityPlayer();
            boolean humanHasPriority = priorityPlayer == null || priorityPlayer.getLobbyPlayer() instanceof LobbyPlayerHuman;

            if (humanCount > 0 && humanHasPriority) {
                game.getAction().checkGameOverCondition();
            }
            else {
                game.isGameOver(); // this is synchronized method - it's used to make Game-0 thread see changes made here
                getInputQueue().onGameOver(false); //release any waiting input, effectively passing priority
            }

            if (playbackControl != null) {
                playbackControl.onGameStopRequested();
            }
        }
    }

    public static void endCurrentGame() {
        if (game == null) { return; }

        game = null;
        currentPlayer = null;
        humanControllers.clear();

        controller.afterGameEnd();
    }

    public static void pause() {
        SoundSystem.instance.pause();
        //pause playback if needed
        InputQueue inputQueue = getInputQueue();
        if (inputQueue != null && inputQueue.getInput() instanceof InputPlaybackControl) {
            ((InputPlaybackControl)inputQueue.getInput()).pause();
        }
    }

    public static void resume() {
        SoundSystem.instance.resume();
    }

    private final static boolean LOG_UIEVENTS = false;

    // UI-related events should arrive here
    public static void fireEvent(UiEvent uiEvent) {
        if (LOG_UIEVENTS) {
            //System.out.println("UI: " + uiEvent.toString()  + " \t\t " + FThreads.debugGetStackTraceItem(4, true));
        }
        uiEvents.post(uiEvent);
    }

    public static void setupGameState(String filename) {
        int humanLife = -1;
        int aiLife = -1;

        final Map<ZoneType, String> humanCardTexts = new EnumMap<ZoneType, String>(ZoneType.class);
        final Map<ZoneType, String> aiCardTexts = new EnumMap<ZoneType, String>(ZoneType.class);

        String tChangePlayer = "NONE";
        String tChangePhase = "NONE";

        try {
            final FileInputStream fstream = new FileInputStream(filename);
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String temp = "";

            while ((temp = br.readLine()) != null) {
                final String[] tempData = temp.split("=");
                if (tempData.length < 2 || temp.charAt(0) == '#') {
                    continue;
                }

                final String categoryName = tempData[0].toLowerCase();
                final String categoryValue = tempData[1];

                switch (categoryName) {
                case "humanlife":
                    humanLife = Integer.parseInt(categoryValue);
                    break;
                case "ailife":
                    aiLife = Integer.parseInt(categoryValue);
                    break;
                case "activeplayer":
                    tChangePlayer = categoryValue.trim().toLowerCase();
                    break;
                case "activephase":
                    tChangePhase = categoryValue;
                    break;
                case "humancardsinplay":
                    humanCardTexts.put(ZoneType.Battlefield, categoryValue);
                    break;
                case "aicardsinplay":
                    aiCardTexts.put(ZoneType.Battlefield, categoryValue);
                    break;
                case "humancardsinhand":
                    humanCardTexts.put(ZoneType.Hand, categoryValue);
                    break;
                case "aicardsinhand":
                    aiCardTexts.put(ZoneType.Hand, categoryValue);
                    break;
                case "humancardsingraveyard":
                    humanCardTexts.put(ZoneType.Graveyard, categoryValue);
                    break;
                case "aicardsingraveyard":
                    aiCardTexts.put(ZoneType.Graveyard, categoryValue);
                    break;
                case "humancardsinlibrary":
                    humanCardTexts.put(ZoneType.Library, categoryValue);
                    break;
                case "aicardsinlibrary":
                    aiCardTexts.put(ZoneType.Library, categoryValue);
                    break;
                case "humancardsinexile":
                    humanCardTexts.put(ZoneType.Exile, categoryValue);
                    break;
                case "aicardsinexile":
                    aiCardTexts.put(ZoneType.Exile, categoryValue);
                    break;
                }
            }

            in.close();
        }
        catch (final FileNotFoundException fnfe) {
            SOptionPane.showErrorDialog("File not found: " + filename);
        }
        catch (final Exception e) {
            SOptionPane.showErrorDialog("Error loading battle setup file!");
            return;
        }

        setupGameState(humanLife, aiLife, humanCardTexts, aiCardTexts, tChangePlayer, tChangePhase);
    }

    public static void setupGameState(final int humanLife, final int aiLife, final Map<ZoneType, String> humanCardTexts,
            final Map<ZoneType, String> aiCardTexts, final String tChangePlayer, final String tChangePhase) {

        game.getAction().invoke(new Runnable() {
            @Override
            public void run() {
                final Player human = game.getPlayers().get(0);
                final Player ai = game.getPlayers().get(1);

                Player newPlayerTurn = tChangePlayer.equals("human") ? newPlayerTurn = human : tChangePlayer.equals("ai") ? newPlayerTurn = ai : null;
                PhaseType newPhase = tChangePhase.trim().equalsIgnoreCase("none") ? null : PhaseType.smartValueOf(tChangePhase);

                game.getPhaseHandler().devModeSet(newPhase, newPlayerTurn);

                game.getTriggerHandler().suppressMode(TriggerType.ChangesZone);

                setupPlayerState(humanLife, humanCardTexts, human);
                setupPlayerState(aiLife, aiCardTexts, ai);

                game.getTriggerHandler().clearSuppression(TriggerType.ChangesZone);

                game.getAction().checkStaticAbilities(true);
            }
        });
    }

    private static void setupPlayerState(int life, Map<ZoneType, String> cardTexts, final Player p) {
        Map<ZoneType, List<Card>> humanCards = new EnumMap<ZoneType, List<Card>>(ZoneType.class);
        for (Entry<ZoneType, String> kv : cardTexts.entrySet()) {
            humanCards.put(kv.getKey(), processCardsForZone(kv.getValue().split(";"), p));
        }

        if (life > 0) {
            p.setLife(life, null);
        }

        for (Entry<ZoneType, List<Card>> kv : humanCards.entrySet()) {
            if (kv.getKey() == ZoneType.Battlefield) {
                for (final Card c : kv.getValue()) {
                    p.getZone(ZoneType.Hand).add(c);
                    p.getGame().getAction().moveToPlay(c);
                    c.setSickness(false);
                }
            }
            else {
                p.getZone(kv.getKey()).setCards(kv.getValue());
            }
        }
    }

    private static List<Card> processCardsForZone(final String[] data, final Player player) {
        final List<Card> cl = new ArrayList<Card>();
        for (final String element : data) {
            final String[] cardinfo = element.trim().split("\\|");

            final Card c = Card.fromPaperCard(FModel.getMagicDb().getCommonCards().getCard(cardinfo[0]), player);

            boolean hasSetCurSet = false;
            for (final String info : cardinfo) {
                if (info.startsWith("Set:")) {
                    c.setSetCode(info.substring(info.indexOf(':') + 1));
                    hasSetCurSet = true;
                }
                else if (info.equalsIgnoreCase("Tapped:True")) {
                    c.tap();
                }
                else if (info.startsWith("Counters:")) {
                    final String[] counterStrings = info.substring(info.indexOf(':') + 1).split(",");
                    for (final String counter : counterStrings) {
                        c.addCounter(CounterType.valueOf(counter), 1, true);
                    }
                }
                else if (info.equalsIgnoreCase("SummonSick:True")) {
                    c.setSickness(true);
                }
                else if (info.equalsIgnoreCase("FaceDown:True")) {
                    c.setState(CardStateName.FaceDown, true);
                }
            }

            if (!hasSetCurSet) {
                c.setSetCode(c.getMostRecentSet());
            }

            cl.add(c);
        }
        return cl;
    }
    
    /** Returns a random name from the supplied list. */
    public static String getRandomName() {
        String playerName = GuiDisplayUtil.getPlayerName();
        String aiName = NameGenerator.getRandomName("Any", "Generic", playerName);
        return aiName;
    }

    public final static LobbyPlayer getGuiPlayer() {
        return GamePlayerUtil.getGuiPlayer();
    }

    private static class MatchUiEventVisitor implements IUiEventVisitor<Void> {
        @Override
        public Void visit(UiEventBlockerAssigned event) {
            controller.updateSingleCard(event.blocker);
            return null;
        }

        @Override
        public Void visit(UiEventAttackerDeclared event) {
            controller.updateSingleCard(event.attacker);
            return null;
        }

        @Subscribe
        public void receiveEvent(UiEvent evt) {
            evt.visit(this);
        }
    }
}
