package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class DamagePreventAllEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final Card source = sa.getSourceCard();
        final GameState game = sa.getActivatingPlayer().getGame();
        final int numDam = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("Amount"), sa);

        String players = "";
        List<Card> list = new ArrayList<Card>();

        if (sa.hasParam("ValidPlayers")) {
            players = sa.getParam("ValidPlayers");
        }

        if (sa.hasParam("ValidCards")) {
            list = game.getCardsIn(ZoneType.Battlefield);
        }

        list = AbilityUtils.filterListByType(list, sa.getParam("ValidCards"), sa);

        for (final Card c : list) {
            c.addPreventNextDamage(numDam);
        }

        if (!players.equals("")) {
            final ArrayList<Player> playerList = new ArrayList<Player>(game.getPlayers());
            for (final Player p : playerList) {
                if (p.isValid(players, source.getController(), source)) {
                    p.addPreventNextDamage(numDam);
                }
            }
        }
    } // preventDamageAllResolve

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        String desc = sa.getDescription();

        if (desc.contains(":")) {
            desc = desc.split(":")[1];
        }
        sb.append(desc);

        return sb.toString();
    }

}
