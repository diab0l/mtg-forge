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
package forge.quest.bazaar;

import forge.quest.data.QuestAssets;

/**
 * <p>
 * QuestItemZeppelin class.
 * </p>
 * 
 * @author Forge
 * @version $Id: QuestItemZeppelin.java 14797 2012-03-18 18:09:02Z Max mtg $
 */
public class QuestItemZeppelin extends QuestItemBasic {
    /**
     * <p>
     * Constructor for QuestItemZeppelin.
     * </p>
     */
    QuestItemZeppelin() {
        super(QuestItemType.ZEPPELIN); // , QuestStallManager.GEAR
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAvailableForPurchase(QuestAssets qA) {
        return super.isAvailableForPurchase(qA) && qA.hasItem(QuestItemType.MAP);
    }


}
