package forge.gui.deckeditor.views;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.Pair;

import forge.Command;
import forge.card.CardRulesPredicates;
import forge.gui.WrapLayout;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.controllers.CCardCatalog;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSpinner;
import forge.gui.toolbox.FTextField;
import forge.gui.toolbox.ToolTipListener;
import forge.util.TextUtil;

/** 
 * Assembles Swing components of card catalog in deck editor.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 * 
 */
public enum VCardCatalog implements IVDoc<CCardCatalog>, ITableContainer {
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
    private final Map<SEditorUtil.StatTypes, FLabel> statLabels =
            new HashMap<SEditorUtil.StatTypes, FLabel>();

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
    private final JComboBox cbSearchMode = new JComboBox();
    private final JTextField txfSearch = new FTextField.Builder().build();
    private final FLabel lblName = new FLabel.Builder().text("Name").hoverable().selectable().selected().build();
    private final FLabel lblType = new FLabel.Builder().text("Type").hoverable().selectable().selected().build();
    private final FLabel lblText = new FLabel.Builder().text("Text").hoverable().selectable().selected().build();
    private final JPanel pnlRestrictions = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
    
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
    
    // card table
    private JTable tblCards = null;
    private final JScrollPane scroller = new JScrollPane();

    
    //========== Constructor
    /** */
    private VCardCatalog() {
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(null);
        scroller.getViewport().setBorder(null);
        scroller.getVerticalScrollBar().addAdjustmentListener(new ToolTipListener());

        pnlStats.setOpaque(false);
        
        for (SEditorUtil.StatTypes s : SEditorUtil.StatTypes.values()) {
            FLabel label = buildToggleLabel(s, SEditorUtil.StatTypes.TOTAL != s);
            statLabels.put(s, label);
            JComponent component = label;
            if (SEditorUtil.StatTypes.TOTAL == s) {
                label.setToolTipText("Total cards (click to toggle all filters)");
            } else if (SEditorUtil.StatTypes.PACK == s) {
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
        txfSearch.addFocusListener(_selectAllOnFocus);
        pnlSearch.add(txfSearch, "pushx, growx");
        cbSearchMode.addItem("in");
        cbSearchMode.addItem("not in");
        pnlSearch.add(cbSearchMode, "center");
        pnlSearch.add(lblName, "w pref+8, h pref+4");
        pnlSearch.add(lblType, "w pref+8, h pref+4");
        pnlSearch.add(lblText, "w pref+8, h pref+4");

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
        JTextField t = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        t.addFocusListener(_selectAllOnFocus);
    }
    
    private final FocusListener _selectAllOnFocus = new FocusAdapter() {
        @Override public void focusGained(final FocusEvent e) {
            SwingUtilities.invokeLater(new Runnable() { @Override public void run() { ((JTextField)e.getComponent()).selectAll(); } });
        }
    };
    
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
        parentBody.add(scroller, "w 98%!, h 100% - 35, gap 1% 0 0 1%");
    }

    //========== Overridden from ITableContainer
    @Override
    public void setTableView(final JTable tbl0) {
        this.tblCards = tbl0;
        scroller.setViewportView(tblCards);
    }

    @Override
    public FLabel getStatLabel(SEditorUtil.StatTypes s) {
        return statLabels.get(s);
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
    
    public FLabel getBtnAddRestriction() { return btnAddRestriction; }
    public JComboBox getCbSearchMode()   { return cbSearchMode;      }
    public JTextField getTxfSearch()     { return txfSearch;         }

    public Map<SEditorUtil.StatTypes, FLabel> getStatLabels() {
        return statLabels;
    }
    public Map<RangeTypes, Pair<FSpinner, FSpinner>> getSpinners() {
        return spinners;
    }
    
    //========== Other methods
    private FLabel buildToggleLabel(SEditorUtil.StatTypes s, boolean selectable) {
        FLabel label = new FLabel.Builder()
                .icon(s.img).iconScaleAuto(false)
                .fontSize(11)
                .tooltip(s.toLabelString() + " (click to toggle the filter)")
                .hoverable().selectable(selectable).selected(selectable)
                .build();
        
        label.setPreferredSize(labelSize);
        label.setMinimumSize(labelSize);
        
        return label;
    }

    public void focusTable() {
        if (null != tblCards) {
            tblCards.requestFocusInWindow();
            
            if (0 < tblCards.getRowCount()) {
                tblCards.changeSelection(0, 0, false, false);
            }
        }
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
