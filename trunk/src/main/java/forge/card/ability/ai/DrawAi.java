/*

* Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card.ability.ai;

import java.util.List;
import java.util.Random;

import forge.Card;
import forge.Singletons;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.cost.CostDiscard;
import forge.card.cost.CostPart;
import forge.card.cost.PaymentDecision;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCost;
import forge.game.ai.ComputerUtilMana;
import forge.game.phase.PhaseType;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class DrawAi extends SpellAbilityAi {

    @Override
    public boolean chkAIDrawback(SpellAbility sa, AIPlayer ai) {
        return targetAI(ai, sa, false);
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(AIPlayer ai, SpellAbility sa) {
        final Target tgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        final Cost abCost = sa.getPayCosts();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkCreatureSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                for (final CostPart part : abCost.getCostParts()) {
                    if (part instanceof CostDiscard) {
                        CostDiscard cd = (CostDiscard) part;
                        PaymentDecision decision = cd.decideAIPayment((AIPlayer) ai, sa, sa.getSourceCard());
                        if ( null == decision )
                            return false;
                        for (Card discard : decision.cards) {
                            if (!ComputerUtil.isWorseThanDraw(ai, discard)) {
                                return false;
                            }
                        }
                    }
                }
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }

        }

        final boolean bFlag = targetAI(ai, sa, false);

        if (!bFlag) {
            return false;
        }

        if (tgt != null) {
            final List<Player> players = tgt.getTargetPlayers();
            if ((players.size() > 0) && players.get(0).isOpponentOf(ai)) {
                return true;
            }
        }

        // Don't use draw abilities before main 2 if possible
        if (Singletons.getModel().getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)
                && !sa.hasParam("ActivationPhases")) {
            return false;
        }
        if ((!Singletons.getModel().getGame().getPhaseHandler().getNextTurn().equals(ai)
                    || Singletons.getModel().getGame().getPhaseHandler().getPhase().isBefore(PhaseType.END_OF_TURN))
                && !sa.hasParam("PlayerTurn") && !SpellAbilityAi.isSorcerySpeed(sa)
                && ai.getCardsIn(ZoneType.Hand).size() > 1) {
            return false;
        }

        // Don't tap creatures that may be able to block
        if (ComputerUtil.waitForBlocking(sa) && !sa.hasParam("ActivationPhases")) {
            return false;
        }

        double chance = .4; // 40 percent chance of drawing with instant speed
                            // stuff
        final Random r = MyRandom.getRandom();
        boolean randomReturn = r.nextFloat() <= Math.pow(chance, sa.getActivationsThisTurn() + 1);
        if (SpellAbilityAi.isSorcerySpeed(sa)) {
            randomReturn = true;
        }
        if ((Singletons.getModel().getGame().getPhaseHandler().is(PhaseType.END_OF_TURN)
                && Singletons.getModel().getGame().getPhaseHandler().getNextTurn().equals(ai))) {
            randomReturn = true;
        }

        if (SpellAbilityAi.playReusable(ai, sa)) {
            randomReturn = true;
        }

        return randomReturn;
    }

    private boolean targetAI(final Player ai, final SpellAbility sa, final boolean mandatory) {
        final Target tgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        final boolean drawback = (sa instanceof AbilitySub);
        Player opp = ai.getOpponent();

        int computerHandSize = ai.getCardsIn(ZoneType.Hand).size();
        final int humanLibrarySize = opp.getCardsIn(ZoneType.Library).size();
        final int computerLibrarySize = ai.getCardsIn(ZoneType.Library).size();
        final int computerMaxHandSize = ai.getMaxHandSize();

        //if a spell is used don't count the card
        if (sa.isSpell() && source.isInZone(ZoneType.Hand)) {
            computerHandSize -= 1;
        }

        int numCards = 1;
        if (sa.hasParam("NumCards")) {
            numCards = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumCards"), sa);
        }

        boolean xPaid = false;
        final String num = sa.getParam("NumCards");
        if ((num != null) && num.equals("X") && source.getSVar(num).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            if (sa instanceof AbilitySub && !source.getSVar("PayX").equals("")) {
                numCards = Integer.parseInt(source.getSVar("PayX"));
            } else {
                numCards = ComputerUtilMana.determineLeftoverMana(sa, ai);
                source.setSVar("PayX", Integer.toString(numCards));
            }
            xPaid = true;
        }
        //if (n)

        // TODO: if xPaid and one of the below reasons would fail, instead of
        // bailing
        // reduce toPay amount to acceptable level

        if (tgt != null) {
            // ability is targeted
            tgt.resetTargets();

            final boolean canTgtHuman = sa.canTarget(opp);
            final boolean canTgtComp = sa.canTarget(ai);
            boolean tgtHuman = false;

            if (!canTgtHuman && !canTgtComp) {
                return false;
            }

            if (canTgtHuman && !opp.cantLose() && (numCards >= humanLibrarySize)) {
                // Deck the Human? DO IT!
                tgt.addTarget(opp);
                return true;
            }

            if (numCards >= computerLibrarySize) {
                if (xPaid) {
                    numCards = computerLibrarySize - 1;
                    source.setSVar("PayX", Integer.toString(numCards));
                } else {
                    // Don't deck your self
                    if (!mandatory) {
                        return false;
                    }
                    tgtHuman = true;
                }
            }

            if (((computerHandSize + numCards) > computerMaxHandSize)
                    && Singletons.getModel().getGame().getPhaseHandler().isPlayerTurn(ai)) {
                if (xPaid) {
                    numCards = computerMaxHandSize - computerHandSize;
                    source.setSVar("PayX", Integer.toString(numCards));
                } else {
                    // Don't draw too many cards and then risk discarding cards
                    // at EOT
                    if (!(sa.hasParam("NextUpkeep") || (sa instanceof AbilitySub)) && !mandatory) {
                        return false;
                    }
                }
            }

            if (numCards == 0 && !mandatory && !drawback) {
                return false;
            }

            if ((!tgtHuman || !canTgtHuman) && canTgtComp) {
                tgt.addTarget(ai);
            } else if (mandatory && canTgtHuman) {
                tgt.addTarget(opp);
            } else {
                return false;
            }
        } else if (!mandatory) {
            // TODO: consider if human is the defined player

            // ability is not targeted
            if (numCards >= computerLibrarySize) {
                // Don't deck yourself
                return false;
            }

            if (numCards == 0  && !drawback) {
                return false;
            }

            if ((computerHandSize + numCards > computerMaxHandSize)
                    && Singletons.getModel().getGame().getPhaseHandler().isPlayerTurn(ai)
                    && !sa.isTrigger()) {
                // Don't draw too many cards and then risk discarding cards at
                // EOT
                if (!sa.hasParam("NextUpkeep") && !drawback) {
                    return false;
                }
            }
        }
        return true;
    } // drawTargetAI()


    @Override
    protected boolean doTriggerAINoCost(AIPlayer ai, SpellAbility sa, boolean mandatory) {
        return targetAI(ai, sa, mandatory);
    }

}
