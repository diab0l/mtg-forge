package forge.card.abilityfactory.effects;

import java.util.Map;

import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;

public class LifeGainEffect extends SpellEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(Map<String, String> params, SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        final int amount = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("LifeAmount"), sa);
    
        final String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null) {
            sb.append(conditionDesc).append(" ");
        }

        for (final Player player : getTargetPlayers(sa, params)) {
            sb.append(player).append(" ");
        }

        sb.append("gains ").append(amount).append(" life.");
    
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(Map<String, String> params, SpellAbility sa) {
        final int lifeAmount = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("LifeAmount"), sa);
    
        final Target tgt = sa.getTarget();
    
        for (final Player p : getTargetPlayers(sa, params)) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                p.gainLife(lifeAmount, sa.getSourceCard());
            }
        }
    }
    
}