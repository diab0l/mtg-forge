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

import forge.card.MagicColor;
import forge.card.ColorSet;
import forge.deck.generate.GenerateDeckUtil.FilterCMC;
import forge.error.ErrorViewer;
import forge.game.player.PlayerType;
import forge.item.CardPrinted;
import forge.item.ItemPoolView;
import forge.properties.ForgeProps;

/**
 * <p>
 * Generate2ColorDeck class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Generate2ColorDeck extends GenerateColoredDeckBase {
    @Override protected final float getLandsPercentage() { return 0.39f; }
    @Override protected final float getCreatPercentage() { return 0.36f; }
    @Override protected final float getSpellPercentage() { return 0.25f; }

    final List<FilterCMC> cmcLevels = Arrays.asList(
            new GenerateDeckUtil.FilterCMC(0, 2),
            new GenerateDeckUtil.FilterCMC(3, 4),
            new GenerateDeckUtil.FilterCMC(5, 6),
            new GenerateDeckUtil.FilterCMC(7, 20));
    final int[] cmcAmounts = {10, 8, 6, 2};

    // mana curve of the card pool
    // 20x 0 - 2
    // 16x 3 - 4
    // 12x 5 - 6
    // 4x 7 - 20
    // = 52x - card pool (before further random filtering)

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
    public Generate2ColorDeck(final String clr1, final String clr2) {

        if (clr1.equals("AI")) {
            int color1 = r.nextInt(5);
            int color2 = (color1 + 1 + r.nextInt(4)) % 5;
            colors = ColorSet.fromMask(MagicColor.WHITE << color1 | MagicColor.WHITE << color2);
        } else {
            colors = ColorSet.fromNames(clr1, clr2);
        }
    }


    public final ItemPoolView<CardPrinted> get2ColorDeck(final int size, final PlayerType pt) {
        addCreaturesAndSpells(size, cmcLevels, cmcAmounts, pt);

        // Add lands
        int numLands = (int) (getLandsPercentage() * size);

        tmpDeck.append("numLands:").append(numLands).append("\n");

        // Add dual lands

        List<String> duals = GenerateDeckUtil.getDualLandList(colors);
        for (String s : duals) {
            this.cardCounts.put(s, 0);
        }

        int dblsAdded = addSomeStr((numLands / 6), duals);
        numLands -= dblsAdded;

        addBasicLand(numLands);
        tmpDeck.append("DeckSize:").append(tDeck.countAll()).append("\n");

        adjustDeckSize(size);
        tmpDeck.append("DeckSize:").append(tDeck.countAll()).append("\n");
        if (ForgeProps.getProperty("showdeck/2color", "false").equals("true")) {
            ErrorViewer.showError(tmpDeck.toString());
        }

        return tDeck;
    }
}
