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
package forge.game.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.game.Game;
import forge.game.TriggerReplacementBase;
import forge.game.card.Card;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.Ability;
import forge.game.spellability.OptionalCost;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

/**
 * <p>
 * Abstract Trigger class. Constructed by reflection only
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Trigger extends TriggerReplacementBase {

    /** Constant <code>nextID=0</code>. */
    private static int nextID = 0;

    /**
     * <p>
     * resetIDs.
     * </p>
     */
    public static void resetIDs() {
        Trigger.nextID = 50000;
    }

    /** The ID. */
    private int id = Trigger.nextID++;

    /**
     * <p>
     * setID.
     * </p>
     * 
     * @param id
     *            a int.
     */
    public final void setID(final int id) {
        this.id = id;
    }

    /** The map params. */
    protected final HashMap<String, String> mapParams = new HashMap<String, String>();

    /**
     * <p>
     * Getter for the field <code>mapParams</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     */
    public final HashMap<String, String> getMapParams() {
        return this.mapParams;
    }

    /** The run params. */
    private Map<String, Object> runParams;

    private TriggerType mode;

    private HashMap<String, Object> storedTriggeredObjects = null;

    /**
     * <p>
     * Setter for the field <code>storedTriggeredObjects</code>.
     * </p>
     * 
     * @param storedTriggeredObjects
     *            a {@link java.util.HashMap} object.
     * @since 1.0.15
     */
    public final void setStoredTriggeredObjects(final HashMap<String, Object> storedTriggeredObjects) {
        this.storedTriggeredObjects = storedTriggeredObjects;
    }

    /**
     * <p>
     * Getter for the field <code>storedTriggeredObjects</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     * @since 1.0.15
     */
    public final HashMap<String, Object> getStoredTriggeredObjects() {
        return this.storedTriggeredObjects;
    }

    /** The is intrinsic. */
    private final boolean intrinsic;

    private List<PhaseType> validPhases;

    /**
     * <p>
     * Constructor for Trigger.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @param host
     *            a {@link forge.game.card.Card} object.
     * @param intrinsic
     *            the intrinsic
     */
    public Trigger(final Map<String, String> params, final Card host, final boolean intrinsic) {
        this.setRunParams(new HashMap<String, Object>());
        this.mapParams.putAll(params);
        this.setHostCard(host);

        this.intrinsic = intrinsic;
    }

    /**
     * <p>
     * toString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String toString() {
        if (this.mapParams.containsKey("TriggerDescription") && !this.isSuppressed()) {
            return this.mapParams.get("TriggerDescription").replace("CARDNAME", this.getHostCard().getName());
        } else {
            return "";
        }
    }

    /**
     * <p>
     * phasesCheck.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean phasesCheck(final Game game) {
        PhaseHandler phaseHandler = game.getPhaseHandler();
        if (null != validPhases) {
            if (!validPhases.contains(phaseHandler.getPhase())) {
                return false;
            }
        }

        if (this.mapParams.containsKey("PlayerTurn")) {
            if (!phaseHandler.isPlayerTurn(this.getHostCard().getController())) {
                return false;
            }
        }

        if (this.mapParams.containsKey("OpponentTurn")) {
            if (!phaseHandler.getPlayerTurn().isOpponentOf(this.getHostCard().getController())) {
                return false;
            }
        }

        if (this.mapParams.containsKey("FirstUpkeep")) {
            if (!phaseHandler.isFirstUpkeep()) {
                return false;
            }
        }

        if (this.mapParams.containsKey("FirstCombat")) {
            if (!phaseHandler.isFirstCombat()) {
                return false;
            }
        }

        return true;
    }
    /**
     * <p>
     * requirementsCheck.
     * </p>
     * @param game 
     * 
     * @return a boolean.
     */
    public final boolean requirementsCheck(Game game) {
        return this.requirementsCheck(game, this.getRunParams());
    }

    /**
     * <p>
     * requirementsCheck.
     * </p>
     * @param game 
     * 
     * @param runParams2
     *            a {@link java.util.HashMap} object.
     * @return a boolean.
     */
    public final boolean requirementsCheck(Game game, final Map<String, Object> runParams) {

        if (this.mapParams.containsKey("APlayerHasMoreLifeThanEachOther")) {
            int highestLife = -50; // Negative base just in case a few Lich's or Platinum Angels are running around
            final List<Player> healthiest = new ArrayList<Player>();
            for (final Player p : game.getPlayers()) {
                if (p.getLife() > highestLife) {
                    healthiest.clear();
                    highestLife = p.getLife();
                    healthiest.add(p);
                } else if (p.getLife() == highestLife) {
                    highestLife = p.getLife();
                    healthiest.add(p);
                }
            }

            if (healthiest.size() != 1) {
                // More than one player tied for most life
                return false;
            }
        }

        if (this.mapParams.containsKey("APlayerHasMostCardsInHand")) {
            int largestHand = 0;
            final List<Player> withLargestHand = new ArrayList<Player>();
            for (final Player p : game.getPlayers()) {
                if (p.getCardsIn(ZoneType.Hand).size() > largestHand) {
                    withLargestHand.clear();
                    largestHand = p.getCardsIn(ZoneType.Hand).size();
                    withLargestHand.add(p);
                } else if (p.getCardsIn(ZoneType.Hand).size() == largestHand) {
                    largestHand = p.getCardsIn(ZoneType.Hand).size();
                    withLargestHand.add(p);
                }
            }

            if (withLargestHand.size() != 1) {
                // More than one player tied for most life
                return false;
            }
        }
        
        if ( !meetsCommonRequirements(this.mapParams))
            return false;

//        if ( !meetsRequirementsOnTriggeredObjects(runParams) )
//            return false;
        
        if ("True".equals(this.mapParams.get("EvolveCondition"))) {
            final Card moved = (Card) runParams.get("Card");
            if (moved == null) {
                return false;
                // final StringBuilder sb = new StringBuilder();
                // sb.append("Trigger::requirementsCheck() - EvolveCondition condition being checked without a moved card. ");
                // sb.append(this.getHostCard().getName());
                // throw new RuntimeException(sb.toString());
            }
            if (moved.getNetAttack() <= this.getHostCard().getNetAttack()
                    && moved.getNetDefense() <= this.getHostCard().getNetDefense()) {
                return false;
            }
        }
        
        String condition = this.mapParams.get("Condition");
        if( "AltCost".equals(condition) ) {
            final Card moved = (Card) runParams.get("Card");
            if( null != moved && !moved.isOptionalCostPaid(OptionalCost.AltCost))
                return false;
        }


        return true;
    }


