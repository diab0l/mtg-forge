package forge.gui.deckeditor.views;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import forge.game.GameType;
import forge.gui.deckeditor.controllers.CAllDecks;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.toolbox.itemmanager.DeckManager;
import forge.gui.toolbox.itemmanager.ItemManagerContainer;

/** 
 * Assembles Swing components of all deck viewer in deck editor.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VAllDecks implements IVDoc<CAllDecks> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("All Decks");

    private final DeckManager lstDecks = new DeckManager(GameType.Constructed);

    //========== Constructor
    private VAllDecks() {
        lstDecks.setCaption("Decks");
    }

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.EDITOR_ALLDECKS;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CAllDecks getLayoutControl() {
        return CAllDecks.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#populate()
     */
    @Override
    public void populate() {
        JPanel parentBody = parentCell.getBody();
        parentBody.setLayout(new MigLayout("insets 5, gap 0, wrap, hidemode 3"));
        parentBody.add(new ItemManagerContainer(lstDecks), "push, grow");
    }

    //========== Retrieval methods
    /** @return {@link javax.swing.JPanel} */
    public DeckManager getLstDecks() {
        return lstDecks;
    }
}
