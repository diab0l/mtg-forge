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

package forge.model;

import forge.card.IUnOpenedProduct;
import forge.interfaces.IGuiBase;
import forge.item.PaperCard;
import forge.util.MyRandom;
import forge.util.TextUtil;
import forge.util.gui.SGuiChoose;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** 
 * This type extends UnOpenedProduct to support booster choice or random boosters
 * in sealed deck games. See MetaSet.java for further information.
 */

public class UnOpenedMeta implements IUnOpenedProduct {

    private enum JoinOperation {
        RandomOne,
        ChooseOne,
        SelectAll,
    }

    private final ArrayList<MetaSet> metaSets;
    private final JoinOperation operation;
    private final Random generator = MyRandom.getRandom();
    private final IGuiBase gui;

    /**
     * Constructor for UnOpenedMeta.
     * 
     * @param creationString
     *            String, is parsed for MetaSet info.
     * @param choose
     *            sets the random/choice status.
     * @param gui
     *            the gui.
     */
    private UnOpenedMeta(final String creationString, final JoinOperation op, final IGuiBase gui) {
        metaSets = new ArrayList<MetaSet>();
        operation = op;
        this.gui = gui;

        for (String m : TextUtil.splitWithParenthesis(creationString, ';')) {
            metaSets.add(new MetaSet(m, true));
        }
    }

    /**
     * Open the booster pack, return contents.
     * @return List, list of cards.
     */
    @Override
    public List<PaperCard> get() {
        return this.open(true, false);
    }

    /**
     * Like open, can define whether is human or not.
     * @param isHuman
     *      boolean, is human player?
     * @param partialities
     *      known partialities for the AI.
     * @return List, list of cards.
     */
    public List<PaperCard> open(final boolean isHuman, final boolean allowCancel) {
        if (metaSets.isEmpty()) {
            throw new RuntimeException("Empty UnOpenedMetaset, cannot generate booster.");
        }

        switch (operation) {
            case ChooseOne:
                if (isHuman) {
                    final MetaSet ms;
                    if (allowCancel) {
                        ms = SGuiChoose.oneOrNone(gui, "Choose Booster", metaSets);
                        if (ms == null) {
                            return null;
                        }
                    }
                    else {
                        ms = SGuiChoose.one(gui, "Choose Booster", metaSets);
                    }
                    return ms.getBooster(gui).get();
                }

            case RandomOne: // AI should fall though here from the case above
                int selected = generator.nextInt(metaSets.size());
                final IUnOpenedProduct newBooster = metaSets.get(selected).getBooster(gui);
                return newBooster.get();

            case SelectAll:
                List<PaperCard> allCards = new ArrayList<PaperCard>();
                for (MetaSet ms : metaSets) {
                    allCards.addAll(ms.getBooster(gui).get());
                }
                return allCards;
        }
        throw new IllegalStateException("Got wrong operation type in unopenedMeta - execution should never reach this point");
    }

    public static UnOpenedMeta choose(final String desc, final IGuiBase gui) {
        return new UnOpenedMeta(desc, JoinOperation.ChooseOne, gui);
    }
    public static UnOpenedMeta random(final String desc) {
        return new UnOpenedMeta(desc, JoinOperation.RandomOne, null);
    }
    public static UnOpenedMeta selectAll(final String desc) {
        return new UnOpenedMeta(desc, JoinOperation.SelectAll, null);
    }
}
