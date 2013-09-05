/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card.spellability;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Card;
import forge.GameEntity;
import forge.ITargetable;
import forge.card.ability.AbilityUtils;
import forge.card.ability.ApiType;
import forge.card.cost.Cost;
import forge.card.cost.CostPartMana;
import forge.card.mana.Mana;
import forge.card.mana.ManaCost;
import forge.game.Game;
import forge.game.player.Player;
import forge.util.TextUtil;

//only SpellAbility can go on the stack
//override any methods as needed
/**
 * <p>
 * Abstract SpellAbility class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class SpellAbility implements ISpellAbility, ITargetable {

    // choices for constructor isPermanent argument
    private String description = "";
    private String stackDescription = "";
    private ManaCost multiKickerManaCost = null;
    private Player activatingPlayer = null;

    private boolean temporary; // that is given by some static ability
    private boolean basicLandAbility; // granted by basic land type

    private Card sourceCard;
    private Card grantorCard = null; // card which grants the ability (equipment or owner of static ability that gave this one) 

    private List<Card> splicedCards = null;
//    private List<Card> targetList;
    // targetList doesn't appear to be used anymore

    private boolean basicSpell = true;
    private boolean trigger = false;
    private boolean optionalTrigger = false;
    private int sourceTrigger = -1;
    private boolean temporarilySuppressed = false;

    private boolean flashBackAbility = false;
    private boolean cycling = false;
    private boolean delve = false;
    private boolean offering = false;
    private boolean morphup = false;

    /** The pay costs. */
    private Cost payCosts = null;
    private SpellAbilityRestriction restrictions = new SpellAbilityRestriction();
    private SpellAbilityCondition conditions = new SpellAbilityCondition();
    private AbilitySub subAbility = null;

    protected Map<String, String> params = null;
    protected ApiType api = null;

    private final ArrayList<Mana> payingMana = new ArrayList<Mana>();
    private final List<SpellAbility> paidAbilities = new ArrayList<SpellAbility>();

    private HashMap<String, List<Card>> paidLists = new HashMap<String, List<Card>>();

    private HashMap<String, Object> triggeringObjects = new HashMap<String, Object>();

    private HashMap<String, Object> replacingObjects = new HashMap<String, Object>();

    private List<Card> tappedForConvoke = new ArrayList<Card>();
    private Card sacrificedAsOffering = null;

    private HashMap<String, String> sVars = new HashMap<String, String>();

    private AbilityManaPart manaPart = null;

    private boolean undoable;
    
    private boolean isCopied = false;

    public final AbilityManaPart getManaPart() {
        return manaPart;
    }

    public final AbilityManaPart getManaPartRecursive() {
        SpellAbility tail = this;
        while (tail != null) {
            if(tail.manaPart != null) 
                return tail.manaPart;
            tail = tail.getSubAbility();
        }
        return null;
    }
    
    public final boolean isManaAbility() {
        // Check whether spell or ability first
        if (this.isSpell()) 
            return false;
        // without a target
        if (this.usesTargeting()) return false;
        if (getRestrictions() != null && getRestrictions().getPlaneswalker()) 
            return false; //Loyalty ability, not a mana ability.

        return getManaPartRecursive() != null;
    }

    protected final void setManaPart(AbilityManaPart manaPart) {
        this.manaPart = manaPart;
    }

    public final String getSVar(final String name) {
        return sVars.get(name) != null ? sVars.get(name) : "";
    }

    public final void setSVar(final String name, final String value) {
        sVars.put(name, value);
    }

    public Set<String> getSVars() {
        return sVars.keySet();
    }

    /**
     * <p>
     * Constructor for SpellAbility.
     * </p>
     * 
     * @param spellOrAbility
     *            a int.
     * @param iSourceCard
     *            a {@link forge.Card} object.
     */
    public SpellAbility(final Card iSourceCard, Cost toPay) {
        this.sourceCard = iSourceCard;
        this.payCosts = toPay;
    }

    // Spell, and Ability, and other Ability objects override this method
    /**
     * <p>
     * canPlay.
     * </p>
     * 
     * @return a boolean.
     */
    public abstract boolean canPlay();

    // all Spell's and Abilities must override this method
    /**
     * <p>
     * resolve.
     * </p>
     */
    public abstract void resolve();

    /**
     * <p>
     * canPlayAI.
     * </p>
     * 
     * @return a boolean.
     */
    public /*final*/ boolean canPlayAI() {
        return true;
    }

    // This should be overridden by ALL AFs
    /**
     * <p>
     * doTrigger.
     * </p>
     * 
     * @param mandatory
     *            a boolean.
     * @param ai TODO
     * @return a boolean.
     */
    public boolean doTrigger(final boolean mandatory, Player ai) {
        return false;
    }

    /**
     * <p>
     * Getter for the field <code>multiKickerManaCost</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public ManaCost getMultiKickerManaCost() {
        return this.multiKickerManaCost;
    }

    /**
     * <p>
     * Setter for the field <code>multiKickerManaCost</code>.
     * </p>
     * 
     * @param cost
     *            a {@link java.lang.String} object.
     */
    public void setMultiKickerManaCost(final ManaCost cost) {
        this.multiKickerManaCost = cost;
    }


    /**
     * <p>
     * Getter for the field <code>activatingPlayer</code>.
     * </p>
     * 
     * @return a {@link forge.game.player.Player} object.
     */
    public Player getActivatingPlayer() {
        return this.activatingPlayer;
    }

    /**
     * <p>
     * Setter for the field <code>activatingPlayer</code>.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     */
    public void setActivatingPlayer(final Player player) {
        // trickle down activating player
        this.activatingPlayer = player;
        if (this.subAbility != null) {
            this.subAbility.setActivatingPlayer(player);
        }
    }

    /**
     * <p>
     * isSpell.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isSpell() { return false; }
    public boolean isAbility() { return true; }


     /**
     * <p>
     * isMultiKicker.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isMultiKicker() {
        return this.multiKickerManaCost != null && !this.isAnnouncing("Multikicker");
    }

    /**
     * <p>
     * setIsMorphUp.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setIsMorphUp(final boolean b) {
        this.morphup = b;
    }

    /**
     * <p>
     * isMorphUp.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isMorphUp() {
        return this.morphup;
    }

    /**
     * <p>
     * setIsCycling.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setIsCycling(final boolean b) {
        this.cycling = b;
    }

    /**
     * <p>
     * isCycling.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isCycling() {
        return this.cycling;
    }

    /**
     * <p>
     * Setter for the field <code>sourceCard</code>.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public void setSourceCard(final Card c) {
        this.sourceCard = c;
    }

    /**
     * <p>
     * Getter for the field <code>sourceCard</code>.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
    public Card getSourceCard() {
        return this.sourceCard;
    }

    /**
     * <p>
     * Setter for the field <code>originalHost</code>.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public void setOriginalHost(final Card c) {
        this.grantorCard = c;
    }

    /**
     * <p>
     * Getter for the field <code>originalHost</code>.
     * </p>
     * 
     * @return a {@link forge.Card} object.
     */
    public Card getOriginalHost() {
        return this.grantorCard;
    }

    public String getParamOrDefault(String key, String defaultValue) { 
        return params == null || !params.containsKey(key) ? defaultValue : params.get(key);
    }

    public String getParam(String key) {
        return params == null ? null : params.get(key);
    }
    public boolean hasParam(String key) {
        return params == null ? false : params.containsKey(key);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param mapParams
     */
    public void copyParamsToMap(Map<String, String> mapParams) {
        if (null != params) {
            mapParams.putAll(params);
        }
    }

    // If this is not null, then ability was made in a factory
    public ApiType getApi() {
        return api;
    }

    public final boolean isCurse() {
        return this.hasParam("IsCurse");
    }

    // begin - Input methods
    /**
     * <p>
     * Getter for the field <code>payCosts</code>.
     * </p>
     * 
     * @return a {@link forge.card.cost.Cost} object.
     */
    public Cost getPayCosts() {
        return this.payCosts;
    }

    /**
     * <p>
     * Setter for the field <code>payCosts</code>.
     * </p>
     * 
     * @param abCost
     *            a {@link forge.card.cost.Cost} object.
     */
    public void setPayCosts(final Cost abCost) {
        this.payCosts = abCost;
    }

    /**
     * <p>
     * Setter for the field <code>restrictions</code>.
     * </p>
     * 
     * @param restrict
     *            a {@link forge.card.spellability.SpellAbilityRestriction}
     *            object.
     */
    public void setRestrictions(final SpellAbilityRestriction restrict) {
        this.restrictions = restrict;
    }

    /**
     * <p>
     * Getter for the field <code>restrictions</code>.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbilityRestriction} object.
     */
    public SpellAbilityRestriction getRestrictions() {
        return this.restrictions;
    }

    /**
     * <p>
     * Shortcut to see how many activations there were.
     * </p>
     * 
     * @return the activations this turn
     */
    public int getActivationsThisTurn() {
        return this.restrictions.getNumberTurnActivations();
    }

    /**
     * <p>
     * Setter for the field <code>conditions</code>.
     * </p>
     * 
     * @param condition
     *            a {@link forge.card.spellability.SpellAbilityCondition}
     *            object.
     * @since 1.0.15
     */
    public final void setConditions(final SpellAbilityCondition condition) {
        this.conditions = condition;
    }

    /**
     * <p>
     * Getter for the field <code>conditions</code>.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbilityCondition} object.
     * @since 1.0.15
     */
    public SpellAbilityCondition getConditions() {
        return this.conditions;
    }

    /**
     * <p>
     * Getter for the field <code>payingMana</code>.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Mana> getPayingMana() {
        return this.payingMana;
    }

    /**
     * <p>
     * getPayingManaAbilities.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public List<SpellAbility> getPayingManaAbilities() {
        return this.paidAbilities;
    }

    // Combined PaidLists
    /**
     * <p>
     * setPaidHash.
     * </p>
     * 
     * @param hash
     *            a {@link java.util.HashMap} object.
     */
    public void setPaidHash(final HashMap<String, List<Card>> hash) {
        this.paidLists = hash;
    }

    /**
     * <p>
     * getPaidHash.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap<String, List<Card>> getPaidHash() {
        return this.paidLists;
    }

    /**
     * <p>
     * getPaidList.
     * </p>
     * 
     * @param str
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public List<Card> getPaidList(final String str) {
        return this.paidLists.get(str);
    }

    /**
     * <p>
     * addCostToHashList.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param str
     *            a {@link java.lang.String} object.
     */
    public void addCostToHashList(final Card c, final String str) {
        if (!this.paidLists.containsKey(str)) {
            this.paidLists.put(str, new ArrayList<Card>());
        }

        this.paidLists.get(str).add(c);
    }

    /**
     * <p>
     * resetPaidHash.
     * </p>
     */
    public void resetPaidHash() {
        this.paidLists = new HashMap<String, List<Card>>();
    }

    private EnumSet<OptionalCost> optionalCosts = EnumSet.noneOf(OptionalCost.class);
    /**
     * @return the optionalAdditionalCosts
     */
    public Iterable<OptionalCost> getOptionalCosts() {
        return optionalCosts;
    }

    /**
     * @param cost the optionalAdditionalCost to add
     */
    public final void addOptionalCost(OptionalCost cost) {
        // Optional costs are added to swallow copies of original SAs, 
        // Thus, to protect the original's set from changes, we make a copy right here.
        this.optionalCosts = EnumSet.copyOf(optionalCosts);
        this.optionalCosts.add(cost);
    }

    public boolean isBuyBackAbility() {
        return isOptionalCostPaid(OptionalCost.Buyback);
    }

    public boolean isKicked() {
        return isOptionalCostPaid(OptionalCost.Kicker1) || isOptionalCostPaid(OptionalCost.Kicker2);
    }

    public boolean isOptionalCostPaid(OptionalCost cost) {
        SpellAbility saRoot = this.getRootAbility();
        return saRoot.optionalCosts.contains(cost);
    }
    
    /**
     * <p>
     * Getter for the field <code>triggeringObjects</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     * @since 1.0.15
     */
    public HashMap<String, Object> getTriggeringObjects() {
        return this.triggeringObjects;
    }

    /**
     * <p>
     * setAllTriggeringObjects.
     * </p>
     * 
     * @param triggeredObjects
     *            a {@link java.util.HashMap} object.
     * @since 1.0.15
     */
    public void setAllTriggeringObjects(final HashMap<String, Object> triggeredObjects) {
        this.triggeringObjects = triggeredObjects;
    }

    /**
     * <p>
     * setTriggeringObject.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param o
     *            a {@link java.lang.Object} object.
     * @since 1.0.15
     */
    public void setTriggeringObject(final String type, final Object o) {
        this.triggeringObjects.put(type, o);
    }

    /**
     * <p>
     * getTriggeringObject.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     * @since 1.0.15
     */
    public Object getTriggeringObject(final String type) {
        return this.triggeringObjects.get(type);
    }

    /**
     * <p>
     * hasTriggeringObject.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @return a boolean.
     * @since 1.0.15
     */
    public boolean hasTriggeringObject(final String type) {
        return this.triggeringObjects.containsKey(type);
    }

    /**
     * <p>
     * resetTriggeringObjects.
     * </p>
     * 
     * @since 1.0.15
     */
    public void resetTriggeringObjects() {
        this.triggeringObjects = new HashMap<String, Object>();
    }

    /**
     * Gets the replacing objects.
     * 
     * @return the replacing objects
     */
    public HashMap<String, Object> getReplacingObjects() {
        return this.replacingObjects;
    }

    /**
     * Sets the replacing object.
     * 
     * @param type
     *            the type
     * @param o
     *            the o
     */
    public void setReplacingObject(final String type, final Object o) {
        this.replacingObjects.put(type, o);
    }

    /**
     * Gets the replacing object.
     * 
     * @param type
     *            the type
     * @return the replacing object
     */
    public Object getReplacingObject(final String type) {
        final Object res = this.replacingObjects.get(type);
        return res;
    }


    /**
     * <p>
     * resetOnceResolved.
     * </p>
     */
    public void resetOnceResolved() {
        this.resetPaidHash();
        this.resetTargets();
        this.resetTriggeringObjects();

        // Clear SVars
        for (final String store : Card.getStorableSVars()) {
            final String value = this.sourceCard.getSVar(store);
            if (value.length() > 0) {
                this.sourceCard.setSVar(store, "");
            }
        }
    }

    /**
     * <p>
     * Setter for the field <code>stackDescription</code>.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     */
    public void setStackDescription(final String s) {
        this.stackDescription = s;
        if ((this.description == "") && this.sourceCard.getText().equals("")) {
            this.description = s;
        }
    }

    /**
     * <p>
     * Getter for the field <code>stackDescription</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getStackDescription() {
        if (this.stackDescription.equals(this.getSourceCard().getText().trim())) {
            return this.getSourceCard().getName() + " - " + this.getSourceCard().getText();
        }

        return this.stackDescription.replaceAll("CARDNAME", this.getSourceCard().getName());
    }

    // setDescription() includes mana cost and everything like
    // "G, tap: put target creature from your hand onto the battlefield"
    /**
     * <p>
     * Setter for the field <code>description</code>.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     */
    public void setDescription(final String s) {
        this.description = s;
    }

    /**
     * <p>
     * Getter for the field <code>description</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        if (this.isSuppressed()) {
            return "";
        }

        return this.toUnsuppressedString();
    }

    /**
     * To unsuppressed string.
     * 
     * @return the string
     */
    public String toUnsuppressedString() {

        final StringBuilder sb = new StringBuilder();
        SpellAbility node = this;

        while (node != null) {
            if (node != this) {
                sb.append(" ");
            }

            sb.append(node.getDescription().replace("CARDNAME", node.getSourceCard().getName()));
            node = node.getSubAbility();

        }
        return sb.toString();
    }

    public void appendSubAbility(final AbilitySub toAdd) {
        SpellAbility tailend = this;
        while (tailend.getSubAbility() != null) {
            tailend = tailend.getSubAbility();
        }
        tailend.setSubAbility(toAdd);
    }
    
    
    /**
     * <p>
     * Setter for the field <code>subAbility</code>.
     * </p>
     * 
     * @param subAbility
     *            a {@link forge.card.spellability.AbilitySub} object.
     */
    public void setSubAbility(final AbilitySub subAbility) {
        this.subAbility = subAbility;
        if (subAbility != null) {
            subAbility.setParent(this);
        }
    }

    /**
     * <p>
     * Getter for the field <code>subAbility</code>.
     * </p>
     * 
     * @return a {@link forge.card.spellability.AbilitySub} object.
     */
    public AbilitySub getSubAbility() {
        return this.subAbility;
    }

    /**
     * <p>
     * isBasicAbility.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isBasicSpell() {
        return this.basicSpell && !this.isFlashBackAbility() && !this.isBuyBackAbility();
    }

    /**
     * <p>
     * Setter for the field <code>setBasicSpell</code>.
     * </p>
     * 
     * @param basicSpell
     *            a boolean.
     */
    public void setBasicSpell(final boolean basicSpell) {
        this.basicSpell = basicSpell;
    }

    /**
     * <p>
     * Setter for the field <code>flashBackAbility</code>.
     * </p>
     * 
     * @param flashBackAbility
     *            a boolean.
     */
    public void setFlashBackAbility(final boolean flashBackAbility) {
        this.flashBackAbility = flashBackAbility;
    }

    /**
     * <p>
     * isFlashBackAbility.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isFlashBackAbility() {
        return this.flashBackAbility;
    }

    /**
     * <p>
     * copy.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public SpellAbility copy() {
        SpellAbility clone = null;
        try {
            clone = (SpellAbility) this.clone();
        } catch (final CloneNotSupportedException e) {
            System.err.println(e);
        }
        return clone;
    }
    
    public SpellAbility copyWithNoManaCost() {
        final SpellAbility newSA = this.copy();
        newSA.setPayCosts(newSA.getPayCosts().copyWithNoMana());
        newSA.setDescription(newSA.getDescription() + " (without paying its mana cost)");
        return newSA;
    }

    /**
     * <p>
     * Setter for the field <code>trigger</code>.
     * </p>
     * 
     * @param trigger
     *            a boolean.
     */
    public void setTrigger(final boolean trigger) {
        this.trigger = trigger;
    }

    /**
     * <p>
     * isTrigger.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isTrigger() {
        return this.trigger;
    }

    /**
     * Sets the optional trigger.
     * 
     * @param optrigger
     *            the new optional trigger
     */
    public void setOptionalTrigger(final boolean optrigger) {
        this.optionalTrigger = optrigger;
    }

    /**
     * Checks if is optional trigger.
     * 
     * @return true, if is optional trigger
     */
    public boolean isOptionalTrigger() {
        return this.optionalTrigger;
    }

    /**
     * <p>
     * setSourceTrigger.
     * </p>
     * 
     * @param id
     *            a int.
     */
    public void setSourceTrigger(final int id) {
        this.sourceTrigger = id;
    }

    /**
     * <p>
     * getSourceTrigger.
     * </p>
     * 
     * @return a int.
     */
    public int getSourceTrigger() {
        return this.sourceTrigger;
    }

    /**
     * <p>
     * isMandatory.
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isMandatory() {
        return false;
    }

    /**
     * <p>
     * canTarget.
     * </p>
     * 
     * @param entity
     *            a GameEntity
     * @return a boolean.
     */
    public final boolean canTarget(final ITargetable entity) {
        final TargetRestrictions tr = this.getTargetRestrictions();
        
        // Restriction related to this ability
        if (tr != null) {
            if (tr.isUniqueTargets() && this.getUniqueTargets().contains(entity))
                return false;
            
            // If the cards must have a specific controller
            if (hasParam("TargetsWithDefinedController") && entity instanceof Card) {
                final Card c = (Card) entity;
                List<Player> pl = AbilityUtils.getDefinedPlayers(getSourceCard(), getParam("TargetsWithDefinedController"), this);
                if (pl == null || !pl.contains(c.getController()) ) {
                    return false;
                }
            }

            String[] validTgt = tr.getValidTgts();
            if (entity instanceof GameEntity && !((GameEntity) entity).isValid(validTgt, this.getActivatingPlayer(), this.getSourceCard()))
               return false;
        }

        // Restrictions coming from target
        return entity.canBeTargetedBy(this);
    }

    // is this a wrapping ability (used by trigger abilities)
    /**
     * <p>
     * isWrapper.
     * </p>
     * 
     * @return a boolean.
     * @since 1.0.15
     */
    public boolean isWrapper() {
        return false;
    }

    /**
     * Sets the temporarily suppressed.
     * 
     * @param supp
     *            the new temporarily suppressed
     */
    public final void setTemporarilySuppressed(final boolean supp) {
        this.temporarilySuppressed = supp;
    }

    /**
     * Checks if is suppressed.
     * 
     * @return true, if is suppressed
     */
    public final boolean isSuppressed() {
        return (this.temporarilySuppressed);
    }

    /**
     * Gets the checks if is delve.
     * 
     * @return the isDelve
     */
    public final boolean isDelve() {
        return this.delve;
    }

    /**
     * Sets the checks if is delve.
     * 
     * @param isDelve0
     *            the isDelve to set
     */
    public final void setDelve(final boolean isDelve0) {
        this.delve = isDelve0;
    }

    /**
     * Adds the tapped for convoke.
     * 
     * @param c
     *            the c
     */
    public void addTappedForConvoke(final Card c) {
        if (this.tappedForConvoke == null) {
            this.tappedForConvoke = new ArrayList<Card>();
        }

        this.tappedForConvoke.add(c);
    }

    /**
     * Gets the tapped for convoke.
     * 
     * @return the tapped for convoke
     */
    public List<Card> getTappedForConvoke() {
        return this.tappedForConvoke;
    }

    /**
     * Clear tapped for convoke.
     */
    public void clearTappedForConvoke() {
        if (this.tappedForConvoke != null) {
            this.tappedForConvoke.clear();
        }
    }

    /**
     * Returns whether the SA is a patron offering.
     */
    public boolean isOffering() {
        return this.offering;
    }

    /**
     * Sets the SA as a patron offering.
     * 
     * @param c      card sacrificed for a patron offering
     */
    public void setIsOffering(final boolean bOffering) {
        this.offering = bOffering;
    }

    /**
     * Sets the card sacrificed for a patron offering.
     * 
     * @param c      card sacrificed for a patron offering
     */
    public void setSacrificedAsOffering(final Card c) {
        this.sacrificedAsOffering = c;
    }

    /**
     * Gets the card sacrificed for a patron offering.
     * 
     * @return the card sacrificed for a patron offering
     */
    public Card getSacrificedAsOffering() {
        return this.sacrificedAsOffering;
    }

    /**
     * Clear the card sacrificed for a patron offering.
     */
    public void resetSacrificedAsOffering() {
        this.sacrificedAsOffering = null;
    }

    /**
     * @return the splicedCards
     */
    public List<Card> getSplicedCards() {
        return splicedCards;
    }

    /**
     * @param splicedCard the splicedCards to set
     */
    public void setSplicedCards(List<Card> splicedCards) {
        this.splicedCards = splicedCards;
    }

    /**
     * @param splicedCard the splicedCard to add
     */
    public void addSplicedCards(Card splicedCard) {
        if (this.splicedCards == null) {
            this.splicedCards = new ArrayList<Card>();
        }
        this.splicedCards.add(splicedCard);
    }

    /**
     * <p>
     * knownDetermineDefined.
     * </p>
     * 
     * @param defined
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public List<Card> knownDetermineDefined(final String defined) {
        final List<Card> ret = new ArrayList<Card>();
        final List<Card> list = AbilityUtils.getDefinedCards(getSourceCard(), defined, this);
        final Game game = getActivatingPlayer().getGame();
        
        for (final Card c : list) {
            final Card actualCard = game.getCardState(c);
            ret.add(actualCard);
        }
        return ret;
    }

    /**
     * <p>
     * findRootAbility.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public SpellAbility getRootAbility() {
        SpellAbility parent = this;
        while (null != parent.getParent()) {
            parent = parent.getParent();
        }

        return parent;
    }

    public SpellAbility getParent() {
        return null;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public boolean isUndoable() {
        return this.undoable && this.payCosts.isUndoable() && this.getSourceCard().isInPlay();
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void undo() {
        if (isUndoable()) {
            this.payCosts.refundPaidCost(sourceCard);
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param b
     */
    public void setUndoable(boolean b) {
        this.undoable = b;
    }

    /**
     * @return the isCopied
     */
    public boolean isCopied() {
        return isCopied;
    }

    /**
     * @param isCopied0 the isCopied to set
     */
    public void setCopied(boolean isCopied0) {
        this.isCopied = isCopied0; 
    }

    /**
     * Returns whether variable was present in the announce list.
     */
    public boolean isAnnouncing(String variable) {
        String announce = getParam("Announce");
        if (StringUtils.isBlank(announce)) return false;
        String[] announcedOnes = TextUtil.split(announce, ',');
        for(String a : announcedOnes) {
            if( a.trim().equalsIgnoreCase(variable))
                return true;
        }
        return false;
    }

    public boolean isXCost() {
        CostPartMana cm = payCosts != null ? getPayCosts().getCostMana() : null;
        return cm != null && cm.getAmountOfX() > 0; 
    }

    public boolean isBasicLandAbility() {
        return basicLandAbility;
    }

    public void setBasicLandAbility(boolean basicLandAbility) {
        this.basicLandAbility = basicLandAbility; // TODO: Add 0 to parameter's name.
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary; // TODO: Add 0 to parameter's name.
    }
    
    @Override
    public boolean canBeTargetedBy(SpellAbility sa) {
        return sa.canTargetSpellAbility(this);
    }

    /** The chosen target. */
    private TargetRestrictions targetRestricions = null;
    private TargetChoices targetChosen = new TargetChoices();

    public boolean usesTargeting() { 
        return targetRestricions != null;
    }
    
    public TargetRestrictions getTargetRestrictions() {
        return targetRestricions;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // THE CODE BELOW IS RELATED TO TARGETING. It might be extracted to other class from here
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setTargetRestrictions(final TargetRestrictions tgt) {
        targetRestricions = tgt;
    }

    /**
     * Gets the chosen target.
     * 
     * @return the chosenTarget
     */
    public TargetChoices getTargets() {
        return this.targetChosen;
    }

    public void setTargets(TargetChoices targets) {
        this.targetChosen = targets;
    }
    
    public void resetTargets() {
        targetChosen = new TargetChoices();
    }

    /**
     * <p>
     * getAllTargetChoices.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     * @since 1.0.15
     */
    public final ArrayList<TargetChoices> getAllTargetChoices() {
        final ArrayList<TargetChoices> res = new ArrayList<TargetChoices>();
    
        SpellAbility sa = this.getRootAbility();
        if (sa.getTargetRestrictions() != null) {
            res.add(sa.getTargets());
        }
        while (sa.getSubAbility() != null) {
            sa = sa.getSubAbility();
    
            if (sa.getTargetRestrictions() != null) {
                res.add(sa.getTargets());
            }
        }
    
        return res;
    }

    public Card getTargetCard() {
        return targetChosen.getFirstTargetedCard();
    }

    /**
     * <p>
     * Setter for the field <code>targetCard</code>.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     */
    public void setTargetCard(final Card card) {
        if (card == null) {
            System.out.println(this.getSourceCard()
                    + " - SpellAbility.setTargetCard() called with null for target card.");
            return;
        }
    
        resetTargets();
        targetChosen.add(card);
        
        final String desc;
    
        if (!card.isFaceDown()) {
            desc = this.getSourceCard().getName() + " - targeting " + card;
        } else {
            desc = this.getSourceCard().getName() + " - targeting Morph(" + card.getUniqueNumber() + ")";
        }
        this.setStackDescription(desc);
    }

    /**
     * <p>
     * findTargetCards.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public List<Card> findTargetedCards() {
        // First search for targeted cards associated with current ability
        if (targetChosen.isTargetingAnyCard()) {
            return Lists.newArrayList(targetChosen.getTargetCards());
        }
        
        // Next search for source cards of targeted SAs associated with current ability
        if (targetChosen.isTargetingAnySpell()) {
            List<Card> res = Lists.newArrayList();
            for (final SpellAbility ability : targetChosen.getTargetSpells()) {
                res.add(ability.getSourceCard());
            }
            return res;
        }
        
        // Lastly Search parent SAs that targets a card
        SpellAbility parent = this.getParentTargetingCard();
        if (null != parent) {
            return parent.findTargetedCards();
        }
        
        // Lastly Search parent SAs that targets an SA
        parent = this.getParentTargetingSA();
        if (null != parent) {
            return parent.findTargetedCards();
        }
        
        return ImmutableList.<Card>of();
    }


    public SpellAbility getSATargetingCard() {
        return targetChosen.isTargetingAnyCard() ? this : getParentTargetingCard();
    }

    public SpellAbility getParentTargetingCard() {
        SpellAbility parent = this.getParent();
        while (parent != null) {
            if (parent.targetChosen.isTargetingAnyCard())
                return parent;
            parent = parent.getParent();
        }
        return null;
    }

    public SpellAbility getSATargetingSA() {
        return targetChosen.isTargetingAnySpell() ? this : getParentTargetingSA();
    }

    public SpellAbility getParentTargetingSA() {
        SpellAbility parent = this.getParent();
        while (parent != null) {
            if (parent.targetChosen.isTargetingAnySpell())
                return parent;
            parent = parent.getParent();
        }
        return null;
    }

    public SpellAbility getSATargetingPlayer() {
        return targetChosen.isTargetingAnyPlayer() ? this : getParentTargetingPlayer();
    }

    /**
     * <p>
     * findParentsTargetedPlayer.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public SpellAbility getParentTargetingPlayer() {
        SpellAbility parent = this.getParent();
        while (parent != null) {
            if (parent.getTargets().isTargetingAnyPlayer())
                return parent;
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Gets the unique targets.
     * 
     * @param ability
     *            the ability
     * @return the unique targets
     */
    public final List<ITargetable> getUniqueTargets() {
        final List<ITargetable> targets = new ArrayList<ITargetable>();
        SpellAbility child = this.getParent();
        while (child != null) {
            if (child.getTargetRestrictions() != null) {
                Iterables.addAll(targets, child.getTargets().getTargets());
            }
            child = child.getParent();
        }
        return targets;
    }

    public boolean canTargetSpellAbility(final SpellAbility topSA) {
        final TargetRestrictions tgt = this.getTargetRestrictions();
        final String saType = tgt.getTargetSpellAbilityType();
    
        if (null == saType) {
            // just take this to mean no restrictions - carry on.
        } else if (topSA instanceof Spell) {
            if (!saType.contains("Spell")) {
                return false;
            }
        } else if (topSA.isTrigger()) {
            if (!saType.contains("Triggered")) {
                return false;
            }
        } else if (topSA instanceof AbilityActivated) {
            if (!saType.contains("Activated")) {
                return false;
            }
        } else {
            return false; //Static ability? Whatever.
        }
    
        final String splitTargetRestrictions = tgt.getSAValidTargeting();
        if (splitTargetRestrictions != null) {
            // TODO What about spells with SubAbilities with Targets?
    
            final TargetChoices matchTgt = topSA.getTargets();
    
            if (matchTgt == null) {
                return false;
            }
    
            boolean result = false;
    
            for (final ITargetable o : matchTgt.getTargets()) {
                if (matchesValid(o, splitTargetRestrictions.split(","))) {
                    result = true;
                    break;
                }
            }
    
            if (!result) {
                return false;
            }
        }
    
        if (tgt.isSingleTarget()) {
            int totalTargets = 0;
            for(TargetChoices tc : topSA.getAllTargetChoices()) {
                totalTargets += tc.getNumTargeted();
                if (totalTargets > 1) {
                    // As soon as we get more than one, bail out
                    return false;
                }
            }
            if (totalTargets != 1) {
                // Make sure that there actually is one target
                return false;
            }
        }
    
        return topSA.getSourceCard().isValid(tgt.getValidTgts(), this.getActivatingPlayer(), this.getSourceCard());
    }

    private boolean matchesValid(final ITargetable o, final String[] valids) {
        final Card srcCard = this.getSourceCard();
        final Player activatingPlayer = this.getActivatingPlayer();
        if (o instanceof Card) {
            final Card c = (Card) o;
            return c.isValid(valids, activatingPlayer, srcCard);
        }
    
        if (o instanceof Player) {
            Player p = (Player) o;
            if (p.isValid(valids, activatingPlayer, srcCard)) {
                return true;
            }
        }
    
        return false;
    }
}
