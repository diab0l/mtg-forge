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
import forge.Counters;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.control.input.Input;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiUtils;

/**
 * The Class CostUtil.
 */
public class CostUtil {
    private static Random r = new Random();
    
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
        return checkSacrificeCost(cost, source, true);
    }

    /**
     * Check sacrifice cost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @param important
     *            is the gain important enough?
     * @return true, if successful
     */
    public static boolean checkSacrificeCost(final Cost cost, final Card source, final boolean important) {
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostSacrifice) {
                final CostSacrifice sac = (CostSacrifice) part;

                final String type = sac.getType();

                if (type.equals("CARDNAME")) {
                    if (!important) {
                        return false;
                    }
                    CardList auras = new CardList(source.getEnchantedBy());
                    if (!auras.getController(source.getController()).isEmpty()) {
                        return false;
                    }
                    continue;
                }

                CardList typeList = AllZone.getComputerPlayer().getCardsIn(ZoneType.Battlefield);
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
        if (cost == null) {
            return true;
        }
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

                CardList typeList = AllZone.getComputerPlayer().getCardsIn(ZoneType.Battlefield);
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
     * @param sourceAbility TODO
     * @return true, if successful
     */
    public static boolean checkLifeCost(final Cost cost, final Card source, final int remainingLife, SpellAbility sourceAbility) {
        // TODO - Pass in SA for everything else that calls this function
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostPayLife) {
                final CostPayLife payLife = (CostPayLife) part;

                Integer amount = payLife.convertAmount();
                if (amount == null) {
                    amount = AbilityFactory.calculateAmount(source, payLife.getAmount(), sourceAbility);
                }

                if ((AllZone.getComputerPlayer().getLife() - amount) < remainingLife) {
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
    public static boolean checkDamageCost(final Cost cost, final Card source, final int remainingLife) {
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostDamage) {
                final CostDamage pay = (CostDamage) part;
                Player computer = AllZone.getComputerPlayer();
                int realDamage = computer.predictDamage(pay.convertAmount(), source, false);
                if (computer.getLife() - realDamage < remainingLife
                        && realDamage > 0 && !computer.cantLoseForZeroOrLessLife()
                        && computer.canLoseLife()) {
                    return false;
                }
                if (source.getName().equals("Skullscorch") && computer.getCardsIn(ZoneType.Hand).size() < 2) {
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
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostDiscard) {
                final CostDiscard disc = (CostDiscard) part;

                final String type = disc.getType();
                CardList typeList = AllZone.getComputerPlayer().getCardsIn(ZoneType.Hand);
                if (typeList.size() > AllZone.getComputerPlayer().getMaxHandSize()) {
                    continue;
                }
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
        if (cost == null) {
            return true;
        }
        double p1p1Percent = .25;
        if (source.isCreature()) {
            p1p1Percent = .1;
        }
        final double otherPercent = .9;
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostRemoveCounter) {
                final CostRemoveCounter remCounter = (CostRemoveCounter) part;

                // A card has a 25% chance per counter to be able to pass
                // through here
                // 4+ counters will always pass. 0 counters will never
                final Counters type = remCounter.getCounter();
                final double percent = type.name().equals("P1P1") ? p1p1Percent : otherPercent;
                final int currentNum = source.getCounters(type);

                Integer amount = part.convertAmount();
                if (amount == null) {
                    amount = currentNum;
                }
                final double chance = percent * (currentNum / amount);
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
        if (cost == null) {
            return true;
        }
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
     * hasUntapCost.
     * 
     * @param cost
     *            the cost
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean hasUntapCost(final Cost cost, final Card source) {
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostUntapType) {
                return true;
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
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = CostUtil.chooseXValue(source, ability, maxChoice);
            } if (sVar.equals("YChoice")) {
                c = CostUtil.chooseYValue(source, ability, maxChoice);
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
        final Integer chosenX = GuiUtils.chooseOne(card.toString() + " - Choose a Value for X", choiceArray);
        sa.setSVar("ChosenX", "Number$" + Integer.toString(chosenX));
        card.setSVar("ChosenX", "Number$" + Integer.toString(chosenX));

        return chosenX;
    }

    /**
     * Choose x value (for ChosenY).
     * 
     * @param card
     *            the card
     * @param sa
     *            the SpellAbility
     * @param maxValue
     *            the max value
     * @return the int
     */
    public static int chooseYValue(final Card card, final SpellAbility sa, final int maxValue) {
        /*final String chosen = sa.getSVar("ChosenY");
        if (chosen.length() > 0) {
            return AbilityFactory.calculateAmount(card, "ChosenY", null);
        }*/

        final Integer[] choiceArray = new Integer[maxValue + 1];
        for (int i = 0; i < choiceArray.length; i++) {
            choiceArray[i] = Integer.valueOf(i);
        }
        final Integer chosenY = GuiUtils.chooseOne(card.toString() + " - Choose a Value for Y", choiceArray);
        sa.setSVar("ChosenY", "Number$" + Integer.toString(chosenY));
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
