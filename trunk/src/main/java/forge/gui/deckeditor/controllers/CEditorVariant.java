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
package forge.gui.deckeditor.controllers;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.gui.deckeditor.SEditorIO;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.tables.DeckController;
import forge.gui.deckeditor.tables.EditorTableView;
import forge.gui.deckeditor.tables.SColumnUtil;
import forge.gui.deckeditor.tables.SColumnUtil.ColumnName;
import forge.gui.deckeditor.tables.TableColumnInfo;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.framework.EDocID;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.properties.ForgePreferences.FPref;
import forge.util.storage.IStorage;

/**
 * Child controller for constructed deck editor UI.
 * This is the least restrictive mode;
 * all cards are available.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id: CEditorConstructed.java 18430 2012-11-27 22:42:36Z Hellfish $
 */
public final class CEditorVariant extends ACEditorBase<CardPrinted, Deck> {
    private final DeckController<Deck> controller;
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;
    private final Predicate<CardPrinted> cardPoolCondition;
    private final EDocID exitToScreen;

    //=========== Constructor
    /**
     * Child controller for constructed deck editor UI.
     * This is the least restrictive mode;
     * all cards are available.
     */
    public CEditorVariant(final IStorage<Deck> folder, final Predicate<CardPrinted> poolCondition, final EDocID exitTo) {
        super();
        
        cardPoolCondition = poolCondition;
        exitToScreen = exitTo;
        
        final EditorTableView<CardPrinted> tblCatalog = new EditorTableView<CardPrinted>(true, CardPrinted.class);
        final EditorTableView<CardPrinted> tblDeck = new EditorTableView<CardPrinted>(true, CardPrinted.class);

        VCardCatalog.SINGLETON_INSTANCE.setTableView(tblCatalog.getTable());
        VCurrentDeck.SINGLETON_INSTANCE.setTableView(tblDeck.getTable());

        this.setTableCatalog(tblCatalog);
        this.setTableDeck(tblDeck);

        final Supplier<Deck> newCreator = new Supplier<Deck>() {
            @Override
            public Deck get() {
                return new Deck();
            }
        };
        this.controller = new DeckController<Deck>(folder, this, newCreator);
    }

    //=========== Overridden from ACEditorBase

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof CardPrinted) || toAlternate) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;
        this.getTableDeck().addCard(card, qty);
        this.controller.notifyModelChanged();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#removeCard()
     */
    @Override
    public void removeCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof CardPrinted) || toAlternate) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;
        this.getTableDeck().removeCard(card, qty);
        this.controller.notifyModelChanged();
    }

    @Override
    public void buildAddContextMenu(ContextMenuBuilder cmb) {
        cmb.addMoveItems("Move", "card", "cards", "to deck");
        cmb.addTextFilterItem();
    }
    
    @Override
    public void buildRemoveContextMenu(ContextMenuBuilder cmb) {
        cmb.addMoveItems("Move", "card", "cards", "to sideboard");
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#updateView()
     */
    @Override
    public void resetTables() {
        Iterable<CardPrinted> allNT = CardDb.variants().getAllCards();
        allNT = Iterables.filter(allNT, cardPoolCondition);
        
        this.getTableCatalog().setDeck(ItemPool.createFrom(allNT, CardPrinted.class), true);
        this.getTableDeck().setDeck(this.controller.getModel().getOrCreate(DeckSection.Sideboard));
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#getController()
     */
    @Override
    public DeckController<Deck> getDeckController() {
        return this.controller;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#show(forge.Command)
     */
    @Override
    public void init() {
        final List<TableColumnInfo<InventoryItem>> lstCatalogCols = SColumnUtil.getCatalogDefaultColumns();
        lstCatalogCols.remove(SColumnUtil.getColumn(ColumnName.CAT_QUANTITY));

        this.getTableCatalog().setup(VCardCatalog.SINGLETON_INSTANCE, lstCatalogCols);
        this.getTableDeck().setup(VCurrentDeck.SINGLETON_INSTANCE, SColumnUtil.getDeckDefaultColumns());

        SEditorUtil.resetUI();
        
        deckGenParent = removeTab(VDeckgen.SINGLETON_INSTANCE);
        allDecksParent = removeTab(VAllDecks.SINGLETON_INSTANCE);        

        this.controller.newModel();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#exit()
     */
    @Override
    public boolean exit() {
        // Override the submenu save choice - tell it to go to "constructed".
        Singletons.getModel().getPreferences().setPref(FPref.SUBMENU_CURRENTMENU, exitToScreen.toString());

        if (!SEditorIO.confirmSaveChanges())
        {
            return false;
        }
        
        //Re-add tabs
        if (deckGenParent != null) {
            deckGenParent.addDoc(VDeckgen.SINGLETON_INSTANCE);
        }
        if (allDecksParent != null) {
            allDecksParent.addDoc(VAllDecks.SINGLETON_INSTANCE);
        }
        
        return true;
    }
}
