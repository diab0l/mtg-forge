package forge.screens.quest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.FThreads;
import forge.Forge;
import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.card.CardRenderer;
import forge.model.FModel;
import forge.properties.ForgeConstants;
import forge.quest.QuestController;
import forge.quest.QuestUtil;
import forge.quest.data.QuestData;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.io.QuestDataIO;
import forge.screens.FScreen;
import forge.screens.settings.SettingsScreen;
import forge.toolbox.FButton;
import forge.toolbox.FEvent;
import forge.toolbox.FList;
import forge.toolbox.FOptionPane;
import forge.toolbox.FTextArea;
import forge.toolbox.FEvent.FEventHandler;
import forge.util.ThreadUtil;
import forge.util.Utils;
import forge.util.gui.SOptionPane;

public class LoadQuestScreen extends FScreen {
    private static final float PADDING = Utils.AVG_FINGER_HEIGHT * 0.1f;
    private static final FSkinColor OLD_QUESTS_BACK_COLOR = FSkinColor.get(Colors.CLR_INACTIVE).getContrastColor(20);
    private static final FSkinColor SEL_COLOR = FSkinColor.get(Colors.CLR_ACTIVE);

    private final FTextArea lblOldQuests = add(new FTextArea("Loading Existing Quests..."));
    private final QuestFileLister lstQuests = add(new QuestFileLister());
    private final FButton btnNewQuest = add(new FButton("New"));
    private final FButton btnRenameQuest = add(new FButton("Rename"));
    private final FButton btnDeleteQuest = add(new FButton("Delete"));

