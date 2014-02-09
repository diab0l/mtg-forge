package forge.game.ability.effects;

import forge.game.GameEntity;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;

import java.util.List;

public class MustAttackEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final Card host = sa.getSourceCard();
        final StringBuilder sb = new StringBuilder();


        // end standard pre-

        final List<Player> tgtPlayers = getTargetPlayers(sa);

        String defender = null;
        if (sa.getParam("Defender").equals("Self")) {
            defender = host.toString();
        } else {
            defender = host.getController().toString();
        }

        for (final Player player : tgtPlayers) {
            sb.append("Creatures ").append(player).append(" controls attack ");
            sb.append(defender).append(" during his or her next turn.");
        }

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final List<Player> tgtPlayers = getTargetPlayers(sa);
        final TargetRestrictions tgt = sa.getTargetRestrictions();

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                GameEntity entity;
                if (sa.getParam("Defender").equals("Self")) {
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
