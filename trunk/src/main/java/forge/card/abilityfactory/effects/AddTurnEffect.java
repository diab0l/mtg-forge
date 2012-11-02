package forge.card.abilityfactory.effects;

import java.util.ArrayList;

import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.phase.ExtraTurn;
import forge.game.player.Player;

public class AddTurnEffect extends SpellEffect {
    
    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final int numTurns = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumTurns"), sa);

        ArrayList<Player> tgtPlayers;

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                for (int i = 0; i < numTurns; i++) {
                    ExtraTurn extra = Singletons.getModel().getGame().getPhaseHandler().addExtraTurn(p);
                    if (params.containsKey("LoseAtEndStep")) {
                        extra.setLoseAtEndStep(true);
                    }
                    if (params.containsKey("SkipUntap")) {
                        extra.setSkipUntap(true);
                    }
                }
            }
        }
    }

    @Override
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {

        final StringBuilder sb = new StringBuilder();
        final int numTurns = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumTurns"), sa);
    
        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard()).append(" - ");
        } else {
            sb.append(" ");
        }
    
        ArrayList<Player> tgtPlayers;
    
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
    
        for (final Player player : tgtPlayers) {
            sb.append(player).append(" ");
        }
    
        sb.append("takes ");
        if (numTurns > 1) {
            sb.append(numTurns);
        } else {
            sb.append("an");
        }
        sb.append(" extra turn");
        if (numTurns > 1) {
            sb.append("s");
        }
        sb.append(" after this one.");
        return sb.toString();
    }

}
