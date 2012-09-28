package forge.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JOptionPane;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.AllZone;
import forge.Card;
import forge.CardList;
import forge.CardPredicates.Presets;
import forge.CardPredicates;
import forge.CardUtil;
import forge.Constant;
import forge.GameAction;
import forge.Singletons;
import forge.control.FControl;
import forge.control.input.InputMulligan;
import forge.deck.Deck;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.match.CMatchUI;
import forge.gui.match.VMatchUI;
import forge.gui.match.controllers.CMessage;
import forge.gui.match.nonsingleton.VField;
import forge.gui.match.views.VAntes;
import forge.gui.toolbox.FLabel;
import forge.item.CardPrinted;
import forge.properties.ForgePreferences.FPref;
import forge.properties.ForgeProps;
import forge.properties.NewConstants.Lang.GameAction.GameActionText;
import forge.util.Aggregates;
import forge.util.MyRandom;

/** 
 * Methods for all things related to starting a new game.
 * All of these methods can and should be static.
 */
public class GameNew {
    /**
     * Constructor for new game allowing card lists to be put into play
     * immediately, and life totals to be adjusted, for computer and human.
     * 
     * @param humanDeck
     *              &emsp; {@link forge.deck.Deck} object.
     * @param computerDeck
     *              &emsp; {@link forge.deck.Deck} object.
     * @param human
     *              &emsp; {@link forge.CardList} object.
     * @param computer
     *              &emsp; {@link forge.CardList} object.
     * @param humanLife
     *              &emsp; int.
     * @param computerLife
     *              &emsp; int.
     * @param iconEnemy
     *              &emsp; {@link java.lang.String}
     */
    public static void newGame(final Deck humanDeck, final Deck computerDeck, final CardList humanStart,
            final CardList computerStart, final int humanLife, final int computerLife, String iconEnemy) {
        Singletons.getControl().changeState(FControl.MATCH_SCREEN);
        CMatchUI.SINGLETON_INSTANCE.initMatch(iconEnemy);

        GameNew.newGameCleanup();
        GameNew.newMatchCleanup();

        Player human = AllZone.getHumanPlayer();
        Player computer = AllZone.getComputerPlayer();
        
        computer.setStartingLife(computerLife);
        human.setStartingLife(humanLife);
        human.updateObservers();

        Card.resetUniqueNumber();

        PlayerZone humanBf = human.getZone(ZoneType.Battlefield);
        if (humanStart != null) {
            for (final Card c : humanStart) {
                humanBf.add(c, false);
                c.setSickness(true);
                c.setStartsGameInPlay(true);
                c.refreshUniqueNumber();
            }
        }

        PlayerZone aiBf = AllZone.getComputerPlayer().getZone(ZoneType.Battlefield);
        if (computerStart != null) {        
            for (final Card c : computerStart) {
                aiBf.add(c, false);
                c.setSickness(true);
                c.setStartsGameInPlay(true);
                c.refreshUniqueNumber();
            }
        }

        GameNew.actuateGame(humanDeck, computerDeck);
        humanBf.updateObservers();
        aiBf.updateObservers();
    }

    /**
     * The default constructor for a new game.
     * 
     * @param humanDeck
     *            &emsp; {@link forge.deck.Deck} object.
     * @param computerDeck
     *            &emsp; {@link forge.deck.Deck} object.
     */
    public static void newGame(final Deck humanDeck, final Deck computerDeck) {
        newGame(humanDeck, computerDeck, null, null, 20, 20, null);
    }

