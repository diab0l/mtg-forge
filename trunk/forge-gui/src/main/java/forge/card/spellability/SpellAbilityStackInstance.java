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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import forge.card.trigger.TriggerType;
import forge.game.card.Card;
import forge.game.player.Player;

/**
 * <p>
 * SpellAbility_StackInstance class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class SpellAbilityStackInstance {
    // At some point I want this functioning more like Target/Target Choices
    // where the SA has an "active"
    // Stack Instance, and instead of having duplicate parameters, it adds
    // changes directly to the "active" one
    // When hitting the Stack, the active SI gets "applied" to the Stack and
    // gets cleared from the base SI
    // Coming off the Stack would work similarly, except it would just add the
    // full active SI instead of each of the parts
    /** The ability. */
    private SpellAbility ability = null;

    /** The sub instace. */
    private SpellAbilityStackInstance subInstace = null;
    private final Player activator;

    // When going to a SubAbility that SA has a Instance Choice object
    /** The tc. */
    private TargetChoices tc = new TargetChoices();
    private List<Card> splicedCards = null;

    /** The stack description. */
    private String stackDescription = null;

    // Adjusted Mana Cost
    // private String adjustedManaCost = "";

    // Paid Mana Cost
    // private ArrayList<Mana> payingMana = new ArrayList<Mana>();
    // private ArrayList<AbilityMana> paidAbilities = new
    // ArrayList<AbilityMana>();
    private int xManaPaid = 0;

    // Other Paid things
    private HashMap<String, List<Card>> paidHash = new HashMap<String, List<Card>>();

    // Additional info
    // is Kicked, is Buyback

    // Triggers
    private HashMap<String, Object> triggeringObjects = new HashMap<String, Object>();

    private final HashMap<String, String> storedSVars = new HashMap<String, String>();

    /**
     * <p>
     * Constructor for SpellAbility_StackInstance.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public SpellAbilityStackInstance(final SpellAbility sa) {
        // Base SA info
        this.ability = sa;
        this.stackDescription = this.ability.getStackDescription();
        this.activator = sa.getActivatingPlayer();
        
        // Payment info
        this.paidHash = this.ability.getPaidHash();
        this.ability.resetPaidHash();
        this.splicedCards = sa.getSplicedCards();

        // TODO getXManaCostPaid should be on the SA, not the Card
        this.xManaPaid = sa.getSourceCard().getXManaCostPaid();

        // Triggering info
        this.triggeringObjects = sa.getTriggeringObjects();

        final AbilitySub subAb = this.ability.getSubAbility();
        if (subAb != null) {
            this.subInstace = new SpellAbilityStackInstance(subAb);
        }

        // Targeting info -- 29/06/11 Moved to after taking care of SubAbilities
        // because otherwise AF_DealDamage SubAbilities that use Defined$
        // Targeted breaks (since it's parents target is reset)
        if (sa.usesTargeting()) {
            this.tc = ability.getTargets();
            this.ability.resetTargets();
        }

        final Card source = this.ability.getSourceCard();

        // Store SVars and Clear
        for (final String store : Card.getStorableSVars()) {
            final String value = source.getSVar(store);
            if (value.length() > 0) {
                this.storedSVars.put(store, value);
                source.setSVar(store, "");
            }
        }
    }

    /**
     * <p>
     * getSpellAbility.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public final SpellAbility getSpellAbility() {
        this.ability.resetTargets();
        this.ability.setTargets(tc);
        this.ability.setActivatingPlayer(activator);

        // Saved sub-SA needs to be reset on the way out
        if (this.subInstace != null) {
            this.ability.setSubAbility((AbilitySub) this.subInstace.getSpellAbility());
        }

        // Set Cost specific things here
        this.ability.resetPaidHash();
        this.ability.setPaidHash(this.paidHash);
        this.ability.setSplicedCards(splicedCards);
        this.ability.getSourceCard().setXManaCostPaid(this.xManaPaid);

        // Triggered
        this.ability.setAllTriggeringObjects(this.triggeringObjects);

        // Add SVars back in
        final Card source = this.ability.getSourceCard();
        for (final String store : this.storedSVars.keySet()) {
            final String value = this.storedSVars.get(store);
            if (value.length() > 0) {
                source.setSVar(store, value);
            }
        }

        return this.ability;
    }

    // A bit of SA shared abilities to restrict conflicts
    /**
     * <p>
     * Getter for the field <code>stackDescription</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getStackDescription() {
        return this.stackDescription;
    }

    /**
     * <p>
     * getSourceCard.
     * </p>
     * 
     * @return a {@link forge.game.card.Card} object.
     */
    public final Card getSourceCard() {
        return this.ability.getSourceCard();
    }

    /**
     * <p>
     * isSpell.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isSpell() {
        return this.ability.isSpell();
    }

    /**
     * <p>
     * isAbility.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isAbility() {
        return this.ability.isAbility();
    }

    /**
     * <p>
     * isTrigger.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isTrigger() {
        return this.ability.isTrigger();
    }

    /**
     * <p>
     * isStateTrigger.
     * </p>
     * 
     * @param id
     *            a int.
     * @return a boolean.
     */
    public final boolean isStateTrigger(final int id) {
        return this.ability.getSourceTrigger() == id;
    }

    /**
     * Checks if is optional trigger.
     * 
     * @return true, if is optional trigger
     */
    public final boolean isOptionalTrigger() {
        return this.ability.isOptionalTrigger();
    }

    public final SpellAbilityStackInstance getSubInstace() {
        return this.subInstace;
    }

    public final TargetChoices getTargetChoices() {
        return this.tc;
    }

    public void updateTarget(TargetChoices target) {
        if (target != null) {
            this.tc = target;
            this.ability.setTargets(tc);
            this.stackDescription = this.ability.getStackDescription();
            // Run BecomesTargetTrigger
            HashMap<String, Object> runParams = new HashMap<String, Object>();
            runParams.put("SourceSA", this.ability);
            HashSet<Object> distinctObjects = new HashSet<Object>();
            for (final Object tgt : target.getTargets()) {
                if (distinctObjects.contains(tgt)) {
                    continue;
                }
                distinctObjects.add(tgt);
                if (tgt instanceof Card && !((Card) tgt).hasBecomeTargetThisTurn()) {
                    runParams.put("FirstTime", null);
                    ((Card) tgt).setBecameTargetThisTurn(true);
                }
                runParams.put("Target", tgt);
                this.getSourceCard().getGame().getTriggerHandler().runTrigger(TriggerType.BecomesTarget, runParams, false);
            }
        }
    }

    public boolean compareToSpellAbility(SpellAbility sa) {
        // Compare my target choices to the SA passed in
        // TODO? Compare other data points in the SI to the passed SpellAbility for confirmation
        SpellAbility compare = sa;
        SpellAbilityStackInstance sub = this;

        if (!compare.equals(sub.ability)){
            return false;
        }

        while (compare != null && sub != null) {
            TargetChoices choices = compare.getTargetRestrictions() != null ? compare.getTargets() : null;

            if (choices != null && !choices.equals(sub.getTargetChoices())) {
                return false;
            }
            compare = compare.getSubAbility();
            sub = sub.getSubInstace();
        }

        return true;
    }
    
    @Override
    public String toString() {
        return String.format("%s->%s", getSourceCard(), stackDescription);
    }    
}
