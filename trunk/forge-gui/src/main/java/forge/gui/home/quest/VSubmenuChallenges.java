package forge.gui.home.quest;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.LblHeader;
import forge.gui.home.StartButton;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FComboBoxWrapper;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;

/**
 * Assembles Swing components of quest challenges submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VSubmenuChallenges implements IVSubmenu<CSubmenuChallenges>, IVQuestStats {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Quest Challenges");

    //========== INSTANTIATION
    private final JPanel pnlChallenges   = new JPanel();
    private final JPanel pnlStats   = new JPanel();
    private final JPanel pnlStart   = new JPanel();

    private final FScrollPane scrChallenges = new FScrollPane(pnlChallenges,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private final StartButton btnStart  = new StartButton();
    private final FComboBoxWrapper<String> cbxPet  = new FComboBoxWrapper<String>();
    private final FCheckBox cbPlant = new FCheckBox("Summon Plant");
    private final FCheckBox cbCharm = new FCheckBox("Use Charm of Vigor");
    private final FLabel lblZep   = new FLabel.Builder().text("<html>Launch<br>Zeppelin</html>")
            .hoverable(true).icon(FSkin.getIcon(FSkin.QuestIcons.ICO_ZEP))
            .fontSize(16).build();
    private final FLabel lblWorld   = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_MAP))
        .fontSize(15).build();
    private final FLabel lblLife      = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_LIFE))
        .fontSize(15).build();
    private final FLabel lblCredits   = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_COINSTACK))
        .fontSize(15).build();
    private final FLabel lblWins      = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_PLUS))
        .fontSize(15).build();
    private final FLabel lblLosses    = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_MINUS))
        .fontSize(15).build();
    private final FLabel lblWinStreak = new FLabel.Builder()
        .icon(FSkin.getIcon(FSkin.QuestIcons.ICO_PLUSPLUS))
        .fontSize(15).build();
    private final LblHeader lblTitle = new LblHeader("Quest Mode: Challenges");

    private final FLabel lblInfo = new FLabel.Builder().text("Which challenge will you attempt?")
            .fontStyle(Font.BOLD).fontSize(16)
            .fontAlign(SwingConstants.LEFT).build();

    private final FLabel lblCurrentDeck = new FLabel.Builder()
        .text("Current deck hasn't been set yet.")
        .fontSize(12).build();

    private final FLabel lblNextChallengeInWins = new FLabel.Builder()
        .text("Next challenge in wins hasn't been set yet.")
        .fontSize(12).build();

    private final FLabel btnUnlock = new FLabel.ButtonBuilder().text("Unlock Sets").fontSize(16).build();
    private final FLabel btnTravel = new FLabel.ButtonBuilder().text("Travel").fontSize(16).build();
    private final FLabel btnBazaar = new FLabel.ButtonBuilder().text("Bazaar").fontSize(16).build();
    private final FLabel btnSpellShop = new FLabel.ButtonBuilder().text("Spell Shop").fontSize(16).build();

    /**
     * Constructor.
     */
    private VSubmenuChallenges() {
        scrChallenges.setBorder((Border)null);
        pnlChallenges.setOpaque(false);
        pnlChallenges.setLayout(new MigLayout("insets 0, gap 0, wrap"));

        final String constraints = "h 30px!, gap 0 0 0 5px";
        pnlStats.setLayout(new MigLayout("insets 0, gap 0, wrap, hidemode 3"));
        pnlStats.add(btnUnlock, "w 150px!, h 30px!, gap 0 0 0 10px");
        pnlStats.add(btnTravel, "w 150px!, h 30px!, gap 0 0 0 10px");
        pnlStats.add(btnSpellShop, "w 150px!, h 30px!, gap 0 0 0 10px");
        pnlStats.add(btnBazaar, "w 150px!, h 30px!, gap 0 0 0 10px");
        pnlStats.add(lblWins, constraints);
        pnlStats.add(lblLosses, constraints);
        pnlStats.add(lblCredits, constraints);
        pnlStats.add(lblWinStreak, constraints);
        pnlStats.add(lblLife, constraints);
        pnlStats.add(lblWorld, constraints);
        pnlStats.add(cbPlant, constraints);
        pnlStats.add(cbCharm, constraints);
        cbxPet.addTo(pnlStats, constraints);
        pnlStats.add(lblZep, "w 130px!, h 60px!, gap 0 0 0 5px");
        pnlStats.setOpaque(false);
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.QUEST;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Challenges";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuName()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_QUESTCHALLENGES;
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap 2, ax right"));
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 80%!, h 40px!, gap 0 0 15px 35px, span 2, ax right");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblInfo, "h 30px!, span 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblCurrentDeck, "span 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblNextChallengeInWins, "span 2, gap 0 0 0 20px");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrChallenges, "w 88% - 175px!, pushy, growy");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlStats, "w 185px!, pushy, growy, gap 4% 4% 0 0, span 1 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnStart, "gap 0 0 30px 30px, ax center");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlChallenges() {
        return pnlChallenges;
    }

    /** @return {@link forge.gui.toolbox.FPanel} */
    public JPanel getPnlStats() {
        return pnlStats;
    }

    /** @return {@link forge.gui.toolbox.FPanel} */
    public JPanel getPnlStart() {
        return pnlStart;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getLblTitle() {
        return lblTitle;
    }

    /** @return {@link javax.swing.JLabel} */
    @Override
    public FLabel getLblWorld() {
        return lblWorld;
    }

    @Override
    public FLabel getLblLife() {
        return lblLife;
    }

    @Override
    public FLabel getLblCredits() {
        return lblCredits;
    }

    @Override
    public FLabel getLblWins() {
        return lblWins;
    }

    @Override
    public FLabel getLblLosses() {
        return lblLosses;
    }

    @Override
    public FLabel getLblCurrentDeck() {
        return lblCurrentDeck;
    }

    @Override
    public FLabel getLblNextChallengeInWins() {
        return lblNextChallengeInWins;
    }

    @Override
    public FLabel getLblWinStreak() {
        return lblWinStreak;
    }

    @Override
    public FLabel getBtnBazaar() {
        return btnBazaar;
    }

    @Override
    public FLabel getBtnUnlock() {
        return btnUnlock;
    }

    @Override
    public FLabel getBtnTravel() {
        return btnTravel;
    }
    @Override
    public FLabel getBtnSpellShop() {
        return btnSpellShop;
    }

    @Override
    public FCheckBox getCbPlant() {
        return cbPlant;
    }

    @Override
    public FLabel getLblZep() {
        return lblZep;
    }

    @Override
    public FComboBoxWrapper<String> getCbxPet() {
        return cbxPet;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return btnStart;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_QUESTCHALLENGES;
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
    public CSubmenuChallenges getLayoutControl() {
        return CSubmenuChallenges.SINGLETON_INSTANCE;
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

	/**
	 * @return the cbCharm
	 */
	public FCheckBox getCbCharm() {
		return cbCharm;
	}
}
