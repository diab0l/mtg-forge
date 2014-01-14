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
package forge.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import forge.card.CardType;
import forge.card.MagicColor;
import forge.card.CardType.Constant;
import forge.error.BugReporter;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.ability.AbilityFactory;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.ability.effects.CharmEffect;
import forge.game.ai.AiProps;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.card.CardUtil;
import forge.game.card.CounterType;
import forge.game.card.CardPredicates.Presets;
import forge.game.combat.Combat;
import forge.game.combat.CombatUtil;
import forge.game.cost.Cost;
import forge.game.cost.CostDiscard;
import forge.game.cost.CostPart;
import forge.game.cost.CostPayment;
import forge.game.cost.CostPutCounter;
import forge.game.cost.CostSacrifice;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerControllerAi;
import forge.game.spellability.AbilityManaPart;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.spellability.TargetRestrictions;
import forge.game.staticability.StaticAbility;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerType;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;
import forge.util.MyRandom;


/**
 * <p>
 * ComputerUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class ComputerUtil {

    /**
     * <p>
     * handlePlayingSpellAbility.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean handlePlayingSpellAbility(final Player ai, final SpellAbility sa, final Game game) {

        game.getStack().freezeStack();
        final Card source = sa.getSourceCard();

        if (sa.isSpell() && !source.isCopiedSpell()) {
            sa.setSourceCard(game.getAction().moveToStack(source));
        }

        if (sa.getApi() == ApiType.Charm && !sa.isWrapper()) {
            CharmEffect.makeChoices(sa);
        }

        if (sa.hasParam("Bestow")) {
            sa.getSourceCard().animateBestow();
        }

        final Cost cost = sa.getPayCosts();

        if (cost == null) {
            if (ComputerUtilMana.payManaCost(ai, sa)) {
                game.getStack().addAndUnfreeze(sa);
                return true;
            }
        } else {
            final CostPayment pay = new CostPayment(cost, sa);
            if (pay.payComputerCosts(ai, game)) {
                game.getStack().addAndUnfreeze(sa);
                if (sa.getSplicedCards() != null && !sa.getSplicedCards().isEmpty()) {
                    game.getAction().reveal(sa.getSplicedCards(), ai, true, "Computer reveals spliced cards from ");
                }
                return true;
                // TODO: solve problems with TapsForMana triggers by adding
                // sources tapped here if possible (ArsenalNut)
            }
        }
        //Should not arrive here
        System.out.println("AI failed to play " + sa.getSourceCard());
        return false;
    }

    
    private static boolean hasDiscardHandCost(final Cost cost) {
        if (cost == null) {
            return false;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostDiscard) {
                final CostDiscard disc = (CostDiscard) part;
                if (disc.getType().equals("Hand")) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * <p>
     * counterSpellRestriction.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a int.
     */
    public static int counterSpellRestriction(final Player ai, final SpellAbility sa) {
        // Move this to AF?
        // Restriction Level is Based off a handful of factors

        int restrict = 0;

        final Card source = sa.getSourceCard();
        final TargetRestrictions tgt = sa.getTargetRestrictions();


        // Play higher costing spells first?
        final Cost cost = sa.getPayCosts();
        // Convert cost to CMC
        // String totalMana = source.getSVar("PayX"); // + cost.getCMC()

        // Consider the costs here for relative "scoring"
        if (hasDiscardHandCost(cost)) {
            // Null Brooch aid
            restrict -= (ai.getCardsIn(ZoneType.Hand).size() * 20);
        }

        // Abilities before Spells (card advantage)
        if (sa.isAbility()) {
            restrict += 40;
        }

        // TargetValidTargeting gets biggest bonus
        if (tgt.getSAValidTargeting() != null) {
            restrict += 35;
        }

        // Unless Cost gets significant bonus + 10-Payment Amount
        final String unless = sa.getParam("UnlessCost");
        if (unless != null && !unless.endsWith(">")) {
            final int amount = AbilityUtils.calculateAmount(source, unless, sa);

            final int usableManaSources = ComputerUtilMana.getAvailableMana(ai.getOpponent(), true).size();

            // If the Unless isn't enough, this should be less likely to be used
            if (amount > usableManaSources) {
                restrict += 20 - (2 * amount);
            } else {
                restrict -= (10 - (2 * amount));
            }
        }

        // Then base on Targeting Restriction
        final String[] validTgts = tgt.getValidTgts();
        if ((validTgts.length != 1) || !validTgts[0].equals("Card")) {
            restrict += 10;
        }

        // And lastly give some bonus points to least restrictive TargetType
        // (Spell,Ability,Triggered)
        final String tgtType = sa.getParam("TargetType");
        if (tgtType != null)
            restrict -= (5 * tgtType.split(",").length);

        return restrict;
    }

    // this is used for AI's counterspells
    /**
     * <p>
     * playStack.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    public static final void playStack(final SpellAbility sa, final Player ai, final Game game) {
        sa.setActivatingPlayer(ai);
        if (!ComputerUtilCost.canPayCost(sa, ai)) 
            return;
            
        final Card source = sa.getSourceCard();
        if (sa.isSpell() && !source.isCopiedSpell()) {
            sa.setSourceCard(game.getAction().moveToStack(source));
        }
        final Cost cost = sa.getPayCosts();
        if (cost == null) {
            ComputerUtilMana.payManaCost(ai, sa);
            game.getStack().add(sa);
        } else {
            final CostPayment pay = new CostPayment(cost, sa);
            if (pay.payComputerCosts(ai, game)) {
                game.getStack().add(sa);
            }
        }
    }

    /**
     * <p>
     * playStackFree.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    public static final void playSpellAbilityForFree(final Player ai, final SpellAbility sa) {
        sa.setActivatingPlayer(ai);

        final Card source = sa.getSourceCard();
        if (sa.isSpell() && !source.isCopiedSpell()) {
            sa.setSourceCard(ai.getGame().getAction().moveToStack(source));
        }

        ai.getGame().getStack().add(sa);
    }

    /**
     * <p>
     * playSpellAbilityWithoutPayingManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    public static final void playSpellAbilityWithoutPayingManaCost(final Player ai, final SpellAbility sa, final Game game) {
        final SpellAbility newSA = sa.copyWithNoManaCost();
        newSA.setActivatingPlayer(ai);

        if (!CostPayment.canPayAdditionalCosts(newSA.getPayCosts(), newSA)) {
            return;
        }

        final Card source = newSA.getSourceCard();
        if (newSA.isSpell() && !source.isCopiedSpell()) {
            newSA.setSourceCard(game.getAction().moveToStack(source));
        }

        final CostPayment pay = new CostPayment(newSA.getPayCosts(), newSA);
        pay.payComputerCosts(ai, game);

        game.getStack().add(newSA);
    }

    /**
     * <p>
     * playNoStack.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     */
    public static final void playNoStack(final Player ai, final SpellAbility sa, final Game game) {
        sa.setActivatingPlayer(ai);
        // TODO: We should really restrict what doesn't use the Stack
        if (ComputerUtilCost.canPayCost(sa, ai)) {
            final Card source = sa.getSourceCard();
            if (sa.isSpell() && !source.isCopiedSpell()) {
                sa.setSourceCard(game.getAction().moveToStack(source));
            }

            final Cost cost = sa.getPayCosts();
            if (cost == null) {
                ComputerUtilMana.payManaCost(ai, sa);
            } else {
                final CostPayment pay = new CostPayment(cost, sa);
                pay.payComputerCosts(ai, game);
            }

            AbilityUtils.resolve(sa);

            // destroys creatures if they have lethal damage, etc..
            //game.getAction().checkStateEffects();
        }
    } // play()

    /**
     * <p>
     * getCardPreference.
     * </p>
     * 
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param pref
     *            a {@link java.lang.String} object.
     * @param typeList
     *            a {@link forge.CardList} object.
     * @return a {@link forge.game.card.Card} object.
     */
    public static Card getCardPreference(final Player ai, final Card activate, final String pref, final List<Card> typeList) {

        if (activate != null) {
            final String[] prefValid = activate.getSVar("AIPreference").split("\\$");
            if (prefValid[0].equals(pref)) {
                final List<Card> prefList = CardLists.getValidCards(typeList, prefValid[1].split(","), activate.getController(), activate);
                if (prefList.size() != 0) {
                    CardLists.shuffle(prefList);
                    return prefList.get(0);
                }
            }
        }
        if (pref.contains("SacCost")) {
            // search for permanents with SacMe. priority 1 is the lowest, priority 5 the highest
            for (int ip = 0; ip < 6; ip++) {
                final int priority = 6 - ip;
                final List<Card> sacMeList = CardLists.filter(typeList, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return (c.hasSVar("SacMe") && (Integer.parseInt(c.getSVar("SacMe")) == priority));
                    }
                });
                if (!sacMeList.isEmpty()) {
                    CardLists.shuffle(sacMeList);
                    return sacMeList.get(0);
                }
            }

            // Sac lands
            final List<Card> landsInPlay = CardLists.getType(typeList, "Land");
            if (!landsInPlay.isEmpty()) {
                final List<Card> landsInHand = CardLists.getType(ai.getCardsIn(ZoneType.Hand), "Land");
                final List<Card> nonLandsInHand = CardLists.getNotType(ai.getCardsIn(ZoneType.Hand), "Land");
                nonLandsInHand.addAll(ai.getCardsIn(ZoneType.Library));
                final int highestCMC = Math.max(6, Aggregates.max(nonLandsInHand, CardPredicates.Accessors.fnGetCmc));
                if (landsInPlay.size() + landsInHand.size() >= highestCMC) {
                    // Don't need more land.
                    return ComputerUtilCard.getWorstLand(landsInPlay);
                }
            }
        }

        else if (pref.contains("DiscardCost")) { // search for permanents with
                                            // DiscardMe
            for (int ip = 0; ip < 6; ip++) { // priority 0 is the lowest,
                                             // priority 5 the highest
                final int priority = 6 - ip;
                for (Card c : typeList) {
                    if (priority == 3 && c.isLand()
                            && !ai.getCardsIn(ZoneType.Battlefield, "Crucible of Worlds").isEmpty()) {
                        return c;
                    }
                    if (c.hasSVar("DiscardMe") && Integer.parseInt(c.getSVar("DiscardMe")) == priority) {
                        return c;
                    }
                }
            }

            // Discard lands
            final List<Card> landsInHand = CardLists.getType(typeList, "Land");
            if (!landsInHand.isEmpty()) {
                final List<Card> landsInPlay = CardLists.getType(ai.getCardsIn(ZoneType.Battlefield), "Land");
                final List<Card> nonLandsInHand = CardLists.getNotType(ai.getCardsIn(ZoneType.Hand), "Land");
                final int highestCMC = Math.max(6, Aggregates.max(nonLandsInHand, CardPredicates.Accessors.fnGetCmc));
                if (landsInPlay.size() >= highestCMC
                        || (landsInPlay.size() + landsInHand.size() > 6 && landsInHand.size() > 1)) {
                    // Don't need more land.
                    return ComputerUtilCard.getWorstLand(landsInHand);
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * chooseSacrificeType.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param source
     *            a {@link forge.game.card.Card} object.
     * @param target
     *            a {@link forge.game.card.Card} object.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> chooseSacrificeType(final Player ai, final String type, final Card source, final Card target, final int amount) {
        List<Card> typeList = CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), type.split(";"), source.getController(), source);
        if (ai.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
            typeList = CardLists.getNotType(typeList, "Creature");
        }

        if ((target != null) && target.getController() == ai && typeList.contains(target)) {
            typeList.remove(target); // don't sacrifice the card we're pumping
        }

        if (typeList.size() < amount) {
            return null;
        }

        final List<Card> sacList = new ArrayList<Card>();
        int count = 0;

        while (count < amount) {
            Card prefCard = ComputerUtil.getCardPreference(ai, source, "SacCost", typeList);
            if (prefCard == null) {
                prefCard = ComputerUtilCard.getWorstAI(typeList);
            }
            if (prefCard == null) {
                return null;
            }
            sacList.add(prefCard);
            typeList.remove(prefCard);
            count++;
        }
        return sacList;
    }

    /**
     * <p>
     * chooseExileFrom.
     * </p>
     * 
     * @param zone
     *            a {@link java.lang.String} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param target
     *            a {@link forge.game.card.Card} object.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> chooseExileFrom(final Player ai, final ZoneType zone, final String type, final Card activate,
            final Card target, final int amount) {
        final Game game = ai.getGame();
        List<Card> typeList = new ArrayList<Card>();
        if (zone.equals(ZoneType.Stack)) {
            for (SpellAbilityStackInstance si : game.getStack()) {
                typeList.add(si.getSourceCard());
            }
        } else {
            typeList = ai.getCardsIn(zone);
        }
        typeList = CardLists.getValidCards(typeList, type.split(";"), activate.getController(), activate);
        
        if ((target != null) && target.getController() == ai && typeList.contains(target)) {
            typeList.remove(target); // don't exile the card we're pumping
        }

        if (typeList.size() < amount) {
            return null;
        }

        CardLists.sortByPowerAsc(typeList);
        final List<Card> exileList = new ArrayList<Card>();

        for (int i = 0; i < amount; i++) {
            exileList.add(typeList.get(i));
        }
        return exileList;
    }

    /**
     * <p>
     * choosePutToLibraryFrom.
     * </p>
     * 
     * @param zone
     *            a {@link java.lang.String} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param target
     *            a {@link forge.game.card.Card} object.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> choosePutToLibraryFrom(final Player ai, final ZoneType zone, final String type, final Card activate,
            final Card target, final int amount) {
        List<Card> typeList = ai.getCardsIn(zone);

        typeList = CardLists.getValidCards(typeList, type.split(";"), activate.getController(), activate);
        
        if ((target != null) && target.getController() == ai && typeList.contains(target)) {
            typeList.remove(target); // don't move the card we're pumping
        }

        if (typeList.size() < amount) {
            return null;
        }

        CardLists.sortByPowerAsc(typeList);
        final List<Card> list = new ArrayList<Card>();
        
        if (zone != ZoneType.Hand) {
            Collections.reverse(typeList);
        }
        
        for (int i = 0; i < amount; i++) {
            list.add(typeList.get(i));
        }

        return list;
    }

    /**
     * <p>
     * chooseTapType.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param tap
     *            a boolean.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> chooseTapType(final Player ai, final String type, final Card activate, final boolean tap, final int amount) {
        List<Card> typeList =
                CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), type.split(","), activate.getController(), activate);

        // is this needed?
        typeList = CardLists.filter(typeList, Presets.UNTAPPED);

        if (tap) {
            typeList.remove(activate);
        }

        if (typeList.size() < amount) {
            return null;
        }

        CardLists.sortByPowerAsc(typeList);

        final List<Card> tapList = new ArrayList<Card>();

        for (int i = 0; i < amount; i++) {
            tapList.add(typeList.get(i));
        }
        return tapList;
    }

    /**
     * <p>
     * chooseUntapType.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param untap
     *            a boolean.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> chooseUntapType(final Player ai, final String type, final Card activate, final boolean untap, final int amount) {
        List<Card> typeList =
                CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), type.split(","), activate.getController(), activate);

        // is this needed?
        typeList = CardLists.filter(typeList, Presets.TAPPED);

        if (untap) {
            typeList.remove(activate);
        }

        if (typeList.size() < amount) {
            return null;
        }

        CardLists.sortByPowerDesc(typeList);

        final List<Card> untapList = new ArrayList<Card>();

        for (int i = 0; i < amount; i++) {
            untapList.add(typeList.get(i));
        }
        return untapList;
    }

    /**
     * <p>
     * chooseReturnType.
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param activate
     *            a {@link forge.game.card.Card} object.
     * @param target
     *            a {@link forge.game.card.Card} object.
     * @param amount
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> chooseReturnType(final Player ai, final String type, final Card activate, final Card target, final int amount) {
        final List<Card> typeList =
                CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), type.split(";"), activate.getController(), activate);
        if ((target != null) && target.getController() == ai && typeList.contains(target)) {
            // don't bounce the card we're pumping
            typeList.remove(target);
        }

        if (typeList.size() < amount) {
            return null;
        }

        CardLists.sortByPowerAsc(typeList);
        final List<Card> returnList = new ArrayList<Card>();

        for (int i = 0; i < amount; i++) {
            returnList.add(typeList.get(i));
        }
        return returnList;
    }

    /**
     * <p>
     * sacrificePermanents.
     * </p>
     * @param amount
     *            a int.
     * @param source
     *            the source SpellAbility
     * @param destroy
     *            the destroy
     * @param list
     *            a {@link forge.CardList} object.
     * 
     * @return the card list
     */
    public static List<Card> choosePermanentsToSacrifice(final Player ai, final List<Card> cardlist, final int amount, SpellAbility source, 
            final boolean destroy, final boolean isOptional) {
        List<Card> remaining = new ArrayList<Card>(cardlist);
        final List<Card> sacrificed = new ArrayList<Card>();

        if (isOptional && source.getActivatingPlayer().isOpponentOf(ai)) { 
            return sacrificed; // sacrifice none 
        }
        if (isOptional && source.hasParam("Devour")) {
            remaining = CardLists.filter(remaining, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    if ((c.getCMC() <= 1 && c.getNetAttack() < 3)
                            || c.getNetAttack() + c.getNetDefense() <= 3) {
                        return true;
                    }
                    return false;
                }
            });
        }
        CardLists.sortByCmcDesc(remaining);
        Collections.reverse(remaining);

        final int max = Math.min(remaining.size(), amount);

        for (int i = 0; i < max; i++) {
            Card c = chooseCardToSacrifice(remaining, ai, destroy);
            remaining.remove(c);
            sacrificed.add(c);
        }
        return sacrificed;
    }

    // Precondition it wants: remaining are reverse-sorted by CMC
    private static Card chooseCardToSacrifice(final List<Card> remaining, final Player ai, final boolean destroy) {
        // If somehow ("Drop of Honey") they suggest to destroy opponent's card - use the chance!
        for(Card c : remaining) { // first compare is fast, second is precise
            if (c.getController() != ai && ai.getOpponents().contains(c.getController()) )
                return c;
        }
        
        if (destroy) {
            final List<Card> indestructibles = CardLists.getKeyword(remaining, "Indestructible");
            if (!indestructibles.isEmpty()) {
                return indestructibles.get(0);
            }
        }
        for (int ip = 0; ip < 6; ip++) { // priority 0 is the lowest, priority 5 the highest
            final int priority = 6 - ip;
            for (Card card : remaining) {
                if (card.hasSVar("SacMe") && Integer.parseInt(card.getSVar("SacMe")) == priority) {
                    return card;
                }
            }
        }

        Card c;

        if (CardLists.getNotType(remaining, "Creature").size() == 0) {
            c = ComputerUtilCard.getWorstCreatureAI(remaining);
        } else if (CardLists.getNotType(remaining, "Land").size() == 0) {
            c = ComputerUtilCard.getWorstLand(CardLists.filter(remaining, CardPredicates.Presets.LANDS));
        } else {
            c = ComputerUtilCard.getWorstPermanentAI(remaining, false, false, false, false);
        }

        final ArrayList<Card> auras = c.getEnchantedBy();

        if (auras.size() > 0) {
            // TODO: choose "worst" controlled enchanting Aura
            for (int j = 0; j < auras.size(); j++) {
                final Card aura = auras.get(j);
                if (aura.getController().equals(c.getController()) && remaining.contains(aura)) {
                    return aura;
                }
            }
        }
        return c;
    }

    /**
     * <p>
     * canRegenerate.
     * </p>
     * @param ai 
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @return a boolean.
     */
    public static boolean canRegenerate(Player ai, final Card card) {

        if (card.hasKeyword("CARDNAME can't be regenerated.")) {
            return false;
        }

        final Player controller = card.getController();
        final Game game = controller.getGame();
        final List<Card> l = controller.getCardsIn(ZoneType.Battlefield);
        for (final Card c : l) {
            for (final SpellAbility sa : c.getSpellAbilities()) {
                // This try/catch should fix the "computer is thinking" bug
                try {

                    if (!sa.isAbility() || sa.getApi() != ApiType.Regenerate) {
                        continue; // Not a Regenerate ability
                    }
                    sa.setActivatingPlayer(controller);
                    if (!(sa.canPlay() && ComputerUtilCost.canPayCost(sa, controller))) {
                        continue; // Can't play ability
                    }

                    if (controller == ai) {
                        final Cost abCost = sa.getPayCosts();
                        if (abCost != null) {
                            if (!ComputerUtilCost.checkLifeCost(controller, abCost, c, 4, null)) {
                                continue; // Won't play ability
                            }

                            if (!ComputerUtilCost.checkSacrificeCost(controller, abCost, c)) {
                                continue; // Won't play ability
                            }

                            if (!ComputerUtilCost.checkCreatureSacrificeCost(controller, abCost, c)) {
                                continue; // Won't play ability
                            }
                        }
                    }

                    final TargetRestrictions tgt = sa.getTargetRestrictions();
                    if (tgt != null) {
                        if (CardLists.getValidCards(game.getCardsIn(ZoneType.Battlefield), tgt.getValidTgts(), controller, sa.getSourceCard()).contains(card)) {
                            return true;
                        }
                    } else if (AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa).contains(card)) {
                        return true;
                    }

                } catch (final Exception ex) {
                    BugReporter.reportException(ex, "There is an error in the card code for %s:%n", c.getName(), ex.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * possibleDamagePrevention.
     * </p>
     * 
     * @param card
     *            a {@link forge.game.card.Card} object.
     * @return a int.
     */
    public static int possibleDamagePrevention(final Card card) {

        int prevented = 0;

        final Player controller = card.getController();
        final Game game = controller.getGame();

        final List<Card> l = controller.getCardsIn(ZoneType.Battlefield);
        for (final Card c : l) {
            for (final SpellAbility sa : c.getSpellAbilities()) {
                // if SA is from AF_Counter don't add to getPlayable
                // This try/catch should fix the "computer is thinking" bug
                try {
                    if (sa.getApi() == null || !sa.isAbility()) {
                        continue;
                    }

                    if (sa.getApi() == ApiType.PreventDamage && sa.canPlay()
                            && ComputerUtilCost.canPayCost(sa, controller)) {
                        if (AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa).contains(card)) {
                            prevented += AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("Amount"), sa);
                        }
                        final TargetRestrictions tgt = sa.getTargetRestrictions();
                        if (tgt != null) {
                            if (CardLists.getValidCards(game.getCardsIn(ZoneType.Battlefield), tgt.getValidTgts(), controller, sa.getSourceCard()).contains(card)) {
                                prevented += AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("Amount"), sa);
                            }

                        }
                    }
                } catch (final Exception ex) {
                    BugReporter.reportException(ex, "There is an error in the card code for %s:%n", c.getName(),
                            ex.getMessage());
                }
            }
        }
        return prevented;
    }

    /**
     * <p>
     * castPermanentInMain1.
     * </p>
     * 
     * @param sa
     *            a SpellAbility object.
     * @return a boolean.
     */
    public static boolean castPermanentInMain1(final Player ai, final SpellAbility sa) {
        final Card card = sa.getSourceCard();
        if ("True".equals(card.getSVar("NonStackingEffect")) && card.getController().isCardInPlay(card.getName())) {
            return false;
        }
        if (card.getSVar("PlayMain1").equals("TRUE") && (!card.getController().getCreaturesInPlay().isEmpty() || sa.getPayCosts().hasNoManaCost())) {
            return true;
        }
        if ((card.isCreature() && (ComputerUtil.hasACardGivingHaste(ai)
                || card.hasKeyword("Haste"))) || card.hasKeyword("Exalted")) {
            return true;
        }
        //cast equipments in Main1 when there are creatures to equip and no other unequipped equipment
        if (card.isEquipment()) {
            boolean playNow = false;
            for (Card c : card.getController().getCardsIn(ZoneType.Battlefield)) {
                if (c.isEquipment() && !c.isEquipping()) {
                    playNow = false;
                    break;
                }
                if (!playNow && c.isCreature() && CombatUtil.canAttackNextTurn(c) && c.canBeEquippedBy(card)) {
                    playNow = true;
                }
            }
            if (playNow) {
                return true;
            }
        }

        // get all cards the computer controls with BuffedBy
        final List<Card> buffed = ai.getCardsIn(ZoneType.Battlefield);
        for (Card buffedcard : buffed) {
            if (buffedcard.hasSVar("BuffedBy")) {
                final String buffedby = buffedcard.getSVar("BuffedBy");
                final String[] bffdby = buffedby.split(",");
                if (card.isValid(bffdby, buffedcard.getController(), buffedcard)) {
                    return true;
                }
            }
            if (card.isEquipment() && buffedcard.isCreature() && CombatUtil.canAttack(buffedcard, ai.getOpponent())) {
                return true;
            }
            if (card.isCreature()) {
                if (buffedcard.hasKeyword("Soulbond") && !buffedcard.isPaired()) {
                    return true;
                }
                if (buffedcard.hasKeyword("Evolve")) {
                    if (buffedcard.getNetAttack() < card.getNetAttack() || buffedcard.getNetDefense() < card.getNetDefense()) {
                        return true;
                    }
                }
            }
            if (card.hasKeyword("Soulbond") && buffedcard.isCreature() && !buffedcard.isPaired()) {
                return true;
            }

        } // BuffedBy

        // get all cards the human controls with AntiBuffedBy
        final List<Card> antibuffed = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
        for (Card buffedcard : antibuffed) {
            if (buffedcard.hasSVar("AntiBuffedBy")) {
                final String buffedby = buffedcard.getSVar("AntiBuffedBy");
                final String[] bffdby = buffedby.split(",");
                if (card.isValid(bffdby, buffedcard.getController(), buffedcard)) {
                    return true;
                }
            }
        } // AntiBuffedBy
        final List<Card> vengevines = ai.getCardsIn(ZoneType.Graveyard, "Vengevine");
        if (!vengevines.isEmpty()) {
            final List<Card> creatures = ai.getCardsIn(ZoneType.Hand);
            final List<Card> creatures2 = new ArrayList<Card>();
            for (int i = 0; i < creatures.size(); i++) {
                if (creatures.get(i).isCreature() && creatures.get(i).getManaCost().getCMC() <= 3) {
                    creatures2.add(creatures.get(i));
                }
            }
            if (((creatures2.size() + CardUtil.getThisTurnCast("Creature.YouCtrl", vengevines.get(0)).size()) > 1)
                    && card.isCreature() && card.getManaCost().getCMC() <= 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is it OK to cast this for less than the Max Targets?
     * @param source the source Card
     * @return true if it's OK to cast this Card for less than the max targets
     */
    public static boolean shouldCastLessThanMax(final Player ai, final Card source) {
        boolean ret = true;
        if (source.getManaCost().countX() > 0) {
            // If TargetMax is MaxTgts (i.e., an "X" cost), this is fine because AI is limited by mana available.
            return ret;
        } else {
            // Otherwise, if life is possibly in danger, then this is fine.
            Combat combat = new Combat(ai.getOpponent());
            List<Card> attackers = ai.getOpponent().getCreaturesInPlay();
            for (Card att : attackers) {
                if (CombatUtil.canAttackNextTurn(att, ai)) {
                    combat.addAttacker(att, att.getController().getOpponent());
                }
            }
            AiBlockController aiBlock = new AiBlockController(ai);
            aiBlock.assignBlockers(combat);
            if (!ComputerUtilCombat.lifeInDanger(ai, combat)) {
                // Otherwise, return false. Do not play now.
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Is this discard probably worse than a random draw?
     * @param discard Card to discard
     * @return boolean
     */
    public static boolean isWorseThanDraw(final Player ai, Card discard) {
        if (discard.hasSVar("DiscardMe")) {
            return true;
        }
        
        final Game game = ai.getGame();
        final List<Card> landsInPlay = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.LANDS);
        final List<Card> landsInHand = CardLists.filter(ai.getCardsIn(ZoneType.Hand), CardPredicates.Presets.LANDS);
        final List<Card> nonLandsInHand = CardLists.getNotType(ai.getCardsIn(ZoneType.Hand), "Land");
        final int highestCMC = Math.max(6, Aggregates.max(nonLandsInHand, CardPredicates.Accessors.fnGetCmc));
        final int discardCMC = discard.getCMC();
        if (discard.isLand()) {
            if (landsInPlay.size() >= highestCMC
                    || (landsInPlay.size() + landsInHand.size() > 6 && landsInHand.size() > 1)
                    || (landsInPlay.size() > 3 && nonLandsInHand.size() == 0)) {
                // Don't need more land.
                return true;
            }
        } else { //non-land
            if (discardCMC > landsInPlay.size() + landsInHand.size() + 2) {
                // not castable for some time.
                return true;
            } else if (!game.getPhaseHandler().isPlayerTurn(ai)
                    && game.getPhaseHandler().getPhase().isAfter(PhaseType.MAIN2)
                    && discardCMC > landsInPlay.size() + landsInHand.size()
                    && discardCMC > landsInPlay.size() + 1
                    && nonLandsInHand.size() > 1) {
                // not castable for at least one other turn.
                return true;
            } else if (landsInPlay.size() > 5 && discard.getCMC() <= 1
                    && !discard.hasProperty("hasXCost", ai, null)) {
                // Probably don't need small stuff now.
                return true;
            }
        }
        return false;
    }

    // returns true if it's better to wait until blockers are declared
    /**
     * <p>
     * waitForBlocking.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a boolean (returns true if it's better to wait until blockers are declared).
     */
    public static boolean waitForBlocking(final SpellAbility sa) {
        final Game game = sa.getActivatingPlayer().getGame();
        final PhaseHandler ph = game.getPhaseHandler();

        return (sa.getSourceCard().isCreature()
                && sa.getPayCosts().hasTapCost()
                && (ph.getPhase().isBefore(PhaseType.COMBAT_DECLARE_BLOCKERS)
                        || !ph.getNextTurn().equals(sa.getActivatingPlayer()))
                && !sa.getSourceCard().hasKeyword("At the beginning of the end step, exile CARDNAME.")
                && !sa.getSourceCard().hasKeyword("At the beginning of the end step, sacrifice CARDNAME."));
    }
    
    /**
     * <p>
     * castSpellMain1.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a boolean (returns true if it's better to wait until blockers are declared).
     */
    public static boolean castSpellInMain1(final Player ai, final SpellAbility sa) {
        final Card source = sa.getSourceCard();
        final SpellAbility sub = sa.getSubAbility();

        // Cipher spells
        if (sub != null) {
            final ApiType api = sub.getApi();
            if (ApiType.Encode == api && !ai.getCreaturesInPlay().isEmpty()) {
                return true;
            }
            if (ApiType.PumpAll == api && !ai.getCreaturesInPlay().isEmpty()) {
                return true;
            }
            if (ApiType.Pump == api) {
                return true;
            }
        }
        final List<Card> buffed = ai.getCardsIn(ZoneType.Battlefield);
        boolean checkThreshold = sa.isSpell() && !ai.hasThreshold() && !sa.getSourceCard().isInZone(ZoneType.Graveyard);
        for (Card buffedCard : buffed) {
            if (buffedCard.hasSVar("BuffedBy")) {
                final String buffedby = buffedCard.getSVar("BuffedBy");
                final String[] bffdby = buffedby.split(",");
                if (source.isValid(bffdby, buffedCard.getController(), buffedCard)) {
                    return true;
                }
            }
            //Fill the graveyard for Threshold
            if (checkThreshold) {
                for (StaticAbility stAb : buffedCard.getStaticAbilities()) {
                    if ("Threshold".equals(stAb.getMapParams().get("Condition"))) {
                        return true;
                    }
                }
            }
        }

        // get all cards the human controls with AntiBuffedBy
        final List<Card> antibuffed = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
        for (Card buffedcard : antibuffed) {
            if (buffedcard.hasSVar("AntiBuffedBy")) {
                final String buffedby = buffedcard.getSVar("AntiBuffedBy");
                final String[] bffdby = buffedby.split(",");
                if (source.isValid(bffdby, buffedcard.getController(), buffedcard)) {
                    return true;
                }
            }
        } // AntiBuffedBy
        
        if (sub != null) { 
            return castSpellInMain1(ai, sub);
        }
        
        return false;
    }

    // returns true if the AI should stop using the ability
    /**
     * <p>
     * preventRunAwayActivations.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a boolean (returns true if the AI should stop using the ability).
     */
    public static boolean preventRunAwayActivations(final SpellAbility sa) {
        int activations = sa.getRestrictions().getNumberTurnActivations();

        if (sa.isTemporary()) {
        	final Random r = MyRandom.getRandom();
        	return r.nextFloat() >= .95; // Abilities created by static abilities have no memory
        }

        if (activations < 10) { //10 activations per turn should still be acceptable
            return false;
        }

        final Random r = MyRandom.getRandom();
        return r.nextFloat() >= Math.pow(.95, activations);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param sa
     * @return
     */
    public static boolean activateForCost(SpellAbility sa, final Player ai) {
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        if (abCost == null) {
            return false;
        }
        if (abCost.hasTapCost()) {
            for (Card c : ai.getGame().getCardsIn(ZoneType.Battlefield)) {
                if (c.hasSVar("AITapDown")) {
                    if (source.isValid(c.getSVar("AITapDown"), c.getController(), c)) {
                        return true;
                    }
                }
            }
        } else if (sa.hasParam("Planeswalker") && ai.getGame().getPhaseHandler().is(PhaseType.MAIN2)) {
        	for (final CostPart part : abCost.getCostParts()) {
        		if (part instanceof CostPutCounter) {
        			return true;
        		}
        	}
        }
        for (final CostPart part : abCost.getCostParts()) {
            if (part instanceof CostSacrifice) {
                final CostSacrifice sac = (CostSacrifice) part;
    
                final String type = sac.getType();
    
                if (type.equals("CARDNAME")) {
                    if (source.getSVar("SacMe").equals("6")) {
                        return true;
                    }
                    continue;
                }
    
                final List<Card> typeList =
                        CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), type.split(","), source.getController(), source);
                for (Card c : typeList) {
                    if (c.getSVar("SacMe").equals("6")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * hasACardGivingHaste.
     * </p>
     * 
     * @return a boolean.
     */
    public static boolean hasACardGivingHaste(final Player ai) {
        final List<Card> all = new ArrayList<Card>(ai.getCardsIn(ZoneType.Battlefield));
        
        for (final Card c : all) {
            if (c.isEquipment()) {
                for (StaticAbility stAb : c.getStaticAbilities()) {
                    HashMap<String, String> params = stAb.getMapParams();
                    if ("Continuous".equals(params.get("Mode")) && params.containsKey("AddKeyword")
                            && params.get("AddKeyword").contains("Haste") && c.getEquippingCard() == null) {
                        return true;
                    }
                }
            }
        }
        
        all.addAll(ai.getCardsActivableInExternalZones());
        all.addAll(ai.getCardsIn(ZoneType.Hand));
    
        for (final Card c : all) {
            for (final SpellAbility sa : c.getSpellAbilities()) {
                if (sa.getApi() == ApiType.Pump && sa.hasParam("KW") && sa.getParam("KW").contains("Haste")) {
                    return true;
                }
            }
        }
        return false;
    } // hasACardGivingHaste

    /**
     * TODO: Write javadoc for this method.
     * @param ai
     * @return
     */
    public static int possibleNonCombatDamage(Player ai) {
        int damage = 0;
        final List<Card> all = new ArrayList<Card>(ai.getCardsIn(ZoneType.Battlefield));
        all.addAll(ai.getCardsActivableInExternalZones());
        all.addAll(ai.getCardsIn(ZoneType.Hand));
    
        for (final Card c : all) {
            for (final SpellAbility sa : c.getSpellAbilities()) {
                if (sa.getApi() != ApiType.DealDamage) {
                    continue;
                }
                final String numDam = sa.getParam("NumDmg");
                int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), numDam, sa);
                if (dmg <= damage) {
                    continue;
                }
                final TargetRestrictions tgt = sa.getTargetRestrictions();
                if (tgt == null) {
                    continue;
                }
                final Player enemy = ai.getOpponent();
                if (!sa.canTarget(enemy)) {
                    continue;
                }
                if (!ComputerUtilCost.canPayCost(sa, ai)) {
                    continue;
                }
                damage = dmg;
            }
        }
        return damage;
    }

    /**
     * <p>
     * predictThreatenedObjects.
     * </p>
     * 
     * @param saviourAf
     *            a AbilityFactory object
     * @return a {@link java.util.ArrayList} object.
     * @since 1.0.15
     */
    public static List<GameObject> predictThreatenedObjects(final Player aiPlayer, final SpellAbility sa) {
        final Game game = aiPlayer.getGame();
        final List<GameObject> objects = new ArrayList<GameObject>();
        if (game.getStack().isEmpty()) {
            return objects;
        }
    
        // check stack for something that will kill this
        final SpellAbility topStack = game.getStack().peekAbility();
        Iterables.addAll(objects, ComputerUtil.predictThreatenedObjects(aiPlayer, sa, topStack));
    
        return objects;
    }

    /**
     * <p>
     * predictThreatenedObjects.
     * </p>
     * 
     * @param saviourAf
     *            a AbilityFactory object
     * @param topStack
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     * @since 1.0.15
     */
    private static Iterable<? extends GameObject> predictThreatenedObjects(final Player aiPlayer, final SpellAbility saviour,
            final SpellAbility topStack) {
        Iterable<? extends GameObject> objects = new ArrayList<GameObject>();
        final List<GameObject> threatened = new ArrayList<GameObject>();
        ApiType saviourApi = saviour == null ? null : saviour.getApi();
    
        if (topStack == null) {
            return objects;
        }
    
        final Card source = topStack.getSourceCard();
        final ApiType threatApi = topStack.getApi();
    
        // Can only Predict things from AFs
        if (threatApi == null) {
            return threatened;
        }
        final TargetRestrictions tgt = topStack.getTargetRestrictions();

        if (tgt == null) {
            if (topStack.hasParam("Defined")) {
                objects = AbilityUtils.getDefinedObjects(source, topStack.getParam("Defined"), topStack);
            } else if (topStack.hasParam("ValidCards")) {
                List<Card> battleField = aiPlayer.getCardsIn(ZoneType.Battlefield);
                objects = CardLists.getValidCards(battleField, topStack.getParam("ValidCards").split(","), source.getController(), source);
            }
        } else {
            objects = topStack.getTargets().getTargets();
        }

        // Determine if Defined Objects are "threatened" will be destroyed
        // due to this SA

        // Lethal Damage => prevent damage/regeneration/bounce/shroud
        if (threatApi == ApiType.DealDamage || threatApi == ApiType.DamageAll) {
            // If PredictDamage is >= Lethal Damage
            final int dmg = AbilityUtils.calculateAmount(topStack.getSourceCard(),
                    topStack.getParam("NumDmg"), topStack);
            for (final Object o : objects) {
                if (o instanceof Card) {
                    final Card c = (Card) o;

                    // indestructible
                    if (c.hasKeyword("Indestructible")) {
                        continue;
                    }

                    // already regenerated
                    if (!c.getShield().isEmpty()) {
                        continue;
                    }

                    // don't use it on creatures that can't be regenerated
                    if ((saviourApi == ApiType.Regenerate || saviourApi == ApiType.RegenerateAll) && !c.canBeShielded()) {
                        continue;
                    }

                    // give Shroud to targeted creatures
                    if (saviourApi == ApiType.Pump && tgt == null && saviour.hasParam("KW")
                            && (saviour.getParam("KW").endsWith("Shroud")
                                    || saviour.getParam("KW").endsWith("Hexproof"))) {
                        continue;
                    }

                    // don't bounce or blink a permanent that the human
                    // player owns or is a token
                    if (saviourApi == ApiType.ChangeZone && (c.getOwner().isOpponentOf(aiPlayer) || c.isToken())) {
                        continue;
                    }

                    if (ComputerUtilCombat.predictDamageTo(c, dmg, source, false) >= ComputerUtilCombat.getDamageToKill(c)) {
                        threatened.add(c);
                    }
                } else if (o instanceof Player) {
                    final Player p = (Player) o;

                    if (source.hasKeyword("Infect")) {
                        if (ComputerUtilCombat.predictDamageTo(p, dmg, source, false) >= p.getPoisonCounters()) {
                            threatened.add(p);
                        }
                    } else if (ComputerUtilCombat.predictDamageTo(p, dmg, source, false) >= p.getLife()) {
                        threatened.add(p);
                    }
                }
            }
        }
        // Destroy => regeneration/bounce/shroud
        else if ((threatApi == ApiType.Destroy || threatApi == ApiType.DestroyAll)
                && (((saviourApi == ApiType.Regenerate || saviourApi == ApiType.RegenerateAll)
                        && !topStack.hasParam("NoRegen")) || saviourApi == ApiType.ChangeZone || saviourApi == ApiType.Pump
                        || saviourApi == null)) {
            for (final Object o : objects) {
                if (o instanceof Card) {
                    final Card c = (Card) o;
                    // indestructible
                    if (c.hasKeyword("Indestructible")) {
                        continue;
                    }

                    // already regenerated
                    if (!c.getShield().isEmpty()) {
                        continue;
                    }

                    // give Shroud to targeted creatures
                    if (saviourApi == ApiType.Pump && tgt == null && saviour.hasParam("KW")
                            && (saviour.getParam("KW").endsWith("Shroud")
                                    || saviour.getParam("KW").endsWith("Hexproof"))) {
                        continue;
                    }

                    // don't bounce or blink a permanent that the human
                    // player owns or is a token
                    if (saviourApi == ApiType.ChangeZone && (c.getOwner().isOpponentOf(aiPlayer) || c.isToken())) {
                        continue;
                    }

                    // don't use it on creatures that can't be regenerated
                    if (saviourApi == ApiType.Regenerate && !c.canBeShielded()) {
                        continue;
                    }
                    threatened.add(c);
                }
            }
        }
        // Exiling => bounce/shroud
        else if ((threatApi == ApiType.ChangeZone || threatApi == ApiType.ChangeZoneAll)
                && (saviourApi == ApiType.ChangeZone || saviourApi == ApiType.Pump || saviourApi == null)
                && topStack.hasParam("Destination")
                && topStack.getParam("Destination").equals("Exile")) {
            for (final Object o : objects) {
                if (o instanceof Card) {
                    final Card c = (Card) o;
                    // give Shroud to targeted creatures
                    if (saviourApi == ApiType.Pump && tgt == null && saviour.hasParam("KW")
                            && (saviour.getParam("KW").endsWith("Shroud") || saviour.getParam("KW").endsWith("Hexproof"))) {
                        continue;
                    }

                    // don't bounce or blink a permanent that the human
                    // player owns or is a token
                    if (saviourApi == ApiType.ChangeZone && (c.getOwner().isOpponentOf(aiPlayer) || c.isToken())) {
                        continue;
                    }

                    threatened.add(c);
                }
            }
        }
    
        Iterables.addAll(threatened, ComputerUtil.predictThreatenedObjects(aiPlayer, saviour, topStack.getSubAbility()));
        return threatened;
    }
    
    public static boolean playImmediately(Player ai, SpellAbility sa) {
        final Card source = sa.getSourceCard();
        final Zone zone = source.getZone();
 
        if (zone.getZoneType() == ZoneType.Battlefield) {
            if (predictThreatenedObjects(ai, null).contains(source)) {
                return true;
            }
        }
        return false;
    }

    // Computer mulligans if there are no cards with converted mana cost of
    // 0 in its hand
    public static boolean wantMulligan(Player ai) {
        final List<Card> handList = ai.getCardsIn(ZoneType.Hand);
        final boolean hasLittleCmc0Cards = CardLists.getValidCards(handList, "Card.cmcEQ0", ai, null).size() < 2;
        final AiController aic = ((PlayerControllerAi)ai.getController()).getAi();
        return (handList.size() > aic.getIntProperty(AiProps.MULLIGAN_THRESHOLD)) && hasLittleCmc0Cards;
    }
    
    public static List<Card> getPartialParisCandidates(Player ai) {
        final List<Card> candidates = new ArrayList<Card>();
        final List<Card> handList = ai.getCardsIn(ZoneType.Hand);
        
        final List<Card> lands = CardLists.getValidCards(handList, "Card.Land", ai, null);
        final List<Card> nonLands = CardLists.getValidCards(handList, "Card.nonLand", ai, null);
        CardLists.sortByCmcDesc(nonLands);
        
        if(lands.size() >= 3 && lands.size() <= 4)
            return candidates;
        
        if(lands.size() < 3)
        {
            //Not enough lands!
            int tgtCandidates = Math.max(Math.abs(lands.size()-nonLands.size()), 3);
            System.out.println("Partial Paris: " + ai.getName() + " lacks lands, aiming to exile " + tgtCandidates + " cards.");
            
            for(int i=0;i<tgtCandidates;i++)
            {
                candidates.add(nonLands.get(i));
            }
        }
        else
        {
            //Too many lands!
            //Init
            int cntColors = MagicColor.WUBRG.length;
            List<List<Card>> numProducers = new ArrayList<List<Card>>(cntColors);
            for(byte col : MagicColor.WUBRG) {
                numProducers.add(col, new ArrayList<Card>());
            }
            
            
            for(Card c : lands)
            {
                for(SpellAbility sa : c.getManaAbility())
                {
                    AbilityManaPart abmana = sa.getManaPart();
                    for(byte col : MagicColor.WUBRG) {
                        if(abmana.canProduce(MagicColor.toLongString(col))) {
                            numProducers.get(col).add(c);
                        }
                    }
                }                
            }
        }

        System.out.print("Partial Paris: " + ai.getName() + " may exile ");
        for(Card c : candidates)
        {
            System.out.print(c.toString() + ", ");
        }
        System.out.println();
        
        if(candidates.size() < 2)
            candidates.clear();
        return candidates;
    }


    public static boolean scryWillMoveCardToBottomOfLibrary(Player player, Card c) {
        boolean bottom = false;
        if (c.isBasicLand()) {
            List<Card> bl = player.getCardsIn(ZoneType.Battlefield);
            int nBasicLands = Iterables.size(Iterables.filter(bl, CardPredicates.Presets.LANDS));
            bottom = nBasicLands > 5; // if control more than 5 Basic land, probably don't need more
        } else if (c.isCreature()) {
            List<Card> cl = player.getCardsIn(ZoneType.Battlefield);
            cl = CardLists.filter(cl, CardPredicates.Presets.CREATURES);
            bottom = cl.size() > 5; // if control more than 5 Creatures, probably don't need more
        }
        return bottom;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param chooser
     * @param discarder
     * @param sa
     * @param validCards
     * @param min
     * @return
     */
    public static List<Card> getCardsToDiscardFromOpponent(Player chooser, Player discarder, SpellAbility sa, List<Card> validCards, int min, int max) {
        List<Card> goodChoices = CardLists.filter(validCards, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                if (c.hasSVar("DiscardMeByOpp") || c.hasSVar("DiscardMe")) {
                    return false;
                }
                return true;
            }
        });
        if (goodChoices.isEmpty()) {
            goodChoices = validCards;
        }
        final List<Card> dChoices = new ArrayList<Card>();
        if (sa.hasParam("DiscardValid")) {
            final String validString = sa.getParam("DiscardValid");
            if (validString.contains("Creature") && !validString.contains("nonCreature")) {
                final Card c = ComputerUtilCard.getBestCreatureAI(goodChoices);
                if (c != null) {
                    dChoices.add(ComputerUtilCard.getBestCreatureAI(goodChoices));
                }
            }
        }
    
        Collections.sort(goodChoices, CardLists.TextLenComparator);
    
        CardLists.sortByCmcDesc(goodChoices);
        dChoices.add(goodChoices.get(0));
    
        return Aggregates.random(goodChoices, min);
    }

    /**
     * TODO: Write javadoc for this method.
     * @param aiChoser
     * @param p
     * @param sa
     * @param validCards
     * @param min
     * @return
     */
    public static List<Card> getCardsToDiscardFromFriend(Player aiChooser, Player p, SpellAbility sa, List<Card> validCards, int min, int max) {
        if (p == aiChooser) { // ask that ai player what he would like to discard
            final AiController aic = ((PlayerControllerAi)p.getController()).getAi();
            return aic.getCardsToDiscard(min, max, validCards, sa);
        } 
        // no special options for human or remote friends
        return getCardsToDiscardFromOpponent(aiChooser, p, sa, validCards, min, max);
    }

    public static String chooseSomeType(Player ai, String kindOfType, String logic, List<String> invalidTypes) {
        final Game game = ai.getGame();
        String chosen = "";
        if( kindOfType.equals("Card")) {
            // TODO
            // computer will need to choose a type
            // based on whether it needs a creature or land,
            // otherwise, lib search for most common type left
            // then, reveal chosenType to Human
            if (game.getPhaseHandler().is(PhaseType.UNTAP) && logic == null) { // Storage Matrix
                double amount = 0;
                for (String type : Constant.CARD_TYPES) {
                    if (!invalidTypes.contains(type)) {
                        List<Card> list = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), CardPredicates.isType(type), Presets.TAPPED);
                        double i = type.equals("Creature") ? list.size() * 1.5 : list.size();
                        if (i > amount) {
                            amount = i;
                            chosen = type;
                        }
                    }
                }
            }
            if (StringUtils.isEmpty(chosen)) {
                chosen = "Creature";
            }
        } else if (kindOfType.equals("Creature")) {
            Player opp = ai.getOpponent();
            if (logic != null ) {
                if (logic.equals("MostProminentOnBattlefield")) {
                    chosen = ComputerUtilCard.getMostProminentCreatureType(game.getCardsIn(ZoneType.Battlefield));
                }
                else if (logic.equals("MostProminentComputerControls")) {
                    chosen = ComputerUtilCard.getMostProminentCreatureType(ai.getCardsIn(ZoneType.Battlefield));
                }
                else if (logic.equals("MostProminentHumanControls")) {
                    chosen = ComputerUtilCard.getMostProminentCreatureType(opp.getCardsIn(ZoneType.Battlefield));
                    if (!CardType.isACreatureType(chosen) || invalidTypes.contains(chosen)) {
                        chosen = ComputerUtilCard.getMostProminentCreatureType(CardLists.filterControlledBy(game.getCardsInGame(), opp));
                    }
                }
                else if (logic.equals("MostProminentInComputerDeck")) {
                    chosen = ComputerUtilCard.getMostProminentCreatureType(CardLists.filterControlledBy(game.getCardsInGame(), ai));
                }
                else if (logic.equals("MostProminentInComputerGraveyard")) {
                    chosen = ComputerUtilCard.getMostProminentCreatureType(ai.getCardsIn(ZoneType.Graveyard));
                }
            }
            if (!CardType.isACreatureType(chosen) || invalidTypes.contains(chosen)) {
                chosen = "Sliver";
            }

        } else if ( kindOfType.equals("Basic Land")) {
            if (logic != null) {
                if (logic.equals("MostNeededType")) {
                    // Choose a type that is in the deck, but not in hand or on the battlefield 
                    final ArrayList<String> basics = new ArrayList<String>();
                    basics.addAll(CardType.Constant.BASIC_TYPES);
                    List<Card> presentCards = ai.getCardsIn(ZoneType.Battlefield);
                    presentCards.addAll(ai.getCardsIn(ZoneType.Hand));
                    List<Card> possibleCards = ai.getAllCards();
                    
                    for (String b : basics) {
                        if(!Iterables.any(presentCards, CardPredicates.isType(b)) && Iterables.any(possibleCards, CardPredicates.isType(b))) {
                            chosen = b;
                        }
                    }
                    if (chosen.equals("")) {
                        for (String b : basics) {
                            if(Iterables.any(possibleCards, CardPredicates.isType(b))) {
                                chosen = b;
                            }
                        }
                    }
                } else if (logic.equals("ChosenLandwalk")) {
                    for (Card c : ai.getOpponent().getLandsInPlay()) {
                        for (String t : c.getType()) {
                            if (!invalidTypes.contains(t) && CardType.isABasicLandType(t)) {
                                chosen = t;
                                break;
                            }
                        }
                    }
                }
            }

            if (!CardType.isABasicLandType(chosen) || invalidTypes.contains(chosen)) {
                chosen = "Island";
            }

        } else if( kindOfType.equals("Land") ) {
            if (logic != null) {
                if (logic.equals("ChosenLandwalk")) {
                    for (Card c : ai.getOpponent().getLandsInPlay()) {
                        for (String t : c.getType()) {
                            if (!invalidTypes.contains(t) && CardType.isALandType(t)) {
                                chosen = t;
                                break;
                            }
                        }
                    }
                }
            }
            if( StringUtils.isEmpty(chosen))
                chosen = "Island";
        }

        return chosen;
    }
    
    public static List<Card> getSafeTargets(final Player ai, SpellAbility sa, List<Card> validCards) {
        List<Card> safeCards = new ArrayList<Card>(validCards);
        safeCards = CardLists.filter(safeCards, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                if (c.getController() == ai) {
                    if (c.getSVar("Targeting").equals("Dies") || c.getSVar("Targeting").equals("Counter"))
                    return false;
                }
                return true;
            }
        });
        return safeCards;
    }

    public static int damageFromETB(final Player player, final Card permanent) {
        int damage = 0;
        final Game game = player.getGame();
        final ArrayList<Trigger> theTriggers = new ArrayList<Trigger>();

        for (Card card : game.getCardsIn(ZoneType.Battlefield)) {
            theTriggers.addAll(card.getTriggers());
        }
        for (Trigger trigger : theTriggers) {
            HashMap<String, String> trigParams = trigger.getMapParams();
            final Card source = trigger.getHostCard();


            if (!trigger.zonesCheck(game.getZoneOf(source))) {
                continue;
            }
            if (!trigger.requirementsCheck(game)) {
                continue;
            }
            if (trigParams.containsKey("CheckOnTriggeredCard") 
                    && AbilityUtils.getDefinedCards(permanent, source.getSVar(trigParams.get("CheckOnTriggeredCard").split(" ")[0]), null).isEmpty()) {
                continue;
            }
            TriggerType mode = trigger.getMode();
            if (mode != TriggerType.ChangesZone) {
                continue;
            }
            if (!"Battlefield".equals(trigParams.get("Destination"))) {
                continue;
            }
            if (trigParams.containsKey("ValidCard")) {
                if (!permanent.isValid(trigParams.get("ValidCard"), source.getController(), source)) {
                    continue;
                }
            }

            String ability = source.getSVar(trigParams.get("Execute"));
            if (ability.isEmpty()) {
                continue;
            }
         
            final Map<String, String> abilityParams = AbilityFactory.getMapParams(ability);
            // Destroy triggers
            if ((abilityParams.containsKey("AB") && abilityParams.get("AB").equals("DealDamage"))
                    || (abilityParams.containsKey("DB") && abilityParams.get("DB").equals("DealDamage"))) {
                if (!"TriggeredCardController".equals(abilityParams.get("Defined"))) {
                    continue;
                }
                if (!abilityParams.containsKey("NumDmg")) {
                    continue;
                }
                damage += ComputerUtilCombat.predictDamageTo(player, AbilityUtils.calculateAmount(source, abilityParams.get("NumDmg"), null), source, false);
            } else if ((abilityParams.containsKey("AB") && abilityParams.get("AB").equals("LoseLife"))
                    || (abilityParams.containsKey("DB") && abilityParams.get("DB").equals("LoseLife"))) {
                if (!"TriggeredCardController".equals(abilityParams.get("Defined"))) {
                    continue;
                }
                if (!abilityParams.containsKey("LifeAmount")) {
                    continue;
                }
                damage += AbilityUtils.calculateAmount(source, abilityParams.get("LifeAmount"), null);
            }
        }
        return damage;
    }

    public static boolean isNegativeCounter(CounterType type, Card c) {
        return type == CounterType.AGE || type == CounterType.BLAZE || type == CounterType.BRIBERY || type == CounterType.DOOM
                || type == CounterType.ICE || type == CounterType.M1M1 || type == CounterType.M0M2 || type == CounterType.M0M1
                || type == CounterType.M1M0 || type == CounterType.M2M1 || type == CounterType.M2M2 || type == CounterType.MUSIC
                || type == CounterType.PARALYZATION || type == CounterType.SHELL || type == CounterType.SLEEP 
                || type == CounterType.SLEIGHT || (type == CounterType.TIME && !c.isInPlay()) || type == CounterType.WAGE;
    }

    /**
     * <p>
     * evaluateBoardPosition.
     * </p>
     * 
     * @param listToEvaluate
     *            a  list of players to evaluate.
     * @return a Player.
     */
    public static Player evaluateBoardPosition(final List<Player> listToEvaluate) {
        Player bestBoardPosition = listToEvaluate.get(0);
        int bestBoardRating = 0;

        for (final Player p : listToEvaluate) {
            int pRating = p.getLife() * 3;
            pRating += p.getLandsInPlay().size() * 2;

            for (final Card c : p.getCardsIn(ZoneType.Battlefield)) {
                pRating += ComputerUtilCard.evaluateCreature(c) / 3;
            }

            if (p.getCardsIn(ZoneType.Library).size() < 3) {
                pRating /= 5;
            }

            System.out.println("Board position evaluation for " + p + ": " + pRating);

            if (pRating > bestBoardRating) {
                bestBoardRating = pRating;
                bestBoardPosition = p;
            }
        }

        return bestBoardPosition;
    }
}
