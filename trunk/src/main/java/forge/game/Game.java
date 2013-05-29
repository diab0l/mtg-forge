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
package forge.game;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.eventbus.EventBus;

import forge.Card;
import forge.CardLists;
import forge.ColorChanger;
import forge.GameLog;
import forge.Singletons;
import forge.StaticEffects;
import forge.card.replacement.ReplacementHandler;
import forge.card.spellability.Ability;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityStackInstance;
import forge.card.trigger.TriggerHandler;
import forge.card.trigger.TriggerType;
import forge.game.event.GameEvent;
import forge.game.event.GameEventDuelOutcome;
import forge.game.phase.Cleanup;
import forge.game.phase.Combat;
import forge.game.phase.EndOfCombat;
import forge.game.phase.EndOfTurn;
import forge.game.phase.PhaseHandler;
import forge.game.phase.Untap;
import forge.game.phase.Upkeep;
import forge.game.player.Player;
import forge.game.zone.MagicStack;
import forge.game.zone.PlayerZone;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;

    /**
 * Represents the state of a <i>single game</i>, a new instance is created for each game.
 */
public class Game {
    private final GameType type;
    private final List<Player> roIngamePlayers;
    private final List<Player> allPlayers;
    private final List<Player> ingamePlayers = new ArrayList<Player>();
    
    private List<Card> activePlanes = null;
    
    public final Cleanup cleanup;
    public final EndOfTurn endOfTurn;
    public final EndOfCombat endOfCombat;
    public final Untap untap;
    public final Upkeep upkeep;
    private final PhaseHandler phaseHandler;
    public final MagicStack stack;
    private final StaticEffects staticEffects = new StaticEffects();
    private final TriggerHandler triggerHandler = new TriggerHandler(this);
    private final ReplacementHandler replacementHandler = new ReplacementHandler();
    private Combat combat = new Combat();
    private final EventBus events = new EventBus();
    private final GameLog gameLog = new GameLog();
    private final ColorChanger colorChanger = new ColorChanger();

    private final Zone stackZone = new Zone(ZoneType.Stack);

    private long timestamp = 0;
    public final GameAction action;
    private final Match match;
    private GameAge age = GameAge.BeforeMulligan;

    /**
     * Constructor.
     * @param players2.entrySet()
     * @param match0 
     * @param input 
     */
    public Game(List<RegisteredPlayer> players0, GameType t, Match match0) { /* no more zones to map here */
        type = t;
        match = match0;
        List<Player> players = new ArrayList<Player>();
        allPlayers = Collections.unmodifiableList(players);
        roIngamePlayers = Collections.unmodifiableList(ingamePlayers);
        
        for (RegisteredPlayer psc : players0) {
            Player pl = psc.getPlayer().getPlayer(this);
            players.add(pl);
            ingamePlayers.add(pl);

            pl.setStartingLife(psc.getStartingLife());
            pl.setMaxHandSize(psc.getStartingHand());
            pl.setStartingHandSize(psc.getStartingHand());
            pl.setTeam(psc.getTeamNumber());
        }

        
        
        action = new GameAction(this);
        stack = new MagicStack(this);
        phaseHandler = new PhaseHandler(this);

        untap = new Untap(this);
        upkeep = new Upkeep(this);
        cleanup = new Cleanup(this);
        endOfTurn = new EndOfTurn(this);
        endOfCombat = new EndOfCombat(this);
        
        if ( match0.getGameType() == GameType.Quest)
            events.register(Singletons.getModel().getQuest()); // this one listens to player's mulligans ATM

        events.register(gameLog);
    }
    

    /**
     * Gets the players who are still fighting to win.
     * 
     * @return the players
     */
    public final List<Player> getPlayers() {
        return roIngamePlayers;
    }
    /**
     * Gets the players who participated in match (regardless of outcome).
     * <i>Use this in UI and after match calculations</i>
     * 
     * @return the players
     */
    public final List<Player> getRegisteredPlayers() {
        return allPlayers;
    }

