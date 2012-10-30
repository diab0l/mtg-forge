package forge.card.abilityfactory.ai;

import java.util.List;
import java.util.Map;
import java.util.Random;

import forge.Card;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellAiLogic;
import forge.card.cost.Cost;
import forge.card.cost.CostUtil;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.phase.PhaseType;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class DiscardAi extends SpellAiLogic {

    /**
     * <p>
     * discardCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    @Override
    public boolean canPlayAI(Player ai, Map<String,String> params, SpellAbility sa) {
        final Target tgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        final Cost abCost = sa.getPayCosts();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!CostUtil.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!CostUtil.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!CostUtil.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!CostUtil.checkRemoveCounterCost(abCost, source)) {
                return false;
            }

        }

        final boolean humanHasHand = ai.getOpponent().getCardsIn(ZoneType.Hand).size() > 0;

        if (tgt != null) {
            if (!discardTargetAI(ai, sa)) {
                return false;
            }
        } else {
            // TODO: Add appropriate restrictions
            final List<Player> players = AbilityFactory.getDefinedPlayers(sa.getSourceCard(),
                    params.get("Defined"), sa);

            if (players.size() == 1) {
                if (players.get(0).isComputer()) {
                    // the ai should only be using something like this if he has
                    // few cards in hand,
                    // cards like this better have a good drawback to be in the
                    // AIs deck
                } else {
                    // defined to the human, so that's fine as long the human
                    // has cards
                    if (!humanHasHand) {
                        return false;
                    }
                }
            } else {
                // Both players discard, any restrictions?
            }
        }

        if (!params.get("Mode").equals("Hand") && !params.get("Mode").equals("RevealDiscardAll")) {
            if (params.get("NumCards").equals("X") && source.getSVar("X").equals("Count$xPaid")) {
                // Set PayX here to maximum value.
                final int cardsToDiscard = Math.min(ComputerUtil.determineLeftoverMana(sa, ai), ai.getOpponent()
                        .getCardsIn(ZoneType.Hand).size());
                source.setSVar("PayX", Integer.toString(cardsToDiscard));
            }
        }

        // Don't use draw abilities before main 2 if possible
        if (Singletons.getModel().getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)
                && !params.containsKey("ActivationPhases")) {
            return false;
        }

        // Don't tap creatures that may be able to block
        if (ComputerUtil.waitForBlocking(sa) && !params.containsKey("ActivationPhases")) {
            return false;
        }

        final Random r = MyRandom.getRandom();
        boolean randomReturn = r.nextFloat() <= Math.pow(0.9, sa.getActivationsThisTurn());

        // some other variables here, like handsize vs. maxHandSize

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            randomReturn &= subAb.chkAIDrawback();
        }
        return randomReturn;
    } // discardCanPlayAI()

    /**
     * <p>
     * discardTargetAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private boolean discardTargetAI(final Player ai, final SpellAbility sa) {
        final Target tgt = sa.getTarget();
        Player opp = ai.getOpponent();
        if (opp.getCardsIn(ZoneType.Hand).isEmpty()) {
            return false;
        }
        if (tgt != null) {
            if (sa.canTarget(opp)) {
                tgt.addTarget(opp);
                return true;
            }
        }
        return false;
    } // discardTargetAI()



    /**
     * <p>
     * discardTriggerNoCost.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    @Override
    public boolean doTriggerAINoCost(Player ai, Map<String,String> params, SpellAbility sa, boolean mandatory) {
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            Player opp = ai.getOpponent();
            if (!discardTargetAI(ai, sa)) {
                if (mandatory && sa.canTarget(opp)) {
                    tgt.addTarget(opp);
                } else if (mandatory && sa.canTarget(ai)) {
                    tgt.addTarget(ai);
                } else {
                    return false;
                }
            }
        }

        return true;
    } // discardTrigger()

    /**
     * <p>
     * discardCheckDrawbackAI.
     * </p>
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param subAb
     *            a {@link forge.card.spellability.AbilitySub} object.
     * 
     * @return a boolean.
     */
    @Override
    public boolean chkAIDrawback(Map<String,String> params, SpellAbility sa, Player ai) {
        // Drawback AI improvements
        // if parent draws cards, make sure cards in hand + cards drawn > 0
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            return discardTargetAI(ai, sa);
        }
        // TODO: check for some extra things
        return true;
    } // discardCheckDrawbackAI()
}