package forge.card.abilityfactory.effects;

import java.util.List;

import forge.Card;
import forge.CardLists;
import forge.Counters;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class CountersPutAllEffect extends SpellEffect  { 

    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final String type = params.get("CounterType");
        final int counterAmount = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("CounterNum"), sa);
        final String valid = params.get("ValidCards");
        final ZoneType zone = params.containsKey("ValidZone") ? ZoneType.smartValueOf(params.get("ValidZone")) : ZoneType.Battlefield;

        List<Card> cards = Singletons.getModel().getGame().getCardsIn(zone);
        cards = CardLists.getValidCards(cards, valid, sa.getSourceCard().getController(), sa.getSourceCard());

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            final Player pl = sa.getTargetPlayer();
            cards = CardLists.filterControlledBy(cards, pl);
        }

        for (final Card tgtCard : cards) {
            if (Singletons.getModel().getGame().getZoneOf(tgtCard).is(ZoneType.Battlefield)) {
                tgtCard.addCounter(Counters.valueOf(type), counterAmount);
            } else {
                // adding counters to something like re-suspend cards
                tgtCard.addCounterFromNonEffect(Counters.valueOf(type), counterAmount);
            }
        }
    }

    @Override
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
    
        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }
    
        final Counters cType = Counters.valueOf(params.get("CounterType"));
        final int amount = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("CounterNum"), sa);
        final String zone = params.containsKey("ValidZone") ? params.get("ValidZone") : "Battlefield";
    
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

}