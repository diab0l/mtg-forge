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
package forge.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import forge.game.card.Card;
import forge.game.card.CardUtil;
import forge.game.player.Player;
import forge.game.replacement.ReplacementEffect;
import forge.game.spellability.AbilityStatic;
import forge.game.spellability.SpellAbility;
import forge.game.staticability.StaticAbility;

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
    private final List<StaticEffect> staticEffects = new ArrayList<StaticEffect>();
    //Global rule changes
    private final Set<GlobalRuleChange> ruleChanges = EnumSet.noneOf(GlobalRuleChange.class);

    public final void clearStaticEffects(final Set<Card> affectedCards) {
        ruleChanges.clear();

        // remove all static effects
        for (final StaticEffect se : staticEffects) {
            affectedCards.addAll(removeStaticEffect(se));
        }
        this.staticEffects.clear();
    }

    public void setGlobalRuleChange(GlobalRuleChange change) {
        this.ruleChanges.add(change);
    }

    public boolean getGlobalRuleChange(GlobalRuleChange change) {
        return this.ruleChanges.contains(change);
    }

    /**
     * Add a static effect to the list of static effects.
     * 
     * @param staticEffect
     *            a {@link StaticEffect}.
     */
    public final void addStaticEffect(final StaticEffect staticEffect) {
        this.staticEffects.add(staticEffect);
    }

    /**
     * Remove a static effect from the list of static effects and undo everything that was changed by the effect.
     * 
     * @param se
     *            a {@link StaticEffect}.
     */
    private static final List<Card> removeStaticEffect(final StaticEffect se) {
        final List<Card> affectedCards = se.getAffectedCards();
        final ArrayList<Player> affectedPlayers = se.getAffectedPlayers();
        final Map<String, String> params = se.getParams();
        final Player controller = se.getSource().getController();

        String changeColorWordsTo = null;

        int powerBonus = 0;
        String addP = "";
        int toughnessBonus = 0;
        String addT = "";
        int keywordMultiplier = 1;
        boolean setPT = false;
        String[] addHiddenKeywords = null;
        String addColors = null;
        boolean removeMayLookAt = false, removeMayPlay = false;

        if (params.containsKey("ChangeColorWordsTo")) {
            changeColorWordsTo = params.get("ChangeColorWordsTo");
        }

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
                addColors = CardUtil.getShortColorsString(se.getSource().getChosenColors());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(" & "))));
            }
        }

        if (params.containsKey("SetColor")) {
            final String colors = params.get("SetColor");
            if (colors.equals("ChosenColor")) {
                addColors = CardUtil.getShortColorsString(se.getSource().getChosenColors());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(" & "))));
            }
        }

        if (params.containsKey("MayLookAt")) {
            removeMayLookAt = true;
        }
        if (params.containsKey("MayPlay")) {
            removeMayPlay = true;
        }

        if (params.containsKey("IgnoreEffectCost")) {
            for (final SpellAbility s : se.getSource().getSpellAbilities()) {
                if (s instanceof AbilityStatic && s.isTemporary()) {
                    se.getSource().removeSpellAbility(s);
                }
            }
        }

        // modify players
        for (final Player p : affectedPlayers) {
            p.setUnlimitedHandSize(false);
            p.setMaxHandSize(p.getStartingHandSize());
            p.removeChangedKeywords(se.getTimestamp());
        }

        // modify the affected card
        for (final Card affectedCard : affectedCards) {
            // Gain control
            if (params.containsKey("GainControl")) {
                affectedCard.removeTempController(se.getTimestamp());
            }

            // Revert changed color words
            if (changeColorWordsTo != null) {
                affectedCard.removeChangedTextColorWord(se.getTimestamp());
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
            affectedCard.addSemiPermanentPowerBoost(powerBonus * -1);
            affectedCard.addSemiPermanentToughnessBoost(toughnessBonus * -1);

            // remove keywords
            // TODO regular keywords currently don't try to use keyword multiplier
            // (Although nothing uses it at this time)
            if (params.containsKey("AddKeyword") || params.containsKey("RemoveKeyword")
                    || params.containsKey("RemoveAllAbilities")) {
                affectedCard.removeChangedCardKeywords(se.getTimestamp());
            }

            // remove abilities
            if (params.containsKey("AddAbility") || params.containsKey("GainsAbilitiesOf")) {
                for (final SpellAbility s : affectedCard.getSpellAbilities().threadSafeIterator()) {
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
                for (final ReplacementEffect rE : affectedCard.getReplacementEffects()) {
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

            // remove may look at
            if (removeMayLookAt) {
                affectedCard.setMayLookAt(controller, false);
            }
            if (removeMayPlay) {
                affectedCard.removeMayPlay(controller);
            }
        }
        se.clearTimestamps();
        return affectedCards;
    }
}
