package forge.gui.home.variant;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import forge.Singletons;
import forge.game.card.Card;
import forge.gui.CardDetailPanel;
import forge.gui.deckchooser.FDeckChooser;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.LblHeader;
import forge.gui.home.StartButton;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FList;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FRadioButton;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTabbedPane;
import forge.item.PaperCard;
import forge.item.IPaperCard;

/** 
 * Assembles Swing components of constructed submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuVanguard implements IVSubmenu<CSubmenuVanguard> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Vanguard Mode");

    /** */
    private final LblHeader lblTitle = new LblHeader("Variant: Vanguard");

    private final JPanel pnlStart = new JPanel(new MigLayout("insets 0, gap 0, wrap 2"));

    private final StartButton btnStart  = new StartButton();

    private final JCheckBox cbSingletons = new FCheckBox("Singleton Mode");
    private final JCheckBox cbArtifacts = new FCheckBox("Remove Artifacts");
    private final JCheckBox cbRemoveSmall = new FCheckBox("Remove Small Creatures");

    private final List<PaperCard> allAvatars = new ArrayList<PaperCard>();

    private final List<PaperCard> allAiAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomHumanAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomAiAvatars = new ArrayList<PaperCard>();

    //////////////////////////////

    private final FTabbedPane tabPane = new FTabbedPane();
    private final List<FPanel> playerPanels = new ArrayList<FPanel>();
    private final List<FDeckChooser> deckChoosers = new ArrayList<FDeckChooser>();
    private final List<FList<Object>> avatarLists = new ArrayList<FList<Object>>();

    private final List<JRadioButton> fieldRadios = new ArrayList<JRadioButton>();
    private final List<JRadioButton> playerIsAIRadios = new ArrayList<JRadioButton>();
    private final ButtonGroup grpFields = new ButtonGroup();
    private final FCheckBox cbDefaultAvatars = new FCheckBox("Use deck-default avatars if possible.");
    private final List<CardDetailPanel> cdpAvatarDetails = new ArrayList<CardDetailPanel>();
    private int currentNumTabsShown = 8;

    //////////////////////////////

    private VSubmenuVanguard() {

        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        Vector<Object> humanListData = new Vector<Object>();
        Vector<Object> aiListData = new Vector<Object>();
        humanListData.add("Random");
        aiListData.add("Random");
        for (PaperCard cp : getAllAvatars()) {
            humanListData.add(cp);
            if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                nonRandomHumanAvatars.add(cp);
            }
            if (!cp.getRules().getAiHints().getRemAIDecks()) {
                aiListData.add(cp);
                allAiAvatars.add(cp);
                if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                    nonRandomAiAvatars.add(cp);
                }
            }
        }

        //This listener will look for any of the radio buttons being selected
        //and call the method that shows/hides tabs appropriately.
        ItemListener iListener = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                FRadioButton aButton = (FRadioButton) arg0.getSource();

                if (arg0.getStateChange() == ItemEvent.SELECTED) {
                    changeTabs(Integer.parseInt(aButton.getText()));
                }
            }

        };

        //This listener will look for an avatar being selected in the lists and update
        //the corresponding detail panel.
        ListSelectionListener lsListener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                int index = avatarLists.indexOf(arg0.getSource());
                Object obj = avatarLists.get(index).getSelectedValue();

                if (obj instanceof PaperCard) {
                    cdpAvatarDetails.get(index).setCard(Card.getCardForUi((IPaperCard) obj));
                }
            }

        };

        //Create all 8 player settings panel
        FRadioButton tempRadio = null;
        FPanel tempPanel;
        FDeckChooser tempChooser;
        FList<Object> tempList;
        CardDetailPanel tempDetail;

        //Settings panel
        FPanel settingsPanel = new FPanel();
        settingsPanel.setLayout(new MigLayout("wrap 2, ax center"));
        FPanel radioPane = new FPanel();
        radioPane.setLayout(new MigLayout("wrap 1"));
        radioPane.setOpaque(false);
        radioPane.add(new FLabel.Builder().text("Set number of opponents").build(), "wrap");
        for (int i = 1; i < 8; i++) {
            tempRadio = new FRadioButton();
            tempRadio.setText(String.valueOf(i));
            fieldRadios.add(tempRadio);
            grpFields.add(tempRadio);
            tempRadio.setSelected(true);
            tempRadio.addItemListener(iListener);
            radioPane.add(tempRadio, "wrap,align 50% 50%");
        }
        settingsPanel.add(radioPane, "span 1 2");
        settingsPanel.add(cbDefaultAvatars);
        tabPane.add("Settings", settingsPanel);

        //Player panels (Human + 7 AIs)
        for (int i = 0; i < 8; i++) {
            tempPanel = new FPanel();
            tempPanel.setLayout(new MigLayout("insets 0, gap 0 , wrap 2, flowy, ax center"));

            tempChooser = new FDeckChooser(i != 0);
            tempChooser.initialize();

            tempList = new FList<Object>();

            tempList.setListData(i == 0 ? humanListData : aiListData);
            tempList.setSelectedIndex(0);
            tempList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            tempList.addListSelectionListener(lsListener);

            deckChoosers.add(tempChooser);
            avatarLists.add(tempList);
            
            ButtonGroup tempBtnGroup = new ButtonGroup();            
            FRadioButton tmpAI = new FRadioButton();
            tmpAI.setText("AI");
            tmpAI.setSelected(i != 0);
            FRadioButton tmpHuman = new FRadioButton();
            tmpHuman.setText("Human");
            tmpHuman.setSelected(i == 0);
            
            FPanel typeBtnPanel = new FPanel();
            typeBtnPanel.add(tmpAI);
            typeBtnPanel.add(tmpHuman,"wrap");
            tempPanel.add(typeBtnPanel);
            
            tempBtnGroup.add(tmpAI);
            tempBtnGroup.add(tmpHuman);
            playerIsAIRadios.add(tmpAI);

            tempPanel.add(tempChooser, "span 1 2, w 33%, gap 10px 10px 0px 10px, growy, pushy, wrap");

            tempPanel.add(new FLabel.Builder().text("Select Avatar:").build(), "gap 0px 0px 10px 10px, flowy");

            FScrollPane scrAvatar = new FScrollPane(tempList, true, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            tempPanel.add(scrAvatar, "h 90%, w 33%, gap 0px 10px 0px 10px, growy, pushy, wrap");

            tempDetail = new CardDetailPanel(null);
            cdpAvatarDetails.add(tempDetail);

            tempPanel.add(tempDetail, "span 1 2, w 33%, gap 0px 10px 10px 10px, growy, pushy, growx");

            playerPanels.add(tempPanel);
            
            tabPane.add("Player " + (i+1), tempPanel);
        }

        final String strCheckboxConstraints = "h 30px!, gap 0 20px 0 0";
        pnlStart.setOpaque(false);
        pnlStart.add(cbSingletons, strCheckboxConstraints);
        pnlStart.add(btnStart, "span 1 3, growx, pushx, align center");
        pnlStart.add(cbArtifacts, strCheckboxConstraints);
        pnlStart.add(cbRemoveSmall, strCheckboxConstraints);
    }
    
    public boolean isPlayerAI(int playernum) {
    	return playerIsAIRadios.get(playernum).isSelected();
    }

    private void changeTabs(int toShow) {
        if (toShow < currentNumTabsShown) {
            for (int i = currentNumTabsShown; i > toShow + 1; i--) {
                tabPane.remove(i);
            }
            currentNumTabsShown = tabPane.getComponentCount() - 1;
        }
        else {
            for (int i = currentNumTabsShown; i <= toShow; i++) {
                tabPane.add("Player " + i, playerPanels.get(i));
            }
            currentNumTabsShown = tabPane.getComponentCount() - 1;
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getGroupEnum()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.VARIANT;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Vanguard";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_VANGUARD;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap 1, ax right"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 80%!, h 40px!, gap 0 0 15px 15px, ax right");

        for (FDeckChooser fdc : deckChoosers) {
            fdc.populate();
        }

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(tabPane, "gap 20px 20px 20px 0px, pushx, pushy, growx, growy");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlStart, "gap 0 0 3.5%! 3.5%!, ax center");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();

    }


    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return this.btnStart;
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
        return EDocID.HOME_VANGUARD;
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
    public CSubmenuVanguard getLayoutControl() {
        return CSubmenuVanguard.SINGLETON_INSTANCE;
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
     * 
     * @return a deckchooser for every player
     */
    public List<FDeckChooser> getDeckChoosers() {
        return deckChoosers;
    }

    /**
     * @return the avatarLists
     */
    public List<FList<Object>> getAvatarLists() {
        return avatarLists;
    }

    /**
     * @return the currentNumTabsShown
     */
    public int getNumPlayers() {
        return currentNumTabsShown;
    }

    /**
     * @return the allAvatars
     */
    public Iterable<PaperCard> getAllAvatars() {
        if (allAvatars.isEmpty()) {
            for (PaperCard c : Singletons.getMagicDb().getVariantCards().getAllCards()) {
                if (c.getRules().getType().isVanguard()) {
                    allAvatars.add(c);
                }
            }
        }
        return allAvatars;
    }

    /**
     * @return the allAiAvatars
     */
    public List<PaperCard> getAllAiAvatars() {
        return allAiAvatars;
    }

    /**
     * @return the non-random human Avatars
     */
    public List<PaperCard> getNonRandomHumanAvatars() {
        return nonRandomHumanAvatars;
    }

    /**
     * @return the non-random AI Avatars
     */
    public List<PaperCard> getNonRandomAiAvatars() {
        return nonRandomAiAvatars;
    }

    /**
     * @return the cbDefaultAvatars
     */
    public FCheckBox getCbDefaultAvatars() {
        return cbDefaultAvatars;
    }
}
