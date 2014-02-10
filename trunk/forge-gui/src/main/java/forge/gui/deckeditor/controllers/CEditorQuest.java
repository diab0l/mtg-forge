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

// import java.awt.Font;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import forge.UiCommand;
import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.gui.deckeditor.SEditorIO;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.framework.FScreen;
import forge.gui.home.quest.CSubmenuQuestDecks;
import forge.gui.toolbox.itemmanager.CardManager;
import forge.gui.toolbox.itemmanager.SItemManagerUtil;
import forge.gui.toolbox.itemmanager.views.ColumnDef;
import forge.gui.toolbox.itemmanager.views.GroupDef;
import forge.gui.toolbox.itemmanager.views.ItemColumn;
import forge.gui.toolbox.itemmanager.views.SColumnUtil;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.util.ItemPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import forge.quest.data.QuestBoosterPack;

/**
 * Child controller for quest deck editor UI.
 * <br><br>
 * Card catalog and decks are drawn from a QuestController object.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id$
 */
public final class CEditorQuest extends ACEditorBase<PaperCard, Deck> {
    private final QuestController questData;
    private final DeckController<Deck> controller;
    private final List<DeckSection> allSections = new ArrayList<DeckSection>();
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;

    private Map<PaperCard, Integer> decksUsingMyCards;

