package forge.gui.home.quest;

import forge.UiCommand;
import forge.Singletons;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.gui.home.CHomeUI;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.JXButtonPanel;
import forge.quest.QuestController;
import forge.quest.QuestEventChallenge;
import forge.quest.bazaar.QuestItemType;
import forge.quest.bazaar.QuestPetController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/** 
 * Controls the quest challenges submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CSubmenuChallenges implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#initialize()
     */
    @SuppressWarnings("serial")
    @Override
    public void initialize() {
        final VSubmenuChallenges view = VSubmenuChallenges.SINGLETON_INSTANCE;

        view.getBtnSpellShop().setCommand(
                new UiCommand() { @Override
                    public void run() { SSubmenuQuestUtil.showSpellShop(); } });

        view.getBtnBazaar().setCommand(
                new UiCommand() { @Override
                    public void run() { SSubmenuQuestUtil.showBazaar(); } });

        view.getBtnUnlock().setCommand(
                new UiCommand() { @Override
                    public void run() { SSubmenuQuestUtil.chooseAndUnlockEdition(); CSubmenuChallenges.this.update(); } });

        view.getBtnTravel().setCommand(
                new UiCommand() { @Override
                    public void run() { SSubmenuQuestUtil.travelWorld(); CSubmenuChallenges.this.update(); } });

        view.getBtnStart().addActionListener(
                new ActionListener() { @Override
            public void actionPerformed(final ActionEvent e) { SSubmenuQuestUtil.startGame(); } });

        ((FLabel) view.getLblZep()).setCommand(
                new UiCommand() {
                    @Override
                    public void run() {
                        if (!SSubmenuQuestUtil.checkActiveQuest("Launch a Zeppelin.")) {
                            return;
                        }
                        Singletons.getModel().getQuest().getAchievements().setCurrentChallenges(null);
                        Singletons.getModel().getQuest().getAssets().setItemLevel(QuestItemType.ZEPPELIN, 2);
                        update();
                    }
                });

        final QuestController quest = Singletons.getModel().getQuest();
        view.getCbPlant().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quest.selectPet(0, view.getCbPlant().isSelected() ? "Plant" : null);
                quest.save();
            }
        });

        view.getCbxPet().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final int slot = 1;
                final int index = view.getCbxPet().getSelectedIndex();
                List<QuestPetController> pets = quest.getPetsStorage().getAvaliablePets(slot, quest.getAssets());
                String petName = index <= 0 || index > pets.size() ? null : pets.get(index - 1).getName();
                quest.selectPet(slot, petName);
                quest.save();
            }
        });

        view.getCbCharm().addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		quest.setCharmState(view.getCbCharm().isSelected());
        		quest.save();
        	}
        });
    }

    private final KeyAdapter _startOnEnter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (KeyEvent.VK_ENTER == e.getKeyChar()) {
                VSubmenuChallenges.SINGLETON_INSTANCE.getBtnStart().doClick();
            }
        }
    };
    private final MouseAdapter _startOnDblClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (MouseEvent.BUTTON1 == e.getButton() && 2 == e.getClickCount()) {
                VSubmenuChallenges.SINGLETON_INSTANCE.getBtnStart().doClick();
            }
        }
    };

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        SSubmenuQuestUtil.updateQuestView(VSubmenuChallenges.SINGLETON_INSTANCE);

        final VSubmenuChallenges view = VSubmenuChallenges.SINGLETON_INSTANCE;
        final QuestController qCtrl = Singletons.getModel().getQuest();

        if (qCtrl.getAchievements() == null) return;

        view.getLblTitle().setText("Challenges: " + qCtrl.getRank());

        view.getPnlChallenges().removeAll();
        qCtrl.regenerateChallenges();
        final List<QuestEventChallenge> challenges = new ArrayList<QuestEventChallenge>();
        for(Object id : qCtrl.getAchievements().getCurrentChallenges()) {
            challenges.add(qCtrl.getChallenges().get(id.toString()));
        }

        JXButtonPanel grpPanel = new JXButtonPanel();

        boolean haveAnyChallenges = true;
        for (QuestEventChallenge qc : challenges) {
            final PnlEvent temp = new PnlEvent(qc);
            final JRadioButton rad = temp.getRad();
            if (haveAnyChallenges) {
                rad.setSelected(true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() { rad.requestFocusInWindow(); }
                });
                haveAnyChallenges = false;
            }
            rad.addKeyListener(_startOnEnter);
            rad.addMouseListener(_startOnDblClick);
            grpPanel.add(temp, rad, "w 100%!, h 135px!, gapy 15px");
        }
        view.getPnlChallenges().add(grpPanel, "w 100%!");

        if (!haveAnyChallenges) {
            final FLabel lbl = new FLabel.Builder()
                .text(VSubmenuChallenges.SINGLETON_INSTANCE.getLblNextChallengeInWins().getText())
                .fontAlign(SwingConstants.CENTER).build();
            lbl.setForeground(Color.red);
            lbl.setBackground(Color.white);
            lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
            lbl.setOpaque(true);
            view.getPnlChallenges().add(lbl, "w 50%!, h 30px!, gap 25% 0 50px 0");
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() { view.getBtnTravel().requestFocusInWindow(); }
            });
        }

        Singletons.getView().getFrame().validate();

    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @SuppressWarnings("serial")
    @Override
    public UiCommand getCommandOnSelect() {
        final QuestController qc = Singletons.getModel().getQuest();
        return new UiCommand() {
            @Override
            public void run() {
                if (qc.getAchievements() == null) {
                    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_QUESTDATA);
                }
            }
        };
    }
}