    /**
     * Gets the cleanup step.
     * 
     * @return the cleanup step
     */
    public final Cleanup getCleanup() {
        return this.cleanup;
    }

    /**
     * Gets the end of turn.
     * 
     * @return the endOfTurn
     */
    public final EndOfTurn getEndOfTurn() {
        return this.endOfTurn;
    }

    /**
     * Gets the end of combat.
     * 
     * @return the endOfCombat
     */
    public final EndOfCombat getEndOfCombat() {
        return this.endOfCombat;
    }

    /**
     * Gets the upkeep.
     * 
     * @return the upkeep
     */
    public final Upkeep getUpkeep() {
        return this.upkeep;
    }

    /**
     * Gets the untap.
     * 
     * @return the upkeep
     */
    public final Untap getUntap() {
        return this.untap;
    }

    /**
     * Gets the phaseHandler.
     * 
     * @return the phaseHandler
     */
    public final PhaseHandler getPhaseHandler() {
        return this.phaseHandler;
    }

    /**
     * Gets the stack.
     * 
     * @return the stack
     */
    public final MagicStack getStack() {
        return this.stack;
    }

    /**
     * Gets the static effects.
     * 
     * @return the staticEffects
     */
    public final StaticEffects getStaticEffects() {
        return this.staticEffects;
    }

    /**
     * Gets the trigger handler.
     * 
     * @return the triggerHandler
     */
    public final TriggerHandler getTriggerHandler() {
        return this.triggerHandler;
    }

    /**
     * Gets the combat.
     * 
     * @return the combat
     */
    public final Combat getCombat() {
        return this.combat;
    }

    /**
     * Sets the combat.
     * 
     * @param combat0
     *            the combat to set
     */
    public final void setCombat(final Combat combat0) {
        this.combat = combat0;
    }

    /**
     * Gets the game log.
     * 
     * @return the game log
     */
    public final GameLog getGameLog() {
        return this.gameLog;
    }

    /**
     * Gets the stack zone.
     * 
     * @return the stackZone
     */
    public final Zone getStackZone() {
        return this.stackZone;
    }

    /**
     * Create and return the next timestamp.
     * 
     * @return the next timestamp
     */
    public final long getNextTimestamp() {
        this.timestamp = this.getTimestamp() + 1;
        return this.getTimestamp();
    }

    /**
     * Gets the timestamp.
     * 
     * @return the timestamp
     */
    public final long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the replacementHandler
     */
    public ReplacementHandler getReplacementHandler() {
        return replacementHandler;
    }

    /**
     * @return the gameOver
     */
    public synchronized boolean isGameOver() {
        return age == GameAge.GameOver;
    }

    /**
     * @param reason 
     * @param go the gameOver to set
     */
    public synchronized void setGameOver(GameEndReason reason) {
        this.age = GameAge.GameOver;
        for (Player p : allPlayers ) {
            if (p.isMindSlaved()) 
                p.releaseControl(); // for correct totals
        }

        for (Player p : roIngamePlayers) {
            p.onGameOver();
        }
        
        final GameOutcome result = new GameOutcome(reason, getRegisteredPlayers());
        result.setTurnsPlayed(getPhaseHandler().getTurn());

        match.addGamePlayed(result);
        
        // The log shall listen to events and generate text internally
        fireEvent(new GameEventDuelOutcome(result, match.getPlayedGames()));
    }

    public Zone getZoneOf(final Card c) {
        if (getStackZone().contains(c)) {
            return getStackZone();
        }

        for (final Player p : getPlayers()) {
            for (final ZoneType z : Player.ALL_ZONES) {
                final PlayerZone pz = p.getZone(z);
                if (pz.contains(c)) {
                    return pz;
                }
            }
        }

        return null;
    }

