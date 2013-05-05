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
package forge.card.spellability;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.card.ability.AbilityFactory;
import forge.card.ability.ApiType;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.replacement.ReplaceMoved;
import forge.card.replacement.ReplacementEffect;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerType;
import forge.game.GameState;
import forge.game.GlobalRuleChange;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCost;
import forge.game.ai.ComputerUtilMana;
import forge.game.phase.PhaseType;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * <p>
 * Spell_Permanent class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class SpellPermanent extends Spell {
    /** Constant <code>serialVersionUID=2413495058630644447L</code>. */
    private static final long serialVersionUID = 2413495058630644447L;

    /**
     * <p>
     * Constructor for Spell_Permanent.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     */
    public SpellPermanent(final Card sourceCard) {
        super(sourceCard, new Cost(sourceCard.getManaCost(), false));

        if (sourceCard.isCreature()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(sourceCard.getName()).append(" - Creature ").append(sourceCard.getNetAttack());
            sb.append(" / ").append(sourceCard.getNetDefense());
            this.setStackDescription(sb.toString());
        } else {
            this.setStackDescription(sourceCard.getName());
        }


        this.setDescription(this.getStackDescription());

        if (this.getPayCosts().getTotalMana().countX() > 0 && StringUtils.isNotBlank(getSourceCard().getSVar("X"))) {
            this.setSVar("X", this.getSourceCard().getSVar("X"));
        }

    } // Spell_Permanent()

    /** {@inheritDoc} */
    @Override
    public boolean canPlayAI() {

        final Card card = this.getSourceCard();
        ManaCost mana = this.getPayCosts().getTotalMana();
        final Player ai = getActivatingPlayer();
        final GameState game = ai.getGame();
        if (mana.countX() > 0) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtilMana.determineLeftoverMana(this, ai);
            if (xPay <= 0) {
                return false;
            }
            card.setSVar("PayX", Integer.toString(xPay));
        }
        // Prevent the computer from summoning Ball Lightning type creatures after attacking
        if (card.hasKeyword("At the beginning of the end step, sacrifice CARDNAME.")
                && (game.getPhaseHandler().isPlayerTurn(ai.getOpponent())
                     || game.getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS))) {
            return false;
        }

        // Prevent the computer from summoning Ball Lightning type creatures after attacking
        if (card.hasStartOfKeyword("You may cast CARDNAME as though it had flash. If")
                && !card.getController().couldCastSorcery(this)) {
            return false;
        }
        
        // Wait for Main2 if possible
        if (game.getPhaseHandler().is(PhaseType.MAIN1)
                && game.getPhaseHandler().isPlayerTurn(ai)
                && ai.getManaPool().totalMana() <= 0
                && !ComputerUtil.castPermanentInMain1(ai, this)) {
            return false;
        }
        // save cards with flash for surprise blocking
        if (card.hasKeyword("Flash")
                && (ai.isUnlimitedHandSize() || ai.getCardsIn(ZoneType.Hand).size() <= ai.getMaxHandSize())
                && ai.getManaPool().totalMana() <= 0
                && (game.getPhaseHandler().isPlayerTurn(ai)
                        || game.getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                && !card.hasETBTrigger())
                && !ComputerUtil.castPermanentInMain1(ai, this)) {
            return false;
        }

        return canPlayFromEffectAI(false, true);
    } // canPlayAI()

    /** {@inheritDoc} */
    @Override
    public boolean canPlayFromEffectAI(final boolean mandatory, final boolean withOutManaCost) {
        if (mandatory) {
            return true;
        }
        final AIPlayer ai = (AIPlayer) getActivatingPlayer();
        final Card card = this.getSourceCard();
        ManaCost mana = this.getPayCosts().getTotalMana();
        final Cost cost = this.getPayCosts();

        if (cost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkLifeCost(ai, cost, card, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, cost, card)) {
                return false;
            }

            if (!ComputerUtilCost.checkSacrificeCost(ai, cost, card)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(cost, card)) {
                return false;
            }
        }

        // check on legendary
        if (card.isType("Legendary")
                && !ai.getGame().getStaticEffects().getGlobalRuleChange(GlobalRuleChange.noLegendRule)) {
            final List<Card> list = ai.getCardsIn(ZoneType.Battlefield);
            if (Iterables.any(list, CardPredicates.nameEquals(card.getName()))) {
                return false;
            }
        }
        if (card.isPlaneswalker()) {
            List<Card> list = ai.getCardsIn(ZoneType.Battlefield);
            list = CardLists.filter(list, CardPredicates.Presets.PLANEWALKERS);

            for (int i = 0; i < list.size(); i++) {
                List<String> type = card.getType();
                final String subtype = type.get(type.size() - 1);
                final List<Card> cl = CardLists.getType(list, subtype);

                if (cl.size() > 0) {
                    return false;
                }
            }
        }
        if (card.isType("World")) {
            List<Card> list = ai.getCardsIn(ZoneType.Battlefield);
            list = CardLists.getType(list, "World");
            if (list.size() > 0) {
                return false;
            }
        }

        if (card.isCreature() && (card.getNetDefense() <= 0) && !card.hasStartOfKeyword("etbCounter")
                && mana.countX() == 0 && !card.hasETBTrigger()
                && !card.hasETBReplacement()) {
            return false;
        }

        if (!SpellPermanent.checkETBEffects(card, this, null, ai)) {
            return false;
        }
        return super.canPlayAI();
    }

    public static boolean checkETBEffects(final Card card, final AIPlayer ai) {
        return checkETBEffects(card, null, null, ai);
    }

    public static boolean checkETBEffects(final Card card, final SpellAbility sa, final ApiType api, final AIPlayer ai) {
        boolean rightapi = false;
        final GameState game = ai.getGame();

        if (card.isCreature()
                && ai.getGame().getStaticEffects().getGlobalRuleChange(GlobalRuleChange.noCreatureETBTriggers)) {
            return api == null;
        }

        // Trigger play improvements
        for (final Trigger tr : card.getTriggers()) {
            // These triggers all care for ETB effects

            final HashMap<String, String> params = tr.getMapParams();
            if (tr.getMode() != TriggerType.ChangesZone) {
                continue;
            }

            if (!params.get("Destination").equals(ZoneType.Battlefield.toString())) {
                continue;
            }

            if (params.containsKey("ValidCard")) {
                if (!params.get("ValidCard").contains("Self")) {
                    continue;
                }
                if (params.get("ValidCard").contains("notkicked")) {
                    if (sa.isKicked()) {
                        continue;
                    }
                } else if (params.get("ValidCard").contains("kicked")) {
                    if (params.get("ValidCard").contains("kicked ")) { // want a specific kicker
                        String s = params.get("ValidCard").split("kicked ")[1];
                        if ( "1".equals(s) && !sa.isOptionalCostPaid(OptionalCost.Kicker1)) continue;
                        if ( "2".equals(s) && !sa.isOptionalCostPaid(OptionalCost.Kicker2)) continue;
                    } else if (!sa.isKicked()) { 
                        continue;
                    }
                }
            }

            if (!tr.requirementsCheck(game)) {
                continue;
            }

            if (tr.getOverridingAbility() != null) {
                // Abilities yet
                continue;
            }

            // Maybe better considerations
            final String execute = params.get("Execute");
            if (execute == null) {
                continue;
            }
            final SpellAbility exSA = AbilityFactory.getAbility(card.getSVar(execute), card);

            if (api != null) {
                if (exSA.getApi() != api) {
                    continue;
                } else {
                    rightapi = true;
                }
            }

            if (sa != null) {
                exSA.setActivatingPlayer(sa.getActivatingPlayer());
            }
            else if (ai != null) {
                exSA.setActivatingPlayer(ai);
            }
            else {
                throw new InvalidParameterException("Either ai or sa must be not null!");
            }
            exSA.setTrigger(true);

            // Run non-mandatory trigger.
            // These checks only work if the Executing SpellAbility is an
            // Ability_Sub.
            if ((exSA instanceof AbilitySub) && !exSA.doTrigger(false, ai)) {
                // AI would not run this trigger if given the chance

                // if trigger is mandatory, return false
                if (params.get("OptionalDecider") == null) {
                    return false;
                }
                // else
                // otherwise, return false 50% of the time?
            }
        }
        if (api != null && !rightapi) {
            return false;
        }

        // Replacement effects
        for (final ReplacementEffect re : card.getReplacementEffects()) {
            // These Replacements all care for ETB effects

            final Map<String, String> params = re.getMapParams();
            if (!(re instanceof ReplaceMoved)) {
                continue;
            }

            if (!params.get("Destination").equals(ZoneType.Battlefield.toString())) {
                continue;
            }

            if (params.containsKey("ValidCard")) {
                if (!params.get("ValidCard").contains("Self")) {
                    continue;
                }
                if (params.get("ValidCard").contains("notkicked")) {
                    if (sa.isKicked()) {
                        continue;
                    }
                } else if (params.get("ValidCard").contains("kicked")) {
                    if (params.get("ValidCard").contains("kicked ")) { // want a specific kicker
                        String s = params.get("ValidCard").split("kicked ")[1];
                        if ( "1".equals(s) && !sa.isOptionalCostPaid(OptionalCost.Kicker1)) continue;
                        if ( "2".equals(s) && !sa.isOptionalCostPaid(OptionalCost.Kicker2)) continue;
                    } else if (!sa.isKicked()) { // otherwise just any must be present
                        continue;
                    }
                }
            }

            if (!re.requirementsCheck(game)) {
                continue;
            }
            final SpellAbility exSA = re.getOverridingAbility();

            if (exSA != null) {
                if (sa != null) {
                    exSA.setActivatingPlayer(sa.getActivatingPlayer());
                }
                else if (ai != null) {
                    exSA.setActivatingPlayer(ai);
                }
                else {
                    throw new InvalidParameterException("Either ai or sa must be not null!");
                }

                if (exSA.getActivatingPlayer() == null) {
                    throw new InvalidParameterException("Executing SpellAbility for Replacement Effect has no activating player");
                }
            }

            // ETBReplacement uses overriding abilities.
            // These checks only work if the Executing SpellAbility is an
            // Ability_Sub.
            if (exSA != null && (exSA instanceof AbilitySub) && !exSA.doTrigger(false, ai)) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void resolve() {
        Card c = this.getSourceCard();
        c.setController(this.getActivatingPlayer(), 0);
        this.getActivatingPlayer().getGame().getAction().moveTo(ZoneType.Battlefield, c);
    }
}
