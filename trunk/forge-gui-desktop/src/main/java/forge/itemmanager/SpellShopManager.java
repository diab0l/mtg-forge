package forge.itemmanager;

import forge.item.InventoryItem;
import forge.itemmanager.filters.ItemFilter;

import javax.swing.*;

/** 
 * TODO: Write javadoc for this type.
 *
 */
@SuppressWarnings("serial")
public final class SpellShopManager extends ItemManager<InventoryItem> {
    public SpellShopManager(boolean wantUnique0) {
        super(InventoryItem.class, wantUnique0);
    }

    @Override
    protected void addDefaultFilters() {
        CardManager.addDefaultFilters(this);
    }

    @Override
    protected ItemFilter<? extends InventoryItem> createSearchFilter() {
        return CardManager.createSearchFilter(this);
    }

    @Override
    protected void buildAddFilterMenu(JMenu menu) {
        CardManager.buildAddFilterMenu(menu, this);
    }
}
