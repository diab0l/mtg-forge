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
package forge.deck.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import forge.Constant;
import forge.Singletons;
import forge.card.CardColor;
import forge.card.CardRules;
import forge.deck.generate.GenerateDeckUtil.FilterCMC;
import forge.game.player.PlayerType;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.ItemPool;
import forge.properties.ForgePreferences.FPref;
import forge.util.MyRandom;
import forge.util.closures.Predicate;

/**
 * <p>
 * Generate2ColorDeck class.
 * </p>
 * 
 * @author Forge
 * @version $Id: Generate2ColorDeck.java 14959 2012-03-28 14:03:43Z Chris H. $
 */
public abstract class GenerateColoredDeckBase {
    protected final Random r = MyRandom.getRandom();
    protected final Map<String, Integer> cardCounts = new HashMap<String, Integer>();
    protected final int maxDuplicates;

    protected CardColor colors;
    protected final ItemPool<CardPrinted> tDeck;

    StringBuilder tmpDeck = new StringBuilder();

//    protected final float landsPercentage = 0.42f;
//    protected float creatPercentage = 0.34f;
//    protected float spellPercentage = 0.24f;
    /**
     * <p>
     * Constructor for Generate2ColorDeck.
     * </p>
     * 
     * @param clr1
     *            a {@link java.lang.String} object.
     * @param clr2
     *            a {@link java.lang.String} object.
     */
    public GenerateColoredDeckBase() {
        this.maxDuplicates = Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_SINGLETONS) ? 1 : 4;
        tDeck = new ItemPool<CardPrinted>(CardPrinted.class);
    }



    protected void addSome(int cnt, List<CardPrinted> source) {
        for (int i = 0; i < cnt; i++) {
            CardPrinted c;
            int lc = 0;
            do {
                c = source.get(this.r.nextInt(source.size()));
                lc++;
            } while ((this.cardCounts.get(c.getName()) > (this.maxDuplicates - 1)) || (lc > 100));

            if (lc > 100) {
                throw new RuntimeException("Generate2ColorDeck : get2ColorDeck -- looped too much -- Cr12");
            }

            tDeck.add(c);
            final int n = this.cardCounts.get(c.getName());
            this.cardCounts.put(c.getName(), n + 1);
            tmpDeck.append(c.getName() + " " + c.getCard().getManaCost() + "\n");
        }
    }

    protected int addSomeStr(int cnt, List<String> source) {
        int res = 0;
        for (int i = 0; i < cnt; i++) {
            String s;
            int lc = 0;
            do {
                s = source.get(this.r.nextInt(source.size()));
                lc++;
            } while ((this.cardCounts.get(s) > (this.maxDuplicates - 1)) || (lc > 20));
            // not an error if looped too much - could play singleton mode, with 6 slots for 3 non-basic lands.

            tDeck.add(CardDb.instance().getCard(s));
            final int n = this.cardCounts.get(s);
            this.cardCounts.put(s, n + 1);
            tmpDeck.append(s + "\n");
        }
        return res;
    }

    protected void addBasicLand(int cnt) {
        // attempt to optimize basic land counts according to colors of picked cards
        final CCnt[] clrCnts = countLands(tDeck);
        // total of all ClrCnts
        int totalColor = 0;
        for (int i = 0; i < 5; i++) {
            totalColor += clrCnts[i].getCount();
            tmpDeck.append(clrCnts[i].Color).append(":").append(clrCnts[i].getCount()).append("\n");
        }

        tmpDeck.append("totalColor:").append(totalColor).append("\n");

        for (int i = 0; i < 5; i++) {
            if (clrCnts[i].getCount() <= 0) {
                continue;
            }

            // calculate number of lands for each color
            float p = (float) clrCnts[i].getCount() / (float) totalColor;
            final int nLand = (int) (cnt * p);
            tmpDeck.append("nLand-").append(clrCnts[i].Color).append(":").append(nLand).append("\n");

            // just to prevent a null exception by the deck size fixing
            // code
            this.cardCounts.put(clrCnts[i].Color, nLand);

            for (int j = 0; j <= nLand; j++) {
                tDeck.add(CardDb.instance().getCard(clrCnts[i].Color));
            }
        }
    }

    protected void adjustDeckSize(int targetSize) {
        // fix under-sized or over-sized decks, due to integer arithmetic
        int actualSize = tDeck.countAll();
        if (actualSize < targetSize) {
            final int diff = targetSize - actualSize;
            addSome(diff, tDeck.toFlatList());
        } else if (actualSize > targetSize) {

            Predicate<CardRules> exceptBasicLand = Predicate.not(CardRules.Predicates.Presets.IS_BASIC_LAND);

            for (int i = 0; i < 3 && actualSize > targetSize; i++) {
                List<CardPrinted> toRemove = exceptBasicLand.random(tDeck.toFlatList(), CardPrinted.FN_GET_RULES, actualSize - targetSize);
                tDeck.removeAllFlat(toRemove);

                for (CardPrinted c : toRemove) {
                    tmpDeck.append("Removed:").append(c.getName()).append("\n");
                }
                actualSize = tDeck.countAll();
            }
        }
    }

    protected void addCmcAdjusted(List<CardPrinted> source, int cnt, List<FilterCMC> cmcLevels, int[] cmcAmounts) {
        final List<CardPrinted> curved = new ArrayList<CardPrinted>();

        for (int i = 0; i < cmcAmounts.length; i++) {
            curved.addAll(cmcLevels.get(i).random(source, CardPrinted.FN_GET_RULES, cmcAmounts[i]));
        }

        for (CardPrinted c : curved) {
            this.cardCounts.put(c.getName(), 0);
        }

        addSome(cnt, curved);
    }

    protected List<CardPrinted> selectCardsOfMatchingColorForPlayer(PlayerType pt) {

        // start with all cards
        // remove cards that generated decks don't like
        Predicate<CardRules> canPlay = pt == PlayerType.HUMAN ? GenerateDeckUtil.humanCanPlay : GenerateDeckUtil.aiCanPlay;
        Predicate<CardRules> hasColor = new GenerateDeckUtil.ContainsAllColorsFrom(colors);

        if (!Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_ARTIFACTS)) {
            hasColor = Predicate.or(hasColor, GenerateDeckUtil.colorlessCards);
        }

        return Predicate.and(canPlay, hasColor).select(CardDb.instance().getAllCards(), CardPrinted.FN_GET_RULES);

    }

    protected static CCnt[] countLands(ItemPool<CardPrinted> outList) {
        // attempt to optimize basic land counts according
        // to color representation

        String[] bl = Constant.Color.BASIC_LANDS;

        final CCnt[] clrCnts = { new CCnt(bl[0], 0), new CCnt(bl[1], 0), new CCnt(bl[2], 0),
                new CCnt(bl[3], 0), new CCnt(bl[4], 0) };

        // count each card color using mana costs
        // TODO: count hybrid mana differently?
        for (Entry<CardPrinted, Integer> cpe : outList) {

            int profile = cpe.getKey().getCard().getManaCost().getColorProfile();

            if ((profile & CardColor.WHITE) != 0) {
                clrCnts[0].increment(cpe.getValue());
            } else if ((profile & CardColor.BLUE) != 0) {
                clrCnts[1].increment(cpe.getValue());
            } else if ((profile & CardColor.BLACK) != 0) {
                clrCnts[2].increment(cpe.getValue());
            } else if ((profile & CardColor.RED) != 0) {
                clrCnts[3].increment(cpe.getValue());
            } else if ((profile & CardColor.GREEN) != 0) {
                clrCnts[4].increment(cpe.getValue());
            }

        }
        return clrCnts;
    }
}
