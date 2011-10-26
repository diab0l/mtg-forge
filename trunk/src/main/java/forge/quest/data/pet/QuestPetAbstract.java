package forge.quest.data.pet;

import forge.AllZone;
import forge.Card;
import forge.quest.data.bazaar.QuestStallManager;
import forge.quest.data.bazaar.QuestStallPurchasable;

/**
 * <p>
 * Abstract QuestPetAbstract class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class QuestPetAbstract implements QuestStallPurchasable {

    /** The level. */
    int level;
    private int maxLevel;
    // transient here ?
    private String name;
    private String description;

    /**
     * <p>
     * getPetCard.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
    public abstract Card getPetCard();

    /**
     * <p>
     * getAllUpgradePrices.
     * </p>
     * 
     * @return an array of int.
     */
    public abstract int[] getAllUpgradePrices();

    /**
     * <p>
     * getPrice.
     * </p>
     * 
     * @return a int.
     */
    public final int getPrice() {
        return getAllUpgradePrices()[level];
    }

    /**
     * <p>
     * getAllUpgradeDescriptions.
     * </p>
     * 
     * @return an array of {@link java.lang.String} objects.
     */
    public abstract String[] getAllUpgradeDescriptions();

    /**
     * <p>
     * getUpgradeDescription.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getUpgradeDescription() {
        return getAllUpgradeDescriptions()[level];
    }

    /**
     * <p>
     * getAllImageNames.
     * </p>
     * 
     * @return an array of {@link java.lang.String} objects.
     */
    public abstract String[] getAllImageNames();

    /**
     * <p>
     * getImageName.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getImageName() {
        return getAllImageNames()[level];
    }

    /**
     * <p>
     * getAllStats.
     * </p>
     * 
     * @return an array of {@link java.lang.String} objects.
     */
    public abstract String[] getAllStats();

    /**
     * <p>
     * getStats.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getStats() {
        return getAllStats()[level];
    }

    /**
     * <p>
     * getUpgradedStats.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getUpgradedStats() {
        return getAllStats()[level + 1];
    }

    /**
     * <p>
     * Getter for the field <code>level</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getLevel() {
        return level;
    }

    /**
     * <p>
     * incrementLevel.
     * </p>
     */
    public final void incrementLevel() {
        if (level < maxLevel) {
            level++;
        }
    }

    /**
     * <p>
     * Getter for the field <code>maxLevel</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getMaxLevel() {
        return maxLevel;
    }

    /**
     * <p>
     * Constructor for QuestPetAbstract.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @param description
     *            a {@link java.lang.String} object.
     * @param maxLevel
     *            a int.
     */
    protected QuestPetAbstract(final String name, final String description, final int maxLevel) {
        this.description = description;
        this.name = name;
        this.maxLevel = maxLevel;
    }

    /**
     * <p>
     * Setter for the field <code>level</code>.
     * </p>
     * 
     * @param level
     *            a int.
     */
    public final void setLevel(final int level) {
        this.level = level;
    }

    /**
     * <p>
     * getPurchaseDescription.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getPurchaseDescription() {
        return "<em>" + getDescription() + "</em><br>" + getUpgradeDescription() + "<br><br><u>Current stats:</u> "
                + getStats() + "<br><u>Upgraded stats:</u> " + getUpgradedStats();

    }

    /**
     * <p>
     * Getter for the field <code>description</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * <p>
     * Getter for the field <code>name</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return name;
    }

    /** {@inheritDoc} */
    public final int compareTo(final Object o) {
        return name.compareTo(o.toString());
    }

    /**
     * <p>
     * getPurchaseName.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getPurchaseName() {
        return name;
    }

    /**
     * <p>
     * getStallName.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getStallName() {
        return QuestStallManager.PET_SHOP;
    }

    /**
     * <p>
     * isAvailableForPurchase.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isAvailableForPurchase() {
        QuestPetAbstract pet = AllZone.getQuestData().getPetManager().getPet(name);
        if (pet == null) {
            return true;
        }
        return pet.level < pet.getMaxLevel();
    }

    /**
     * <p>
     * onPurchase.
     * </p>
     */
    public void onPurchase() {
        AllZone.getQuestData().getPetManager().addPetLevel(name);
    }
}
