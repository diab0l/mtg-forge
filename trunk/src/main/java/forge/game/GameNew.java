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
import forge.Singletons;
import forge.deck.Deck;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
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
    private static void prepareSingleLibrary(final Player player, final Deck deck, final Map<Player, List<String>> removedAnteCards, final List<String> rAICards, boolean canRandomFoil) {
        final Random generator = MyRandom.getRandom();
        boolean useAnte = Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE);

        PlayerZone library = player.getZone(ZoneType.Library);
        for (final Entry<CardPrinted, Integer> stackOfCards : deck.getMain()) {
            final CardPrinted cardPrinted = stackOfCards.getKey();
            for (int i = 0; i < stackOfCards.getValue(); i++) {

                final Card card = cardPrinted.toForgeCard(player);
                
                // apply random pictures for cards
                if ( player.isComputer() ) {
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
                    if(!removedAnteCards.containsKey(player))
                        removedAnteCards.put(player, new ArrayList<String>());
                    removedAnteCards.get(player).add(card.getName());
                } else {
                    library.add(card);
                }
                
                // mark card as difficult for AI to play
                if ( player.isComputer() && card.getSVar("RemAIDeck").equals("True") && !rAICards.contains(card.getName())) {
                    rAICards.add(card.getName());
                    // get card picture so that it is in the image cache
                    // ImageCache.getImage(card);
                }
            }
        }
        
        // Shuffling
        // Ai may cheat 
        if ( player.isComputer() && Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_SMOOTH_LAND) ) {
            // do this instead of shuffling Computer's deck
            final Iterable<Card> c1 = GameNew.smoothComputerManaCurve(player.getCardsIn(ZoneType.Library));
            player.getZone(ZoneType.Library).setCards(c1);
        } else
            player.shuffle();
    }
    
    
    /**
     * Constructor for new game allowing card lists to be put into play
     * immediately, and life totals to be adjusted, for computer and human.
     * 
     * TODO: Accept something like match state as parameter. Match should be aware of players, 
     * their decks and other special starting conditions. 
     */
    public static void newGame(final Map<Player, PlayerStartConditions> playersConditions, final GameState game, final boolean canRandomFoil ) {
        Singletons.getModel().getMatch().getInput().clearInput();

        Card.resetUniqueNumber();
        // need this code here, otherwise observables fail
        forge.card.trigger.Trigger.resetIDs();
        game.getTriggerHandler().clearTriggerSettings();
        game.getTriggerHandler().clearDelayedTrigger();

        // friendliness
        final Map<Player, List<String>> removedAnteCards = new HashMap<Player, List<String>>();
        final List<String> rAICards = new ArrayList<String>();

        for( Entry<Player, PlayerStartConditions> p : playersConditions.entrySet() ) {
            final Player player = p.getKey();
            player.setStartingLife(p.getValue().getStartingLife());
            // what if I call it for AI player?
            PlayerZone bf = player.getZone(ZoneType.Battlefield);
            if (p.getValue().getCardsOnTable() != null) {
                for (final Card c : p.getValue().getCardsOnTable()) {
                    c.addController(player);
                    c.setOwner(player);
                    bf.add(c, false);
                    c.setSickness(true);
                    c.setStartsGameInPlay(true);
                    c.refreshUniqueNumber();
                }
            }
            
            prepareSingleLibrary(player, p.getValue().getDeck(), removedAnteCards, rAICards, canRandomFoil);
            player.updateObservers();
            bf.updateObservers();
            player.getZone(ZoneType.Hand).updateObservers();
        }
    
    
        
        if (rAICards.size() > 0) {
            String message = buildFourColumnList("AI deck contains the following cards that it can't play or may be buggy:", rAICards);
            JOptionPane.showMessageDialog(null, message, "", JOptionPane.INFORMATION_MESSAGE);
        }
        
        if (!removedAnteCards.isEmpty()) {
            StringBuilder ante = new StringBuilder("The following ante cards were removed:\n\n");
            for(Entry<Player, List<String>> ants : removedAnteCards.entrySet() ) {
                ante.append(buildFourColumnList( "From the " + ants.getKey().getName() + "'s deck:", ants.getValue()));
            }
            JOptionPane.showMessageDialog(null, ante.toString(), "", JOptionPane.INFORMATION_MESSAGE);
        }

        GameNew.actuateGame(game);
    }

    /**
     * This must be separated from the newGame method since life totals and
     * player details could be adjusted before the game is started.
     * 
     * That process (also cleanup and observer updates) should be done in
     * newGame, then when all is ready, call this function.
     */
    private static void actuateGame(final GameState game) {

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
            setPlayersFirstTurn(goesFirst);
        }
            


        // Draw 7 cards 
        for (final Player p : game.getPlayers())
        {
            for (int i = 0; i < 7; i++) {
                p.drawCard();
            }
        }
    } // newGame()
    
    private static String buildFourColumnList(String firstLine, List<String> cAnteRemoved ) {
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
        
        List<Player> allPlayers = Singletons.getModel().getGame().getPlayers();
        setPlayersFirstTurn(allPlayers.get(MyRandom.getRandom().nextInt(allPlayers.size())));
    }
    
    private static void setPlayersFirstTurn(Player goesFirst) 
    { 
        String message = goesFirst + " has won the coin toss.";
        if ( goesFirst.isHuman() ) {
            if( !humanPlayOrDraw(message) );
            
        } else {
            computerPlayOrDraw(message);
        }
        Singletons.getModel().getGame().getPhaseHandler().setPlayerTurn(goesFirst);
    } // seeWhoPlaysFirstDice()

    private static boolean humanPlayOrDraw(String message) {
        final Object[] possibleValues = { "Play", "Draw" };
        
        final Object playDraw = JOptionPane.showOptionDialog(null, message + "\n\nWould you like to play or draw?", 
                "Play or Draw?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, 
                possibleValues, possibleValues[0]);
        
        return !playDraw.equals(1);
    }
    
    private static void computerPlayOrDraw(String message) {
        JOptionPane.showMessageDialog(null, message + "\nComputer Going First", 
                "Play or Draw?", JOptionPane.INFORMATION_MESSAGE);
    }
}