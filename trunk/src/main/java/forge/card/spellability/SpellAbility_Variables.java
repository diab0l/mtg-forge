package forge.card.spellability;

import java.util.ArrayList;

import forge.Constant;

/**
 * <p>
 * SpellAbility_Variables class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 * @since 1.0.15
 */
public class SpellAbility_Variables {
    // A class for handling SpellAbility Variables. These restrictions include:
    // Zone, Phase, OwnTurn, Speed (instant/sorcery), Amount per Turn, Player,
    // Threshold, Metalcraft, Hellbent, LevelRange, etc
    // Each value will have a default, that can be overridden (mostly by
    // AbilityFactory)

    /**
     * <p>
     * Constructor for SpellAbility_Variables.
     * </p>
     */
    public SpellAbility_Variables() {
    }

    // default values for Sorcery speed abilities
    /** The zone. */
    protected Constant.Zone zone = Constant.Zone.Battlefield;

    /** The phases. */
    private ArrayList<String> phases = new ArrayList<String>();

    /** The b sorcery speed. */
    private boolean sorcerySpeed = false;

    /** The b any player. */
    protected boolean bAnyPlayer = false;

    /** The b opponent turn. */
    protected boolean bOpponentTurn = false;

    /** The b player turn. */
    private boolean playerTurn = false;

    /** The activation limit. */
    private int activationLimit = -1;

    /** The number turn activations. */
    private int numberTurnActivations = 0;

    /** The activation number sacrifice. */
    private int activationNumberSacrifice = -1;

    /** The n cards in hand. */
    protected int nCardsInHand = -1;

    /** The threshold. */
    private boolean threshold = false;

    /** The metalcraft. */
    private boolean metalcraft = false;

    /** The hellbent. */
    private boolean hellbent = false;

    /** The prowl. */
    private ArrayList<String> prowl = null;

    /** The s is present. */
    protected String sIsPresent = null;

    /** The present compare. */
    private String presentCompare = "GE1"; // Default Compare to Greater or
                                             // Equal to 1

    /** The present defined. */
    protected String presentDefined = null;

    /** The present zone. */
    protected Constant.Zone presentZone = Constant.Zone.Battlefield;

    /** The svar to check. */
    protected String svarToCheck = null;

    /** The svar operator. */
    protected String svarOperator = "GE";

    /** The svar operand. */
    protected String svarOperand = "1";

    /** The life total. */
    private String lifeTotal = null;

    /** The life amount. */
    private String lifeAmount = "GE1";

    /** The mana spent. */
    protected String manaSpent = "";

    /** The pw ability. */
    private boolean pwAbility = false;

    /** The all m12 empires. */
    protected boolean allM12Empires = false;

    /** The not all m12 empires. */
    protected boolean notAllM12Empires = false;

    /**
     * <p>
     * Setter for the field <code>notAllM12Empires</code>.
     * </p>
     * 
     * @param b
     *            a boolean
     */
    public final void setNotAllM12Empires(final boolean b) {
        notAllM12Empires = b;
    }

    /**
     * <p>
     * Getter for the field <code>notAllM12Empires</code>.
     * </p>
     * 
     * @return a boolean
     */
    public final boolean getNotAllM12Empires() {
        return notAllM12Empires;
    }

    /**
     * <p>
     * Setter for the field <code>allM12Empires</code>.
     * </p>
     * 
     * @param b
     *            a boolean
     */
    public final void setAllM12Empires(final boolean b) {
        allM12Empires = b;
    }

    /**
     * <p>
     * Getter for the field <code>allM12Empires</code>.
     * </p>
     * 
     * @return a boolean
     */
    public final boolean getAllM12Empires() {
        return allM12Empires;
    }

    /**
     * <p>
     * Setter for the field <code>manaSpent</code>.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     */
    public final void setManaSpent(final String s) {
        manaSpent = s;
    }

    /**
     * <p>
     * Getter for the field <code>manaSpent</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getManaSpent() {
        return manaSpent;
    }

    /**
     * <p>
     * Setter for the field <code>zone</code>.
     * </p>
     * 
     * @param zone
     *            a {@link java.lang.String} object.
     */
    public final void setZone(final Constant.Zone zone) {
        this.zone = zone;
    }

    /**
     * <p>
     * Getter for the field <code>zone</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final Constant.Zone getZone() {
        return zone;
    }

    /**
     * <p>
     * setSorcerySpeed.
     * </p>
     * 
     * @param bSpeed
     *            a boolean.
     */
    public final void setSorcerySpeed(final boolean bSpeed) {
        sorcerySpeed = bSpeed;
    }

