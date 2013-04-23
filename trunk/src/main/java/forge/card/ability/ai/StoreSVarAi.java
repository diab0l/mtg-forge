package forge.card.ability.ai;

import forge.Card;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCombat;
import forge.game.player.AIPlayer;

public class StoreSVarAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        //Tree of Redemption

        final Card source = sa.getSourceCard();
        if (ComputerUtil.waitForBlocking(sa) || ai.getLife() + 1 >= source.getNetDefense()
                || (ai.getLife() > 5 && !ComputerUtilCombat.lifeInSeriousDanger(ai, ai.getGame().getCombat()))) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(AIPlayer aiPlayer, SpellAbility sa, boolean mandatory) {

        return true;
    }

}
