package forge.card.ability.ai;

import forge.card.ability.SpellAiLogic;
import forge.card.spellability.SpellAbility;
import forge.game.player.AIPlayer;

public class ShuffleAi extends SpellAiLogic {
    @Override
    protected boolean canPlayAI(AIPlayer aiPlayer, SpellAbility sa) {
        // not really sure when the compy would use this; maybe only after a
        // human
        // deliberately put a card on top of their library
        return false;
        /*
         * if (!ComputerUtil.canPayCost(sa)) return false;
         * 
         * Card source = sa.getSourceCard();
         * 
         * Random r = MyRandom.random; boolean randomReturn = r.nextFloat() <=
         * Math.pow(.667, sa.getActivationsThisTurn()+1);
         * 
         * if (AbilityFactory.playReusable(sa)) randomReturn = true;
         * 
         * Ability_Sub subAb = sa.getSubAbility(); if (subAb != null)
         * randomReturn &= subAb.chkAI_Drawback(); return randomReturn;
         */
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer aiPlayer) {
        return shuffleTargetAI(/*sa, false, false*/);
    }


    private boolean shuffleTargetAI(/*final SpellAbility sa, final boolean primarySA, final boolean mandatory*/) {
        return false;



    } // shuffleTargetAI()

    @Override
    protected boolean doTriggerAINoCost(AIPlayer aiPlayer, SpellAbility sa, boolean mandatory) {
        if (!shuffleTargetAI(/*sa, false, mandatory*/)) {
            return false;
        }

        return true;
    }
}
