package forge.gui.workshop.controllers;

import forge.UiCommand;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.itemmanager.views.ColumnDef;
import forge.gui.toolbox.itemmanager.views.ItemColumn;
import forge.gui.toolbox.itemmanager.views.SColumnUtil;
import forge.gui.workshop.views.VWorkshopCatalog;

import java.util.Map;

/** 
 * Controls the "card catalog" panel in the workshop UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CWorkshopCatalog implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private CWorkshopCatalog() {
    }

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {
    }
    
    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        final Map<ColumnDef, ItemColumn> lstCatalogCols = SColumnUtil.getCatalogDefaultColumns();
        lstCatalogCols.remove(ColumnDef.QUANTITY);
        VWorkshopCatalog.SINGLETON_INSTANCE.getCardManager().setup(lstCatalogCols);
        //TODO: Restore previously selected card
    }
}
