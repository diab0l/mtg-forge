package forge.quest.gui.main;

import forge.AllZone;
import forge.QuestData;
import forge.Quest_Assignment;
import forge.ReadQuest_Assignment;
import forge.gui.GuiUtils;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestQuest extends QuestSelectablePanel {

    Quest_Assignment assignment;

    public QuestQuest(Quest_Assignment assignment) {
        super(assignment.getName(), assignment.getDifficulty(), assignment.getDesc(), GuiUtils.getIconFromFile(assignment.getIconName()));
        this.assignment = assignment;

        JLabel repeatabilityLabel;
        if (assignment.isRepeatable()){
            repeatabilityLabel = new JLabel("This quest is repeatable");
        }

        else{
            repeatabilityLabel = new JLabel("This quest is not repeatable");
        }

        GuiUtils.addGap(centerPanel);
        this.centerPanel.add(repeatabilityLabel);
    }

    public static List<QuestQuest> getQuests() {
        List<QuestQuest> quests = new ArrayList<QuestQuest>();

        List<Quest_Assignment> questList = readQuests();

        for (Quest_Assignment assignment : questList) {
            quests.add(new QuestQuest(assignment));
        }
        return quests;
    }

    private static List<Quest_Assignment> readQuests() {
        QuestData questData = AllZone.QuestData;
        ReadQuest_Assignment read = new ReadQuest_Assignment(ForgeProps.getFile(NewConstants.QUEST.QUESTS), questData);
        read.run();

        ArrayList<Quest_Assignment> questsToDisplay = new ArrayList<Quest_Assignment>();

        if (questData.getAvailableQuests() != null && questData.getAvailableQuests().size() > 0) {
            ArrayList<Quest_Assignment> availableQuests = read.getQuestsByIds(questData.getAvailableQuests());
            questsToDisplay = availableQuests;

        }
        else {
            ArrayList<Quest_Assignment> allAvailableQuests = read.getQuests();

            ArrayList<Integer> availableInts = new ArrayList<Integer>();

            int maxQuests = questData.getWin() / 10;
            if (maxQuests > 5) {
                maxQuests = 5;
            }
            if (allAvailableQuests.size() < maxQuests) {
                maxQuests = allAvailableQuests.size();
            }

            Collections.shuffle(allAvailableQuests);

            for (int i = 0; i < maxQuests; i++) {
                Quest_Assignment qa = allAvailableQuests.get(i);

                availableInts.add(qa.getId());

                questsToDisplay.add(qa);
            }
            questData.setAvailableQuests(availableInts);
            QuestData.saveData(questData);
        }//else
        return questsToDisplay;
    }

    public Quest_Assignment getQuestAssignment() {
        return assignment;
    }
}
