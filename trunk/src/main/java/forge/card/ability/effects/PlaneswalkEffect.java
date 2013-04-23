package forge.card.ability.effects;

import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.GameState;
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
        GameState game = sa.getActivatingPlayer().getGame();
        
        System.out.println("AF Planeswalking!");
        
        for(Player p : game.getPlayers())
        {
            p.leaveCurrentPlane();
        }
        sa.getActivatingPlayer().planeswalk();
    }

}
