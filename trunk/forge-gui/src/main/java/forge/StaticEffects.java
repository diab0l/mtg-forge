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
package forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.esotericsoftware.minlog.Log;

import forge.card.TriggerReplacementBase;
import forge.card.spellability.SpellAbility;
import forge.card.staticability.StaticAbility;
import forge.game.Game;
import forge.game.GlobalRuleChange;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * <p>
 * StaticEffects class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class StaticEffects {

    // **************** StaticAbility system **************************
    private final ArrayList<StaticEffect> staticEffects = new ArrayList<StaticEffect>();
    //Global rule changes
    private final EnumSet<GlobalRuleChange> ruleChanges = EnumSet.noneOf(GlobalRuleChange.class);

    private final Game game; 
    
    public final Set<Card> clearStaticEffects() {
        ruleChanges.clear();
        Set<Card> clearedCards = new HashSet<Card>();

        // remove all static effects
        for (StaticEffect se : staticEffects) {
            clearedCards.addAll(this.removeStaticEffect(se));
        }
        this.staticEffects.clear();
        
        return clearedCards;
    }

    public void setGlobalRuleChange(GlobalRuleChange change) {
        this.ruleChanges.add(change);
    }

    public boolean getGlobalRuleChange(GlobalRuleChange change) {
        return this.ruleChanges.contains(change);
    }

    /**
     * addStaticEffect. TODO Write javadoc for this method.
     * 
     * @param staticEffect
     *            a StaticEffect
     */
    public final void addStaticEffect(final StaticEffect staticEffect) {
        this.staticEffects.add(staticEffect);
    }

    /**
     * removeStaticEffect TODO Write javadoc for this method.
     * 
     * @param se
     *            a StaticEffect
     */
    private final List<Card> removeStaticEffect(final StaticEffect se) {
        final List<Card> affectedCards = se.getAffectedCards();
        final ArrayList<Player> affectedPlayers = se.getAffectedPlayers();
        final HashMap<String, String> params = se.getParams();

        int powerBonus = 0;
        String addP = "";
        int toughnessBonus = 0;
        String addT = "";
        int keywordMultiplier = 1;
        boolean setPT = false;
        String[] addKeywords = null;
        String[] addHiddenKeywords = null;
        String addColors = null;

        if (params.containsKey("SetPower") || params.containsKey("SetToughness")) {
            setPT = true;
        }

        if (params.containsKey("AddPower")) {
            addP = params.get("AddPower");
            if (addP.matches("[0-9][0-9]?")) {
                powerBonus = Integer.valueOf(addP);
            } else if (addP.equals("AffectedX")) {
                // gets calculated at runtime
            } else {
                powerBonus = se.getXValue();
            }
        }

        if (params.containsKey("AddToughness")) {
            addT = params.get("AddToughness");
            if (addT.matches("[0-9][0-9]?")) {
                toughnessBonus = Integer.valueOf(addT);
            } else if (addT.equals("AffectedX")) {
                // gets calculated at runtime
            } else {
                toughnessBonus = se.getYValue();
            }
        }

        if (params.containsKey("KeywordMultiplier")) {
            String multiplier = params.get("KeywordMultiplier");
            if (multiplier.equals("X")) {
                keywordMultiplier = se.getXValue();
            } else {
                keywordMultiplier = Integer.valueOf(multiplier);
            }
        }

        if (params.containsKey("AddHiddenKeyword")) {
            addHiddenKeywords = params.get("AddHiddenKeyword").split(" & ");
        }

        if (params.containsKey("AddColor")) {
            final String colors = params.get("AddColor");
            if (colors.equals("ChosenColor")) {
                addColors = CardUtil.getShortColorsString(se.getSource().getChosenColor());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(" & "))));
            }
        }

        if (params.containsKey("SetColor")) {
            final String colors = params.get("SetColor");
            if (colors.equals("ChosenColor")) {
                addColors = CardUtil.getShortColorsString(se.getSource().getChosenColor());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(" & "))));
            }
        }

        // modify players
        for (final Player p : affectedPlayers) {
            p.setUnlimitedHandSize(false);
            p.setMaxHandSize(p.getStartingHandSize());

            if (params.containsKey("AddKeyword")) {
                addKeywords = params.get("AddKeyword").split(" & ");
            }

            // add keywords
            if (addKeywords != null) {
                for (final String keyword : addKeywords) {
                    for (int i = 0; i < keywordMultiplier; i++) {
                        p.removeKeyword(keyword);
                    }
                }
            }
        }

        // modify the affected card
        for (final Card affectedCard : affectedCards) {
            // Gain control
            if (params.containsKey("GainControl")) {
                affectedCard.removeTempController(se.getTimestamp());
            }

            // remove set P/T
            if (!params.containsKey("CharacteristicDefining") && setPT) {
                affectedCard.removeNewPT(se.getTimestamp());
            }

            // remove P/T bonus
            if (addP.startsWith("AffectedX")) {
                powerBonus = se.getXMapValue(affectedCard);
            }
            if (addT.startsWith("AffectedX")) {
                toughnessBonus = se.getXMapValue(affectedCard);
            }
            affectedCard.addSemiPermanentAttackBoost(powerBonus * -1);
            affectedCard.addSemiPermanentDefenseBoost(toughnessBonus * -1);

            // remove keywords
            // TODO regular keywords currently don't try to use keyword multiplier
            // (Although nothing uses it at this time)
            if (params.containsKey("AddKeyword") || params.containsKey("RemoveKeyword")
                    || params.containsKey("RemoveAllAbilities")) {
                affectedCard.removeChangedCardKeywords(se.getTimestamp());
            }

            // remove abilities
            if (params.containsKey("AddAbility") || params.containsKey("GainsAbilitiesOf")) {
                for (final SpellAbility s : affectedCard.getSpellAbilities()) {
                    if (s.isTemporary()) {
                        affectedCard.removeSpellAbility(s);
                    }
                }
            }

            if (addHiddenKeywords != null) {
                for (final String k : addHiddenKeywords) {
                    for (int j = 0; j < keywordMultiplier; j++) {
                        affectedCard.removeHiddenExtrinsicKeyword(k);
                    }
                }
            }

            // remove abilities
            if (params.containsKey("RemoveAllAbilities")) {
                for (final SpellAbility ab : affectedCard.getSpellAbilities()) {
                    ab.setTemporarilySuppressed(false);
                }
                for (final StaticAbility stA : affectedCard.getStaticAbilities()) {
                    stA.setTemporarilySuppressed(false);
                }
                for (final TriggerReplacementBase rE : affectedCard.getReplacementEffects()) {
                    rE.setTemporarilySuppressed(false);
                }
            }

            // remove Types
            if (params.containsKey("AddType") || params.containsKey("RemoveType")) {
                affectedCard.removeChangedCardTypes(se.getTimestamp());
            }

            // remove colors
            if (addColors != null) {
                affectedCard.removeColor(addColors, affectedCard, !se.isOverwriteColors(),
                        se.getTimestamp(affectedCard));
            }
        }
        se.clearTimestamps();
        return affectedCards;
    }

    // **************** End StaticAbility system **************************

    // this is used to keep track of all state-based effects in play:
    private final HashMap<String, Integer> stateBasedMap = new HashMap<String, Integer>();

    // this is used to define all cards that are state-based effects, and map
    // the
    // corresponding commands to their cardnames
    /** Constant <code>cardToEffectsList</code>. */
    private static HashMap<String, String[]> cardToEffectsList = new HashMap<String, String[]>();

    /**
     * <p>
     * Constructor for StaticEffects.
     * </p>
     */
    public StaticEffects(Game game) {
        this.game = game;
        this.initStateBasedEffectsList();
    }

    /**
     * <p>
     * initStateBasedEffectsList.
     * </p>
     */
    public final void initStateBasedEffectsList() {
        // value has to be an array, since certain cards have multiple commands
        // associated with them

        StaticEffects.cardToEffectsList.put("Liu Bei, Lord of Shu", new String[] { "Liu_Bei" });
        StaticEffects.cardToEffectsList.put("Old Man of the Sea", new String[] { "Old_Man_of_the_Sea" });
    }

    /**
     * <p>
     * Getter for the field <code>cardToEffectsList</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     */
    public final HashMap<String, String[]> getCardToEffectsList() {
        return StaticEffects.cardToEffectsList;
    }

    /**
     * <p>
     * addStateBasedEffect.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     */
    public final void addStateBasedEffect(final String s) {
        if (this.stateBasedMap.containsKey(s)) {
            this.stateBasedMap.put(s, this.stateBasedMap.get(s) + 1);
        } else {
            this.stateBasedMap.put(s, 1);
        }
    }

    /**
     * <p>
     * removeStateBasedEffect.
     * </p>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     */
    public final void removeStateBasedEffect(final String s) {
        if (this.stateBasedMap.containsKey(s)) {
            this.stateBasedMap.put(s, this.stateBasedMap.get(s) - 1);
            if (this.stateBasedMap.get(s) == 0) {
                this.stateBasedMap.remove(s);
            }
        }
    }

    /**
     * <p>
     * Getter for the field <code>stateBasedMap</code>.
     * </p>
     * 
     * @return a {@link java.util.HashMap} object.
     */
    public final HashMap<String, Integer> getStateBasedMap() {
        return this.stateBasedMap;
    }

    /**
     * <p>
     * reset.
     * </p>
     */
    public final void reset() {
        this.stateBasedMap.clear();
    }

    /**
     * <p>
     * rePopulateStateBasedList.
     * </p>
     * @param game 
     */
    public final void rePopulateStateBasedList() {
        this.reset();

        final List<Card> cards = game.getCardsIn(ZoneType.Battlefield);

        Log.debug("== Start add state effects ==");
        for (Card c : cards) {
            if (StaticEffects.cardToEffectsList.containsKey(c.getName())) {
                final String[] effects = this.getCardToEffectsList().get(c.getName());
                for (final String effect : effects) {
                    this.addStateBasedEffect(effect);
                    Log.debug("Added " + effect);
                }
            }
        }
        Log.debug("== End add state effects ==");

    }

} // end class StaticEffects
