package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.card.ability.AbilityUtils;
import forge.card.ability.ApiType;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.Game;
import forge.game.ai.ComputerUtilCombat;
import forge.game.ai.ComputerUtilCost;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class ChooseSourceAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(final Player ai, SpellAbility sa) {
        // TODO: AI Support! Currently this is copied from AF ChooseCard.
        //       When implementing AI, I believe AI also needs to be made aware of the damage sources chosen
        //       to be prevented (e.g. so the AI doesn't attack with a creature that will not deal any damage
        //       to the player because a CoP was pre-activated on it - unless, of course, there's another
        //       possible reason to attack with that creature).
        final Card host = sa.getSourceCard();
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }
        }

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (sa.canTarget(ai.getOpponent())) {
                tgt.addTarget(ai.getOpponent());
            } else {
                return false;
            }
        }
        if (sa.hasParam("AILogic")) {
            final Game game = ai.getGame();
            if (sa.getParam("AILogic").equals("NeedsPrevention")) {
                if (!game.getStack().isEmpty()) {
                    final SpellAbility topStack = game.getStack().peekAbility();
                    if (sa.hasParam("Choices") && !topStack.getSourceCard().isValid(sa.getParam("Choices"), ai, source)) {
                        return false;
                    }
                    final ApiType threatApi = topStack.getApi();
                    if (threatApi != ApiType.DealDamage && threatApi != ApiType.DamageAll) {
                        return false;
                    }

                    final Card threatSource = topStack.getSourceCard();
                    ArrayList<Object> objects = new ArrayList<Object>();
                    final Target threatTgt = topStack.getTarget();

                    if (threatTgt == null) {
                        if (topStack.hasParam("Defined")) {
                            objects = AbilityUtils.getDefinedObjects(threatSource, topStack.getParam("Defined"), topStack);
                        } else if (topStack.hasParam("ValidPlayers")) {
                            objects.addAll(AbilityUtils.getDefinedPlayers(threatSource, topStack.getParam("ValidPlayers"), topStack));
                        }
                    } else {
                        objects.addAll(threatTgt.getTargetPlayers());
                    }
                    if (!objects.contains(ai) || topStack.hasParam("NoPrevention")) {
                        return false;
                    }
                    int dmg = AbilityUtils.calculateAmount(threatSource, topStack.getParam("NumDmg"), topStack);
                    if (ComputerUtilCombat.predictDamageTo(ai, dmg, threatSource, false) <= 0) {
                        return false;
                    }
                    return true;
                }
                if (!game.getPhaseHandler().getPhase()
                        .equals(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
                    return false;
                }
                List<Card> choices = game.getCardsIn(ZoneType.Battlefield);
                if (sa.hasParam("Choices")) {
                    choices = CardLists.getValidCards(choices, sa.getParam("Choices"), host.getController(), host);
                }
                choices = CardLists.filter(choices, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        if (!c.isAttacking(ai) || !game.getCombat().isUnblocked(c)) {
                            return false;
                        }
                        return ComputerUtilCombat.damageIfUnblocked(c, ai, game.getCombat()) > 0;
                    }
                });
                if (choices.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
