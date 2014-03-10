package forge.ai.ability;

import com.google.common.base.Predicate;
import forge.ai.ComputerUtil;
import forge.ai.ComputerUtilCard;
import forge.ai.SpellAbilityAi;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates.Presets;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CopyPermanentAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        // Card source = sa.getHostCard();
        // TODO - I'm sure someone can do this AI better

        if (ComputerUtil.preventRunAwayActivations(sa)) {
            return false;
        }

        if (sa.hasParam("AtEOT") && !aiPlayer.getGame().getPhaseHandler().is(PhaseType.MAIN1)) {
            return false;
        } else {
        	return this.doTriggerAINoCost(aiPlayer, sa, false);
        }
    }

    @Override
    protected boolean doTriggerAINoCost(final Player aiPlayer, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getHostCard();

        // ////
        // Targeting

        final TargetRestrictions abTgt = sa.getTargetRestrictions();

        if (abTgt != null) {
            sa.resetTargets();
            if (sa.hasParam("TargetingPlayer")) {
                Player targetingPlayer = AbilityUtils.getDefinedPlayers(source, sa.getParam("TargetingPlayer"), sa).get(0);
                sa.setTargetingPlayer(targetingPlayer);
                return targetingPlayer.getController().chooseTargetsFor(sa);
            }
            List<Card> list = aiPlayer.getGame().getCardsIn(ZoneType.Battlefield);
            list = CardLists.getValidCards(list, abTgt.getValidTgts(), source.getController(), source);
            list = CardLists.getTargetableCards(list, sa);
            list = CardLists.filter(list, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    final Map<String, String> vars = c.getSVars();
                    return !vars.containsKey("RemAIDeck");
                }
            }); 
            // target loop
            while (sa.getTargets().getNumTargeted() < abTgt.getMaxTargets(sa.getHostCard(), sa)) {
                if (list.isEmpty()) {
                    if ((sa.getTargets().getNumTargeted() < abTgt.getMinTargets(sa.getHostCard(), sa))
                            || (sa.getTargets().getNumTargeted() == 0)) {
                        sa.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }

                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return !c.isType("Legendary") || c.getController().isOpponentOf(aiPlayer);
                    }
                });
                Card choice;
                if (!CardLists.filter(list, Presets.CREATURES).isEmpty()) {
                    choice = ComputerUtilCard.getBestCreatureAI(list);
                } else {
                    choice = ComputerUtilCard.getMostExpensivePermanentAI(list, sa, true);
                }

                if (choice == null) { // can't find anything left
                    if ((sa.getTargets().getNumTargeted() < abTgt.getMinTargets(sa.getHostCard(), sa))
                            || (sa.getTargets().getNumTargeted() == 0)) {
                        sa.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }
                list.remove(choice);
                sa.getTargets().add(choice);
            }
        } else {
            // if no targeting, it should always be ok
        }

        return true;
    }
    
    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#confirmAction(forge.game.player.Player, forge.card.spellability.SpellAbility, forge.game.player.PlayerActionConfirmMode, java.lang.String)
     */
    @Override
    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        //TODO: add logic here
        return true;
    }
    
    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#chooseSingleCard(forge.game.player.Player, forge.card.spellability.SpellAbility, java.util.List, boolean)
     */
    @Override
    public Card chooseSingleCard(Player ai, SpellAbility sa, Collection<Card> options, boolean isOptional, Player targetedPlayer) {
        // Select a card to attach to
        return ComputerUtilCard.getBestAI(options);
    }

}
