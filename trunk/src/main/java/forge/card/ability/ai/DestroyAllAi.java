package forge.card.ability.ai;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.ai.ComputerUtilMana;
import forge.game.player.AIPlayer;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class DestroyAllAi extends SpellAbilityAi {

    private static final Predicate<Card> predicate = new Predicate<Card>() {
        @Override
        public boolean apply(final Card c) {
            return !(c.hasKeyword("Indestructible") || c.getSVar("SacMe").length() > 0);
        }
    };

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#doTriggerAINoCost(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility, boolean)
     */
    @Override
    protected boolean doTriggerAINoCost(AIPlayer ai, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getSourceCard();
        final Target tgt = sa.getTarget();
        String valid = "";
        if (mandatory) {
            return true;
        }
        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }
        List<Card> humanlist =
                CardLists.getValidCards(ai.getOpponent().getCardsIn(ZoneType.Battlefield), valid.split(","), source.getController(), source);
        List<Card> computerlist =
                CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), valid.split(","), source.getController(), source);
        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(ai.getOpponent());
            computerlist.clear();
        }

        humanlist = CardLists.filter(humanlist, predicate);
        computerlist = CardLists.filter(computerlist, predicate);
        if (humanlist.isEmpty() && !computerlist.isEmpty()) {
            return false;
        }

        // if only creatures are affected evaluate both lists and pass only if
        // human creatures are more valuable
        if ((CardLists.getNotType(humanlist, "Creature").size() == 0) && (CardLists.getNotType(computerlist, "Creature").size() == 0)) {
            if (ComputerUtilCard.evaluateCreatureList(computerlist) >= ComputerUtilCard.evaluateCreatureList(humanlist)
                    && !computerlist.isEmpty()) {
                return false;
            }
        } // otherwise evaluate both lists by CMC and pass only if human
          // permanents are more valuable
        else if (ComputerUtilCard.evaluatePermanentList(computerlist) >= ComputerUtilCard.evaluatePermanentList(humanlist)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer aiPlayer) {
        //TODO: Check for bad outcome
        return true;
    }

    @Override
    protected boolean canPlayAI(final AIPlayer ai, SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what the expected targets could be
        final Random r = MyRandom.getRandom();
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        String valid = "";

        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        if (valid.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(xPay));
            valid = valid.replace("X", Integer.toString(xPay));
        }

        final Target tgt = sa.getTarget();

        List<Card> humanlist =
                CardLists.getValidCards(ai.getOpponent().getCardsIn(ZoneType.Battlefield), valid.split(","), source.getController(), source);
        List<Card> computerlist =
                CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), valid.split(","), source.getController(), source);
        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(ai.getOpponent());
            computerlist.clear();
        }

        humanlist = CardLists.filter(humanlist, predicate);
        computerlist = CardLists.filter(computerlist, predicate);

        if (abCost != null) {
            // AI currently disabled for some costs

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // if only creatures are affected evaluate both lists and pass only if
        // human creatures are more valuable
        if ((CardLists.getNotType(humanlist, "Creature").size() == 0) && (CardLists.getNotType(computerlist, "Creature").size() == 0)) {
            if ((ComputerUtilCard.evaluateCreatureList(computerlist) + 200) >= ComputerUtilCard
                    .evaluateCreatureList(humanlist)) {
                return false;
            }
        } // only lands involved
        else if ((CardLists.getNotType(humanlist, "Land").size() == 0) && (CardLists.getNotType(computerlist, "Land").size() == 0)) {
            if ((ComputerUtilCard.evaluatePermanentList(computerlist) + 1) >= ComputerUtilCard
                    .evaluatePermanentList(humanlist)) {
                return false;
            }
        } // otherwise evaluate both lists by CMC and pass only if human
          // permanents are more valuable
        else if ((ComputerUtilCard.evaluatePermanentList(computerlist) + 3) >= ComputerUtilCard
                .evaluatePermanentList(humanlist)) {
            return false;
        }

        return chance;
    }

}