    public LoadQuestScreen() {
        super("Load Quest", QuestMenu.getMenu());

        lblOldQuests.setFont(FSkinFont.get(12));
        lblOldQuests.setAlignment(HAlignment.CENTER);

        btnNewQuest.setFont(FSkinFont.get(16));
        btnNewQuest.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                Forge.openScreen(new NewQuestScreen());
            }
        });
        btnRenameQuest.setFont(btnNewQuest.getFont());
        btnRenameQuest.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                renameQuest(lstQuests.getSelectedQuest());
            }
        });
        btnDeleteQuest.setFont(btnNewQuest.getFont());
        btnDeleteQuest.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                deleteQuest(lstQuests.getSelectedQuest());
            }
        });

        FThreads.invokeInBackgroundThread(new Runnable() {
            @Override
            public void run() {
                final File dirQuests = new File(ForgeConstants.QUEST_SAVE_DIR);
                final QuestController qc = FModel.getQuest();

                // Iterate over files and load quest data for each.
                FilenameFilter takeDatFiles = new FilenameFilter() {
                    @Override
                    public boolean accept(final File dir, final String name) {
                        return name.endsWith(".dat");
                    }
                };
                File[] arrFiles = dirQuests.listFiles(takeDatFiles);
                Map<String, QuestData> arrQuests = new HashMap<String, QuestData>();
                for (File f : arrFiles) {
                    arrQuests.put(f.getName(), QuestDataIO.loadData(f));
                }

                // Populate list with available quest data.
                lstQuests.setQuests(new ArrayList<QuestData>(arrQuests.values()));

                // If there are quests available, force select.
                if (arrQuests.size() > 0) {
                    final String questname = FModel.getQuestPreferences().getPref(QPref.CURRENT_QUEST);

                    // Attempt to select previous quest.
                    if (arrQuests.get(questname) != null) {
                        lstQuests.setSelectedQuest(arrQuests.get(questname));
                    }
                    else {
                        lstQuests.setSelectedIndex(0);
                    }

                    // Drop into AllZone.
                    qc.load(lstQuests.getSelectedQuest());
                }
                else {
                    qc.load(null);
                }
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        lblOldQuests.setText("Old quest data? Put into \""
                                + ForgeConstants.QUEST_SAVE_DIR.replace('\\', '/') + "\" and restart Forge.");
                        revalidate();
                    }
                });
            }
        });
    }

    @Override
    protected void drawBackground(Graphics g) {
        super.drawBackground(g);
        float y = getHeader().getBottom();
        g.fillRect(OLD_QUESTS_BACK_COLOR, 0, y, getWidth(), lstQuests.getTop() - y);
    }

    @Override
    protected void drawOverlay(Graphics g) {
        float y = lstQuests.getTop();
        g.drawLine(1, FList.LINE_COLOR, 0, y, getWidth(), y); //draw top border for list
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        float buttonWidth = (width - 2 * PADDING) / 3;
        float buttonHeight = btnNewQuest.getAutoSizeBounds().height * 1.2f;

        float y = startY + 2 * PADDING;
        lblOldQuests.setBounds(0, y, width, lblOldQuests.getPreferredHeight(width));
        y += lblOldQuests.getHeight() + PADDING;
        lstQuests.setBounds(0, y, width, height - y - buttonHeight - 2 * PADDING);
        y += lstQuests.getHeight() + PADDING;

        float x = 0;
        btnNewQuest.setBounds(x, y, buttonWidth, buttonHeight);
        x += buttonWidth + PADDING;
        btnRenameQuest.setBounds(x, y, buttonWidth, buttonHeight);
        x += buttonWidth + PADDING;
        btnDeleteQuest.setBounds(x, y, buttonWidth, buttonHeight);
    }

    /** Changes between quest data files. */
    private void changeQuest() {
        FModel.getQuestPreferences().setPref(QPref.CURRENT_QUEST,
                lstQuests.getSelectedQuest().getName() + ".dat");
        FModel.getQuestPreferences().save();

        Forge.back();
        QuestMenu.launchQuestMode();
    }

    private void renameQuest(final QuestData quest) {
        if (quest == null) { return; }

        ThreadUtil.invokeInGameThread(new Runnable() {
            @Override
            public void run() {
                String questName;
                String oldQuestName = quest.getName();
                while (true) {
                    questName = SOptionPane.showInputDialog("Enter new name for quest:", "Rename Quest", null, oldQuestName);
                    if (questName == null) { return; }

                    questName = QuestUtil.cleanString(questName);
                    if (questName.equals(oldQuestName)) { return; } //quit if chose same name

                    if (questName.isEmpty()) {
                        FOptionPane.showMessageDialog("Please specify a quest name.");
                        continue;
                    }

                    boolean exists = false;
                    for (QuestData questData : lstQuests) {
                        if (questData.getName().equalsIgnoreCase(questName)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        FOptionPane.showMessageDialog("A quest already exists with that name. Please pick another quest name.");
                        continue;
                    }
                    break;
                }

                quest.rename(questName);
            }
        });
    }

    private void deleteQuest(final QuestData quest) {
        if (quest == null) { return; }

        ThreadUtil.invokeInGameThread(new Runnable() {
            @Override
            public void run() {
                if (!SOptionPane.showConfirmDialog(
                        "Are you sure you want to delete '" + quest.getName() + "'?",
                        "Delete Quest", "Delete", "Cancel")) {
                    return;
                }

                new File(ForgeConstants.QUEST_SAVE_DIR, quest.getName() + ".dat").delete();

                lstQuests.removeItem(quest);
            }
        });
    }

    private class QuestFileLister extends FList<QuestData> {
        private int selectedIndex = 0;
        
        private QuestFileLister() {
            setListItemRenderer(new ListItemRenderer<QuestData>() {
                @Override
                public boolean tap(Integer index, QuestData value, float x, float y, int count) {
                    if (count == 2) {
                        changeQuest();
                    }
                    else {
                        selectedIndex = index;
                    }
                    return true;
                }

                @Override
                public float getItemHeight() {
                    return CardRenderer.getCardListItemHeight();
                }

                @Override
                public void drawValue(Graphics g, Integer index, QuestData value, FSkinFont font, FSkinColor foreColor, boolean pressed, float x, float y, float w, float h) {
                    float offset = w * SettingsScreen.INSETS_FACTOR - FList.PADDING; //increase padding for settings items
                    x += offset;
                    y += offset;
                    w -= 2 * offset;
                    h -= 2 * offset;

                    float totalHeight = h;
                    String name = value.getName() + " (" + value.getMode().toString() + ")";
                    h = font.getMultiLineBounds(name).height + SettingsScreen.SETTING_PADDING;

                    String winRatio = value.getAchievements().getWin() + "/" + value.getAchievements().getLost();
                    float winRatioWidth = font.getBounds(winRatio).width + SettingsScreen.SETTING_PADDING;

                    g.drawText(name, font, foreColor, x, y, w - winRatioWidth, h, false, HAlignment.LEFT, false);
                    g.drawText(winRatio, font, foreColor, x, y, w, h, false, HAlignment.RIGHT, false);

                    h += SettingsScreen.SETTING_PADDING;
                    g.drawText(FModel.getQuest().getRank(value.getAchievements().getLevel()), FSkinFont.get(12), SettingsScreen.DESC_COLOR, x, y + h, w, totalHeight - h + w * SettingsScreen.INSETS_FACTOR, true, HAlignment.LEFT, false);
                }
            });
        }

        @Override
        protected FSkinColor getItemFillColor(int index) {
            if (index == selectedIndex) {
                return SEL_COLOR;
            }
            return null;
        }

        public void setQuests(List<QuestData> qd0) {
            List<QuestData> sorted = new ArrayList<QuestData>();
            for (QuestData qd : qd0) {
                sorted.add(qd);
            }
            Collections.sort(sorted, new Comparator<QuestData>() {
                @Override
                public int compare(final QuestData x, final QuestData y) {
                    return x.getName().compareTo(y.getName());
                }
            });
            lstQuests.setListData(sorted);
        }

        public boolean setSelectedIndex(int i0) {
            if (i0 >= getCount()) { return false; }
            selectedIndex = i0;
            return true;
        }

        public QuestData getSelectedQuest() {
            if (selectedIndex == -1) { return null; }
            return lstQuests.getItemAt(selectedIndex);
        }

        public boolean setSelectedQuest(QuestData qd0) {
            for (int i = 0; i < getCount(); i++) {
                if (getItemAt(i) == qd0) {
                    selectedIndex = i;
                    return true;
                }
            }
            return false;
        }
    }
}
