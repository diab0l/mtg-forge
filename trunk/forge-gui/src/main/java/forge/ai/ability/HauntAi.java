package forge.ai.ability;

import java.util.Collection;
import java.util.List;

import forge.ai.ComputerUtilCard;
import forge.ai.SpellAbilityAi;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;

public class HauntAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#canPlayAI(forge.game.player.Player, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        return false; // should not get here
    }
    

    @Override
    public Card chooseSingleCard(Player ai, SpellAbility sa, Collection<Card> creats, boolean isOptional, Player targetedPlayer) {
        final List<Card> oppCreats = CardLists.filterControlledBy(creats, ai.getOpponents());
        return ComputerUtilCard.getWorstCreatureAI(oppCreats.isEmpty() ? creats : oppCreats);
    }

}