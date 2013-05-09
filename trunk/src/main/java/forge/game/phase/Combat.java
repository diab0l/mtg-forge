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
package forge.game.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.GameEntity;
import forge.card.trigger.TriggerType;
import forge.game.event.BlockerAssignedEvent;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * <p>
 * Combat class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Combat {
    // key is attacker Card
    // value is List<Card> of blockers
    private final Map<Card, List<Card>> attackerMap = new TreeMap<Card, List<Card>>();
    private final Map<Card, List<Card>> blockerMap = new TreeMap<Card, List<Card>>();

    private final Set<Card> blocked = new HashSet<Card>();
    private final Set<Card> unblocked = new HashSet<Card>();
    private final HashMap<Card, Integer> defendingDamageMap = new HashMap<Card, Integer>();

    // Defenders are the Defending Player + Each controlled Planeswalker
    private List<GameEntity> defenders = new ArrayList<GameEntity>();
    private Map<GameEntity, List<Card>> defenderMap = new HashMap<GameEntity, List<Card>>();


    // This Hash keeps track of
    private final HashMap<Card, GameEntity> attackerToDefender = new HashMap<Card, GameEntity>();

    private Player attackingPlayer = null;

    /**
     * <p>
     * Constructor for Combat.
     * </p>
     */
    public Combat() {
        // Let the Begin Turn/Untap Phase Reset Combat properly
    }

    /**
     * <p>
     * reset.
     * </p>
     */
    public final void reset(Player playerTurn) {
        this.resetAttackers();
        this.blocked.clear();

        this.unblocked.clear();
        this.defendingDamageMap.clear();

        this.attackingPlayer = playerTurn;

        this.initiatePossibleDefenders(playerTurn.getOpponents());
    }

    /**
     * <p>
     * initiatePossibleDefenders.
     * </p>
     * 
     * @param defender
     *            a {@link forge.game.player.Player} object.
     */
    public final void initiatePossibleDefenders(final Iterable<Player> defenders) {
        this.defenders.clear();
        this.defenderMap.clear();
        for (Player defender : defenders) {
            fillDefenderMaps(defender);
        }
    }

    public final void initiatePossibleDefenders(final Player defender) {
        this.defenders.clear();
        this.defenderMap.clear();
        fillDefenderMaps(defender);
    }

    private void fillDefenderMaps(final Player defender) {
        this.defenders.add(defender);
        this.defenderMap.put(defender, new ArrayList<Card>());
        List<Card> planeswalkers =
                CardLists.filter(defender.getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.PLANEWALKERS);
        for (final Card pw : planeswalkers) {
            this.defenders.add(pw);
            this.defenderMap.put(pw, new ArrayList<Card>());
        }
    }

    /**
     * <p>
     * Getter for the field <code>defenders</code>.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final List<GameEntity> getDefenders() {
        return this.defenders;
    }

    /**
     * <p>
     * Setter for the field <code>defenders</code>.
     * </p>
     * 
     * @param newDef
     *            a {@link java.util.ArrayList} object.
     */
    public final void setDefenders(final List<GameEntity> newDef) {
        this.defenders = newDef;
        for (GameEntity entity : this.defenders) {
            this.defenderMap.put(entity, new ArrayList<Card>());
        }
    }

    /**
     * <p>
     * getDefendingPlaneswalkers.
     * </p>
     * 
     * @return an array of {@link forge.Card} objects.
     */
    public final List<Card> getDefendingPlaneswalkers() {
        final List<Card> pwDefending = new ArrayList<Card>();

        for (final GameEntity o : this.defenders) {
            if (o instanceof Card) {
                pwDefending.add((Card) o);
            }
        }

        return pwDefending;
    }

    /**
     * <p>
     * Setter for the field <code>attackingPlayer</code>.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     */
    public final void setAttackingPlayer(final Player player) {
        this.attackingPlayer = player;
    }

    /**
     * <p>
     * Getter for the field <code>attackingPlayer</code>.
     * </p>
     * 
     * @return a {@link forge.game.player.Player} object.
     */
    public final Player getAttackingPlayer() {
        return this.attackingPlayer;
    }


    /**
     * <p>
     * Getter for the field <code>defendingDamageMap</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     */
    public final HashMap<Card, Integer> getDefendingDamageMap() {
        return this.defendingDamageMap;
    }

    /**
     * <p>
     * addDefendingDamage.
     * </p>
     * 
     * @param n
     *            a int.
     * @param source
     *            a {@link forge.Card} object.
     */
    public final void addDefendingDamage(final int n, final Card source) {
        final GameEntity ge = this.getDefenderByAttacker(source);

        if (ge instanceof Card) {
            final Card planeswalker = (Card) ge;
            planeswalker.addAssignedDamage(n, source);

            return;
        }

        if (!this.defendingDamageMap.containsKey(source)) {
            this.defendingDamageMap.put(source, n);
        } else {
            this.defendingDamageMap.put(source, this.defendingDamageMap.get(source) + n);
        }
    }

    public final List<Card> getAttackersByDefenderSlot(int slot) {
        GameEntity entity = this.defenders.get(slot);
        return this.defenderMap.get(entity);
    }
    
    public final List<Card> getAttackersOf(GameEntity defender) {
        return defenderMap.get(defender);
    }

    /**
     * <p>
     * isAttacking.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean isAttacking(final Card c) {
        return this.attackerMap.containsKey(c);
    }

    /**
     * <p>
     * addAttacker.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param defender
     *            a GameEntity object.
     */
    public final void addAttacker(final Card c, GameEntity defender) {
        if (!defenders.contains(defender)) {
            System.out.println("Trying to add Attacker " + c + " to missing defender " + defender);
            return;
        }

        this.attackerMap.put(c, new ArrayList<Card>());
        this.attackerToDefender.put(c, defender);
        this.defenderMap.get(defender).add(c);
    }

    /**
     * <p>
     * getDefenderByAttacker.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.lang.Object} object.
     */
    public final GameEntity getDefenderByAttacker(final Card c) {
        return this.attackerToDefender.get(c);
    }

    public final Player getDefenderPlayerByAttacker(final Card c) {
        GameEntity defender = getDefenderByAttacker(c);

        // System.out.println(c.toString() + " attacks " + defender.toString());
        if (defender instanceof Player) {
            return (Player) defender;
        }

        // maybe attack on a controlled planeswalker?
        if (defender instanceof Card) {
            return ((Card) defender).getController();
        }
        return null;
    }

    public final GameEntity getDefendingEntity(final Card c) {
        GameEntity defender = this.attackerToDefender.get(c);

        if (this.defenders.contains(defender)) {
            return defender;
        }

        System.out.println("Attacker " + c + " missing defender " + defender);

        return null;
    }

    /**
     * <p>
     * resetAttackers.
     * </p>
     */
    public final void resetAttackers() {
        this.attackerMap.clear();
        this.attackerToDefender.clear();
        this.blockerMap.clear();
    }

    /**
     * <p>
     * getAttackers.
     * </p>
     * 
     * @return an array of {@link forge.Card} objects.
     */
    public final List<Card> getAttackers() {
        return new ArrayList<Card>(this.attackerMap.keySet());
    } // getAttackers()

    /**
     * <p>
     * isBlocked.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean isBlocked(final Card attacker) {
        return this.blocked.contains(attacker);
    }

    public final void setBlocked(final Card attacker) {
        if (!this.blocked.contains(attacker)) {
            this.blocked.add(attacker);
            this.unblocked.remove(attacker);
        }
    }

    /**
     * <p>
     * addBlocker.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @param blocker
     *            a {@link forge.Card} object.
     */
    public final void addBlocker(final Card attacker, final Card blocker) {
        this.blocked.add(attacker);
        this.attackerMap.get(attacker).add(blocker);
        if (!this.blockerMap.containsKey(blocker)) {
            this.blockerMap.put(blocker, CardLists.createCardList(attacker));
        }
        else {
            this.blockerMap.get(blocker).add(attacker);
        }
        attacker.getGame().getEvents().post(new BlockerAssignedEvent());
    }

    public final void removeBlockAssignment(final Card attacker, final Card blocker) {
        this.attackerMap.get(attacker).remove(blocker);
        this.blockerMap.get(blocker).remove(attacker);
        if (this.attackerMap.get(attacker).isEmpty()) {
            this.blocked.remove(attacker);
        }
        if (this.blockerMap.get(blocker).isEmpty()) {
            this.blockerMap.remove(blocker);
        }
    }

    /**
     * <p>
     * undoBlockingAssignment.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.Card} object.
     */
    public final void undoBlockingAssignment(final Card blocker) {
        final List<Card> att = this.getAttackers();
        for (final Card attacker : att) {
            if (this.getBlockers(attacker).contains(blocker)) {
                this.getBlockingAttackerList(attacker).remove(blocker);
                if (this.getBlockers(attacker).isEmpty()) {
                    this.blocked.remove(attacker);
                }
            }
        }
        this.blockerMap.remove(blocker);
    } // undoBlockingAssignment(Card)

    /**
     * <p>
     * getAllBlockers.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final List<Card> getAllBlockers() {
        final List<Card> block = new ArrayList<Card>();
        block.addAll(blockerMap.keySet());

        return block;
    } // getAllBlockers()

    /**
     * <p>
     * getBlockers.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    public final List<Card> getBlockers(final Card attacker) {
        if (this.getBlockingAttackerList(attacker) == null) {
            return new ArrayList<Card>();
        } else {
            return new ArrayList<Card>(this.getBlockingAttackerList(attacker));
        }
    }

    /**
     * <p>
     * getAttackerBlockedBy.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public final List<Card> getAttackersBlockedBy(final Card blocker) {
        if (blockerMap.containsKey(blocker)) {
            return blockerMap.get(blocker);
        }
        return new ArrayList<Card>();
    }

    /**
     * <p>
     * getList.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    private List<Card> getBlockingAttackerList(final Card attacker) {
        return this.attackerMap.get(attacker);
    }


    /**
     * <p>
     * getDefendingPlayer.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @return a {@link forge.Player} object.
     */
    public List<Player> getDefendingPlayerRelatedTo(final Card source) {
        List<Player> players = new ArrayList<Player>();
        Card attacker = source;
        if (source.isAura()) {
            attacker = source.getEnchantingCard();
        } else if (source.isEquipment()) {
            attacker = source.getEquippingCard();
        }

        // return the corresponding defender
        Player defender = getDefenderPlayerByAttacker(attacker);
        if (null != defender) {
            players.add(defender);
            return players;
        }
        
        // return all defenders
        List<GameEntity> defenders = this.getDefenders();
        for (GameEntity ge : defenders) {
            if (ge instanceof Player) {
                players.add((Player) ge);
            }
        }
        return players;
    }

    /**
     * <p>
     * setBlockerList.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.Card} object.
     * @param blockers
     *            a {@link forge.CardList} object.
     */
    public void setBlockerList(final Card attacker, final List<Card> blockers) {
        this.attackerMap.put(attacker, blockers);
    }

    public void setAttackersBlockedByList(final Card blocker, final List<Card> attackers) {
        this.blockerMap.put(blocker, attackers);
    }

    /**
     * <p>
     * removeFromCombat.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void removeFromCombat(final Card c) {
        // todo(sol) add some more solid error checking in here
        // is card an attacker?
        if (this.attackerMap.containsKey(c)) {
            // Keep track of all of the different maps
            List<Card> blockers = this.attackerMap.get(c);
            this.attackerMap.remove(c);
            for (Card b : blockers) {
                this.blockerMap.get(b).remove(c);
            }

            // Keep track of all of the different maps
            GameEntity entity = this.attackerToDefender.get(c);
            this.attackerToDefender.remove(c);
            this.defenderMap.get(entity).remove(c);
        } else if (this.blockerMap.containsKey(c)) { // card is a blocker
            List<Card> attackers = this.blockerMap.get(c);

            boolean stillDeclaring = c.getGame().getPhaseHandler().is(PhaseType.COMBAT_DECLARE_BLOCKERS);
            this.blockerMap.remove(c);
            for (Card a : attackers) {
                this.attackerMap.get(a).remove(c);
                if (stillDeclaring && this.attackerMap.get(a).isEmpty()) {
                    this.blocked.remove(a);
                }
            }
        }
    } // removeFromCombat()

    /**
     * <p>
     * verifyCreaturesInPlay.
     * </p>
     */
    public final void verifyCreaturesInPlay() {
        final List<Card> all = new ArrayList<Card>();
        all.addAll(this.getAttackers());
        all.addAll(this.getAllBlockers());

        for (int i = 0; i < all.size(); i++) {
            if (!all.get(i).isInPlay()) {
                this.removeFromCombat(all.get(i));
            }
        }
    } // verifyCreaturesInPlay()

    /**
     * <p>
     * setUnblocked.
     * </p>
     */
    public final void setUnblocked() {
        final List<Card> attacking = this.getAttackers();

        for (final Card attacker : attacking) {
            final List<Card> block = this.getBlockers(attacker);

            if (block.isEmpty()) {
                // this damage is assigned to a player by setPlayerDamage()
                this.addUnblockedAttacker(attacker);

                // Run Unblocked Trigger
                final HashMap<String, Object> runParams = new HashMap<String, Object>();
                runParams.put("Attacker", attacker);
                runParams.put("Defender",this.getDefenderByAttacker(attacker));
                attacker.getGame().getTriggerHandler().runTrigger(TriggerType.AttackerUnblocked, runParams, false);

            }
        }
    }

    private final boolean assignBlockersDamage(boolean firstStrikeDamage) {
        final List<Card> blockers = this.getAllBlockers();
        boolean assignedDamage = false;

        for (final Card blocker : blockers) {
            if (blocker.hasDoubleStrike() || blocker.hasFirstStrike() == firstStrikeDamage) {
                List<Card> attackers = this.getAttackersBlockedBy(blocker);

                final int damage = blocker.getNetCombatDamage();

                if (!attackers.isEmpty()) {
                    assignedDamage = true;
                    Map<Card, Integer> map = blocker.getController().getController().assignCombatDamage(blocker, attackers, damage, null);
                    for (Entry<Card, Integer> dt : map.entrySet()) {
                        dt.getKey().addAssignedDamage(dt.getValue(), blocker);
                        dt.getKey().updateObservers();
                    }
                }
            }
        }

        return assignedDamage;
    }

    private final boolean assignAttackersDamage(boolean firstStrikeDamage) {
        this.defendingDamageMap.clear(); // this should really happen in deal damage
        List<Card> blockers = null;
        final List<Card> attackers = this.getAttackers();
        boolean assignedDamage = false;
        for (final Card attacker : attackers) {
            // If attacker isn't in the right first/regular strike section, continue along
            if (!(attacker.hasDoubleStrike() || attacker.hasFirstStrike() == firstStrikeDamage)) {
                continue;
            }

            // If potential damage is 0, continue along
            final int damageDealt = attacker.getNetCombatDamage();
            if (damageDealt <= 0) {
                continue;
            }

            boolean trampler = attacker.hasKeyword("Trample");
            blockers = this.getBlockers(attacker);
            assignedDamage = true;
            // If the Attacker is unblocked, or it's a trampler and has 0 blockers, deal damage to defender
            if (blockers.isEmpty()) {
                if (trampler || this.isUnblocked(attacker)) {
                    this.addDefendingDamage(damageDealt, attacker);
                } else {
                    // Else no damage can be dealt anywhere
                    continue;
                }
            } else {
                Map<Card, Integer> map = this.getAttackingPlayer().getController().assignCombatDamage(attacker, blockers, damageDealt, this.getDefenderByAttacker(attacker));
                for (Entry<Card, Integer> dt : map.entrySet()) {
                    if( dt.getKey() == null) {
                        if (dt.getValue() > 0) 
                            addDefendingDamage(dt.getValue(), attacker);
                    } else {
                        dt.getKey().addAssignedDamage(dt.getValue(), attacker);
                        dt.getKey().updateObservers();
                    }
                }

            } // if !hasFirstStrike ...
        } // for
        return assignedDamage;
    }

    public final boolean assignCombatDamage(boolean firstStrikeDamage) {
        boolean assignedDamage = assignAttackersDamage(firstStrikeDamage);
        assignedDamage |= assignBlockersDamage(firstStrikeDamage);
        return assignedDamage;
    }

    /**
     * <p>
     * dealAssignedDamage.
     * </p>
     */
    public void dealAssignedDamage() {
        // This function handles both Regular and First Strike combat assignment

        final HashMap<Card, Integer> defMap = this.getDefendingDamageMap();
        final HashMap<GameEntity, List<Card>> wasDamaged = new HashMap<GameEntity, List<Card>>();

        for (final Entry<Card, Integer> entry : defMap.entrySet()) {
            GameEntity defender = getDefendingEntity(entry.getKey());
            if (defender instanceof Player) { // player
                if (((Player) defender).addCombatDamage(entry.getValue(), entry.getKey())) {
                    if (wasDamaged.containsKey(defender)) {
                        wasDamaged.get(defender).add(entry.getKey());
                    } else {
                        List<Card> l = new ArrayList<Card>();
                        l.add(entry.getKey());
                        wasDamaged.put(defender, l);
                    }
                }
            } else if (defender instanceof Card) { // planeswalker
                if (((Card) defender).getController().addCombatDamage(entry.getValue(), entry.getKey())) {
                    if (wasDamaged.containsKey(defender)) {
                        wasDamaged.get(defender).add(entry.getKey());
                    } else {
                        List<Card> l = new ArrayList<Card>();
                        l.add(entry.getKey());
                        wasDamaged.put(defender, l);
                    }
                }
            }
        }

        // this can be much better below here...

        final List<Card> combatants = new ArrayList<Card>();
        combatants.addAll(this.getAttackers());
        combatants.addAll(this.getAllBlockers());
        combatants.addAll(this.getDefendingPlaneswalkers());

        Card c;
        for (int i = 0; i < combatants.size(); i++) {
            c = combatants.get(i);

            // if no assigned damage to resolve, move to next
            if (c.getTotalAssignedDamage() == 0) {
                continue;
            }

            final Map<Card, Integer> assignedDamageMap = c.getAssignedDamageMap();
            final HashMap<Card, Integer> damageMap = new HashMap<Card, Integer>();

            for (final Entry<Card, Integer> entry : assignedDamageMap.entrySet()) {
                final Card crd = entry.getKey();
                damageMap.put(crd, entry.getValue());
            }
            c.addCombatDamage(damageMap);

            damageMap.clear();
            c.clearAssignedDamage();
        }
        
        // Run triggers
        for (final GameEntity ge : wasDamaged.keySet()) {
            final HashMap<String, Object> runParams = new HashMap<String, Object>();
            runParams.put("DamageSources", wasDamaged.get(ge));
            runParams.put("DamageTarget", ge);
            ge.getGame().getTriggerHandler().runTrigger(TriggerType.CombatDamageDoneOnce, runParams, false);
        }

        // This was deeper before, but that resulted in the stack entry acting
        // like before.

    }

    /**
     * <p>
     * isUnblocked.
     * </p>
     * 
     * @param att
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean isUnblocked(final Card att) {
        return this.unblocked.contains(att);
    }

    /**
     * <p>
     * getUnblockedAttackers.
     * </p>
     * 
     * @return an array of {@link forge.Card} objects.
     */
    public final List<Card> getUnblockedAttackers() {
        final List<Card> out = new ArrayList<Card>();
        for (Card c : this.unblocked) {
            if (!c.hasFirstStrike()) {
                out.add(c);
            }
        }
        return out;
    } // getUnblockedAttackers()

    /**
     * <p>
     * getUnblockedFirstStrikeAttackers.
     * </p>
     * 
     * @return an array of {@link forge.Card} objects.
     */
    public final List<Card> getUnblockedFirstStrikeAttackers() {
        final List<Card> out = new ArrayList<Card>();
        for (Card c : this.unblocked) { // only add creatures without firstStrike to this
            if (c.hasFirstStrike() || c.hasDoubleStrike()) {
                out.add(c);
            }
        }
        return out;
    } // getUnblockedAttackers()

    /**
     * <p>
     * addUnblockedAttacker.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void addUnblockedAttacker(final Card c) {
        if (!this.unblocked.contains(c)) {
            this.unblocked.add(c);
        }
    }

    public boolean isPlayerAttacked(Player priority) {

        // System.out.println("\nWho attacks attacks " + priority.toString() + "?");
        for (Card c : getAttackers()) {

            if (priority.equals(getDefenderPlayerByAttacker(c))) {
                return true;
            }
        }
        return false;
    }

} // Class Combat
