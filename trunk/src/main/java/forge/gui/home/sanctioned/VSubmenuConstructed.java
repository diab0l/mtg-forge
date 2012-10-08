package forge.gui.home.sanctioned;

import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.StartButton;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FList;
import forge.gui.toolbox.FRadioButton;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;

/** 
 * Assembles Swing components of constructed submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuConstructed implements IVSubmenu {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Constructed Mode");

    /** */
    private final FLabel lblTitle = new FLabel.Builder()
        .text("Sanctioned Format: Constructed").fontAlign(SwingConstants.CENTER)
        .fontSize(16).opaque(true).build();

    private final FLabel lblDecklist = new FLabel.Builder()
        .text("Double click a non-random deck for its decklist.")
        .fontSize(12).build();

    private final JPanel pnlDecksHuman = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JPanel pnlDecksAI = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JPanel pnlRadiosHuman = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JPanel pnlRadiosAI = new JPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final JPanel pnlStart = new JPanel(new MigLayout("insets 0, gap 0, wrap 2"));

    private final StartButton btnStart  = new StartButton();
    private final JList lstHumanDecks   = new FList();
    private final JList lstAIDecks      = new FList();

    private final JRadioButton radColorsHuman = new FRadioButton("Fully random color deck");
    private final JRadioButton radThemesHuman = new FRadioButton("Semi-random theme deck");
    private final JRadioButton radCustomHuman = new FRadioButton("Custom user deck");
    private final JRadioButton radQuestsHuman = new FRadioButton("Quest opponent deck");

    private final JRadioButton radColorsAI = new FRadioButton("Fully random color deck");
    private final JRadioButton radThemesAI = new FRadioButton("Semi-random theme deck");
    private final JRadioButton radCustomAI = new FRadioButton("Custom user deck");
    private final JRadioButton radQuestsAI = new FRadioButton("Quest opponent deck");

    private final JCheckBox cbSingletons = new FCheckBox("Singleton Mode");
    private final JCheckBox cbArtifacts = new FCheckBox("Remove Artifacts");
    private final JCheckBox cbRemoveSmall = new FCheckBox("Remove Small Creatures");

    private final JScrollPane scrHumanDecks  = new FScrollPane(lstHumanDecks,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final JScrollPane scrAIDecks  = new FScrollPane(lstAIDecks,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final FLabel btnHumanRandom = new FLabel.Builder().text("Random").fontSize(14).opaque(true)
            .hoverable(true).build();

    private final FLabel btnAIRandom = new FLabel.Builder().text("Random").fontSize(14).opaque(true)
            .hoverable(true).build();

    private VSubmenuConstructed() {
        // Radio button group: Human
        final ButtonGroup grpRadiosHuman = new ButtonGroup();
        grpRadiosHuman.add(radCustomHuman);
        grpRadiosHuman.add(radQuestsHuman);
        grpRadiosHuman.add(radColorsHuman);
        grpRadiosHuman.add(radThemesHuman);

        // Radio button group: AI
        final ButtonGroup grpRadiosAI = new ButtonGroup();
        grpRadiosAI.add(radCustomAI);
        grpRadiosAI.add(radQuestsAI);
        grpRadiosAI.add(radColorsAI);
        grpRadiosAI.add(radThemesAI);

        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // Deck scrollers
        pnlDecksHuman.setOpaque(false);
        pnlDecksHuman.add(scrHumanDecks, "w 100%!, pushy, growy");

        pnlDecksAI.setOpaque(false);
        pnlDecksAI.add(scrAIDecks, "w 100%!, pushy, growy");

        // Radio button panels: Human and AI
        final String strRadioConstraints = "w 100%!, h 30px!";

        pnlRadiosHuman.setOpaque(false);
        pnlRadiosHuman.add(new FLabel.Builder().text("Select your deck:")
                .fontStyle(Font.BOLD).fontSize(16)
                .fontAlign(SwingConstants.LEFT).build(), strRadioConstraints);
        pnlRadiosHuman.add(radCustomHuman, strRadioConstraints);
        pnlRadiosHuman.add(radQuestsHuman, strRadioConstraints);
        pnlRadiosHuman.add(radColorsHuman, strRadioConstraints);
        pnlRadiosHuman.add(radThemesHuman, strRadioConstraints);
        pnlRadiosHuman.add(btnHumanRandom, "w 200px!, h 30px!, gap 0 0 10px 0, ax center");

        pnlRadiosAI.setOpaque(false);
        pnlRadiosAI.add(new FLabel.Builder().text("Select an AI deck:")
                .fontStyle(Font.BOLD).fontSize(16)
                .fontAlign(SwingConstants.LEFT).build(), strRadioConstraints);
        pnlRadiosAI.add(radCustomAI, strRadioConstraints);
        pnlRadiosAI.add(radQuestsAI, strRadioConstraints);
        pnlRadiosAI.add(radColorsAI, strRadioConstraints);
        pnlRadiosAI.add(radThemesAI, strRadioConstraints);
        pnlRadiosAI.add(btnAIRandom, "w 200px!, h 30px!, gap 0 0 10px 0, ax center");

        final String strCheckboxConstraints = "w 200px!, h 30px!, gap 0 20px 0 0";
        pnlStart.setOpaque(false);
        pnlStart.add(cbSingletons, strCheckboxConstraints);
        pnlStart.add(btnStart, "span 1 3, growx, pushx, align center");
        pnlStart.add(cbArtifacts, strCheckboxConstraints);
        pnlStart.add(cbRemoveSmall, strCheckboxConstraints);
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getGroupEnum()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.SANCTIONED;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Constructed";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_CONSTRUCTED;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#populate()
     */
    @Override
    public void populate() {
        parentCell.getBody().setLayout(new MigLayout("insets 0, gap 0, wrap 2"));
        parentCell.getBody().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px, span 2");
        parentCell.getBody().add(lblDecklist, "h 20px!, span 2, ax center");
        parentCell.getBody().add(pnlRadiosAI, "w 45%!, gap 1% 8% 20px 20px");
        parentCell.getBody().add(pnlRadiosHuman, "w 45%!, gap 0 0 20px 20px");
        parentCell.getBody().add(pnlDecksAI, "w 45%!, gap 1% 8% 0 0, growy, pushy");
        parentCell.getBody().add(pnlDecksHuman, "w 45%!, growy, pushy");
        parentCell.getBody().add(pnlStart, "span 2, gap 1% 0 50px 50px, ax center");
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstHumanDecks() {
        return this.lstHumanDecks;
    }

    /** @return {@link javax.swing.JList} */
    public JList getLstAIDecks() {
        return this.lstAIDecks;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return this.btnStart;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnHumanRandom() {
        return this.btnHumanRandom;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnAIRandom() {
        return this.btnAIRandom;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadColorsHuman() {
        return this.radColorsHuman;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadThemesHuman() {
        return this.radThemesHuman;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadCustomHuman() {
        return this.radCustomHuman;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadQuestsHuman() {
        return this.radQuestsHuman;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadColorsAI() {
        return this.radColorsAI;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadThemesAI() {
        return this.radThemesAI;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadCustomAI() {
        return this.radCustomAI;
    }

    /** @return {@link javax.swing.JRadioButton} */
    public JRadioButton getRadQuestsAI() {
        return this.radQuestsAI;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbSingletons() {
        return cbSingletons;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbArtifacts() {
        return cbArtifacts;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRemoveSmall() {
        return cbRemoveSmall;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_CONSTRUCTED;
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
        return CSubmenuConstructed.SINGLETON_INSTANCE;
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