//    private boolean meetsRequirementsOnTriggeredObjects(Map<String, Object> runParams) {
//
//        return true;
//    }

    /**
     * <p>
     * isSecondary.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isSecondary() {
        if (this.mapParams.containsKey("Secondary")) {
            if (this.mapParams.get("Secondary").equals("True")) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof Trigger)) {
            return false;
        }

        return this.getId() == ((Trigger) o).getId();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 41 * (41 + this.getId());
    }

    /**
     * <p>
     * performTest.
     * </p>
     * 
     * @param runParams2
     *            a {@link java.util.HashMap} object.
     * @return a boolean.
     */
    public abstract boolean performTest(java.util.Map<String, Object> runParams2);

    /**
     * <p>
     * setTriggeringObjects.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    public abstract void setTriggeringObjects(SpellAbility sa);

    /** The temporary. */
    private boolean temporary = false;

    /**
     * Sets the temporary.
     * 
     * @param temp
     *            the new temporary
     */
    public final void setTemporary(final boolean temp) {
        this.temporary = temp;
    }

    /**
     * Checks if is temporary.
     * 
     * @return true, if is temporary
     */
    public final boolean isTemporary() {
        return this.temporary;
    }

    /**
     * Checks if is intrinsic.
     * 
     * @return the isIntrinsic
     */
    public boolean isIntrinsic() {
        return this.intrinsic;
    }


    /**
     * Gets the run params.
     * 
     * @return the runParams
     */
    public Map<String, Object> getRunParams() {
        return this.runParams;
    }

    /**
     * Sets the run params.
     * 
     * @param runParams0
     *            the runParams to set
     */
    public void setRunParams(final Map<String, Object> runParams0) {
        this.runParams = runParams0;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    private Ability triggeredSA;

    /**
     * Gets the triggered sa.
     * 
     * @return the triggered sa
     */
    public final Ability getTriggeredSA() {
        System.out.println("TriggeredSA = " + this.triggeredSA);
        return this.triggeredSA;
    }

    /**
     * Sets the triggered sa.
     * 
     * @param sa
     *            the triggered sa to set
     */
    public void setTriggeredSA(final Ability sa) {
        this.triggeredSA = sa;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return the mode
     */
    public TriggerType getMode() {
        return mode;
    }

    /**
     * 
     * @param triggerType
     *            the triggerType to set
     * @param triggerType
     */
    void setMode(TriggerType triggerType) {
        mode = triggerType;
    }
    

    public final Trigger getCopyForHostCard(Card newHost) {
        TriggerType tt = TriggerType.getTypeFor(this);
        Trigger copy = tt.createTrigger(mapParams, newHost, intrinsic); 

        if (this.getOverridingAbility() != null) {
            copy.setOverridingAbility(this.getOverridingAbility());
        }
        
        copy.setID(this.getId());
        copy.setMode(this.getMode());
        copy.setTriggerPhases(this.validPhases);
        copy.setActiveZone(validHostZones);
        copy.setTemporary(isTemporary());
        return copy;
    }

    public boolean isStatic() {
        return this.mapParams.containsKey("Static"); // && params.get("Static").equals("True") [always true if present]
    }

    public void setTriggerPhases(List<PhaseType> phases) {
        validPhases = phases;
    }
}
