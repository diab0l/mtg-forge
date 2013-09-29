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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.Constant;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.mana.Mana;
import forge.card.mana.ManaPool;
import forge.card.replacement.ReplacementResult;
import forge.card.trigger.TriggerType;
import forge.game.GameType;
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
    private final String addsKeywords;
    private final boolean persistentMana;
    private String manaReplaceType;
    
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
        this.addsKeywords = params.get("AddsKeywords");
        this.persistentMana = (null == params.get("PersistentMana")) ? false :
            "True".equalsIgnoreCase(params.get("PersistentMana"));
        this.manaReplaceType = params.containsKey("ManaReplaceType") ? params.get("ManaReplaceType") : "";
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
        String afterReplace = applyManaReplacement(sa, produced);
        final HashMap<String, Object> repParams = new HashMap<String, Object>();
        repParams.put("Event", "ProduceMana");
        repParams.put("Mana", afterReplace);
        repParams.put("Affected", source);
        repParams.put("Player", player);
        repParams.put("AbilityMana", sa);
        if (player.getGame().getReplacementHandler().run(repParams) != ReplacementResult.NotReplaced) {
            return;
        }
        ColorSet CID = null;

        if (player.getGame().getType() == GameType.Commander) {
            CID = player.getCommander().getRules().getColorIdentity();
        }
        //clear lastProduced
        this.lastManaProduced.clear();

        // loop over mana produced string
        for (final String c : afterReplace.split(" ")) {
            if (StringUtils.isNumeric(c)) {
                for (int i = Integer.parseInt(c); i > 0; i--) {
                    this.lastManaProduced.add(new Mana(MagicColor.COLORLESS, source, this));
                }
            } else {
                byte attemptedMana = MagicColor.fromName(c);
                if (CID != null) {
                    if (!CID.hasAnyColor(attemptedMana)) {
                        attemptedMana = MagicColor.COLORLESS;
                    }
                }

                this.lastManaProduced.add(new Mana(attemptedMana, source, this));
            }
        }

        // add the mana produced to the mana pool
        manaPool.add(this.lastManaProduced);

        // Run triggers
        final HashMap<String, Object> runParams = new HashMap<String, Object>();

        runParams.put("Card", source);
        runParams.put("Player", player);
        runParams.put("AbilityMana", sa);
        runParams.put("Produced", afterReplace);
        player.getGame().getTriggerHandler().runTrigger(TriggerType.TapsForMana, runParams, false);
        // Clear Mana replacement
        this.manaReplaceType = "";
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
     * addKeywords.
     * </p>
     * @param saBeingPaid 
     * 
     * @return a {@link java.lang.String} object.
     */
    public boolean addKeywords(SpellAbility saBeingPaid) {
        return this.addsKeywords != null;
    }

    /**
     * <p>
     * getKeywords.
     * </p>
     * @param saBeingPaid 
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getKeywords() {
        return this.addsKeywords;
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
            if (restriction.equals("nonSpell")) {
                return !sa.isSpell();
            }

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
            if(retVal.contains("ColorIdentity")) {
                retVal = "";
                if(this.getSourceCard().getOwner().getCommander() == null)
                {
                    return "";
                }
                ColorSet CID = this.getSourceCard().getOwner().getCommander().getRules().getColorIdentity();
                if(CID.hasWhite())
                {
                    retVal += "W ";
                }
                if(CID.hasBlue())
                {
                    retVal += "U ";
                }
                if(CID.hasBlack())
                {
                    retVal += "B ";
                }
                if(CID.hasRed())
                {
                    retVal += "R ";
                }
                if(CID.hasGreen())
                {
                    retVal += "G ";
                }
                retVal = retVal.substring(0,retVal.length()-1);
            }
        }
        return retVal;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    /**
     * <p>
     * isPersistentMana.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isPersistentMana() {
        return this.persistentMana;
    }

    /**
     * @return the manaReplaceType
     */
    public String getManaReplaceType() {
        return manaReplaceType;
    }

    /**
     * setManaReplaceType.
     */
    public void setManaReplaceType(final String type) {
        this.manaReplaceType = type;
    }
    /**
     * <p>
     * applyManaReplacement.
     * </p>
     * @return a String
     */
    public static String applyManaReplacement(final SpellAbility sa, final String original) {
        final HashMap<String, String> repMap = new HashMap<String, String>();
        final Player act = sa.getActivatingPlayer();
        final String manaReplace = sa.getManaPart().getManaReplaceType();
        if (manaReplace.isEmpty()) {
            return original;
        }
        if (manaReplace.startsWith("Any")) {
            // Replace any type and amount
            return manaReplace.split("->")[1];
        }
        final Pattern splitter = Pattern.compile("->");
        // Replace any type
        for (String part : manaReplace.split(" & ")) {
            final String[] v = splitter.split(part, 2);
            if (v[0].equals("Colorless")) {
                repMap.put("[0-9][0-9]?", v.length > 1 ? v[1].trim() : "");
            } else {
                repMap.put(v[0], v.length > 1 ? v[1].trim() : "");
            }
        }
        // Handle different replacement simultaneously
        Pattern pattern = Pattern.compile(StringUtils.join(repMap.keySet().iterator(), "|"));
        Matcher m = pattern.matcher(original);
        StringBuffer sb = new StringBuffer();
        while(m.find()) {
            if (m.group().matches("[0-9][0-9]?")) {
                final String rep = StringUtils.repeat(repMap.get("[0-9][0-9]?") + " ", 
                        Integer.parseInt(m.group())).trim();
                m.appendReplacement(sb, rep);
            } else {
                m.appendReplacement(sb, repMap.get(m.group()));
            }
        }
        m.appendTail(sb);
        String replaced = sb.toString();
        while (replaced.contains("Any")) {
            String rs = act.getController().chooseSingleColor(Constant.Color.ONLY_COLORS);
            replaced = replaced.replaceFirst("Any", MagicColor.toShortString(rs));
        }
        return replaced;
    }

} // end class AbilityMana

