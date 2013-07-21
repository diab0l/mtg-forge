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
package forge.card.staticability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.CardUtil;
import forge.StaticEffect;
import forge.StaticEffects;
import forge.card.CardType;
import forge.card.TriggerReplacementBase;
import forge.card.ability.AbilityFactory;
import forge.card.ability.AbilityUtils;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.replacement.ReplacementEffect;
import forge.card.replacement.ReplacementHandler;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.SpellAbility;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.game.Game;
import forge.game.GlobalRuleChange;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * The Class StaticAbility_Continuous.
 */
public class StaticAbilityContinuous {

    /**
     * 
     * TODO Write javadoc for this method.
     * 
     * @param stAb
     *            a StaticAbility
     * @return 
     * 
     */
    public static List<Card> applyContinuousAbility(final StaticAbility stAb) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();

        final StaticEffect se = new StaticEffect(hostCard);
        final List<Card> affectedCards = StaticAbilityContinuous.getAffectedCards(stAb);
        final ArrayList<Player> affectedPlayers = StaticAbilityContinuous.getAffectedPlayers(stAb);
        final Game game = hostCard.getGame();

        se.setAffectedCards(affectedCards);
        se.setAffectedPlayers(affectedPlayers);
        se.setParams(params);
        se.setTimestamp(hostCard.getTimestamp());
        game.getStaticEffects().addStaticEffect(se);

        String addP = "";
        int powerBonus = 0;
        String addT = "";
        int toughnessBonus = 0;
        String setP = "";
        int setPower = -1;
        String setT = "";
        int setToughness = -1;
        int keywordMultiplier = 1;

        String[] addKeywords = null;
        String[] addHiddenKeywords = null;
        String[] removeKeywords = null;
        String[] addAbilities = null;
        String[] addReplacements = null;
        String[] addSVars = null;
        String[] addTypes = null;
        String[] removeTypes = null;
        String addColors = null;
        String[] addTriggers = null;
        ArrayList<SpellAbility> addFullAbs = null;
        boolean removeAllAbilities = false;
        boolean removeSuperTypes = false;
        boolean removeCardTypes = false;
        boolean removeSubTypes = false;
        boolean removeCreatureTypes = false;

        //Global rules changes
        if (params.containsKey("GlobalRule")) {
            final StaticEffects effects = game.getStaticEffects();
            effects.setGlobalRuleChange(GlobalRuleChange.fromString(params.get("GlobalRule")));
        }

        if (params.containsKey("SetPower")) {
            setP = params.get("SetPower");
            setPower = AbilityUtils.calculateAmount(hostCard, setP, null);
        }

        if (params.containsKey("SetToughness")) {
            setT = params.get("SetToughness");
            setToughness = AbilityUtils.calculateAmount(hostCard, setT, null);
        }

        if (params.containsKey("AddPower")) {
            addP = params.get("AddPower");
            powerBonus = AbilityUtils.calculateAmount(hostCard, addP, null);
            if (!StringUtils.isNumeric(addP) && !addP.equals("AffectedX")) {
                se.setXValue(powerBonus);
            }
        }

        if (params.containsKey("AddToughness")) {
            addT = params.get("AddToughness");
            toughnessBonus = AbilityUtils.calculateAmount(hostCard, addT, null);
            if (!StringUtils.isNumeric(addT) && !addT.equals("AffectedX")) {
                se.setYValue(toughnessBonus);
            }
        }

        if (params.containsKey("KeywordMultiplier")) {
            String multiplier = params.get("KeywordMultiplier");
            if (multiplier.equals("X")) {
                keywordMultiplier = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X"));
                se.setXValue(keywordMultiplier);
            } else {
                keywordMultiplier = Integer.valueOf(multiplier);
            }
        }

        if (params.containsKey("AddKeyword")) {
            addKeywords = params.get("AddKeyword").split(" & ");
            final List<String> chosencolors = hostCard.getChosenColor();
            for (final String color : chosencolors) {
                for (int w = 0; w < addKeywords.length; w++) {
                    addKeywords[w] = addKeywords[w].replaceAll("ChosenColor", color.substring(0, 1).toUpperCase().concat(color.substring(1, color.length())));
                }
            }
            final String chosenType = hostCard.getChosenType();
            for (int w = 0; w < addKeywords.length; w++) {
                addKeywords[w] = addKeywords[w].replaceAll("ChosenType", chosenType);
            }
            final String chosenName = hostCard.getNamedCard();
            for (int w = 0; w < addKeywords.length; w++) {
                if (addKeywords[w].startsWith("Protection:")) {
                    addKeywords[w] = addKeywords[w].replaceAll("ChosenName", "Card.named" + chosenName);
                }
            }
            if (params.containsKey("SharedKeywordsZone")) {
                List<ZoneType> zones = ZoneType.listValueOf(params.get("SharedKeywordsZone"));
                String[] restrictions = params.containsKey("SharedRestrictions") ? params.get("SharedRestrictions").split(",") : new String[] {"Card"};
                List<String> kw = CardFactoryUtil.sharedKeywords(addKeywords, restrictions, zones, hostCard);
                addKeywords = kw.toArray(new String[kw.size()]);
            }
        }

