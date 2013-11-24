package forge.game.ability.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import forge.Singletons;
import forge.ai.ComputerUtil;
import forge.game.GameEntity;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CounterType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.gui.input.InputProliferate;

public class CountersProliferateEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Proliferate.");
        sb.append(" (You choose any number of permanents and/or players with ");
        sb.append("counters on them, then give each another counter of a kind already there.)");

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        Player controller = sa.getSourceCard().getController();
        if (controller.isHuman()) {
            InputProliferate inp = new InputProliferate();
            inp.setCancelAllowed(true);
            Singletons.getControl().getInputQueue().setInputAndWait(inp);
            if ( inp.hasCancelled() )
                return;
            
            for(GameEntity ge: inp.getSelected()) {
                if( ge instanceof Player )
                    ((Player) ge).addPoisonCounters(1, sa.getSourceCard());
                else if( ge instanceof Card)
                    ((Card) ge).addCounter(inp.getCounterFor(ge), 1, true);
            }
        } else {
            resolveAI(controller, sa);
        }
    }

    private static void resolveAI(final Player ai, final SpellAbility sa) {
        final List<Player> allies = ai.getAllies();
        allies.add(ai);
        final List<Player> enemies = ai.getOpponents();
        final Predicate<Card> predProliferate = new Predicate<Card>() {
            @Override
            public boolean apply(Card crd) {
                for (final Entry<CounterType, Integer> c1 : crd.getCounters().entrySet()) {
                    if (ComputerUtil.isNegativeCounter(c1.getKey(), crd) && enemies.contains(crd.getController())) {
                        return true;
                    }
                    if (!ComputerUtil.isNegativeCounter(c1.getKey(), crd) && allies.contains(crd.getController())) {
                        return true;
                    }
                }
                return false;
            }
        };

        List<Card> cardsToProliferate = CardLists.filter(ai.getGame().getCardsIn(ZoneType.Battlefield), predProliferate);
        List<Player> playersToPoison = new ArrayList<Player>();
        for (Player e : enemies) {
            if (e.getPoisonCounters() > 0) {
                playersToPoison.add(e);
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("<html>Proliferate. Computer selects:<br>");
        if (cardsToProliferate.isEmpty() && playersToPoison.isEmpty()) {
            sb.append("<b>nothing</b>.");
        } else {
            for (Card c : cardsToProliferate) {
                sb.append(c.getController().getName());
                sb.append("'s <b>");
                sb.append(c.getName());
                sb.append("</b><br>");
            }

            if (!playersToPoison.isEmpty()) {
                sb.append("<br>The following players: <br>");
            }
            for (Player p : playersToPoison) {
                sb.append("<b>");
                sb.append(p.getName());
                sb.append("</b><br>");
            }
        } // else
        sb.append("</html>");

        // add a counter of one counter type, if it would benefit the
        // computer
        for (final Card c : cardsToProliferate) {
            for (final Entry<CounterType, Integer> c1 : c.getCounters().entrySet()) {
                if (ComputerUtil.isNegativeCounter(c1.getKey(), c) && enemies.contains(c.getController()))
                {
                    c.addCounter(c1.getKey(), 1, true);
                    break;
                }
                if (!ComputerUtil.isNegativeCounter(c1.getKey(), c) && allies.contains(c.getController()))
                {
                    c.addCounter(c1.getKey(), 1, true);
                    break;
                }
            }
        }

        for (final Player p : playersToPoison) {
            p.addPoisonCounters(1, sa.getSourceCard());
        }
    }

}
