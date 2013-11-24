package forge.game.ability.effects;

import java.util.List;

import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.util.Aggregates;

public class ChoosePlayerEffect extends SpellAbilityEffect {

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        for (final Player p : getTargetPlayers(sa)) {
            sb.append(p).append(" ");
        }
        sb.append("chooses a player.");

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final Card card = sa.getSourceCard();

        final List<Player> tgtPlayers = getTargetPlayers(sa);

        final TargetRestrictions tgt = sa.getTargetRestrictions();

        final List<Player> choices = sa.hasParam("Choices") ? AbilityUtils.getDefinedPlayers(
                sa.getSourceCard(), sa.getParam("Choices"), sa) : sa.getActivatingPlayer().getGame().getPlayers();

        final String choiceDesc = sa.hasParam("ChoiceTitle") ? sa.getParam("ChoiceTitle") : "Choose a player";
        final boolean random = sa.hasParam("Random");

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {

                // Was if (sa.getActivatingPlayer().isHuman()) but defined player was being
                // overwritten by activatingPlayer (or controller if no activator was set).
                // Revert if it causes issues and remove Goblin Festival from card database.
                Player chosen;
                if (random) {
                    chosen = choices.isEmpty() ? null : Aggregates.random(choices);
                } else {
                    chosen = choices.isEmpty() ? null : p.getController().chooseSinglePlayerForEffect(choices, sa, choiceDesc);
                }
                if( null != chosen )
                    card.setChosenPlayer(chosen);
            }
        }
    }
}
