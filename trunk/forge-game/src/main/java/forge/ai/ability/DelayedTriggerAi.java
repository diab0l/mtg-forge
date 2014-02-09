package forge.ai.ability;

import forge.ai.AiController;
import forge.ai.AiPlayDecision;
import forge.ai.SpellAbilityAi;
import forge.game.ability.AbilityFactory;
import forge.game.player.Player;
import forge.game.player.PlayerControllerAi;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;

public class DelayedTriggerAi extends SpellAbilityAi {

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getHostCard().getSVar(svarName), sa.getHostCard());
        trigsa.setActivatingPlayer(ai);

        if (trigsa instanceof AbilitySub) {
            return ((AbilitySub) trigsa).getApi().getAi().chkDrawbackWithSubs(ai, (AbilitySub)trigsa);
        } else {
            return AiPlayDecision.WillPlay == ((PlayerControllerAi)ai.getController()).getAi().canPlaySa(trigsa);
        }
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getHostCard().getSVar(svarName), sa.getHostCard());
        AiController aic = ((PlayerControllerAi)ai.getController()).getAi();
        trigsa.setActivatingPlayer(ai);

        if (!sa.hasParam("OptionalDecider")) {
            return aic.doTrigger(trigsa, true);
        } else {
            return aic.doTrigger(trigsa, !sa.getParam("OptionalDecider").equals("You"));
        }
    }

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getHostCard().getSVar(svarName), sa.getHostCard());
        trigsa.setActivatingPlayer(ai);
        return AiPlayDecision.WillPlay == ((PlayerControllerAi)ai.getController()).getAi().canPlaySa(trigsa);
    }

}