    /**
     * This must be separated from the newGame method since life totals and
     * player details could be adjusted before the game is started.
     * 
     * That process (also cleanup and observer updates) should be done in
     * newGame, then when all is ready, call this function.
     */
    private static void actuateGame(final Deck humanDeck, final Deck computerDeck) {
        forge.card.trigger.Trigger.resetIDs();
        AllZone.getTriggerHandler().clearTriggerSettings();
        AllZone.getTriggerHandler().clearDelayedTrigger();
        CMessage.SINGLETON_INSTANCE.updateGameInfo();

        // friendliness
        final boolean canRandomFoil = Constant.Runtime.RANDOM_FOIL[0]
                && Constant.Runtime.getGameType().equals(GameType.Constructed);
        final Random generator = MyRandom.getRandom();

        final ArrayList<String> hAnteRemoved = new ArrayList<String>();
        final ArrayList<String> cAnteRemoved = new ArrayList<String>();

        for (final Entry<CardPrinted, Integer> stackOfCards : humanDeck.getMain()) {
            final CardPrinted cardPrinted = stackOfCards.getKey();
            for (int i = 0; i < stackOfCards.getValue(); i++) {

                final Card card = cardPrinted.toForgeCard(AllZone.getHumanPlayer());

                // Assign random foiling on approximately 1:20 cards
                if (cardPrinted.isFoil() || (canRandomFoil && MyRandom.percentTrue(5))) {
                    final int iFoil = MyRandom.getRandom().nextInt(9) + 1;
                    card.setFoil(iFoil);
                }

                if (!Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE)
                        && card.hasKeyword("Remove CARDNAME from your deck before playing if you're not playing for ante.")) {
                    hAnteRemoved.add(card.getName());
                } else {
                    AllZone.getHumanPlayer().getZone(ZoneType.Library).add(card);
                }
            }
        }
        final ArrayList<String> rAICards = new ArrayList<String>();
        for (final Entry<CardPrinted, Integer> stackOfCards : computerDeck.getMain()) {
            final CardPrinted cardPrinted = stackOfCards.getKey();
            for (int i = 0; i < stackOfCards.getValue(); i++) {

                final Card card = cardPrinted.toForgeCard(AllZone.getComputerPlayer());
                final int cntVariants = cardPrinted.getCard().getEditionInfo(cardPrinted.getEdition()).getCopiesCount();
                if (cntVariants > 1) {
                    card.setRandomPicture(generator.nextInt(cntVariants - 1) + 1);
                    card.setImageFilename(CardUtil.buildFilename(card));
                }

                // Assign random foiling on approximately 1:20 cards
                if (cardPrinted.isFoil() || (canRandomFoil && MyRandom.percentTrue(5))) {
                    final int iFoil = MyRandom.getRandom().nextInt(9) + 1;
                    card.setFoil(iFoil);
                }

                if (!Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE)
                        && card.hasKeyword("Remove CARDNAME from your deck before playing if you're not playing for ante.")) {
                    cAnteRemoved.add(card.getName());
                } else {
                    AllZone.getComputerPlayer().getZone(ZoneType.Library).add(card);
                }

                if (card.getSVar("RemAIDeck").equals("True") && !rAICards.contains(card.getName())) {
                    rAICards.add(card.getName());
                    // get card picture so that it is in the image cache
                    // ImageCache.getImage(card);
                }

            }
        }

        if (rAICards.size() > 0) {
            final StringBuilder sb = new StringBuilder(
                    "AI deck contains the following cards that it can't play or may be buggy:\n");
            for (int i = 0; i < rAICards.size(); i++) {
                sb.append(rAICards.get(i));
                if (((i % 4) == 0) && (i > 0)) {
                    sb.append("\n");
                } else if (i != (rAICards.size() - 1)) {
                    sb.append(", ");
                }
            }

            JOptionPane.showMessageDialog(null, sb.toString(), "", JOptionPane.INFORMATION_MESSAGE);

        }
        if (hAnteRemoved.size() > 0) {
            final StringBuilder sb = new StringBuilder("The following ante cards were removed from the human's deck:\n");
            for (int i = 0; i < hAnteRemoved.size(); i++) {
                sb.append(hAnteRemoved.get(i));
                if (((i % 4) == 0) && (i > 0)) {
                    sb.append("\n");
                } else if (i != (hAnteRemoved.size() - 1)) {
                    sb.append(", ");
                }
            }

            JOptionPane.showMessageDialog(null, sb.toString(), "", JOptionPane.INFORMATION_MESSAGE);

        }
        if (cAnteRemoved.size() > 0) {
            final StringBuilder sb = new StringBuilder(
                    "The following ante cards were removed from the computer's deck:\n");
            for (int i = 0; i < cAnteRemoved.size(); i++) {
                sb.append(cAnteRemoved.get(i));
                if (((i % 4) == 0) && (i > 0)) {
                    sb.append("\n");
                } else if (i != (cAnteRemoved.size() - 1)) {
                    sb.append(", ");
                }
            }

            JOptionPane.showMessageDialog(null, sb.toString(), "", JOptionPane.INFORMATION_MESSAGE);

        }

        for (int i = 0; i < 100; i++) {
            AllZone.getHumanPlayer().shuffle();
        }

        // do this instead of shuffling Computer's deck
        final boolean smoothLand = Constant.Runtime.SMOOTH[0];

        if (smoothLand) {
            final Iterable<Card> c1 = GameNew.smoothComputerManaCurve(AllZone.getComputerPlayer().getCardsIn(ZoneType.Library));
            AllZone.getComputerPlayer().getZone(ZoneType.Library).setCards(c1);
        } else {
            // WTF? (it was so before refactor)
            AllZone.getComputerPlayer().getZone(ZoneType.Library)
                    .setCards(AllZone.getComputerPlayer().getCardsIn(ZoneType.Library));
            AllZone.getComputerPlayer().shuffle();
        }

        // Only cut/coin toss if it's the first game of the match
        if (Singletons.getModel().getMatchState().getGamesPlayedCount() == 0) {
            // New code to determine who goes first. Delete this if it doesn't
            // work properly
            if (Singletons.getModel().getGameAction().isStartCut()) {
                GameNew.seeWhoPlaysFirst();
            } else {
                GameNew.seeWhoPlaysFirstDice();
            }
        } else if (Singletons.getModel().getMatchState().hasWonLastGame(AllZone.getHumanPlayer().getName())) {
            // if player won last, AI starts
            GameNew.computerPlayOrDraw("Computer lost the last game.");
        } else {
            GameNew.humanPlayOrDraw("Human lost the last game.");
        }

        if (Singletons.getModel().getPreferences().getPrefBoolean(FPref.UI_ANTE)) {
            final String nl = System.getProperty("line.separator");
            final StringBuilder msg = new StringBuilder();
            for (final Player p : AllZone.getPlayersInGame()) {
                final List<Card> lib = p.getCardsIn(ZoneType.Library);
                Predicate<Card> goodForAnte = Predicates.not(CardPredicates.Presets.BASIC_LANDS);
                Card ante = Aggregates.random(Iterables.filter(lib, goodForAnte));
                if (ante == null ) {
                    if (!lib.isEmpty()) 
                        ante = lib.get(0);
                    else
                        throw new RuntimeException(p + " library is empty.");                        
                }
                AllZone.getGameLog().add("Ante", p + " anted " + ante, 0);
                VAntes.SINGLETON_INSTANCE.addAnteCard(p, ante);
                Singletons.getModel().getGameAction().moveTo(ZoneType.Ante, ante);
                msg.append(p.getName()).append(" ante: ").append(ante).append(nl);
            }
            JOptionPane.showMessageDialog(null, msg, "Ante", JOptionPane.INFORMATION_MESSAGE);
        }

        for (int i = 0; i < 7; i++) {
            AllZone.getHumanPlayer().drawCard();
            AllZone.getComputerPlayer().drawCard();
        }

        CMatchUI.SINGLETON_INSTANCE.setCard(AllZone.getHumanPlayer().getCardsIn(ZoneType.Hand).get(0));

        AllZone.getInputControl().setInput(new InputMulligan());
    } // newGame()

    private static void newGameCleanup() {
        final GameState gs = Singletons.getModel().getGameState();

        gs.setGameSummary(
                new GameSummary(gs.getHumanPlayer().getName(), gs.getComputerPlayer().getName()));

        gs.getPhaseHandler().reset();
        gs.getStack().reset();
        gs.getCombat().reset();
        gs.getEndOfTurn().reset();
        gs.getEndOfCombat().reset();
        gs.getUntap().reset();
        gs.getUpkeep().reset();
        gs.getGameLog().reset();
        gs.setGameOver(false);

        for (final Player p : gs.getPlayers()) {
            p.reset();
            for (final ZoneType z : Player.ALL_ZONES) {
                p.getZone(z).reset();
            }
        }

        gs.getStaticEffects().reset();

        AllZone.getInputControl().clearInput();
        AllZone.getColorChanger().reset();
    }

    private static void newMatchCleanup() {
        if (Singletons.getModel().getMatchState().getGamesPlayedCount() != 0) { return; }

        // Update mouse events in case of dev mode toggle
        if (Constant.Runtime.DEV_MODE[0]) {
            // TODO restore this functionality!!!
            //VMatchUI.SINGLETON_INSTANCE.getViewDevMode().getDocument().setVisible(true);
            final List<VField> allFields = VMatchUI.SINGLETON_INSTANCE.getFieldViews();

            for (final VField field : allFields) {
                ((FLabel) field.getLblHand()).setHoverable(true);
                ((FLabel) field.getLblLibrary()).setHoverable(true);
            }
        }
        else {
            // TODO restore this functionality!!!
            //VMatchUI.SINGLETON_INSTANCE.getViewDevMode().getDocument().setVisible(false);
            final List<VField> allFields = VMatchUI.SINGLETON_INSTANCE.getFieldViews();

            for (final VField field : allFields) {
                ((FLabel) field.getLblHand()).setHoverable(false);
                ((FLabel) field.getLblLibrary()).setHoverable(false);
            }
        }

        VAntes.SINGLETON_INSTANCE.clearAnteCards();
        AllZone.getInputControl().resetInput();
        Singletons.getModel().getMatchState().reset();
        Singletons.getModel().loadPrefs();
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
        final CardList library = new CardList(in);
        library.shuffle();

        // remove all land, keep non-basicland in there, shuffled
        CardList land = library.filter(CardPredicates.Presets.LANDS);
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
        
        if (playerDie > computerDie) {
            humanPlayOrDraw(dieRollMessage(playerDie, computerDie));
        }
        else {
            computerPlayOrDraw(dieRollMessage(playerDie, computerDie));
        }
    } // seeWhoPlaysFirstDice()

    /**
     * <p>
     * seeWhoPlaysFirst.
     * </p>
     */
    private static void seeWhoPlaysFirst() {
        final GameAction ga = Singletons.getModel().getGameAction();
        Predicate<Card> nonLand = Predicates.not(Presets.LANDS);
        Iterable<Card> hLibrary = Iterables.filter(AllZone.getHumanPlayer().getCardsIn(ZoneType.Library), nonLand);
        Iterable<Card> cLibrary = Iterables.filter(AllZone.getComputerPlayer().getCardsIn(ZoneType.Library), nonLand);


        final boolean starterDetermined = false;
        int cutCount = 0;
        final int cutCountMax = 20;
        for (int i = 0; i < cutCountMax; i++) {
            if (starterDetermined) {
                break;
            }

            Card hRandom = Aggregates.random(hLibrary);
            if ( null != hRandom ) {
                ga.setHumanCut(hRandom);
            } else {
                GameNew.computerStartsGame();
                JOptionPane.showMessageDialog(null, ForgeProps.getLocalized(GameActionText.HUMAN_MANA_COST) + "\r\n"
                        + ForgeProps.getLocalized(GameActionText.COMPUTER_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Card cRandom = Aggregates.random(cLibrary);
            if (cRandom != null) {
                ga.setComputerCut(cRandom);
            } else {
                JOptionPane.showMessageDialog(null, ForgeProps.getLocalized(GameActionText.COMPUTER_MANA_COST) + "\r\n"
                        + ForgeProps.getLocalized(GameActionText.HUMAN_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            cutCount = cutCount + 1;
            ga.moveTo(AllZone.getHumanPlayer().getZone(ZoneType.Library),
                    ga.getHumanCut());
            ga.moveTo(AllZone.getComputerPlayer().getZone(ZoneType.Library),
                    ga.getComputerCut());

            final StringBuilder sb = new StringBuilder();
            sb.append(ForgeProps.getLocalized(GameActionText.HUMAN_CUT) + ga.getHumanCut().getName() + " ("
                    + ga.getHumanCut().getManaCost() + ")" + "\r\n");
            sb.append(ForgeProps.getLocalized(GameActionText.COMPUTER_CUT) + ga.getComputerCut().getName() + " ("
                    + ga.getComputerCut().getManaCost() + ")" + "\r\n");
            sb.append("\r\n" + "Number of times the deck has been cut: " + cutCount + "\r\n");
            if (ga.getComputerCut().getManaCost().getCMC() > ga.getHumanCut().getManaCost().getCMC()) {
                GameNew.computerStartsGame();
                JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GameActionText.COMPUTER_STARTS), "",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            } else if (ga.getComputerCut().getManaCost().getCMC() < ga.getHumanCut().getManaCost().getCMC()) {
                JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GameActionText.HUMAN_STARTS), "",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                sb.append(ForgeProps.getLocalized(GameActionText.EQUAL_CONVERTED_MANA) + "\r\n");
                if (i == (cutCountMax - 1)) {
                    sb.append(ForgeProps.getLocalized(GameActionText.RESOLVE_STARTER));
                    if (MyRandom.getRandom().nextInt(2) == 1) {
                        JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GameActionText.HUMAN_WIN), "",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        GameNew.computerStartsGame();
                        JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GameActionText.COMPUTER_WIN),
                                "", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return;
                } else {
                    sb.append(ForgeProps.getLocalized(GameActionText.CUTTING_AGAIN));
                }
                JOptionPane.showMessageDialog(null, sb, "", JOptionPane.INFORMATION_MESSAGE);
            }
        } // for-loop for multiple card cutting

    } // seeWhoPlaysFirst()


    private static void computerStartsGame() {
        final Player computer = AllZone.getComputerPlayer();
        Singletons.getModel().getGameState().getPhaseHandler().setPlayerTurn(computer);
        // AllZone.getGameInfo().setPlayerWhoGotFirstTurn(computer.getName());
    }
    
    private static String dieRollMessage(int playerDie, int computerDie) {
        StringBuilder sb = new StringBuilder();
        sb.append("Human has rolled a: ");
        sb.append(playerDie);
        sb.append(". ");
        sb.append("Computer has rolled a: ");
        sb.append(computerDie);
        sb.append(".");
        return sb.toString();
    }
    
    private static void humanPlayOrDraw(String message) {
        final Object[] possibleValues = { "Play", "Draw" };
        
        final Object playDraw = JOptionPane.showOptionDialog(null, message + "\n\nWould you like to play or draw?", 
                "Play or Draw?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, 
                possibleValues, possibleValues[0]);
        
        if (playDraw.equals(1)) {
            computerStartsGame();
        }
    }
    
    private static void computerPlayOrDraw(String message) {
        JOptionPane.showMessageDialog(null, message + "\nComputer Going First", 
                "Play or Draw?", JOptionPane.INFORMATION_MESSAGE);
        
        computerStartsGame();
    }  
}