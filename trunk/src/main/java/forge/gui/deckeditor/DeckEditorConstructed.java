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
package forge.gui.deckeditor;

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import net.slightlymagic.braids.util.lambda.Lambda0;
import net.slightlymagic.maxmtg.Predicate;
import forge.AllZone;
import forge.Command;
import forge.deck.Deck;
import forge.error.ErrorViewer;
import forge.gui.deckeditor.elements.CardPanelHeavy;
import forge.gui.deckeditor.elements.FilterCheckBoxes;
import forge.gui.deckeditor.elements.FilterNameTypeSetPanel;
import forge.gui.deckeditor.elements.ManaCostRenderer;
import forge.gui.deckeditor.elements.TableColumnInfo;
import forge.gui.deckeditor.elements.TableView;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPool;

/**
 * <p>
 * Gui_DeckEditor class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class DeckEditorConstructed extends DeckEditorBase<CardPrinted, Deck> {
    /** Constant <code>serialVersionUID=130339644136746796L</code>. */
    private static final long serialVersionUID = 130339644136746796L;

    private final JButton removeButton = new JButton();
    private final JButton addButton = new JButton();
    private final JButton importButton = new JButton();

    private final JButton analysisButton = new JButton();
    private final JButton clearFilterButton = new JButton();

    private final JLabel jLabelAnalysisGap = new JLabel("");
    private FilterNameTypeSetPanel filterNameTypeSet;

    private final IDeckController<Deck> controller;
    /**
     * Show.
     * 
     * @param exitCommand
     *            the exit command
     */
    public void show(final Command exitCommand) {
        final Command exit = new Command() {
            private static final long serialVersionUID = 5210924838133689758L;

            @Override
            public void execute() {
                DeckEditorConstructed.this.dispose();
                exitCommand.execute();
            }
        };

        final MenuCommon menu = new MenuCommon(getController(), exit);
        this.setJMenuBar(menu);

        // do not change this!!!!
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent ev) {menu.close(); }
        });

        this.setup();
        
        this.controller.newModel();
        
        this.getTopTableWithCards().sort(1, true);
        this.getBottomTableWithCards().sort(1, true);

    } // show(Command)

    private void setup() {
        final List<TableColumnInfo<InventoryItem>> columns = new ArrayList<TableColumnInfo<InventoryItem>>();
        columns.add(new TableColumnInfo<InventoryItem>("Qty", 30, PresetColumns.FN_QTY_COMPARE, PresetColumns.FN_QTY_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Name", 175, PresetColumns.FN_NAME_COMPARE, PresetColumns.FN_NAME_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Cost", 75, PresetColumns.FN_COST_COMPARE, PresetColumns.FN_COST_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Color", 60, PresetColumns.FN_COLOR_COMPARE, PresetColumns.FN_COLOR_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Type", 100, PresetColumns.FN_TYPE_COMPARE, PresetColumns.FN_TYPE_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Stats", 60, PresetColumns.FN_STATS_COMPARE, PresetColumns.FN_STATS_GET));
        columns.add(new TableColumnInfo<InventoryItem>("R", 25, PresetColumns.FN_RARITY_COMPARE, PresetColumns.FN_RARITY_GET));
        columns.add(new TableColumnInfo<InventoryItem>("Set", 40, PresetColumns.FN_SET_COMPARE, PresetColumns.FN_SET_GET));
        columns.add(new TableColumnInfo<InventoryItem>("AI", 30, PresetColumns.FN_AI_STATUS_COMPARE, PresetColumns.FN_AI_STATUS_GET));
        columns.get(2).setCellRenderer(new ManaCostRenderer());

        this.getTopTableWithCards().setup(columns, this.getCardView());
        this.getBottomTableWithCards().setup(columns, this.getCardView());

        this.filterNameTypeSet.setListeners(new OnChangeTextUpdateDisplay(), this.getItemListenerUpdatesDisplay());

        this.setSize(1024, 740);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);

    }

    /**
     * Instantiates a new deck editor common.
     * 
     * @param gameType
     *            the game type
     */
    public DeckEditorConstructed() {
        try {
            this.setFilterBoxes(new FilterCheckBoxes(true));
            this.setTopTableWithCards(new TableView<CardPrinted>("Avaliable Cards", true, true, CardPrinted.class));
            this.setBottomTableWithCards(new TableView<CardPrinted>("Deck", true, CardPrinted.class));
            this.setCardView(new CardPanelHeavy());
            this.filterNameTypeSet = new FilterNameTypeSetPanel();

            this.jbInit();
        } catch (final Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        Lambda0<Deck> newCreator = new Lambda0<Deck>(){ @Override public Deck apply() { return new Deck(); } };
        controller = new DeckController<Deck>(AllZone.getDecks().getConstructed(), this, newCreator);
    }

    private void jbInit() {

        final Font fButtons = new java.awt.Font("Dialog", 0, 13);
        this.removeButton.setFont(fButtons);
        this.addButton.setFont(fButtons);
        this.importButton.setFont(fButtons);
        this.clearFilterButton.setFont(fButtons);
        this.analysisButton.setFont(fButtons);

        this.addButton.setText("Add to Deck");
        this.removeButton.setText("Remove from Deck");
        this.importButton.setText("Import a Deck");
        this.clearFilterButton.setText("Clear Filter");
        this.analysisButton.setText("Deck Analysis");

        this.removeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckEditorConstructed.this.removeButtonClicked(e);
            }
        });
        this.addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckEditorConstructed.this.addButtonActionPerformed(e);
            }
        });
        this.importButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckEditorConstructed.this.importButtonActionPerformed(e);
            }
        });
        this.clearFilterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckEditorConstructed.this.clearFilterButtonActionPerformed(e);
            }
        });
        this.analysisButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckEditorConstructed.this.analysisButtonActionPerformed(e);
            }
        });

        // Type filtering
        final Font f = new Font("Tahoma", Font.PLAIN, 10);
        for (final JCheckBox box : this.getFilterBoxes().getAllTypes()) {
                box.setFont(f);
            box.setOpaque(false);
        }

        // Color filtering
        for (final JCheckBox box : this.getFilterBoxes().getAllColors()) {
            box.setOpaque(false);
        }

        // Do not lower statsLabel any lower, we want this to be visible at 1024
        // x 768 screen size
        this.setTitle("Deck Editor");

        final Container content = this.getContentPane();
        final MigLayout layout = new MigLayout("fill");
        content.setLayout(layout);

        boolean isFirst = true;
        for (final JCheckBox box : this.getFilterBoxes().getAllTypes()) {
            String growParameter = "grow";
            if (isFirst) {
                growParameter = "cell 0 0, egx checkbox, grow, split 14";
                isFirst = false;
            }
            content.add(box, growParameter);
            box.addItemListener(this.getItemListenerUpdatesDisplay());
        }

        for (final JCheckBox box : this.getFilterBoxes().getAllColors()) {
            content.add(box, "grow");
            box.addItemListener(this.getItemListenerUpdatesDisplay());
        }

        content.add(this.clearFilterButton, "wmin 100, hmin 25, wmax 140, hmax 25, grow");

        content.add(this.filterNameTypeSet, "cell 0 1, grow");
        content.add(this.getTopTableWithCards().getTableDecorated(), "cell 0 2 1 2, pushy, grow");
        content.add(this.getTopTableWithCards().getLabel(), "cell 0 4");

        content.add(this.addButton, "w 100, h 49, sg button, cell 0 5, split 5");
        content.add(this.removeButton, "w 100, h 49, sg button");
        content.add(this.importButton, "w 100, h 49, sg button, gapleft 40px");
        // Label is used to push the analysis button to the right to separate
        // analysis button from add/remove card ones
        content.add(this.jLabelAnalysisGap, "wmin 75, growx");
        content.add(this.analysisButton, "w 100, h 49, wrap");

        content.add(this.getBottomTableWithCards().getTableDecorated(), "cell 0 6, grow");
        content.add(this.getBottomTableWithCards().getLabel(), "cell 0 7");

        content.add(this.getCardView(), "cell 1 0 1 8, flowy, grow");

        this.getTopTableWithCards().getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DeckEditorConstructed.this.addCardToDeck();
                }
            }
        });
        this.getTopTableWithCards().getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    DeckEditorConstructed.this.addCardToDeck();
                }
            }
        });

        // javax.swing.JRootPane rootPane = this.getRootPane();
        // rootPane.setDefaultButton(filterButton);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.deckeditor.DeckEditorBase#buildFilter()
     */
    @Override
    protected Predicate<CardPrinted> buildFilter() {
        final Predicate<CardPrinted> cardFilter = Predicate.and(this.getFilterBoxes().buildFilter(),
                this.filterNameTypeSet.buildFilter());
        return Predicate.instanceOf(cardFilter, CardPrinted.class);
    }

    /**
     * Clear filter button_action performed.
     * 
     * @param e
     *            the e
     */
    void clearFilterButtonActionPerformed(final ActionEvent e) {
        // disable automatic update triggered by listeners
        this.setFiltersChangeFiringUpdate(false);

        for (final JCheckBox box : this.getFilterBoxes().getAllTypes()) {
            if (!box.isSelected()) {
                box.doClick();
            }
        }
        for (final JCheckBox box : this.getFilterBoxes().getAllColors()) {
            if (!box.isSelected()) {
                box.doClick();
            }
        }

        this.filterNameTypeSet.clearFilters();

        this.setFiltersChangeFiringUpdate(true);

        this.getTopTableWithCards().setFilter(null);
    }

    /**
     * Adds the button_action performed.
     * 
     * @param e
     *            the e
     */
    void addButtonActionPerformed(final ActionEvent e) {
        this.addCardToDeck();
    }

    /**
     * Adds the card to deck.
     */
    void addCardToDeck() {
        final InventoryItem item = this.getTopTableWithCards().getSelectedCard();
        if ((item == null) || !(item instanceof CardPrinted)) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;
        this.getBottomTableWithCards().addCard(card);
        this.controller.notifyModelChanged();
    }

    /**
     * Removes the button clicked.
     * 
     * @param e
     *            the e
     */
    void removeButtonClicked(final ActionEvent e) {
        final InventoryItem item = this.getBottomTableWithCards().getSelectedCard();
        if ((item == null) || !(item instanceof CardPrinted)) {
            return;
        }

        final CardPrinted card = (CardPrinted) item;
        this.getBottomTableWithCards().removeCard(card);
        this.controller.notifyModelChanged();
    }

    /**
     * Import button_action performed.
     * 
     * @param e
     *            the e
     */
    void importButtonActionPerformed(final ActionEvent e) {
        final DeckEditorConstructed g = this;
        final DeckImport dImport = new DeckImport(g);
        dImport.setModalityType(ModalityType.APPLICATION_MODAL);
        dImport.setVisible(true);
    }


    /* (non-Javadoc)
     * @see forge.gui.deckeditor.DeckEditorBase#updateView()
     */
    @Override
    public void updateView() {
        // if constructed, can add the all cards above
        getTopTableWithCards().setDeck(ItemPool.createFrom(CardDb.instance().getAllCards(), CardPrinted.class));
        getBottomTableWithCards().setDeck(controller.getModel().getMain());
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.DeckEditorBase#getController()
     */
    @Override
    public IDeckController<Deck> getController() {
        return controller;
    }

}
