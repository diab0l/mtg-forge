package forge.achievement;

import forge.properties.ForgeConstants;

public class QuestAchievements extends AchievementCollection {
    public QuestAchievements() {
        super("Quest Mode", ForgeConstants.ACHIEVEMENTS_DIR + "quest.xml");
    }

    //add achievements that should appear at the top above core achievements for each game mode
    @Override
    protected void buildTopShelf() {
    }

    //add achievements that should appear at the bottom below core achievements for each game mode
    @Override
    protected void buildBottomShelf() {
    }
}
