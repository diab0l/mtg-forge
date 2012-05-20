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

import forge.Constant;
import forge.Singletons;
import forge.control.FControl;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.game.limited.BoosterDraft;
import forge.game.limited.IBoosterDraft;
import forge.gui.deckeditor.tables.DeckController;
import forge.gui.deckeditor.tables.SColumnUtil;
import forge.gui.deckeditor.tables.TableView;
import forge.gui.deckeditor.views.VCardCatalog;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.home.sanctioned.CSubmenuDraft;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPoolView;

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

    //========== Constructor

    /**
     * Updates the deck editor UI as necessary draft selection mode.
     */
    public CEditorDraftingProcess() {
        final TableView<CardPrinted> tblCatalog = new TableView<CardPrinted>(true, CardPrinted.class);
        final TableView<CardPrinted> tblDeck = new TableView<CardPrinted>(true, CardPrinted.class);

        VCardCatalog.SINGLETON_INSTANCE.setTableView(tblCatalog.getTable());
        VCurrentDeck.SINGLETON_INSTANCE.setTableView(tblDeck.getTable());

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
        this.init();
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

    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard() {
        final InventoryItem item = this.getTableCatalog().getSelectedCard();
        if ((item == null) || !(item instanceof CardPrinted)) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;

        this.getTableDeck().addCard(card);

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
    public void removeCard() {
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
        Constant.Runtime.HUMAN_DECK[0] = deck;

        // add sideboard to deck
        deck.getSideboard().addAll(this.getTableDeck().getCards());

        final String landSet = IBoosterDraft.LAND_SET_CODE[0];
        final int landsCount = 20;
        deck.getSideboard().add(CardDb.instance().getCard("Forest", landSet), landsCount);
        deck.getSideboard().add(CardDb.instance().getCard("Mountain", landSet), landsCount);
        deck.getSideboard().add(CardDb.instance().getCard("Swamp", landSet), landsCount);
        deck.getSideboard().add(CardDb.instance().getCard("Island", landSet), landsCount);
        deck.getSideboard().add(CardDb.instance().getCard("Plains", landSet), landsCount);

        return deck;
    } // getPlayersDeck()

    /**
     * <p>
     * saveDraft.
     * </p>
     */
    private void saveDraft() {
        String s = "";
        while ((s == null) || (s.length() == 0)) {
            s = JOptionPane.showInputDialog(null,
                    "Save this draft as:",
                    "Save draft",
                    JOptionPane.QUESTION_MESSAGE);
        }
        // TODO: check if overwriting the same name, and let the user delete old
        // drafts

        // construct computer's decks
        // save draft
        final Deck[] computer = this.boosterDraft.getDecks();

        final DeckGroup finishedDraft = new DeckGroup(s);
        finishedDraft.setHumanDeck((Deck) this.getPlayersDeck().copyTo(s));
        finishedDraft.addAiDecks(computer);

        // DeckManager deckManager = new
        // DeckManager(ForgeProps.getFile(NEW_DECKS));

        // write the file
        Singletons.getModel().getDecks().getDraft().add(finishedDraft);

        CSubmenuDraft.SINGLETON_INSTANCE.update();
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
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.controllers.ACEditorBase#exit()
     */
    @Override
    public boolean exit() {
        CSubmenuDraft.SINGLETON_INSTANCE.update();
        return true;
    }
}
