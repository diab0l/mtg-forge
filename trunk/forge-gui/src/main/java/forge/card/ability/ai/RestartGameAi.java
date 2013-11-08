package forge.card.ability.ai;

import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;

public class RestartGameAi extends SpellAbilityAi {

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.abilityfactory.AbilityFactoryAlterLife.SpellAiLogic#canPlayAI
     * (forge.game.player.Player, java.util.Map,
     * forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        // The only card that uses this is Karn Liberated

        // TODO Add Logic, check if AI is losing game state, or life

        // TODO Add Logic, check if any good cards will be available to be returned

        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        // This trigger AI is completely unused, but return true just in case
        return true;
    }
}