        if (params.containsKey("AddHiddenKeyword")) {
            addHiddenKeywords = params.get("AddHiddenKeyword").split(" & ");
        }

        if (params.containsKey("RemoveKeyword")) {
            removeKeywords = params.get("RemoveKeyword").split(" & ");
        }

        if (params.containsKey("RemoveAllAbilities")) {
            removeAllAbilities = true;
        }

        if (params.containsKey("AddAbility")) {
            final String[] sVars = params.get("AddAbility").split(" & ");
            for (int i = 0; i < sVars.length; i++) {
                sVars[i] = hostCard.getSVar(sVars[i]);
            }
            addAbilities = sVars;
        }

        if (params.containsKey("AddReplacementEffects")) {
            final String[] sVars = params.get("AddReplacementEffects").split(" & ");
            for (int i = 0; i < sVars.length; i++) {
                sVars[i] = hostCard.getSVar(sVars[i]);
            }
            addReplacements = sVars;
        }

        if (params.containsKey("AddSVar")) {
            addSVars = params.get("AddSVar").split(" & ");
        }

        if (params.containsKey("AddType")) {
            addTypes = params.get("AddType").split(" & ");
            if (addTypes[0].equals("ChosenType")) {
                final String chosenType = hostCard.getChosenType();
                addTypes[0] = chosenType;
                se.setChosenType(chosenType);
            } else if (addTypes[0].equals("ImprintedCreatureType")) {
                final ArrayList<String> imprint = hostCard.getImprinted().get(0).getType();
                ArrayList<String> imprinted = new ArrayList<String>();
                for (String t : imprint) {
                    if (CardType.isACreatureType(t) || t.equals("AllCreatureTypes")) {
                        imprinted.add(t);
                    }
                }
                addTypes = imprinted.toArray(new String[imprinted.size()]);
            }
        }

        if (params.containsKey("RemoveType")) {
            removeTypes = params.get("RemoveType").split(" & ");
            if (removeTypes[0].equals("ChosenType")) {
                final String chosenType = hostCard.getChosenType();
                removeTypes[0] = chosenType;
                se.setChosenType(chosenType);
            }
        }

        if (params.containsKey("RemoveSuperTypes")) {
            removeSuperTypes = true;
        }

        if (params.containsKey("RemoveCardTypes")) {
            removeCardTypes = true;
        }

        if (params.containsKey("RemoveSubTypes")) {
            removeSubTypes = true;
        }

        if (params.containsKey("RemoveCreatureTypes")) {
            removeCreatureTypes = true;
        }

