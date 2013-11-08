package forge.gui.home.quest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import forge.Command;
import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.game.GameFormat;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;
import forge.properties.NewConstants;
import forge.quest.QuestController;
import forge.quest.QuestMode;
import forge.quest.QuestWorld;
import forge.quest.StartingPoolPreferences;
import forge.quest.StartingPoolType;
import forge.quest.data.GameFormatQuest;
import forge.quest.data.QuestData;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.io.QuestDataIO;

/** 
 * Controls the quest data submenu in the home UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
@SuppressWarnings("serial")
public enum CSubmenuQuestData implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private final Map<String, QuestData> arrQuests = new HashMap<String, QuestData>();

    private final VSubmenuQuestData view = VSubmenuQuestData.SINGLETON_INSTANCE;
    private final List<String> customFormatCodes = new ArrayList<String>();
    private final List<String> customPrizeFormatCodes = new ArrayList<String>();

    private final Command cmdQuestSelect = new Command() { @Override
        public void run() { changeQuest(); } };

    private final Command cmdQuestDelete = new Command() { @Override
        public void run() { update(); } };

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void initialize() {
        view.getBtnEmbark().setCommand(
                new Command() { @Override public void run() { newQuest(); } });

        // disable the very powerful sets -- they can be unlocked later for a high price
        final List<String> unselectableSets = new ArrayList<String>();
        unselectableSets.add("LEA");
        unselectableSets.add("LEB");
        unselectableSets.add("MBP");
        unselectableSets.add("VAN");
        unselectableSets.add("ARC");
        unselectableSets.add("PC2");

        view.getBtnCustomFormat().setCommand(new Command() { @Override public void run() {
            final DialogChooseSets dialog = new DialogChooseSets(customFormatCodes, unselectableSets, false);
            dialog.setOkCallback(new Runnable() {
                @Override
                public void run() {
                    customFormatCodes.clear();
                    customFormatCodes.addAll(dialog.getSelectedSets());
                }
            });
        } });

        view.getBtnPrizeCustomFormat().setCommand(new Command() { @Override public void run() {
            final DialogChooseSets dialog = new DialogChooseSets(customPrizeFormatCodes, unselectableSets, false);
            dialog.setOkCallback(new Runnable() {
                @Override
                public void run() {
                    customPrizeFormatCodes.clear();
                    customPrizeFormatCodes.addAll(dialog.getSelectedSets());
                }
            });
        } });
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        final VSubmenuQuestData view = VSubmenuQuestData.SINGLETON_INSTANCE;
        final File dirQuests = new File(NewConstants.QUEST_SAVE_DIR);
        final QuestController qc = Singletons.getModel().getQuest();

        // Iterate over files and load quest data for each.
        FilenameFilter takeDatFiles = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".dat");
            }
        };
        File[] arrFiles = dirQuests.listFiles(takeDatFiles);
        arrQuests.clear();
        for (File f : arrFiles) {
            arrQuests.put(f.getName(), QuestDataIO.loadData(f));
        }

        // Populate list with available quest data.
        view.getLstQuests().setQuests(new ArrayList<QuestData>(arrQuests.values()));

        // If there are quests available, force select.
        if (arrQuests.size() > 0) {
            final String questname = Singletons.getModel().getQuestPreferences().getPref(QPref.CURRENT_QUEST);

            // Attempt to select previous quest.
            if (arrQuests.get(questname) != null) {
                view.getLstQuests().setSelectedQuestData(arrQuests.get(questname));
            }
            else {
                view.getLstQuests().setSelectedIndex(0);
            }

            // Drop into AllZone.
            qc.load(view.getLstQuests().getSelectedQuest());
        }
        else {
            qc.load(null);
        }

        view.getLstQuests().setSelectCommand(cmdQuestSelect);
        view.getLstQuests().setDeleteCommand(cmdQuestDelete);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { view.getBtnEmbark().requestFocusInWindow(); }
        });
    }

    /**
     * The actuator for new quests.
     */
    private void newQuest() {
        final VSubmenuQuestData view = VSubmenuQuestData.SINGLETON_INSTANCE;
        int difficulty = view.getSelectedDifficulty();

        final QuestMode mode = view.isFantasy() ? QuestMode.Fantasy : QuestMode.Classic;

        Deck dckStartPool = null;
        GameFormat fmtStartPool = null;
        QuestWorld startWorld = Singletons.getModel().getWorlds().get(view.getStartingWorldName());

        GameFormat worldFormat = (startWorld == null ? null : startWorld.getFormat());

        if (worldFormat == null) {
         switch(view.getStartingPoolType()) {
            case Rotating:
                fmtStartPool = view.getRotatingFormat();
                break;

            case CustomFormat:
                if (customFormatCodes.isEmpty()) {

                    int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "You have defined custom format as containing no sets.\nThis will start a game without restriction.\n\nContinue?");
                    if (JOptionPane.YES_OPTION != answer) {
                        return;
                    }
                }
                fmtStartPool = customFormatCodes.isEmpty() ? null : new GameFormatQuest("Custom", customFormatCodes, null); // chosen sets and no banend cards
                break;

            case DraftDeck:
            case SealedDeck:
            case Cube:
                dckStartPool = view.getSelectedDeck();
                if (null == dckStartPool) {

                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "You have not selected a deck to start", "Cannot start a quest", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;

            case Precon:
                dckStartPool = QuestController.getPrecons().get(view.getSelectedPrecon()).getDeck();
                break;

            case Complete:
            default:
                // leave everything as nulls
                break;
         }
        }
        else {
            fmtStartPool = worldFormat;
        }


        GameFormat fmtPrizes = null;

        // The starting QuestWorld format should NOT affect what you get if you travel to a world that doesn't have one...
        // if (worldFormat == null) {
        StartingPoolType prizedPoolType = view.getPrizedPoolType();
        if (null == prizedPoolType) {
            fmtPrizes = fmtStartPool;
            if (null == fmtPrizes && dckStartPool != null) { // build it form deck
                Set<String> sets = new HashSet<String>();
                for (Entry<PaperCard, Integer> c : dckStartPool.getMain()) {
                    sets.add(c.getKey().getEdition());
                }
                if (dckStartPool.has(DeckSection.Sideboard))
                    for (Entry<PaperCard, Integer> c : dckStartPool.get(DeckSection.Sideboard)) {
                        sets.add(c.getKey().getEdition());
                    }
                fmtPrizes = new GameFormat("From deck", sets, null);
            }
         } else {
            switch(prizedPoolType) {
                case Complete:
                    fmtPrizes = null;
                    break;
                case CustomFormat:
                    if (customPrizeFormatCodes.isEmpty()) {

                        int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "You have defined custom format as containing no sets.\nThis will choose all editions without restriction as prized.\n\nContinue?");
                        if (JOptionPane.YES_OPTION != answer) {
                            return;
                        }
                    }
                    fmtPrizes = customPrizeFormatCodes.isEmpty() ? null : new GameFormat("Custom Prizes", customPrizeFormatCodes, null); // chosen sets and no banend cards
                    break;
                case Rotating:
                    fmtPrizes = view.getPrizedRotatingFormat();
                    break;
                default:
                    throw new RuntimeException("Should not get this result");
            }
        }
        // } else {
        //    fmtPrizes = worldFormat;
        // }
        final StartingPoolPreferences userPrefs = new StartingPoolPreferences(view.randomizeColorDistribution(), view.getPreferredColor());

        final Object o = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Poets will remember your quest as:", "Quest Name", JOptionPane.OK_CANCEL_OPTION);
        if (o == null) { return; }

        final String questName = SSubmenuQuestUtil.cleanString(o.toString());

        if (getAllQuests().get(questName) != null || questName.equals("")) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Please pick another quest name, a quest already has that name.");
            return;
        }

        QuestController qc = Singletons.getModel().getQuest();

        qc.newGame(questName, difficulty, mode, fmtPrizes, view.isUnlockSetsAllowed(), dckStartPool, fmtStartPool, view.getStartingWorldName(), userPrefs);
        Singletons.getModel().getQuest().save();

        // Save in preferences.
        Singletons.getModel().getQuestPreferences().setPref(QPref.CURRENT_QUEST, questName + ".dat");
        Singletons.getModel().getQuestPreferences().save();

        update();
    }   // New Quest

    /** Changes between quest data files. */
    private void changeQuest() {
        Singletons.getModel().getQuest().load(VSubmenuQuestData.SINGLETON_INSTANCE
                .getLstQuests().getSelectedQuest());

        // Save in preferences.
        Singletons.getModel().getQuestPreferences().setPref(QPref.CURRENT_QUEST,
                Singletons.getModel().getQuest().getName() + ".dat");
        Singletons.getModel().getQuestPreferences().save();

        //SSubmenuQuestUtil.updateQuestInfo();

        CSubmenuDuels.SINGLETON_INSTANCE.update();
        CSubmenuChallenges.SINGLETON_INSTANCE.update();
        CSubmenuQuestDecks.SINGLETON_INSTANCE.update();
    }

    /** @return  */
    private Map<String, QuestData> getAllQuests() {
        return arrQuests;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }
}
