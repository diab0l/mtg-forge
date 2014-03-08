package forge.ai.ability;

import forge.ai.ComputerUtilCost;
import forge.ai.SpellAbilityAi;
import forge.game.cost.Cost;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.util.Aggregates;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * TODO: Write javadoc for this type.
 *
 */
public class ChooseGenericEffectAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        return false;
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#chkAIDrawback(java.util.Map, forge.card.spellability.SpellAbility, forge.game.player.Player)
     */
    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player aiPlayer) {
        return canPlayAI(aiPlayer, sa);
    }    
    
    public SpellAbility chooseSingleSpellAbility(Player player, SpellAbility sa, List<SpellAbility> spells) {
        final String logic = sa.getParam("AILogic");
        if ("Random".equals(logic)) {
            return Aggregates.random(spells);
        } else if ("Phasing".equals(logic)) { // Teferi's Realm : keep aggressive 
            List<SpellAbility> filtered = Lists.newArrayList(Iterables.filter(spells, new Predicate<SpellAbility>() {
                @Override
                public boolean apply(final SpellAbility sp) {
                    return !sp.getDescription().contains("Creature") && !sp.getDescription().contains("Land");
                }
            }));
            return Aggregates.random(filtered);
        } else if ("PayUnlessCost".equals(logic)) {
            for (final SpellAbility sp : spells) {
                String unlessCost = sp.getParam("UnlessCost");
                sp.setActivatingPlayer(sa.getActivatingPlayer());
                Cost unless = new Cost(unlessCost, false);
                SpellAbility paycost = new SpellAbility.EmptySa(sa.getHostCard(), player);
                paycost.setPayCosts(unless);
                if (ComputerUtilCost.willPayUnlessCost(sp, player, unless, false, Lists.newArrayList(player))
                        && ComputerUtilCost.canPayCost(paycost, player)) {
                    return sp;
                }
            }
            return spells.get(0);
        } else {
            return spells.get(0);
        }
    }
}