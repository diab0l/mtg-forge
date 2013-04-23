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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Constant;
import forge.Singletons;
import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.deck.generate.GenerateDeckUtil.FilterCMC;
import forge.game.player.PlayerType;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.properties.ForgePreferences.FPref;
import forge.util.Aggregates;
import forge.util.MyRandom;

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
    protected int maxDuplicates;

    protected ColorSet colors;
    protected final ItemPool<CardPrinted> tDeck;

    // 2-colored deck generator has its own constants. The rest works fine with these ones
    protected float getLandsPercentage() { return 0.44f; }
    protected float getCreatPercentage() { return 0.34f; }
    protected float getSpellPercentage() { return 0.22f; }

    StringBuilder tmpDeck = new StringBuilder();

    public GenerateColoredDeckBase() {
        this.maxDuplicates = Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_SINGLETONS) ? 1 : 4;
        tDeck = new ItemPool<CardPrinted>(CardPrinted.class);
    }

    protected void addCreaturesAndSpells(int size, List<ImmutablePair<FilterCMC, Integer>> cmcLevels, PlayerType pt) {
        tmpDeck.append("Building deck of ").append(size).append("cards\n");
        
        final Iterable<CardPrinted> cards = selectCardsOfMatchingColorForPlayer(pt);
        // build subsets based on type

        final Iterable<CardPrinted> creatures = Iterables.filter(cards, Predicates.compose(CardRulesPredicates.Presets.IS_CREATURE, CardPrinted.FN_GET_RULES));
        final int creatCnt = (int) Math.ceil(getCreatPercentage() * size);
        tmpDeck.append("Creatures to add:").append(creatCnt).append("\n");
        addCmcAdjusted(creatures, creatCnt, cmcLevels);

        Predicate<CardPrinted> preSpells = Predicates.compose(CardRulesPredicates.Presets.IS_NONCREATURE_SPELL_FOR_GENERATOR, CardPrinted.FN_GET_RULES);
        final Iterable<CardPrinted> spells = Iterables.filter(cards, preSpells);
        final int spellCnt = (int) Math.ceil(getSpellPercentage() * size);
        tmpDeck.append("Spells to add:").append(spellCnt).append("\n");
        addCmcAdjusted(spells, spellCnt, cmcLevels);
        
        tmpDeck.append(String.format("Current deck size: %d... should be %f%n", tDeck.countAll(), size * (getCreatPercentage() + getSpellPercentage())));
    }

    public ItemPoolView<CardPrinted> getDeck(final int size, final PlayerType pt) {
        return null; // all but theme deck do override this method
    }

    protected void addSome(int cnt, List<CardPrinted> source) {
        for (int i = 0; i < cnt; i++) {
            CardPrinted cp;
            int lc = 0;
            int srcLen = source.size();
            do {
                cp = source.get(this.r.nextInt(srcLen));
                lc++;
            } while (this.cardCounts.get(cp.getName()) > this.maxDuplicates - 1 && lc <= 100);

            if (lc > 100) {
                throw new RuntimeException("Generate2ColorDeck : get2ColorDeck -- looped too much -- Cr12");
            }

            tDeck.add(cp);
            final int n = this.cardCounts.get(cp.getName());
            this.cardCounts.put(cp.getName(), n + 1);
            if( n + 1 == this.maxDuplicates )
                source.remove(cp);
            tmpDeck.append(String.format("(%d) %s [%s]%n", cp.getRules().getManaCost().getCMC(), cp.getName(), cp.getRules().getManaCost()));
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
            } while ((this.cardCounts.get(s) > 3) && (lc <= 20));
            // not an error if looped too much - could play singleton mode, with 6 slots for 3 non-basic lands.

            CardPrinted cp = CardDb.instance().getCard(s);
            tDeck.add(CardDb.instance().getCard(cp.getName(), Aggregates.random(cp.getRules().getSets())));

            final int n = this.cardCounts.get(s);
            this.cardCounts.put(s, n + 1);
            tmpDeck.append(s + "\n");
            res++;
        }
        return res;
    }

    protected void addBasicLand(int cnt) {
        tmpDeck.append(cnt).append(" basic lands remain").append("\n");
        
        // attempt to optimize basic land counts according to colors of picked cards
        final Map<String, Integer> clrCnts = countLands(tDeck);
        // total of all ClrCnts
        float totalColor = 0;
        for (Entry<String, Integer> c : clrCnts.entrySet()) {
            totalColor += c.getValue();
            tmpDeck.append(c.getKey()).append(":").append(c.getValue()).append("\n");
        }

        tmpDeck.append("totalColor:").append(totalColor).append("\n");

        int landsLeft = cnt;
        for (Entry<String, Integer> c : clrCnts.entrySet()) {
            String color = c.getKey();


            // calculate number of lands for each color
            final int nLand = Math.min(landsLeft, Math.round(cnt * c.getValue() / totalColor));
            tmpDeck.append("nLand-").append(color).append(":").append(nLand).append("\n");

            // just to prevent a null exception by the deck size fixing code
            this.cardCounts.put(color, nLand);

            CardPrinted cp = CardDb.instance().getCard(color);
            String basicLandSet = Aggregates.random(cp.getRules().getSets());

            tDeck.add(CardDb.instance().getCard(cp.getName(), basicLandSet), nLand);
            landsLeft -= nLand;
        }
    }

    protected void adjustDeckSize(int targetSize) {
        // fix under-sized or over-sized decks, due to integer arithmetic
        int actualSize = tDeck.countAll();
        if (actualSize < targetSize) {
            final int diff = targetSize - actualSize;
            addSome(diff, tDeck.toFlatList());
        } else if (actualSize > targetSize) {

            Predicate<CardPrinted> exceptBasicLand = Predicates.not(Predicates.compose(CardRulesPredicates.Presets.IS_BASIC_LAND, CardPrinted.FN_GET_RULES));

            for (int i = 0; i < 3 && actualSize > targetSize; i++) {
                Iterable<CardPrinted> matchingCards = Iterables.filter(tDeck.toFlatList(), exceptBasicLand);
                List<CardPrinted> toRemove = Aggregates.random(matchingCards,  actualSize - targetSize);
                tDeck.removeAllFlat(toRemove);

                for (CardPrinted c : toRemove) {
                    tmpDeck.append("Removed:").append(c.getName()).append("\n");
                }
                actualSize = tDeck.countAll();
            }
        }
    }

    protected void addCmcAdjusted(Iterable<CardPrinted> source, int cnt, List<ImmutablePair<FilterCMC, Integer>> cmcLevels) {
        int totalWeight = 0;
        for (ImmutablePair<FilterCMC, Integer> pair : cmcLevels) {
            totalWeight += pair.getRight();
        }
        
        float variability = 0.6f; // if set to 1, you'll get minimum cards to choose from
        float desiredWeight = (float)cnt / ( maxDuplicates * variability ); 
        float desiredOverTotal = desiredWeight / totalWeight;
        float requestedOverTotal = (float)cnt / totalWeight;
        
        for (ImmutablePair<FilterCMC, Integer> pair : cmcLevels) {
            Iterable<CardPrinted> matchingCards = Iterables.filter(source, Predicates.compose(pair.getLeft(), CardPrinted.FN_GET_RULES));
            int cmcCountForPool = (int) Math.ceil(pair.getRight().intValue() * desiredOverTotal);
            
            int addOfThisCmc = Math.round(pair.getRight().intValue() * requestedOverTotal);
            tmpDeck.append(String.format("Adding %d cards for cmc range from a pool with %d cards:%n", addOfThisCmc, cmcCountForPool));

            final List<CardPrinted> curved = Aggregates.random(matchingCards, cmcCountForPool);
            final List<CardPrinted> curvedRandomized = Lists.newArrayList();
            for (CardPrinted c : curved) {
                this.cardCounts.put(c.getName(), 0);
                CardPrinted cpRandomSet = CardDb.instance().getCard(c.getName(), Aggregates.random(c.getRules().getSets()));
                curvedRandomized.add(cpRandomSet);
            }

            addSome(addOfThisCmc, curvedRandomized);
        }
    }

    protected Iterable<CardPrinted> selectCardsOfMatchingColorForPlayer(PlayerType pt) {

        // start with all cards
        // remove cards that generated decks don't like
        Predicate<CardRules> canPlay = pt == PlayerType.HUMAN ? GenerateDeckUtil.HUMAN_CAN_PLAY : GenerateDeckUtil.AI_CAN_PLAY;
        Predicate<CardRules> hasColor = new GenerateDeckUtil.CanBePaidWithColors(colors);

        if (!Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_ARTIFACTS)) {
            hasColor = Predicates.or(hasColor, GenerateDeckUtil.COLORLESS_CARDS);
        }
        return Iterables.filter(CardDb.instance().getAllCards(), Predicates.compose(Predicates.and(canPlay, hasColor), CardPrinted.FN_GET_RULES));
    }

    protected static Map<String, Integer> countLands(ItemPool<CardPrinted> outList) {
        // attempt to optimize basic land counts according
        // to color representation

        Map<String, Integer> res = new TreeMap<String, Integer>();
        // count each card color using mana costs
        // TODO: count hybrid mana differently?
        for (Entry<CardPrinted, Integer> cpe : outList) {

            int profile = cpe.getKey().getRules().getManaCost().getColorProfile();

            if ((profile & MagicColor.WHITE) != 0) {
                increment(res, Constant.Color.BASIC_LANDS.get(0), cpe.getValue());
            } else if ((profile & MagicColor.BLUE) != 0) {
                increment(res, Constant.Color.BASIC_LANDS.get(1), cpe.getValue());
            } else if ((profile & MagicColor.BLACK) != 0) {
                increment(res, Constant.Color.BASIC_LANDS.get(2), cpe.getValue());
            } else if ((profile & MagicColor.RED) != 0) {
                increment(res, Constant.Color.BASIC_LANDS.get(3), cpe.getValue());
            } else if ((profile & MagicColor.GREEN) != 0) {
                increment(res, Constant.Color.BASIC_LANDS.get(4), cpe.getValue());
            }

        }
        return res;
    }

    protected static void increment(Map<String, Integer> map, String key, int delta)
    {
        final Integer boxed = map.get(key);
        map.put(key, boxed == null ? delta : boxed.intValue() + delta);
    }
}
