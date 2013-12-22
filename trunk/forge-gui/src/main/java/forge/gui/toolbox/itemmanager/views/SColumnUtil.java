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
package forge.gui.toolbox.itemmanager.views;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.google.common.base.Function;

import forge.Singletons;
import forge.card.CardAiHints;
import forge.card.CardEdition;
import forge.card.CardRarity;
import forge.card.CardRules;
import forge.card.ColorSet;
import forge.card.mana.ManaCost;
import forge.gui.CardPreferences;
import forge.gui.toolbox.itemmanager.SItemManagerIO;
import forge.item.PaperCard;
import forge.item.IPaperCard;
import forge.item.InventoryItem;
import forge.item.InventoryItemFromSet;
import forge.limited.DraftRankCache;

/**
 * A collection of methods pertaining to columns in card catalog and
 * current deck tables, for use in the deck editor.
 * <br><br>
 * <i>(S at beginning of class name denotes a static factory.)</i>
 * 
 */
public final class SColumnUtil {
    /**
     * Each catalog column identified in the XML file is
     * referenced using these names. Its name in the XML
     * should match the name in the enum. Underscores
     * will be replaced with spaces in the display.
     * <br><br>
     * Note: To add a new column, put an enum here, and also add in the XML prefs file.
     */
    public enum ColumnName {
        CAT_FAVORITE,
        CAT_QUANTITY,
        CAT_NAME,
        CAT_COST,
        CAT_COLOR,
        CAT_TYPE,
        CAT_POWER,
        CAT_TOUGHNESS,
        CAT_CMC,
        CAT_RARITY,
        CAT_SET,
        CAT_AI,
        CAT_NEW,
        CAT_PURCHASE_PRICE,
        CAT_OWNED,
        CAT_RANKING,
        DECK_QUANTITY,
        DECK_NAME,
        DECK_COST,
        DECK_COLOR,
        DECK_TYPE,
        DECK_POWER,
        DECK_TOUGHNESS,
        DECK_CMC,
        DECK_RARITY,
        DECK_SET,
        DECK_AI,
        DECK_NEW,
        DECK_SALE_PRICE,
        DECK_DECKS,
        DECK_RANKING;
    }

    /** Possible states of data sorting in a column: none, ascending, or descending. */
    public enum SortState {
        NONE,
        ASC,
        DESC
    }

