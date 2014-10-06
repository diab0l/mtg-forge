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
package forge.game.combat;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.card.CardType;
import forge.card.MagicColor;
import forge.card.mana.ManaCost;
import forge.game.Game;
import forge.game.GameEntity;
import forge.game.GlobalRuleChange;
import forge.game.ability.AbilityFactory;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardFactoryUtil;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.cost.Cost;
import forge.game.phase.PhaseType;
import forge.game.phase.Untap;
import forge.game.player.Player;
import forge.game.player.PlayerController.ManaPaymentPurpose;
import forge.game.spellability.SpellAbility;
import forge.game.staticability.StaticAbility;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;
import forge.util.Expressions;
import forge.util.Lang;
import forge.util.TextUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * <p>
 * CombatUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CombatUtil {

    // can the creature block given the combat state?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card blocker, final Combat combat) {
        if (blocker == null) {
            return false;
        }
        if (combat == null) {
            return CombatUtil.canBlock(blocker);
        }

        if (!CombatUtil.canBlockMoreCreatures(blocker, combat.getAttackersBlockedBy(blocker))) {
            return false;
        }
        final Game game = blocker.getGame();
        final int blockers = combat.getAllBlockers().size();

        if (blockers > 1 && game.getStaticEffects().getGlobalRuleChange(GlobalRuleChange.onlyTwoBlockers)) {
            return false;
        }

        if (blockers > 0 && game.getStaticEffects().getGlobalRuleChange(GlobalRuleChange.onlyOneBlocker)) {
            return false;
        }

        return CombatUtil.canBlock(blocker);
    }

    // can the creature block at all?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card blocker) {
        return canBlock(blocker, false);
    }

    // can the creature block at all?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card blocker, final boolean nextTurn) {
        if (blocker == null) {
            return false;
        }

        if (!nextTurn && blocker.isTapped() && !blocker.hasKeyword("CARDNAME can block as though it were untapped.")) {
            return false;
        }

        if (blocker.hasKeyword("CARDNAME can't block.") || blocker.hasKeyword("CARDNAME can't attack or block.")
                || blocker.isPhasedOut()) {
            return false;
        }

        final List<Card> list = blocker.getController().getCreaturesInPlay();
        if (list.size() < 2 && blocker.hasKeyword("CARDNAME can't attack or block alone.")) {
            return false;
        }

        return true;
    }

    public static boolean canBlockMoreCreatures(final Card blocker, final List<Card> blockedBy) {
        // TODO(sol) expand this for the additional blocking keyword
        if (blockedBy.isEmpty() || blocker.hasKeyword("CARDNAME can block any number of creatures.")) {
            return true;
        }
        int canBlockMore = blocker.getKeywordAmount("CARDNAME can block an additional creature.")
                + blocker.getKeywordAmount("CARDNAME can block an additional ninety-nine creatures.") * 99;
        return canBlockMore >= blockedBy.size();
    }

    // can the attacker be blocked at all?
    /**
     * <p>
     * canBeBlocked.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean canBeBlocked(final Card attacker, final Combat combat, Player defendingPlayer) {
        if (attacker == null) {
            return true;
        }

        if ( combat != null ) {
            if (attacker.hasStartOfKeyword("CantBeBlockedByAmount GT") && !combat.getBlockers(attacker).isEmpty()) {
                return false;
            }

            // Rule 802.4a: A player can block only creatures attacking him or a planeswalker he controls
            Player attacked = combat.getDefendingPlayerRelatedTo(attacker);
            if (attacked != null && attacked != defendingPlayer) {
                return false;
            }
        }
        return CombatUtil.canBeBlocked(attacker, defendingPlayer);
    }

    // can the attacker be blocked at all?
    /**
     * <p>
     * canBeBlocked.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canBeBlocked(final Card attacker, Player defender) {
        if (attacker == null) {
            return true;
        }

        if (attacker.hasKeyword("Unblockable")) {
            return false;
        }

        // Landwalk
        if (isUnblockableFromLandwalk(attacker, defender)) {
            return false;
        }

        return true;
    }


    public static boolean isUnblockableFromLandwalk(final Card attacker, Player defendingPlayer) {
        //May be blocked as though it doesn't have landwalk. (Staff of the Ages)
        if (attacker.hasKeyword("May be blocked as though it doesn't have landwalk.")) {
            return false;
        }

        ArrayList<String> walkTypes = new ArrayList<String>();

        for (String basic : MagicColor.Constant.BASIC_LANDS) {
            StringBuilder sbLand = new StringBuilder();
            sbLand.append(basic);
            sbLand.append("walk");
            String landwalk = sbLand.toString();

            StringBuilder sbSnow = new StringBuilder();
            sbSnow.append("Snow ");
            sbSnow.append(landwalk.toLowerCase());
            String snowwalk = sbSnow.toString();

            sbLand.insert(0, "May be blocked as though it doesn't have "); //Deadfall, etc.
            sbLand.append(".");

            String mayBeBlocked = sbLand.toString();

            if (attacker.hasKeyword(landwalk) && !attacker.hasKeyword(mayBeBlocked)) {
                walkTypes.add(basic);
            }

            if (attacker.hasKeyword(snowwalk)) {
                StringBuilder sbSnowType = new StringBuilder();
                sbSnowType.append(basic);
                sbSnowType.append(".Snow");
                walkTypes.add(sbSnowType.toString());
            }
        }

        for (String keyword : attacker.getKeyword()) {
            if (keyword.equals("Legendary landwalk")) {
                walkTypes.add("Land.Legendary");
            } else if (keyword.equals("Desertwalk")) {
                walkTypes.add("Desert");
            } else if (keyword.equals("Nonbasic landwalk")) {
                walkTypes.add("Land.nonBasic");
            } else if (keyword.equals("Snow landwalk")) {
                walkTypes.add("Land.Snow");
            } else if (keyword.endsWith("walk")) {
                final String landtype = keyword.replace("walk", "");
                if (CardType.isALandType(landtype)) {
                    if (!walkTypes.contains(landtype)) {
                        walkTypes.add(landtype);
                    }
                }
            }
        }

        if (walkTypes.isEmpty()) {
            return false;
        }

        String valid = StringUtils.join(walkTypes, ",");
        List<Card> defendingLands = defendingPlayer.getCardsIn(ZoneType.Battlefield);
        for (Card c : defendingLands) {
            if (c.isValid(valid.split(","), defendingPlayer, attacker)) {
                return true;
            }
        }

        return false;
    }

    /**
     * canBlockAtLeastOne.
     * 
     * @param blocker
     *            the blocker
     * @param attackers
     *            the attackers
     * @return true, if one can be blocked
     */
    public static boolean canBlockAtLeastOne(final Card blocker, final Iterable<Card> attackers) {
        for (Card attacker : attackers) {
            if (CombatUtil.canBlock(attacker, blocker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Can be blocked.
     * 
     * @param attacker
     *            the attacker
     * @param blockers
     *            the blockers
     * @return true, if successful
     */
    public static boolean canBeBlocked(final Card attacker, final List<Card> blockers, final Combat combat) {
        int blocks = 0;
        for (final Card blocker : blockers) {
            if (CombatUtil.canBeBlocked(attacker, blocker.getController()) && CombatUtil.canBlock(attacker, blocker)) {
                blocks++;
            }
        }

        return canAttackerBeBlockedWithAmount(attacker, blocks, combat);
    }

    /**
     * <p>
     * needsMoreBlockers.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static int needsBlockers(final Card attacker) {

        if (attacker == null) {
            return 0;
        }

        if (attacker.hasKeyword("CantBeBlockedByAmount LT2")) {
            return 2;
        } else if (attacker.hasKeyword("CantBeBlockedByAmount LT3")) {
            return 3;
        } else
            return 1;
    }

    // Has the human player chosen all mandatory blocks?
    /**
     * <p>
     * finishedMandatotyBlocks.
     * </p>
     * 
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static String validateBlocks(final Combat combat, final Player defending) {
        final List<Card> defendersArmy = defending.getCreaturesInPlay();
        final List<Card> attackers = combat.getAttackers();
        final List<Card> blockers = CardLists.filterControlledBy(combat.getAllBlockers(), defending);

        // if a creature does not block but should, return false
        for (final Card blocker : defendersArmy) {
            if (blocker.getMustBlockCards() != null) {
                int mustBlockAmt = blocker.getMustBlockCards().size();
                List<Card> blockedSoFar = combat.getAttackersBlockedBy(blocker);
                List<Card> remainingBlockables = new ArrayList<Card>();
                for (final Card attacker : attackers) {
                    if (!blockedSoFar.contains(attacker) && CombatUtil.canBlock(attacker, blocker)) {
                        remainingBlockables.add(attacker);
                    }
                }
                boolean canBlockAnother = CombatUtil.canBlockMoreCreatures(blocker, blockedSoFar);
                if (canBlockAnother && mustBlockAmt > blockedSoFar.size() && remainingBlockables.size() > 0) {
                    return String.format("%s must still block %s.", blocker, Lang.joinHomogenous(remainingBlockables));
                }
            }
            // lure effects
            if (!blockers.contains(blocker) && CombatUtil.mustBlockAnAttacker(blocker, combat)) {
                return String.format("%s must block an attacker, but has not been assigned to block any.", blocker);
            }

            // "CARDNAME blocks each turn if able."
            if (!blockers.contains(blocker) && blocker.hasKeyword("CARDNAME blocks each turn if able.")) {
                for (final Card attacker : attackers) {
                    if (CombatUtil.canBlock(attacker, blocker, combat)) {
                        boolean must = true;
                        if (attacker.hasStartOfKeyword("CantBeBlockedByAmount LT")) {
                            final List<Card> possibleBlockers = Lists.newArrayList(defendersArmy);
                            possibleBlockers.remove(blocker);
                            if (!CombatUtil.canBeBlocked(attacker, possibleBlockers, combat)) {
                                must = false;
                            }
                        }
                        if (must) {
                            return String.format("%s must block each turn, but was not assigned to block any attacker now", blocker);
                        }
                    }
                }
            }
        }

        for (final Card attacker : attackers) {
            int cntBlockers = combat.getBlockers(attacker).size();
            // don't accept blocker amount for attackers with keyword defining valid blockers amount
            if (cntBlockers > 0 && !canAttackerBeBlockedWithAmount(attacker, cntBlockers, combat))
                return String.format("%s cannot be blocked with %d creatures you've assigned", attacker, cntBlockers);
        }

        return null;
    }

    // can the blocker block an attacker with a lure effect?
    /**
     * <p>
     * mustBlockAnAttacker.
     * </p>
     * 
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean mustBlockAnAttacker(final Card blocker, final Combat combat) {
        if (blocker == null || combat == null) {
            return false;
        }

        if (!CombatUtil.canBlock(blocker, combat)) {
            return false;
        }

        final List<Card> attackers = combat.getAttackers();
        final List<Card> attackersWithLure = new ArrayList<Card>();
        for (final Card attacker : attackers) {
            if (attacker.hasStartOfKeyword("All creatures able to block CARDNAME do so.")
                    || (attacker.hasStartOfKeyword("All Walls able to block CARDNAME do so.") && blocker.isType("Wall"))
                    || (attacker.hasStartOfKeyword("All creatures with flying able to block CARDNAME do so.") && blocker.hasKeyword("Flying"))
                    || (attacker.hasStartOfKeyword("CARDNAME must be blocked if able.")
                            && combat.getBlockers(attacker).isEmpty())) {
                attackersWithLure.add(attacker);
            }
        }

        final Player defender = blocker.getController();
        for (final Card attacker : attackersWithLure) {
            if (CombatUtil.canBeBlocked(attacker, combat, defender) && CombatUtil.canBlock(attacker, blocker)) {
                boolean canBe = true;
                if (attacker.hasStartOfKeyword("CantBeBlockedByAmount LT")) {
                    final List<Card> blockers = combat.getDefenderPlayerByAttacker(attacker).getCreaturesInPlay();
                    blockers.remove(blocker);
                    if (!CombatUtil.canBeBlocked(attacker, blockers, combat)) {
                        canBe = false;
                    }
                }
                if (canBe) {
                    return true;
                }
            }
        }

        if (blocker.getMustBlockCards() != null) {
            for (final Card attacker : blocker.getMustBlockCards()) {
                if (CombatUtil.canBeBlocked(attacker, combat, defender) && CombatUtil.canBlock(attacker, blocker)
                        && combat.isAttacking(attacker)) {
                    boolean canBe = true;
                    if (attacker.hasStartOfKeyword("CantBeBlockedByAmount LT")) {
                        final List<Card> blockers = combat.getDefenderPlayerByAttacker(attacker).getCreaturesInPlay();
                        blockers.remove(blocker);
                        if (!CombatUtil.canBeBlocked(attacker, blockers, combat)) {
                            canBe = false;
                        }
                    }
                    if (canBe) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // can a player block with one or more creatures at the moment?
    /**
     * <p>
     * canAttack.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean canBlock(Player p, Combat combat) {
        List<Card> creatures = p.getCreaturesInPlay();
        if (creatures.isEmpty()) { return false; }

        List<Card> attackers = combat.getAttackers();
        if (attackers.isEmpty()) { return false; }

        for (Card c : creatures) {
            for (Card a : attackers) {
                if (CombatUtil.canBlock(a, c, combat)) {
                    return true;
                }
            }
        }
        return false;
    }

    // can the blocker block the attacker given the combat state?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card attacker, final Card blocker, final Combat combat) {
        if (attacker == null || blocker == null) {
            return false;
        }

        if (!CombatUtil.canBlock(blocker, combat)) {
            return false;
        }
        if (!CombatUtil.canBeBlocked(attacker, combat, blocker.getController())) {
            return false;
        }
        if (combat != null && combat.isBlocking(blocker, attacker)) { // Can't block if already blocking the attacker
            return false;
        }

        // if the attacker has no lure effect, but the blocker can block another
        // attacker with lure, the blocker can't block the former
        if (!attacker.hasKeyword("All creatures able to block CARDNAME do so.")
                && !(attacker.hasStartOfKeyword("All Walls able to block CARDNAME do so.") && blocker.isType("Wall"))
                && !(attacker.hasStartOfKeyword("All creatures with flying able to block CARDNAME do so.") && blocker.hasKeyword("Flying"))
                && !(attacker.hasKeyword("CARDNAME must be blocked if able.") && combat.getBlockers(attacker).isEmpty())
                && !(blocker.getMustBlockCards() != null && blocker.getMustBlockCards().contains(attacker))
                && CombatUtil.mustBlockAnAttacker(blocker, combat)) {
            return false;
        }

        return CombatUtil.canBlock(attacker, blocker);
    }

 // can the blocker block the attacker?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card attacker, final Card blocker) {
        return canBlock(attacker, blocker, false);
    }

    // can the blocker block the attacker?
    /**
     * <p>
     * canBlock.
     * </p>
     * 
     * @param attacker
     *            a {@link forge.game.card.Card} object.
     * @param blocker
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canBlock(final Card attacker, final Card blocker, final boolean nextTurn) {
        if ((attacker == null) || (blocker == null)) {
            return false;
        }

        if (!CombatUtil.canBlock(blocker, nextTurn)) {
            return false;
        }
        if (!CombatUtil.canBeBlocked(attacker, blocker.getController())) {
            return false;
        }

        if (CardFactoryUtil.hasProtectionFrom(blocker, attacker)) {
            return false;
        }

        // rare case:
        if (blocker.hasKeyword("Shadow")
                && blocker.hasKeyword("CARDNAME can block creatures with shadow as though they didn't have shadow.")) {
            return false;
        }

        if (attacker.hasKeyword("Shadow") && !blocker.hasKeyword("Shadow")
                && !blocker.hasKeyword("CARDNAME can block creatures with shadow as though they didn't have shadow.")) {
            return false;
        }

        if (!attacker.hasKeyword("Shadow") && blocker.hasKeyword("Shadow")) {
            return false;
        }

        if (attacker.hasKeyword("Creatures with power less than CARDNAME's power can't block it.") && attacker.getNetAttack() > blocker.getNetAttack()) {
            return false;
        }

        if (attacker.hasStartOfKeyword("CantBeBlockedBy ")) {
            final int keywordPosition = attacker.getKeywordPosition("CantBeBlockedBy ");
            final String parse = attacker.getKeyword().get(keywordPosition).toString();
            final String[] k = parse.split(" ", 2);
            final String[] restrictions = k[1].split(",");
            if (blocker.isValid(restrictions, attacker.getController(), attacker)) {
                return false;
            }
        }

        if (blocker.hasStartOfKeyword("CantBlock")) {
            final int keywordPosition = blocker.getKeywordPosition("CantBlock");
            final String parse = blocker.getKeyword().get(keywordPosition).toString();
            if (parse.startsWith("CantBlockCardUID")) {
                final String[] k = parse.split("_", 2);
                if (attacker.getId() == Integer.parseInt(k[1])) {
                    return false;
                }
            } else {
                final String[] parse0 = parse.split(":");
                final String[] k = parse0[0].split(" ", 2);
                final String[] restrictions = k[1].split(",");
                if (attacker.isValid(restrictions, blocker.getController(), blocker)) {
                    return false;
                }
            }
        }

        if (blocker.hasKeyword("CARDNAME can block only creatures with flying.") && !attacker.hasKeyword("Flying")) {
            return false;
        }

        if (attacker.hasKeyword("Flying") && !blocker.hasKeyword("Flying") && !blocker.hasKeyword("Reach")) {
            return false;
        }

        if (attacker.hasKeyword("Horsemanship") && !blocker.hasKeyword("Horsemanship")) {
            return false;
        }

        if (attacker.hasKeyword("Fear") && !blocker.isArtifact() && !blocker.isBlack()) {
            return false;
        }

        if (attacker.hasKeyword("Intimidate") && !blocker.isArtifact() && !blocker.sharesColorWith(attacker)) {
            return false;
        }

        return true;
    } // canBlock()

    public static void checkAttackOrBlockAlone(Combat combat) {
        // Handles removing cards like Mogg Flunkies from combat if group attack
        // didn't occur
        for (Card c1 : combat.getAttackers()) {
            if (c1.hasKeyword("CARDNAME can't attack or block alone.")
                    || c1.hasKeyword("CARDNAME can't attack alone.")) {
                if (combat.getAttackers().size() < 2) {
                    combat.removeFromCombat(c1);
                }
            }
        }
    }

    // can a player attack with one or more creatures at the moment?
    /**
     * <p>
     * canAttack.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player} object.
     * @return a boolean.
     */
    public static boolean canAttack(Player p) {
        List<Card> creatures = p.getCreaturesInPlay();
        if (creatures.isEmpty()) { return false; }

        List<Player> defenders = p.getOpponents();
        if (defenders.isEmpty()) { return false; }

        boolean foundCreatureThatCantAttackAlone = false;

        for (Card c : creatures) {
            if (CombatUtil.canAttack(c)) {
                for (Player def : defenders) {
                    if (CombatUtil.canAttackNextTurn(c, def)) {
                        if (c.hasKeyword("CARDNAME can't attack or block alone.")
                                || c.hasKeyword("CARDNAME can't attack alone.")) {
                            //ensure another possible attacker is found
                            //if the first one found can't attack alone
                            if (!foundCreatureThatCantAttackAlone) {
                                foundCreatureThatCantAttackAlone = true;
                                break;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // can a creature attack given the combat state
    /**
     * <p>
     * canAttack.
     * </p>
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     * @param combat
     *            a {@link forge.game.combat.Combat} object.
     * @return a boolean.
     */
    public static boolean canAttack(final Card c, final GameEntity def, final Combat combat) {
        final int cntAttackers = combat.getAttackers().size();
        final Game game = c.getGame();

        if (c.getController().getMustAttackEntity() != null && c.getController().getMustAttackEntity() != def) {
            return false;
        }

        if (cntAttackers > 0) {
            for (final Card card : game.getCardsIn(ZoneType.Battlefield)) {
                for (final String keyword : card.getKeyword()) {
                    if (cntAttackers > 1) {
                        if (keyword.equals("No more than two creatures can attack each combat.")) {
                            return false;
                        }
                    }
                    if (keyword.equals("CARDNAME can only attack alone.") && combat.isAttacking(card)) {
                        return false;
                    }
                }
            }

            if (c.hasKeyword("CARDNAME can only attack alone.")) {
                return false;
            }

            if (game.getStaticEffects().getGlobalRuleChange(GlobalRuleChange.onlyOneAttackerACombat)) {
                return false;
            }
        }

        final int cntAttackersToDef = combat.getAttackersOf(def).size();
        if (cntAttackersToDef > 1) {
            for (final Card card : game.getCardsIn(ZoneType.Battlefield)) {
                for (final String keyword : card.getKeyword()) {
                	 if (keyword.equals("No more than two creatures can attack you each combat.") &&
                             card.getController().equals(def)) {
                         return false;
                     }
                }
            }
        }

        if ((c.hasKeyword("CARDNAME can't attack or block alone.") || c.hasKeyword("CARDNAME can't attack alone."))
                && c.getController().getCreaturesInPlay().size() < 2) {
            return false;
        }

        if ((cntAttackers > 0 || c.getController().getAttackedWithCreatureThisTurn()) &&
                game.getStaticEffects().getGlobalRuleChange(GlobalRuleChange.onlyOneAttackerATurn)) {
            return false;
        }

        return CombatUtil.canAttack(c, def);
    }

    // can a creature attack at the moment?
    /**
     * <p>
     * canAttack.
     * </p>
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canAttack(final Card c, final GameEntity defender) {
        return canAttack(c) && canAttackNextTurn(c, defender);
    }

    public static boolean canAttack(final Card c) {
        final Game game = c.getGame();
        if (c.isTapped() || c.isPhasedOut()
                || (c.hasSickness() && !c.hasKeyword("CARDNAME can attack as though it had haste."))
                || game.getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
            return false;
        }
        return true;
    }

    public static boolean canAttackerBeBlockedWithAmount(Card attacker, int amount, Combat combat) {
        if( amount == 0 )
            return false; // no block

        List<String> restrictions = Lists.newArrayList();
        for ( String kw : attacker.getKeyword() ) {
            if ( kw.startsWith("CantBeBlockedByAmount") )
                restrictions.add(TextUtil.split(kw, ' ', 2)[1]);
        }
        for ( String res : restrictions ) {
            int operand = Integer.parseInt(res.substring(2));
            String operator = res.substring(0,2);
            if (Expressions.compare(amount, operator, operand) )
                return false;
        }
        if (combat != null && attacker.hasKeyword("CARDNAME can't be blocked " +
                "unless all creatures defending player controls block it.")) {
            Player defender = combat.getDefenderPlayerByAttacker(attacker);
            if (amount < defender.getCreaturesInPlay().size()) {
                return false;
            }
        }

        return true;
    }

    // can a creature attack if untapped and without summoning sickness?
    /**
     * <p>
     * canAttackNextTurn.
     * </p>
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     * @param def
     *            the defending {@link GameEntity}.
     * @return a boolean.
     */
    public static boolean canAttackNextTurn(final Card c, final GameEntity defender) {
        if (!c.isCreature()) {
            return false;
        }

        for (String keyword : c.getKeyword()) {
            if (keyword.equals("CARDNAME can't attack.") || keyword.equals("CARDNAME can't attack or block.")) {
                return false;
            } else if (keyword.equals("CARDNAME can't attack if you cast a spell this turn.") && c.getController().getSpellsCastThisTurn() > 0) {
                return false;
            } else if (keyword.equals("Defender") && !c.hasKeyword("CARDNAME can attack as though it didn't have defender.")) {
                return false;
            } else if (keyword.equals("CARDNAME can't attack during extra turns.")) {
                if (c.getGame().getPhaseHandler().getPlayerTurn().isPlayingExtraTurn())
                    return false;
            } else if (keyword.startsWith("CARDNAME attacks specific player each combat if able")) {
                final String defined = keyword.split(":")[1];
                final Player player = AbilityUtils.getDefinedPlayers(c, defined, null).get(0);
                if (!defender.equals(player)) {
                    return false;
                }
            }
        }

        // The creature won't untap next turn
        if (c.isTapped() && !Untap.canUntap(c)) {
            return false;
        }
        final Game game = c.getGame();
        // CantBeActivated static abilities
        for (final Card ca : game.getCardsIn(ZoneType.listValueOf("Battlefield,Command"))) {
            final ArrayList<StaticAbility> staticAbilities = ca.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                if (stAb.applyAbility("CantAttack", c, defender)) {
                    return false;
                }
            }
        }

        return true;
    } // canAttack()

    /**
     * <p>
     * checkPropagandaEffects.
     * </p>
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     * @param bLast
     *            a boolean.
     */
    public static boolean checkPropagandaEffects(final Game game, final Card c, final Combat combat) {
        Cost attackCost = new Cost(ManaCost.ZERO, true);
        // Sort abilities to apply them in proper order
        for (Card card : game.getCardsIn(ZoneType.listValueOf("Battlefield,Command"))) {
            final ArrayList<StaticAbility> staticAbilities = card.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                Cost additionalCost = stAb.getAttackCost(c, combat.getDefenderByAttacker(c));
                if ( null != additionalCost )
                    attackCost.add(additionalCost);
            }
        }

        // Not a great solution, but prevents a crash by passing a fake SA for Propaganda payments
        // If there's a better way of handling this somewhere deeper in the code, feel free to remove
        SpellAbility fakeSA = new SpellAbility.EmptySa(c, c.getController());

        boolean isFree = attackCost.getTotalMana().isZero() && attackCost.isOnlyManaCost(); // true if needless to pay
        return isFree || c.getController().getController().payManaOptional(c, attackCost, fakeSA, "Pay additional cost to declare " + c + " an attacker", ManaPaymentPurpose.DeclareAttacker);
    }

    /**
     * <p>
     * This method checks triggered effects of attacking creatures, right before
     * defending player declares blockers.
     * </p>
     * @param game
     * 
     * @param c
     *            a {@link forge.game.card.Card} object.
     */
    public static void checkDeclaredAttacker(final Game game, final Card c, final Combat combat) {
        // Run triggers
        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Attacker", c);
        final List<Card> otherAttackers = combat.getAttackers();
        otherAttackers.remove(c);
        runParams.put("OtherAttackers", otherAttackers);
        runParams.put("Attacked", combat.getDefenderByAttacker(c));
        game.getTriggerHandler().runTrigger(TriggerType.Attacks, runParams, false);

        // Annihilator: can be copied by Strionic Resonator now
        if (!c.getDamageHistory().getCreatureAttackedThisCombat()) {
            for (final String key : c.getKeyword()) {
                if (!key.startsWith("Annihilator ")) continue;
                final String[] k = key.split(" ", 2);

                String sb = "Annihilator - Defending player sacrifices " + k[1] + " permanents.";
                String effect = "AB$ Sacrifice | Cost$ 0 | Defined$ DefendingPlayer | SacValid$ Permanent | Amount$ " + k[1];

                SpellAbility ability = AbilityFactory.getAbility(effect, c);
                ability.setActivatingPlayer(c.getController());
                ability.setDescription(sb);
                ability.setStackDescription(sb);
                ability.setTrigger(true);

                game.getStack().addSimultaneousStackEntry(ability);

            }
        }

        c.getDamageHistory().setCreatureAttackedThisCombat(true);
        c.getDamageHistory().clearNotAttackedSinceLastUpkeepOf();
        c.getController().setAttackedWithCreatureThisTurn(true);
        c.getController().incrementAttackersDeclaredThisTurn();
    } // checkDeclareAttackers


    public static void handleRampage(final Game game, final Card a, final List<Card> blockers) {
        for (final String keyword : a.getKeyword()) {
            int idx = keyword.indexOf("Rampage ");
            if ( idx < 0)
                continue;

            final int numBlockers = blockers.size();
            if (numBlockers > 1) {
                final int magnitude = Integer.valueOf(keyword.substring(idx + "Rampage ".length()));
                CombatUtil.executeRampageAbility(game, a, magnitude, numBlockers);
            }
        } // end Rampage
    }

    public static void handleFlankingKeyword(final Game game, final Card attacker, final List<Card> blockers) {
        for (Card blocker : blockers) {
            if (attacker.hasKeyword("Flanking") && !blocker.hasKeyword("Flanking")) {
                int flankingMagnitude = 0;

                for (String kw : attacker.getKeyword()) {
                    if (kw.equals("Flanking")) {
                        flankingMagnitude++;
                    }
                }

                // Rule 702.23b:  If a creature has multiple instances of flanking, each triggers separately.
                for( int i = 0; i < flankingMagnitude; i++ ) {
                    String effect = String.format("AB$ Pump | Cost$ 0 | Defined$ CardUID_%d | NumAtt$ -1 | NumDef$ -1 | ", blocker.getId());
                    String desc = String.format("StackDescription$ Flanking (The blocking %s gets -1/-1 until end of turn)", blocker.getName());

                    SpellAbility ability = AbilityFactory.getAbility(effect + desc, attacker);
                    ability.setActivatingPlayer(attacker.getController());
                    ability.setDescription(ability.getStackDescription());
                    ability.setTrigger(true);

                    game.getStack().addSimultaneousStackEntry(ability);
                }
            } // flanking

            blocker.addBlockedThisTurn(attacker);
            attacker.addBlockedByThisTurn(blocker);
        }
    }

    /**
     * executes Rampage abilities for a given card.
     * @param game
     * 
     * @param c
     *            the card to add rampage bonus to
     * @param magnitude
     *            the magnitude of rampage (ie Rampage 2 means magnitude should
     *            be 2)
     * @param numBlockers
     *            - the number of creatures blocking this rampaging creature
     */
    private static void executeRampageAbility(final Game game, final Card c, final int magnitude, final int numBlockers) {
        // numBlockers starts with 1 since it is for every creature beyond the first
        for (int i = 1; i < numBlockers; i++) {
            String effect = "AB$ Pump | Cost$ 0 | " + c.getId() + " | NumAtt$ " + magnitude + " | NumDef$ " + magnitude + " | ";
            String desc = "StackDescription$ Rampage " + magnitude + " (Whenever CARDNAME becomes blocked, it gets +" + magnitude + "/+"
                    + magnitude + " until end of turn for each creature blocking it beyond the first.)";

            SpellAbility ability = AbilityFactory.getAbility(effect + desc, c);
            ability.setActivatingPlayer(c.getController());
            ability.setDescription(ability.getStackDescription());
            ability.setTrigger(true);

            game.getStack().addSimultaneousStackEntry(ability);
        }
    }

    public static List<Pair<Card, GameEntity>> getMandatoryAttackers(Player attackingPlayer, Combat combat, List<GameEntity> defenders) {
        List<Pair<Card, GameEntity>> attackers = new ArrayList<Pair<Card, GameEntity>>();
        List<Card> possibleAttackers = attackingPlayer.getCardsIn(ZoneType.Battlefield);
        for (Card c : Iterables.filter(possibleAttackers, CardPredicates.Presets.CREATURES)) {
            GameEntity mustAttack = c.getController().getMustAttackEntity() ;
            if (c.hasStartOfKeyword("CARDNAME attacks specific player each combat if able")) {
                final int i = c.getKeywordPosition("CARDNAME attacks specific player each combat if able");
                final String defined = c.getKeyword().get(i).split(":")[1];
                mustAttack = AbilityUtils.getDefinedPlayers(c, defined, null).get(0);
            }
            if (mustAttack != null && CombatUtil.canAttack(c, mustAttack, combat)) {
                attackers.add(Pair.of(c, mustAttack));
                continue;
            }
            if (c.hasKeyword("CARDNAME attacks each combat if able.") || 
                    (c.hasKeyword("CARDNAME attacks each turn if able.") && !c.getDamageHistory().getCreatureAttackedThisTurn())) {
                for (GameEntity def : defenders) {
                    if (CombatUtil.canAttack(c, def, combat)) {
                        attackers.add(Pair.of(c, def));
                        break;
                    }
                }
            }
        }
        return attackers;
    }

} // end class CombatUtil
