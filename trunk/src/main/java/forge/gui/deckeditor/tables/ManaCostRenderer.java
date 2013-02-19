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
package forge.gui.deckeditor.tables;

import java.awt.Component;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import forge.card.mana.ManaCostShard;
import forge.card.mana.ManaCost;
import forge.gui.toolbox.CardFaceSymbols;

/**
 * Displays mana cost as symbols.
 */
public class ManaCostRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1770527102334163549L;

    private ManaCost value;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
     * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public final Component getTableCellRendererComponent(final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.value = (ManaCost) value;
        this.setToolTipText(this.value.toString());
        return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public final void paint(final Graphics g) {
        super.paint(g);

        final int elemtWidth = 13;
        final int elemtGap = 0;
        final int padding = 1;

        float xpos = padding;

        final int genericManaCost = this.value.getGenericCost();
        final int xManaCosts = this.value.countX();
        final boolean hasGeneric = (genericManaCost > 0) || this.value.isPureGeneric();
        final List<ManaCostShard> shards = this.value.getShards();

        final int cellWidth = this.getWidth();
        final int cntGlyphs = hasGeneric ? shards.size() + 1 : shards.size();
        final float offsetIfNoSpace = cntGlyphs > 1 ? (cellWidth - padding - elemtWidth) / (cntGlyphs - 1f)
                : elemtWidth + elemtGap;
        final float offset = Math.min(elemtWidth + elemtGap, offsetIfNoSpace);

        // Display X Mana before any other type of mana
        if (xManaCosts > 0) {
            for (int i = 0; i < xManaCosts; i++) {
                CardFaceSymbols.drawSymbol(ManaCostShard.X.getImageKey(), g, (int) xpos, 1);
                xpos += offset;
            }
        }

        // Display colorless mana before colored mana
        if (hasGeneric) {
            final String sGeneric = Integer.toString(genericManaCost);
            CardFaceSymbols.drawSymbol(sGeneric, g, (int) xpos, 1);
            xpos += offset;
        }

        for (final ManaCostShard s : shards) {
            if (s.equals(ManaCostShard.X)) {
                // X costs already drawn up above
                continue;
            }
            CardFaceSymbols.drawSymbol(s.getImageKey(), g, (int) xpos, 1);
            xpos += offset;
        }
    }

}