    public boolean isCardInZone(final Card c, final ZoneType zone) {
         if (zone.equals(ZoneType.Stack)) {
            if (getStackZone().contains(c)) {
                return true;
            }
        } else {
            for (final Player p : getPlayers()) {
                if (p.getZone(zone).contains(c)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<Card> getCardsIn(final ZoneType zone) {
        if (zone == ZoneType.Stack) {
            return getStackZone().getCards();
        } else {
            List<Card> cards = new ArrayList<Card>();
            for (final Player p : getPlayers()) {
                cards.addAll(p.getZone(zone).getCards());
            }
            return cards;
        }
    }

    public List<Card> getCardsIn(final Iterable<ZoneType> zones) {
        final List<Card> cards = new ArrayList<Card>();
        for (final ZoneType z : zones) {
            cards.addAll(getCardsIn(z));
        }
        return cards;
    }

    public boolean isCardExiled(final Card c) {
        return getCardsIn(ZoneType.Exile).contains(c);
    }

    public boolean isCardInPlay(final String cardName) {
        for (final Player p : getPlayers()) {
            if (p.isCardInPlay(cardName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCardInCommand(final String cardName) {
        for (final Player p : getPlayers()) {
            if (p.isCardInCommand(cardName)) {
                return true;
            }
        }
        return false;
    }

    public List<Card> getColoredCardsInPlay(final String color) {
        final List<Card> cards = new ArrayList<Card>();
        for (Player p : getPlayers()) {
            cards.addAll(p.getColoredCardsInPlay(color));
        }
        return cards;
    }

    public Card getCardState(final Card card) {
        for (final Card c : getCardsInGame()) {
            if (card.equals(c)) {
                return c;
            }
        }

        return card;
    }

    /**
     * <p>
     * compareTypeAmountInPlay.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int compareTypeAmountInPlay(final Player player, final String type) {
        // returns the difference between player's
        final Player opponent = player.getOpponent();
        final List<Card> playerList = CardLists.getType(player.getCardsIn(ZoneType.Battlefield), type);
        final List<Card> opponentList = CardLists.getType(opponent.getCardsIn(ZoneType.Battlefield), type);
        return (playerList.size() - opponentList.size());
    }

    /**
     * <p>
     * compareTypeAmountInGraveyard.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int compareTypeAmountInGraveyard(final Player player, final String type) {
        // returns the difference between player's
        final Player opponent = player.getOpponent();
        final List<Card> playerList = CardLists.getType(player.getCardsIn(ZoneType.Graveyard), type);
        final List<Card> opponentList = CardLists.getType(opponent.getCardsIn(ZoneType.Graveyard), type);
        return (playerList.size() - opponentList.size());
    }

    public List<Card> getCardsInGame() {
        final List<Card> all = new ArrayList<Card>();
        for (final Player player : getPlayers()) {
            all.addAll(player.getZone(ZoneType.Graveyard).getCards());
            all.addAll(player.getZone(ZoneType.Hand).getCards());
            all.addAll(player.getZone(ZoneType.Library).getCards());
            all.addAll(player.getZone(ZoneType.Battlefield).getCards(false));
            all.addAll(player.getZone(ZoneType.Exile).getCards());
            all.addAll(player.getZone(ZoneType.Command).getCards());
        }
        all.addAll(getStackZone().getCards());
        return all;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public ColorChanger getColorChanger() {
        return colorChanger;
    }

    public final GameAction getAction() {
        return action;
    }

    public final Match getMatch() {
        return match;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param playerTurn
     * @return
     */
    public Player getNextPlayerAfter(final Player playerTurn) {
        int iPlayer = roIngamePlayers.indexOf(playerTurn);

        if (-1 == iPlayer && !roIngamePlayers.isEmpty()) { // if playerTurn has just lost
            int iAlive;
            iPlayer = allPlayers.indexOf(playerTurn);
            do {
                iPlayer = (iPlayer + 1) % allPlayers.size();
                iAlive = roIngamePlayers.indexOf(allPlayers.get(iPlayer));
            } while(iAlive < 0);
            iPlayer = iAlive;
        } else { // for the case noone has died
            if (iPlayer == roIngamePlayers.size() - 1) {
                iPlayer = -1;
            }
            iPlayer++;
        }

        return roIngamePlayers.get(iPlayer);

    }
    
    public int getPosition(Player player, Player startingPlayer) {
        int startPosition = roIngamePlayers.indexOf(startingPlayer);
        int position = (roIngamePlayers.indexOf(player) + startPosition) % roIngamePlayers.size() + 1;
        return position;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param p
     */
    public void onPlayerLost(Player p) {
        ingamePlayers.remove(p);

        final Map<String, Object> runParams = new TreeMap<String, Object>();
        runParams.put("Player", p);
        this.getTriggerHandler().runTrigger(TriggerType.LosesGame, runParams, false);

    }

    /**
     * Fire only the events after they became real for gamestate and won't get replaced.<br>
     * The events are sent to UI, log and sound system. Network listeners are under development. 
     */
    public void fireEvent(GameEvent event) {
        events.post(event);
    }
    public void subscribeToEvents(Object subscriber) {
        events.register(subscriber);
    }

    /**
     * @return the type of game (Constructed/Limited/Planechase/etc...)
     */
    public GameType getType() {
        return type;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @param player
     * @return
     */
    public List<SpellAbility> getAbilitesOfCard(Card c, Player player) {
        // this can only be called by the Human
        final Zone zone = this.getZoneOf(c);

        final List<SpellAbility> abilities = new ArrayList<SpellAbility>();
        for (SpellAbility sa : c.getSpellAbilities()) {
            //add alternative costs as additional spell abilities
            abilities.add(sa);
            abilities.addAll(GameActionUtil.getAlternativeCosts(sa));
        }

        for (int iSa = 0; iSa < abilities.size();) {
            SpellAbility sa = abilities.get(iSa); 
            sa.setActivatingPlayer(player);
            if (!sa.canPlay())
                abilities.remove(iSa);
            else
                iSa++;
        }

        if (c.isLand() && player.canPlayLand(c)) {
            if (zone.is(ZoneType.Hand) || (!zone.is(ZoneType.Battlefield) && c.hasStartOfKeyword("May be played"))) {
                Ability.PLAY_LAND_SURROGATE.setSourceCard(c);
                abilities.add(Ability.PLAY_LAND_SURROGATE);
            }
        }

        return abilities;
    }

    /**
     * @return the activePlane
     */
    public List<Card> getActivePlanes() {
        return activePlanes;
    }

    /**
     * @param activePlane0 the activePlane to set
     */
    public void setActivePlanes(List<Card> activePlane0) {
        this.activePlanes = activePlane0;
    }

    public void archenemy904_10() {
        //904.10. If a non-ongoing scheme card is face up in the
        //command zone, and it isn't the source of a triggered ability
        //that has triggered but not yet left the stack, that scheme card
        //is turned face down and put on the bottom of its owner's scheme
        //deck the next time a player would receive priority.
        //(This is a state-based action. See rule 704.)

        for (int i = 0; i < getCardsIn(ZoneType.Command).size(); i++) {
            Card c = getCardsIn(ZoneType.Command).get(i);
            if (c.isScheme() && !c.isType("Ongoing")) {

                boolean foundonstack = false;
                for (SpellAbilityStackInstance si : getStack()) {
                    if (si.getSourceCard().equals(c)) {

                        foundonstack = true;
                        break;
                    }
                }
                if (!foundonstack) {

                    getTriggerHandler().suppressMode(TriggerType.ChangesZone);
                    c.getController().getZone(ZoneType.Command).remove(c);
                    i--;
                    getTriggerHandler().clearSuppression(TriggerType.ChangesZone);

                    c.getController().getZone(ZoneType.SchemeDeck).add(c);
                }
            }

        }
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(GameAge value) {
        age = value;
    }
}
