package forge.ai.ability;

import java.util.List;

import forge.ai.SpellAbilityAi;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class BalanceAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#canPlayAI(forge.game.player.Player, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        String logic = sa.getParam("AILogic");
        
        int diff = 0;
        // TODO Add support for multiplayer logic
        final Player opp = aiPlayer.getOpponent();
        final List<Card> humPerms = opp.getCardsIn(ZoneType.Battlefield);
        final List<Card> compPerms = aiPlayer.getCardsIn(ZoneType.Battlefield);
        
        if ("BalanceCreaturesAndLands".equals(logic)) {
            // Copied over from hardcoded Balance. We should be checking value of the lands/creatures not just counting
            diff += CardLists.filter(humPerms, CardPredicates.Presets.LANDS).size() - 
                    CardLists.filter(compPerms, CardPredicates.Presets.LANDS).size();
            diff += 1.5 * ( CardLists.filter(humPerms, CardPredicates.Presets.CREATURES).size() - 
                    CardLists.filter(compPerms, CardPredicates.Presets.CREATURES).size());
        } else if ("BalancePermanents".equals(logic)) {
            // Don't cast if you have to sacrifice permanents
            diff += humPerms.size() - compPerms.size();
        }
        
        if (diff < 0) {
            // Don't sacrifice permanents even if opponent has a ton of cards in hand
            return false;
        }
            
        final List<Card> humHand = opp.getCardsIn(ZoneType.Hand);
        final List<Card> compHand = aiPlayer.getCardsIn(ZoneType.Hand);
        diff += 0.5 * (humHand.size() - compHand.size());

        // Larger differential == more chance to actually cast this spell
        return diff > 2 && MyRandom.getRandom().nextInt(100) < diff*10;
    }
}
