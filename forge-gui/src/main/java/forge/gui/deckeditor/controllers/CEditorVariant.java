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

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.gui.deckeditor.SEditorIO;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.framework.FScreen;
import forge.gui.toolbox.itemmanager.CardManager;
import forge.gui.toolbox.itemmanager.ItemManagerConfig;
import forge.gui.toolbox.itemmanager.SItemManagerUtil;
import forge.item.PaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.util.ItemPool;
import forge.util.storage.IStorage;

import java.util.Map.Entry;

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
public final class CEditorVariant extends ACEditorBase<PaperCard, Deck> {
    private final DeckController<Deck> controller;
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;
    private final Predicate<PaperCard> cardPoolCondition;

    //=========== Constructor
    /**
     * Child controller for constructed deck editor UI.
     * This is the least restrictive mode;
     * all cards are available.
     */
    public CEditorVariant(final IStorage<Deck> folder, final Predicate<PaperCard> poolCondition, final DeckSection deckSection0, final FScreen screen0) {
        super(screen0);

        this.cardPoolCondition = poolCondition;
        this.sectionMode = deckSection0;

        CardManager catalogManager = new CardManager(true);
        CardManager deckManager = new CardManager(true);

        catalogManager.setCaption("Catalog");

        this.setCatalogManager(catalogManager);
        this.setDeckManager(deckManager);

        final Supplier<Deck> newCreator = new Supplier<Deck>() {
            @Override
            public Deck get() {
                return new Deck();
            }
        };
        this.controller = new DeckController<Deck>(folder, this, newCreator);
    }

    //=========== Overridden from ACEditorBase

    @Override
    protected CardLimit getCardLimit() {
        if (Singletons.getModel().getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY)) {
            return CardLimit.Default;
        }
        return CardLimit.None; //if not enforcing deck legality, don't enforce default limit
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#onAddItems()
     */
    @Override
    protected void onAddItems(Iterable<Entry<PaperCard, Integer>> items, boolean toAlternate) {
        if (toAlternate) { return; }

        ItemPool<PaperCard> itemsToAdd = getAllowedAdditions(items);
        if (itemsToAdd.isEmpty()) { return; }

        this.getDeckManager().addItems(itemsToAdd);
        this.getCatalogManager().selectItemEntrys(itemsToAdd); //just select all added cards in Catalog
        this.controller.notifyModelChanged();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#onRemoveItems()
     */
    @Override
    protected void onRemoveItems(Iterable<Entry<PaperCard, Integer>> items, boolean toAlternate) {
        if (toAlternate) { return; }

        this.getDeckManager().removeItems(items);
        this.getCatalogManager().selectItemEntrys(items); //just select all removed cards in Catalog
        this.controller.notifyModelChanged();
    }

    @Override
    protected void buildAddContextMenu(EditorContextMenuBuilder cmb) {
        cmb.addMoveItems("Add", "to deck");
    }

    @Override
    protected void buildRemoveContextMenu(EditorContextMenuBuilder cmb) {
        cmb.addMoveItems("Remove", "from deck");
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#updateView()
     */
    @Override
    public void resetTables() {
        Iterable<PaperCard> allNT = Singletons.getMagicDb().getVariantCards().getAllCards();
        allNT = Iterables.filter(allNT, cardPoolCondition);

        this.getCatalogManager().setPool(ItemPool.createFrom(allNT, PaperCard.class), true);
        this.getDeckManager().setPool(this.controller.getModel().getOrCreate(this.sectionMode));
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
    public void update() {
        this.getCatalogManager().setup(ItemManagerConfig.CARD_CATALOG);
        this.getDeckManager().setup(ItemManagerConfig.DECK_EDITOR);

        SItemManagerUtil.resetUI(this);

        deckGenParent = removeTab(VDeckgen.SINGLETON_INSTANCE);
        allDecksParent = removeTab(VAllDecks.SINGLETON_INSTANCE);

        this.controller.refreshModel();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#canSwitchAway()
     */
    @Override
    public boolean canSwitchAway(boolean isClosing) {
        return SEditorIO.confirmSaveChanges(getScreen(), isClosing);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#resetUIChanges()
     */
    @Override
    public void resetUIChanges() {
        //Re-add tabs
        if (deckGenParent != null) {
            deckGenParent.addDoc(VDeckgen.SINGLETON_INSTANCE);
        }
        if (allDecksParent != null) {
            allDecksParent.addDoc(VAllDecks.SINGLETON_INSTANCE);
        }
    }
}
