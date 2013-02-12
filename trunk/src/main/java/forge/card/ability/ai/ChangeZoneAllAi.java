package forge.card.ability.ai;

import java.util.List;
import java.util.Random;

import forge.Card;
import forge.CardLists;
import forge.Singletons;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAiLogic;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.ai.ComputerUtilCost;
import forge.game.phase.PhaseType;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class ChangeZoneAllAi extends SpellAiLogic {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        // Change Zone All, can be any type moving from one zone to another
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        final ZoneType destination = ZoneType.smartValueOf(sa.getParam("Destination"));
        final ZoneType origin = ZoneType.smartValueOf(sa.getParam("Origin"));

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

        }

        final Random r = MyRandom.getRandom();
        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // TODO targeting with ChangeZoneAll
        // really two types of targeting.
        // Target Player has all their types change zones
        // or target permanent and do something relative to that permanent
        // ex. "Return all Auras attached to target"
        // ex. "Return all blocking/blocked by target creature"

        final Player opp = ai.getOpponent();
        final List<Card> humanType = AbilityUtils.filterListByType(opp.getCardsIn(origin), sa.getParam("ChangeType"), sa);
        List<Card> computerType = ai.getCardsIn(origin);
        computerType = AbilityUtils.filterListByType(computerType, sa.getParam("ChangeType"), sa);
        final Target tgt = sa.getTarget();

        // TODO improve restrictions on when the AI would want to use this
        // spBounceAll has some AI we can compare to.
        if (origin.equals(ZoneType.Hand) || origin.equals(ZoneType.Library)) {
            if (tgt != null) {
                if (opp.getCardsIn(ZoneType.Hand).isEmpty()
                        || !opp.canBeTargetedBy(sa)) {
                    return false;
                }
                tgt.resetTargets();
                tgt.addTarget(opp);
            }
        } else if (origin.equals(ZoneType.Battlefield)) {
            // this statement is assuming the AI is trying to use this spell
            // offensively
            // if the AI is using it defensively, then something else needs to
            // occur
            // if only creatures are affected evaluate both lists and pass only
            // if human creatures are more valuable
            if (tgt != null) {
                if (opp.getCardsIn(ZoneType.Hand).isEmpty()
                        || !opp.canBeTargetedBy(sa)) {
                    return false;
                }
                tgt.resetTargets();
                tgt.addTarget(opp);
                computerType.clear();
            }
            if ((CardLists.getNotType(humanType, "Creature").size() == 0) && (CardLists.getNotType(computerType, "Creature").size() == 0)) {
                if ((CardFactoryUtil.evaluateCreatureList(computerType) + 200) >= CardFactoryUtil
                        .evaluateCreatureList(humanType)) {
                    return false;
                }
            } // otherwise evaluate both lists by CMC and pass only if human
              // permanents are more valuable
            else if ((CardFactoryUtil.evaluatePermanentList(computerType) + 3) >= CardFactoryUtil
                    .evaluatePermanentList(humanType)) {
                return false;
            }

            // Don't cast during main1?
            if (Singletons.getModel().getGame().getPhaseHandler().is(PhaseType.MAIN1, ai)) {
                return false;
            }
        } else if (origin.equals(ZoneType.Graveyard)) {
            if (tgt != null) {
                if (opp.getCardsIn(ZoneType.Graveyard).isEmpty()
                        || !opp.canBeTargetedBy(sa)) {
                    return false;
                }
                tgt.resetTargets();
                tgt.addTarget(opp);
            }
        } else if (origin.equals(ZoneType.Exile)) {

        } else if (origin.equals(ZoneType.Stack)) {
            // time stop can do something like this:
            // Origin$ Stack | Destination$ Exile | SubAbility$ DBSkip
            // DBSKipToPhase | DB$SkipToPhase | Phase$ Cleanup
            // otherwise, this situation doesn't exist
            return false;
        }

        if (destination.equals(ZoneType.Battlefield)) {
            if (sa.getParam("GainControl") != null) {
                // Check if the cards are valuable enough
                if ((CardLists.getNotType(humanType, "Creature").size() == 0) && (CardLists.getNotType(computerType, "Creature").size() == 0)) {
                    if ((CardFactoryUtil.evaluateCreatureList(computerType) + CardFactoryUtil
                            .evaluateCreatureList(humanType)) < 400) {
                        return false;
                    }
                } // otherwise evaluate both lists by CMC and pass only if human
                  // permanents are less valuable
                else if ((CardFactoryUtil.evaluatePermanentList(computerType) + CardFactoryUtil
                        .evaluatePermanentList(humanType)) < 6) {
                    return false;
                }
            } else {
                // don't activate if human gets more back than AI does
                if ((CardLists.getNotType(humanType, "Creature").size() == 0) && (CardLists.getNotType(computerType, "Creature").size() == 0)) {
                    if (CardFactoryUtil.evaluateCreatureList(computerType) <= (CardFactoryUtil
                            .evaluateCreatureList(humanType) + 100)) {
                        return false;
                    }
                } // otherwise evaluate both lists by CMC and pass only if human
                  // permanents are less valuable
                else if (CardFactoryUtil.evaluatePermanentList(computerType) <= (CardFactoryUtil
                        .evaluatePermanentList(humanType) + 2)) {
                    return false;
                }
            }
        }

        return (((r.nextFloat() < .8) || sa.isTrigger()) && chance);
    }

    /**
     * <p>
     * changeZoneAllPlayDrawbackAI.
     * </p>
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     * 
     * @return a boolean.
     */
    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer aiPlayer) {
        // if putting cards from hand to library and parent is drawing cards
        // make sure this will actually do something:

        return true;
    }


    @Override
    protected boolean doTriggerAINoCost(AIPlayer ai, SpellAbility sa, boolean mandatory) {
        // Change Zone All, can be any type moving from one zone to another

        final ZoneType destination = ZoneType.smartValueOf(sa.getParam("Destination"));
        final ZoneType origin = ZoneType.smartValueOf(sa.getParam("Origin"));

        final Player opp = ai.getOpponent();
        final List<Card> humanType = AbilityUtils.filterListByType(opp.getCardsIn(origin), sa.getParam("ChangeType"), sa);
        List<Card> computerType = ai.getCardsIn(origin);
        computerType = AbilityUtils.filterListByType(computerType, sa.getParam("ChangeType"), sa);

        // TODO improve restrictions on when the AI would want to use this
        // spBounceAll has some AI we can compare to.
        if (origin.equals(ZoneType.Hand) || origin.equals(ZoneType.Library)) {
            final Target tgt = sa.getTarget();
            if (tgt != null) {
                if (opp.getCardsIn(ZoneType.Hand).isEmpty()
                        || !opp.canBeTargetedBy(sa)) {
                    return false;
                }
                tgt.resetTargets();
                tgt.addTarget(opp);
            }
        } else if (origin.equals(ZoneType.Battlefield)) {
            // this statement is assuming the AI is trying to use this spell offensively
            // if the AI is using it defensively, then something else needs to occur
            // if only creatures are affected evaluate both lists and pass only
            // if human creatures are more valuable
            if ((CardLists.getNotType(humanType, "Creature").isEmpty()) && (CardLists.getNotType(computerType, "Creature").isEmpty())) {
                if (CardFactoryUtil.evaluateCreatureList(computerType) >= CardFactoryUtil
                        .evaluateCreatureList(humanType)) {
                    return false;
                }
            } // otherwise evaluate both lists by CMC and pass only if human
              // permanents are more valuable
            else if (CardFactoryUtil.evaluatePermanentList(computerType) >= CardFactoryUtil
                    .evaluatePermanentList(humanType)) {
                return false;
            }
        } else if (origin.equals(ZoneType.Graveyard)) {
            final Target tgt = sa.getTarget();
            if (tgt != null) {
                if (opp.getCardsIn(ZoneType.Graveyard).isEmpty()
                        || !opp.canBeTargetedBy(sa)) {
                    return false;
                }
                tgt.resetTargets();
                tgt.addTarget(opp);
            }
        } else if (origin.equals(ZoneType.Exile)) {

        } else if (origin.equals(ZoneType.Stack)) {
            // time stop can do something like this:
            // Origin$ Stack | Destination$ Exile | SubAbility$ DBSkip
            // DBSKipToPhase | DB$SkipToPhase | Phase$ Cleanup
            // otherwise, this situation doesn't exist
            return false;
        }

        if (destination.equals(ZoneType.Battlefield)) {
            if (sa.getParam("GainControl") != null) {
                // Check if the cards are valuable enough
                if ((CardLists.getNotType(humanType, "Creature").size() == 0) && (CardLists.getNotType(computerType, "Creature").size() == 0)) {
                    if ((CardFactoryUtil.evaluateCreatureList(computerType) + CardFactoryUtil
                            .evaluateCreatureList(humanType)) < 1) {
                        return false;
                    }
                } // otherwise evaluate both lists by CMC and pass only if human
                  // permanents are less valuable
                else if ((CardFactoryUtil.evaluatePermanentList(computerType) + CardFactoryUtil
                        .evaluatePermanentList(humanType)) < 1) {
                    return false;
                }
            } else {
                // don't activate if human gets more back than AI does
                if ((CardLists.getNotType(humanType, "Creature").isEmpty()) && (CardLists.getNotType(computerType, "Creature").isEmpty())) {
                    if (CardFactoryUtil.evaluateCreatureList(computerType) <= CardFactoryUtil
                            .evaluateCreatureList(humanType)) {
                        return false;
                    }
                } // otherwise evaluate both lists by CMC and pass only if human
                  // permanents are less valuable
                else if (CardFactoryUtil.evaluatePermanentList(computerType) <= CardFactoryUtil
                        .evaluatePermanentList(humanType)) {
                    return false;
                }
            }
        }

        return true;
    }

}
