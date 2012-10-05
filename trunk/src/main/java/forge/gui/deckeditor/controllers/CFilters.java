package forge.gui.deckeditor.controllers;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import forge.Command;
import forge.deck.DeckBase;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.SFilterUtil;
import forge.gui.deckeditor.views.VFilters;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.FLabel;
import forge.item.CardPrinted;
import forge.item.InventoryItem;
import forge.item.ItemPredicate;

/** 
 * Controls the "filters" panel in the deck editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CFilters implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private boolean filtersAllEnabled = true;

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @SuppressWarnings("serial")
    @Override
    public void initialize() {
        final ItemListener iliFilter = new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent arg0) {
                if (!SFilterUtil.isFilteringPrevented()) {
                    buildFilter();
                }
            }
        };

        VFilters.SINGLETON_INSTANCE.getCbxSets().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxPLow().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxPHigh().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxTLow().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxTHigh().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxCMCLow().addItemListener(iliFilter);
        VFilters.SINGLETON_INSTANCE.getCbxCMCHigh().addItemListener(iliFilter);

        ((FLabel) VFilters.SINGLETON_INSTANCE.getBtnToggle()).setCommand(new Command() {
            @Override
            public void execute() {
                SFilterUtil.setPreventFiltering(true);
                toggleColorTypeSetFilter();
                SFilterUtil.setPreventFiltering(false);
                buildFilter();
            }
        });

        ((FLabel) VFilters.SINGLETON_INSTANCE.getBtnResetIntervals()).setCommand(new Command() {
            @Override
            public void execute() {
                SFilterUtil.setPreventFiltering(true);
                VFilters.SINGLETON_INSTANCE.getCbxPLow().setSelectedIndex(0);
                VFilters.SINGLETON_INSTANCE.getCbxTLow().setSelectedIndex(0);
                VFilters.SINGLETON_INSTANCE.getCbxCMCLow().setSelectedIndex(0);

                VFilters.SINGLETON_INSTANCE.getCbxPHigh().setSelectedIndex(
                        VFilters.SINGLETON_INSTANCE.getCbxPHigh().getItemCount() - 1);
                VFilters.SINGLETON_INSTANCE.getCbxTHigh().setSelectedIndex(
                        VFilters.SINGLETON_INSTANCE.getCbxTHigh().getItemCount() - 1);
                VFilters.SINGLETON_INSTANCE.getCbxCMCHigh().setSelectedIndex(
                        VFilters.SINGLETON_INSTANCE.getCbxCMCHigh().getItemCount() - 1);

                SFilterUtil.setPreventFiltering(false);
                buildFilter();
            }
        });

        ((FLabel) VFilters.SINGLETON_INSTANCE.getBtnResetText()).setCommand(new Command() {
            @Override
            public void execute() {
                VFilters.SINGLETON_INSTANCE.getTxfContains().setText("");
                VFilters.SINGLETON_INSTANCE.getTxfWithout().setText("");
                buildFilter();
            }
        });

        VFilters.SINGLETON_INSTANCE.getTxfContains().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.getKeyCode() == 10) { buildFilter(); }
            }
        });

        VFilters.SINGLETON_INSTANCE.getTxfWithout().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.getKeyCode() == 10) { buildFilter(); }
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

    /**
     * Clear filter button_action performed.
     * 
     * @param e
     *            the e
     */
    private <TItem extends InventoryItem, TModel extends DeckBase> void toggleColorTypeSetFilter() {
        VFilters.SINGLETON_INSTANCE.getCbxSets().setSelectedIndex(0);

        if (filtersAllEnabled) {
            filtersAllEnabled = false;
            SFilterUtil.toggleColorCheckboxes(false);
            SFilterUtil.toggleTypeCheckboxes(false);
        }
        else {
            filtersAllEnabled = true;
            SFilterUtil.toggleColorCheckboxes(true);
            SFilterUtil.toggleTypeCheckboxes(true);
        }
    }

    //===========

    /**
     * 
     * Assembles filter from the ones available. To prevent a block
     * of filters from being used, set its parent panel's visibility to false.
     * 
     * @param <TItem> &emsp; extends InventoryItem
     * @param <TModel> &emsp; extends DeckBase
     */
    @SuppressWarnings("unchecked")
    public <TItem extends InventoryItem, TModel extends DeckBase> void buildFilter() {
        // The main trick here is to apply a CardPrinted predicate
        // to the table. CardRules will lead to difficulties.

        final ACEditorBase<TItem, TModel> ed = (ACEditorBase<TItem, TModel>)
                CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        Predicate<? super CardPrinted> classFilter = Predicates.instanceOf(CardPrinted.class);
        Predicate<CardPrinted> color = SFilterUtil.buildColorFilter();
        Predicate<CardPrinted> type = SFilterUtil.buildTypeFilter();
        Predicate<CardPrinted> set = SFilterUtil.buildSetAndFormatFilter();
        Predicate<CardPrinted> text = SFilterUtil.buildTextFilter();
        Predicate<CardPrinted> interval = SFilterUtil.buildIntervalFilter();

        // Until this is filterable, always show packs and decks in the card shop.
        Predicate<? super CardPrinted> cardFilter = Predicates.and(classFilter, color, type, set, text, interval);

        Predicate<? super CardPrinted> itemFilter = Predicates.or(cardFilter, ItemPredicate.Presets.IS_PACK, ItemPredicate.Presets.IS_DECK);

        // Apply to table
        ed.getTableCatalog().setFilter((Predicate<TItem>) itemFilter);
    }
}