        if (params.containsKey("AddColor")) {
            final String colors = params.get("AddColor");
            if (colors.equals("ChosenColor")) {
                addColors = CardUtil.getShortColorsString(hostCard.getChosenColor());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(
                    " & "))));
            }
        }

        if (params.containsKey("SetColor")) {
            final String colors = params.get("SetColor");
            if (colors.equals("ChosenColor")) {
                addColors = CardUtil.getShortColorsString(hostCard.getChosenColor());
            } else {
                addColors = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(
                        colors.split(" & "))));
            }
            se.setOverwriteColors(true);
        }

        if (params.containsKey("AddTrigger")) {
            final String[] sVars = params.get("AddTrigger").split(" & ");
            for (int i = 0; i < sVars.length; i++) {
                sVars[i] = hostCard.getSVar(sVars[i]);
            }
            addTriggers = sVars;
        }

        if (params.containsKey("GainsAbilitiesOf")) {
            final String[] valids = params.get("GainsAbilitiesOf").split(",");
            ArrayList<ZoneType> validZones = new ArrayList<ZoneType>();
            validZones.add(ZoneType.Battlefield);
            if (params.containsKey("GainsAbilitiesOfZones")) {
                validZones.clear();
                for (String s : params.get("GainsAbilitiesOfZones").split(",")) {
                    validZones.add(ZoneType.smartValueOf(s));
                }
            }

            List<Card> cardsIGainedAbilitiesFrom = game.getCardsIn(validZones);
            cardsIGainedAbilitiesFrom = CardLists.getValidCards(cardsIGainedAbilitiesFrom, valids, hostCard.getController(), hostCard);

            if (cardsIGainedAbilitiesFrom.size() > 0) {

                addFullAbs = new ArrayList<SpellAbility>();

                for (Card c : cardsIGainedAbilitiesFrom) {
                    for (SpellAbility sa : c.getSpellAbilities()) {
                        if (sa instanceof AbilityActivated) {
                            SpellAbility newSA = ((AbilityActivated) sa).getCopy();
                            newSA.setTemporary(true);
                            CardFactoryUtil.correctAbilityChainSourceCard(newSA, hostCard);
                            addFullAbs.add(newSA);
                        }
                    }
                }
            }
        }

        // modify players
        for (final Player p : affectedPlayers) {

            // add keywords
            if (addKeywords != null) {
                for (final String keyword : addKeywords) {
                    for (int i = 0; i < keywordMultiplier; i++) {
                        p.addKeyword(keyword);
                    }
                }
            }

            if (params.containsKey("SetMaxHandSize")) {
                String mhs = params.get("SetMaxHandSize");
                if (mhs.equals("Unlimited")) {
                    p.setUnlimitedHandSize(true);
                } else {
                    int max = AbilityUtils.calculateAmount(hostCard, mhs, null);
                    p.setMaxHandSize(max);
                }
            }

            if (params.containsKey("RaiseMaxHandSize")) {
                String rmhs = params.get("RaiseMaxHandSize");
                int rmax = AbilityUtils.calculateAmount(hostCard, rmhs, null);
                p.setMaxHandSize(p.getMaxHandSize() + rmax);
            }
        }

        // start modifying the cards
        for (int i = 0; i < affectedCards.size(); i++) {
            final Card affectedCard = affectedCards.get(i);
            
            // Gain control
            if (params.containsKey("GainControl")) {
                affectedCard.addTempController(hostCard.getController(), hostCard.getTimestamp());
            }

            // set P/T
            if (params.containsKey("CharacteristicDefining")) {
                if (setPower != -1) {
                    affectedCard.setBaseAttack(setPower);
                }
                if (setToughness != -1) {
                    affectedCard.setBaseDefense(setToughness);
                }
            } else // non CharacteristicDefining
            if ((setPower != -1) || (setToughness != -1)) {
                if (setP.startsWith("AffectedX")) {
                    setPower = CardFactoryUtil.xCount(affectedCard, hostCard.getSVar(setP));
                }
                if (setT.startsWith("AffectedX")) {
                    setToughness = CardFactoryUtil.xCount(affectedCard, hostCard.getSVar(setT));
                }
                affectedCard.addNewPT(setPower, setToughness, hostCard.getTimestamp());
            }

            // add P/T bonus
            if (addP.startsWith("AffectedX")) {
                powerBonus = CardFactoryUtil.xCount(affectedCard, hostCard.getSVar(addP));
                se.addXMapValue(affectedCard, powerBonus);
            }
            if (addT.startsWith("AffectedX")) {
                toughnessBonus = CardFactoryUtil.xCount(affectedCard, hostCard.getSVar(addT));
                se.addXMapValue(affectedCard, toughnessBonus);
            }
            affectedCard.addSemiPermanentAttackBoost(powerBonus);
            affectedCard.addSemiPermanentDefenseBoost(toughnessBonus);

            // add keywords
            // TODO regular keywords currently don't try to use keyword multiplier
            // (Although nothing uses it at this time)
            if ((addKeywords != null) || (removeKeywords != null) || removeAllAbilities) {
                affectedCard.addChangedCardKeywords(addKeywords, removeKeywords, removeAllAbilities,
                        hostCard.getTimestamp());
            }

            // add HIDDEN keywords
            if (addHiddenKeywords != null) {
                for (final String k : addHiddenKeywords) {
                    for (int j = 0; j < keywordMultiplier; j++) {
                        affectedCard.addHiddenExtrinsicKeyword(k);
                    }
                }
            }

            // add SVars
            if (addSVars != null) {
                for (final String sVar : addSVars) {
                    String actualSVar = hostCard.getSVar(sVar);
                    String name = sVar;
                    if (actualSVar.startsWith("SVar:")) {
                        actualSVar = actualSVar.split("SVar:")[1];
                        name = actualSVar.split(":")[0];
                        actualSVar = actualSVar.split(":")[1];
                    }
                    affectedCard.setSVar(name, actualSVar);
                }
            }

            if (addFullAbs != null) {
                for (final SpellAbility ab : addFullAbs) {
                    affectedCard.addSpellAbility(ab);
                }
            }

            // add abilities
            if (addAbilities != null) {
                for (String abilty : addAbilities) {
                    if (abilty.contains("CardManaCost")) {
                        abilty = abilty.replace("CardManaCost", affectedCard.getManaCost().toString());
                    } else if (abilty.contains("ConvertedManaCost")) {
                        final String costcmc = Integer.toString(affectedCard.getCMC());
                        abilty = abilty.replace("ConvertedManaCost", costcmc);
                    }
                    if (abilty.startsWith("AB")) { // grant the ability
                        final SpellAbility sa = AbilityFactory.getAbility(abilty, affectedCard);
                        sa.setTemporary(true);
                        sa.setOriginalHost(hostCard);
                        affectedCard.addSpellAbility(sa);
                    }
                }
            }

            // add Replacement effects
            if (addReplacements != null) {
                for (String rep : addReplacements) {
                    final ReplacementEffect actualRep = ReplacementHandler.parseReplacement(rep, affectedCard, false);
                    affectedCard.addReplacementEffect(actualRep).setTemporary(true);;
                }
            }
            
            // add Types
            if ((addTypes != null) || (removeTypes != null)) {
                affectedCard.addChangedCardTypes(addTypes, removeTypes, removeSuperTypes, removeCardTypes,
                        removeSubTypes, removeCreatureTypes, hostCard.getTimestamp());
            }

            // add colors
            if (addColors != null) {
                final long t = affectedCard.addColor(addColors, !se.isOverwriteColors(), true);
                se.addTimestamp(affectedCard, t);
            }

            // add triggers
            if (addTriggers != null) {
                for (final String trigger : addTriggers) {
                    final Trigger actualTrigger = TriggerHandler.parseTrigger(trigger, affectedCard, false);
                    affectedCard.addTrigger(actualTrigger).setTemporary(true);
                }
            }

            // remove triggers
            if (params.containsKey("RemoveTriggers") || removeAllAbilities) {
                for (final Trigger trigger : affectedCard.getTriggers()) {
                    trigger.setTemporarilySuppressed(true);
                }
            }

            // remove activated and static abilities
            if (removeAllAbilities) {
                for (final SpellAbility ab : affectedCard.getSpellAbilities()) {
                    ab.setTemporarilySuppressed(true);
                }
                for (final StaticAbility stA : affectedCard.getStaticAbilities()) {
                    stA.setTemporarilySuppressed(true);
                }
                for (final TriggerReplacementBase rE : affectedCard.getReplacementEffects()) {
                    rE.setTemporarilySuppressed(true);
                }
            }
        }
        
        return affectedCards;
    }

    private static ArrayList<Player> getAffectedPlayers(final StaticAbility stAb) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();
        final Player controller = hostCard.getController();

        final ArrayList<Player> players = new ArrayList<Player>();

        if (!params.containsKey("Affected")) {
            return players;
        }

        final String[] strngs = params.get("Affected").split(",");

        for (Player p : controller.getGame().getPlayers()) {
            if (p.isValid(strngs, controller, hostCard)) {
                players.add(p);
            }
        }

        return players;
    }

    private static List<Card> getAffectedCards(final StaticAbility stAb) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();
        final Game game = hostCard.getGame();
        final Player controller = hostCard.getController();

        if (params.containsKey("CharacteristicDefining")) {
            return Lists.newArrayList(hostCard); // will always be the card itself
        }

        // non - CharacteristicDefining
        List<Card> affectedCards = new ArrayList<Card>();

        if (params.containsKey("AffectedZone")) {
            affectedCards.addAll(game.getCardsIn(ZoneType.listValueOf(params.get("AffectedZone"))));
        } else {
            affectedCards = game.getCardsIn(ZoneType.Battlefield);
        }

        if (params.containsKey("Affected") && !params.get("Affected").contains(",")) {
            if (params.get("Affected").contains("Self")) {
                affectedCards = Lists.newArrayList(hostCard);
            } else if (params.get("Affected").contains("EnchantedBy")) {
                affectedCards = Lists.newArrayList(hostCard.getEnchantingCard());
            } else if (params.get("Affected").contains("EquippedBy")) {
                affectedCards = Lists.newArrayList(hostCard.getEquippingCard());
            } else if (params.get("Affected").equals("EffectSource")) {
                affectedCards = new ArrayList<Card>(AbilityUtils.getDefinedCards(hostCard, params.get("Affected"), null));
                return affectedCards;
            }
        }

        if (params.containsKey("Affected")) {
            affectedCards = CardLists.getValidCards(affectedCards, params.get("Affected").split(","), controller, hostCard);
        }

        return affectedCards;
    }

}
