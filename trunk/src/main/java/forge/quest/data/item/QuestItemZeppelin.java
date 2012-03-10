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
package forge.quest.data.item;

import javax.swing.ImageIcon;

import forge.gui.toolbox.FSkin;
import forge.quest.data.QuestAssets;

/**
 * <p>
 * QuestItemZeppelin class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestItemZeppelin extends QuestItemAbstract {

    /** The zeppelin used. */
    private boolean zeppelinUsed = false;

    /**
     * <p>
     * Constructor for QuestItemZeppelin.
     * </p>
     */
    QuestItemZeppelin() {
        super("Zeppelin"); // , QuestStallManager.GEAR
    }

    /** {@inheritDoc} */
    @Override
    public final String getPurchaseName() {
        return "Zeppelin";
    }

    /** {@inheritDoc} */
    @Override
    public final String getPurchaseDescription() {
        return "This extremely comfortable airship allows for more efficient and safe travel to faraway destinations.\n"
                + "\nEffect: Quest assignments become available more frequently."
                + "\nEffect: Adds +3 to max life during quest games."
                + "\nEffect: Allows travel to far places, allowing you to see a new set of opponents,";
    }

    /** {@inheritDoc} */
    @Override
    public final ImageIcon getIcon() {
        return FSkin.getIcon(FSkin.QuestIcons.ICO_ZEP);
    }

    /** {@inheritDoc} */
    @Override
    public final int getBuyingPrice(QuestAssets qA) {
        return 5000;
    }

    /** {@inheritDoc} */
    @Override
    public final int getSellingPrice(QuestAssets qA) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAvailableForPurchase(QuestAssets qA) {
        return super.isAvailableForPurchase(qA) && qA.getInventory().hasItem("Map");
    }

    /**
     * <p>
     * hasBeenUsed.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean hasBeenUsed() {
        return this.zeppelinUsed;
    }

    /**
     * <p>
     * Setter for the field <code>zeppelinUsed</code>.
     * </p>
     * 
     * @param used
     *            a boolean.
     */
    public final void setZeppelinUsed(final boolean used) {
        this.zeppelinUsed = used;
    }
}
