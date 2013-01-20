package forge.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JOptionPane;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.CardUtil;
import forge.GameAction;
import forge.Singletons;
import forge.card.trigger.TriggerHandler;
import forge.card.trigger.TriggerType;
import forge.control.input.InputControl;
import forge.control.input.InputMulligan;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.game.event.FlipCoinEvent;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.match.views.VAntes;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences.FPref;
import forge.util.Aggregates;
import forge.util.MyRandom;

/** 
 * Methods for all things related to starting a new game.
 * All of these methods can and should be static.
 */
public class GameNew {
    private static void prepareGameLibrary(Player player, Deck deck, Map<Player, List<String>> removedAnteCards, List<String> rAICards, boolean canRandomFoil, Random generator, boolean useAnte) {

        PlayerZone library = player.getZone(ZoneType.Library);
        PlayerZone sideboard = player.getZone(ZoneType.Sideboard);
        for (final Entry<CardPrinted, Integer> stackOfCards : deck.getMain()) {
            final CardPrinted cardPrinted = stackOfCards.getKey();
            for (int i = 0; i < stackOfCards.getValue(); i++) {

                final Card card = cardPrinted.toForgeCard(player);

                // apply random pictures for cards
                if (player.isComputer() || Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_RANDOM_CARD_ART)) {
                    final int cntVariants = cardPrinted.getCard().getEditionInfo(cardPrinted.getEdition()).getCopiesCount();
                    if (cntVariants > 1) {
                        card.setRandomPicture(generator.nextInt(cntVariants - 1) + 1);
                        card.setImageFilename(CardUtil.buildFilename(card));
                    }
                }

                // Assign random foiling on approximately 1:20 cards
                if (cardPrinted.isFoil() || (canRandomFoil && MyRandom.percentTrue(5))) {
                    final int iFoil = MyRandom.getRandom().nextInt(9) + 1;
                    card.setFoil(iFoil);
                }

                if (!useAnte && card.hasKeyword("Remove CARDNAME from your deck before playing if you're not playing for ante.")) {
                    if (!removedAnteCards.containsKey(player)) {
                        removedAnteCards.put(player, new ArrayList<String>());
                    }
                    removedAnteCards.get(player).add(card.getName());
                } else {
                    library.add(card);
                }

                // mark card as difficult for AI to play
                if (player.isComputer() && card.getSVar("RemAIDeck").equals("True") && !rAICards.contains(card.getName())) {
                    rAICards.add(card.getName());
                    // get card picture so that it is in the image cache
                    // ImageCache.getImage(card);
                }
            }
        }
        for (final Entry<CardPrinted, Integer> stackOfCards : deck.getSideboard()) {
            final CardPrinted cardPrinted = stackOfCards.getKey();
            for (int i = 0; i < stackOfCards.getValue(); i++) {

                final Card card = cardPrinted.toForgeCard(player);

                // apply random pictures for cards
                if (player.isComputer() || Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_RANDOM_CARD_ART)) {
                    final int cntVariants = cardPrinted.getCard().getEditionInfo(cardPrinted.getEdition()).getCopiesCount();
                    if (cntVariants > 1) {
                        card.setRandomPicture(generator.nextInt(cntVariants - 1) + 1);
                        card.setImageFilename(CardUtil.buildFilename(card));
                    }
                }

                // Assign random foiling on approximately 1:20 cards
                if (cardPrinted.isFoil() || (canRandomFoil && MyRandom.percentTrue(5))) {
                    final int iFoil = MyRandom.getRandom().nextInt(9) + 1;
                    card.setFoil(iFoil);
                }

                if (!useAnte && card.hasKeyword("Remove CARDNAME from your deck before playing if you're not playing for ante.")) {
                    if (!removedAnteCards.containsKey(player)) {
                        removedAnteCards.put(player, new ArrayList<String>());
                    }
                    removedAnteCards.get(player).add(card.getName());
                } else {
                    sideboard.add(card);
                }

                // TODO: Enable the code below for Limited modes only when the AI can play all the
                // cards in the sideboard (probably means all cards in Forge because in Limited mode,
                // any cards can end up in the AI sideboard?)

                // mark card as difficult for AI to play
                if (player.isComputer() && card.getSVar("RemAIDeck").equals("True") && !rAICards.contains(card.getName())) {
                    if (Singletons.getModel().getMatch().getGameType() != GameType.Draft
                            && Singletons.getModel().getMatch().getGameType() != GameType.Sealed) {
                        rAICards.add(card.getName());
                        // get card picture so that it is in the image cache
                        // ImageCache.getImage(card);
                    }
                }
            }
        }
    }

    private static void prepareSingleLibrary(final Player player, final Deck deck, final Map<Player, List<String>> removedAnteCards, final List<String> rAICards, boolean canRandomFoil) {
        final Random generator = MyRandom.getRandom();
        boolean useAnte = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE);

        prepareGameLibrary(player, deck, removedAnteCards, rAICards, canRandomFoil, generator, useAnte);

        // Shuffling
        // Ai may cheat
        if (player.isComputer() && Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_SMOOTH_LAND)) {
            // do this instead of shuffling Computer's deck
            final Iterable<Card> c1 = GameNew.smoothComputerManaCurve(player.getCardsIn(ZoneType.Library));
            player.getZone(ZoneType.Library).setCards(c1);
        } else {
            player.shuffle();
        }
    }

    private static Deck sideboard(final Player player, final Deck deck) {
        final GameType gameType = Singletons.getModel().getMatch().getGameType();
        boolean deckHasSideboard = (deck.getSideboard().countAll() > 0);

        DeckSection sideboard = deck.getSideboard();


        if (!deckHasSideboard || !gameType.isSideboardingAllowed()) {
            return deck;
        }

        if (player.isComputer())
            // Here is where the AI could sideboard, but needs to gather hints during the first game about what to SB
            return deck;

        // Human Sideboarding

        int deckMinSize = Math.min(deck.getMain().countAll(), gameType.getDecksFormat().getMainRange().getMinimumInteger());
        //IntRange sbRange = gameType.getDecksFormat().getSideRange();
        int sideboardSize = sideboard.countAll();

        DeckSection newSb = new DeckSection();
        List<CardPrinted> newMain = null;
        
 
        while (newMain == null || newMain.size() < deckMinSize) {
            
            if ( newMain != null ) {
                String errMsg = String.format("Too few cards in your main deck (minimum %d), please make modifications to your deck again.", deckMinSize);
                JOptionPane.showMessageDialog(null, errMsg, "Invalid deck", JOptionPane.ERROR_MESSAGE);
            }
            
            newMain = GuiChoose.getOrderChoices("Sideboard", "Main Deck", sideboardSize, deck.getSideboard().toFlatList(), deck.getMain().toFlatList(), null, true);
        }

        newSb.clear();
        newSb.addAll(deck.getMain());
        newSb.addAll(deck.getSideboard());
        for(CardPrinted c : newMain)
            newSb.remove(c);

        Deck res = (Deck) deck.copyTo(deck.getName());
        res.getMain().clear();
        res.getMain().add(newMain);
        res.getSideboard().clear();
        res.getSideboard().addAll(newSb);
        return res;

    }

    /**
     * Constructor for new game allowing card lists to be put into play
     * immediately, and life totals to be adjusted, for computer and human.
     * 
     * TODO: Accept something like match state as parameter. Match should be aware of players,
     * their decks and other special starting conditions.
     */
    public static void newGame(final Map<Player, PlayerStartConditions> playersConditions, final GameState game, final boolean canRandomFoil) {
        Singletons.getModel().getMatch().getInput().clearInput();

        Card.resetUniqueNumber();
        // need this code here, otherwise observables fail
        forge.card.trigger.Trigger.resetIDs();
        TriggerHandler trigHandler = game.getTriggerHandler();
        trigHandler.clearTriggerSettings();
        trigHandler.clearDelayedTrigger();

        // friendliness
        final Map<Player, List<String>> removedAnteCards = new HashMap<Player, List<String>>();
        final List<String> rAICards = new ArrayList<String>();

        for (Entry<Player, PlayerStartConditions> p : playersConditions.entrySet()) {
            final Player player = p.getKey();
            player.setStartingLife(p.getValue().getStartingLife());
            int hand = p.getValue().getStartingHand();
            player.setMaxHandSize(hand);
            player.setStartingHandSize(hand);
            putCardsOnBattlefield(player, p.getValue().getCardsOnBattlefield(player));

            PlayerZone com = player.getZone(ZoneType.Command);
            Iterable<Card> inCommand = p.getValue().getCardsInCommand(player);
            if (inCommand != null) {
                for (final Card c : inCommand) {
                    com.add(c, false);
                    c.refreshUniqueNumber(); // really need it?
                }
            }

            Iterable<Card> schemes = p.getValue().getSchemes(player);
            if (schemes != null) {
                player.setSchemeDeck(schemes);
            }
            
            Iterable<Card> planes = p.getValue().getPlanes(player);
            if (planes != null) {
                ArrayList<Card> l = new ArrayList<Card>();
                for(Card c : planes)
                {
                    l.add(c);
                }
                CardLists.shuffle(l);
                player.setPlanarDeck(l);
            }


            Deck toUse;
            boolean isFirstGame = Singletons.getModel().getMatch().getPlayedGames().isEmpty();
            if (!isFirstGame) {
                toUse = sideboard(player, p.getValue().getCurrentDeck());
            } else { 
                p.getValue().restoreOriginalDeck();
                toUse = p.getValue().getCurrentDeck();
            }

            prepareSingleLibrary(player, toUse, removedAnteCards, rAICards, canRandomFoil);
            player.updateObservers();
            player.getZone(ZoneType.Battlefield).updateObservers();
            player.getZone(ZoneType.Hand).updateObservers();
            player.getZone(ZoneType.Command).updateObservers();
            player.getZone(ZoneType.Battlefield).updateObservers();
        }



        if (rAICards.size() > 0) {
            String message = buildFourColumnList("AI deck contains the following cards that it can't play or may be buggy:", rAICards);
            JOptionPane.showMessageDialog(null, message, "", JOptionPane.INFORMATION_MESSAGE);
        }

        if (!removedAnteCards.isEmpty()) {
            StringBuilder ante = new StringBuilder("The following ante cards were removed:\n\n");
            for (Entry<Player, List<String>> ants : removedAnteCards.entrySet()) {
                ante.append(buildFourColumnList("From the " + ants.getKey().getName() + "'s deck:", ants.getValue()));
            }
            JOptionPane.showMessageDialog(null, ante.toString(), "", JOptionPane.INFORMATION_MESSAGE);
        }

        GameNew.actuateGame(game, false);
    }
    
    private static void putCardsOnBattlefield(Player player, Iterable<Card> cards) {
        PlayerZone bf = player.getZone(ZoneType.Battlefield);
        if (cards != null) {
            for (final Card c : cards) {
                c.addController(player);
                bf.add(c, false);
                c.setSickness(true);
                c.setStartsGameInPlay(true);
                c.refreshUniqueNumber();
            }
        }
    
    }

    // ultimate of Karn the Liberated
    public static void restartGame(final GameState game, final Player startingTurn, Map<Player, List<Card>> playerLibraries) {
        MatchController match = Singletons.getModel().getMatch();

        Map<LobbyPlayer, PlayerStartConditions> players = match.getPlayers();
        Map<Player, PlayerStartConditions> playersConditions = new HashMap<Player, PlayerStartConditions>();

        for (Player p : game.getPlayers()) {
            playersConditions.put(p, players.get(p.getLobbyPlayer()));
        }

        match.getInput().clearInput();

        //Card.resetUniqueNumber();
        // need this code here, otherwise observables fail
        forge.card.trigger.Trigger.resetIDs();
        TriggerHandler trigHandler = game.getTriggerHandler();
        trigHandler.clearTriggerSettings();
        trigHandler.clearDelayedTrigger();
        trigHandler.cleanUpTemporaryTriggers();
        trigHandler.suppressMode(TriggerType.ChangesZone);

        game.getStack().reset();
        GameAction action = game.getAction();


        for (Entry<Player, PlayerStartConditions> p : playersConditions.entrySet()) {
            final Player player = p.getKey();
            player.setStartingLife(p.getValue().getStartingLife());
            player.setNumLandsPlayed(0);
            putCardsOnBattlefield(player, p.getValue().getCardsOnBattlefield(player));

            PlayerZone library = player.getZone(ZoneType.Library);
            List<Card> newLibrary = playerLibraries.get(player);
            for (Card c : newLibrary) {
                action.moveTo(library, c);
            }

            player.shuffle();
            player.getZone(ZoneType.Battlefield).updateObservers();
            player.updateObservers();
            player.getZone(ZoneType.Hand).updateObservers();
        }

        trigHandler.clearSuppression(TriggerType.ChangesZone);

        PhaseHandler phaseHandler = game.getPhaseHandler();
        phaseHandler.setPlayerTurn(startingTurn);

        GameNew.actuateGame(game, true);
    }

    /**
     * This must be separated from the newGame method since life totals and
     * player details could be adjusted before the game is started.
     * 
     * That process (also cleanup and observer updates) should be done in
     * newGame, then when all is ready, call this function.
     * @param isRestartedGame Whether the actuated game is the first start or a restart
     */
    private static void actuateGame(final GameState game, boolean isRestartedGame) {
        if (!isRestartedGame) {
            // Deciding which cards go to ante
            if (Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE)) {
                final String nl = System.getProperty("line.separator");
                final StringBuilder msg = new StringBuilder();
                for (final Player p : game.getPlayers()) {

                    final List<Card> lib = p.getCardsIn(ZoneType.Library);
                    Predicate<Card> goodForAnte = Predicates.not(CardPredicates.Presets.BASIC_LANDS);
                    Card ante = Aggregates.random(Iterables.filter(lib, goodForAnte));
                    if (ante == null) {
                        throw new RuntimeException(p + " library is empty.");
                    }
                    game.getGameLog().add("Ante", p + " anted " + ante, 0);
                    VAntes.SINGLETON_INSTANCE.addAnteCard(p, ante);
                    game.getAction().moveTo(ZoneType.Ante, ante);
                    msg.append(p.getName()).append(" ante: ").append(ante).append(nl);
                }
                JOptionPane.showMessageDialog(null, msg, "Ante", JOptionPane.INFORMATION_MESSAGE);
            }

            GameOutcome lastGameOutcome = Singletons.getModel().getMatch().getLastGameOutcome();
            // Only cut/coin toss if it's the first game of the match
            if (lastGameOutcome == null) {
                GameNew.seeWhoPlaysFirstDice();
            } else {
                Player human = Singletons.getControl().getPlayer();
                Player goesFirst = lastGameOutcome.isWinner(human.getLobbyPlayer()) ? human.getOpponent() : human;
                setPlayersFirstTurn(goesFirst, false);
            }
        }

        // Draw <handsize> cards
        for (final Player p : game.getPlayers()) {
            p.drawCards(p.getMaxHandSize());
        }

        game.getPhaseHandler().setPhaseState(PhaseType.MULLIGAN);
        InputControl control = Singletons.getModel().getMatch().getInput();
        control.setInput(new InputMulligan());
    } // newGame()

    private static String buildFourColumnList(String firstLine, List<String> cAnteRemoved) {
        StringBuilder sb = new StringBuilder(firstLine);
        sb.append("\n");
        for (int i = 0; i < cAnteRemoved.size(); i++) {
            sb.append(cAnteRemoved.get(i));
            if (((i % 4) == 0) && (i > 0)) {
                sb.append("\n");
            } else if (i != (cAnteRemoved.size() - 1)) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

 // this is where the computer cheats
    // changes AllZone.getComputerPlayer().getZone(Zone.Library)

    /**
     * <p>
     * smoothComputerManaCurve.
     * </p>
     * 
     * @param in
     *            an array of {@link forge.Card} objects.
     * @return an array of {@link forge.Card} objects.
     */
    private static Iterable<Card> smoothComputerManaCurve(final Iterable<Card> in) {
        final List<Card> library = Lists.newArrayList(in);
        CardLists.shuffle(library);

        // remove all land, keep non-basicland in there, shuffled
        List<Card> land = CardLists.filter(library, CardPredicates.Presets.LANDS);
        for (Card c : land) {
            if (c.isLand()) {
                library.remove(c);
            }
        }

        try {
            // mana weave, total of 7 land
            // The Following have all been reduced by 1, to account for the
            // computer starting first.
            library.add(5, land.get(0));
            library.add(6, land.get(1));
            library.add(8, land.get(2));
            library.add(9, land.get(3));
            library.add(10, land.get(4));

            library.add(12, land.get(5));
            library.add(15, land.get(6));
        } catch (final IndexOutOfBoundsException e) {
            System.err.println("Error: cannot smooth mana curve, not enough land");
            return in;
        }

        // add the rest of land to the end of the deck
        for (int i = 0; i < land.size(); i++) {
            if (!library.contains(land.get(i))) {
                library.add(land.get(i));
            }
        }

        // check
        for (int i = 0; i < library.size(); i++) {
            System.out.println(library.get(i));
        }

        return library;
    } // smoothComputerManaCurve()

    // decides who goes first when starting another game, used by newGame()
    /**
     * <p>
     * seeWhoPlaysFirstCoinToss.
     * </p>
     */
    private static void seeWhoPlaysFirstDice() {
        int playerDie = 0;
        int computerDie = 0;

        while (playerDie == computerDie) {
            playerDie = MyRandom.getRandom().nextInt(20);
            computerDie = MyRandom.getRandom().nextInt(20);
        }

        // Play the Flip Coin sound
        Singletons.getModel().getGame().getEvents().post(new FlipCoinEvent());

        List<Player> allPlayers = Singletons.getModel().getGame().getPlayers();
        setPlayersFirstTurn(allPlayers.get(MyRandom.getRandom().nextInt(allPlayers.size())), true);
    }

    private static void setPlayersFirstTurn(Player goesFirst, boolean firstGame) {
        StringBuilder sb = new StringBuilder(goesFirst.toString());
        if (firstGame) {
            sb.append(" has won the coin toss.");
        }
        else {
          sb.append(" lost the last game.");
        }
        if (goesFirst.isHuman()) {
            if (!humanPlayOrDraw(sb.toString())) {
                goesFirst = goesFirst.getOpponent();
            }
        } else {
            sb.append("\nComputer Going First");
            JOptionPane.showMessageDialog(null, sb.toString(),
                    "Play or Draw?", JOptionPane.INFORMATION_MESSAGE);
        }
        Singletons.getModel().getGame().getPhaseHandler().setPlayerTurn(goesFirst);
    } // seeWhoPlaysFirstDice()

    private static boolean humanPlayOrDraw(String message) {
        final String[] possibleValues = { "Play", "Draw" };

        final Object playDraw = JOptionPane.showOptionDialog(null, message + "\n\nWould you like to play or draw?",
                "Play or Draw?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);

        return !playDraw.equals(1);
    }
}
