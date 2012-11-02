package forge.card.abilityfactory.effects;

import java.util.ArrayList;

import forge.Card;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.gui.GuiChoose;

public class ChoosePlayerEffect extends SpellEffect {
    
    @Override
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        
        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard()).append(" - ");
        }
        
        ArrayList<Player> tgtPlayers;
        
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
        
        for (final Player p : tgtPlayers) {
            sb.append(p).append(" ");
        }
        sb.append("chooses a player.");
        
        return sb.toString();
    }
    
    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final Card card = sa.getSourceCard();
        
        ArrayList<Player> tgtPlayers;
        
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
        
        final ArrayList<Player> choices = params.containsKey("Choices") ? AbilityFactory.getDefinedPlayers(
                sa.getSourceCard(), params.get("Choices"), sa) : new ArrayList<Player>(Singletons.getModel().getGame().getPlayers());
        
        final String choiceDesc = params.containsKey("ChoiceTitle") ? params.get("ChoiceTitle") : "Choose a player";
        
        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                if (p.isHuman()) {
                    // Was if (sa.getActivatingPlayer().isHuman()) but defined player was being
                    // overwritten by activatingPlayer (or controller if no activator was set).
                    // Revert if it causes issues and remove Goblin Festival from card database.
                    final Object o = GuiChoose.one(choiceDesc, choices);
                    if (null == o) {
                        return;
                    }
                    final Player chosen = (Player) o;
                    card.setChosenPlayer(chosen);
                    
                } else {
                    if (params.containsKey("AILogic")) {
                        if (params.get("AILogic").equals("Curse")) {
                            card.setChosenPlayer(p.getOpponent());
                        } else {
                            card.setChosenPlayer(p);
                        }
                    } else {
                        card.setChosenPlayer(p);
                    }
                }
            }
        }
    }
}