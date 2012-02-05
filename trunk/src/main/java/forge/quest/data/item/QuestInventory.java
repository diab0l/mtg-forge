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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * QuestInventory class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestInventory {

    /** The inventory. */
    private final Map<String, QuestItemAbstract> inventory = new HashMap<String, QuestItemAbstract>();

    /**
     * <p>
     * Constructor for QuestInventory.
     * </p>
     */
    public QuestInventory() {
        final Set<QuestItemAbstract> allItems = QuestInventory.getAllItems();
        for (final QuestItemAbstract item : allItems) {
            this.inventory.put(item.getName(), item);
        }
    }

    /**
     * <p>
     * hasItem.
     * </p>
     * 
     * @param itemName
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean hasItem(final String itemName) {
        return this.inventory.containsKey(itemName) && (this.inventory.get(itemName).getLevel() > 0);
    }

    /**
     * <p>
     * addItem.
     * </p>
     * 
     * @param item
     *            a {@link forge.quest.data.item.QuestItemAbstract} object.
     */
    public final void addItem(final QuestItemAbstract item) {
        this.inventory.put(item.getName(), item);
    }

    /**
     * <p>
     * getItemLevel.
     * </p>
     * 
     * @param itemName
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public final int getItemLevel(final String itemName) {
        final QuestItemAbstract item = this.inventory.get(itemName);
        if (item == null) {
            return 0;
        }
        return item.getLevel();
    }

    /**
     * <p>
     * setItemLevel.
     * </p>
     * 
     * @param itemName
     *            a {@link java.lang.String} object.
     * @param level
     *            a int.
     */
    public final void setItemLevel(final String itemName, final int level) {
        this.inventory.get(itemName).setLevel(level);
    }

    /**
     * <p>
     * getAllItems.
     * </p>
     * 
     * @return a {@link java.util.Set} object.
     */
    private static Set<QuestItemAbstract> getAllItems() {
        final SortedSet<QuestItemAbstract> set = new TreeSet<QuestItemAbstract>();

        set.add(new QuestItemPoundFlesh());
        set.add(new QuestItemElixir());
        set.add(new QuestItemEstates());
        set.add(new QuestItemLuckyCoin());
        set.add(new QuestItemMap());
        set.add(new QuestItemSleight());
        set.add(new QuestItemZeppelin());

        return set;
    }

    // Magic to support added pet types when reading saves.
    /**
     * <p>
     * readResolve.
     * </p>
     * 
     * @return a {@link java.lang.Object} object.
     */
    private Object readResolve() {
        for (final QuestItemAbstract item : QuestInventory.getAllItems()) {
            if (!this.inventory.containsKey(item.getName())) {
                this.inventory.put(item.getName(), item);
            }
        }
        return this;
    }

    /**
     * <p>
     * getItems.
     * </p>
     * 
     * @return a {@link java.util.Collection} object.
     */
    public Collection<QuestItemAbstract> getItems() {
        return this.inventory.values();
    }

    /**
     * <p>
     * getItem.
     * </p>
     * 
     * @param itemName
     *            a {@link java.lang.String} object.
     * @return a {@link forge.quest.data.item.QuestItemAbstract} object.
     */
    public QuestItemAbstract getItem(final String itemName) {
        return this.inventory.get(itemName);
    }
}
