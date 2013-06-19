package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.CounterType;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.cost.CostPart;
import forge.card.cost.CostSacrifice;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class DestroyAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#chkAIDrawback(java.util.Map, forge.card.spellability.SpellAbility, forge.game.player.Player)
     */
    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        return canPlayAI(ai, sa);
    }
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(final Player ai, SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what the expected targets could be
        final Random r = MyRandom.getRandom();
        final Cost abCost = sa.getPayCosts();
        final TargetRestrictions abTgt = sa.getTargetRestrictions();
        final Card source = sa.getSourceCard();
        final boolean noRegen = sa.hasParam("NoRegen");
        List<Card> list;

        if (abCost != null) {
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // Targeting
        if (abTgt != null) {
            sa.resetTargets();
            list = CardLists.getTargetableCards(ai.getOpponent().getCardsIn(ZoneType.Battlefield), sa);
            list = CardLists.getValidCards(list, abTgt.getValidTgts(), source.getController(), source);
            if (sa.hasParam("AITgts")) {
                list = CardLists.getValidCards(list, sa.getParam("AITgts"), sa.getActivatingPlayer(), source);
            }
            list = CardLists.getNotKeyword(list, "Indestructible");
            if (!SpellAbilityAi.playReusable(ai, sa)) {
                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        //Check for cards that can be sacrificed in response
                        for (final SpellAbility ability : c.getAllSpellAbilities()) {
                            if (ability.isAbility()) {
                                final Cost cost = ability.getPayCosts();
                                for (final CostPart part : cost.getCostParts()) {
                                    if (!(part instanceof CostSacrifice)) {
                                        continue;
                                    }
                                    CostSacrifice sacCost = (CostSacrifice) part;
                                    if (sacCost.payCostFromSource() && ComputerUtilCost.canPayCost(ability, c.getController())) {
                                        return false;
                                    }
                                }
                            }
                        }
                        //Check for undying
                        return (!c.hasKeyword("Undying") || c.getCounters(CounterType.P1P1) > 0);
                    }
                });
            }

            // If NoRegen is not set, filter out creatures that have a
            // regeneration shield
            if (!noRegen) {
                // TODO filter out things that might be tougher?
                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return ((c.getShield() == 0) && !ComputerUtil.canRegenerate(ai, c));
                    }
                });
            }

            if (list.size() == 0) {
                return false;
            }
            // target loop
            while (sa.getTargets().getNumTargeted() < abTgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    if ((sa.getTargets().getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (sa.getTargets().getNumTargeted() == 0)) {
                        sa.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }

                Card choice = null;
                // If the targets are only of one type, take the best
                if (CardLists.getNotType(list, "Creature").isEmpty()) {
                    choice = ComputerUtilCard.getBestCreatureAI(list);
                } else if (CardLists.getNotType(list, "Land").isEmpty()) {
                    choice = ComputerUtilCard.getBestLandAI(list);
                } else {
                    choice = ComputerUtilCard.getMostExpensivePermanentAI(list, sa, true);
                }

                if (choice == null) { // can't find anything left
                    if ((sa.getTargets().getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (sa.getTargets().getNumTargeted() == 0)) {
                        sa.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }
                list.remove(choice);
                sa.getTargets().add(choice);
            }
        } else {
            if (sa.hasParam("Defined")) {
                list = new ArrayList<Card>(AbilityUtils.getDefinedCards(source, sa.getParam("Defined"), sa));
                if (list.isEmpty()
                        || !CardLists.filterControlledBy(list, ai).isEmpty()
                        || CardLists.getNotKeyword(list, "Indestructible").isEmpty()) {
                    return false;
                }
            }
        }

        return chance;
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Card source = sa.getSourceCard();
        final boolean noRegen = sa.hasParam("NoRegen");
        final Player opp = ai.getOpponent();
        if (tgt != null) {
            List<Card> list;
            list = ai.getGame().getCardsIn(ZoneType.Battlefield);
            list = CardLists.getTargetableCards(list, sa);
            list = CardLists.getValidCards(list, tgt.getValidTgts(), source.getController(), source);

            if ((list.size() == 0) || (list.size() < tgt.getMinTargets(sa.getSourceCard(), sa))) {
                return false;
            }

            sa.resetTargets();

            List<Card> preferred = CardLists.getNotKeyword(list, "Indestructible");
            preferred = CardLists.filterControlledBy(preferred, opp);

            // If NoRegen is not set, filter out creatures that have a
            // regeneration shield
            if (!noRegen) {
                // TODO filter out things that could regenerate in response?
                // might be tougher?
                preferred = CardLists.filter(preferred, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.getShield() == 0;
                    }
                });
            }

            for (final Card c : preferred) {
                list.remove(c);
            }

            while (sa.getTargets().getNumTargeted() < tgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (preferred.size() == 0) {
                    if ((sa.getTargets().getNumTargeted() == 0)
                            || (sa.getTargets().getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa))) {
                        if (!mandatory) {
                            sa.resetTargets();
                            return false;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    Card c;
                    if (CardLists.getNotType(preferred, "Creature").size() == 0) {
                        c = ComputerUtilCard.getBestCreatureAI(preferred);
                    } else if (CardLists.getNotType(preferred, "Land").size() == 0) {
                        c = ComputerUtilCard.getBestLandAI(preferred);
                    } else {
                        c = ComputerUtilCard.getMostExpensivePermanentAI(preferred, sa, false);
                    }
                    sa.getTargets().add(c);
                    preferred.remove(c);
                }
            }

            while (sa.getTargets().getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    break;
                } else {
                    Card c;
                    if (CardLists.getNotType(list, "Creature").size() == 0) {
                        c = ComputerUtilCard.getWorstCreatureAI(list);
                    } else {
                        c = ComputerUtilCard.getCheapestPermanentAI(list, sa, false);
                    }
                    sa.getTargets().add(c);
                    list.remove(c);
                }
            }

            if (sa.getTargets().getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
                return false;
            }
        } else {
            if (!mandatory) {
                return false;
            }
        }

        return true;
    }

}
