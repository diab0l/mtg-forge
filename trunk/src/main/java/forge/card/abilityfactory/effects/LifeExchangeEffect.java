package forge.card.abilityfactory.effects;

import java.util.List;
import java.util.Map;

import forge.Card;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;

public class LifeExchangeEffect extends SpellEffect {

    // *************************************************************************
    // ************************ EXCHANGE LIFE **********************************
    // *************************************************************************
    
        
    
    // *************************************************************************
    // ************************* LOSE LIFE *************************************
    // *************************************************************************
    
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(Map<String, String> params, SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        final Player activatingPlayer = sa.getActivatingPlayer();
        final List<Player> tgtPlayers = getTargetPlayers(sa, params);

    
        if (tgtPlayers.size() == 1) {
            sb.append(activatingPlayer).append(" exchanges life totals with ");
            sb.append(tgtPlayers.get(0));
        } else if (tgtPlayers.size() > 1) {
            sb.append(tgtPlayers.get(0)).append(" exchanges life totals with ");
            sb.append(tgtPlayers.get(1));
        }
        sb.append(".");
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.AbilityFactoryAlterLife.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(Map<String, String> params, SpellAbility sa) {
        final Card source = sa.getSourceCard();
        Player p1;
        Player p2;
    
        final List<Player> tgtPlayers = getTargetPlayers(sa, params);
    
        if (tgtPlayers.size() == 1) {
            p1 = sa.getActivatingPlayer();
            p2 = tgtPlayers.get(0);
        } else {
            p1 = tgtPlayers.get(0);
            p2 = tgtPlayers.get(1);
        }
    
        final int life1 = p1.getLife();
        final int life2 = p2.getLife();
    
        if ((life1 > life2) && p1.canLoseLife()) {
            final int diff = life1 - life2;
            p1.loseLife(diff, source);
            p2.gainLife(diff, source);
        } else if ((life2 > life1) && p2.canLoseLife()) {
            final int diff = life2 - life1;
            p2.loseLife(diff, source);
            p1.gainLife(diff, source);
        } else {
            // they are equal, so nothing to do
        }
    
    }
    
}