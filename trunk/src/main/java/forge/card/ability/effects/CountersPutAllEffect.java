package forge.card.ability.effects;

import java.util.List;

import forge.Card;
import forge.CardLists;
import forge.CounterType;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class CountersPutAllEffect extends SpellAbilityEffect  {

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        final CounterType cType = CounterType.valueOf(sa.getParam("CounterType"));
        final int amount = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("CounterNum"), sa);
        final String zone = sa.hasParam("ValidZone") ? sa.getParam("ValidZone") : "Battlefield";

        sb.append("Put ").append(amount).append(" ").append(cType.getName()).append(" counter");
        if (amount != 1) {
            sb.append("s");
        }
        sb.append(" on each valid ");
        if (zone.matches("Battlefield")) {
            sb.append("permanent.");
        } else {
            sb.append("card in ").append(zone).append(".");
        }

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final String type = sa.getParam("CounterType");
        final int counterAmount = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("CounterNum"), sa);
        final String valid = sa.getParam("ValidCards");
        final ZoneType zone = sa.hasParam("ValidZone") ? ZoneType.smartValueOf(sa.getParam("ValidZone")) : ZoneType.Battlefield;
        final GameState game = sa.getActivatingPlayer().getGame();

        List<Card> cards = game.getCardsIn(zone);
        cards = CardLists.getValidCards(cards, valid, sa.getSourceCard().getController(), sa.getSourceCard());

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            final Player pl = sa.getTargetPlayer();
            cards = CardLists.filterControlledBy(cards, pl);
        }

        for (final Card tgtCard : cards) {
            if (game.getZoneOf(tgtCard).is(ZoneType.Battlefield)) {
                tgtCard.addCounter(CounterType.valueOf(type), counterAmount, true);
            } else {
                // adding counters to something like re-suspend cards
                tgtCard.addCounter(CounterType.valueOf(type), counterAmount, false);
            }
        }
    }

}
