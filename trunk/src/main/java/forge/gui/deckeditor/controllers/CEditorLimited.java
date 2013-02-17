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

import com.google.common.base.Supplier;

import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckSection;
import forge.gui.deckeditor.SEditorIO;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.tables.DeckController;
import forge.gui.deckeditor.tables.EditorTableView;
import forge.gui.deckeditor.tables.SColumnUtil;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.home.sanctioned.CSubmenuDraft;
import forge.gui.home.sanctioned.CSubmenuSealed;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.util.storage.IStorage;

/**
 * Child controller for limited deck editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id: DeckEditorCommon.java 12850 2011-12-26 14:55:09Z slapshot5 $
 */
public final class CEditorLimited extends ACEditorBase<CardPrinted, DeckGroup> {

    private final DeckController<DeckGroup> controller;
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;

    //========== Constructor

    /**
     * Child controller for limited deck editor UI.
     *
     * @param deckMap0 &emsp; {@link forge.deck.DeckGroup}<{@link forge.util.storage.IStorage}>
     */
    public CEditorLimited(final IStorage<DeckGroup> deckMap0) {
        final EditorTableView<CardPrinted> tblCatalog = new EditorTableView<CardPrinted>(false, CardPrinted.class);
        final EditorTableView<CardPrinted> tblDeck = new EditorTableView<CardPrinted>(false, CardPrinted.class);

        VCardCatalog.SINGLETON_INSTANCE.setTableView(tblCatalog.getTable());
        VCurrentDeck.SINGLETON_INSTANCE.setTableView(tblDeck.getTable());

        tblCatalog.setAlwaysNonUnique(true);
        tblDeck.setAlwaysNonUnique(true);

        this.setTableCatalog(tblCatalog);
        this.setTableDeck(tblDeck);

        final Supplier<DeckGroup> newCreator = new Supplier<DeckGroup>() {
            @Override
            public DeckGroup get() {
                return new DeckGroup("");
            }
        };
        this.controller = new DeckController<DeckGroup>(deckMap0, this, newCreator);
    }

    /**
     * @param model
     * @return
     */
    private Deck getSelectedDeck(final DeckGroup model) {
        return model.getHumanDeck();
    }

    //========== Overridden from ACEditorBase

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof CardPrinted) || toAlternate) {
            return;
        }

        // update view
        final CardPrinted card = (CardPrinted) item;
        this.getTableDeck().addCard(card, qty);
        this.getTableCatalog().removeCard(card, qty);
        this.getDeckController().notifyModelChanged();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#removeCard()
     */
    @Override
    public void removeCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof CardPrinted) || toAlternate) {
            return;
        }

        // update view
        final CardPrinted card = (CardPrinted) item;
        this.getTableCatalog().addCard(card, qty);
        this.getTableDeck().removeCard(card, qty);
        this.getDeckController().notifyModelChanged();
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
        final Deck toEdit = this.getSelectedDeck(this.controller.getModel());
        this.getTableCatalog().setDeck(toEdit.getOrCreate(DeckSection.Sideboard));
        this.getTableDeck().setDeck(toEdit.getMain());
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#getController()
     */
    @Override
    public DeckController<DeckGroup> getDeckController() {
        return this.controller;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#show(forge.Command)
     */
    @Override
    public void init() {
        this.getTableCatalog().setup(VCardCatalog.SINGLETON_INSTANCE, SColumnUtil.getCatalogDefaultColumns());
        this.getTableDeck().setup(VCurrentDeck.SINGLETON_INSTANCE, SColumnUtil.getDeckDefaultColumns());

        SEditorUtil.resetUI();

        VCurrentDeck.SINGLETON_INSTANCE.getBtnPrintProxies().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setEnabled(false);

        VCardCatalog.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);
        VCardCatalog.SINGLETON_INSTANCE.getLblTitle().setText("Deck Editor: Limited Mode");
        
        deckGenParent = removeTab(VDeckgen.SINGLETON_INSTANCE);
        allDecksParent = removeTab(VAllDecks.SINGLETON_INSTANCE);        
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#exit()
     */
    @Override
    public boolean exit() {
        final boolean okToExit = SEditorIO.confirmSaveChanges();

        if (okToExit) {
            CSubmenuDraft.SINGLETON_INSTANCE.update();
            CSubmenuSealed.SINGLETON_INSTANCE.update();
            
            //Re-add tabs
            if (deckGenParent != null) {
                deckGenParent.addDoc(VDeckgen.SINGLETON_INSTANCE);
            }
            if (allDecksParent != null) {
                allDecksParent.addDoc(VAllDecks.SINGLETON_INSTANCE);
            }
            
        }

        return okToExit;
    }
}