    private final Function<Entry<InventoryItem, Integer>, Comparable<?>> fnDeckCompare = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            final Integer iValue = decksUsingMyCards.get(from.getKey());
            return iValue == null ? Integer.valueOf(0) : iValue;
        }
    };

    private final Function<Entry<? extends InventoryItem, Integer>, Object> fnDeckGet = new Function<Entry<? extends InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<? extends InventoryItem, Integer> from) {
            final Integer iValue = decksUsingMyCards.get(from.getKey());
            return iValue == null ? "" : iValue.toString();
        }
    };

    /**
     * Child controller for quest deck editor UI.
     * <br><br>
     * Card catalog and decks are drawn from a QuestController object.
     * 
     * @param questData0 &emsp; {@link forge.quest.QuestController}
     */
    public CEditorQuest(final QuestController questData0) {
        super(FScreen.DECK_EDITOR_QUEST);

        allSections.add(DeckSection.Main);
        allSections.add(DeckSection.Sideboard);

        this.questData = questData0;

        final CardManager catalogManager = new CardManager(false);
        final CardManager deckManager = new CardManager(false);

        catalogManager.setCaption("Quest Inventory");

        catalogManager.setAlwaysNonUnique(true);
        deckManager.setAlwaysNonUnique(true);

        this.setCatalogManager(catalogManager);
        this.setDeckManager(deckManager);

        final Supplier<Deck> newCreator = new Supplier<Deck>() {
            @Override
            public Deck get() {
                return new Deck();
            }
        };

        this.controller = new DeckController<Deck>(questData0.getMyDecks(), this, newCreator);
    }

    /**
     * Adds any card to the catalog and data pool.
     * 
     * @param card {@link forge.item.PaperCard}
     */
    public void addCheatCard(final PaperCard card, int qty) {
        this.getCatalogManager().addItem(card, qty);
        this.questData.getCards().getCardpool().add(card, qty);
    }

    // fills number of decks using each card
    private Map<PaperCard, Integer> countDecksForEachCard() {
        final Map<PaperCard, Integer> result = new HashMap<PaperCard, Integer>();
        for (final Deck deck : this.questData.getMyDecks()) {
            for (final Entry<PaperCard, Integer> e : deck.getMain()) {
                final PaperCard card = e.getKey();
                final Integer amount = result.get(card);
                result.put(card, Integer.valueOf(amount == null ? 1 : 1 + amount.intValue()));
            }
        }
        return result;
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
        CEditorConstructed.onAddItems(this, items, toAlternate);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#onRemoveItems()
     */
    @Override
    protected void onRemoveItems(Iterable<Entry<PaperCard, Integer>> items, boolean toAlternate) {
        CEditorConstructed.onRemoveItems(this, items, toAlternate);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#buildAddContextMenu()
     */
    @Override
    protected void buildAddContextMenu(EditorContextMenuBuilder cmb) {
        CEditorConstructed.buildAddContextMenu(cmb, sectionMode);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#buildRemoveContextMenu()
     */
    @Override
    protected void buildRemoveContextMenu(EditorContextMenuBuilder cmb) {
        CEditorConstructed.buildRemoveContextMenu(cmb, sectionMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#updateView()
     */
    @Override
    public void resetTables() {
        this.sectionMode = DeckSection.Main;

        final Deck deck = this.controller.getModel();

        final ItemPool<PaperCard> cardpool = new ItemPool<PaperCard>(PaperCard.class);
        cardpool.addAll(this.questData.getCards().getCardpool());
        // remove bottom cards that are in the deck from the card pool
        cardpool.removeAll(deck.getMain());
        // remove sideboard cards from the catalog
        cardpool.removeAll(deck.getOrCreate(DeckSection.Sideboard));
        // show cards, makes this user friendly
        this.getCatalogManager().setPool(cardpool);
        this.getDeckManager().setPool(deck.getMain());
    }

    //=========== Overridden from ACEditorBase

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#getController()
     */
    @Override
    public DeckController<Deck> getDeckController() {
        return this.controller;
    }

    /**
     * Switch between the main deck and the sideboard editor.
     */
    public void cycleEditorMode() {
        int curindex = allSections.indexOf(sectionMode);
        curindex = (curindex + 1) % allSections.size();
        sectionMode = allSections.get(curindex);

        if (sectionMode == DeckSection.Sideboard) {
            this.getDeckManager().setPool(this.controller.getModel().getOrCreate(DeckSection.Sideboard));
        }
        else {
            this.getDeckManager().setPool(this.controller.getModel().getMain());
        }

        this.controller.updateCaptions();
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#show(forge.Command)
     */
    @SuppressWarnings("serial")
    @Override
    public void update() {
        final Map<ColumnDef, ItemColumn> columnsCatalog = SColumnUtil.getCatalogDefaultColumns();
        final Map<ColumnDef, ItemColumn> columnsDeck = SColumnUtil.getDeckDefaultColumns();

        this.decksUsingMyCards = this.countDecksForEachCard();

        // Add "new" column in catalog and deck
        columnsCatalog.put(ColumnDef.NEW, new ItemColumn(ColumnDef.NEW, this.questData.getCards().getFnNewCompare(), this.questData.getCards().getFnNewGet()));

        columnsDeck.put(ColumnDef.NEW, new ItemColumn(ColumnDef.NEW, this.questData.getCards().getFnNewCompare(), this.questData.getCards().getFnNewGet()));

        columnsDeck.put(ColumnDef.DECKS, new ItemColumn(ColumnDef.DECKS, this.fnDeckCompare, this.fnDeckGet));

        this.getCatalogManager().setup(columnsCatalog);
        this.getDeckManager().setup(columnsDeck, GroupDef.CREATURE_SPELL_LAND, ColumnDef.CMC, 1);

        SItemManagerUtil.resetUI(this);

        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setVisible(false);

        this.getBtnCycleSection().setVisible(true);
        this.getBtnCycleSection().setCommand(new UiCommand() {
            @Override
            public void run() {
                cycleEditorMode();
        } });

        deckGenParent = removeTab(VDeckgen.SINGLETON_INSTANCE);
        allDecksParent = removeTab(VAllDecks.SINGLETON_INSTANCE);

        if (this.controller.getModel() == null) {
            this.getDeckController().setModel(new Deck());
        }
        else {
            this.controller.refreshModel();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#canSwitchAway()
     */
    @Override
    public boolean canSwitchAway(boolean isClosing) {
        if (SEditorIO.confirmSaveChanges(FScreen.DECK_EDITOR_QUEST, isClosing)) {
            Singletons.getModel().getQuest().save();
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#resetUIChanges()
     */
    @Override
    public void resetUIChanges() {
        CSubmenuQuestDecks.SINGLETON_INSTANCE.update();
        //Re-add tabs
        if (deckGenParent != null) {
            deckGenParent.addDoc(VDeckgen.SINGLETON_INSTANCE);
        }
        if (allDecksParent != null) {
            allDecksParent.addDoc(VAllDecks.SINGLETON_INSTANCE);
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param d0
     */
    public void load(Deck deck) {
        controller.setModel(deck);
    }
}
