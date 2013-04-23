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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import forge.Card;
import forge.card.CardType;
import forge.card.ability.AbilityUtils;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * <p>
 * Target class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Target {
    // Target has two things happening:
    // Targeting restrictions (Creature, Min/Maxm etc) which are true for this

    // Target Choices (which is specific for the StackInstance)
    private Card srcCard;
    // What this Object is restricted to targeting
    private boolean tgtValid = false;
    private String[] validTgts;
    private String vtSelection = "";
    private List<ZoneType> tgtZone = Arrays.asList(ZoneType.Battlefield);

    //SpellAbility Restrictions
    // Comma-separated <Spell,Activated,Triggered>
    private String targetSpellAbilityType = null;
    // The target SA of this SA must be targeting a Valid X
    private String saValidTargeting = null;
    
    // Additional restrictions that may not fit into Valid
    private boolean uniqueTargets = false;
    private boolean singleZone = false;
    private boolean differentZone = false;
    private boolean differentControllers = false;
    private boolean sameController = false;
    private boolean withoutSameCreatureType = false;
    private boolean singleTarget = false;
    private boolean randomTarget = false;
    private String definedController = null;

    // How many can be targeted?
    private String minTargets;
    private String maxTargets;

    // What Choices are actually made for targeting
    private TargetChoices choice = null;

    // For "Divided" cards. Is this better in TargetChoices?
    private boolean dividedAsYouChoose = false;
    private HashMap<Object, Integer> dividedMap = new HashMap<Object, Integer>();
    private int stillToDivide = 0;
    
    // Not sure what's up with Mandatory? Why wouldn't targeting be mandatory?
    private boolean bMandatory = false;

    /**
     * <p>
     * Copy Constructor for Target.
     * </p>
     * 
     * @param target
     *            a {@link forge.card.spellability.Target} object.
     */
    public Target(final Target target) {
        this.tgtValid = true;
        this.srcCard = target.getSourceCard();
        this.vtSelection = target.getVTSelection();
        this.validTgts = target.getValidTgts();
        this.minTargets = target.getMinTargets();
        this.maxTargets = target.getMaxTargets();
        this.tgtZone = target.getZone();
        this.targetSpellAbilityType = target.getTargetSpellAbilityType();
        this.saValidTargeting = target.getSAValidTargeting();
        this.dividedAsYouChoose = target.isDividedAsYouChoose();
        this.uniqueTargets = target.isUniqueTargets();
        this.singleZone = target.isSingleZone();
        this.differentControllers = target.isDifferentControllers();
        this.differentZone = target.isDifferentZone();
        this.sameController = target.isSameController();
        this.withoutSameCreatureType = target.isWithoutSameCreatureType();
        this.definedController = target.getDefinedController();
        this.singleTarget = target.isSingleTarget();
        this.choice = target.getTargetChoices();
    }

    /**
     * <p>
     * Constructor for Target.
     * </p>
     * 
     * @param src
     *            a {@link forge.Card} object.
     * @param select
     *            a {@link java.lang.String} object.
     * @param valid
     *            an array of {@link java.lang.String} objects.
     */
    public Target(final Card src, final String select, final String[] valid) {
        this(src, select, valid, "1", "1");
    }

    /**
     * <p>
     * Constructor for Target.
     * </p>
     * 
     * @param src
     *            a {@link forge.Card} object.
     * @param select
     *            a {@link java.lang.String} object.
     * @param valid
     *            a {@link java.lang.String} object.
     */
    public Target(final Card src, final String select, final String valid) {
        this(src, select, valid.split(","), "1", "1");
    }

    /**
     * <p>
     * Constructor for Target.
     * </p>
     * 
     * @param src
     *            a {@link forge.Card} object.
     * @param select
     *            a {@link java.lang.String} object.
     * @param valid
     *            an array of {@link java.lang.String} objects.
     * @param min
     *            a {@link java.lang.String} object.
     * @param max
     *            a {@link java.lang.String} object.
     */
    public Target(final Card src, final String select, final String[] valid, final String min, final String max) {
        this.srcCard = src;
        this.tgtValid = true;
        this.vtSelection = select;
        this.validTgts = valid;

        this.minTargets = min;
        this.maxTargets = max;
    }
    
    /**
     * <p>
     * getSourceCard.
     * </p>
     * 
     * @return a Card object.
     */
    public final Card getSourceCard() {
        return this.srcCard;
    }

    /**
     * <p>
     * setSourceCard.
     * </p>
     * 
     * @param source
     *            a Card object.
     */
    public final void setSourceCard(final Card source) {
        this.srcCard = source;
    }

    /**
     * <p>
     * getTargetChoices.
     * </p>
     * 
     * @return a {@link forge.card.spellability.TargetChoices} object.
     */
    public final TargetChoices getTargetChoices() {
        return this.choice;
    }

    /**
     * <p>
     * setTargetChoices.
     * </p>
     * 
     * @param tc
     *            a {@link forge.card.spellability.TargetChoices} object.
     */
    public final void setTargetChoices(final TargetChoices tc) {
        this.choice = tc;
    }

    /**
     * <p>
     * getMandatory.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getMandatory() {
        return this.bMandatory;
    }

    /**
     * <p>
     * setMandatory.
     * </p>
     * 
     * @param m
     *            a boolean.
     */
    public final void setMandatory(final boolean m) {
        this.bMandatory = m;
    }

    /**
     * <p>
     * doesTarget.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean doesTarget() {
        return this.tgtValid;
    }

    /**
     * <p>
     * getValidTgts.
     * </p>
     * 
     * @return an array of {@link java.lang.String} objects.
     */
    public final String[] getValidTgts() {
        return this.validTgts;
    }

    /**
     * <p>
     * getVTSelection.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getVTSelection() {
        return this.vtSelection;
    }

    /**
     * Gets the min targets.
     *
     * @return the min targets
     */
    private final String getMinTargets() {
        return this.minTargets;
    }

    /**
     * Gets the max targets.
     *
     * @return the max targets
     */
    private final String getMaxTargets() {
        return this.maxTargets;
    }

    /**
     * <p>
     * Getter for the field <code>minTargets</code>.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a int.
     */
    public final int getMinTargets(final Card c, final SpellAbility sa) {
        return AbilityUtils.calculateAmount(c, this.minTargets, sa);
    }

    /**
     * <p>
     * Getter for the field <code>maxTargets</code>.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a int.
     */
    public final int getMaxTargets(final Card c, final SpellAbility sa) {
        return AbilityUtils.calculateAmount(c, this.maxTargets, sa);
    }

    /**
     * <p>
     * isMaxTargetsChosen.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public final boolean isMaxTargetsChosen(final Card c, final SpellAbility sa) {
        return (this.choice != null) && (this.getMaxTargets(c, sa) == this.choice.getNumTargeted());
    }

    /**
     * <p>
     * isMinTargetsChosen.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public final boolean isMinTargetsChosen(final Card c, final SpellAbility sa) {
        if (this.getMinTargets(c, sa) == 0) {
            return true;
        }
        return (this.choice != null) && (this.getMinTargets(c, sa) <= this.choice.getNumTargeted());
    }

    /**
     * <p>
     * setZone.
     * </p>
     * 
     * @param tZone
     *            a {@link java.lang.String} object.
     */
    public final void setZone(final ZoneType tZone) {
        this.tgtZone = Arrays.asList(tZone);
    }

    /**
     * Sets the zone.
     * 
     * @param tZone
     *            the new zone
     */
    public final void setZone(final List<ZoneType> tZone) {
        this.tgtZone = tZone;
    }

    /**
     * <p>
     * getZone.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final List<ZoneType> getZone() {
        return this.tgtZone;
    }

    /**
     * <p>
     * Setter for the field <code>targetSpellAbilityType</code>.
     * </p>
     * 
     * @param tgtSAType
     *            a {@link java.lang.String} object.
     */
    public final void setTargetSpellAbilityType(final String tgtSAType) {
        this.targetSpellAbilityType = tgtSAType;
    }

    /**
     * <p>
     * Getter for the field <code>targetSpellAbilityType</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getTargetSpellAbilityType() {
        return this.targetSpellAbilityType;
    }

    /**
     * <p>
     * setSAValidTargeting.
     * </p>
     * 
     * @param saValidTgting
     *            a {@link java.lang.String} object.
     */
    public final void setSAValidTargeting(final String saValidTgting) {
        this.saValidTargeting = saValidTgting;
    }

    /**
     * <p>
     * getSAValidTargeting.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getSAValidTargeting() {
        return this.saValidTargeting;
    }

    // Leaving old structure behind for compatibility.
    /**
     * <p>
     * addTarget.
     * </p>
     * 
     * @param o
     *            a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public final boolean addTarget(final Object o) {
        if (this.choice == null) {
            this.choice = new TargetChoices();
        }

        if (o instanceof Card) {
            return this.choice.addTarget((Card) o);
        }

        if (o instanceof Player) {
            return this.choice.addTarget((Player) o);
        }

        if (o instanceof SpellAbility) {
            return this.choice.addTarget((SpellAbility) o);
        }

        return false;
    }

    /**
     * <p>
     * getTargetCards.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final List<Card> getTargetCards() {
        if (this.choice == null) {
            return new ArrayList<Card>();
        }

        return this.choice.getTargetCards();
    }

    /**
     * <p>
     * getTargetPlayers.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final List<Player> getTargetPlayers() {
        if (this.choice == null) {
            return new ArrayList<Player>();
        }

        return this.choice.getTargetPlayers();
    }

    /**
     * <p>
     * getTargetSAs.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final List<SpellAbility> getTargetSAs() {
        if (this.choice == null) {
            return new ArrayList<SpellAbility>();
        }

        return this.choice.getTargetSAs();
    }

    /**
     * <p>
     * getTargets.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    public final ArrayList<Object> getTargets() {
        if (this.choice == null) {
            return new ArrayList<Object>();
        }

        return this.choice.getTargets();
    }

    /**
     * <p>
     * getNumTargeted.
     * </p>
     * 
     * @return a int.
     */
    public final int getNumTargeted() {
        if (this.choice == null) {
            return 0;
        }
        return this.choice.getNumTargeted();
    }

    /**
     * <p>
     * resetTargets.
     * </p>
     */
    public final void resetTargets() {
        this.choice = null;
    }

    /**
     * <p>
     * getTargetedString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getTargetedString() {
        final ArrayList<Object> tgts = this.getTargets();
        final StringBuilder sb = new StringBuilder("");
        for (final Object o : tgts) {
            if (o instanceof Player) {
                final Player p = (Player) o;
                sb.append(p.getName());
            }
            if (o instanceof Card) {
                final Card c = (Card) o;
                sb.append(c);
            }
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * <p>
     * canOnlyTgtOpponent.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean canOnlyTgtOpponent() {
        boolean player = false;
        boolean opponent = false;
        for (final String s : this.validTgts) {
            if (s.startsWith("Opponent")) {
                opponent = true;
            } else if (s.startsWith("Player")) {
                player = true;
            }
        }
        return opponent && !player;
    }

    /**
     * <p>
     * canTgtPlayer.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean canTgtPlayer() {
        for (final String s : this.validTgts) {
            if (s.startsWith("Player") || s.startsWith("Opponent")) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * canTgtCreature.
     * </p>
     * 
     * @return a boolean.
     */

    public final boolean canTgtPermanent() {
        for (final String s : this.validTgts) {
            if (s.contains("Permanent")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Can tgt creature.
     * 
     * @return true, if successful
     */
    public final boolean canTgtCreature() {
        for (final String s : this.validTgts) {
            if ((s.contains("Creature") || CardType.isACreatureType(s) || s.startsWith("Permanent"))
                    && !s.contains("nonCreature")) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * canTgtCreatureAndPlayer.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean canTgtCreatureAndPlayer() {
        return this.canTgtPlayer() && this.canTgtCreature();
    }

    /**
     * <p>
     * hasCandidates.
     * </p>
     * 
     * @param sa
     *            the sa
     * @param isTargeted
     *            Check Valid Candidates and Targeting
     * @return a boolean.
     */
    public final boolean hasCandidates(final SpellAbility sa, final boolean isTargeted) {
        final GameState game = sa.getActivatingPlayer().getGame();
        for (Player player : game.getPlayers()) {
            if (sa.canTarget(player)) {
                return true;
            }
        }

        if (this.tgtZone.contains(ZoneType.Stack)) {
            // Stack Zone targets are considered later
            return true;
        } else {
            for (final Card c : game.getCardsIn(this.tgtZone)) {
                boolean isValidTarget = c.isValid(this.validTgts, this.srcCard.getController(), this.srcCard);
                boolean canTarget = (!isTargeted || c.canBeTargetedBy(sa));
                boolean isAlreadyTargeted = this.getTargetCards().contains(c);
                //System.out.print(c);
                if (isValidTarget && canTarget && !isAlreadyTargeted) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * <p>
     * getNumCandidates.
     * </p>
     * 
     * @param sa
     *            the sa
     * @param isTargeted
     *            Check Valid Candidates and Targeting
     * @return a int.
     */
    public final int getNumCandidates(final SpellAbility sa, final boolean isTargeted) {
        return getAllCandidates(sa, isTargeted).size();
    }

    /**
     * <p>
     * getAllCandidates.
     * </p>
     * 
     * @param sa
     *            the sa
     * @param isTargeted
     *            Check Valid Candidates and Targeting
     * @return a List<Object>.
     */
    public final List<Object> getAllCandidates(final SpellAbility sa, final boolean isTargeted) {
        final GameState game = sa.getActivatingPlayer().getGame();
        List<Object> candidates = new ArrayList<Object>();
        for (Player player : game.getPlayers()) {
            if (sa.canTarget(player)) {
                candidates.add(player);
            }
        }

        if (this.tgtZone.contains(ZoneType.Stack)) {
            for (final Card c : game.getStackZone().getCards()) {
                boolean isValidTarget = c.isValid(this.validTgts, this.srcCard.getController(), this.srcCard);
                boolean canTarget = (!isTargeted || c.canBeTargetedBy(sa));
                boolean isAlreadyTargeted = this.getTargetCards().contains(c);
                if (isValidTarget && canTarget && !isAlreadyTargeted) {
                    candidates.add(c);
                }
            }
        } else {
            for (final Card c : game.getCardsIn(this.tgtZone)) {
                boolean isValidTarget = c.isValid(this.validTgts, this.srcCard.getController(), this.srcCard);
                boolean canTarget = (!isTargeted || c.canBeTargetedBy(sa));
                boolean isAlreadyTargeted = this.getTargetCards().contains(c);
                if (isValidTarget && canTarget && !isAlreadyTargeted) {
                    candidates.add(c);
                }
            }
        }

        return candidates;
    }

    /**
     * Checks if is unique targets.
     * 
     * @return true, if is unique targets
     */
    public final boolean isUniqueTargets() {
        return this.uniqueTargets;
    }

    /**
     * Sets the unique targets.
     * 
     * @param unique
     *            the new unique targets
     */
    public final void setUniqueTargets(final boolean unique) {
        this.uniqueTargets = unique;
    }

    /**
     * Checks if targets must be from a single zone.
     * 
     * @return true, if singleZone
     */
    public final boolean isSingleZone() {
        return this.singleZone;
    }

    /**
     * Sets if targets must be from a single zone.
     * 
     * @param single
     *            the new singleZone
     */
    public final void setSingleZone(final boolean single) {
        this.singleZone = single;
    }

    /**
     * @return the withoutSameCreatureType
     */
    public boolean isWithoutSameCreatureType() {
        return withoutSameCreatureType;
    }

    /**
     * @param b the withoutSameCreatureType to set
     */
    public void setWithoutSameCreatureType(boolean b) {
        this.withoutSameCreatureType = b;
    }

    /**
     * <p>
     * copy.
     * </p>
     * 
     * @return a {@link forge.card.spellability.Target} object.
     */
    public Target copy() {
        Target clone = null;
        try {
            clone = (Target) this.clone();
        } catch (final CloneNotSupportedException e) {
            System.err.println(e);
        }
        return clone;
    }

    /**
     * @return the differentZone
     */
    public boolean isDifferentZone() {
        return differentZone;
    }

    /**
     * @param different the differentZone to set
     */
    public void setDifferentZone(boolean different) {
        this.differentZone = different;
    }

    /**
     * @return the randomTarget
     */
    public boolean isRandomTarget() {
        return randomTarget;
    }

    /**
     * @param random the randomTarget to set
     */
    public void setRandomTarget(boolean random) {
        this.randomTarget = random;
    }

    /**
     * @return the differentControllers
     */
    public boolean isDifferentControllers() {
        return differentControllers;
    }

    /**
     * @param different the differentControllers to set
     */
    public void setDifferentControllers(boolean different) {
        this.differentControllers = different;
    }
    /**
     * Checks if is same controller.
     * 
     * @return true, if it targets same controller
     */
    public final boolean isSameController() {
        return this.sameController;
    }

    /**
     * Sets the same controller.
     * 
     * @param same
     *            the new unique targets
     */
    public final void setSameController(final boolean same) {
        this.sameController = same;
    }

    /**
     * @return the definedController
     */
    public String getDefinedController() {
        return definedController;
    }

    /**
     * @param defined the definedController to set
     */
    public void setDefinedController(String defined) {
        this.definedController = defined;
    }

    /**
     * @return the singleTarget
     */
    public boolean isSingleTarget() {
        return singleTarget;
    }

    /**
     * @param singleTarget the singleTarget to set
     */
    public void setSingleTarget(boolean singleTarget) {
        this.singleTarget = singleTarget;
    }

    /**
     * @return a boolean dividedAsYouChoose
     */
    public boolean isDividedAsYouChoose() {
        return this.dividedAsYouChoose;
    }

    /**
     * @param divided the boolean to set
     */
    public void setDividedAsYouChoose(boolean divided) {
        this.dividedAsYouChoose = divided;
    }

    /**
     * Get the amount remaining to distribute.
     * @return int stillToDivide
     */
    public int getStillToDivide() {
        return this.stillToDivide;
    }

    /**
     * @param remaining set the amount still to be divided
     */
    public void setStillToDivide(final int remaining) {
        this.stillToDivide = remaining;
    }
    
    public void calculateStillToDivide(String toDistribute, Card source, SpellAbility sa) {
        // Recalculate this value just in case it's variable
        if (!this.dividedAsYouChoose) {
            return;
        }

        if (toDistribute.matches("[0-9][0-9]")) {
            this.setStillToDivide(Integer.parseInt(toDistribute));
        } else if (source.getSVar(toDistribute).equals("xPaid")) {
            this.setStillToDivide(source.getXManaCostPaid());
        } else {
            this.setStillToDivide(AbilityUtils.calculateAmount(source, toDistribute, sa));
        }
    }

    /**
     * Store divided amount relative to a specific card/player.
     * @param tgt the targeted object
     * @param portionAllocated the divided portion allocated
     */
    public final void addDividedAllocation(final Object tgt, final Integer portionAllocated) {
        if (this.dividedMap.containsKey(tgt)) {
            this.dividedMap.remove(tgt);
        }
        this.dividedMap.put(tgt, portionAllocated);
    }

    /**
     * Get the divided amount relative to a specific card/player.
     * @param tgt the targeted object
     * @return an int.
     */
    public int getDividedValue(Object tgt) {
        return this.dividedMap.get(tgt);
    }
}
