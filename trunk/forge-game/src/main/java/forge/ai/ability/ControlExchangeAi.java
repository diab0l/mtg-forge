package forge.ai.ability;

import com.google.common.base.Predicate;
import forge.ai.ComputerUtilCard;
import forge.ai.SpellAbilityAi;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

import java.util.List;
import java.util.Map;

public class ControlExchangeAi extends SpellAbilityAi {

/* (non-Javadoc)
 * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
 */
    @Override
    protected boolean canPlayAI(Player ai, final SpellAbility sa) {
        Card object1 = null;
        Card object2 = null;
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        sa.resetTargets();

        List<Card> list =
                CardLists.getValidCards(ai.getOpponent().getCardsIn(ZoneType.Battlefield), tgt.getValidTgts(), ai, sa.getHostCard());
        // AI won't try to grab cards that are filtered out of AI decks on
        // purpose
        list = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                final Map<String, String> vars = c.getSVars();
                return !vars.containsKey("RemAIDeck") && c.canBeTargetedBy(sa);
            }
        });
        object1 = ComputerUtilCard.getBestAI(list);
        if (sa.hasParam("Defined")) {
            object2 = AbilityUtils.getDefinedCards(sa.getHostCard(), sa.getParam("Defined"), sa).get(0);
        } else if (tgt.getMinTargets(sa.getHostCard(), sa) > 1) {
            List<Card> list2 = ai.getCardsIn(ZoneType.Battlefield);
            list2 = CardLists.getValidCards(list2, tgt.getValidTgts(), ai, sa.getHostCard());
            object2 = ComputerUtilCard.getWorstAI(list2);
            sa.getTargets().add(object2);
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        if (ComputerUtilCard.evaluateCreature(object1) > ComputerUtilCard.evaluateCreature(object2) + 40) {
            sa.getTargets().add(object1);
            return MyRandom.getRandom().nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#doTriggerAINoCost(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility, boolean)
     */
    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        if (sa.getTargetRestrictions() == null) {
            if (mandatory) {
                return true;
            }
        } else {
            return canPlayAI(aiPlayer, sa);
        }
        return true;
    }
}
