package forge.card.ability.ai;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class DebuffAllAi extends SpellAbilityAi {
    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        String valid = "";
        final Random r = MyRandom.getRandom();
        // final Card source = sa.getSourceCard();
        final Card hostCard = sa.getSourceCard();
        final Player opp = ai.getOpponent();

        final boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn()); // to
        // prevent
        // runaway
        // activations

        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        List<Card> comp = CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), valid, hostCard.getController(), hostCard);
        List<Card> human = CardLists.getValidCards(opp.getCardsIn(ZoneType.Battlefield), valid, hostCard.getController(), hostCard);

        // TODO - add blocking situations here also

        // only count creatures that can attack
        human = CardLists.filter(human, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return CombatUtil.canAttack(c, opp);
            }
        });

        // don't use DebuffAll after Combat_Begin until AI is improved
        if (ai.getGame().getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_BEGIN)) {
            return false;
        }

        if (comp.size() > human.size()) {
            return false;
        }

        return (r.nextFloat() < .6667) && chance;
    } // debuffAllCanPlayAI()

    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        return true;
    }
}
