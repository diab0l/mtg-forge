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
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityStatic;
import forge.card.staticability.StaticAbility;
import forge.card.trigger.TriggerType;
import forge.game.Game;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCost;
import forge.game.player.HumanPlay;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * <p>
 * PhaseUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class PhaseUtil {
    // ******* UNTAP PHASE *****
    /**
     * <p>
     * skipUntap.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player.Player} object.
     * @return a boolean.
     */
    static boolean isSkipUntap(final Player p) {

        if (p.hasKeyword("Skip your next untap step.")) {
            p.removeKeyword("Skip your next untap step.");
            return true;
        }
        if (p.hasKeyword("Skip the untap step of this turn.")
                || p.hasKeyword("Skip your untap step.")) {
            return true;
        }

        return false;
    }


    // ********* Declare Attackers ***********

    /**
     * <p>
     * handleDeclareAttackers.
     * </p>
     */
    public static void handleDeclareAttackers(final Game game) {
        final Combat combat = game.getCombat();
        combat.verifyCreaturesInPlay();

        // Handles removing cards like Mogg Flunkies from combat if group attack
        // didn't occur
        final List<Card> filterList = combat.getAttackers();
        for (Card c : filterList) {
            if (c.hasKeyword("CARDNAME can't attack or block alone.") && c.isAttacking()) {
                if (combat.getAttackers().size() < 2) {
                    combat.removeFromCombat(c);
                }
            }
        }

        final List<Card> list = combat.getAttackers();

        // TODO move propaganda to happen as the Attacker is Declared
        // Remove illegal Propaganda attacks first only for attacking the Player


        for (final Card c : list) {
            boolean canAttack = CombatUtil.checkPropagandaEffects(game, c);
            if ( canAttack ) {
                if (!c.hasKeyword("Vigilance")) { c.tap(); }
            } else {
                game.getCombat().removeFromCombat(c);
            }
        }
        PhaseUtil.handleAttackingTriggers(game);
    }

    /**
     * <p>
     * handleAttackingTriggers.
     * </p>
     * @param game 
     */
    public static void handleAttackingTriggers(Game game) {
        final List<Card> list = game.getCombat().getAttackers();
        game.getStack().freezeStack();
        // Then run other Attacker bonuses
        // check for exalted:
        if (list.size() == 1) {
            final Player attackingPlayer = game.getCombat().getAttackingPlayer();
            int exaltedMagnitude = 0;
            for (Card card : attackingPlayer.getCardsIn(ZoneType.Battlefield)) {
                exaltedMagnitude += card.getKeywordAmount("Exalted");
            }

            if (exaltedMagnitude > 0) {
                CombatUtil.executeExaltedAbility(game, list.get(0), exaltedMagnitude);
                // Make sure exalted effects get applied only once per combat
            }

        }

        game.getGameLog().addCombatAttackers(game.getCombat());

        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Attackers", list);
        runParams.put("AttackingPlayer", game.getCombat().getAttackingPlayer());
        game.getTriggerHandler().runTrigger(TriggerType.AttackersDeclared, runParams, false);

        for (final Card c : list) {
            CombatUtil.checkDeclareAttackers(game, c);
        }
        game.getStack().unfreezeStack();
    }

    /**
     * <p>
     * handleDeclareBlockers.
     * </p>
     * 
     * @param game
     */
    public static void handleDeclareBlockers(Game game) {
        final Combat combat = game.getCombat();
        combat.verifyCreaturesInPlay();

        // Handles removing cards like Mogg Flunkies from combat if group block
        // didn't occur
        final List<Card> filterList = combat.getAllBlockers();
        for (Card blocker : filterList) {
            final List<Card> attackers = new ArrayList<Card>(combat.getAttackersBlockedBy(blocker));
            for (Card attacker : attackers) {
                Cost blockCost = new Cost(ManaCost.ZERO, true);
                // Sort abilities to apply them in proper order
                for (Card card : game.getCardsIn(ZoneType.Battlefield)) {
                    final ArrayList<StaticAbility> staticAbilities = card.getStaticAbilities();
                    for (final StaticAbility stAb : staticAbilities) {
                        Cost c1 = stAb.getBlockCost(blocker, attacker);
                        if ( c1 != null )
                            blockCost.add(c1);
                    }
                }
                
                boolean hasPaid = blockCost.getTotalMana().isZero() && blockCost.isOnlyManaCost(); // true if needless to pay
                if (!hasPaid) { 
                    final Ability ability = new AbilityStatic(blocker, blockCost, null) { @Override public void resolve() {} };
                    ability.setActivatingPlayer(blocker.getController());

                    if (blocker.getController().isHuman()) {
                        hasPaid = HumanPlay.payCostDuringAbilityResolve(ability, blockCost, null, game);
                    } else { // computer
                        if (ComputerUtilCost.canPayCost(ability, blocker.getController())) {
                            ComputerUtil.playNoStack(blocker.getController(), ability, game);
                            hasPaid = true;
                        }
                    }
                }

                if ( !hasPaid ) {
                    combat.removeBlockAssignment(attacker, blocker);
                }
            }
        }
        for (Card c : filterList) {
            if (c.hasKeyword("CARDNAME can't attack or block alone.") && c.isBlocking()) {
                if (combat.getAllBlockers().size() < 2) {
                    combat.undoBlockingAssignment(c);
                }
            }
        }

        game.getStack().freezeStack();

        combat.setUnblocked();

        List<Card> list = new ArrayList<Card>();
        list.addAll(combat.getAllBlockers());

        list = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !c.getDamageHistory().getCreatureBlockedThisCombat();
            }
        });

        final List<Card> attList = combat.getAttackers();

        CombatUtil.checkDeclareBlockers(game, list);

        for (final Card a : attList) {
            CombatUtil.checkBlockedAttackers(game, a, combat.getBlockers(a));
        }

        game.getStack().unfreezeStack();

        game.getGameLog().addCombatBlockers(game.getCombat());
    }
}
