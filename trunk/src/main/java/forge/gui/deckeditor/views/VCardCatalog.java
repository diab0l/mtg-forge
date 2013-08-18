package forge.gui.deckeditor.views;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.Pair;

import forge.Command;
import forge.card.CardRulesPredicates;
import forge.gui.WrapLayout;
import forge.gui.deckeditor.controllers.CCardCatalog;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSpinner;
import forge.gui.toolbox.FTextField;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.gui.toolbox.itemmanager.ItemManagerContainer;
import forge.gui.toolbox.itemmanager.SItemManagerUtil;
import forge.item.InventoryItem;
import forge.util.TextUtil;

/** 
 * Assembles Swing components of card catalog in deck editor.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 * 
 */
public enum VCardCatalog implements IVDoc<CCardCatalog> {
    /** */
    SINGLETON_INSTANCE;
    
    public static final int SEARCH_MODE_INVERSE_INDEX = 1;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Catalog");

    // panel where special instructions appear
    private final JPanel pnlHeader = new JPanel(new MigLayout("insets 0, gap 0, center"));
    private final FLabel lblTitle = new FLabel.Builder().fontSize(14).build();

    // Total and color count labels/filter toggles
    private final Dimension labelSize = new Dimension(60, 24);
    private final JPanel pnlStats = new JPanel(new WrapLayout(FlowLayout.LEFT));
    private final Map<SItemManagerUtil.StatTypes, FLabel> statLabels =
            new HashMap<SItemManagerUtil.StatTypes, FLabel>();

