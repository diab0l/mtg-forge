package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import forge.Card;
import forge.CardLists;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.player.AIPlayer;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class PlayAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();

        final Random r = MyRandom.getRandom();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }
        }

        // don't use this as a response
        if (!ai.getGame().getStack().isEmpty()) {
            return false;
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getRestrictions().getNumberTurnActivations());

        List<Card> cards;
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            ZoneType zone = tgt.getZone().get(0);
            cards = CardLists.getValidCards(ai.getGame().getCardsIn(zone), tgt.getValidTgts(), ai, source);
            if (cards.isEmpty()) {
                return false;
            }
            tgt.addTarget(ComputerUtilCard.getBestAI(cards));
        } else if (!sa.hasParam("Valid")) {
            cards = new ArrayList<Card>(AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa));
            if (cards.isEmpty()) {
                return false;
            }
        }
        return chance;
    }
    
    /**
     * <p>
     * doTriggerAINoCost
     * </p>
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     *
     * @return a boolean.
     */
    @Override
    protected boolean doTriggerAINoCost(final AIPlayer ai, final SpellAbility sa, final boolean mandatory) {

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            return false;
        }

        return true;
    }
}
