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
package forge.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import forge.GuiBase;
import forge.LobbyPlayer;
import forge.card.CardCharacteristicName;
import forge.game.Game;
import forge.game.GameType;
import forge.game.PlanarDice;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.card.CounterType;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.AbilityManaPart;
import forge.game.spellability.SpellAbility;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.match.input.Input;
import forge.match.input.InputPassPriority;
import forge.match.input.InputSelectCardsFromList;
import forge.model.FModel;
import forge.player.HumanPlay;
import forge.properties.ForgeConstants;
import forge.properties.ForgePreferences.FPref;
import forge.util.gui.SGuiChoose;
import forge.util.gui.SGuiDialog;
import forge.util.gui.SOptionPane;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public final class GuiDisplayUtil {
    private GuiDisplayUtil() {
        throw new AssertionError();
    }

    public static void devModeGenerateMana() {
        Player pPriority = getGame().getPhaseHandler().getPriorityPlayer();
        if (pPriority == null) {
            SGuiDialog.message("No player has priority at the moment, so mana cannot be added to their pool.");
            return;
        }

        final Card dummy = new Card(-777777);
        dummy.setOwner(pPriority);
        Map<String, String> produced = new HashMap<String, String>();
        produced.put("Produced", "W W W W W W W U U U U U U U B B B B B B B G G G G G G G R R R R R R R 7");
        final AbilityManaPart abMana = new AbilityManaPart(dummy, produced);
        getGame().getAction().invoke(new Runnable() {
            @Override public void run() { abMana.produceMana(null); }
        });
    }

    public static void devSetupGameState() {
        int humanLife = -1;
        int computerLife = -1;

        final Map<ZoneType, String> humanCardTexts = new EnumMap<ZoneType, String>(ZoneType.class);
        final Map<ZoneType, String> aiCardTexts = new EnumMap<ZoneType, String>(ZoneType.class);

        String tChangePlayer = "NONE";
        String tChangePhase = "NONE";

        File gamesDir = new File(ForgeConstants.USER_GAMES_DIR);
        if (!gamesDir.exists()) { // if the directory does not exist, try to create it
            gamesDir.mkdir();
        }
        
        String filename = GuiBase.getInterface().showFileDialog("Select Game State File", ForgeConstants.USER_GAMES_DIR);
        if (filename == null) {
            return;
        }

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

                if (categoryName.equals("humanlife"))                   humanLife = Integer.parseInt(categoryValue);
                else if (categoryName.equals("ailife"))                 computerLife = Integer.parseInt(categoryValue);

                else if (categoryName.equals("activeplayer"))           tChangePlayer = categoryValue.trim().toLowerCase();
                else if (categoryName.equals("activephase"))            tChangePhase = categoryValue;

                else if (categoryName.equals("humancardsinplay"))       humanCardTexts.put(ZoneType.Battlefield, categoryValue);
                else if (categoryName.equals("aicardsinplay"))          aiCardTexts.put(ZoneType.Battlefield, categoryValue);
                else if (categoryName.equals("humancardsinhand"))       humanCardTexts.put(ZoneType.Hand, categoryValue);
                else if (categoryName.equals("aicardsinhand"))          aiCardTexts.put(ZoneType.Hand, categoryValue);
                else if (categoryName.equals("humancardsingraveyard"))  humanCardTexts.put(ZoneType.Graveyard, categoryValue);
                else if (categoryName.equals("aicardsingraveyard"))     aiCardTexts.put(ZoneType.Graveyard, categoryValue);
                else if (categoryName.equals("humancardsinlibrary"))    humanCardTexts.put(ZoneType.Library, categoryValue);
                else if (categoryName.equals("aicardsinlibrary"))       aiCardTexts.put(ZoneType.Library, categoryValue);
                else if (categoryName.equals("humancardsinexile"))      humanCardTexts.put(ZoneType.Exile, categoryValue);
                else if (categoryName.equals("aicardsinexile"))         aiCardTexts.put(ZoneType.Exile, categoryValue);

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

        setupGameState(humanLife, computerLife, humanCardTexts, aiCardTexts, tChangePlayer, tChangePhase);
    }

    private static void setupGameState(final int humanLife, final int computerLife, final Map<ZoneType, String> humanCardTexts,
            final Map<ZoneType, String> aiCardTexts, final String tChangePlayer, final String tChangePhase) {

        final Game game = getGame();
        Player pPriority = game.getPhaseHandler().getPriorityPlayer();
        if (pPriority == null) {
            SGuiDialog.message("No player has priority at the moment, so game state cannot be setup.");
            return;
        }
        game.getAction().invoke(new Runnable() {
            @Override
            public void run() {
                final Player human = game.getPlayers().get(0);
                final Player ai = game.getPlayers().get(1);

                Player newPlayerTurn = tChangePlayer.equals("human") ? newPlayerTurn = human : tChangePlayer.equals("ai") ? newPlayerTurn = ai : null;
                PhaseType newPhase = tChangePhase.trim().equalsIgnoreCase("none") ? null : PhaseType.smartValueOf(tChangePhase);

                game.getPhaseHandler().devModeSet(newPhase, newPlayerTurn);

                game.getTriggerHandler().suppressMode(TriggerType.ChangesZone);

                devSetupPlayerState(humanLife, humanCardTexts, human);
                devSetupPlayerState(computerLife, aiCardTexts, ai);

                game.getTriggerHandler().clearSuppression(TriggerType.ChangesZone);

                game.getAction().checkStaticAbilities();
            }
        });
    }

    private static void devSetupPlayerState(int life, Map<ZoneType, String> cardTexts, final Player p) {
        Map<ZoneType, List<Card>> humanCards = new EnumMap<ZoneType, List<Card>>(ZoneType.class);
        for(Entry<ZoneType, String> kv : cardTexts.entrySet()) {
            humanCards.put(kv.getKey(), GuiDisplayUtil.devProcessCardsForZone(kv.getValue().split(";"), p));
        }

        if (life > 0) p.setLife(life, null);
        for (Entry<ZoneType, List<Card>> kv : humanCards.entrySet()) {
            if (kv.getKey() == ZoneType.Battlefield) {
                for (final Card c : kv.getValue()) {
                    p.getZone(ZoneType.Hand).add(c);
                    p.getGame().getAction().moveToPlay(c);
                    c.setSickness(false);
                }
            } else {
                p.getZone(kv.getKey()).setCards(kv.getValue());
            }
        }
    }

    /**
     * <p>
     * devProcessCardsForZone.
     * </p>
     * 
     * @param data
     *            an array of {@link java.lang.String} objects.
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @return a {@link forge.CardList} object.
     */
    private static List<Card> devProcessCardsForZone(final String[] data, final Player player) {
        final List<Card> cl = new ArrayList<Card>();
        for (final String element : data) {
            final String[] cardinfo = element.trim().split("\\|");

            final Card c = Card.fromPaperCard(FModel.getMagicDb().getCommonCards().getCard(cardinfo[0]), player);

            boolean hasSetCurSet = false;
            for (final String info : cardinfo) {
                if (info.startsWith("Set:")) {
                    c.setCurSetCode(info.substring(info.indexOf(':') + 1));
                    hasSetCurSet = true;
                } else if (info.equalsIgnoreCase("Tapped:True")) {
                    c.tap();
                } else if (info.startsWith("Counters:")) {
                    final String[] counterStrings = info.substring(info.indexOf(':') + 1).split(",");
                    for (final String counter : counterStrings) {
                        c.addCounter(CounterType.valueOf(counter), 1, true);
                    }
                } else if (info.equalsIgnoreCase("SummonSick:True")) {
                    c.setSickness(true);
                } else if (info.equalsIgnoreCase("FaceDown:True")) {
                    c.setState(CardCharacteristicName.FaceDown);
                }
            }

            if (!hasSetCurSet) {
                c.setCurSetCode(c.getMostRecentSet());
            }

            cl.add(c);
        }
        return cl;
    }

    /**
     * <p>
     * devModeTutor.
     * </p>
     * 
     * @since 1.0.15
     */
    public static void devModeTutor() {
        Player pPriority = getGame().getPhaseHandler().getPriorityPlayer();
        if (pPriority == null) {
            SGuiDialog.message("No player has priority at the moment, so their deck can't be tutored from.");
            return;
        }

        final List<Card> lib = pPriority.getCardsIn(ZoneType.Library);
        final Card card = SGuiChoose.oneOrNone("Choose a card", lib);
        if (card == null) { return; }

        getGame().getAction().invoke(new Runnable() {
            @Override
            public void run() {
                getGame().getAction().moveToHand(card);
            }
        });
    }

    /**
     * <p>
     * devModeAddCounter.
     * </p>
     * 
     * @since 1.0.15
     */
    public static void devModeAddCounter() {
        final Card card = SGuiChoose.oneOrNone("Add counters to which card?", getGame().getCardsIn(ZoneType.Battlefield));
        if (card == null) { return; }

        final CounterType counter = SGuiChoose.oneOrNone("Which type of counter?", CounterType.values());
        if (counter == null) { return; }

        final Integer count = SGuiChoose.getInteger("How many counters?", 1, Integer.MAX_VALUE, 10);
        if (count == null) { return; }
        
        card.addCounter(counter, count, false);
    }

    /**
     * <p>
     * devModeTapPerm.
     * </p>
     * 
     * @since 1.0.15
     */
    public static void devModeTapPerm() {
        final Game game = getGame();
        game.getAction().invoke(new Runnable() {
            @Override
            public void run() {
                final List<Card> untapped = CardLists.filter(game.getCardsIn(ZoneType.Battlefield), Predicates.not(CardPredicates.Presets.TAPPED));
                InputSelectCardsFromList inp = new InputSelectCardsFromList(0, Integer.MAX_VALUE, untapped);
                inp.setCancelAllowed(true);
                inp.setMessage("Choose permanents to tap");
                inp.showAndWait();
                if (!inp.hasCancelled()) {
                    for (Card c : inp.getSelected()) {
                        c.tap();
                    }
                }
            }
        });
    }

    /**
     * <p>
     * devModeUntapPerm.
     * </p>
     * 
     * @since 1.0.15
     */
    public static void devModeUntapPerm() {
        final Game game = getGame();
        game.getAction().invoke(new Runnable() {
            @Override
            public void run() {
                final List<Card> tapped = CardLists.filter(game.getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.TAPPED);
                InputSelectCardsFromList inp = new InputSelectCardsFromList(0, Integer.MAX_VALUE, tapped);
                inp.setCancelAllowed(true);
                inp.setMessage("Choose permanents to untap");
                inp.showAndWait();
                if( !inp.hasCancelled() )
                    for(Card c : inp.getSelected())
                        c.untap();
            }
        });
    }

    /**
     * <p>
     * devModeSetLife.
     * </p>
     * 
     * @since 1.1.3
     */
    public static void devModeSetLife() {
        final List<Player> players = getGame().getPlayers();
        final Player player = SGuiChoose.oneOrNone("Set life for which player?", players);
        if (player == null) { return; }

        final Integer life = SGuiChoose.getInteger("Set life to what?", 0);
        if (life == null) { return; }

        player.setLife(life, null);
    }

    /**
     * <p>
     * devModeWinGame.
     * </p>
     * 
     * @since 1.5.23
     */
    public static void devModeWinGame() {
        Input input = GuiBase.getInterface().getInputQueue().getInput();
        if (!(input instanceof InputPassPriority)) {
            SOptionPane.showMessageDialog("You must have priority to use this feature.", "Win Game", SOptionPane.INFORMATION_ICON);
            return;
        }

        //set life of all other players to 0
        LobbyPlayer guiPlayer = GuiBase.getInterface().getGuiPlayer();
        final List<Player> players = getGame().getPlayers();
        for (Player player : players) {
            if (player.getLobbyPlayer() != guiPlayer) {
                player.setLife(0, null);
            }
        }

        //pass priority so that causes gui player to win
        input.selectButtonOK();
    }

    /**
     * <p>
     * devModeTutorAnyCard.
     * </p>
     * 
     * @since 1.2.7
     */
    public static void devModeCardToHand() {
        final List<Player> players = getGame().getPlayers();
        final Player p = SGuiChoose.oneOrNone("Put card in hand for which player?", players);
        if (null == p) {
            return;
        }

        final List<PaperCard> cards =  Lists.newArrayList(FModel.getMagicDb().getCommonCards().getUniqueCards());
        Collections.sort(cards);

        // use standard forge's list selection dialog
        final IPaperCard c = SGuiChoose.oneOrNone("Name the card", cards);
        if (c == null) {
            return;
        }

        getGame().getAction().invoke(new Runnable() { @Override public void run() {
            getGame().getAction().moveToHand(Card.fromPaperCard(c, p));
        }});
    }

    public static void devModeCardToBattlefield() {
        final List<Player> players = getGame().getPlayers();
        final Player p = SGuiChoose.oneOrNone("Put card in play for which player?", players);
        if (null == p) {
            return;
        }

        final List<PaperCard> cards =  Lists.newArrayList(FModel.getMagicDb().getCommonCards().getUniqueCards());
        Collections.sort(cards);

        // use standard forge's list selection dialog
        final IPaperCard c = SGuiChoose.oneOrNone("Name the card", cards);
        if (c == null) {
            return;
        }

        final Game game = getGame();
        game.getAction().invoke(new Runnable() {
            @Override public void run() {
                final Card forgeCard = Card.fromPaperCard(c, p);

                if (c.getRules().getType().isLand()) {
                    game.getAction().moveToPlay(forgeCard);
                } else {
                    final List<SpellAbility> choices = forgeCard.getBasicSpells();
                    if (choices.isEmpty()) {
                        return; // when would it happen?
                    }

                    final SpellAbility sa = choices.size() == 1 ? choices.get(0) : SGuiChoose.oneOrNone("Choose", choices);
                    if (sa == null) {
                        return; // happens if cancelled
                    }

                    game.getAction().moveToHand(forgeCard); // this is really needed (for rollbacks at least)
                    // Human player is choosing targets for an ability controlled by chosen player.
                    sa.setActivatingPlayer(p);
                    HumanPlay.playSaWithoutPayingManaCost(game, sa, true);
                }
                game.getStack().addAllTriggeredAbilitiesToStack(); // playSa could fire some triggers
            }
        });
    }

    public static void devModeRiggedPlanarRoll() {
        final List<Player> players = getGame().getPlayers();
        final Player player = SGuiChoose.oneOrNone("Which player should roll?", players);
        if (player == null) { return; }

        final PlanarDice res = SGuiChoose.oneOrNone("Choose result", PlanarDice.values());
        if (res == null) { return; }

        System.out.println("Rigging planar dice roll: " + res.toString());

        //DBG
        //System.out.println("ActivePlanes: " + getGame().getActivePlanes());
        //System.out.println("CommandPlanes: " + getGame().getCardsIn(ZoneType.Command));

        

        getGame().getAction().invoke(new Runnable() {
            @Override
            public void run() {
                PlanarDice.roll(player, res);
            }
        });
    }

    public static void devModePlaneswalkTo() {
        final Game game = getGame();
        if (!game.getRules().hasAppliedVariant(GameType.Planechase)) { return; }
        final Player p = game.getPhaseHandler().getPlayerTurn();

        final List<PaperCard> allPlanars = new ArrayList<PaperCard>();
        for (PaperCard c : FModel.getMagicDb().getVariantCards().getAllCards()) {
            if (c.getRules().getType().isPlane() || c.getRules().getType().isPhenomenon()) {
                allPlanars.add(c);
            }
        }
        Collections.sort(allPlanars);

        // use standard forge's list selection dialog
        final IPaperCard c = SGuiChoose.oneOrNone("Name the card", allPlanars);
        if (c == null) { return; }
        final Card forgeCard = Card.fromPaperCard(c, p);

        forgeCard.setOwner(p);
        getGame().getAction().invoke(new Runnable() { 
            @Override
            public void run() {
                getGame().getAction().changeZone(null, p.getZone(ZoneType.PlanarDeck), forgeCard, 0);
                PlanarDice.roll(p, PlanarDice.Planeswalk);
            }
        });
    }

    private static Game getGame() {
        return GuiBase.getInterface().getGame();
    }

    public static String getPlayerName() {
        return FModel.getPreferences().getPref(FPref.PLAYER_NAME);
    }

    public static String personalizeHuman(String text) {
        String playerName = FModel.getPreferences().getPref(FPref.PLAYER_NAME);
        return text.replaceAll("(?i)human", playerName);
    }

    public static String getVariantDescription(GameType variant) {
        switch (variant) {
        case Archenemy:
            return "One player is the Archenemy and can play scheme cards.";
        case ArchenemyRumble:
            return "All players are Archenemies and can play scheme cards.";
        case Commander:
            return "Each player has a legendary \"General\" card which can be cast at any time and determines deck colors.";
        case MomirBasic:
            return "Each player has a deck containing 60 basic lands and the Momir Vig avatar.";
        case Planechase:
            return "Plane cards apply global effects. Plane card changed when a player rolls \"Chaos\" on the planar die.";
        case Vanguard:
            return "Each player has a special \"Avatar\" card that affects the game.";
        default:
            return ""; //other game types don't need descriptions
        }
    }
} // end class GuiDisplayUtil
