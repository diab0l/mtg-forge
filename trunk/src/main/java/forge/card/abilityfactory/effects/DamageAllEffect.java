package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.CardLists;
import forge.Singletons;
import forge.card.abilityfactory.AbilityUtils;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class DamageAllEffect extends SpellEffect {
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

        final String damage = sa.getParam("NumDmg");
        final int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), damage, sa);

        final Target tgt = sa.getTarget();
        Player targetPlayer = null;
        if (tgt != null) {
            targetPlayer = tgt.getTargetPlayers().get(0);
        }

        String players = "";
        List<Card> list = new ArrayList<Card>();

        if (sa.hasParam("ValidPlayers")) {
            players = sa.getParam("ValidPlayers");
        }

        if (sa.hasParam("ValidCards")) {
            list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
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
