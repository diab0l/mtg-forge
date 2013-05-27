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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.mana.Mana;
import forge.card.mana.ManaPool;
import forge.card.trigger.TriggerType;
import forge.game.player.Player;

/**
 * <p>
 * Abstract AbilityMana class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class AbilityManaPart implements java.io.Serializable {
    /** Constant <code>serialVersionUID=-6816356991224950520L</code>. */
    private static final long serialVersionUID = -6816356991224950520L;

    private final String origProduced;
    private String lastExpressChoice = "";
    private final String manaRestrictions;
    private final String cannotCounterSpell;
    
    private transient ArrayList<Mana> lastManaProduced = new ArrayList<Mana>();

    private final transient Card sourceCard;

    // Spells paid with this mana spell can't be countered.


    /**
     * <p>
     * Constructor for AbilityMana.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param parse
     *            a {@link java.lang.String} object.
     * @param produced
     *            a {@link java.lang.String} object.
     * @param num
     *            a int.
     */
    public AbilityManaPart(final Card sourceCard, final Map<String, String> params) {
        this.sourceCard = sourceCard;

        origProduced = params.containsKey("Produced") ? params.get("Produced") : "1";
        this.manaRestrictions = params.containsKey("RestrictValid") ? params.get("RestrictValid") : "";
        this.cannotCounterSpell = params.get("AddsNoCounter");
    }

    /**
     * <p>
     * produceMana.
     * </p>
     * @param ability
     */
    public final void produceMana(SpellAbility sa) {
        this.produceMana(this.getOrigProduced(), this.getSourceCard().getController(), sa);
    }

    /**
     * <p>
     * produceMana.
     * </p>
     * 
     * @param produced
     *            a {@link java.lang.String} object.
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param sa
     */
    public final void produceMana(final String produced, final Player player, SpellAbility sa) {
        final Card source = this.getSourceCard();
        final ManaPool manaPool = player.getManaPool();

        //clear lastProduced
        this.lastManaProduced.clear();

        // loop over mana produced string
        for (final String c : produced.split(" ")) {
            if(StringUtils.isNumeric(c)) 
                for(int i = Integer.parseInt(c); i > 0; i--) {
                    this.lastManaProduced.add(new Mana(MagicColor.COLORLESS, source, this));
                }
            else
                this.lastManaProduced.add(new Mana(MagicColor.fromName(c), source, this));
        }

        // add the mana produced to the mana pool
        manaPool.add(this.lastManaProduced);

        // Run triggers
        final HashMap<String, Object> runParams = new HashMap<String, Object>();

        runParams.put("Card", source);
        runParams.put("Player", player);
        runParams.put("AbilityMana", sa);
        runParams.put("Produced", produced);
        player.getGame().getTriggerHandler().runTrigger(TriggerType.TapsForMana, runParams, false);

    } // end produceMana(String)

    /**
     * <p>
     * cannotCounterPaidWith.
     * </p>
     * @param saBeingPaid 
     * 
     * @return a {@link java.lang.String} object.
     */
    public boolean cannotCounterPaidWith(SpellAbility saBeingPaid) {
        if (null == cannotCounterSpell) return false;
        if ("True".equalsIgnoreCase(cannotCounterSpell)) return true;

        Card source = saBeingPaid.getSourceCard();
        if (source == null) return false;
        return source.isValid(cannotCounterSpell, sourceCard.getController(), sourceCard);
    }

    /**
     * <p>
     * getManaRestrictions.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getManaRestrictions() {
        return this.manaRestrictions;
    }

    /**
     * <p>
     * meetsManaRestrictions.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public boolean meetsManaRestrictions(final SpellAbility sa) {
        // No restrictions
        if (this.manaRestrictions.isEmpty()) {
            return true;
        }

        // Loop over restrictions
        for (String restriction : this.manaRestrictions.split(",")) {
            if (restriction.startsWith("CostContainsX")) {
                if (sa.isXCost()) {
                    return true;
                }
                continue;
            }

            if (sa.isAbility()) {
                if (restriction.startsWith("Activated")) {
                    restriction = restriction.replace("Activated", "Card");
                }
                else {
                    continue;
                }
            }

            if (sa.getSourceCard() != null) {
                if (sa.getSourceCard().isValid(restriction, this.getSourceCard().getController(), this.getSourceCard())) {
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * <p>
     * mana.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String mana() {
        if (this.getOrigProduced().contains("Chosen")) {
            if (this.getSourceCard() != null && !this.getSourceCard().getChosenColor().isEmpty()) {
                return MagicColor.toShortString(this.getSourceCard()
                .getChosenColor().get(0));
            }
        }
        return this.getOrigProduced();
    }

    /**
     * <p>
     * setAnyChoice.
     * </p>
     *
     * @param s a {@link java.lang.String} object.
     */
    public void setExpressChoice(String s) {
        this.lastExpressChoice = s;
    }

    public void setExpressChoice(ColorSet cs) {
        StringBuilder sb = new StringBuilder();
        if(cs.hasBlack()) sb.append("B ");
        if(cs.hasBlue()) sb.append("U ");
        if(cs.hasWhite()) sb.append("W ");
        if(cs.hasRed()) sb.append("R ");
        if(cs.hasGreen()) sb.append("G ");
        this.lastExpressChoice = sb.toString().trim();
    }    
    
    /**
     * <p>
     * Getter for the field <code>lastAnyChoice</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExpressChoice() {
        return this.lastExpressChoice;
    }

    /**
     * <p>
     * clearExpressChoice.
     * </p>
     *
     */
    public void clearExpressChoice() {
        this.lastExpressChoice = "";
    }

    /**
     * <p>
     * Getter for the field <code>lastProduced</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public ArrayList<Mana> getLastManaProduced() {
        return this.lastManaProduced;
    }

    /**
     * <p>
     * isSnow.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isSnow() {
        return this.getSourceCard().isSnow();
    }

    /**
     * <p>
     * isAnyMana.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isAnyMana() {
        return this.getOrigProduced().contains("Any");
    }

    /**
     * <p>
     * isComboMana.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isComboMana() {
        return this.getOrigProduced().contains("Combo");
    }

    /**
     * <p>
     * canProduce.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean canProduce(final String s) {
        if (isAnyMana()) {
            return true;
        }

        if (this.getOrigProduced().contains("Chosen") && sourceCard != null ) { 
            List<String> chosenCol = this.getSourceCard().getChosenColor();
            if ( !chosenCol.isEmpty() && MagicColor.toShortString(chosenCol.get(0)).contains(s)) {
                return true;
            }
        }
        return this.getOrigProduced().contains(s);
    }

    /**
     * <p>
     * isBasic.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isBasic() {
        if (this.getOrigProduced().length() != 1 && !this.getOrigProduced().contains("Any")
                && !this.getOrigProduced().contains("Chosen")) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object o) {
        // Mana abilities with same Descriptions are "equal"
        if ((o == null) || !(o instanceof AbilityManaPart)) {
            return false;
        }

        final AbilityManaPart abm = (AbilityManaPart) o;

        return sourceCard.equals(abm.sourceCard) && origProduced.equals(abm.getOrigProduced());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (41 * (41 + this.getSourceCard().hashCode()));
    }

    /**
     * @return the origProduced
     */
    public String getOrigProduced() {
        return origProduced;
    }

    /**
     * @return the color available in combination mana
     */
    public String getComboColors() {
        String retVal = "";
        if (this.getOrigProduced().contains("Combo")) {
            retVal = this.getOrigProduced().replace("Combo ", "");
            if (retVal.contains("Any")) {
                retVal = "W U B R G";
            }
        }
        return retVal;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

} // end class AbilityMana