    /**
     * <p>
     * getSorcerySpeed.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isSorcerySpeed() {
        return sorcerySpeed;
    }

    /**
     * <p>
     * setAnyPlayer.
     * </p>
     * 
     * @param anyPlayer
     *            a boolean.
     */
    public final void setAnyPlayer(final boolean anyPlayer) {
        bAnyPlayer = anyPlayer;
    }

    /**
     * <p>
     * getAnyPlayer.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getAnyPlayer() {
        return bAnyPlayer;
    }

    /**
     * <p>
     * setPlayerTurn.
     * </p>
     * 
     * @param bTurn
     *            a boolean.
     */
    public final void setPlayerTurn(final boolean bTurn) {
        playerTurn = bTurn;
    }

    /**
     * <p>
     * getPlayerTurn.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getPlayerTurn() {
        return isPlayerTurn();
    }

    /**
     * <p>
     * setOpponentTurn.
     * </p>
     * 
     * @param bTurn
     *            a boolean.
     */
    public final void setOpponentTurn(final boolean bTurn) {
        bOpponentTurn = bTurn;
    }

    /**
     * <p>
     * getOpponentTurn.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getOpponentTurn() {
        return bOpponentTurn;
    }

    /**
     * <p>
     * Setter for the field <code>activationLimit</code>.
     * </p>
     * 
     * @param limit
     *            a int.
     */
    public final void setActivationLimit(final int limit) {
        activationLimit = limit;
    }

    /**
     * <p>
     * abilityActivated.
     * </p>
     */
    public final void abilityActivated() {
        numberTurnActivations++;
    }

    /**
     * <p>
     * Getter for the field <code>numberTurnActivations</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getNumberTurnActivations() {
        return numberTurnActivations;
    }

    /**
     * <p>
     * resetTurnActivations.
     * </p>
     */
    public final void resetTurnActivations() {
        numberTurnActivations = 0;
    }

    /**
     * <p>
     * Setter for the field <code>activationNumberSacrifice</code>.
     * </p>
     * 
     * @param num
     *            a int.
     */
    public final void setActivationNumberSacrifice(final int num) {
        activationNumberSacrifice = num;
    }

    /**
     * <p>
     * Getter for the field <code>activationNumberSacrifice</code>.
     * </p>
     * 
     * @return a int.
     */
    public final int getActivationNumberSacrifice() {
        return activationNumberSacrifice;
    }

    /**
     * <p>
     * Setter for the field <code>phases</code>.
     * </p>
     * 
     * @param phasesString
     *            a {@link java.lang.String} object.
     */
    public final void setPhases(final String phasesString) {
        for (String s : phasesString.split(",")) {
            phases.add(s);
        }
    }

    /**
     * <p>
     * setActivateCardsInHand.
     * </p>
     * 
     * @param cards
     *            a int.
     */
    public final void setActivateCardsInHand(final int cards) {
        nCardsInHand = cards;
    }

    // specific named conditions
    /**
     * <p>
     * Setter for the field <code>hellbent</code>.
     * </p>
     * 
     * @param bHellbent
     *            a boolean.
     */
    public final void setHellbent(final boolean bHellbent) {
        hellbent = bHellbent;
    }

    /**
     * <p>
     * Setter for the field <code>threshold</code>.
     * </p>
     * 
     * @param bThreshold
     *            a boolean.
     */
    public final void setThreshold(final boolean bThreshold) {
        threshold = bThreshold;
    }

    /**
     * <p>
     * Setter for the field <code>metalcraft</code>.
     * </p>
     * 
     * @param bMetalcraft
     *            a boolean.
     */
    public final void setMetalcraft(final boolean bMetalcraft) {
        metalcraft = bMetalcraft;
    }

    /**
     * <p>
     * Setter for the field <code>prowl</code>.
     * </p>
     * 
     * @param types
     *            the new prowl
     */
    public final void setProwl(final ArrayList<String> types) {
        prowl = types;
    }

    // IsPresent for Valid battlefield stuff

    /**
     * <p>
     * setIsPresent.
     * </p>
     * 
     * @param present
     *            a {@link java.lang.String} object.
     */
    public final void setIsPresent(final String present) {
        sIsPresent = present;
    }

    /**
     * <p>
     * Setter for the field <code>presentCompare</code>.
     * </p>
     * 
     * @param compare
     *            a {@link java.lang.String} object.
     */
    public final void setPresentCompare(final String compare) {
        presentCompare = compare;
    }

