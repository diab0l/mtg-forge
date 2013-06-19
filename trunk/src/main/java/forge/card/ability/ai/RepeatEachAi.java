package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.CounterType;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class RepeatEachAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        String logic = sa.getParam("AILogic");

        if ("CloneMyTokens".equals(logic)) {
            if (CardLists.filter(aiPlayer.getCreaturesInPlay(), Presets.TOKEN).size() < 2) {
                return false;
            }
        } else if ("CloneAllTokens".equals(logic)) {
            final Player opp = aiPlayer.getOpponent();
            List<Card> humTokenCreats = CardLists.filter(opp.getCreaturesInPlay(), Presets.TOKEN);
            List<Card> compTokenCreats = CardLists.filter(aiPlayer.getCreaturesInPlay(), Presets.TOKEN);

            if (compTokenCreats.size() <= humTokenCreats.size()) {
                return false;
            }
        } else if ("DoubleCounters".equals(logic)) {
            // TODO Improve this logic, double Planeswalker counters first, then +1/+1 on Useful creatures
            // Then Charge Counters, then -1/-1 on Opposing Creatures
            List<Card> perms = new ArrayList<Card>(aiPlayer.getCardsIn(ZoneType.Battlefield));
            perms = CardLists.filter(CardLists.getTargetableCards(perms, sa), new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return c.hasCounters();
                }
            });
            if (perms.isEmpty()) {
                return false;
            }
            CardLists.shuffle(perms);
            sa.setTargetCard(perms.get(0));
        } else if ("RemoveAllCounters".equals(logic)) {
            // Break Dark Depths
            List<Card> depthsList = aiPlayer.getCardsIn(ZoneType.Battlefield, "Dark Depths");
            depthsList = CardLists.filter(depthsList, new Predicate<Card>() {
                @Override
                public boolean apply(final Card crd) {
                    return crd.getCounters(CounterType.ICE) >= 3;
                }
            });

            if (depthsList.size() > 0) {
                sa.getTargets().add(depthsList.get(0));
                return true;
            }

            // Get rid of Planeswalkers:
            List<Card> list = new ArrayList<Card>(aiPlayer.getOpponent().getCardsIn(ZoneType.Battlefield));
            list = CardLists.filter(list, new Predicate<Card>() {
                @Override
                public boolean apply(final Card crd) {
                    return crd.isPlaneswalker() && (crd.getCounters(CounterType.LOYALTY) >= 5);
                }
            });

            if (list.isEmpty()) {
                return false;
            }

            sa.getTargets().add(list.get(0));
        } else if ("BalanceLands".equals(logic)) {
            if (CardLists.filter(aiPlayer.getCardsIn(ZoneType.Battlefield), Presets.LANDS).size() >= 5) {
                return false;
            }

            List<Player> opponents = aiPlayer.getOpponents();
            for(Player opp : opponents) {
                if (CardLists.filter(opp.getCardsIn(ZoneType.Battlefield), Presets.LANDS).size() < 4) {
                    return false;
                }
            }
        }

        // TODO Add some normal AI variability here

        return true;
    }
}
