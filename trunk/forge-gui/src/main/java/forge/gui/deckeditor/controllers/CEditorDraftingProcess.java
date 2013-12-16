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

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import forge.Singletons;
import forge.card.CardEdition;
import forge.card.MagicColor;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckSection;
import forge.game.limited.BoosterDraft;
import forge.game.limited.IBoosterDraft;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.framework.FScreen;
import forge.gui.home.sanctioned.CSubmenuDraft;
import forge.gui.toolbox.itemmanager.CardManager;
import forge.gui.toolbox.itemmanager.table.SColumnUtil;
import forge.item.PaperCard;
import forge.item.InventoryItem;
import forge.util.ItemPoolView;

/**
 * Updates the deck editor UI as necessary draft selection mode.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id$
 */
public class CEditorDraftingProcess extends ACEditorBase<PaperCard, DeckGroup> {
    private IBoosterDraft boosterDraft;

    private String ccAddLabel = "Add card";
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;

    //========== Constructor

    /**
     * Updates the deck editor UI as necessary draft selection mode.
     */
    public CEditorDraftingProcess() {
        super(FScreen.DRAFTING_PROCESS);

        final CardManager catalogManager = new CardManager(false);
        final CardManager deckManager = new CardManager(false);

        catalogManager.setAlwaysNonUnique(true);
        deckManager.setAlwaysNonUnique(true);

        this.setCatalogManager(catalogManager);
        this.setDeckManager(deckManager);
    }

    /**
     * Show gui.
     * 
     * @param inBoosterDraft
     *            the in_booster draft
     */
    public final void showGui(final IBoosterDraft inBoosterDraft) {
        this.boosterDraft = inBoosterDraft;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof PaperCard) || toAlternate) {
            return;
        }

        final PaperCard card = (PaperCard) item;

        // can only draft one at a time, regardless of the requested quantity
        this.getDeckManager().addItem(card, 1);

        // get next booster pack
        this.boosterDraft.setChoice(card);

        if (this.boosterDraft.hasNextChoice()) {
            this.showChoices(this.boosterDraft.nextChoice());
        } else {
            this.boosterDraft.finishedDrafting();
            this.saveDraft();
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#removeCard()
     */
    @Override
    public void removeCard(InventoryItem item, boolean toAlternate, int qty) {
    }

    @Override
    public void buildAddContextMenu(ContextMenuBuilder cmb) {
        cmb.addMoveItems("Draft", "card", "cards", null);
        cmb.addTextFilterItem();
    }
    
    @Override
    public void buildRemoveContextMenu(ContextMenuBuilder cmb) {
        // no valid remove options
    }

    /**
     * <p>
     * showChoices.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     */
    private void showChoices(final ItemPoolView<PaperCard> list) {
        VCardCatalog.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);
        VCardCatalog.SINGLETON_INSTANCE.getLblTitle().setText("Select a card from pack number "
                + (((BoosterDraft) boosterDraft).getCurrentBoosterIndex() + 1) + ".");
        this.getCatalogManager().setPool(list);
        this.getCatalogManager().getTable().fixSelection(0);
    } // showChoices()

    /**
     * <p>
     * getPlayersDeck.
     * </p>
     * 
     * @return a {@link forge.deck.Deck} object.
     */
    private Deck getPlayersDeck() {
        final Deck deck = new Deck();

        // add sideboard to deck
        CardPool side = deck.getOrCreate(DeckSection.Sideboard);
        side.addAll(this.getDeckManager().getPool());

        final CardEdition landSet = IBoosterDraft.LAND_SET_CODE[0];
        final int landsCount = 20;
        for(String landName : MagicColor.Constant.BASIC_LANDS) {
            side.add(Singletons.getMagicDb().getCommonCards().getCard(landName, landSet.getCode()), landsCount);
        }

        return deck;
    } // getPlayersDeck()

    /**
     * <p>
     * saveDraft.
     * </p>
     */
    private void saveDraft() {
        String s = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                "Save this draft as:",
                "Save draft",
                JOptionPane.QUESTION_MESSAGE);

        // Cancel button will be null; OK will return string.
        // Must check for null value first, then string length.
        // Recurse, if either null or empty string.
        if (s == null || s.length() == 0) {
            saveDraft();
            return;
        }

        // Check for overwrite case
        for (DeckGroup d : Singletons.getModel().getDecks().getDraft()) {
            if (s.equalsIgnoreCase(d.getName())) {
                final int m = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
                        "There is already a deck named '" + s + "'. Overwrite?",
                        "Overwrite Deck?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                // If no overwrite, recurse.
                if (m == JOptionPane.NO_OPTION) {
                    saveDraft();
                    return;
                }
                break;
            }
        }

        // Construct computer's decks and save draft
        final Deck[] computer = this.boosterDraft.getDecks();

        final DeckGroup finishedDraft = new DeckGroup(s);
        finishedDraft.setHumanDeck((Deck) this.getPlayersDeck().copyTo(s));
        finishedDraft.addAiDecks(computer);

        Singletons.getModel().getDecks().getDraft().add(finishedDraft);
        CSubmenuDraft.SINGLETON_INSTANCE.update();

        Singletons.getControl().setCurrentScreen(FScreen.DECK_EDITOR_DRAFT);
        CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(new CEditorLimited(Singletons.getModel().getDecks().getDraft(), FScreen.DECK_EDITOR_DRAFT));
        CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController().getDeckController().load(s);
        FScreen.DRAFTING_PROCESS.close();
    }

    //========== Overridden from ACEditorBase

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#getController()
     */
    @Override
    public DeckController<DeckGroup> getDeckController() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#updateView()
     */
    @Override
    public void resetTables() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.ACEditorBase#show(forge.Command)
     */
    @Override
    public void update() {
        this.getCatalogManager().getTable().setup(SColumnUtil.getCatalogDefaultColumns());
        this.getDeckManager().getTable().setup(SColumnUtil.getDeckDefaultColumns());

        ccAddLabel = this.getBtnAdd().getText();
        this.getBtnAdd().setText("Choose Card");

        if (this.getDeckManager().getPool() == null) { //avoid showing next choice or resetting pool if just switching back to Draft screen 
            this.showChoices(this.boosterDraft.nextChoice());
            this.getDeckManager().setPool((Iterable<InventoryItem>) null);
        }
        else {
            this.showChoices(this.getCatalogManager().getPool());
        }

        //Remove buttons
        this.getBtnAdd4().setVisible(false);
        this.getBtnRemove().setVisible(false);
        this.getBtnRemove4().setVisible(false);

        this.getBtnCycleSection().setVisible(false);

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(false);

        deckGenParent = removeTab(VDeckgen.SINGLETON_INSTANCE);
        allDecksParent = removeTab(VAllDecks.SINGLETON_INSTANCE);
        
        // set catalog table to single-selection only mode
        getCatalogManager().getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#canSwitchAway()
     */
    @Override
    public boolean canSwitchAway(boolean isClosing) {
        return true;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#resetUIChanges()
     */
    @Override
    public void resetUIChanges() {
        //Re-rename buttons
        this.getBtnAdd().setText(ccAddLabel);

        //Re-add buttons
        this.getBtnAdd4().setVisible(true);
        this.getBtnRemove().setVisible(true);
        this.getBtnRemove4().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);

        //Re-add tabs
        if (deckGenParent != null) {
            deckGenParent.addDoc(VDeckgen.SINGLETON_INSTANCE);
        }
        if (allDecksParent != null) {
            allDecksParent.addDoc(VAllDecks.SINGLETON_INSTANCE);
        }
        
        // set catalog table back to free-selection mode
        getCatalogManager().getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
}
