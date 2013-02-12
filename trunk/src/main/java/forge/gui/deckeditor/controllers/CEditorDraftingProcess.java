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
import javax.swing.SwingUtilities;

import forge.Singletons;
import forge.control.FControl;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckSection;
import forge.game.limited.BoosterDraft;
import forge.game.limited.IBoosterDraft;
import forge.gui.deckeditor.tables.DeckController;
import forge.gui.deckeditor.tables.EditorTableView;
import forge.gui.deckeditor.tables.SColumnUtil;
import forge.gui.deckeditor.views.VAllDecks;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.deckeditor.views.VDeckgen;
import forge.gui.framework.DragCell;
import forge.gui.framework.SRearrangingUtil;
import forge.gui.home.sanctioned.CSubmenuDraft;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPoolView;
import forge.view.FView;

/**
 * Updates the deck editor UI as necessary draft selection mode.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id$
 */
public class CEditorDraftingProcess extends ACEditorBase<CardPrinted, DeckGroup> {
    private IBoosterDraft boosterDraft;

    private String ccAddLabel = "Add card";
    private DragCell allDecksParent = null;
    private DragCell deckGenParent = null;

    //========== Constructor

    /**
     * Updates the deck editor UI as necessary draft selection mode.
     */
    public CEditorDraftingProcess() {
        final EditorTableView<CardPrinted> tblCatalog = new EditorTableView<CardPrinted>(false, CardPrinted.class);
        final EditorTableView<CardPrinted> tblDeck = new EditorTableView<CardPrinted>(false, CardPrinted.class);

        VCardCatalog.SINGLETON_INSTANCE.setTableView(tblCatalog.getTable());
        VCurrentDeck.SINGLETON_INSTANCE.setTableView(tblDeck.getTable());

        tblCatalog.setAlwaysNonUnique(true);
        tblDeck.setAlwaysNonUnique(true);

        this.setTableCatalog(tblCatalog);
        this.setTableDeck(tblDeck);
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

    /**
     * <p>
     * setup.
     * </p>
     */
    private void setup() {
        this.getTableCatalog().setup(VCardCatalog.SINGLETON_INSTANCE, SColumnUtil.getCatalogDefaultColumns());
        this.getTableDeck().setup(VCurrentDeck.SINGLETON_INSTANCE, SColumnUtil.getDeckDefaultColumns());

        /*
        this.getTableCatalog().getTable().addMouseListener(this.pickWithMouse);
        this.getTableCatalog().getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    CEditorDraftingProcess.this.addCard();
                }
            }
        });
        */

        ccAddLabel = VCardCatalog.SINGLETON_INSTANCE.getBtnAdd().getText();
        VCardCatalog.SINGLETON_INSTANCE.getBtnAdd().setText("Choose Card");

    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard(InventoryItem item, int qty) {
        if ((item == null) || !(item instanceof CardPrinted)) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;

        this.getTableDeck().addCard(card, qty);

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
    public void removeCard(InventoryItem item, int qty) {
    }

    /**
     * <p>
     * showChoices.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     */
    private void showChoices(final ItemPoolView<CardPrinted> list) {
        VCardCatalog.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);
        VCardCatalog.SINGLETON_INSTANCE.getLblTitle().setText("Select a card from pack number "
                + (((BoosterDraft) boosterDraft).getCurrentBoosterIndex() + 1) + ".");
        this.getTableCatalog().setDeck(list);
        this.getTableCatalog().fixSelection(0);
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
        side.addAll(this.getTableDeck().getCards());

        final String landSet = IBoosterDraft.LAND_SET_CODE[0];
        final int landsCount = 20;
        side.add(CardDb.instance().getCard("Forest", landSet), landsCount);
        side.add(CardDb.instance().getCard("Mountain", landSet), landsCount);
        side.add(CardDb.instance().getCard("Swamp", landSet), landsCount);
        side.add(CardDb.instance().getCard("Island", landSet), landsCount);
        side.add(CardDb.instance().getCard("Plains", landSet), landsCount);

        return deck;
    } // getPlayersDeck()

    /**
     * <p>
     * saveDraft.
     * </p>
     */
    private void saveDraft() {
        String s = JOptionPane.showInputDialog(null,
                "Save this draft as:",
                "Save draft",
                JOptionPane.QUESTION_MESSAGE);

        // Cancel button will be null; OK will return string.
        // Must check for null value first, then string length.
        if (s != null) {
            // Recurse, if empty string.
            if (s.length() == 0) {
                saveDraft();
                return;
            }

            // Check for overwrite case
            for (DeckGroup d : Singletons.getModel().getDecks().getDraft()) {
                if (s.equalsIgnoreCase(d.getName())) {
                    final int m = JOptionPane.showConfirmDialog(null,
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
        }

        FControl.SINGLETON_INSTANCE.changeState(FControl.HOME_SCREEN);
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
    public void init() {
        this.setup();
        this.showChoices(this.boosterDraft.nextChoice());
        this.getTableDeck().setDeck((Iterable<InventoryItem>) null);

        //Remove buttons
        VCardCatalog.SINGLETON_INSTANCE.getBtnAdd4().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnRemove().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnRemove4().setVisible(false);

        VCurrentDeck.SINGLETON_INSTANCE.getBtnDoSideboard().setVisible(false);

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(false);

        if (VDeckgen.SINGLETON_INSTANCE.getParentCell() != null) {
            deckGenParent = VDeckgen.SINGLETON_INSTANCE.getParentCell();
            deckGenParent.removeDoc(VDeckgen.SINGLETON_INSTANCE);
            VDeckgen.SINGLETON_INSTANCE.setParentCell(null);

            // If Deck Gen was first tab, the new first tab needs re-selecting.
            if (deckGenParent.getDocs().size() > 0) {
                deckGenParent.setSelected(deckGenParent.getDocs().get(0));
            }
        }
        if (VAllDecks.SINGLETON_INSTANCE.getParentCell() != null) {
            allDecksParent = VAllDecks.SINGLETON_INSTANCE.getParentCell();
            allDecksParent.removeDoc(VAllDecks.SINGLETON_INSTANCE);
            VAllDecks.SINGLETON_INSTANCE.setParentCell(null);

            // If All Decks was first tab, the new first tab needs re-selecting.
            if (allDecksParent.getDocs().size() > 0) {
                allDecksParent.setSelected(allDecksParent.getDocs().get(0));
            }
        }

        // Fill in gaps
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (final DragCell c : FView.SINGLETON_INSTANCE.getDragCells()) {
                    if (c.getDocs().size() == 0) {
                        SRearrangingUtil.fillGap(c);
                        FView.SINGLETON_INSTANCE.removeDragCell(c);
                    }
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#exit()
     */
    @Override
    public boolean exit() {
        CSubmenuDraft.SINGLETON_INSTANCE.update();

        //Re-rename buttons
        VCardCatalog.SINGLETON_INSTANCE.getBtnAdd().setText(ccAddLabel);

        //Re-add buttons
        VCardCatalog.SINGLETON_INSTANCE.getBtnAdd4().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnRemove().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnRemove4().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);

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
