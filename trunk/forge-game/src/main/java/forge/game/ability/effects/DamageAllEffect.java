package forge.game.ability.effects;

import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

public class DamageAllEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        String desc = "";
        if (sa.hasParam("ValidDescription")) {
            desc = sa.getParam("ValidDescription");
        }

        final String damage = sa.getParam("NumDmg");
        final int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), damage, sa);


        final List<Card> definedSources = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("DamageSource"), sa);
        final Card source = definedSources.get(0);

        if (source != sa.getSourceCard()) {
            sb.append(source.toString()).append(" deals");
        } else {
            sb.append("Deals");
        }

        sb.append(" ").append(dmg).append(" damage to ").append(desc);

            return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final List<Card> definedSources = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("DamageSource"), sa);
        final Card card = definedSources.get(0);
        final Card source = sa.getSourceCard();
        final Game game = sa.getActivatingPlayer().getGame();

        final String damage = sa.getParam("NumDmg");
        final int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), damage, sa);

        Player targetPlayer = sa.getTargets().getFirstTargetedPlayer();

        String players = "";
        List<Card> list = new ArrayList<Card>();

        if (sa.hasParam("ValidPlayers")) {
            players = sa.getParam("ValidPlayers");
        }

        if (sa.hasParam("ValidCards")) {
            list = game.getCardsIn(ZoneType.Battlefield);
        }

        if (targetPlayer != null) {
            list = CardLists.filterControlledBy(list, targetPlayer);
        }

        list = AbilityUtils.filterListByType(list, sa.getParam("ValidCards"), sa);

        for (final Card c : list) {
            if (c.addDamage(dmg, card) && sa.hasParam("RememberDamaged")) {
                source.addRemembered(c);
            }
        }

        if (!players.equals("")) {
            final List<Player> playerList = AbilityUtils.getDefinedPlayers(card, players, sa);
            for (final Player p : playerList) {
                if (p.addDamage(dmg, card) && sa.hasParam("RememberDamaged")) {
                    source.addRemembered(p);
                }
            }
        }
    }
}
