package forge.card.ability.ai;

import forge.Card;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityAi;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.GameState;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.ai.ComputerUtilMana;
import forge.game.player.AIPlayer;
import forge.util.MyRandom;

public class CounterAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        boolean toReturn = true;
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        final GameState game = ai.getGame();
        if (game.getStack().isEmpty()) {
            return false;
        }

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }
            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }
        }

        final Target tgt = sa.getTarget();
        if (tgt != null) {

            final SpellAbility topSA = game.getStack().peekAbility();
            if (!CardFactoryUtil.isCounterableBy(topSA.getSourceCard(), sa) || topSA.getActivatingPlayer() == ai) { 
                // might as well check for player's friendliness
                return false;
            }
            if (sa.hasParam("AITgts") && (topSA.getSourceCard() == null
                    || !topSA.getSourceCard().isValid(sa.getParam("AITgts"), sa.getActivatingPlayer(), source))) {
                return false;
            }

            tgt.resetTargets();
            if (sa.canTargetSpellAbility(topSA)) {
                tgt.addTarget(topSA);
            } else {
                return false;
            }
        } else {
            return false;
        }

        String unlessCost = sa.hasParam("UnlessCost") ? sa.getParam("UnlessCost").trim() : null;

        if (unlessCost != null && !unlessCost.endsWith(">")) {
            // Is this Usable Mana Sources? Or Total Available Mana?
            final int usableManaSources = ComputerUtilCard.getUsableManaSources(ai.getOpponent());
            int toPay = 0;
            boolean setPayX = false;
            if (unlessCost.equals("X") && source.getSVar(unlessCost).equals("Count$xPaid")) {
                setPayX = true;
                toPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
            } else {
                toPay = AbilityUtils.calculateAmount(source, unlessCost, sa);
            }

            if (toPay == 0) {
                return false;
            }

            if (toPay <= usableManaSources) {
                // If this is a reusable Resource, feel free to play it most of
                // the time
                if (!SpellAbilityAi.playReusable(ai,sa)) {
                    return false;
                }
            }

            if (setPayX) {
                source.setSVar("PayX", Integer.toString(toPay));
            }
        }

        // TODO Improve AI

        // Will return true if this spell can counter (or is Reusable and can
        // force the Human into making decisions)

        // But really it should be more picky about how it counters things


        return toReturn;
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer aiPlayer) {
        return doTriggerAINoCost(aiPlayer, sa, true);
    }

    @Override
    protected boolean doTriggerAINoCost(AIPlayer ai, SpellAbility sa, boolean mandatory) {

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            final GameState game = ai.getGame();
            if (game.getStack().isEmpty()) {
                return false;
            }
            final SpellAbility topSA = game.getStack().peekAbility();
            if (!CardFactoryUtil.isCounterableBy(topSA.getSourceCard(), sa) || topSA.getActivatingPlayer() == ai) {
                return false;
            }

            tgt.resetTargets();
            if (sa.canTargetSpellAbility(topSA)) {
                tgt.addTarget(topSA);
            } else {
                return false;
            }

            String unlessCost = sa.hasParam("UnlessCost") ? sa.getParam("UnlessCost").trim() : null;

            final Card source = sa.getSourceCard();
            if (unlessCost != null) {
                // Is this Usable Mana Sources? Or Total Available Mana?
                final int usableManaSources = ComputerUtilCard.getUsableManaSources(ai.getOpponent());
                int toPay = 0;
                boolean setPayX = false;
                if (unlessCost.equals("X") && source.getSVar(unlessCost).equals("Count$xPaid")) {
                    setPayX = true;
                    toPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
                } else {
                    toPay = AbilityUtils.calculateAmount(source, unlessCost, sa);
                }

                if (toPay == 0) {
                    return false;
                }

                if (toPay <= usableManaSources) {
                    // If this is a reusable Resource, feel free to play it most
                    // of the time
                    if (!SpellAbilityAi.playReusable(ai,sa) || (MyRandom.getRandom().nextFloat() < .4)) {
                        return false;
                    }
                }

                if (setPayX) {
                    source.setSVar("PayX", Integer.toString(toPay));
                }
            }
        }

        // TODO Improve AI

        // Will return true if this spell can counter (or is Reusable and can
        // force the Human into making decisions)

        // But really it should be more picky about how it counters things

        return true;
    }

}
