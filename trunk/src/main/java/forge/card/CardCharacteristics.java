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
package forge.card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import forge.CardColor;
import forge.card.mana.ManaCost;
import forge.card.replacement.ReplacementEffect;
import forge.card.spellability.SpellAbility;
import forge.card.staticability.StaticAbility;
import forge.card.trigger.Trigger;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public class CardCharacteristics {
    private String name = "";
    private List<String> type = new CopyOnWriteArrayList<String>();
    private ManaCost manaCost = ManaCost.NO_COST;
    private List<CardColor> cardColor = new ArrayList<CardColor>();
    private int baseAttack = 0;
    private int baseDefense = 0;
    private List<String> intrinsicKeyword = new ArrayList<String>();
    private final List<SpellAbility> spellAbility = new ArrayList<SpellAbility>();
    private final List<SpellAbility> manaAbility = new ArrayList<SpellAbility>();
    private List<String> unparsedAbilities = new ArrayList<String>();
    private List<Trigger> triggers = new CopyOnWriteArrayList<Trigger>();
    private List<ReplacementEffect> replacementEffects = new ArrayList<ReplacementEffect>();
    private List<StaticAbility> staticAbilities = new ArrayList<StaticAbility>();
    private List<String> staticAbilityStrings = new ArrayList<String>();
    private String imageKey = "";
    private Map<String, String> sVars = new TreeMap<String, String>();

    private CardRarity rarity = CardRarity.Unknown;
    private String curSetCode = CardEdition.UNKNOWN.getCode();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name0
     *            the name to set
     */
    public final void setName(final String name0) {
        this.name = name0;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public final List<String> getType() {
        return this.type;
    }

    /**
     * Sets the type.
     * 
     * @param type0
     *            the type to set
     */
    public final void setType(final ArrayList<String> type0) {
        this.type.clear();
        this.type.addAll(type0);
    }

    /**
     * Gets the mana cost.
     * 
     * @return the manaCost
     */
    public final ManaCost getManaCost() {
        return this.manaCost;
    }

    /**
     * Sets the mana cost.
     * 
     * @param manaCost0
     *            the manaCost to set
     */
    public final void setManaCost(final ManaCost manaCost0) {
        this.manaCost = manaCost0;
    }

    /**
     * Gets the card color.
     * 
     * @return the cardColor
     */
    public final List<CardColor> getCardColor() {
        return this.cardColor;
    }

    /**
     * Sets the card color.
     * 
     * @param cardColor0
     *            the cardColor to set
     */
    public final void setCardColor(final Iterable<CardColor> cardColor0) {
        this.cardColor = Lists.newArrayList(cardColor0);
    }
    
    /**
     * Resets the card color.
     */
    public final void resetCardColor() {
        if (!this.cardColor.isEmpty()) {
            this.cardColor = Lists.newArrayList(this.cardColor.subList(0, 1));
        }
    }

    /**
     * Gets the base attack.
     * 
     * @return the baseAttack
     */
    public final int getBaseAttack() {
        return this.baseAttack;
    }

    /**
     * Sets the base attack.
     * 
     * @param baseAttack0
     *            the baseAttack to set
     */
    public final void setBaseAttack(final int baseAttack0) {
        this.baseAttack = baseAttack0;
    }

    /**
     * Gets the base defense.
     * 
     * @return the baseDefense
     */
    public final int getBaseDefense() {
        return this.baseDefense;
    }

    /**
     * Sets the base defense.
     * 
     * @param baseDefense0
     *            the baseDefense to set
     */
    public final void setBaseDefense(final int baseDefense0) {
        this.baseDefense = baseDefense0;
    }

    /**
     * Gets the intrinsic keyword.
     * 
     * @return the intrinsicKeyword
     */
    public final List<String> getIntrinsicKeyword() {
        return this.intrinsicKeyword;
    }

    /**
     * Sets the intrinsic keyword.
     * 
     * @param intrinsicKeyword0
     *            the intrinsicKeyword to set
     */
    public final void setIntrinsicKeyword(final ArrayList<String> intrinsicKeyword0) {
        this.intrinsicKeyword = intrinsicKeyword0;
    }

    /**
     * Gets the spell ability.
     * 
     * @return the spellAbility
     */
    public final List<SpellAbility> getSpellAbility() {
        return this.spellAbility;
    }


    /**
     * Gets the intrinsic ability.
     * 
     * @return the intrinsicAbility
     */
    public final List<String> getUnparsedAbilities() {
        return this.unparsedAbilities;
    }

    /**
     * Sets the intrinsic ability.
     * 
     * @param list
     *            the intrinsicAbility to set
     */
    public final void setUnparsedAbilities(final List<String> list) {
        this.unparsedAbilities = list;
    }

    /**
     * Gets the mana ability.
     * 
     * @return the manaAbility
     */
    public final List<SpellAbility> getManaAbility() {
        return this.manaAbility;
    }

    /**
     * Gets the triggers.
     * 
     * @return the triggers
     */
    public final List<Trigger> getTriggers() {
        return this.triggers;
    }

    /**
     * Sets the triggers.
     * 
     * @param triggers0
     *            the triggers to set
     */
    public final void setTriggers(final List<Trigger> triggers0) {
        this.triggers = triggers0;
    }

    /**
     * Gets the static abilities.
     * 
     * @return the staticAbilities
     */
    public final List<StaticAbility> getStaticAbilities() {
        return this.staticAbilities;
    }

    /**
     * Sets the static abilities.
     * 
     * @param staticAbilities0
     *            the staticAbilities to set
     */
    public final void setStaticAbilities(final ArrayList<StaticAbility> staticAbilities0) {
        this.staticAbilities = new ArrayList<StaticAbility>(staticAbilities0);
    }

    /**
     * Gets the image filename.
     * 
     * @return the imageFilename
     */
    public final String getImageKey() {
        return this.imageKey;
    }

    /**
     * Sets the image filename.
     * 
     * @param imageFilename0
     *            the imageFilename to set
     */
    public final void setImageKey(final String imageFilename0) {
        this.imageKey = imageFilename0;
    }

    /**
     * Gets the static ability strings.
     * 
     * @return the staticAbilityStrings
     */
    public final List<String> getStaticAbilityStrings() {
        return this.staticAbilityStrings;
    }

    /**
     * Sets the static ability strings.
     * 
     * @param staticAbilityStrings0
     *            the staticAbilityStrings to set
     */
    public final void setStaticAbilityStrings(final ArrayList<String> staticAbilityStrings0) {
        this.staticAbilityStrings = staticAbilityStrings0;
    }

    /**
     * @return the replacementEffects
     */
    public List<ReplacementEffect> getReplacementEffects() {
        return replacementEffects;
    }

    /**
     * <p>
     * getSVar.
     * </p>
     * 
     * @param var
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public final String getSVar(final String var) {
        if (this.sVars.containsKey(var)) {
            return this.sVars.get(var);
        } else {
            return "";
        }
    }

    /**
     * <p>
     * hasSVar.
     * </p>
     * 
     * @param var
     *            a {@link java.lang.String} object.
     */
    public final boolean hasSVar(final String var) {
        return this.sVars.containsKey(var);
    }

    /**
     * <p>
     * setSVar.
     * </p>
     * 
     * @param var
     *            a {@link java.lang.String} object.
     * @param str
     *            a {@link java.lang.String} object.
     */
    public final void setSVar(final String var, final String str) {
        this.sVars.put(var, str);
    }

    /**
     * <p>
     * getSVars.
     * </p>
     * 
     * @return a Map object.
     */
    public final Map<String, String> getSVars() {
        return this.sVars;
    }

    /**
     * <p>
     * setSVars.
     * </p>
     * 
     * @param newSVars
     *            a Map object.
     */
    public final void setSVars(final Map<String, String> newSVars) {
        this.sVars = newSVars;
    }

    /**
     * <p>
     * copy.
     * </p>
     * 
     * @param source
     *            a Map object.
     */
    public final void copyFrom(final CardCharacteristics source) {
        // Makes a "deeper" copy of a CardCharacteristics object

        // String name : just copy reference
        this.name = source.getName();
        // ArrayList<String> type : list of String objects so use copy constructor
        this.type = new CopyOnWriteArrayList<String>(source.getType());
        // CardManaCost manaCost : not sure if a deep copy is needed
        this.manaCost = source.getManaCost();
        // ArrayList<CardColor> cardColor : not sure if a deep copy is needed
        this.cardColor = new ArrayList<CardColor>(source.getCardColor());
        // int baseAttack : set value
        this.baseAttack = source.getBaseAttack();
        // int baseDefense : set value
        this.baseDefense = source.getBaseDefense();
        // ArrayList<String> intrinsicKeyword : list of String objects so use copy constructor
        this.intrinsicKeyword =  new ArrayList<String>(source.getIntrinsicKeyword());
        // ArrayList<String> intrinsicAbility : list of String objects so use copy constructor
        this.unparsedAbilities = new ArrayList<String>(source.getUnparsedAbilities());
        // ArrayList<String> staticAbilityStrings : list of String objects so use copy constructor
        this.staticAbilityStrings = new ArrayList<String>(source.getStaticAbilityStrings());
        // String imageFilename = copy reference
        this.imageKey = source.getImageKey();
        this.rarity = source.rarity;
        this.curSetCode = source.curSetCode;
        // Map<String, String> sVars
        this.sVars = new TreeMap<String, String>(source.getSVars());
        this.replacementEffects = new ArrayList<ReplacementEffect>();
        for (ReplacementEffect RE : source.getReplacementEffects()) {
            this.replacementEffects.add(RE.getCopy());
        }
    }


    public CardRarity getRarity() {
        return rarity;
    }


    public void setRarity(CardRarity rarity) {
        this.rarity = rarity;
    }


    public String getCurSetCode() {
        return curSetCode;
    }


    public void setCurSetCode(String curSetCode) {
        this.curSetCode = curSetCode; 
    }

}
