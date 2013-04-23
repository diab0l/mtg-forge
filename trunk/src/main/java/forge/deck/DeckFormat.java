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
package forge.deck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.IntRange;

import forge.Constant;
import forge.Singletons;
import forge.card.CardCoreType;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.IPaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.util.Aggregates;

/**
 * GameType is an enum to determine the type of current game. :)
 */
public enum DeckFormat {

    //            Main board: allowed size             SB: restriction   Max distinct non basic cards
    Constructed ( new IntRange(60, Integer.MAX_VALUE), new IntRange(15), 4),
    QuestDeck   ( new IntRange(40, Integer.MAX_VALUE), new IntRange(15), 4),
    Limited     ( new IntRange(40, Integer.MAX_VALUE), null,             Integer.MAX_VALUE),
    Commander   ( new IntRange(99),                    new IntRange(10), 1),
    Vanguard    ( new IntRange(60, Integer.MAX_VALUE), new IntRange(0),  4),
    Planechase  ( new IntRange(60, Integer.MAX_VALUE), new IntRange(0),  4),
    Archenemy   ( new IntRange(60, Integer.MAX_VALUE), new IntRange(0),  4);

    private final IntRange mainRange;
    private final IntRange sideRange; // null => no check
    private final int maxCardCopies;


    /**
     * Instantiates a new game type.
     * 
     * @param isLimited
     *            the is limited
     */
    DeckFormat(IntRange main, IntRange side, int maxCopies) {
        mainRange = main;
        sideRange = side;
        maxCardCopies = maxCopies;
    }

