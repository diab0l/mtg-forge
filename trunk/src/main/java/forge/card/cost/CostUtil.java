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

import java.util.Random;

import forge.AllZone;
import forge.Card;
import forge.CardList;
import forge.ComputerUtil;
import forge.Constant.Zone;
import forge.Counters;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.control.input.Input;
import forge.gui.GuiUtils;

/**
 * The Class CostUtil.
 */
public class CostUtil {
    private static Random r = new Random();
    private static double p1p1Percent = .25;
    private static double otherPercent = .9;

    /**
     * Check sacrifice cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean checkSacrificeCost(final Cost cost, final Card source) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostSacrifice) {
                final CostSacrifice sac = (CostSacrifice) part;

                final String type = sac.getType();

                if (type.equals("CARDNAME")) {
                    continue;
                }

                CardList typeList = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
                typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check creature sacrifice cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean checkCreatureSacrificeCost(final Cost cost, final Card source) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostSacrifice) {
                final CostSacrifice sac = (CostSacrifice) part;
                if (sac.getThis() && source.isCreature()) {
                    return false;
                }
                final String type = sac.getType();

                if (type.equals("CARDNAME")) {
                    continue;
                }

                CardList typeList = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
                typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check life cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @param remainingLife
     *            the remaining life
     * @return true, if successful
     */
    public static boolean checkLifeCost(final Cost cost, final Card source, final int remainingLife) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostPayLife) {
                final CostPayLife payLife = (CostPayLife) part;
                if ((AllZone.getComputerPlayer().getLife() - payLife.convertAmount()) < remainingLife) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check discard cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean checkDiscardCost(final Cost cost, final Card source) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostDiscard) {
                final CostDiscard disc = (CostDiscard) part;

                final String type = disc.getType();
                CardList typeList = AllZone.getComputerPlayer().getCardsIn(Zone.Hand);
                typeList = typeList.getValidCards(type.split(","), source.getController(), source);
                if (ComputerUtil.getCardPreference(source, "DiscardCost", typeList) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check remove counter cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean checkRemoveCounterCost(final Cost cost, final Card source) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostRemoveCounter) {
                final CostRemoveCounter remCounter = (CostRemoveCounter) part;

                // A card has a 25% chance per counter to be able to pass
                // through here
                // 4+ counters will always pass. 0 counters will never
                final Counters type = remCounter.getCounter();
                final double percent = type.name().equals("P1P1") ? CostUtil.p1p1Percent : CostUtil.otherPercent;
                final int currentNum = source.getCounters(type);

                Integer amount = part.convertAmount();
                if (amount == null) {
                    amount = currentNum;
                }
                final double chance = percent * (currentNum / part.convertAmount());
                if (chance <= CostUtil.r.nextFloat()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check add m1 m1 counter cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean checkAddM1M1CounterCost(final Cost cost, final Card source) {
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostPutCounter) {
                final CostPutCounter addCounter = (CostPutCounter) part;
                final Counters type = addCounter.getCounter();

                if (type.equals(Counters.M1M1)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks for discard hand cost.
     * 
     * @param cost
     *            the cost
     * @return true, if successful
     */
    public static boolean hasDiscardHandCost(final Cost cost) {
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
     * Determine amount.
     * 
     * @param part
     *            the part
     * @param source
     *            the source
     * @param ability
     *            the ability
     * @param maxChoice
     *            the max choice
     * @return the integer
     */
    public static Integer determineAmount(final CostPart part, final Card source, final SpellAbility ability,
            final int maxChoice) {
        final String amount = part.getAmount();
        Integer c = part.convertAmount();
        if (c == null) {
            final String sVar = source.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = CostUtil.chooseXValue(source, maxChoice);
            } if (sVar.equals("YChoice")) {
                c = CostUtil.chooseYValue(source, maxChoice);
            } else {
                c = AbilityFactory.calculateAmount(source, amount, ability);
            }
        }
        return c;
    }

    /**
     * Choose x value.
     * 
     * @param card
     *            the card
     * @param maxValue
     *            the max value
     * @return the int
     */
    public static int chooseXValue(final Card card, final int maxValue) {
        final String chosen = card.getSVar("ChosenX");
        if (chosen.length() > 0) {
            return AbilityFactory.calculateAmount(card, "ChosenX", null);
        }

        final Integer[] choiceArray = new Integer[maxValue + 1];
        for (int i = 0; i < choiceArray.length; i++) {
            choiceArray[i] = i;
        }
        final Object o = GuiUtils.chooseOne(card.toString() + " - Choose a Value for X", choiceArray);
        final int chosenX = (Integer) o;
        card.setSVar("ChosenX", "Number$" + Integer.toString(chosenX));

        return chosenX;
    }

    /**
     * Choose x value (for ChosenY).
     * 
     * @param card
     *            the card
     * @param maxValue
     *            the max value
     * @return the int
     */
    public static int chooseYValue(final Card card, final int maxValue) {
        final String chosen = card.getSVar("ChosenY");
        if (chosen.length() > 0) {
            return AbilityFactory.calculateAmount(card, "ChosenY", null);
        }

        final Integer[] choiceArray = new Integer[maxValue + 1];
        for (int i = 0; i < choiceArray.length; i++) {
            choiceArray[i] = i;
        }
        final Object o = GuiUtils.chooseOne(card.toString() + " - Choose a Value for Y", choiceArray);
        final int chosenY = (Integer) o;
        card.setSVar("ChosenY", "Number$" + Integer.toString(chosenY));

        return chosenY;
    }

    /**
     * <p>
     * setInput.
     * </p>
     * 
     * @param in
     *            a {@link forge.control.input.Input} object.
     */
    public static void setInput(final Input in) {
        // Just a shortcut..
        AllZone.getInputControl().setInput(in, true);
    }
}
