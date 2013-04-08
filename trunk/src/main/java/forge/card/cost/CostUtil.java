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
package forge.card.cost;

import forge.Card;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.spellability.SpellAbility;
import forge.gui.GuiChoose;

/**
 * The Class CostUtil.
 */
public class CostUtil {
    /**
     * Checks for discard hand cost.
     * 
     * @param cost
     *            the cost
     * @return true, if successful
     */
    public static boolean hasDiscardHandCost(final Cost cost) {
        if (cost == null) {
            return false;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostDiscard) {
                final CostDiscard disc = (CostDiscard) part;
                if (disc.getType().equals("Hand")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * hasTapCost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean hasTapCost(final Cost cost, final Card source) {
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostTapType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Choose x value.
     * 
     * @param card
     *            the card
     * @param sa
     *            the SpellAbility
     * @param maxValue
     *            the max value
     * @return the int
     */
    public static int chooseXValue(final Card card, final SpellAbility sa, final int maxValue) {
        /*final String chosen = sa.getSVar("ChosenX");
        if (chosen.length() > 0) {
            return AbilityFactory.calculateAmount(card, "ChosenX", null);
        }*/

        final Integer[] choiceArray = new Integer[maxValue + 1];
        for (int i = 0; i < choiceArray.length; i++) {
            choiceArray[i] = i;
        }
        final Integer chosenX = GuiChoose.one(card.toString() + " - Choose a Value for X", choiceArray);
        sa.setSVar("ChosenX", Integer.toString(chosenX));
        card.setSVar("ChosenX", Integer.toString(chosenX));

        return chosenX;
    }


    public static Cost combineCosts(Cost cost1, Cost cost2) {
        if (cost1 == null) return cost2;
        if (cost2 == null) return cost1;

        CostPartMana costPart2 = cost2.getCostMana();
        for (final CostPart part : cost1.getCostParts()) {
            if (part instanceof CostPartMana && costPart2 != null) {
                ManaCostBeingPaid oldManaCost = new ManaCostBeingPaid(((CostPartMana) part).getMana());
                boolean xCanBe0 = ((CostPartMana) part).canXbe0() && costPart2.canXbe0();
                oldManaCost.combineManaCost(costPart2.getMana());
                String r2 = costPart2.getRestiction();
                String r1 = ((CostPartMana) part).getRestiction();
                String r = r1 == null ? r2 : ( r2 == null ? r1 : r1+"."+r2);
                cost2.getCostParts().remove(costPart2);
                cost2.getCostParts().add(0, new CostPartMana(oldManaCost.toManaCost(), r, !xCanBe0));
            } else { 
                cost2.getCostParts().add(part);
            }
        }
        return cost2;
    }
}