    /** @return List<TableColumnInfo<InventoryItem>> */
    public static List<TableColumnInfo<InventoryItem>> getCatalogDefaultColumns() {
        final List<TableColumnInfo<InventoryItem>> columns = new ArrayList<TableColumnInfo<InventoryItem>>();

        columns.add(SColumnUtil.getColumn(ColumnName.CAT_FAVORITE));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_QUANTITY));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_NAME));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_COST));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_COLOR));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_TYPE));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_POWER));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_TOUGHNESS));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_CMC));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_RARITY));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_SET));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_AI));
        columns.add(SColumnUtil.getColumn(ColumnName.CAT_RANKING));

        return columns;
    }

    /** @return List<TableColumnInfo<InventoryItem>> */
    public static List<TableColumnInfo<InventoryItem>> getDeckDefaultColumns() {
        final List<TableColumnInfo<InventoryItem>> columns = new ArrayList<TableColumnInfo<InventoryItem>>();

        columns.add(SColumnUtil.getColumn(ColumnName.DECK_QUANTITY));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_NAME));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_COST));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_COLOR));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_TYPE));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_POWER));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_TOUGHNESS));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_CMC));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_RARITY));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_SET));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_AI));
        columns.add(SColumnUtil.getColumn(ColumnName.DECK_RANKING));

        return columns;
    }

    /** Should be called after column preferences has run, which has created a new column list.  */
    public static void attachSortAndDisplayFunctions() {
        TableColumnInfo<InventoryItem> favoriteColumn = SColumnUtil.getColumn(ColumnName.CAT_FAVORITE);
        favoriteColumn.setMinWidth(18); //prevent resizing favorite column
        favoriteColumn.setMaxWidth(18);
        favoriteColumn.setDefaultSortState(SortState.DESC); //ensure favorites appear on top by default
        favoriteColumn.setSortAndDisplayFunctions(SColumnUtil.FN_FAV_COMPARE, SColumnUtil.FN_FAV_GET, new StarRenderer());

        SColumnUtil.getColumn(ColumnName.CAT_QUANTITY).setSortAndDisplayFunctions(
                SColumnUtil.FN_QTY_COMPARE, SColumnUtil.FN_QTY_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_NAME).setSortAndDisplayFunctions(
                SColumnUtil.FN_NAME_COMPARE, SColumnUtil.FN_NAME_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_COST).setSortAndDisplayFunctions(
                SColumnUtil.FN_COST_COMPARE, SColumnUtil.FN_COST_GET, new ManaCostRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_COLOR).setSortAndDisplayFunctions(
                SColumnUtil.FN_COLOR_COMPARE, SColumnUtil.FN_COLOR_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_TYPE).setSortAndDisplayFunctions(
                SColumnUtil.FN_TYPE_COMPARE, SColumnUtil.FN_TYPE_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_POWER).setSortAndDisplayFunctions(
                SColumnUtil.FN_POWER_COMPARE, SColumnUtil.FN_POWER_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_TOUGHNESS).setSortAndDisplayFunctions(
                SColumnUtil.FN_TOUGHNESS_COMPARE, SColumnUtil.FN_TOUGHNESS_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_CMC).setSortAndDisplayFunctions(
                SColumnUtil.FN_CMC_COMPARE, SColumnUtil.FN_CMC_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_RARITY).setSortAndDisplayFunctions(
                SColumnUtil.FN_RARITY_COMPARE, SColumnUtil.FN_RARITY_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_SET).setSortAndDisplayFunctions(
                SColumnUtil.FN_SET_COMPARE, SColumnUtil.FN_SET_GET, new SetCodeRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_AI).setSortAndDisplayFunctions(
                SColumnUtil.FN_AI_STATUS_COMPARE, SColumnUtil.FN_AI_STATUS_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.CAT_RANKING).setSortAndDisplayFunctions(
                SColumnUtil.FN_RANKING_COMPARE, SColumnUtil.FN_RANKING_GET, new ItemCellRenderer());

        SColumnUtil.getColumn(ColumnName.DECK_QUANTITY).setSortAndDisplayFunctions(
                SColumnUtil.FN_QTY_COMPARE, SColumnUtil.FN_QTY_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_NAME).setSortAndDisplayFunctions(
                SColumnUtil.FN_NAME_COMPARE, SColumnUtil.FN_NAME_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_COST).setSortAndDisplayFunctions(
                SColumnUtil.FN_COST_COMPARE, SColumnUtil.FN_COST_GET, new ManaCostRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_COLOR).setSortAndDisplayFunctions(
                SColumnUtil.FN_COLOR_COMPARE, SColumnUtil.FN_COLOR_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_TYPE).setSortAndDisplayFunctions(
                SColumnUtil.FN_TYPE_COMPARE, SColumnUtil.FN_TYPE_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_POWER).setSortAndDisplayFunctions(
                SColumnUtil.FN_POWER_COMPARE, SColumnUtil.FN_POWER_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_TOUGHNESS).setSortAndDisplayFunctions(
                SColumnUtil.FN_TOUGHNESS_COMPARE, SColumnUtil.FN_TOUGHNESS_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_CMC).setSortAndDisplayFunctions(
                SColumnUtil.FN_CMC_COMPARE, SColumnUtil.FN_CMC_GET, new IntegerRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_RARITY).setSortAndDisplayFunctions(
                SColumnUtil.FN_RARITY_COMPARE, SColumnUtil.FN_RARITY_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_SET).setSortAndDisplayFunctions(
                SColumnUtil.FN_SET_COMPARE, SColumnUtil.FN_SET_GET, new SetCodeRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_AI).setSortAndDisplayFunctions(
                SColumnUtil.FN_AI_STATUS_COMPARE, SColumnUtil.FN_AI_STATUS_GET, new ItemCellRenderer());
        SColumnUtil.getColumn(ColumnName.DECK_RANKING).setSortAndDisplayFunctions(
                SColumnUtil.FN_RANKING_COMPARE, SColumnUtil.FN_RANKING_GET, new ItemCellRenderer());
    }

    /**
     * Hides/shows a table column.
     * 
     * @param table JTable
     * @param col0 TableColumnInfo<InventoryItem>
     */
    public static void toggleColumn(final JTable table, final TableColumnInfo<InventoryItem> col0) {
        final TableColumnModel colmodel = table.getColumnModel();

        if (col0.isShowing()) {
            col0.setShowing(false);
            colmodel.removeColumn(col0);
        }
        else {
            col0.setShowing(true);
            colmodel.addColumn(col0);

            if (col0.getModelIndex() < colmodel.getColumnCount()) {
                colmodel.moveColumn(colmodel.getColumnIndex(col0.getIdentifier()), col0.getModelIndex());
                Enumeration<TableColumn> cols = colmodel.getColumns();
                int index = 0;
                // If you're getting renderer "can't cast T to U" errors, that's
                // a sign that the model index needs updating.
                while (cols.hasMoreElements()) {
                   cols.nextElement().setModelIndex(index++);
                }
            }
            else {
                col0.setModelIndex(colmodel.getColumnCount());
            }
        }
    }

    /**
     * Retrieve a custom column (uses identical method in SEditorIO).
     * 
     * @param id0 &emsp; {@link forge.gui.deckeditor.SEditorUtil.CatalogColumnName}
     * @return TableColumnInfo<InventoryItem>
     */
    public static TableColumnInfo<InventoryItem> getColumn(final ColumnName id0) {
        return SItemManagerIO.getColumn(id0);
    }

    /**
     * Convenience method to get a column's index in the view (that is,
     * in the TableColumnModel).
     * 
     * @param table JTable
     * @param id0 &emsp; {@link forge.gui.deckeditor.SItemManagerUtil.ColumnName}
     * @return int
     */
    public static int getColumnViewIndex(final JTable table, final ColumnName id0) {
        int index = -1;

        try {
            index = table.getColumnModel().getColumnIndex(SColumnUtil.getColumn(id0).getIdentifier());
        }
        catch (final Exception e) { }

        return index;
    }

    /**
     * Convenience method to get a column's index in the model (that is,
     * in the EditorTableModel, NOT the TableColumnModel).
     * 
     * @param table JTable
     * @param id0 &emsp; {@link forge.gui.deckeditor.SItemManagerUtil.ColumnName}
     * @return int
     */
    public static int getColumnModelIndex(final JTable table, final ColumnName id0) {
        return table.getColumn(SColumnUtil.getColumn(id0).getIdentifier()).getModelIndex();
    }

    //========== Display functions

    private static final Pattern AE_FINDER = Pattern.compile("AE", Pattern.LITERAL);

    private static IPaperCard toCard(final InventoryItem i) {
        return i instanceof IPaperCard ? ((IPaperCard) i) : null;
    }
    private static ManaCost toManaCost(final InventoryItem i) {
        return i instanceof IPaperCard ? ((IPaperCard) i).getRules().getManaCost() : ManaCost.NO_COST;
    }
    private static CardRules toCardRules(final InventoryItem i) {
        return i instanceof IPaperCard ? ((IPaperCard) i).getRules() : null;
    }

    private static ColorSet toColor(final InventoryItem i) {
        return i instanceof IPaperCard ? ((IPaperCard) i).getRules().getColor() : ColorSet.getNullColor();
    }

    private static int toPower(final InventoryItem i) {
        int result = -1;
        if (i instanceof PaperCard) {
            result = ((IPaperCard) i).getRules().getIntPower();
            if (result == -1) {
                result = ((IPaperCard) i).getRules().getInitialLoyalty();
            }
        }
        return result;
    }

    private static int toToughness(final InventoryItem i) {
        return i instanceof PaperCard ? ((IPaperCard) i).getRules().getIntToughness() : -1;
    }

    private static Integer toCMC(final InventoryItem i) {
        return i instanceof PaperCard ? ((IPaperCard) i).getRules().getManaCost().getCMC() : -1;
    }

    private static CardRarity toRarity(final InventoryItem i) {
        return i instanceof PaperCard ? ((IPaperCard) i).getRarity() : CardRarity.Unknown;
    }

    private static CardEdition toSetCmp(final InventoryItem i) {
        return i instanceof InventoryItemFromSet ? Singletons.getMagicDb().getEditions()
                .get(((InventoryItemFromSet) i).getEdition()) : CardEdition.UNKNOWN;
    }

    private static String toSetStr(final InventoryItem i) {
        return i instanceof InventoryItemFromSet ? ((InventoryItemFromSet) i).getEdition() : "n/a";
    }

    private static Integer toAiCmp(final InventoryItem i) {
        return i instanceof PaperCard ? ((IPaperCard) i).getRules().getAiHints().getAiStatusComparable() : Integer.valueOf(-1);
    }

    private static String toAiStr(final InventoryItem i) {
        if (!(i instanceof PaperCard))
            return "n/a";
        
        IPaperCard cp = (IPaperCard) i;
        CardAiHints ai = cp.getRules().getAiHints();
        
        return ai.getRemAIDecks() ? (ai.getRemRandomDecks() ? "AI ?" : "AI")
                : (ai.getRemRandomDecks() ? "?" : "");
    }
    
    private static Double toRankingCmp(final InventoryItem i) {
        Double ranking = 500D;
        if (i != null && i instanceof PaperCard){
            PaperCard cp = (PaperCard) i;
            ranking = DraftRankCache.getRanking(cp.getName(), cp.getEdition());
            if ( ranking == null )
                ranking = 500D;
        }
        return ranking;
    }

    //==========

    /** Lamda sort fnQtyCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_FAV_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            IPaperCard card = SColumnUtil.toCard(from.getKey());
            if (card == null) {
                return -1;
            }
            return CardPreferences.getPrefs(card.getName()).getStarCount();
        }
    };

    /** Lamda sort fnQtyGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_FAV_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toCard(from.getKey());
        }
    };

    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_QTY_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return from.getValue();
        }
    };

    /** Lamda sort fnQtyGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_QTY_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return from.getValue();
        }
    };

    /** Lamda sort fnNameCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_NAME_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return from.getKey().getName();
        }
    };

    /** Lamda sort fnNameGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_NAME_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            final String name = from.getKey().getName();
            return name.contains("AE") ? SColumnUtil.AE_FINDER.matcher(name).replaceAll("\u00C6") : name;
        }
    };

    /** Lamda sort fnCostCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_COST_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toManaCost(from.getKey());
        }
    };

    /** Lamda sort fnCostGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_COST_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toCardRules(from.getKey());
        }
    };

    /** Lamda sort fnColorCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_COLOR_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toColor(from.getKey());
        }
    };

    /** Lamda sort fnColorGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_COLOR_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toColor(from.getKey());
        }
    };

    /** Lamda sort fnTypeCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_TYPE_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            InventoryItem i = from.getKey();
            return i instanceof PaperCard ? ((IPaperCard)i).getRules().getType().toString() : i.getItemType();
        }
    };

    /** Lamda sort fnTypeGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_TYPE_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            InventoryItem i = from.getKey();
            return i instanceof PaperCard ? ((IPaperCard)i).getRules().getType().toString() : i.getItemType();
        }
    };

    /** Lamda sort fnPowerCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_POWER_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return Integer.valueOf(SColumnUtil.toPower(from.getKey()));
        }
    };

    /** Lamda sort fnPowerGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_POWER_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return Integer.valueOf(SColumnUtil.toPower(from.getKey()));
        }
    };

    /** Lamda sort fnToughnessCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_TOUGHNESS_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return Integer.valueOf(SColumnUtil.toToughness(from.getKey()));
        }
    };

    /** Lamda sort fnToughnessGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_TOUGHNESS_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return Integer.valueOf(SColumnUtil.toToughness(from.getKey()));
        }
    };

    /** Lamda sort fnCMCCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_CMC_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toCMC(from.getKey());
        }
    };

    /** Lamda sort fnCMCGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_CMC_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toCMC(from.getKey());
        }
    };

    /** Lamda sort fnRarityCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_RARITY_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toRarity(from.getKey());
        }
    };

    /** Lamda sort fnRarityGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_RARITY_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toRarity(from.getKey());
        }
    };

    /** Lamda sort fnSetCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_SET_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toSetCmp(from.getKey());
        }
    };

    /** Lamda sort fnSetGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_SET_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toSetStr(from.getKey());
        }
    };

    /** Lamda sort fnAiStatusCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_AI_STATUS_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toAiCmp(from.getKey());
        }
    };

    /** Lamda sort fnAiStatusGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_AI_STATUS_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toAiStr(from.getKey());
        }
    };
    
    /** Lamda sort fnRankingCompare. */
    private static final Function<Entry<InventoryItem, Integer>, Comparable<?>> FN_RANKING_COMPARE = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return SColumnUtil.toRankingCmp(from.getKey());
        }
    };

    /** Lamda sort fnRankingGet. */
    private static final Function<Entry<InventoryItem, Integer>, Object> FN_RANKING_GET = new Function<Entry<InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<InventoryItem, Integer> from) {
            return String.valueOf(SColumnUtil.toRankingCmp(from.getKey()));
        }
    };
}
