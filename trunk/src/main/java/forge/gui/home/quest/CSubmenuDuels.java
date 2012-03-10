package forge.gui.home.quest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import forge.AllZone;
import forge.Command;
import forge.gui.home.EMenuItem;
import forge.gui.home.ICSubmenu;
import forge.gui.home.quest.SubmenuQuestUtil.SelectablePanel;
import forge.quest.data.QuestController;
import forge.quest.data.QuestDuel;
import forge.quest.data.QuestEventManager;
import forge.view.ViewHomeUI;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public enum CSubmenuDuels implements ICSubmenu {
    /** */
    SINGLETON_INSTANCE;

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#getCommand()
     */
    @SuppressWarnings("serial")
    @Override
    public Command getMenuCommand() {
        final QuestController qc = AllZone.getQuest();
        return new Command() {
            public void execute() {
                if (qc.getAchievements() == null) {
                    ViewHomeUI.SINGLETON_INSTANCE.itemClick(EMenuItem.QUEST_DATA);
                }
            }
        };
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#initialize()
     */
    @SuppressWarnings("serial")
    @Override
    public void initialize() {
        final VSubmenuDuels view = VSubmenuDuels.SINGLETON_INSTANCE;
        view.populate();
        CSubmenuDuels.SINGLETON_INSTANCE.update();

        view.getBtnSpellShop().setCommand(
                new Command() { @Override
                    public void execute() { SubmenuQuestUtil.showSpellShop(); } });

        view.getBtnBazaar().setCommand(
                new Command() { @Override
                    public void execute() { SubmenuQuestUtil.showBazaar(); } });

        view.getBtnStart().addActionListener(
                new ActionListener() { @Override
            public void actionPerformed(final ActionEvent e) { SubmenuQuestUtil.startGame(); } });

        view.getBtnCurrentDeck().setCommand(
                new Command() { @Override
                    public void execute() {
                        ViewHomeUI.SINGLETON_INSTANCE.itemClick(EMenuItem.QUEST_DECKS);
                    }
                });

        final QuestController quest = AllZone.getQuest();
        view.getCbPlant().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quest.getAssets().getPetManager().setUsePlant(view.getCbPlant().isSelected());
            }
        });

        view.getCbZep().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quest.getAssets().getPetManager().setUsePlant(view.getCbZep().isSelected());
            }
        });

        view.getCbxPet().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final int index = view.getCbxPet().getSelectedIndex();
                if (index != -1 && index != 0) {
                    final String pet = ((String) view.getCbxPet().getSelectedItem());
                    quest.getAssets().getPetManager().setSelectedPet(pet.substring(7));
                }
                else {
                    quest.getAssets().getPetManager().setSelectedPet(null);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        SubmenuQuestUtil.updateStatsAndPet();

        final VSubmenuDuels view = VSubmenuDuels.SINGLETON_INSTANCE;

        if (AllZone.getQuest().getAchievements() != null) {
            view.getLblTitle().setText("Duels: " + AllZone.getQuest().getRank());

            view.getPnlDuels().removeAll();
            final List<QuestDuel> duels = QuestEventManager.INSTANCE.generateDuels();

            for (final QuestDuel d : duels) {
                final SelectablePanel temp = new SelectablePanel(d);
                view.getPnlDuels().add(temp, "w 96%!, h 86px!, gap 2% 0 5px 5px");
            }
        }
    }
}
