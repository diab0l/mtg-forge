package forge.card.ability.ai;


import forge.card.ability.AbilityFactory;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;

public class RepeatAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Player opp = ai.getOpponent();
        if (tgt != null) {
            if (!opp.canBeTargetedBy(sa)) {
                return false;
            }
            sa.resetTargets();
            sa.getTargets().add(opp);
        }
        return true;
    }
    
    @Override
    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
      //TODO add logic to have computer make better choice (ArsenalNut)
        return false;
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
    	// setup subability to repeat
        final SpellAbility repeat = AbilityFactory.getAbility(sa.getSourceCard().getSVar(sa.getParam("RepeatSubAbility")), sa.getSourceCard());

        if (repeat == null) {
        	return false;
        }

        repeat.setActivatingPlayer(sa.getActivatingPlayer());
        ((AbilitySub) repeat).setParent(sa);
        return repeat.doTrigger(mandatory, ai);
    }
}