    /**
     * Smart value of.
     *
     * @param value the value
     * @param defaultValue the default value
     * @return the game type
     */
    public static DeckFormat smartValueOf(final String value, DeckFormat defaultValue) {
        if (null == value) {
            return defaultValue;
        }

        final String valToCompate = value.trim();
        for (final DeckFormat v : DeckFormat.values()) {
            if (v.name().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }

        throw new IllegalArgumentException("No element named " + value + " in enum GameType");
    }


    /**
     * @return the sideRange
     */
    public IntRange getSideRange() {
        return sideRange;
    }


    /**
     * @return the mainRange
     */
    public IntRange getMainRange() {
        return mainRange;
    }


    /**
     * @return the maxCardCopies
     */
    public int getMaxCardCopies() {
        return maxCardCopies;
    }



    @SuppressWarnings("incomplete-switch")
    public String getDeckConformanceProblem(Deck deck) {
        // That's really a bad dependence
        if (!Singletons.getModel().getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY)) {
            return null;
        }

        int deckSize = deck.getMain().countAll();

        int min = getMainRange().getMinimumInteger();
        int max = getMainRange().getMaximumInteger();

        if (deckSize < min) {
            return String.format("should have a minimum of %d cards", min);
        }

        if (deckSize > max) {
            return String.format("should not exceed a maximum of %d cards", max);
        }

        switch(this) {
            case Commander: //Must contain exactly 1 legendary Commander and a sideboard of 10 or zero cards.

                final CardPool cmd = deck.get(DeckSection.Commander);
                if (null == cmd || cmd.isEmpty()) {
                    return "is missing a commander";
                }
                if (!cmd.get(0).getRules().getType().isLegendary()
                    || !cmd.get(0).getRules().getType().isCreature()) {
                    return "has a commander that is not a legendary creature";
                }
                
                ColorSet cmdCI = cmd.get(0).getRules().getColorIdentity();
                List<CardPrinted> erroneousCI = new ArrayList<CardPrinted>();
                                
                for(Entry<CardPrinted, Integer> cp : deck.get(DeckSection.Main)) {
                    if(!cp.getKey().getRules().getColorIdentity().hasNoColorsExcept(cmdCI.getColor()))
                    {
                        erroneousCI.add(cp.getKey());
                    }
                    if(cp.getKey().getRules().getType().isLand())
                    {
                        for(String key : Constant.Color.COLOR_TO_BASIC_LAND_TYPE_MAP.keySet())
                        {
                            if(!cmdCI.hasAnyColor(MagicColor.fromName(key)))
                            {
                                if(cp.getKey().getRules().getType().subTypeContains(Constant.Color.COLOR_TO_BASIC_LAND_TYPE_MAP.get(key)))
                                {
                                    erroneousCI.add(cp.getKey());
                                }
                            }
                        }
                    }
                }
                for(Entry<CardPrinted, Integer> cp : deck.get(DeckSection.Sideboard)) {
                    if(!cp.getKey().getRules().getColorIdentity().hasNoColorsExcept(cmdCI.getColor()))
                    {
                        erroneousCI.add(cp.getKey());
                    }
                    if(cp.getKey().getRules().getType().isLand())
                    {
                        for(String key : Constant.Color.COLOR_TO_BASIC_LAND_TYPE_MAP.keySet())
                        {
                            if(!cmdCI.hasAnyColor(MagicColor.fromName(key)))
                            {
                                if(cp.getKey().getRules().getType().subTypeContains(Constant.Color.COLOR_TO_BASIC_LAND_TYPE_MAP.get(key)))
                                {
                                    erroneousCI.add(cp.getKey());
                                }
                            }
                        }
                    }
                }
                
                if(erroneousCI.size() > 0)
                {
                    StringBuilder sb = new StringBuilder("contains card that do not match the commanders color identity:");
                    
                    for(CardPrinted cp : erroneousCI)
                    {
                        sb.append("\n").append(cp.getName());
                    }
                    
                    return sb.toString();
                }
                
                break;

            case Planechase: //Must contain at least 10 planes/phenomenons, but max 2 phenomenons. Singleton.
                final CardPool planes = deck.get(DeckSection.Planes);
                if (planes == null || planes.countAll() < 10) {
                    return "should have at least 10 planes";
                }
                int phenoms = 0;
                for (Entry<CardPrinted, Integer> cp : planes) {

                    if (cp.getKey().getRules().getType().typeContains(CardCoreType.Phenomenon)) {
                        phenoms++;
                    }
                    if (cp.getValue() > 1) {
                        return "must not contain multiple copies of any Plane or Phenomena";
                    }

                }
                if (phenoms > 2) {
                    return "must not contain more than 2 Phenomena";
                }
                break;

            case Archenemy:  //Must contain at least 20 schemes, max 2 of each.
                final CardPool schemes = deck.get(DeckSection.Schemes);
                if (schemes == null || schemes.countAll() < 20) {
                    return "must contain at least 20 schemes";
                }

                for (Entry<CardPrinted, Integer> cp : schemes) {
                    if (cp.getValue() > 2) {
                        return String.format("must not contain more than 2 copies of any Scheme, but has %d of '%s'", cp.getValue(), cp.getKey().getName());
                    }
                }
                break;
        }

        int maxCopies = getMaxCardCopies();
        if (maxCopies < Integer.MAX_VALUE) {
            //Must contain no more than 4 of the same card
            //shared among the main deck and sideboard, except
            //basic lands and Relentless Rats

            CardPool tmp = new CardPool(deck.getMain());
            if ( deck.has(DeckSection.Sideboard))
                tmp.addAll(deck.get(DeckSection.Sideboard));
            if ( deck.has(DeckSection.Commander) && this == Commander)
                tmp.addAll(deck.get(DeckSection.Commander));

            List<String> limitExceptions = Arrays.asList("Relentless Rats");

            // should group all cards by name, so that different editions of same card are really counted as the same card
            for (Entry<String, Integer> cp : Aggregates.groupSumBy(tmp, CardPrinted.FN_GET_NAME)) {

                IPaperCard simpleCard = CardDb.instance().getCard(cp.getKey());
                boolean canHaveMultiple = simpleCard.getRules().getType().isBasicLand() || limitExceptions.contains(cp.getKey());

                if (!canHaveMultiple && cp.getValue() > maxCopies) {
                    return String.format("must not contain more than %d of '%s' card", maxCopies, cp.getKey());
                }
            }
        }

        // The sideboard must contain either 0 or 15 cards
        int sideboardSize = deck.has(DeckSection.Sideboard) ? deck.get(DeckSection.Sideboard).countAll() : 0;
        IntRange sbRange = getSideRange();
        if (sbRange != null && sideboardSize > 0 && !sbRange.containsInteger(sideboardSize)) {
            return sbRange.getMinimumInteger() == sbRange.getMaximumInteger()
            ? String.format("must have a sideboard of %d cards or no sideboard at all", sbRange.getMaximumInteger())
            : String.format("must have a sideboard of %d to %d cards or no sideboard at all", sbRange.getMinimumInteger(), sbRange.getMaximumInteger());
        }

        return null;
    }
}
