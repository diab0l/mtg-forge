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

import java.util.Arrays;
import java.util.List;
import forge.card.CardColor;
import forge.card.CardRules;
import forge.deck.generate.GenerateDeckUtil.FilterCMC;
import forge.error.ErrorViewer;
import forge.game.player.PlayerType;
import forge.item.CardPrinted;
import forge.item.ItemPoolView;
import forge.properties.ForgeProps;

/**
 * <p>
 * Generate3ColorDeck class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Generate3ColorDeck extends GenerateColoredDeckBase {
    private final float landsPercentage = .44f;
    private final float creatPercentage = .34f;
    private final float spellPercentage = .22f;

    final List<FilterCMC> cmcLevels = Arrays.asList(
        new GenerateDeckUtil.FilterCMC(0, 2),
        new GenerateDeckUtil.FilterCMC(3, 5),
        new GenerateDeckUtil.FilterCMC(6, 20));
    final int[] cmcAmounts = {12, 9, 3};

    /**
     * <p>
     * Constructor for Generate3ColorDeck.
     * </p>
     * 
     * @param clr1
     *            a {@link java.lang.String} object.
     * @param clr2
     *            a {@link java.lang.String} object.
     * @param clr3
     *            a {@link java.lang.String} object.
     */
    public Generate3ColorDeck(final String clr1, final String clr2, final String clr3) {
        if (clr1.equals("AI")) {
            int color1 = r.nextInt(5);
            int color2 = (color1 + 1 + r.nextInt(4)) % 5;
            colors = CardColor.fromMask(CardColor.WHITE << color1 | CardColor.WHITE << color2).inverse();
        } else {
            colors = CardColor.fromNames(clr1, clr2, clr3);
        }
    }

    /**
     * <p>
     * get3ColorDeck.
     * </p>
     * 
     * @param size
     *            a int.
     * @param pt
     *            the pt
     * @return a {@link forge.CardList} object.
     */
    public final ItemPoolView<CardPrinted> get3ColorDeck(final int size, final PlayerType pt) {
        List<CardPrinted> cards = selectCardsOfMatchingColorForPlayer(pt);
        // build subsets based on type
        final List<CardPrinted> creatures = CardRules.Predicates.Presets.IS_CREATURE.select(cards, CardPrinted.FN_GET_RULES);
        final List<CardPrinted> spells = CardRules.Predicates.Presets.isNonCreatureSpellForGenerator.select(cards, CardPrinted.FN_GET_RULES);

        final int creatCnt = (int) (creatPercentage * size);
        tmpDeck.append("Creature Count:").append(creatCnt).append("\n");
        addCmcAdjusted(creatures, creatCnt, cmcLevels, cmcAmounts);

        final int spellCnt = (int) (spellPercentage * size);
        tmpDeck.append("Spell Count:").append(spellCnt).append("\n");
        addCmcAdjusted(spells, spellCnt, cmcLevels, cmcAmounts);

        // Add lands
        int numLands = (int) (landsPercentage * size);

        tmpDeck.append("numLands:").append(numLands).append("\n");

        // Add dual lands

        List<String> duals = GenerateDeckUtil.getDualLandList(colors);
        for (String s : duals) {
            this.cardCounts.put(s, 0);
        }
        int dblsAdded = addSomeStr((numLands / 4), duals);
        numLands -= dblsAdded;

        addBasicLand(numLands);
        tmpDeck.append("DeckSize:").append(tDeck.countAll()).append("\n");

        adjustDeckSize(size);
        tmpDeck.append("DeckSize:").append(tDeck.countAll()).append("\n");
        if (ForgeProps.getProperty("showdeck/3color", "false").equals("true")) {
            ErrorViewer.showError(tmpDeck.toString());
        }

        return tDeck;
    }
}