    /**
     * Gets the present zone.
     * 
     * @return the present zone
     */
    public final Constant.Zone getPresentZone() {
        return presentZone;
    }

    /**
     * Sets the present zone.
     * 
     * @param presentZone
     *            the new present zone
     */
    public final void setPresentZone(final Constant.Zone presentZone) {
        this.presentZone = presentZone;
    }

    /**
     * <p>
     * Setter for the field <code>presentDefined</code>.
     * </p>
     * 
     * @param defined
     *            a {@link java.lang.String} object.
     */
    public final void setPresentDefined(final String defined) {
        presentDefined = defined;
    }

    // used to define as a Planeswalker ability
    /**
     * <p>
     * setPlaneswalker.
     * </p>
     * 
     * @param bPlaneswalker
     *            a boolean.
     */
    public final void setPlaneswalker(final boolean bPlaneswalker) {
        setPwAbility(bPlaneswalker);
    }

    /**
     * <p>
     * getPlaneswalker.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getPlaneswalker() {
        return isPwAbility();
    }

    // Checking the values of SVars (Mostly for Traps)
    /**
     * <p>
     * Setter for the field <code>svarToCheck</code>.
     * </p>
     * 
     * @param sVar
     *            a {@link java.lang.String} object.
     */
    public final void setSvarToCheck(final String sVar) {
        svarToCheck = sVar;
    }

    /**
     * <p>
     * Setter for the field <code>svarOperator</code>.
     * </p>
     * 
     * @param operator
     *            a {@link java.lang.String} object.
     */
    public final void setSvarOperator(final String operator) {
        svarOperator = operator;
    }

    /**
     * <p>
     * Setter for the field <code>svarOperand</code>.
     * </p>
     * 
     * @param operand
     *            a {@link java.lang.String} object.
     */
    public final void setSvarOperand(final String operand) {
        svarOperand = operand;
    }

    /**
     * Gets the activation limit.
     *
     * @return the activationLimit
     */
    public int getActivationLimit() {
        return activationLimit;
    }

    /**
     * Checks if is threshold.
     *
     * @return the threshold
     */
    public final boolean isThreshold() {
        return threshold;
    }

    /**
     * Checks if is metalcraft.
     *
     * @return the metalcraft
     */
    public final boolean isMetalcraft() {
        return metalcraft;
    }

    /**
     * Checks if is hellbent.
     *
     * @return the hellbent
     */
    public final boolean isHellbent() {
        return hellbent;
    }

    /**
     * Checks if is pw ability.
     *
     * @return the pwAbility
     */
    public final boolean isPwAbility() {
        return pwAbility;
    }

    /**
     * Sets the pw ability.
     *
     * @param pwAbility the new pw ability
     */
    public final void setPwAbility(final boolean pwAbility) {
        this.pwAbility = pwAbility; // TODO Add 0 to parameter's name.
    }

    /**
     * Checks if is player turn.
     *
     * @return the playerTurn
     */
    public boolean isPlayerTurn() {
        return playerTurn;
    }

    /**
     * Gets the prowl.
     *
     * @return the prowl
     */
    public ArrayList<String> getProwl() {
        return prowl;
    }

    /**
     * Gets the present compare.
     *
     * @return the presentCompare
     */
    public String getPresentCompare() {
        return presentCompare;
    }

    /**
     * Gets the life total.
     *
     * @return the lifeTotal
     */
    public final String getLifeTotal() {
        return lifeTotal;
    }

    /**
     * Sets the life total.
     *
     * @param lifeTotal the lifeTotal to set
     */
    public final void setLifeTotal(final String lifeTotal) {
        this.lifeTotal = lifeTotal; // TODO Add 0 to parameter's name.
    }

    /**
     * Gets the life amount.
     *
     * @return the lifeAmount
     */
    public final String getLifeAmount() {
        return lifeAmount;
    }

    /**
     * Sets the life amount.
     *
     * @param lifeAmount the lifeAmount to set
     */
    public final void setLifeAmount(final String lifeAmount) {
        this.lifeAmount = lifeAmount; // TODO Add 0 to parameter's name.
    }

    /**
     * Gets the phases.
     *
     * @return the phases
     */
    public final ArrayList<String> getPhases() {
        return phases;
    }

    /**
     * Sets the phases.
     *
     * @param phases the new phases
     */
    public final void setPhases(final ArrayList<String> phases) {
        this.phases = phases; // TODO Add 0 to parameter's name.
    }

} // end class SpellAbility_Variables
