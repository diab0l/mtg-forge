package forge.gui.home.gauntlet;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.gauntlet.GauntletIO;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FList;
import forge.gui.toolbox.FRadioButton;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTextArea;
import forge.gui.toolbox.FTextField;

/** 
 * Assembles Swing components of "build gauntlet" submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuGauntletBuild implements IVSubmenu {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Gauntlet Builder");

    // Other fields
    private final FLabel lblTitle     = new FLabel.Builder()
        .text("Gauntlet Builder").fontAlign(SwingConstants.CENTER)
        .opaque(true).fontSize(16).build();

    private final JPanel pnlFileHandling = new JPanel(new MigLayout("insets 0, gap 0, align center"));
    private final JPanel pnlRadios = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JPanel pnlButtons = new JPanel();
    private final JPanel pnlStrut = new JPanel();
    private final JPanel pnlDirections = new JPanel();

    private final JRadioButton radUserDecks = new FRadioButton("Custom user decks");
    private final JRadioButton radQuestDecks = new FRadioButton("Quest Decks");
    private final JRadioButton radColorDecks = new FRadioButton("Fully random color decks");
    private final JRadioButton radThemeDecks = new FRadioButton("Semi-random theme decks");

    private final JList lstLeft = new FList();
    private final JList lstRight = new FList();

    private final JScrollPane scrLeft  = new FScrollPane(lstLeft,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final JScrollPane scrRight  = new FScrollPane(lstRight,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final JTextField txfFilename = new FTextField();

    private final FTextArea tarDesc1 = new FTextArea(
            "Use the left or right arrows to add or remove decks from the gauntlet.");

    private final FTextArea tarDesc2 = new FTextArea(
            "Change the order of the opponents by using the up and down arrows.");

    private final JLabel lblSave = new FLabel.Builder().text("Changes not yet saved.")
            .build();

    private final FLabel btnUp = new FLabel.Builder()
        .tooltip("Move this deck up in the gauntlet").hoverable(true)
        .iconScaleAuto(true).iconScaleFactor(1.0)
        .icon(new ImageIcon(FSkin.getImage(FSkin.LayoutImages.IMG_CUR_T))).build();

    private final FLabel btnDown = new FLabel.Builder()
        .tooltip("Move this deck down in the gauntlet").hoverable(true)
        .iconScaleAuto(true).iconScaleFactor(1.0)
        .icon(new ImageIcon(FSkin.getImage(FSkin.LayoutImages.IMG_CUR_B))).build();

    private final FLabel btnRight = new FLabel.Builder()
        .tooltip("Add this deck to the gauntlet").hoverable(true)
        .iconScaleAuto(true).iconScaleFactor(1.0)
        .icon(new ImageIcon(FSkin.getImage(FSkin.LayoutImages.IMG_CUR_R))).build();

    private final FLabel btnLeft = new FLabel.Builder()
        .tooltip("Remove this deck from the gauntlet").hoverable(true)
        .iconScaleAuto(true).iconScaleFactor(1.0)
        .icon(new ImageIcon(FSkin.getImage(FSkin.LayoutImages.IMG_CUR_L))).build();

    private final FLabel btnSave = new FLabel.Builder()
        .fontSize(14)
        .tooltip("Save this gauntlet")
        .iconInBackground(true)
        .iconAlignX(SwingConstants.CENTER).iconAlpha(1.0f)
        .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_SAVE))
        .text(" ").hoverable(true).build();

    private final FLabel btnNew = new FLabel.Builder()
        .fontSize(14)
        .tooltip("Build a new gauntlet")
        .iconInBackground(true)
        .iconAlignX(SwingConstants.CENTER).iconAlpha(1.0f)
        .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_NEW))
        .text(" ").hoverable(true).build();

    private final FLabel btnOpen = new FLabel.Builder()
        .fontSize(14)
        .tooltip("Load a gauntlet")
        .iconInBackground(true)
        .iconAlignX(SwingConstants.CENTER).iconAlpha(1.0f)
        .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_OPEN))
        .text(" ").hoverable(true).build();

    private final JTextField txfSearch = new FTextField();

    private VSubmenuGauntletBuild() {
        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // Radio button grouping
        final ButtonGroup grpRadios = new ButtonGroup();
        grpRadios.add(radUserDecks);
        grpRadios.add(radQuestDecks);
        grpRadios.add(radColorDecks);
        grpRadios.add(radThemeDecks);

        // Text areas
        txfFilename.setText(GauntletIO.TXF_PROMPT);
        txfFilename.setMargin(new Insets(5, 5, 5, 5));
        txfFilename.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        txfFilename.setOpaque(true);
        txfFilename.setEditable(true);
        txfFilename.setFocusable(true);
        txfFilename.setOpaque(true);
        txfFilename.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // File handling panel
        final FLabel lblFilename = new FLabel.Builder()
            .text("Gauntlet Name:").fontSize(14).build();
        pnlFileHandling.setOpaque(false);
        pnlFileHandling.add(lblFilename, "h 30px!, gap 0 5px 0");
        pnlFileHandling.add(txfFilename, "h 30px!, w 200px!, gap 0 5px 0 0");
        pnlFileHandling.add(btnSave, "h 30px!, w 30px!, gap 0 5px 0 0");
        pnlFileHandling.add(btnNew, "h 30px!, w 30px!, gap 0 5px 0 0");
        pnlFileHandling.add(btnOpen, "h 30px!, w 30px!, gap 0 5px 0 0");

        // Radio button panel
        txfSearch.setText("Search");
        txfSearch.setMargin(new Insets(5, 5, 5, 5));
        txfSearch.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        txfSearch.setOpaque(true);
        txfSearch.setEditable(true);
        txfSearch.setFocusable(true);
        txfSearch.setOpaque(true);
        txfSearch.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        pnlRadios.setOpaque(false);
        pnlRadios.add(radUserDecks, "h 30px!, gap 0 0 0 5px");
        pnlRadios.add(radQuestDecks, "h 30px!, gap 0 0 0 5px");
        pnlRadios.add(radColorDecks, "h 30px!, gap 0 0 0 5px");
        pnlRadios.add(radThemeDecks, "h 30px!, gap 0 0 0 5px");
        //pnlRadios.add(txfSearch, "h 30px!, w 100%!, gap 0 0 0 5px");

        // Directions panel
        final JPanel pnlSpacer = new JPanel();
        pnlSpacer.setOpaque(false);
        tarDesc1.setFont(FSkin.getFont(14));
        tarDesc2.setFont(FSkin.getFont(14));

        pnlStrut.setOpaque(false);
        lblSave.setForeground(Color.red);
        lblSave.setVisible(false);
        pnlDirections.setOpaque(false);
        pnlDirections.setLayout(new MigLayout("insets 0, gap 0, wrap"));
        pnlDirections.add(pnlSpacer, "pushy, growy");
        pnlDirections.add(tarDesc1, "w 98%!, gap 1% 0 0 10px");
        pnlDirections.add(tarDesc2, "w 98%!, gap 1% 0 0 20px");
        pnlDirections.add(lblSave, "ax center, gap 0 0 0 5px");

        // Deck movement panel
        pnlButtons.setLayout(new MigLayout("insets 0, gap 0, wrap, ay center"));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnRight, "h 40px!, w 100%!");
        pnlButtons.add(btnLeft, "h 40px!, w 100%!");
        pnlButtons.add(btnUp, "h 40px!, w 100%!, gap 0 0 50px 0, ay bottom");
        pnlButtons.add(btnDown, "h 40px!, w 100%!, ay baseline");
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getGroupEnum()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.GAUNTLET;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Build A Gauntlet";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_GAUNTLETBUILD;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#populate()
     */
    @Override
    public void populate() {
        parentCell.getBody().setLayout(new MigLayout("insets 0, gap 0, wrap 3"));
        parentCell.getBody().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px, span 3");
        parentCell.getBody().add(pnlFileHandling, "w 98%!, gap 1% 0 1% 5px, span 3");

        parentCell.getBody().add(pnlRadios, "w 48% - 20px!, gap 1% 0 0 15px");
        parentCell.getBody().add(pnlStrut, "w 40px!, gap 1% 1% 0 15px");
        parentCell.getBody().add(pnlDirections, "w 48% - 20px!, gap 0 0 0 15px");

        parentCell.getBody().add(scrLeft, "w 48% - 20px!, gap 1% 0 0 15px, pushy, growy");
        parentCell.getBody().add(pnlButtons, "w 40px!, gap 1% 1% 0 15px, pushy, growy");
        parentCell.getBody().add(scrRight, "w 48% - 20px!, gap 0 0 0 15px, pushy, growy");
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstLeft() {
        return this.lstLeft;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstRight() {
        return this.lstRight;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadUserDecks() {
        return this.radUserDecks;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadQuestDecks() {
        return this.radQuestDecks;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadColorDecks() {
        return this.radColorDecks;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadThemeDecks() {
        return this.radThemeDecks;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnUp() {
        return btnUp;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnDown() {
        return btnDown;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnRight() {
        return btnRight;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnLeft() {
        return btnLeft;
    }

    /** @return {@link javax.swing.JLabel} */
    public FLabel getBtnSave() {
        return btnSave;
    }

    /** @return {@link javax.swing.JLabel} */
    public FLabel getBtnOpen() {
        return btnOpen;
    }

    /** @return {@link javax.swing.JLabel} */
    public FLabel getBtnNew() {
        return btnNew;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getLblSave() {
        return lblSave;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public JTextField getTxfSearch() {
        return this.txfSearch;
    }

    /** @return {@link javax.swing.JTextField} */
    public JTextField getTxfFilename() {
        return txfFilename;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_GAUNTLETBUILD;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public ICDoc getLayoutControl() {
        return CSubmenuGauntletBuild.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return parentCell;
    }
}
