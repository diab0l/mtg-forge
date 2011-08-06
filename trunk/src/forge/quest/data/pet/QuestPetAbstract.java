package forge.quest.data.pet;

import forge.Card;

public abstract class QuestPetAbstract implements Comparable{
    int level;
    private int maxLevel;
    private String name;
    private String description;

    public abstract Card getPetCard();

    public abstract int[] getAllUpgradePrices();
    public int getUpgradePrice(){
        return getAllUpgradePrices()[level];
    }

    public abstract String[] getAllUpgradeDescriptions();
    public String getUpgradeDescription(){
        return getAllUpgradeDescriptions()[level];
    }


    public abstract String[] getAllImageNames();
    public String getImageName(){
        return getAllImageNames()[level];
    }

    public abstract String[] getAllStats();
    public String getStats(){
        return getAllStats()[level];
    }


    public int getLevel() {
        return level;
    }

    public void incrementLevel(){
        if (level < maxLevel){
            level++;
        }
    }

    public int getMaxLevel(){
        return maxLevel;
    }

    protected QuestPetAbstract(String name, String description, int maxLevel) {
        this.description = description;
        this.name = name;
        this.maxLevel = maxLevel;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        return name.compareTo(o.toString());
    }
}
