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
package forge.card.cardfactory;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.CardPredicates.Presets;
import forge.CardUtil;
import forge.Command;
import forge.CounterType;
import forge.GameEntity;
import forge.GameLogEntryType;
import forge.Singletons;
import forge.card.CardCharacteristicName;
import forge.card.CardType;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.ability.AbilityFactory;
import forge.card.ability.AbilityUtils;
import forge.card.ability.ApiType;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostParser;
import forge.card.replacement.ReplacementEffect;
import forge.card.replacement.ReplacementHandler;
import forge.card.replacement.ReplacementLayer;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.OptionalCost;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityRestriction;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.TargetRestrictions;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.game.Game;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.event.GameEventCardStatsChanged;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.input.InputSelectCards;
import forge.gui.input.InputSelectCardsFromList;
import forge.util.Aggregates;
import forge.util.Lang;

/**
 * <p>
 * CardFactoryUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryUtil {

    /**
     * <p>
     * abilityUnearth.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.spellability.AbilityActivated} object.
     */
    public static AbilityActivated abilityUnearth(final Card sourceCard, final String manaCost) {

        final Cost cost = new Cost(manaCost, true);
        class AbilityUnearth extends AbilityActivated {
            public AbilityUnearth(final Card ca, final Cost co, final TargetRestrictions t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityUnearth(getSourceCard(),
                        getPayCosts(), getTargetRestrictions() == null ? null : new TargetRestrictions(getTargetRestrictions()));
                CardFactory.copySpellAbility(this, res);
                final SpellAbilityRestriction restrict = new SpellAbilityRestriction();
                restrict.setZone(ZoneType.Graveyard);
                restrict.setSorcerySpeed(true);
                res.setRestrictions(restrict);
                return res;
            }

            private static final long serialVersionUID = -5633945565395478009L;

            @Override
            public void resolve() {
                final Card card = sourceCard.getGame().getAction().moveToPlay(sourceCard);

                card.addHiddenExtrinsicKeyword("At the beginning of the end step, exile CARDNAME.");
                card.addIntrinsicKeyword("Haste");
                card.setUnearthed(true);
            }

            @Override
            public boolean canPlayAI() {
                PhaseHandler phase = sourceCard.getGame().getPhaseHandler();
                if (phase.getPhase().isAfter(PhaseType.MAIN1) || !phase.isPlayerTurn(getActivatingPlayer())) {
                    return false;
                }
                return ComputerUtilCost.canPayCost(this, getActivatingPlayer());
            }
        }
        final AbilityActivated unearth = new AbilityUnearth(sourceCard, cost, null);

        final SpellAbilityRestriction restrict = new SpellAbilityRestriction();
        restrict.setZone(ZoneType.Graveyard);
        restrict.setSorcerySpeed(true);
        unearth.setRestrictions(restrict);

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append("Unearth: ").append(sourceCard.getName());
        unearth.setStackDescription(sbStack.toString());

        return unearth;
    } // abilityUnearth()

    /**
     * <p>
     * abilityMorphDown.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilityMorphDown(final Card sourceCard) {
        final Spell morphDown = new Spell(sourceCard, new Cost(ManaCost.THREE, false)) {
            private static final long serialVersionUID = -1438810964807867610L;

            @Override
            public void resolve() {
                Card c = sourceCard.getGame().getAction().moveToPlay(sourceCard);
                c.setPreFaceDownCharacteristic(CardCharacteristicName.Original);
            }

            @Override
            public boolean canPlay() {
                //Lands do not have SpellPermanents.
                if (sourceCard.isLand()) {
                    return (sourceCard.getGame().getZoneOf(sourceCard).is(ZoneType.Hand) || sourceCard.hasKeyword("May be played"))
                            && sourceCard.getController().canCastSorcery();
                }
                else {
                    return sourceCard.getSpellPermanent().canPlay();
                }
            }
        };

        morphDown.setDescription("(You may cast this face down as a 2/2 creature for {3}.)");
        morphDown.setStackDescription("Morph - Creature 2/2");
        morphDown.setCastFaceDown(true);

        return morphDown;
    }

    /**
     * <p>
     * abilityMorphUp.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param cost
     *            a {@link forge.card.cost.Cost} object.
     * @return a {@link forge.card.spellability.AbilityActivated} object.
     */
    public static AbilityStatic abilityMorphUp(final Card sourceCard, final Cost cost) {
        final AbilityStatic morphUp = new AbilityStatic(sourceCard, cost, null) {

            @Override
            public void resolve() {
                if (sourceCard.turnFaceUp()) {
                    String sb = this.getActivatingPlayer() + " has unmorphed " + sourceCard.getName();
                    sourceCard.getGame().getGameLog().add(GameLogEntryType.STACK_RESOLVE, sb);
                    sourceCard.getGame().fireEvent(new GameEventCardStatsChanged(sourceCard));
                }
            }

            @Override
            public boolean canPlay() {
                return sourceCard.getController().equals(this.getActivatingPlayer()) && sourceCard.isFaceDown()
                        && sourceCard.isInPlay();
            }

        }; // morph_up

        String costDesc = cost.toString();
        // get rid of the ": " at the end
        costDesc = costDesc.substring(0, costDesc.length() - 2);
        final StringBuilder sb = new StringBuilder();
        sb.append("Morph");
        if (!cost.isOnlyManaCost()) {
            sb.append(" -");
        }
        sb.append(" ").append(costDesc).append(" (Turn this face up any time for its morph cost.)");
        morphUp.setDescription(sb.toString());

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard.getName()).append(" - turn this card face up.");
        morphUp.setStackDescription(sbStack.toString());
        morphUp.setIsMorphUp(true);

        return morphUp;
    }

    /**
     * <p>
     * abilityCycle.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param cycleCost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilityCycle(final Card sourceCard, String cycleCost) {
        StringBuilder sb = new StringBuilder();
        sb.append("AB$ Draw | Cost$ ");
        sb.append(cycleCost);
        sb.append(" Discard<1/CARDNAME> | ActivationZone$ Hand | PrecostDesc$ Cycling ");
        sb.append("| SpellDescription$ Draw a card.");

        SpellAbility cycle = AbilityFactory.getAbility(sb.toString(), sourceCard);
        cycle.setIsCycling(true);

        return cycle;
    } // abilityCycle()

    /**
     * <p>
     * abilityTypecycle.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param cycleCost
     *            a {@link java.lang.String} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilityTypecycle(final Card sourceCard, String cycleCost, final String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("AB$ ChangeZone | Cost$ ").append(cycleCost);

        String desc = type;
        if (type.equals("Basic")) {
            desc = "Basic land";
        }

        sb.append(" Discard<1/CARDNAME> | ActivationZone$ Hand | PrecostDesc$ ").append(desc).append("cycling ");
        sb.append("| Origin$ Library | Destination$ Hand |");
        sb.append("ChangeType$ ").append(type);
        sb.append(" | SpellDescription$ Search your library for a ").append(desc).append(" card, reveal it,");
        sb.append(" and put it into your hand. Then shuffle your library.");

        SpellAbility cycle = AbilityFactory.getAbility(sb.toString(), sourceCard);
        cycle.setIsCycling(true);

        return cycle;
    } // abilityTypecycle()

    /**
     * <p>
     * abilityTransmute.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param transmuteCost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilityTransmute(final Card sourceCard, String transmuteCost) {
        transmuteCost += " Discard<1/CARDNAME>";
        final Cost abCost = new Cost(transmuteCost, true);
        class AbilityTransmute extends AbilityActivated {
            public AbilityTransmute(final Card ca, final Cost co, final TargetRestrictions t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityTransmute(getSourceCard(), getPayCosts(), getTargetRestrictions());
                CardFactory.copySpellAbility(this, res);
                res.getRestrictions().setZone(ZoneType.Hand);
                return res;
            }

            private static final long serialVersionUID = -4960704261761785512L;

            @Override
            public boolean canPlayAI() {
                return false;
            }

            @Override
            public boolean canPlay() {
                return super.canPlay() && sourceCard.getController().canCastSorcery();
            }

            @Override
            public void resolve() {
                final List<Card> cards = sourceCard.getController().getCardsIn(ZoneType.Library);
                final List<Card> sameCost = new ArrayList<Card>();

                for (Card c : cards) {
                    if (c.isSplitCard() && c.getCurState() == CardCharacteristicName.Original) {
                        if (c.getState(CardCharacteristicName.LeftSplit).getManaCost().getCMC() == sourceCard.getManaCost().getCMC() ||
                                c.getState(CardCharacteristicName.RightSplit).getManaCost().getCMC() == sourceCard.getManaCost().getCMC()) {
                            sameCost.add(c);
                        }
                    }
                    else if (c.getManaCost().getCMC() == sourceCard.getManaCost().getCMC()) {
                        sameCost.add(c);
                    }
                }

                if (sameCost.isEmpty()) {
                    return;
                }

                final Card c1 = GuiChoose.oneOrNone("Select a card", sameCost);
                if (c1 != null) {
                    // ability.setTargetCard((Card)o);

                    sourceCard.getController().discard(sourceCard, this);
                    sourceCard.getGame().getAction().moveToHand(c1);

                }
                sourceCard.getController().shuffle(this);
            }
        }
        final SpellAbility transmute = new AbilityTransmute(sourceCard, abCost, null);

        final StringBuilder sbDesc = new StringBuilder();
        sbDesc.append("Transmute (").append(abCost.toString());
        sbDesc.append("Search your library for a card with the same converted mana cost as this card, reveal it, ");
        sbDesc.append("and put it into your hand. Then shuffle your library. Transmute only as a sorcery.)");
        transmute.setDescription(sbDesc.toString());

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard).append(" Transmute: Search your library ");
        sbStack.append("for a card with the same converted mana cost.)");
        transmute.setStackDescription(sbStack.toString());

        transmute.getRestrictions().setZone(ZoneType.Hand);
        return transmute;
    } // abilityTransmute()

    /**
     * <p>
     * abilitySuspendStatic.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param suspendCost
     *            a {@link java.lang.String} object.
     * @param timeCounters
     *            a int.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilitySuspendStatic(final Card sourceCard, final String suspendCost, final String timeCounters) {
        // be careful with Suspend ability, it will not hit the stack
        Cost cost = new Cost(suspendCost, true);
        final SpellAbility suspend = new AbilityStatic(sourceCard, cost, null) {
            @Override
            public boolean canPlay() {
                if (!(this.getRestrictions().canPlay(sourceCard, this))) {
                    return false;
                }

                if (sourceCard.isInstant() || sourceCard.hasKeyword("Flash")) {
                    return true;
                }

                return sourceCard.getOwner().canCastSorcery();
            }

            @Override
            public boolean canPlayAI() {
                return true;
                // Suspend currently not functional for the AI,
                // seems to be an issue with regaining Priority after Suspension
            }

            @Override
            public void resolve() {
                final Game game = sourceCard.getGame();
                final Card c = game.getAction().exile(sourceCard);

                int counters = AbilityUtils.calculateAmount(c, timeCounters, this);
                c.addCounter(CounterType.TIME, counters, true);
                
                String sb = String.format("%s has suspended %s with %d time counters on it.", this.getActivatingPlayer(), c.getName(), counters);
                game.getGameLog().add(GameLogEntryType.STACK_RESOLVE, sb);
            }
        };
        final StringBuilder sbDesc = new StringBuilder();
        sbDesc.append("Suspend ").append(timeCounters).append(" - ").append(cost.toSimpleString());
        suspend.setDescription(sbDesc.toString());

        String svar = "X"; // emulate "References X" here
        suspend.setSVar(svar, sourceCard.getSVar(svar));

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard.getName()).append(" suspending for ").append(timeCounters).append(" turns.)");
        suspend.setStackDescription(sbStack.toString());

        suspend.getRestrictions().setZone(ZoneType.Hand);
        return suspend;
    } // abilitySuspendStatic()

    public static void addSuspendUpkeepTrigger(Card card) {
        //upkeep trigger
        StringBuilder upkeepTrig = new StringBuilder();
        UUID triggerSvar = UUID.randomUUID();
        UUID removeCounterSvar = UUID.randomUUID();

        upkeepTrig.append("Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | TriggerZones$ Exile | CheckSVar$ ");
        upkeepTrig.append(triggerSvar);
        upkeepTrig.append(" | SVarCompare$ GE1 | References$ ");
        upkeepTrig.append(triggerSvar);
        upkeepTrig.append(" | Execute$ ");
        upkeepTrig.append(removeCounterSvar);
        upkeepTrig.append(" | TriggerDescription$ At the beginning of your upkeep, if this card is suspended, remove a time counter from it");

        card.setSVar(removeCounterSvar.toString(), "DB$ RemoveCounter | Defined$ Self | CounterType$ TIME | CounterNum$ 1");
        card.setSVar(triggerSvar.toString(),"Count$ValidExile Card.Self+suspended");

        final Trigger parsedUpkeepTrig = TriggerHandler.parseTrigger(upkeepTrig.toString(), card, true);
        card.addTrigger(parsedUpkeepTrig);
    }

    public static void addSuspendPlayTrigger(Card card) {
        //play trigger
        StringBuilder playTrig = new StringBuilder();
        UUID playSvar = UUID.randomUUID();

        playTrig.append("Mode$ CounterRemoved | TriggerZones$ Exile | ValidCard$ Card.Self | NewCounterAmount$ 0 | Secondary$ True | Execute$ ");
        playTrig.append(playSvar.toString());
        playTrig.append(" | TriggerDescription$ When the last time counter is removed from this card, if it's exiled, play it without paying its mana cost if able.  ");
        playTrig.append("If you can't, it remains exiled. If you cast a creature spell this way, it gains haste until you lose control of the spell or the permanent it becomes.");

        StringBuilder playWithoutCost = new StringBuilder();
        playWithoutCost.append("DB$ Play | Defined$ Self | WithoutManaCost$ True | SuspendCast$ True");

        final Trigger parsedPlayTrigger = TriggerHandler.parseTrigger(playTrig.toString(), card, true);
        card.addTrigger(parsedPlayTrigger);

        card.setSVar(playSvar.toString(),playWithoutCost.toString());
    }



    /**
     * <p>
     * getNumberOfManaSymbolsByColor.
     * </p>
     * 
     * @param colorAbb
     *            a {@link java.lang.String} object.
     * @param cards
     *            a {@link forge.List<Card>} object.
     * @return a int.
     */
    public static int getNumberOfManaSymbolsByColor(final String colorAbb, final List<Card> cards) {
        int count = 0;
        for (Card c : cards) {
            // Certain tokens can have mana cost, so don't skip them
            count += getNumberOfManaSymbolsByColor(colorAbb, c);
        }
        return count;
    }

    /**
     * <p>
     * getNumberOfManaSymbolsByColor.
     * </p>
     * 
     * @param colorAbb
     *            a {@link java.lang.String} object.
     * @param card
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int getNumberOfManaSymbolsByColor(final String colorAbb, final Card card) {
        return countOccurrences(card.getManaCost().toString().trim(), colorAbb);
    }

    /**
     * <p>
     * multiplyCost.
     * </p>
     * 
     * @param manacost
     *            a {@link java.lang.String} object.
     * @param multiplier
     *            a int.
     * @return a {@link java.lang.String} object.
     */
    public static String multiplyCost(final String manacost, final int multiplier) {
        if (multiplier == 0) {
            return "";
        }
        if (multiplier == 1) {
            return manacost;
        }

        final String[] tokenized = manacost.split("\\s");
        final StringBuilder sb = new StringBuilder();

        if (Character.isDigit(tokenized[0].charAt(0))) {
            // cost starts with "colorless" number cost
            int cost = Integer.parseInt(tokenized[0]);
            cost = multiplier * cost;
            tokenized[0] = "" + cost;
            sb.append(tokenized[0]);
        } else {
            if (tokenized[0].contains("<")) {
                final String[] advCostPart = tokenized[0].split("<");
                final String costVariable = advCostPart[1].split(">")[0];
                final String[] advCostPartValid = costVariable.split("\\/", 2);
                // multiply the number part of the cost object
                int num = Integer.parseInt(advCostPartValid[0]);
                num = multiplier * num;
                tokenized[0] = advCostPart[0] + "<" + num;
                if (advCostPartValid.length > 1) {
                    tokenized[0] = tokenized[0] + "/" + advCostPartValid[1];
                }
                tokenized[0] = tokenized[0] + ">";
                sb.append(tokenized[0]);
            } else {
                for (int i = 0; i < multiplier; i++) {
                    // tokenized[0] = tokenized[0] + " " + tokenized[0];
                    sb.append((" "));
                    sb.append(tokenized[0]);
                }
            }
        }

        for (int i = 1; i < tokenized.length; i++) {
            if (tokenized[i].contains("<")) {
                final String[] advCostParts = tokenized[i].split("<");
                final String costVariables = advCostParts[1].split(">")[0];
                final String[] advCostPartsValid = costVariables.split("\\/", 2);
                // multiply the number part of the cost object
                int num = Integer.parseInt(advCostPartsValid[0]);
                num = multiplier * num;
                tokenized[i] = advCostParts[0] + "<" + num;
                if (advCostPartsValid.length > 1) {
                    tokenized[i] = tokenized[i] + "/" + advCostPartsValid[1];
                }
                tokenized[i] = tokenized[i] + ">";
                sb.append((" "));
                sb.append(tokenized[i]);
            } else {
                for (int j = 0; j < multiplier; j++) {
                    // tokenized[i] = tokenized[i] + " " + tokenized[i];
                    sb.append((" "));
                    sb.append(tokenized[i]);
                }
            }
        }

        String result = sb.toString();
        System.out.println("result: " + result);
        result = result.trim();
        return result;
    }

    /**
     * <p>
     * isTargetStillValid.
     * </p>
     * 
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param target
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean isTargetStillValid(final SpellAbility ability, final Card target) {

        Zone zone = target.getGame().getZoneOf(target);
        if (zone == null) {
            return false; // for tokens that disappeared
        }

        final Card source = ability.getSourceCard();
        final TargetRestrictions tgt = ability.getTargetRestrictions();
        if (tgt != null) {
            // Reconfirm the Validity of a TgtValid, or if the Creature is still
            // a Creature
            if (tgt.doesTarget()
                    && !target.isValid(tgt.getValidTgts(), ability.getActivatingPlayer(), ability.getSourceCard())) {
                return false;
            }

            // Check if the target is in the zone it needs to be in to be targeted
            if (!tgt.getZone().contains(zone.getZoneType())) {
                return false;
            }
        } else {
            // If an Aura's target is removed before it resolves, the Aura
            // fizzles
            if (source.isAura() && !target.isInZone(ZoneType.Battlefield)) {
                return false;
            }
        }

        // Make sure it's still targetable as well
        return target.canBeTargetedBy(ability);
    }

    // does "target" have protection from "card"?
    /**
     * <p>
     * hasProtectionFrom.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param target
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean hasProtectionFrom(final Card card, final Card target) {
        if (target == null) {
            return false;
        }

        return target.hasProtectionFrom(card);
    }

    /**
     * <p>
     * isCounterable.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean isCounterable(final Card c) {
        if (c.hasKeyword("CARDNAME can't be countered.") || !c.getCanCounter()) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * isCounterableBy.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            the sa
     * @return a boolean.
     */
    public static boolean isCounterableBy(final Card c, final SpellAbility sa) {
        if (!isCounterable(c)) {
            return false;
        }
        // Autumn's Veil
        if (c.hasKeyword("CARDNAME can't be countered by blue or black spells.") && sa.isSpell() 
                && (sa.getSourceCard().isBlack() || sa.getSourceCard().isBlue())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * countOccurrences.
     * </p>
     * 
     * @param arg1
     *            a {@link java.lang.String} object.
     * @param arg2
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int countOccurrences(final String arg1, final String arg2) {

        int count = 0;
        int index = 0;
        while ((index = arg1.indexOf(arg2, index)) != -1) {
            ++index;
            ++count;
        }
        return count;
    }

    /**
     * <p>
     * parseMath.
     * </p>
     * 
     * @param l
     *            an array of {@link java.lang.String} objects.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String extractOperators(final String expression) {
        String[] l = expression.split("/");
        return l.length > 1 ? l[1] : null;
    }

    /**
     * <p>
     * Parse player targeted X variables.
     * </p>
     * 
     * @param players
     *            a {@link java.util.ArrayList} object.
     * @param s
     *            a {@link java.lang.String} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int objectXCount(final List<?> objects, final String s, final Card source) {
        if (objects.isEmpty()) {
            return 0;
        }

        int n = s.startsWith("Amount") ? objects.size() : 0;
        return doXMath(n, extractOperators(s), source);
    }

    /**
     * <p>
     * Parse player targeted X variables.
     * </p>
     * 
     * @param players
     *            a {@link java.util.ArrayList} object.
     * @param s
     *            a {@link java.lang.String} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int playerXCount(final List<Player> players, final String s, final Card source) {
        if (players.size() == 0) {
            return 0;
        }

        final String[] l = s.split("/");
        final String m = extractOperators(s);

        int n = 0;

        // methods for getting the highest/lowest playerXCount from a range of players
        if (l[0].startsWith("Highest")) {
            for (final Player player : players) {
                final int current = playerXProperty(player, s.replace("Highest", ""), source);
                if (current > n) {
                    n = current;
                }
            }

            return doXMath(n, m, source);
        } else if (l[0].startsWith("Lowest")) {
            n = 99999; // if no players have fewer than 99999 valids, the game is frozen anyway
            for (final Player player : players) {
                final int current = playerXProperty(player, s.replace("Lowest", ""), source);
                if (current < n) {
                    n = current;
                }
            }
            return doXMath(n, m, source);
        }


        final String[] sq;
        sq = l[0].split("\\.");

        // the number of players passed in
        if (sq[0].equals("Amount")) {
            return doXMath(players.size(), m, source);
        }
        if (sq[0].contains("DamageThisTurn")) {
            int totDmg = 0;
            for (Player p : players) {
                totDmg += p.getAssignedDamage();
            }
            return doXMath(totDmg, m, source);
        }

        if(players.size() > 0)
            return playerXProperty(players.get(0), s, source);

        return doXMath(n, m, source);
    }
    
    public static int playerXProperty(Player player, String s, Card source) {
        final String[] l = s.split("/");
        final String m = extractOperators(s);
        
        final Game game = player.getGame();
        
        // count valid cards in any specified zone/s
        if (l[0].startsWith("Valid") && !l[0].contains("Valid ")) {
            String[] lparts = l[0].split(" ", 2);
            final List<ZoneType> vZone = ZoneType.listValueOf(lparts[0].split("Valid")[1]);
            String restrictions = l[0].replace(lparts[0] + " ", "");
            final String[] rest = restrictions.split(",");
            List<Card> cards = game.getCardsIn(vZone);
            cards = CardLists.getValidCards(cards, rest, player, source);
            return doXMath(cards.size(), m, source);
        }
        // count valid cards on the battlefield
        if (l[0].startsWith("Valid ")) {
            final String restrictions = l[0].substring(6);
            final String[] rest = restrictions.split(",");
            List<Card> cardsonbattlefield = game.getCardsIn(ZoneType.Battlefield);
            cardsonbattlefield = CardLists.getValidCards(cardsonbattlefield, rest, player, source);
            return doXMath(cardsonbattlefield.size(), m, source);
        }
        
        final String[] sq = l[0].split("\\.");
        final String value = sq[0];
        
        if (value.contains("CardsInHand")) {
            return doXMath(player.getCardsIn(ZoneType.Hand).size(), m, source);
        }

        if (value.contains("NumPowerSurgeLands")) {
            return doXMath(player.getNumPowerSurgeLands(), m, source);
        }

        if (value.contains("DomainPlayer")) {
            int n = 0;
            final List<Card> someCards = new ArrayList<Card>();
            someCards.addAll(player.getCardsIn(ZoneType.Battlefield));
            final List<String> basic = MagicColor.Constant.BASIC_LANDS;

            for (int i = 0; i < basic.size(); i++) {
                if (!CardLists.getType(someCards, basic.get(i)).isEmpty()) {
                    n++;
                }
            }
            return doXMath(n, m, source);
        }

        if (value.contains("CardsInLibrary")) {
            return doXMath(player.getCardsIn(ZoneType.Library).size(), m, source);
        }

        if (value.contains("CardsInGraveyard")) {
            return doXMath(player.getCardsIn(ZoneType.Graveyard).size(), m, source);
        }
        if (value.contains("LandsInGraveyard")) {
            return doXMath(CardLists.getType(player.getCardsIn(ZoneType.Graveyard), "Land").size(), m, source);
        }

        if (value.contains("CreaturesInPlay")) {
            return doXMath(player.getCreaturesInPlay().size(), m, source);
        }

        if (value.contains("CardsInPlay")) {
            return doXMath(player.getCardsIn(ZoneType.Battlefield).size(), m, source);
        }

        if (value.contains("LifeTotal")) {
            return doXMath(player.getLife(), m, source);
        }

        if (value.contains("LifeLostThisTurn")) {
            return doXMath(player.getLifeLostThisTurn(), m, source);
        }

        if (value.contains("LifeGainedThisTurn")) {
            return doXMath(player.getLifeGainedThisTurn(), m, source);
        }

        if (value.contains("PoisonCounters")) {
            return doXMath(player.getPoisonCounters(), m, source);
        }

        if (value.contains("TopOfLibraryCMC")) {
            return doXMath(Aggregates.sum(player.getCardsIn(ZoneType.Library, 1), CardPredicates.Accessors.fnGetCmc), m, source);
        }

        if (value.contains("LandsPlayed")) {
            return doXMath(player.getNumLandsPlayed(), m, source);
        }

        if (value.contains("CardsDrawn")) {
            return doXMath(player.getNumDrawnThisTurn(), m, source);
        }
        
        if (value.contains("CardsDiscardedThisTurn")) {
            return doXMath(player.getNumDiscardedThisTurn(), m, source);
        }

        if (value.contains("AttackersDeclared")) {
            return doXMath(player.getAttackersDeclaredThisTurn(), m, source);
        }

        if (value.equals("DamageDoneToPlayerBy")) {
            return doXMath(source.getDamageDoneToPlayerBy(player.getName()), m, source);
        }

        if (value.contains("DamageToOppsThisTurn")) {
            int oppDmg = 0;
            for (Player opp : player.getOpponents()) {
                oppDmg += opp.getAssignedDamage();
            }
            return doXMath(oppDmg, m, source);
        }
        
        return doXMath(0, m, source);
    }

    /**
     * <p>
     * Parse non-mana X variables.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param expression
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int xCount(final Card c, final String expression) {
        if (StringUtils.isBlank(expression)) return 0;
        if (StringUtils.isNumeric(expression)) return Integer.parseInt(expression);

        final Player cc = c.getController();
        final Player ccOpp = cc.getOpponent();
        final Game game = c.getGame();
        final Player activePlayer = game.getPhaseHandler().getPlayerTurn();
        

        final String[] l = expression.split("/");
        final String m = extractOperators(expression);

        // accept straight numbers
        if (l[0].startsWith("Number$")) {
            final String number = l[0].substring(7);
            if (number.equals("ChosenNumber")) {
                return doXMath(c.getChosenNumber(), m, c);
            } else {
                return doXMath(Integer.parseInt(number), m, c);
            }
        }

        if (l[0].startsWith("Count$")) {
            l[0] = l[0].substring(6);
        }

        if (l[0].startsWith("SVar$")) {
            return doXMath(xCount(c, c.getSVar(l[0].substring(5))), m, c);
        }
        
        if (l[0].startsWith("Controller$"))
            return playerXProperty(cc, l[0].substring(11), c);
        

        // Manapool
        if (l[0].startsWith("ManaPool")) {
            final String color = l[0].split(":")[1];
            if (color.equals("All")) {
                return cc.getManaPool().totalMana();
            } else {
                return cc.getManaPool().getAmountOfColor(MagicColor.fromName(color));
            }
        }

        // count valid cards in any specified zone/s
        if (l[0].startsWith("Valid")) {
            String[] lparts = l[0].split(" ", 2);
            final String[] rest = lparts[1].split(",");

            final List<Card> cardsInZones = lparts[0].length() > 5 
                ? game.getCardsIn(ZoneType.listValueOf(lparts[0].substring(5)))
                : game.getCardsIn(ZoneType.Battlefield);

            List<Card> cards = CardLists.getValidCards(cardsInZones, rest, cc, c);
            return doXMath(cards.size(), m, c);
        }


        if (l[0].startsWith("ImprintedCardPower") && !c.getImprinted().isEmpty())       return c.getImprinted().get(0).getNetAttack();
        if (l[0].startsWith("ImprintedCardToughness") && !c.getImprinted().isEmpty())   return c.getImprinted().get(0).getNetDefense();
        if (l[0].startsWith("ImprintedCardManaCost") && !c.getImprinted().isEmpty())    return c.getImprinted().get(0).getCMC();

        if (l[0].startsWith("GreatestPowerYouControl")) {
            int highest = 0;
            for (final Card crd : c.getController().getCreaturesInPlay()) {
                if (crd.getNetAttack() > highest) {
                    highest = crd.getNetAttack();
                }
            }
            return highest;
        }

        if (l[0].startsWith("GreatestPowerYouDontControl")) {
            int highest = 0;
            for (final Card crd : c.getController().getGame().getCardsIn(ZoneType.Battlefield)) {
                if (!crd.isCreature() || crd.getController() == c.getController())
                    continue;
                if (crd.getNetAttack() > highest) {
                    highest = crd.getNetAttack();
                }
            }
            return highest;
        }

        if (l[0].startsWith("HighestCMC_")) {
            final String restriction = l[0].substring(11);
            final String[] rest = restriction.split(",");
            List<Card> list = cc.getGame().getCardsInGame();
            list = CardLists.getValidCards(list, rest, cc, c);
            int highest = 0;
            for (final Card crd : list) {
                if (crd.isSplitCard()) {
                    if (crd.getCMC(Card.SplitCMCMode.LeftSplitCMC) > highest) {
                        highest = crd.getCMC(Card.SplitCMCMode.LeftSplitCMC);
                    }
                    if (crd.getCMC(Card.SplitCMCMode.RightSplitCMC) > highest) {
                        highest = crd.getCMC(Card.SplitCMCMode.RightSplitCMC);
                    }
                } else {
                    if (crd.getCMC() > highest) {
                        highest = crd.getCMC();
                    }
                }
            }
            return highest;
        }

        if (l[0].startsWith("DifferentCardNames_")) {
            final List<String> crdname = new ArrayList<String>();
            final String restriction = l[0].substring(19);
            final String[] rest = restriction.split(",");
            List<Card> list = cc.getGame().getCardsInGame();
            list = CardLists.getValidCards(list, rest, cc, c);
            for (final Card card : list) {
                if (!crdname.contains(card.getName())) {
                    crdname.add(card.getName());
                }
            }
            return doXMath(crdname.size(), m, c);
        }

        if (l[0].startsWith("RememberedSize")) {
            return doXMath(c.getRemembered().size(), m, c);
        }

        // Count$CountersAdded <CounterType> <ValidSource>
        if (l[0].startsWith("CountersAdded")) {
            final String[] components = l[0].split(" ", 3);
            final CounterType counterType = CounterType.valueOf(components[1]);
            String restrictions = components[2];
            final String[] rest = restrictions.split(",");
            List<Card> candidates = game.getCardsInGame();
            candidates = CardLists.getValidCards(candidates, rest, cc, c);

            int added = 0;
            for (final Card counterSource : candidates) {
                added += c.getCountersAddedBy(counterSource, counterType);
            }
            return doXMath(added, m, c);
        }

        if (l[0].startsWith("CommanderCastFromCommandZone")) {
            // Read SVar CommanderCostRaise from Commander effect
            Card commeff = CardLists.filter(cc.getCardsIn(ZoneType.Command),
                    CardPredicates.nameEquals("Commander effect")).get(0);
            return doXMath(xCount(commeff, commeff.getSVar("CommanderCostRaise")), "DivideEvenlyDown.2", c);
        }
        
        if (l[0].startsWith("MostProminentCreatureType")) {
            String restriction = l[0].split(" ")[1];
            List<Card> list = CardLists.getValidCards(game.getCardsIn(ZoneType.Battlefield), restriction, cc, c);
            return doXMath(getMostProminentCreatureTypeSize(list), m, c);
        }

        if (l[0].startsWith("RolledThisTurn")) {
            return game.getPhaseHandler().getPlanarDiceRolledthisTurn();
        }

        final String[] sq;
        sq = l[0].split("\\.");

        if (sq[0].contains("xPaid"))            return doXMath(c.getXManaCostPaid(), m, c);


        if (sq[0].equals("YouDrewThisTurn"))    return doXMath(c.getController().getNumDrawnThisTurn(), m, c);
        if (sq[0].equals("OppDrewThisTurn"))    return doXMath(c.getController().getOpponent().getNumDrawnThisTurn(), m, c);
        

        if (sq[0].equals("StormCount"))         return doXMath(game.getStack().getCardsCastThisTurn().size() - 1, m, c);
        if (sq[0].equals("DamageDoneThisTurn")) return doXMath(c.getDamageDoneThisTurn(), m, c);
        if (sq[0].equals("BloodthirstAmount"))  return doXMath(c.getController().getBloodthirstAmount(), m, c);
        if (sq[0].equals("RegeneratedThisTurn")) return doXMath(c.getRegeneratedThisTurn(), m, c);
        
        // TriggeringObjects
        if (sq[0].startsWith("Triggered"))      return doXMath(xCount((Card) c.getTriggeringObject("Card"), sq[0].substring(9)), m, c);

        if (sq[0].contains("YourStartingLife"))     return doXMath(cc.getStartingLife(), m, c);
        //if (sq[0].contains("OppStartingLife"))    return doXMath(oppController.getStartingLife(), m, c); // found no cards using it
        

        if (sq[0].contains("YourLifeTotal"))        return doXMath(cc.getLife(), m, c);
        if (sq[0].contains("OppLifeTotal"))         return doXMath(ccOpp.getLife(), m, c);

        //  Count$TargetedLifeTotal (targeted player's life total)
        if (sq[0].contains("TargetedLifeTotal")) {
            for (final SpellAbility sa : c.getCharacteristics().getSpellAbility()) {
                final SpellAbility saTargeting = sa.getSATargetingPlayer();
                if (saTargeting != null) {
                    for (final Player tgtP : saTargeting.getTargets().getTargetPlayers()) {
                        return doXMath(tgtP.getLife(), m, c);
                    }
                }
            }
        }

        if (sq[0].contains("LifeYouLostThisTurn"))  return doXMath(cc.getLifeLostThisTurn(), m, c);
        if (sq[0].contains("LifeYouGainedThisTurn"))  return doXMath(cc.getLifeGainedThisTurn(), m, c);
        if (sq[0].contains("LifeOppsLostThisTurn")) {
            int lost = 0;
            for (Player opp : cc.getOpponents()) {
                lost += opp.getLifeLostThisTurn();
            }
            return doXMath(lost, m, c);
        }

        if (sq[0].equals("TotalDamageDoneByThisTurn"))      return doXMath(c.getTotalDamageDoneBy(), m, c);
        if (sq[0].equals("TotalDamageReceivedThisTurn"))    return doXMath(c.getTotalDamageRecievedThisTurn(), m, c);

        if (sq[0].contains("YourPoisonCounters"))           return doXMath(cc.getPoisonCounters(), m, c);
        if (sq[0].contains("OppPoisonCounters"))            return doXMath(ccOpp.getPoisonCounters(), m, c);

        if (sq[0].contains("OppDamageThisTurn"))            return doXMath(ccOpp.getAssignedDamage(), m, c);
        if (sq[0].contains("YourDamageThisTurn"))           return doXMath(cc.getAssignedDamage(), m, c);

        // Count$YourTypeDamageThisTurn Type
        if (sq[0].contains("YourTypeDamageThisTurn"))       return doXMath(cc.getAssignedDamage(sq[0].split(" ")[1]), m, c);
        if (sq[0].contains("YourDamageSourcesThisTurn")) {
            Iterable<Card> allSrc = cc.getAssignedDamageSources();
            String restriction = sq[0].split(" ")[1];
            List<Card> filtered = CardLists.getValidCards(allSrc, restriction, cc, c);
            return doXMath(filtered.size(), m, c);
        }
        
        if (sq[0].contains("YourLandsPlayed"))              return doXMath(cc.getNumLandsPlayed(), m, c);
        if (sq[0].contains("OppLandsPlayed"))               return doXMath(ccOpp.getNumLandsPlayed(), m, c);


        // Count$HighestLifeTotal
        if (sq[0].contains("HighestLifeTotal")) {
            return doXMath(Aggregates.max(cc.getGame().getPlayers(), Player.Accessors.FN_GET_LIFE), m, c);
        }

        // Count$LowestLifeTotal
        if (sq[0].contains("LowestLifeTotal")) {
            final String[] playerType = sq[0].split(" ");
            final boolean onlyOpponents = playerType.length > 1 && playerType[1].equals("Opponent");
            List<Player> checked = onlyOpponents ? cc.getOpponents() : cc.getGame().getPlayers();
            return doXMath(Aggregates.min(checked, Player.Accessors.FN_GET_LIFE), m, c);
        }

        // Count$TopOfLibraryCMC
        if (sq[0].contains("TopOfLibraryCMC")) {
            final List<Card> library = cc.getCardsIn(ZoneType.Library);
            return doXMath(library.isEmpty() ? 0 : library.get(0).getCMC(), m, c);
        }

        // Count$EnchantedControllerCreatures
        if (sq[0].contains("EnchantedControllerCreatures")) {
            List<Card> enchantedControllerInPlay = new ArrayList<Card>();
            if (c.getEnchantingCard() != null) {
                enchantedControllerInPlay = c.getEnchantingCard().getController().getCardsIn(ZoneType.Battlefield);
                enchantedControllerInPlay = CardLists.filter(enchantedControllerInPlay, CardPredicates.Presets.CREATURES);
            }
            return enchantedControllerInPlay.size();
        }

        // Count$LowestLibrary
        if (sq[0].contains("LowestLibrary")) {
            return Aggregates.min(cc.getGame().getPlayers(), Player.Accessors.countCardsInZone(ZoneType.Library));
        }
        
        // Count$MonstrosityMagnitude
        if (sq[0].contains("MonstrosityMagnitude")) {
            return doXMath(c.getMonstrosityNum(), m, c);
        }

        // Count$Chroma.<mana letter>
        if (sq[0].contains("Chroma") || sq[0].contains("Devotion")) {
            ZoneType sourceZone = sq[0].contains("ChromaInGrave") ?  ZoneType.Graveyard : ZoneType.Battlefield;
            String colorAbb = sq[1];
            if (colorAbb.contains("Chosen")) {
                colorAbb = MagicColor.toShortString(c.getChosenColor().get(0));
            }
            final List<Card> cards;
            if (sq[0].contains("ChromaSource")) { // Runs Chroma for passed in Source card
                cards = Lists.newArrayList(c);
            } else {
                cards = cc.getCardsIn(sourceZone);
            }
            return doXMath(getNumberOfManaSymbolsByColor(colorAbb, cards), m, c);
        }

        if (sq[0].contains("Hellbent"))         return doXMath(Integer.parseInt(sq[cc.hasHellbent() ? 1 : 2]), m, c);
        if (sq[0].contains("Metalcraft"))       return doXMath(Integer.parseInt(sq[cc.hasMetalcraft() ? 1 : 2]), m, c);
        if (sq[0].contains("FatefulHour"))      return doXMath(Integer.parseInt(sq[cc.getLife() <= 5 ? 1 : 2]), m, c);

        if (sq[0].contains("Landfall"))         return doXMath(Integer.parseInt(sq[cc.hasLandfall() ? 1 : 2]), m, c);
        if (sq[0].contains("Threshold"))        return doXMath(Integer.parseInt(sq[cc.hasThreshold() ? 1 : 2]), m, c);
        if (sq[0].startsWith("Kicked"))         return doXMath(Integer.parseInt(sq[c.getKickerMagnitude() > 0 ? 1 : 2]), m, c);
        if (sq[0].startsWith("AltCost"))        return doXMath(Integer.parseInt(sq[c.isOptionalCostPaid(OptionalCost.AltCost) ? 1 : 2]), m, c);

        // Count$wasCastFrom<Zone>.<true>.<false>
        if (sq[0].startsWith("wasCastFrom")) {
            boolean zonesMatch = c.getCastFrom() == ZoneType.smartValueOf(sq[0].substring(11)); 
            return doXMath(Integer.parseInt(sq[zonesMatch ? 1 : 2]), m, c);
        }

        if (sq[0].contains("GraveyardWithGE20Cards")) {
            final boolean hasBigGrave = Aggregates.max(cc.getGame().getPlayers(), Player.Accessors.countCardsInZone(ZoneType.Graveyard)) >= 20;
            return doXMath(Integer.parseInt(sq[ hasBigGrave ? 1 : 2]), m, c);
        }

        if (sq[0].startsWith("Devoured")) {
            final String validDevoured = l[0].split(" ")[1];
            List<Card> cl = CardLists.getValidCards(c.getDevoured(), validDevoured.split(","), cc, c);
            return doXMath(cl.size(), m, c);
        }

        if (sq[0].contains("CardPower"))        return doXMath(c.getNetAttack(), m, c);
        if (sq[0].contains("CardToughness"))    return doXMath(c.getNetDefense(), m, c);
        if (sq[0].contains("CardSumPT"))        return doXMath((c.getNetAttack() + c.getNetDefense()), m, c);

        // Count$SumPower_valid
        if (sq[0].contains("SumPower")) {
            final String[] restrictions = l[0].split("_");
            final String[] rest = restrictions[1].split(",");
            List<Card> filteredCards = CardLists.getValidCards(cc.getGame().getCardsIn(ZoneType.Battlefield), rest, cc, c);
            return doXMath(Aggregates.sum(filteredCards, CardPredicates.Accessors.fnGetNetAttack), m, c);
        }
        // Count$CardManaCost
        if (sq[0].contains("CardManaCost")) {
            Card ce = sq[0].contains("Equipped") && c.isEquipping() ? c.getEquipping().get(0) : c;
            return doXMath(ce.getCMC(), m, c);
        }
        // Count$SumCMC_valid
        if (sq[0].contains("SumCMC")) {
            final String[] restrictions = l[0].split("_");
            final String[] rest = restrictions[1].split(",");
            List<Card> cardsonbattlefield = game.getCardsIn(ZoneType.Battlefield);
            List<Card> filteredCards = CardLists.getValidCards(cardsonbattlefield, rest, cc, c);
            return Aggregates.sum(filteredCards, CardPredicates.Accessors.fnGetCmc);
        }

        if (sq[0].contains("CardNumColors"))    return doXMath(CardUtil.getColors(c).countColors(), m, c);
        if (sq[0].contains("ChosenNumber"))     return doXMath(c.getChosenNumber(), m, c);
        if (sq[0].contains("CardCounters")) {
            // CardCounters.ALL to be used for Kinsbaile Borderguard and anything that cares about all counters
            int count = 0;
            if (sq[1].equals("ALL")) {
                for(Integer i : c.getCounters().values()) {
                    if (i != null && i > 0) {
                        count += i;
                    }
                }
            } else {
                count = c.getCounters(CounterType.getType(sq[1]));
            }
            return doXMath(count, m, c);
        }

        // Count$TotalCounters.<counterType>_<valid>
        if (sq[0].contains("TotalCounters")) {
            final String[] restrictions = l[0].split("_");
            final CounterType cType = CounterType.getType(restrictions[1]);
            final String[] validFilter = restrictions[2].split(",");
            List<Card> validCards = game.getCardsIn(ZoneType.Battlefield);
            validCards = CardLists.getValidCards(validCards, validFilter, cc, c);
            int cCount = 0;
            for (final Card card : validCards) {
                cCount += card.getCounters(cType);
            }
            return doXMath(cCount, m, c);
        }

        if (sq[0].contains("CardTypes"))    return doXMath(getCardTypesFromList(game.getCardsIn(ZoneType.smartValueOf(sq[1]))), m, c);

        if (sq[0].contains("BushidoPoint"))     return doXMath(c.getKeywordMagnitude("Bushido"), m, c);
        if (sq[0].contains("TimesKicked"))      return doXMath(c.getKickerMagnitude(), m, c);
        if (sq[0].contains("NumCounters"))      return doXMath(c.getCounters(CounterType.getType(sq[1])), m, c);


        // Count$IfMainPhase.<numMain>.<numNotMain> // 7/10
        if (sq[0].contains("IfMainPhase")) {
            final PhaseHandler cPhase = cc.getGame().getPhaseHandler();
            final boolean isMyMain = cPhase.getPhase().isMain() && cPhase.getPlayerTurn().equals(cc);
            return doXMath(Integer.parseInt(sq[isMyMain ? 1 : 2]), m, c);
        }

        // Count$M12Empires.<numIf>.<numIfNot>
        if (sq[0].contains("AllM12Empires")) {
            boolean has = cc.isCardInPlay("Crown of Empires") && cc.isCardInPlay("Scepter of Empires") && cc.isCardInPlay("Throne of Empires");
            return doXMath(Integer.parseInt(sq[has ? 1 : 2]), m, c);
        }

        // Count$ThisTurnEntered <ZoneDestination> [from <ZoneOrigin>] <Valid>
        if (sq[0].contains("ThisTurnEntered")) {
            final String[] workingCopy = l[0].split("_");
            
            ZoneType destination = ZoneType.smartValueOf(workingCopy[1]);
            final boolean hasFrom = workingCopy[2].equals("from");
            ZoneType origin = hasFrom ? ZoneType.smartValueOf(workingCopy[3]) : null;
            String validFilter = workingCopy[hasFrom ? 4 : 2] ;

            final List<Card> res = CardUtil.getThisTurnEntered(destination, origin, validFilter, c);
            return doXMath(res.size(), m, c);
        }

        // Count$AttackersDeclared
        if (sq[0].contains("AttackersDeclared")) {
            return doXMath(cc.getAttackersDeclaredThisTurn(), m, c);
        }

        // Count$ThisTurnCast <Valid>
        // Count$LastTurnCast <Valid>
        if (sq[0].contains("ThisTurnCast") || sq[0].contains("LastTurnCast")) {

            final String[] workingCopy = l[0].split("_");
            final String validFilter = workingCopy[1];

            List<Card> res;

            if (workingCopy[0].contains("This")) {
                res = CardUtil.getThisTurnCast(validFilter, c);
            } else {
                res = CardUtil.getLastTurnCast(validFilter, c);
            }

            final int ret = doXMath(res.size(), m, c);
            return ret;
        }

        // Count$Morbid.<True>.<False>
        if (sq[0].startsWith("Morbid")) {
            final List<Card> res = CardUtil.getThisTurnEntered(ZoneType.Graveyard, ZoneType.Battlefield, "Creature", c);
            if (res.size() > 0) {
                return doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        if (sq[0].equals("YourTurns")) {
            return doXMath(cc.getTurn(), m, c);
        }

        if (sq[0].equals("TotalTurns")) {
            // Sorry for the Singleton use, replace this once this function has game passed into it
            return doXMath(game.getPhaseHandler().getTurn(), m, c);
        }
        
        //Count$Random.<Min>.<Max>
        if (sq[0].equals("Random")) {
            int min = StringUtils.isNumeric(sq[1]) ? Integer.parseInt(sq[1]) : xCount(c, c.getSVar(sq[1]));
            int max = StringUtils.isNumeric(sq[2]) ? Integer.parseInt(sq[2]) : xCount(c, c.getSVar(sq[2]));

            return forge.util.MyRandom.getRandom().nextInt(1+max-min) + min;
        }


        // Count$Domain
        if (sq[0].startsWith("Domain")) {
            int n = 0;
            Player neededPlayer = sq[0].equals("DomainActivePlayer") ? activePlayer : cc;
            List<Card> someCards = CardLists.filter(neededPlayer.getCardsIn(ZoneType.Battlefield), Presets.LANDS);
            for (String basic : MagicColor.Constant.BASIC_LANDS) {
                if (!CardLists.getType(someCards, basic).isEmpty()) {
                    n++;
                }
            }
            return doXMath(n, m, c);
        }

        // Count$ColoredCreatures *a DOMAIN for creatures*
        if (sq[0].contains("ColoredCreatures")) {
            int mask = 0;
            List<Card> someCards = CardLists.filter(cc.getCardsIn(ZoneType.Battlefield), Presets.CREATURES);
            for (final Card crd : someCards) {
                mask |= CardUtil.getColors(crd).getColor();
            }
            return doXMath(ColorSet.fromMask(mask).countColors(), m, c);
        }
        
        // Count$CardMulticolor.<numMC>.<numNotMC>
        if (sq[0].contains("CardMulticolor")) {
            final boolean isMulti = CardUtil.getColors(c).isMulticolor(); 
            return doXMath(Integer.parseInt(sq[isMulti ? 1 : 2]), m, c);
        }

        
        // Complex counting methods
        List<Card> someCards = getCardListForXCount(c, cc, ccOpp, sq);
        
        // 1/10 - Count$MaxCMCYouCtrl
        if (sq[0].contains("MaxCMC")) {
            int mmc = Aggregates.max(someCards, CardPredicates.Accessors.fnGetCmc);
            return doXMath(mmc, m, c);
        }

        return doXMath(someCards.size(), m, c);
    }

    private static List<Card> getCardListForXCount(final Card c, final Player cc, final Player ccOpp, final String[] sq) {
        List<Card> someCards = new ArrayList<Card>();
        final Game game = c.getGame();
        
        // Generic Zone-based counting
        // Count$QualityAndZones.Subquality

        // build a list of cards in each possible specified zone

        // if a card was ever written to count two different zones,
        // make sure they don't get added twice.
        boolean mf = false, my = false, mh = false;
        boolean of = false, oy = false, oh = false;

        if (sq[0].contains("YouCtrl") && !mf) {
            someCards.addAll(cc.getCardsIn(ZoneType.Battlefield));
            mf = true;
        }

        if (sq[0].contains("InYourYard") && !my) {
            someCards.addAll(cc.getCardsIn(ZoneType.Graveyard));
            my = true;
        }

        if (sq[0].contains("InYourLibrary") && !my) {
            someCards.addAll(cc.getCardsIn(ZoneType.Library));
            my = true;
        }

        if (sq[0].contains("InYourHand") && !mh) {
            someCards.addAll(cc.getCardsIn(ZoneType.Hand));
            mh = true;
        }

        if (sq[0].contains("InYourSideboard") && !mh) {
            someCards.addAll(cc.getCardsIn(ZoneType.Sideboard));
            mh = true;
        }

        if (sq[0].contains("OppCtrl")) {
            if (!of) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Battlefield));
                of = true;
            }
        }

        if (sq[0].contains("InOppYard")) {
            if (!oy) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Graveyard));
                oy = true;
            }
        }

        if (sq[0].contains("InOppHand")) {
            if (!oh) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Hand));
                oh = true;
            }
        }

        if (sq[0].contains("InChosenHand")) {
            if (!oh) {
                if (c.getChosenPlayer() != null) {
                    someCards.addAll(c.getChosenPlayer().getCardsIn(ZoneType.Hand));
                }
                oh = true;
            }
        }

        if (sq[0].contains("InChosenYard")) {
            if (!oh) {
                if (c.getChosenPlayer() != null) {
                    someCards.addAll(c.getChosenPlayer().getCardsIn(ZoneType.Graveyard));
                }
                oh = true;
            }
        }

        if (sq[0].contains("OnBattlefield")) {
            if (!mf) {
                someCards.addAll(cc.getCardsIn(ZoneType.Battlefield));
            }
            if (!of) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Battlefield));
            }
        }

        if (sq[0].contains("InAllYards")) {
            if (!my) {
                someCards.addAll(cc.getCardsIn(ZoneType.Graveyard));
            }
            if (!oy) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Graveyard));
            }
        }

        if (sq[0].contains("SpellsOnStack")) {
            someCards.addAll(game.getCardsIn(ZoneType.Stack));
        }

        if (sq[0].contains("InAllHands")) {
            if (!mh) {
                someCards.addAll(cc.getCardsIn(ZoneType.Hand));
            }
            if (!oh) {
                someCards.addAll(ccOpp.getCardsIn(ZoneType.Hand));
            }
        }

        //  Count$InTargetedHand (targeted player's cards in hand)
        if (sq[0].contains("InTargetedHand")) {
            for (final SpellAbility sa : c.getCharacteristics().getSpellAbility()) {
                final SpellAbility saTargeting = sa.getSATargetingPlayer();
                if (saTargeting != null) {
                    for (final Player tgtP : saTargeting.getTargets().getTargetPlayers()) {
                        someCards.addAll(tgtP.getCardsIn(ZoneType.Hand));
                    }
                }
            }
        }

        //  Count$InTargetedHand (targeted player's cards in hand)
        if (sq[0].contains("InEnchantedHand")) {
            GameEntity o = c.getEnchanting();
            Player controller = null;
            if (o instanceof Card) {
                controller = ((Card) o).getController();
            }
            else {
                controller = (Player) o;
            }
            if (controller != null) {
                someCards.addAll(controller.getCardsIn(ZoneType.Hand));
            }
        }
        if (sq[0].contains("InEnchantedYard")) {
            GameEntity o = c.getEnchanting();
            Player controller = null;
            if (o instanceof Card) {
                controller = ((Card) o).getController();
            }
            else {
                controller = (Player) o;
            }
            if (controller != null) {
                someCards.addAll(controller.getCardsIn(ZoneType.Graveyard));
            }
        }
        // filter lists based on the specified quality

        // "Clerics you control" - Count$TypeYouCtrl.Cleric
        if (sq[0].contains("Type")) {
            someCards = CardLists.filter(someCards, CardPredicates.isType(sq[1]));
        }

        // "Named <CARDNAME> in all graveyards" - Count$NamedAllYards.<CARDNAME>

        if (sq[0].contains("Named")) {
            if (sq[1].equals("CARDNAME")) {
                sq[1] = c.getName();
            }
            someCards = CardLists.filter(someCards, CardPredicates.nameEquals(sq[1]));
        }

        // Refined qualities

        // "Untapped Lands" - Count$UntappedTypeYouCtrl.Land
        if (sq[0].contains("Untapped")) {
            someCards = CardLists.filter(someCards, Presets.UNTAPPED);
        }

        if (sq[0].contains("Tapped")) {
            someCards = CardLists.filter(someCards, Presets.TAPPED);
        }

//        String sq0 = sq[0].toLowerCase();
//        for(String color : MagicColor.Constant.ONLY_COLORS) {
//            if( sq0.contains(color) )
//                someCards = someCards.filter(CardListFilter.WHITE);
//        }
        // "White Creatures" - Count$WhiteTypeYouCtrl.Creature
        if (sq[0].contains("White")) someCards = CardLists.filter(someCards, CardPredicates.isColor(MagicColor.WHITE));
        if (sq[0].contains("Blue"))  someCards = CardLists.filter(someCards, CardPredicates.isColor(MagicColor.BLUE));
        if (sq[0].contains("Black")) someCards = CardLists.filter(someCards, CardPredicates.isColor(MagicColor.BLACK));
        if (sq[0].contains("Red"))   someCards = CardLists.filter(someCards, CardPredicates.isColor(MagicColor.RED));
        if (sq[0].contains("Green")) someCards = CardLists.filter(someCards, CardPredicates.isColor(MagicColor.GREEN));

        if (sq[0].contains("Multicolor")) {
            someCards = CardLists.filter(someCards, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return CardUtil.getColors(c).isMulticolor();
                }
            });
        }

        if (sq[0].contains("Monocolor")) {
            someCards = CardLists.filter(someCards, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return CardUtil.getColors(c).isMonoColor();
                }
            });
        }
        return someCards;
    }

    public static int doXMath(final int num, final String operators, final Card c) {
        if (operators == null || operators.equals("none")) {
            return num;
        }

        final String[] s = operators.split("\\.");
        int secondaryNum = 0;

        try {
            if (s.length == 2) {
                secondaryNum = Integer.parseInt(s[1]);
            }
        } catch (final Exception e) {
            secondaryNum = xCount(c, c.getSVar(s[1]));
        }

        if (s[0].contains("Plus")) {
            return num + secondaryNum;
        } else if (s[0].contains("NMinus")) {
            return secondaryNum - num;
        } else if (s[0].contains("Minus")) {
            return num - secondaryNum;
        } else if (s[0].contains("Twice")) {
            return num * 2;
        } else if (s[0].contains("Thrice")) {
            return num * 3;
        } else if (s[0].contains("HalfUp")) {
            return (int) (Math.ceil(num / 2.0));
        } else if (s[0].contains("HalfDown")) {
            return (int) (Math.floor(num / 2.0));
        } else if (s[0].contains("ThirdUp")) {
            return (int) (Math.ceil(num / 3.0));
        } else if (s[0].contains("ThirdDown")) {
            return (int) (Math.floor(num / 3.0));
        } else if (s[0].contains("Negative")) {
            return num * -1;
        } else if (s[0].contains("Times")) {
            return num * secondaryNum;
        } else if (s[0].contains("DivideEvenlyDown")) {
            if (secondaryNum == 0) {
                return 0;
            } else {
                return num / secondaryNum;
            }
        } else if (s[0].contains("Mod")) {
            return num % secondaryNum;
        } else if (s[0].contains("LimitMax")) {
            if (num < secondaryNum) {
                return num;
            } else {
                return secondaryNum;
            }
        } else if (s[0].contains("LimitMin")) {
            if (num > secondaryNum) {
                return num;
            } else {
                return secondaryNum;
            }

        } else {
            return num;
        }
    }

    /**
     * <p>
     * handlePaid.
     * </p>
     * 
     * @param paidList
     *            a {@link forge.CardList} object.
     * @param string
     *            a {@link java.lang.String} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int handlePaid(final List<Card> paidList, final String string, final Card source) {
        if (paidList == null) {
            if (string.contains(".")) {
                final String[] splitString = string.split("\\.", 2);
                return doXMath(0, splitString[1], source);
            } else {
                return 0;
            }
        }
        if (string.startsWith("Amount")) {
            if (string.contains(".")) {
                final String[] splitString = string.split("\\.", 2);
                return doXMath(paidList.size(), splitString[1], source);
            } else {
                return paidList.size();
            }

        }
        if (string.startsWith("Valid")) {
            
            final String[] splitString = string.split("/", 2);
            String valid = splitString[0].substring(6);
            final List<Card> list = CardLists.getValidCards(paidList, valid, source.getController(), source);
            return doXMath(list.size(), splitString.length > 1 ? splitString[1] : null, source);
        }

        String filteredString = string;
        List<Card> filteredList = new ArrayList<Card>(paidList);
        final String[] filter = filteredString.split("_");

        if (string.startsWith("FilterControlledBy")) {
            final String pString = filter[0].substring(18);
            List<Player> controllers = new ArrayList<Player>(AbilityUtils.getDefinedPlayers(source, pString, null));
            filteredList = CardLists.filterControlledBy(filteredList, controllers);
            filteredString = filteredString.replace(pString, "");
            filteredString = filteredString.replace("FilterControlledBy_", "");
        }

        int tot = 0;
        for (final Card c : filteredList) {
            tot += xCount(c, filteredString);
        }

        return tot;
    }

    /**
     * <p>
     * isMostProminentColor.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a boolean.
     */
    public static byte getMostProminentColors(final List<Card> list) {
        int cntColors = MagicColor.WUBRG.length;
        final Integer[] map = new Integer[cntColors];
        for(int i = 0; i < cntColors; i++) {
            map[i] = 0;
        }

        for (final Card crd : list) {
            ColorSet color = CardUtil.getColors(crd);
            for(int i = 0; i < cntColors; i++) {
                if( color.hasAnyColor(MagicColor.WUBRG[i]))
                    map[i]++;
            }
        } // for

        byte mask = 0;
        int nMax = -1;
        for(int i = 0; i < cntColors; i++) { 
            if ( map[i] > nMax )
                mask = MagicColor.WUBRG[i];
            else if ( map[i] == nMax )
                mask |= MagicColor.WUBRG[i];
            else 
                continue;
            nMax = map[i];
        }
        return mask;
    }

    /**
     * <p>
     * getMostProminentColorsFromList.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a boolean.
     */
    public static byte getMostProminentColorsFromList(final List<Card> list, final List<String> restrictedToColors) {
        List<Byte> colorRestrictions = new ArrayList<Byte>();
        for (final String col : restrictedToColors) {
            colorRestrictions.add(MagicColor.fromName(col));
        }
        int cntColors = colorRestrictions.size();
        final Integer[] map = new Integer[cntColors];
        for(int i = 0; i < cntColors; i++) {
            map[i] = 0;
        }

        for (final Card crd : list) {
            ColorSet color = CardUtil.getColors(crd);
            for (int i = 0; i < cntColors; i++) {
                if (color.hasAnyColor(colorRestrictions.get(i))) {
                    map[i]++;
                }
            }
        }

        byte mask = 0;
        int nMax = -1;
        for(int i = 0; i < cntColors; i++) { 
            if ( map[i] > nMax )
                mask = colorRestrictions.get(i);
            else if ( map[i] == nMax )
                mask |= colorRestrictions.get(i);
            else 
                continue;
            nMax = map[i];
        }
        return mask;
    }

    /**
     * <p>
     * getMostProminentCreatureType.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return an int.
     */
    public static int getMostProminentCreatureTypeSize(final List<Card> list) {
    
        if (list.isEmpty()) {
            return 0;
        }
        int allCreatureType = 0;

        final Map<String, Integer> map = new HashMap<String, Integer>();
        for (final Card c : list) {
            // Remove Duplicated types
            final Set<String> types = new HashSet<String>(c.getType());
            if (types.contains("AllCreatureTypes")) {
                allCreatureType++;
                continue;
            }
            for (final String var : types) {
                if (CardType.isACreatureType(var)) {
                    if (!map.containsKey(var)) {
                        map.put(var, 1);
                    } else {
                        map.put(var, map.get(var) + 1);
                    }
                }
            }
        }
    
        int max = 0;
        for (final Entry<String, Integer> entry : map.entrySet()) {
            if (max < entry.getValue()) {
                max = entry.getValue();
            }
        }

        return max +  allCreatureType;
    }

    /**
     * <p>
     * sharedKeywords.
     * </p>
     * 
     * @param kw
     *            a {@link forge.CardList} object.
     * @return a List<String>.
     */
    public static List<String> sharedKeywords(final String[] kw, final String[] restrictions,
            final List<ZoneType> zones, final Card host) {
        final List<String> filteredkw = new ArrayList<String>();
        final Player p = host.getController();
        List<Card> cardlist = new ArrayList<Card>(p.getGame().getCardsIn(zones));
        final List<String> landkw = new ArrayList<String>();
        final List<String> protectionkw = new ArrayList<String>();
        final List<String> allkw = new ArrayList<String>();
        
        cardlist = CardLists.getValidCards(cardlist, restrictions, p, host);
        for (Card c : cardlist) {
            for (String k : c.getKeyword()) {
                if (k.endsWith("walk")) {
                    if (!landkw.contains(k)) {
                        landkw.add(k);
                    }
                } else if (k.startsWith("Protection")) {
                    if (!protectionkw.contains(k)) {
                        protectionkw.add(k);
                    }
                }
                if (!allkw.contains(k)) {
                    allkw.add(k);
                }
            }
        }
        for (String keyword : kw) {
            if (keyword.equals("Protection")) {
                filteredkw.addAll(protectionkw);
            } else if (keyword.equals("Landwalk")) {
                filteredkw.addAll(landkw);
            } else if (allkw.contains(keyword)) {
                filteredkw.add(keyword);
            }
        }
        return filteredkw;
    }

    /**
     * <p>
     * getCardTypesFromList.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a int.
     */
    public static int getCardTypesFromList(final List<Card> list) {
        int count = 0;
        for (Card c1 : list) {
            if (c1.isCreature()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isSorcery()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isInstant()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isArtifact()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isEnchantment()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isLand()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isPlaneswalker()) {
                count++;
                break;
            }
        }
        for (Card c1 : list) {
            if (c1.isTribal()) {
                count++;
                break;
            }
        }
        return count;
    }

    /**
     * <p>
     * getBushidoEffects.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<SpellAbility> getBushidoEffects(final Card c) {
        final ArrayList<SpellAbility> list = new ArrayList<SpellAbility>();
        for (final String kw : c.getKeyword()) {
            if (kw.contains("Bushido")) {
                final String[] parse = kw.split(" ");
                final String s = parse[1];
                final int magnitude = Integer.parseInt(s);

                String description = String.format("Bushido %d (When this blocks or becomes blocked, it gets +%d/+%d until end of turn).", magnitude, magnitude, magnitude);
                String regularPart = String.format("AB$ Pump | Cost$ 0 | Defined$ CardUID_%d | NumAtt$ +%d | NumDef$ +%d | StackDescription$ %s", c.getUniqueNumber(), magnitude, magnitude, description);
                
                SpellAbility ability = AbilityFactory.getAbility( regularPart, c);
                ability.setDescription(ability.getStackDescription());
                ability.setTrigger(true); // can be copied by Strionic Resonator
                list.add(ability);
            }
        }
        return list;
    }

    /**
     * <p>
     * getNeededXDamage.
     * </p>
     * 
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a int.
     */
    public static int getNeededXDamage(final SpellAbility ability) {
        // when targeting a creature, make sure the AI won't overkill on X
        // damage
        final Card target = ability.getTargetCard();
        int neededDamage = -1;

        if ((target != null)) {
            neededDamage = target.getNetDefense() - target.getDamage();
        }

        return neededDamage;
    }

    public static void correctAbilityChainSourceCard(final SpellAbility sa, final Card card) {

        sa.setSourceCard(card);

        if (sa.getSubAbility() != null) {
            correctAbilityChainSourceCard(sa.getSubAbility(), card);
        }
    }

    /**
     * Adds the ability factory abilities.
     * 
     * @param card
     *            the card
     */
    public static final void addAbilityFactoryAbilities(final Card card) {
        // **************************************************
        // AbilityFactory cards
        for (String rawAbility : card.getUnparsedAbilities()) {
            card.addSpellAbility(AbilityFactory.getAbility(rawAbility, card));
        }
    }
    /*
    public static final void addCommanderAbilities(final Card cmd) {
        ReplacementEffect re = ReplacementHandler.parseReplacement(
                "Event$ Moved | Destination$ Graveyard,Exile | ValidCard$ Card.Self | Secondary$ True | Optional$ True | OptionalDecider$ You | ReplaceWith$ CommanderMoveReplacement | " +
                "Description$ If a commander would be put into its owner's graveyard or exile from anywhere, that player may put it into the command zone instead.",
                cmd, true);
        cmd.addReplacementEffect(re);
        if(StringUtils.isBlank(cmd.getSVar("CommanderCostRaise"))) // why condition check is needed?
            cmd.setSVar("CommanderCostRaise", "Number$0");

        String cmdManaCost = cmd.getManaCost().toString();
        cmd.setSVar("CommanderMoveReplacement", "DB$ ChangeZone | Origin$ Battlefield,Graveyard,Exile,Library | Destination$ Command | Defined$ ReplacedCard");
        cmd.setSVar("DBCommanderIncCast", "DB$ StoreSVar | SVar$ CommanderCostRaise | Type$ CountSVar | Expression$ CommanderCostRaise/Plus.2");
        SpellAbility sa = AbilityFactory.getAbility("SP$ PermanentCreature | SorcerySpeed$ True | ActivationZone$ Command | SubAbility$ DBCommanderIncCast | Cost$ " + cmdManaCost, cmd);
        cmd.addSpellAbility(sa);

        cmd.addIntrinsicAbility("SP$ PermanentCreature | SorcerySpeed$ True | ActivationZone$ Command | SubAbility$ DBCommanderIncCast | Cost$ " + cmdManaCost);
        cmd.addStaticAbility("Mode$ RaiseCost | Amount$ CommanderCostRaise | Type$ Spell | ValidCard$ Card.Self+wasCastFromCommand | EffectZone$ All | AffectedZone$ Stack");
    }
    */
    public static final String getCommanderInfo(final Player originPlayer ) {
        StringBuilder sb = new StringBuilder();
        for(Player p : originPlayer.getGame().getPlayers()) {
            if(p.equals(originPlayer))
                continue;
            
            Map<Card,Integer> map = p.getCommanderDamage();
            if(map.containsKey(originPlayer.getCommander())) {
                sb.append("Commander Damage to " + p.getName() + ": "+ map.get(originPlayer.getCommander()) + "\r\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * <p>
     * postFactoryKeywords.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     */
    public static void setupKeywordedAbilities(final Card card) {
        // this function should handle any keywords that need to be added after
        // a spell goes through the factory
        // Cards with Cycling abilities
        // -1 means keyword "Cycling" not found

        if (hasKeyword(card, "Multikicker") != -1) {
            final int n = hasKeyword(card, "Multikicker");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                final String[] k = parse.split("kicker ");

                final SpellAbility sa = card.getFirstSpellAbility();
                sa.setMultiKickerManaCost(new ManaCost(new ManaCostParser(k[1])));
            }
        }
        
        if(hasKeyword(card, "Fuse") != -1) {
            card.getState(CardCharacteristicName.Original).getSpellAbility().add(AbilityFactory.buildFusedAbility(card));
        }

        final int evokePos = hasKeyword(card, "Evoke");
        if (evokePos != -1) {
            card.addSpellAbility(makeEvokeSpell(card, card.getKeyword().get(evokePos)));
        }
        final int monstrousPos = hasKeyword(card, "Monstrosity");
        if (monstrousPos != -1) {
            final String parse = card.getKeyword().get(monstrousPos).toString();
            final String[] k = parse.split(":");
            final String magnitude = k[0].substring(12);
            final String manacost = k[1];
            card.removeIntrinsicKeyword(parse);

            String ref = "X".equals(magnitude) ? " | References$ X" : "";
            String counters = StringUtils.isNumeric(magnitude) 
                    ? Lang.nounWithNumeral(Integer.parseInt(magnitude), "+1/+1 counter"): "X +1/+1 counters";
            String effect = "AB$ PutCounter | Cost$ " + manacost + " | ConditionPresent$ " +
            		"Card.Self+IsNotMonstrous | Monstrosity$ True | CounterNum$ " +
                    magnitude + " | CounterType$ P1P1 | SpellDescription$ Monstrosity " +
            		magnitude + " (If this creature isn't monstrous, put " + 
                    counters + " on it and it becomes monstrous.) | StackDescription$ SpellDescription" + ref;

            card.addSpellAbility(AbilityFactory.getAbility(effect, card));
            // add ability to instrinic strings so copies/clones create the ability also
            card.getUnparsedAbilities().add(effect);
        }

        if (hasKeyword(card, "Cycling") != -1) {
            final int n = hasKeyword(card, "Cycling");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String manacost = k[1];

                card.addSpellAbility(abilityCycle(card, manacost));
            }
        } // Cycling

        while (hasKeyword(card, "TypeCycling") != -1) {
            final int n = hasKeyword(card, "TypeCycling");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String type = k[1];
                final String manacost = k[2];

                card.addSpellAbility(abilityTypecycle(card, manacost, type));
            }
        } // TypeCycling

        if (hasKeyword(card, "Transmute") != -1) {
            final int n = hasKeyword(card, "Transmute");
            if (n != -1) {
                final String parse = card.getKeyword().get(n);
                card.removeIntrinsicKeyword(parse);
                final String manacost = parse.split(":")[1];

                card.addSpellAbility(abilityTransmute(card, manacost));
            }
        } // transmute

        int shiftPos = hasKeyword(card, "Soulshift");
        while (shiftPos != -1) {
            final int n = shiftPos;
            final String parse = card.getKeyword().get(n);
            final String[] k = parse.split(" ");
            final int manacost = Integer.parseInt(k[1]);

            final String actualTrigger = "Mode$ ChangesZone | Origin$ Battlefield | Destination$ Graveyard"
                    + "| OptionalDecider$ You | ValidCard$ Card.Self | Execute$ SoulshiftAbility"
                    + "| TriggerController$ TriggeredCardController | TriggerDescription$ " + parse
                    + " (When this creature dies, you may return target Spirit card with converted mana cost "
                    + manacost + " or less from your graveyard to your hand.)";
            final String abString = "DB$ ChangeZone | Origin$ Graveyard | Destination$ Hand"
                    + "| ValidTgts$ Spirit.YouOwn+cmcLE" + manacost;
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("SoulshiftAbility", abString);
            shiftPos = hasKeyword(card, "Soulshift", n + 1);
        } // Soulshift

        final int championPos = hasKeyword(card, "Champion");
        if (championPos != -1) {
            String parse = card.getKeyword().get(championPos);
            card.removeIntrinsicKeyword(parse);

            final String[] k = parse.split(":");
            final String[] valid = k[1].split(",");
            String desc = k.length > 2 ? k[2] : k[1];

            StringBuilder changeType = new StringBuilder();
            for (String v : valid) {
                if (changeType.length() != 0) {
                    changeType.append(",");
                }
                changeType.append(v).append(".YouCtrl+Other");
            }

            StringBuilder trig = new StringBuilder();
            trig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | ");
            trig.append("Execute$ ChampionAbility | TriggerDescription$ Champion a(n) ");
            trig.append(desc).append(" (When this enters the battlefield, sacrifice it unless you exile another ");
            trig.append(desc).append(" you control. When this leaves the battlefield, that card returns to the battlefield.)");

            StringBuilder trigReturn = new StringBuilder();
            trigReturn.append("Mode$ ChangesZone | Origin$ Battlefield | Destination$ Any | ValidCard$ Card.Self | ");
            trigReturn.append("Execute$ ChampionReturn | Secondary$ True | TriggerDescription$ When this leaves the battlefield, that card returns to the battlefield.");

            StringBuilder ab = new StringBuilder();
            ab.append("DB$ ChangeZone | Origin$ Battlefield | Destination$ Exile | RememberChanged$ True | Champion$ True | ");
            ab.append("Hidden$ True | Optional$ True | SubAbility$ DBSacrifice | ChangeType$ ").append(changeType);

            StringBuilder subAb = new StringBuilder();
            subAb.append("DB$ Sacrifice | Defined$ Card.Self | ConditionDefined$ Remembered | ConditionPresent$ Card | ConditionCompare$ EQ0");

            String returnChampion = "DB$ ChangeZone | Defined$ Remembered | Origin$ Exile | Destination$ Battlefield";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(trig.toString(), card, true);
            final Trigger parsedTrigReturn = TriggerHandler.parseTrigger(trigReturn.toString(), card, true);
            card.addTrigger(parsedTrigger);
            card.addTrigger(parsedTrigReturn);
            card.setSVar("ChampionAbility", ab.toString());
            card.setSVar("ChampionReturn", returnChampion);
            card.setSVar("DBSacrifice", subAb.toString());
        }
        
        if (card.hasKeyword("If CARDNAME would be put into a graveyard "
                + "from anywhere, reveal CARDNAME and shuffle it into its owner's library instead.")) {

            String replacement = "Event$ Moved | Destination$ Graveyard | ValidCard$ Card.Self | ReplaceWith$ GraveyardToLibrary";
            String ab =  "DB$ ChangeZone | Hidden$ True | Origin$ All | Destination$ Library | Defined$ ReplacedCard | Reveal$ True | Shuffle$ True";

            card.addReplacementEffect(ReplacementHandler.parseReplacement(replacement, card, true));
            card.setSVar("GraveyardToLibrary", ab);
        }

        final int echoPos = hasKeyword(card, "Echo");
        if (echoPos != -1) {
            // card.removeIntrinsicKeyword(parse);
            final String[] k = card.getKeyword().get(echoPos).split(":");
            final String manacost = k[1];

            card.setEchoCost(manacost);

            final Command intoPlay = new Command() {

                private static final long serialVersionUID = -7913835645603984242L;

                @Override
                public void run() {
                    card.addExtrinsicKeyword("(Echo unpaid)");
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        } // echo

        if (hasKeyword(card, "Suspend") != -1) {
            // Suspend:<TimeCounters>:<Cost>
            final int n = hasKeyword(card, "Suspend");
            if (n != -1) {
                final String parse = card.getKeyword().get(n);
                card.removeIntrinsicKeyword(parse);
                card.setSuspend(true);
                final String[] k = parse.split(":");

                final String timeCounters = k[1];
                final String cost = k[2];
                card.addSpellAbility(abilitySuspendStatic(card, cost, timeCounters));
                addSuspendUpkeepTrigger(card);
                addSuspendPlayTrigger(card);
            }
        } // Suspend

        if (hasKeyword(card, "Fading") != -1) {
            final int n = hasKeyword(card, "Fading");
            if (n != -1) {
                final String[] k = card.getKeyword().get(n).split(":");

                card.addIntrinsicKeyword("etbCounter:FADE:" + k[1] + ":no Condition:no desc");

                String upkeepTrig = "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | TriggerZones$ Battlefield " +
                        " | Execute$ TrigUpkeepFading | Secondary$ True | TriggerDescription$ At the beginning of " +
                        "your upkeep, remove a fade counter from CARDNAME. If you can't, sacrifice CARDNAME.";

                card.setSVar("TrigUpkeepFading", "AB$ RemoveCounter | Cost$ 0 | Defined$ Self | CounterType$ FADE" +
                		" | CounterNum$ 1 | RememberRemoved$ True | SubAbility$ DBUpkeepFadingSac");
                card.setSVar("DBUpkeepFadingSac","DB$ Sacrifice | SacValid$ Self | ConditionCheckSVar$ FadingCheckSVar" +
                		" | ConditionSVarCompare$ EQ0 | References$ FadingCheckSVar | SubAbility$ FadingCleanup");
                card.setSVar("FadingCleanup","DB$ Cleanup | ClearRemembered$ True");
                card.setSVar("FadingCheckSVar","Count$RememberedSize");
                final Trigger parsedUpkeepTrig = TriggerHandler.parseTrigger(upkeepTrig, card, true);
                card.addTrigger(parsedUpkeepTrig);
            }
        } // Fading

        if (hasKeyword(card, "Vanishing") != -1) {
            final int n = hasKeyword(card, "Vanishing");
            if (n != -1) {
                final String[] k = card.getKeyword().get(n).split(":");
                // etbcounter
                card.addIntrinsicKeyword("etbCounter:TIME:" + k[1] + ":no Condition:no desc");
                // Remove Time counter trigger
                String upkeepTrig = "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | " +
                		"TriggerZones$ Battlefield | IsPresent$ Card.Self+counters_GE1_TIME" +
                		" | Execute$ TrigUpkeepVanishing | TriggerDescription$ At the " +
                		"beginning of your upkeep, if CARDNAME has a time counter on it, " +
                		"remove a time counter from it. | Secondary$ True";
                card.setSVar("TrigUpkeepVanishing", "AB$ RemoveCounter | Cost$ 0 | Defined$ Self" +
                		" | CounterType$ TIME | CounterNum$ 1");
                final Trigger parsedUpkeepTrig = TriggerHandler.parseTrigger(upkeepTrig, card, true);
                card.addTrigger(parsedUpkeepTrig);
                // sacrifice trigger
                String sacTrig = "Mode$ CounterRemoved | TriggerZones$ Battlefield | ValidCard$" +
                		" Card.Self | NewCounterAmount$ 0 | Secondary$ True | CounterType$ TIME |" +
                		" Execute$ TrigVanishingSac | TriggerDescription$ When the last time " +
                		"counter is removed from CARDNAME, sacrifice it.";
                card.setSVar("TrigVanishingSac", "AB$ Sacrifice | Cost$ 0 | SacValid$ Self");

                final Trigger parsedSacTrigger = TriggerHandler.parseTrigger(sacTrig, card, true);
                card.addTrigger(parsedSacTrigger);
            }
        } // Vanishing

        // AddCost
        if (card.hasSVar("FullCost")) {
            final SpellAbility sa1 = card.getFirstSpellAbility();
            if (sa1 != null && sa1.isSpell()) {
                sa1.setPayCosts(new Cost(card.getSVar("FullCost"), sa1.isAbility()));
            }
        }

        // AltCost
        String altCost = card.getSVar("AltCost");
        if (StringUtils.isNotBlank(altCost)) {
            final SpellAbility sa1 = card.getFirstSpellAbility();
            if (sa1 != null && sa1.isSpell()) {
                card.addSpellAbility(makeAltCostAbility(card, altCost, sa1));
            }
        }

        if (card.hasKeyword("Delve")) {
            card.getSpellAbilities().get(0).setDelve(true);
        }

        if (card.hasStartOfKeyword("Haunt")) {
            setupHauntSpell(card);
        }

        if (card.hasKeyword("Provoke")) {
            final String actualTrigger = "Mode$ Attacks | ValidCard$ Card.Self | "
                    + "OptionalDecider$ You | Execute$ ProvokeAbility | Secondary$ True | TriggerDescription$ "
                    + "When this attacks, you may have target creature defending player "
                    + "controls untap and block it if able.";
            final String abString = "DB$ MustBlock | ValidTgts$ Creature.DefenderCtrl | "
                    + "TgtPrompt$ Select target creature defending player controls | SubAbility$ DBUntap";
            final String dbString = "DB$ Untap | Defined$ Targeted";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("ProvokeAbility", abString);
            card.setSVar("DBUntap", dbString);
        }

        if (card.hasKeyword("Living Weapon")) {
            card.removeIntrinsicKeyword("Living Weapon");

            final StringBuilder sbTrig = new StringBuilder();
            sbTrig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ");
            sbTrig.append("ValidCard$ Card.Self | Execute$ TrigGerm | TriggerDescription$ ");
            sbTrig.append("Living Weapon (When this Equipment enters the battlefield, ");
            sbTrig.append("put a 0/0 black Germ creature token onto the battlefield, then attach this to it.)");

            final StringBuilder sbGerm = new StringBuilder();
            sbGerm.append("DB$ Token | TokenAmount$ 1 | TokenName$ Germ | TokenTypes$ Creature,Germ | RememberTokens$ True | ");
            sbGerm.append("TokenOwner$ You | TokenColors$ Black | TokenPower$ 0 | TokenToughness$ 0 | TokenImage$ B 0 0 Germ | SubAbility$ DBGermAttach");

            final StringBuilder sbAttach = new StringBuilder();
            sbAttach.append("DB$ Attach | Defined$ Remembered | SubAbility$ DBGermClear");

            final StringBuilder sbClear = new StringBuilder();
            sbClear.append("DB$ Cleanup | ClearRemembered$ True");

            card.setSVar("TrigGerm", sbGerm.toString());
            card.setSVar("DBGermAttach", sbAttach.toString());
            card.setSVar("DBGermClear", sbClear.toString());

            final Trigger etbTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), card, true);
            card.addTrigger(etbTrigger);
        }
        
        if (card.hasKeyword("Epic")) {
            makeEpic(card);
        }

        if (card.hasKeyword("Soulbond")) {
            // Setup ETB trigger for card with Soulbond keyword
            final String actualTriggerSelf = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + "ValidCard$ Card.Self | Execute$ TrigBondOther | OptionalDecider$ You | "
                    + "IsPresent$ Creature.Other+YouCtrl+NotPaired | Secondary$ True | "
                    + "TriggerDescription$ When CARDNAME enters the battlefield, "
                    + "you may pair CARDNAME with another unpaired creature you control";
            final String abStringSelf = "AB$ Bond | Cost$ 0 | Defined$ Self | ValidCards$ Creature.Other+YouCtrl+NotPaired";
            final Trigger parsedTriggerSelf = TriggerHandler.parseTrigger(actualTriggerSelf, card, true);
            card.addTrigger(parsedTriggerSelf);
            card.setSVar("TrigBondOther", abStringSelf);
            // Setup ETB trigger for other creatures you control
            final String actualTriggerOther = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + "ValidCard$ Creature.Other+YouCtrl | TriggerZones$ Battlefield | OptionalDecider$ You | "
                    + "Execute$ TrigBondSelf | IsPresent$ Creature.Self+NotPaired | Secondary$ True | "
                    + " TriggerDescription$ When another unpaired creature you control enters the battlefield, "
                    + "you may pair it with CARDNAME";
            final String abStringOther = "AB$ Bond | Cost$ 0 | Defined$ TriggeredCard | ValidCards$ Creature.Self+NotPaired";
            final Trigger parsedTriggerOther = TriggerHandler.parseTrigger(actualTriggerOther, card, true);
            card.addTrigger(parsedTriggerOther);
            card.setSVar("TrigBondSelf", abStringOther);
        }

        if (card.hasKeyword("Extort")) {
            final String extortTrigger = "Mode$ SpellCast | ValidCard$ Card | ValidActivatingPlayer$ You | "
                    + "TriggerZones$ Battlefield | Execute$ ExtortOpps | Secondary$ True"
                    + " | TriggerDescription$ Extort (Whenever you cast a spell, you may pay W/B. If you do, "
                    + "each opponent loses 1 life and you gain that much life.)";
            final String abString = "AB$ LoseLife | Cost$ WB | Defined$ Player.Opponent | "
                    + "LifeAmount$ 1 | SubAbility$ ExtortGainLife";
            final String dbString = "DB$ GainLife | Defined$ You | LifeAmount$ AFLifeLost | References$ AFLifeLost";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(extortTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("ExtortOpps", abString);
            card.setSVar("ExtortGainLife", dbString);
            card.setSVar("AFLifeLost", "Number$0");
        }

        if (card.hasKeyword("Evolve")) {
            final String evolveTrigger = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + " ValidCard$ Creature.YouCtrl+Other | EvolveCondition$ True | "
                    + "TriggerZones$ Battlefield | Execute$ EvolveAddCounter | Secondary$ True | "
                    + "TriggerDescription$ Evolve (Whenever a creature enters the battlefield under your "
                    + "control, if that creature has greater power or toughness than this creature, put a "
                    + "+1/+1 counter on this creature.)";
            final String abString = "AB$ PutCounter | Cost$ 0 | Defined$ Self | CounterType$ P1P1 | "
                    + "CounterNum$ 1 | Evolve$ True";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(evolveTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("EvolveAddCounter", abString);
        }

        if (card.hasStartOfKeyword("Amplify")) {
            // find position of Amplify keyword
            final int equipPos = card.getKeywordPosition("Amplify");
            final String[] ampString = card.getKeyword().get(equipPos).split(":");
            final String amplifyMagnitude = ampString[1];
            final String suffix = !amplifyMagnitude.equals("1") ? "s" : "";
            final String ampTypes = ampString[2];
            String[] refinedTypes = ampTypes.split(",");
            final StringBuilder types = new StringBuilder();
            for (int i = 0; i < refinedTypes.length; i++) {
                types.append("Card.").append(refinedTypes[i]).append("+YouCtrl");
                if (i + 1 != refinedTypes.length) {
                    types.append(",");
                }
            }
            // Setup ETB trigger for card with Amplify keyword
            final String actualTrigger = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + "ValidCard$ Card.Self | Execute$ AmplifyReveal | Static$ True | Secondary$ True | "
                    + "TriggerDescription$ As this creature enters the battlefield, put "
                    + amplifyMagnitude + " +1/+1 counter" + suffix + " on it for each "
                    + ampTypes.replace(",", " and/or ") + " card you reveal in your hand.)";
            final String abString = "AB$ Reveal | Cost$ 0 | AnyNumber$ True | RevealValid$ "
                    + types.toString() + " | RememberRevealed$ True | SubAbility$ Amplify";
            final String dbString = "DB$ PutCounter | Defined$ Self | CounterType$ P1P1 | "
                    + "CounterNum$ AmpMagnitude | References$ Revealed,AmpMagnitude | SubAbility$ DBCleanup";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("AmplifyReveal", abString);
            card.setSVar("Amplify", dbString);
            card.setSVar("DBCleanup", "DB$ Cleanup | ClearRemembered$ True");
            card.setSVar("AmpMagnitude", "SVar$Revealed/Times." + amplifyMagnitude);
            card.setSVar("Revealed", "Remembered$Amount");
        }

        if (card.hasStartOfKeyword("Equip")) {
            // find position of Equip keyword
            final int equipPos = card.getKeywordPosition("Equip");
            // Check for additional params such as preferred AI targets
            final String equipString = card.getKeyword().get(equipPos).substring(5);
            final String[] equipExtras = equipString.contains("\\|") ? equipString.split("\\|", 2) : null;
            // Get cost string
            String equipCost = "";
            if (equipExtras != null) {
                equipCost = equipExtras[0].trim();
            } else {
                equipCost = equipString.trim();
            }
           // Create attach ability string
            final StringBuilder abilityStr = new StringBuilder();
            abilityStr.append("AB$ Attach | Cost$ ");
            abilityStr.append(equipCost);
            abilityStr.append(" | ValidTgts$ Creature.YouCtrl | TgtPrompt$ Select target creature you control ");
            abilityStr.append("| SorcerySpeed$ True | Equip$ True | AILogic$ Pump | IsPresent$ Card.Self+nonCreature ");
            if (equipExtras != null) {
                abilityStr.append("| ").append(equipExtras[1]).append(" ");
            }
            if (equipCost.matches(".+<.+>")) { //Something other than a mana cost
                abilityStr.append("| PrecostDesc$ Equip - | SpellDescription$ (Attach to target creature you control. Equip only as a sorcery.)");
            }
            else {
                abilityStr.append("| PrecostDesc$ Equip | SpellDescription$ (Attach to target creature you control. Equip only as a sorcery.)");
            }
            // instantiate attach ability
            final SpellAbility sa = AbilityFactory.getAbility(abilityStr.toString(), card);
            card.addSpellAbility(sa);
            // add ability to instrinic strings so copies/clones create the ability also
            card.getUnparsedAbilities().add(abilityStr.toString());
        }

        if (card.hasStartOfKeyword("Fortify")) {
            final int equipPos = card.getKeywordPosition("Fortify");
            final String equipString = card.getKeyword().get(equipPos).substring(7);
            final String[] equipExtras = equipString.contains("\\|") ? equipString.split("\\|", 2) : null;
            // Get cost string
            String equipCost = "";
            if (equipExtras != null) {
                equipCost = equipExtras[0].trim();
            } else {
                equipCost = equipString.trim();
            }
           // Create attach ability string
            final StringBuilder abilityStr = new StringBuilder();
            abilityStr.append("AB$ Attach | Cost$ ");
            abilityStr.append(equipCost);
            abilityStr.append(" | ValidTgts$ Land.YouCtrl | TgtPrompt$ Select target land you control ");
            abilityStr.append("| SorcerySpeed$ True | AILogic$ Pump | IsPresent$ Card.Self+nonCreature ");
            if (equipExtras != null) {
                abilityStr.append("| ").append(equipExtras[1]).append(" ");
            }
            abilityStr.append("| PrecostDesc$ Fortify | SpellDescription$ (Attach to target land you control. Fortify only as a sorcery.)");
 
            // instantiate attach ability
            final SpellAbility sa = AbilityFactory.getAbility(abilityStr.toString(), card);
            card.addSpellAbility(sa);
            // add ability to instrinic strings so copies/clones create the ability also
            card.getUnparsedAbilities().add(abilityStr.toString());
        }

        if (card.hasStartOfKeyword("Bestow")) {
            final int bestowPos = card.getKeywordPosition("Bestow");
            final String cost = card.getKeyword().get(bestowPos).split(":")[1];
            card.removeIntrinsicKeyword(card.getKeyword().get(bestowPos));       

            final StringBuilder sbAttach = new StringBuilder();
            sbAttach.append("SP$ Attach | Cost$ ");
            sbAttach.append(cost);
            sbAttach.append(" | AILogic$ Pump | Bestow$ True | ValidTgts$ Creature");
            final SpellAbility bestow = AbilityFactory.getAbility(sbAttach.toString(), card);

            bestow.setDescription("Bestow " + cost + " (If you cast this card for" +
                  " its bestow cost, it's an Aura spell with enchant creature. It" +
                  " becomes a creature again if it's not attached to a creature.)");
            bestow.setStackDescription("Bestow - " + card.getName());
            bestow.setBasicSpell(false);
            card.addSpellAbility(bestow);
            card.getUnparsedAbilities().add(sbAttach.toString());

            // Second, a static trigger when bestow cost is paid to animate Aura
            final StringBuilder sbTrigger = new StringBuilder();
            sbTrigger.append("Mode$ SpellCast | ValidCard$ Card.Self | Bestow$ True | ");
            sbTrigger.append("Execute$ BestowAnimateSelf | Static$ True");

            final StringBuilder sbSVar = new StringBuilder();
            sbSVar.append("AB$ Animate | Cost$ 0 | Defined$ Self | OverwriteTypes$ True");
            sbSVar.append(" | Types$ Enchantment,Aura | Keywords$ Enchant creature | Permanent$ True");
            card.setSVar("BestowAnimateSelf", sbSVar.toString());
            final Trigger animateSelf = TriggerHandler.parseTrigger(sbTrigger.toString(), card, true);
            card.addTrigger(animateSelf);
        }

        setupEtbKeywords(card);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     */
    private static void setupEtbKeywords(final Card card) {
        for (String kw : card.getKeyword()) {

            if (kw.startsWith("ETBReplacement")) {
                String[] splitkw = kw.split(":");
                ReplacementLayer layer = ReplacementLayer.smartValueOf(splitkw[1]);
                SpellAbility repAb = AbilityFactory.getAbility(card.getSVar(splitkw[2]), card);
                String desc = repAb.getDescription();
                setupETBReplacementAbility(repAb);

                final String valid = splitkw.length >= 6 ? splitkw[5] : "Card.Self";

                StringBuilder repEffsb = new StringBuilder();
                repEffsb.append("Event$ Moved | ValidCard$ ").append(valid);
                repEffsb.append(" | Destination$ Battlefield | Description$ ").append(desc);
                if (splitkw.length >= 4) {
                    if (splitkw[3].contains("Optional")) {
                        repEffsb.append(" | Optional$ True");
                    }
                }
                if (splitkw.length >= 5) {
                    if (!splitkw[4].isEmpty()) {
                        repEffsb.append(" | ActiveZones$ " + splitkw[4]);
                    }
                }

                ReplacementEffect re = ReplacementHandler.parseReplacement(repEffsb.toString(), card, true);
                re.setLayer(layer);
                re.setOverridingAbility(repAb);

                card.addReplacementEffect(re);
            } else if (kw.startsWith("etbCounter")) {
                String parse = kw;
                card.removeIntrinsicKeyword(parse);

                String[] splitkw = parse.split(":");

                String desc = "CARDNAME enters the battlefield with " + splitkw[2] + " "
                        + CounterType.valueOf(splitkw[1]).getName() + " counters on it.";
                String extraparams = "";
                String amount = splitkw[2];
                if (splitkw.length > 3) {
                    if (!splitkw[3].equals("no Condition")) {
                        extraparams = splitkw[3];
                    }
                }
                if (splitkw.length > 4) {
                    desc = !splitkw[4].equals("no desc") ? splitkw[4] : "";
                }
                String abStr = "AB$ ChangeZone | Cost$ 0 | Hidden$ True | Origin$ All | Destination$ Battlefield"
                        + "| Defined$ ReplacedCard | SubAbility$ ETBCounterDBSVar";
                String dbStr = "DB$ PutCounter | Defined$ Self | CounterType$ " + splitkw[1] + " | CounterNum$ " + amount;
                try {
                    Integer.parseInt(amount);
                }
                catch (NumberFormatException ignored) {
                    dbStr += " | References$ " + amount;
                }
                card.setSVar("ETBCounterSVar", abStr);
                card.setSVar("ETBCounterDBSVar", dbStr);

                String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield "
                        + "| ReplaceWith$ ETBCounterSVar | Description$ " + desc + (!extraparams.equals("") ? " | " + extraparams : "");

                ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, card, true);
                re.setLayer(ReplacementLayer.Other);

                card.addReplacementEffect(re);
            } else if (kw.equals("CARDNAME enters the battlefield tapped.")) {
                String parse = kw;
                card.removeIntrinsicKeyword(parse);

                String abStr = "AB$ Tap | Cost$ 0 | Defined$ Self | ETB$ True | SubAbility$ MoveETB";
                String dbStr = "DB$ ChangeZone | Hidden$ True | Origin$ All | Destination$ Battlefield"
                        + "| Defined$ ReplacedCard";

                card.setSVar("ETBTappedSVar", abStr);
                card.setSVar("MoveETB", dbStr);

                String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield "
                        + "| ReplaceWith$ ETBTappedSVar | Description$ CARDNAME enters the battlefield tapped.";

                ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, card, true);
                re.setLayer(ReplacementLayer.Other);

                card.addReplacementEffect(re);
            }
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @return
     */
    private static void makeEpic(final Card card) {

        // Add the Epic effect as a subAbility
        String dbStr = "DB$ Effect | Triggers$ EpicTrigger | SVars$ EpicCopy | StaticAbilities$ EpicCantBeCast | Duration$ Permanent | Unique$ True";
        
        final AbilitySub newSA = (AbilitySub) AbilityFactory.getAbility(dbStr.toString(), card);
        
        card.setSVar("EpicCantBeCast", "Mode$ CantBeCast | ValidCard$ Card | Caster$ You | EffectZone$ Command | Description$ For the rest of the game, you can't cast spells.");
        card.setSVar("EpicTrigger", "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | Execute$ EpicCopy | TriggerDescription$ "
                + "At the beginning of each of your upkeeps, copy " + card.toString() + " except for its epic ability.");
        card.setSVar("EpicCopy", "DB$ CopySpellAbility | Defined$ EffectSource");
        
        final SpellAbility origSA = card.getSpellAbilities().get(0);
        
        SpellAbility child = origSA;
        while (child.getSubAbility() != null) {
            child = child.getSubAbility();
        }
        child.setSubAbility(newSA);
        newSA.setParent(child);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     */
    private static void setupHauntSpell(final Card card) {
        final int hauntPos = card.getKeywordPosition("Haunt");
        final String[] splitKeyword = card.getKeyword().get(hauntPos).split(":");
        final String hauntSVarName = splitKeyword[1];
        final String abilityDescription = splitKeyword[2];
        final String hauntAbilityDescription = abilityDescription.substring(0, 1).toLowerCase()
                + abilityDescription.substring(1);
        String hauntDescription;
        if (card.isCreature()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("When ").append(card.getName());
            sb.append(" enters the battlefield or the creature it haunts dies, ");
            sb.append(hauntAbilityDescription);
            hauntDescription = sb.toString();
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append("When the creature ").append(card.getName());
            sb.append(" haunts dies, ").append(hauntAbilityDescription);
            hauntDescription = sb.toString();
        }

        card.getKeyword().remove(hauntPos);

        // First, create trigger that runs when the haunter goes to the
        // graveyard
        final StringBuilder sbHaunter = new StringBuilder();
        sbHaunter.append("Mode$ ChangesZone | Origin$ Battlefield | ");
        sbHaunter.append("Destination$ Graveyard | ValidCard$ Card.Self | ");
        sbHaunter.append("Static$ True | Secondary$ True | TriggerDescription$ Blank");

        final Trigger haunterDies = TriggerHandler.parseTrigger(sbHaunter.toString(), card, true);

        final Ability haunterDiesWork = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                this.getTargets().getFirstTargetedCard().addHauntedBy(card);
                card.getGame().getAction().exile(card);
            }
        };
        haunterDiesWork.setDescription(hauntDescription);
        haunterDiesWork.setTargetRestrictions(new TargetRestrictions(null, new String[]{"Creature"}, "1", "1")); // not null to make stack preserve targets set

        final Ability haunterDiesSetup = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                final Game game = card.getGame();
                this.setActivatingPlayer(card.getController());
                haunterDiesWork.setActivatingPlayer(card.getController());
                List<Card> allCreatures = CardLists.filter(game.getCardsIn(ZoneType.Battlefield), Presets.CREATURES);
                final List<Card> creats = CardLists.getTargetableCards(allCreatures, haunterDiesWork);
                if (creats.isEmpty()) {
                    return;
                }

                final Card toHaunt; 
                if (card.getController().isHuman()) {
                    final InputSelectCards target = new InputSelectCardsFromList(1, 1, creats);
                    target.setMessage("Choose target creature to haunt.");
                    Singletons.getControl().getInputQueue().setInputAndWait(target);
                    toHaunt = target.getSelected().get(0);
                } else {
                    // AI choosing what to haunt
                    final List<Card> oppCreats = CardLists.filterControlledBy(creats, card.getController().getOpponent());
                    List<Card> chooseFrom = oppCreats.isEmpty() ? creats : oppCreats;
                    toHaunt = ComputerUtilCard.getWorstCreatureAI(chooseFrom);
                }
                haunterDiesWork.setTargetCard(toHaunt);
                haunterDiesWork.setActivatingPlayer(card.getController());
                game.getStack().add(haunterDiesWork);
            }
        };

        haunterDies.setOverridingAbility(haunterDiesSetup);

        // Second, create the trigger that runs when the haunted creature dies
        final StringBuilder sbDies = new StringBuilder();
        sbDies.append("Mode$ ChangesZone | Origin$ Battlefield | Destination$ Graveyard | ");
        sbDies.append("ValidCard$ Creature.HauntedBy | Execute$ ").append(hauntSVarName);
        sbDies.append(" | TriggerDescription$ ").append(hauntDescription);

        final Trigger hauntedDies = forge.card.trigger.TriggerHandler.parseTrigger(sbDies.toString(), card, true);

        // Third, create the trigger that runs when the haunting creature
        // enters the battlefield
        final StringBuilder sbETB = new StringBuilder();
        sbETB.append("Mode$ ChangesZone | Destination$ Battlefield | ValidCard$ Card.Self | Execute$ ");
        sbETB.append(hauntSVarName).append(" | Secondary$ True | TriggerDescription$ ");
        sbETB.append(hauntDescription);

        final Trigger haunterETB = forge.card.trigger.TriggerHandler.parseTrigger(sbETB.toString(), card, true);

        // Fourth, create a trigger that removes the haunting status if the
        // haunter leaves the exile
        final StringBuilder sbUnExiled = new StringBuilder();
        sbUnExiled.append("Mode$ ChangesZone | Origin$ Exile | Destination$ Any | ");
        sbUnExiled.append("ValidCard$ Card.Self | Static$ True | Secondary$ True | ");
        sbUnExiled.append("TriggerDescription$ Blank");

        final Trigger haunterUnExiled = forge.card.trigger.TriggerHandler.parseTrigger(sbUnExiled.toString(), card,
                true);

        final Ability haunterUnExiledWork = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                if (card.getHaunting() != null) {
                    card.getHaunting().removeHauntedBy(card);
                    card.setHaunting(null);
                }
            }
        };

        haunterUnExiled.setOverridingAbility(haunterUnExiledWork);

        // Fifth, add all triggers and abilities to the card.
        if (card.isCreature()) {
            card.addTrigger(haunterETB);
            card.addTrigger(haunterDies);
        } else {
            final String abString = card.getSVar(hauntSVarName).replace("AB$", "SP$")
                    .replace("Cost$ 0", "Cost$ " + card.getManaCost())
                    + " | SpellDescription$ " + abilityDescription;

            final SpellAbility sa = AbilityFactory.getAbility(abString, card);
            card.addSpellAbility(sa);
        }

        card.addTrigger(hauntedDies);
        card.addTrigger(haunterUnExiled);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @param abilities
     * @return 
     */
    private static SpellAbility makeAltCostAbility(final Card card, final String altCost, final SpellAbility sa) {
        final Map<String, String> params = AbilityFactory.getMapParams(altCost);

        final SpellAbility altCostSA = sa.copy();
        final Cost abCost = new Cost(params.get("Cost"), altCostSA.isAbility());
        altCostSA.setPayCosts(abCost);
        altCostSA.setBasicSpell(false);
        altCostSA.addOptionalCost(OptionalCost.AltCost);

        final SpellAbilityRestriction restriction = new SpellAbilityRestriction();
        restriction.setRestrictions(params);
        if (!params.containsKey("ActivationZone")) {
            restriction.setZone(ZoneType.Hand);
        }
        altCostSA.setRestrictions(restriction);

        final String costDescription = params.containsKey("Description") ? params.get("Description") 
                : String.format("You may %s rather than pay %s's mana cost.", abCost.toStringAlt(), card.getName());
        
        altCostSA.setDescription(costDescription);
        return altCostSA;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @param evokeKeyword
     * @return
     */
    private static SpellAbility makeEvokeSpell(final Card card, final String evokeKeyword) {
        final String[] k = evokeKeyword.split(":");
        final Cost evokedCost = new Cost(k[1], false);
        
        final SpellAbility evokedSpell = new Spell(card, evokedCost) {
            private static final long serialVersionUID = -1598664196463358630L;

            @Override
            public void resolve() {
                final Game game = card.getGame();
                card.setEvoked(true);
                game.getAction().moveToPlay(card);
            }

            @Override
            public boolean canPlayAI() {
                final Game game = card.getGame();
                final Player ai = getActivatingPlayer();
                if (!SpellPermanent.checkETBEffects(card, this.getActivatingPlayer())) {
                    return false;
                }
                // Wait for Main2 if possible
                if (game.getPhaseHandler().is(PhaseType.MAIN1)
                        && game.getPhaseHandler().isPlayerTurn(ai)
                        && ai.getManaPool().totalMana() <= 0
                        && !ComputerUtil.castPermanentInMain1(ai, this)) {
                    return false;
                }

                return super.canPlayAI();
            }
        };
        card.removeIntrinsicKeyword(evokeKeyword);
        final StringBuilder desc = new StringBuilder();
        desc.append("Evoke ").append(evokedCost.toSimpleString());
        desc.append(" (You may cast this spell for its evoke cost. ");
        desc.append("If you do, when it enters the battlefield, sacrifice it.)");

        evokedSpell.setDescription(desc.toString());

        final StringBuilder sb = new StringBuilder();
        sb.append(card.getName()).append(" (Evoked)");
        evokedSpell.setStackDescription(sb.toString());
        evokedSpell.setBasicSpell(false);
        return evokedSpell;
    }

    private static final Map<String,String> emptyMap = new TreeMap<String,String>();
    public static void setupETBReplacementAbility(SpellAbility sa) {
        sa.appendSubAbility(new AbilitySub(ApiType.InternalEtbReplacement, sa.getSourceCard(), null, emptyMap));
        // ETBReplacementMove(sa.getSourceCard(), null));
    }

    /**
     * <p>
     * hasKeyword.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param k
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static final int hasKeyword(final Card c, final String k) {
        return hasKeyword(c, k, 0);
    }

    /**
     * <p>
     * hasKeyword.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param k
     *            a {@link java.lang.String} object.
     * @param startPos
     *            a int.
     * @return a int.
     */
    private static final int hasKeyword(final Card c, final String k, final int startPos) {
        final List<String> a = c.getKeyword();
        for (int i = startPos; i < a.size(); i++) {
            if (a.get(i).startsWith(k)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * <p>
     * parseKeywords.
     * </p>
     * Pulling out the parsing of keywords so it can be used by the token
     * generator
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param cardName
     *            a {@link java.lang.String} object.
     * 
     */
    public static final void parseKeywords(final Card card, final String cardName) {
        if (card.hasKeyword("CARDNAME enters the battlefield tapped unless you control two or fewer other lands.")) {
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 6436821515525468682L;

                @Override
                public void run() {
                    final List<Card> lands = card.getController().getLandsInPlay();
                    lands.remove(card);
                    if (!(lands.size() <= 2)) {
                        // it enters the battlefield this way, and should not
                        // fire triggers
                        card.setTapped(true);
                    }
                }
            });
        }
        if (hasKeyword(card, "CARDNAME enters the battlefield tapped unless you control a") != -1) {
            final int n = hasKeyword(card,
                    "CARDNAME enters the battlefield tapped unless you control a");
            final String parse = card.getKeyword().get(n).toString();

            String splitString;
            if (parse.contains(" or a ")) {
                splitString = " or a ";
            } else {
                splitString = " or an ";
            }

            final String[] types = parse.substring(60, parse.length() - 1).split(splitString);

            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 403635232455049834L;

                @Override
                public void run() {
                    final List<Card> clICtrl = card.getOwner().getCardsIn(ZoneType.Battlefield);

                    boolean fnd = false;

                    for (int i = 0; i < clICtrl.size(); i++) {
                        final Card c = clICtrl.get(i);
                        for (final String type : types) {
                            if (c.isType(type.trim())) {
                                fnd = true;
                            }
                        }
                    }

                    if (!fnd) {
                        // it enters the battlefield this way, and should not
                        // fire triggers
                        card.setTapped(true);
                    }
                }
            });
        }
        if (hasKeyword(card, "Sunburst") != -1) {
            final Command sunburstCIP = new Command() {
                private static final long serialVersionUID = 1489845860231758299L;

                @Override
                public void run() {
                    if (card.isCreature()) {
                        card.addCounter(CounterType.P1P1, card.getSunburstValue(), true);
                    } else {
                        card.addCounter(CounterType.CHARGE, card.getSunburstValue(), true);
                    }

                }
            };

            final Command sunburstLP = new Command() {
                private static final long serialVersionUID = -7564420917490677427L;

                @Override
                public void run() {
                    card.setSunburstValue(0);
                }
            };

            card.addComesIntoPlayCommand(sunburstCIP);
            card.addLeavesPlayCommand(sunburstLP);
        }

        // Enforce the "World rule"
        if (card.isType("World")) {
            final Command intoPlay = new Command() {
                private static final long serialVersionUID = 6536398032388958127L;

                @Override
                public void run() {
                    final Game game = card.getGame();
                    final List<Card> cardsInPlay = CardLists.getType(game.getCardsIn(ZoneType.Battlefield), "World");
                    cardsInPlay.remove(card);
                    for (int i = 0; i < cardsInPlay.size(); i++) {
                        game.getAction().sacrificeDestroy(cardsInPlay.get(i));
                    }
                } // execute()
            }; // Command
            card.addComesIntoPlayCommand(intoPlay);
        }

        if (hasKeyword(card, "Morph") != -1) {
            final int n = hasKeyword(card, "Morph");
            if (n != -1) {

                final String parse = card.getKeyword().get(n).toString();
                Map<String, String> sVars = card.getSVars();

                final String[] k = parse.split(":");
                final Cost cost = new Cost(k[1], true);

                card.addSpellAbility(abilityMorphDown(card));

                card.turnFaceDown();

                card.addSpellAbility(abilityMorphUp(card, cost));
                card.setSVars(sVars); // for Warbreak Trumpeter.

                card.setState(CardCharacteristicName.Original);
            }
        } // Morph

        if (hasKeyword(card, "MayDiscardFromHand") != -1) {
            card.addSpellAbility(abilityDiscardSource(card)); // Circling Vultures
        }

        if (hasKeyword(card, "Unearth") != -1) {
            final int n = hasKeyword(card, "Unearth");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");

                final String manacost = k[1];

                card.addSpellAbility(abilityUnearth(card, manacost));
            }
        } // unearth

        if (hasKeyword(card, "Madness") != -1) {
            final int n = hasKeyword(card, "Madness");
            if (n != -1) {
                // Set Madness Replacement effects
                String repeffstr = "Event$ Discard | ActiveZones$ Hand | ValidCard$ Card.Self | " +
                		"ReplaceWith$ DiscardMadness | Secondary$ True | Description$ If you would" +
                		" discard this card, you discard it, but may exile it instead of putting it" +
                		" into your graveyard";
                ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, card, true);
                card.addReplacementEffect(re);
                String sVarMadness = "DB$ Discard | Defined$ ReplacedPlayer" +
                		" | Mode$ Defined | DefinedCards$ ReplacedCard | Madness$ True";
                card.setSVar("DiscardMadness", sVarMadness);

                // Set Madness Triggers
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);
                final String[] k = parse.split(":");
                String trigStr = "Mode$ Discarded | ValidCard$ Card.Self | IsMadness$ True | " +
                		"Execute$ TrigPlayMadness | Secondary$ True | TriggerDescription$ " +
                		"Play Madness - " + card.getName();
                final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, card, true);
                card.addTrigger(myTrigger);
                String playMadness = "AB$ Play | Cost$ 0 | Defined$ Self | PlayMadness$ " + k[1] + 
                		" | Optional$ True | SubAbility$ DBWasNotPlayMadness | RememberPlayed$ True";
                String moveToYard = "DB$ ChangeZone | Defined$ Self | Origin$ Exile | " +
                		"Destination$ Graveyard | ConditionDefined$ Remembered | ConditionPresent$" +
                		" Card | ConditionCompare$ EQ0 | SubAbility$ DBMadnessCleanup";
                String cleanUp = "DB$ Cleanup | ClearRemembered$ True";
                card.setSVar("TrigPlayMadness", playMadness);
                card.setSVar("DBWasNotPlayMadness", moveToYard);
                card.setSVar("DBMadnessCleanup", cleanUp);
            }
        } // madness

        if (hasKeyword(card, "Miracle") != -1) {
            final int n = hasKeyword(card, "Miracle");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                card.setMiracleCost(new Cost(k[1], false));
            }
        } // miracle

        if (hasKeyword(card, "Devour") != -1) {
            final int n = hasKeyword(card, "Devour");
            if (n != -1) {

                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String magnitude = k[1];

                String abStr = "AB$ ChangeZone | Cost$ 0 | Hidden$ True | Origin$ All | "
                        + "Destination$ Battlefield | Defined$ ReplacedCard | SubAbility$ DevourSac";
                String dbStr = "DB$ Sacrifice | Defined$ You | Amount$ DevourSacX | "
                        + "References$ DevourSacX | SacValid$ Creature.Other | SacMessage$ creature (Devour "
                        + magnitude + ") | RememberSacrificed$ True | Optional$ True | "
                        + "Devour$ True | SubAbility$ DevourCounters";
                String counterStr = "DB$ PutCounter | Defined$ Self | CounterType$ P1P1 | CounterNum$ DevourX"
                        + " | References$ DevourX,DevourSize | SubAbility$ DevourCleanup";

                card.setSVar("DevourETB", abStr);
                card.setSVar("DevourSac", dbStr);
                card.setSVar("DevourSacX", "Count$Valid Creature.YouCtrl+Other");
                card.setSVar("DevourCounters", counterStr);
                card.setSVar("DevourX", "SVar$DevourSize/Times." + magnitude);
                card.setSVar("DevourSize", "Count$RememberedSize");
                card.setSVar("DevourCleanup", "DB$ Cleanup | ClearRemembered$ True");

                String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield | ReplaceWith$ DevourETB";

                ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, card, true);
                re.setLayer(ReplacementLayer.Other);
                card.addReplacementEffect(re);
            }
        } // Devour

        if (hasKeyword(card, "Modular") != -1) {
            final int n = hasKeyword(card, "Modular");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.getKeyword().remove(parse);

                final int m = Integer.parseInt(parse.substring(8));

                card.addIntrinsicKeyword("etbCounter:P1P1:" + m + ":no Condition: " +
                        "Modular " + m + " (This enters the battlefield with " + m + " +1/+1 counters on it. When it's put into a graveyard, " +
                        "you may put its +1/+1 counters on target artifact creature.)");

                final String abStr = "AB$ PutCounter | Cost$ 0 | References$ ModularX | ValidTgts$ Artifact.Creature | " +
                        "TgtPrompt$ Select target artifact creature | CounterType$ P1P1 | CounterNum$ ModularX";
                card.setSVar("ModularTrig", abStr);
                card.setSVar("ModularX", "TriggeredCard$CardCounters.P1P1");
                
                String trigStr = "Mode$ ChangesZone | ValidCard$ Card.Self | Origin$ Battlefield | Destination$ Graveyard" +
                        " | OptionalDecider$ TriggeredCardController | TriggerController$ TriggeredCardController | Execute$ ModularTrig | " +
                        "Secondary$ True | TriggerDescription$ When CARDNAME is put into a graveyard from the battlefield, " +
                        "you may put a +1/+1 counter on target artifact creature for each +1/+1 counter on CARDNAME";
                final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, card, true);
                card.addTrigger(myTrigger);
            }
        } // Modular

        /*
         * WARNING: must keep this keyword processing before etbCounter keyword
         * processing.
         */
        final int graft = hasKeyword(card, "Graft");
        if (graft != -1) {
            final String parse = card.getKeyword().get(graft).toString();

            final int m = Integer.parseInt(parse.substring(6));
            final String abStr = "AB$ MoveCounter | Cost$ 0 | Source$ Self | "
                    + "Defined$ TriggeredCard | CounterType$ P1P1 | CounterNum$ 1";
            card.setSVar("GraftTrig", abStr);

            String trigStr = "Mode$ ChangesZone | ValidCard$ Creature.Other | "
                + "Origin$ Any | Destination$ Battlefield"
                + " | TriggerZones$ Battlefield | OptionalDecider$ You | "
                + "IsPresent$ Card.Self+counters_GE1_P1P1 | "
                + "Execute$ GraftTrig | TriggerDescription$ "
                + "Whenever another creature enters the battlefield, you "
                + "may move a +1/+1 counter from this creature onto it.";
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, card, true);
            card.addTrigger(myTrigger);

            card.addIntrinsicKeyword("etbCounter:P1P1:" + m);
        }

        final int bloodthirst = hasKeyword(card, "Bloodthirst");
        if (bloodthirst != -1) {
            final String numCounters = card.getKeyword().get(bloodthirst).split(" ")[1];
            String desc = "Bloodthirst "
                    + numCounters + " (If an opponent was dealt damage this turn, this creature enters the battlefield with "
                    + numCounters + " +1/+1 counters on it.)";
            if (numCounters.equals("X")) {
                desc = "Bloodthirst X (This creature enters the battlefield with X +1/+1 counters on it, "
                        + "where X is the damage dealt to your opponents this turn.)";
                card.setSVar("X", "Count$BloodthirstAmount");
            }

            card.addIntrinsicKeyword("etbCounter:P1P1:" + numCounters + ":Bloodthirst$ True:" + desc);
        } // bloodthirst

        final int storm = card.getKeywordAmount("Storm");
        for (int i = 0; i < storm; i++) {
            final StringBuilder trigScript = new StringBuilder(
                    "Mode$ SpellCast | ValidCard$ Card.Self | Execute$ Storm "
                            + "| TriggerDescription$ Storm (When you cast this spell, "
                            + "copy it for each spell cast before it this turn.)");

            card.setSVar("Storm", "AB$ CopySpellAbility | Cost$ 0 | Defined$ TriggeredSpellAbility | Amount$ StormCount | References$ StormCount");
            card.setSVar("StormCount", "Count$StormCount");
            final Trigger stormTrigger = TriggerHandler.parseTrigger(trigScript.toString(), card, true);

            card.addTrigger(stormTrigger);
        } // Storm
        final int cascade = card.getKeywordAmount("Cascade");
        for (int i = 0; i < cascade; i++) {
            final StringBuilder trigScript = new StringBuilder(
                    "Mode$ SpellCast | ValidCard$ Card.Self | Execute$ TrigCascade | Secondary$ " +
                    "True | TriggerDescription$ Cascade - CARDNAME");

            final String abString = "AB$ DigUntil | Cost$ 0 | Defined$ You | Amount$ 1 | Valid$ "
                    + "Card.nonLand+cmcLTCascadeX | FoundDestination$ Exile | RevealedDestination$"
                    + " Exile | References$ CascadeX | ImprintRevealed$ True | RememberFound$ True"
                    + " | SubAbility$ CascadeCast";
            final String dbCascadeCast = "DB$ Play | Defined$ Remembered | WithoutManaCost$ True | "
                    + "Optional$ True | SubAbility$ CascadeMoveToLib";
            final String dbMoveToLib = "DB$ ChangeZoneAll | ChangeType$ Card.IsRemembered,Card.IsImprinted"
                    + " | Origin$ Exile | Destination$ Library | RandomOrder$ True | LibraryPosition$ -1"
                    + " | SubAbility$ CascadeCleanup";
            card.setSVar("TrigCascade", abString);
            card.setSVar("CascadeCast", dbCascadeCast);
            card.setSVar("CascadeMoveToLib", dbMoveToLib);
            card.setSVar("CascadeX", "Count$CardManaCost");
            card.setSVar("CascadeCleanup", "DB$ Cleanup | ClearRemembered$ True | ClearImprinted$ True");
            final Trigger cascadeTrigger = TriggerHandler.parseTrigger(trigScript.toString(), card, true);

            card.addTrigger(cascadeTrigger);
        } // Cascade

        if (hasKeyword(card, "Recover") != -1) {
            final String recoverCost = card.getKeyword().get(card.getKeywordPosition("Recover")).split(":")[1];
            final String abStr = "AB$ ChangeZone | Cost$ 0 | Defined$ Self"
            		+ " | Origin$ Graveyard | Destination$ Hand | UnlessCost$ "
                    + recoverCost + " | UnlessPayer$ You | UnlessSwitched$ True"
                    + " | UnlessResolveSubs$ WhenNotPaid | SubAbility$ RecoverExile";
            card.setSVar("RecoverTrig", abStr);
            card.setSVar("RecoverExile", "DB$ ChangeZone | Defined$ Self"
            		+ " | Origin$ Graveyard | Destination$ Exile");
            String trigObject = card.isCreature() ? "Creature.Other+YouOwn" : "Creature.YouOwn";
            String trigArticle = card.isCreature() ? "another" : "a";
            String trigStr = "Mode$ ChangesZone | ValidCard$ " + trigObject
            		+ " | Origin$ Battlefield | Destination$ Graveyard | "
            		+ "TriggerZones$ Graveyard | Execute$ RecoverTrig | "
            		+ "TriggerDescription$ When " + trigArticle + " creature is "
            		+ "put into your graveyard from the battlefield, you "
            		+ "may pay " + recoverCost + ". If you do, return "
            		+ "CARDNAME from your graveyard to your hand. Otherwise,"
            		+ " exile CARDNAME. | Secondary$ True";
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, card, true);
            card.addTrigger(myTrigger);
        } // Recover

        int ripplePos = hasKeyword(card, "Ripple");
        while (ripplePos != -1) {
            final int n = ripplePos;
            final String parse = card.getKeyword().get(n);
            final String[] k = parse.split(":");
            final int num = Integer.parseInt(k[1]);
            UUID triggerSvar = UUID.randomUUID();

            final String actualTrigger = "Mode$ SpellCast | ValidCard$ Card.Self | " +
                    "Execute$ " + triggerSvar + " | Secondary$ True | TriggerDescription$" +
                    " Ripple " + num + " - CARDNAME | OptionalDecider$ You";
            final String abString = "AB$ Dig | Cost$ 0 | NoMove$ True | DigNum$ " + num +
                    " | Reveal$ True | RememberRevealed$ True | SubAbility$ DBCastRipple";
            final String dbCast = "DB$ Play | Valid$ Card.IsRemembered+sameName | " +
                    "ValidZone$ Library | WithoutManaCost$ True | Optional$ True | " +
                    "Amount$ All | SubAbility$ RippleMoveToBottom";

            card.setSVar(triggerSvar.toString(), abString);
            card.setSVar("DBCastRipple", dbCast);
            card.setSVar("RippleMoveToBottom", "DB$ ChangeZoneAll | ChangeType$ " +
                    "Card.IsRemembered | Origin$ Library | Destination$ Library | " +
                    "LibraryPosition$ -1 | SubAbility$ RippleCleanup");
            card.setSVar("RippleCleanup", "DB$ Cleanup | ClearRemembered$ True");

            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, card, true);
            card.addTrigger(parsedTrigger);
            
            ripplePos = hasKeyword(card, "Ripple", n + 1);
        } // Ripple
    }
    
    /**
     * TODO: Write javadoc for this method.
     * @param card
     * @return
     */
    public static AbilityStatic abilityDiscardSource(final Card sourceCard) {
        final AbilityStatic discard = new AbilityStatic(sourceCard, new Cost("0", true), null) {
            @Override
            public void resolve() {
                if (this.getActivatingPlayer().getController().confirmAction(this, null, "Discard this card?")) {
                    this.getActivatingPlayer().discard(sourceCard, this);
                }
            }

            @Override
            public boolean canPlay() {
                return sourceCard.isInZone(ZoneType.Hand) 
                        && sourceCard.getController().equals(this.getActivatingPlayer());
            }

            @Override
            public boolean canPlayAI() {
                return false;
            }

        };
        final StringBuilder sb = new StringBuilder();
        sb.append("You may discard ").append(sourceCard.getName());
        sb.append(" any time you could cast an instant.");
        discard.setDescription(sb.toString());

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard.getName()).append(" - discard this card.");
        discard.setStackDescription(sbStack.toString());
        return discard;
    }

    public final static void refreshTotemArmor(Card c) {
        boolean hasKw = c.hasKeyword("Totem armor");

        List<ReplacementEffect> res = c.getReplacementEffects();
        for ( int ix = 0; ix < res.size(); ix++ ) {
            ReplacementEffect re = res.get(ix);
            if( re.getMapParams().containsKey("TotemArmor") ) {
                if(hasKw) return; // has re and kw - nothing to do here 
                res.remove(ix--);
            }
        }

        if( hasKw ) { 
            ReplacementEffect re = ReplacementHandler.parseReplacement("Event$ Destroy | ActiveZones$ Battlefield | ValidCard$ Card.EnchantedBy | ReplaceWith$ RegenTA | Secondary$ True | TotemArmor$ True | Description$ Totem armor - " + c, c, true);
            c.getSVars().put("RegenTA", "AB$ DealDamage | Cost$ 0 | Defined$ ReplacedCard | Remove$ All | SubAbility$ DestroyMe");
            c.getSVars().put("DestroyMe", "DB$ Destroy | Defined$ Self");
            c.getReplacementEffects().add(re);
        }
    }

} // end class CardFactoryUtil