    // card transfer buttons
    private final JPanel pnlAddButtons =
            new JPanel(new MigLayout("insets 0, gap 0, ax center, hidemode 3"));
    private final FLabel btnAdd = new FLabel.Builder()
            .fontSize(14)
            .text("Add card")
            .tooltip("Add selected card to current deck (or double click the row or hit the spacebar)")
            .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_PLUS))
            .iconScaleAuto(false).hoverable().build();
    private final FLabel btnAdd4 = new FLabel.Builder()
            .fontSize(14)
            .text("Add 4 of card")
            .tooltip("Add up to 4 of selected card to current deck")
            .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_PLUS))
            .iconScaleAuto(false).hoverable().build();

    // restriction button and search widgets
    private final JPanel pnlSearch = new JPanel(new MigLayout("insets 0, gap 5px, center"));
    private final FLabel btnAddRestriction = new FLabel.ButtonBuilder()
            .text("Add filter")
            .tooltip("Click to add custom filters to the card list")
            .reactOnMouseDown().build();
    private final JComboBox<String> cbSearchMode = new JComboBox<String>();
    private final JTextField txfSearch = new FTextField.Builder().ghostText("Search").build();
    private final FLabel lblName = new FLabel.Builder().text("Name").hoverable().selectable().selected().build();
    private final FLabel lblType = new FLabel.Builder().text("Type").hoverable().selectable().selected().build();
    private final FLabel lblText = new FLabel.Builder().text("Text").hoverable().selectable().selected().build();
    private final JPanel pnlRestrictions = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));

    private final ItemManagerContainer itemManagerContainer = new ItemManagerContainer();
    private ItemManager<? extends InventoryItem> itemManager;
    
    // restriction widgets
    public static enum RangeTypes {
        CMC       (CardRulesPredicates.LeafNumber.CardField.CMC),
        POWER     (CardRulesPredicates.LeafNumber.CardField.POWER),
        TOUGHNESS (CardRulesPredicates.LeafNumber.CardField.TOUGHNESS);
        
        public final CardRulesPredicates.LeafNumber.CardField cardField;
        
        RangeTypes(CardRulesPredicates.LeafNumber.CardField cardField) {
            this.cardField = cardField;
        }

        public String toLabelString() {
            if (this == CMC) { return toString(); }
            return TextUtil.enumToLabel(this);
        }
    }
    
    private final Map<RangeTypes, Pair<FSpinner, FSpinner>> spinners = new HashMap<RangeTypes, Pair<FSpinner, FSpinner>>();
    
    //========== Constructor
    /** */
    private VCardCatalog() {
        pnlStats.setOpaque(false);
        
        for (SItemManagerUtil.StatTypes s : SItemManagerUtil.StatTypes.values()) {
            FLabel label = buildToggleLabel(s, SItemManagerUtil.StatTypes.TOTAL != s);
            statLabels.put(s, label);
            JComponent component = label;
            if (SItemManagerUtil.StatTypes.TOTAL == s) {
                label.setToolTipText("Total cards (click to toggle all filters)");
            } else if (SItemManagerUtil.StatTypes.PACK == s) {
                // wrap in a constant-size panel so we can change its visibility without affecting layout
                component = new JPanel(new MigLayout("insets 0, gap 0"));
                component.setPreferredSize(labelSize);
                component.setMinimumSize(labelSize);
                component.setOpaque(false);
                label.setVisible(false);
                component.add(label);
            }
            pnlStats.add(component);
        }
        
        pnlAddButtons.setOpaque(false);
        pnlAddButtons.add(btnAdd, "w 30%!, h 30px!, gap 10 10 5 5");
        pnlAddButtons.add(btnAdd4, "w 30%!, h 30px!, gap 10 10 5 5");
        
        pnlSearch.setOpaque(false);
        pnlSearch.add(btnAddRestriction, "center, w pref+8, h pref+8");
        pnlSearch.add(txfSearch, "pushx, growx");
        cbSearchMode.addItem("in");
        cbSearchMode.addItem("not in");
        pnlSearch.add(cbSearchMode, "center");
        pnlSearch.add(lblName, "w pref+8, h pref+8");
        pnlSearch.add(lblType, "w pref+8, h pref+8");
        pnlSearch.add(lblText, "w pref+8, h pref+8");

        pnlRestrictions.setOpaque(false);

        pnlHeader.setOpaque(false);
        pnlHeader.add(lblTitle, "center, gap 0 0 10 5");
        
        // fill spinner map
        for (RangeTypes t : RangeTypes.values()) {
            FSpinner lowerBound = new FSpinner.Builder().maxValue(10).build();
            FSpinner upperBound = new FSpinner.Builder().maxValue(10).build();
            _setupSpinner(lowerBound);
            _setupSpinner(upperBound);
            spinners.put(t, Pair.of(lowerBound, upperBound));
        }
    }

    private void _setupSpinner (JSpinner spinner) {
        spinner.setFocusable(false); // only the spinner text field should be focusable, not the up/down widget
    }
    
    //========== Overridden from IVDoc

    @Override
    public EDocID getDocumentID() {
        return EDocID.EDITOR_CATALOG;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CCardCatalog getLayoutControl() {
        return CCardCatalog.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    @Override
    public void populate() {
        JPanel parentBody = parentCell.getBody();
        parentBody.setLayout(new MigLayout("insets 0, gap 0, wrap, hidemode 3"));
        parentBody.add(pnlHeader, "w 98%!, gap 1% 1% 5 0");
        parentBody.add(pnlStats, "w 100:520:520, center");
        parentBody.add(pnlAddButtons, "w 96%!, gap 1% 1% 5 5");
        parentBody.add(pnlSearch, "w 96%, gap 1% 1%");
        parentBody.add(pnlRestrictions, "w 96%, gapleft 1%, gapright push");
        parentBody.add(itemManagerContainer, "w 98%!, h 100% - 35, gap 1% 0 0 1%");
    }
    
    public ItemManager<? extends InventoryItem> getItemManager() {
        return this.itemManager;
    }

    public void setItemManager(final ItemManager<? extends InventoryItem> itemManager0) {
        this.itemManager = itemManager0;
        itemManagerContainer.setItemManager(itemManager0);
    }

    //========== Accessor/mutator methods
    public JPanel getPnlHeader()     { return pnlHeader;     }
    public FLabel getLblTitle()      { return lblTitle;      }
    public JPanel getPnlAddButtons() { return pnlAddButtons; }
    public FLabel getBtnAdd()        { return btnAdd;        }
    public FLabel getBtnAdd4()       { return btnAdd4;       }
    public FLabel getLblName()       { return lblName;       }
    public FLabel getLblType()       { return lblType;       }
    public FLabel getLblText()       { return lblText;       }
    
    public FLabel getBtnAddRestriction()       { return btnAddRestriction; }
    public JComboBox<String> getCbSearchMode() { return cbSearchMode;      }
    public JTextField getTxfSearch()           { return txfSearch;         }

    public Map<SItemManagerUtil.StatTypes, FLabel> getStatLabels() {
        return statLabels;
    }
    public Map<RangeTypes, Pair<FSpinner, FSpinner>> getSpinners() {
        return spinners;
    }
    
    //========== Other methods
    private FLabel buildToggleLabel(SItemManagerUtil.StatTypes s, boolean selectable) {
        String tooltip;
        if (selectable) { //construct tooltip for selectable toggle labels, indicating click and right-click behavior
            String labelString = s.toLabelString();
            tooltip = labelString + " (click to toggle the filter, right-click to show only " + labelString.toLowerCase() + ")";
        }
        else { tooltip = ""; }

        FLabel label = new FLabel.Builder()
                .icon(s.img).iconScaleAuto(false)
                .fontSize(11)
                .tooltip(tooltip)
                .hoverable().selectable(selectable).selected(selectable)
                .build();
        
        label.setPreferredSize(labelSize);
        label.setMinimumSize(labelSize);
        
        return label;
    }
    
    @SuppressWarnings("serial")
    public void addRestrictionWidget(JComponent component, final Command onRemove) {
        final JPanel pnl = new JPanel(new MigLayout("insets 2, gap 2, h 30!"));

        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(1, 2, 1, 2, FSkin.getColor(FSkin.Colors.CLR_TEXT)));
        
        pnl.add(component, "h 30!, center");
        pnl.add(new FLabel.Builder().text("X").fontSize(10).hoverable(true)
                .tooltip("Remove filter").cmdClick(new Command() {
                    @Override
                    public void run() {
                        pnlRestrictions.remove(pnl);
                        refreshRestrictionWidgets();
                        onRemove.run();
                    }
                }).build(), "top");

        pnlRestrictions.add(pnl, "h 30!");
        refreshRestrictionWidgets();
    }
    
    public void refreshRestrictionWidgets() {
        Container parent = pnlRestrictions.getParent();
        pnlRestrictions.validate();
        parent.validate();
        parent.repaint();
    }
    
    public JPanel buildRangeRestrictionWidget(RangeTypes t) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, gap 2"));
        pnl.setOpaque(false);
        
        Pair<FSpinner, FSpinner> s = spinners.get(t);
        pnl.add(s.getLeft(), "w 45!, h 26!, center");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build(), "h 26!, center");
        pnl.add(new FLabel.Builder().text(t.toLabelString()).fontSize(11).build(), "h 26!, center");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build(), "h 26!, center");
        pnl.add(s.getRight(), "w 45!, h 26!, center");
        
        return pnl;
    }

    public FLabel buildPlainRestrictionWidget(String label, String tooltip) {
        return new FLabel.Builder().text(label).tooltip(tooltip).fontSize(11).build();
    }
}
