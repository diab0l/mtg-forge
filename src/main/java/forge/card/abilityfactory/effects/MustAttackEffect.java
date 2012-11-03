package forge.card.abilityfactory.effects;

import java.util.List;
import java.util.Map;

import forge.Card;
import forge.GameEntity;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;

public class MustAttackEffect extends SpellEffect { 
    
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(Map<String, String> params, SpellAbility sa) {
        final Card host = sa.getSourceCard();
        final StringBuilder sb = new StringBuilder();

    
        // end standard pre-
    
        final List<Player> tgtPlayers = getTargetPlayers(sa, params);
    
    
        String defender = null;
        if (params.get("Defender").equals("Self")) {
            defender = host.toString();
        } else {
            // TODO - if more needs arise in the future
        }
    
        for (final Player player : tgtPlayers) {
            sb.append("Creatures ").append(player).append(" controls attack ");
            sb.append(defender).append(" during his or her next turn.");
        }

        return sb.toString();
    }

    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final List<Player> tgtPlayers = getTargetPlayers(sa, params);
        final Target tgt = sa.getTarget();

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                GameEntity entity;
                if (params.get("Defender").equals("Self")) {
                    entity = sa.getSourceCard();
                } else {
                    entity = p.getOpponent();
                }
                // System.out.println("Setting mustAttackEntity to: "+entity);
                p.setMustAttackEntity(entity);
            }
        }

    } // mustAttackResolve()

}