package forge.card.ability.effects;

import java.util.List;

import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.player.Player;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class PlaneswalkEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        Game game = sa.getActivatingPlayer().getGame();
        
        for(Player p : game.getPlayers())
        {
            p.leaveCurrentPlane();
        }
        if(sa.hasParam("Defined")) {
            List<Card> destinations = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa);
            sa.getActivatingPlayer().planeswalkTo(destinations);
        }
        else
        {
            sa.getActivatingPlayer().planeswalk();
        }
        
    }

}
