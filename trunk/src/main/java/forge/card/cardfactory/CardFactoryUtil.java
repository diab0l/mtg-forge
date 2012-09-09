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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.esotericsoftware.minlog.Log;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardCharacteristicName;
import forge.CardList;
import forge.CardListFilter;
import forge.CardListUtil;
import forge.CardUtil;
import forge.Command;
import forge.CommandArgs;
import forge.Counters;
import forge.GameActionUtil;
import forge.Singletons;
import forge.card.CardCharacteristics;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostShard;
import forge.card.replacement.ReplacementEffect;
import forge.card.replacement.ReplacementHandler;
import forge.card.replacement.ReplacementLayer;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilityMana;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityRestriction;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.card.trigger.TriggerType;
import forge.control.input.Input;
import forge.control.input.InputPayManaCost;
import forge.control.input.InputPayManaCostUtil;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiUtils;
import forge.gui.match.CMatchUI;
import forge.util.MyRandom;
import forge.view.ButtonUtil;

/**
 * <p>
 * CardFactoryUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryUtil {
    private static Random random = MyRandom.getRandom();

    /**
     * <p>
     * getMostExpensivePermanentAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.Card} object.
     */
    public static Card getMostExpensivePermanentAI(final CardList list, final SpellAbility spell, final boolean targeted) {
        CardList all = list;
        if (targeted) {
            all = all.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return c.canBeTargetedBy(spell);
                }
            });
        }

        return CardFactoryUtil.getMostExpensivePermanentAI(all);
    }

    /**
     * getMostExpensivePermanentAI.
     * 
     * @param all
     *            the all
     * @return the card
     */
    public static Card getMostExpensivePermanentAI(final CardList all) {
        if (all.size() == 0) {
            return null;
        }
        Card biggest = null;
        biggest = all.get(0);

        int bigCMC = 0;
        for (int i = 0; i < all.size(); i++) {
            final Card card = all.get(i);
            int curCMC = card.getCMC();

            // Add all cost of all auras with the same controller
            final CardList auras = new CardList(card.getEnchantedBy());
            auras.getController(card.getController());
            curCMC += auras.getTotalConvertedManaCost() + auras.size();

            if (curCMC >= bigCMC) {
                bigCMC = curCMC;
                biggest = all.get(i);
            }
        }

        return biggest;
    }

    // for Sarkhan the Mad
    /**
     * <p>
     * getCheapestCreatureAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.Card} object.
     */
    public static Card getCheapestCreatureAI(CardList list, final SpellAbility spell, final boolean targeted) {
        list = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.isCreature();
            }
        });
        return CardFactoryUtil.getCheapestPermanentAI(list, spell, targeted);
    }

    /**
     * <p>
     * getCheapestPermanentAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.Card} object.
     */
    public static Card getCheapestPermanentAI(final CardList list, final SpellAbility spell, final boolean targeted) {
        CardList all = list;
        if (targeted) {
            all = all.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return c.canBeTargetedBy(spell);
                }
            });
        }
        if (all.size() == 0) {
            return null;
        }

        // get cheapest card:
        Card cheapest = null;
        cheapest = all.get(0);

        for (int i = 0; i < all.size(); i++) {
            if (cheapest.getManaCost().getCMC() <= cheapest.getManaCost().getCMC()) {
                cheapest = all.get(i);
            }
        }

        return cheapest;

    }

    /**
     * <p>
     * getBestLandAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getBestLandAI(final CardList list) {
        final CardList land = list.getType("Land");
        if (!(land.size() > 0)) {
            return null;
        }

        // prefer to target non basic lands
        final CardList nbLand = land.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (!c.isBasicLand());
            }
        });

        if (nbLand.size() > 0) {
            // TODO - Rank non basics?

            final Random r = MyRandom.getRandom();
            return nbLand.get(r.nextInt(nbLand.size()));
        }

        // if no non-basic lands, target the least represented basic land type
        final String[] names = { "Plains", "Island", "Swamp", "Mountain", "Forest" };
        String sminBL = "";
        int iminBL = 20000; // hopefully no one will ever have more than 20000
                            // lands of one type....
        int n = 0;
        for (int i = 0; i < 5; i++) {
            n = land.getType(names[i]).size();
            if ((n < iminBL) && (n > 0)) {
                // if two or more are tied, only the
                // first
                // one checked will be used
                iminBL = n;
                sminBL = names[i];
            }
        }
        if (iminBL == 20000) {
            return null; // no basic land was a minimum
        }

        final CardList bLand = land.getType(sminBL);
        for (int i = 0; i < bLand.size(); i++) {
            if (!bLand.get(i).isTapped()) {
                // prefer untapped lands
                return bLand.get(i);
            }
        }

        final Random r = MyRandom.getRandom();
        return bLand.get(r.nextInt(bLand.size())); // random tapped land of
                                                   // least represented type
    }

    // The AI doesn't really pick the best enchantment, just the most expensive.
    /**
     * <p>
     * getBestEnchantmentAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.Card} object.
     */
    public static Card getBestEnchantmentAI(final CardList list, final SpellAbility spell, final boolean targeted) {
        CardList all = list;
        all = all.getType("Enchantment");
        if (targeted) {
            all = all.filter(new CardListFilter() {

                @Override
                public boolean addCard(final Card c) {
                    return c.canBeTargetedBy(spell);
                }
            });
        }
        if (all.size() == 0) {
            return null;
        }

        // get biggest Enchantment
        Card biggest = null;
        biggest = all.get(0);

        int bigCMC = 0;
        for (int i = 0; i < all.size(); i++) {
            final int curCMC = all.get(i).getManaCost().getCMC();

            if (curCMC > bigCMC) {
                bigCMC = curCMC;
                biggest = all.get(i);
            }
        }

        return biggest;
    }

    // The AI doesn't really pick the best artifact, just the most expensive.
    /**
     * <p>
     * getBestArtifactAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getBestArtifactAI(final CardList list) {
        CardList all = list;
        all = all.getType("Artifact");
        if (all.size() == 0) {
            return null;
        }

        // get biggest Artifact
        Card biggest = null;
        biggest = all.get(0);

        int bigCMC = 0;
        for (int i = 0; i < all.size(); i++) {
            final int curCMC = all.get(i).getManaCost().getCMC();

            if (curCMC > bigCMC) {
                bigCMC = curCMC;
                biggest = all.get(i);
            }
        }

        return biggest;
    }

    /**
     * <p>
     * doesCreatureAttackAI.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean doesCreatureAttackAI(final Card card) {
        final List<Card> att = ComputerUtil.getAttackers().getAttackers();

        return att.contains(card);
    }

    /**
     * <p>
     * evaluateCreatureList.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a int.
     */
    public static int evaluateCreatureList(final CardList list) {
        int value = 0;
        for (int i = 0; i < list.size(); i++) {
            value += CardFactoryUtil.evaluateCreature(list.get(i));
        }

        return value;
    }

    /**
     * <p>
     * evaluatePermanentList.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a int.
     */
    public static int evaluatePermanentList(final CardList list) {
        int value = 0;
        for (int i = 0; i < list.size(); i++) {
            value += list.get(i).getCMC() + 1;
        }

        return value;
    }

    /**
     * <p>
     * evaluateCreature.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int evaluateCreature(final Card c) {

        int value = 100;
        if (c.isToken()) {
            value = 80; // tokens should be worth less than actual cards
        }
        int power = c.getNetCombatDamage();
        final int toughness = c.getNetDefense();

        if (c.hasKeyword("Prevent all combat damage that would be dealt by CARDNAME.")
                || c.hasKeyword("Prevent all damage that would be dealt by CARDNAME.")
                || c.hasKeyword("Prevent all combat damage that would be dealt to and dealt by CARDNAME.")
                || c.hasKeyword("Prevent all damage that would be dealt to and dealt by CARDNAME.")) {
            power = 0;
        }

        value += power * 15;
        value += toughness * 10;
        value += c.getCMC() * 5;

        // Evasion keywords
        if (c.hasKeyword("Flying")) {
            value += power * 10;
        }
        if (c.hasKeyword("Horsemanship")) {
            value += power * 10;
        }
        if (c.hasKeyword("Unblockable")) {
            value += power * 10;
        }
        if (c.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.")) {
            value += power * 6;
        }
        if (c.hasKeyword("Fear")) {
            value += power * 6;
        }
        if (c.hasKeyword("Intimidate")) {
            value += power * 6;
        }
        if (c.hasStartOfKeyword("CARDNAME can't be blocked except by")) {
            value += power * 5;
        }
        if (c.hasStartOfKeyword("CARDNAME can't be blocked by")) {
            value += power * 2;
        }

        // Battle stats increasing keywords
        if (c.hasKeyword("Double Strike")) {
            value += 10 + (power * 15);
        }
        value += c.getKeywordMagnitude("Bushido") * 16;
        value += c.getAmountOfKeyword("Flanking") * 15;

        // Other good keywords
        if (c.hasKeyword("Deathtouch") && (power > 0)) {
            value += 25;
        }
        value += c.getAmountOfKeyword("Exalted") * 15;
        if (c.hasKeyword("First Strike") && !c.hasKeyword("Double Strike") && (power > 0)) {
            value += 10 + (power * 5);
        }
        if (c.hasKeyword("Lifelink")) {
            value += power * 10;
        }
        if (c.hasKeyword("Trample") && (power > 1)) {
            value += power * 3;
        }
        if (c.hasKeyword("Vigilance")) {
            value += (power * 5) + (toughness * 5);
        }
        if (c.hasKeyword("Wither")) {
            value += power * 10;
        }
        if (c.hasKeyword("Infect")) {
            value += power * 15;
        }
        value += c.getKeywordMagnitude("Rampage");
        value += c.getKeywordMagnitude("Annihilator") * 50;
        if (c.hasKeyword("Whenever a creature dealt damage by CARDNAME this turn is "
                + "put into a graveyard, put a +1/+1 counter on CARDNAME.")
                && (power > 0)) {
            value += 2;
        }
        if (c.hasKeyword("Whenever a creature dealt damage by CARDNAME this turn is "
                + "put into a graveyard, put a +2/+2 counter on CARDNAME.")
                && (power > 0)) {
            value += 4;
        }

        // Defensive Keywords
        if (c.hasKeyword("Reach")) {
            value += 5;
        }
        if (c.hasKeyword("CARDNAME can block creatures with shadow as though they didn't have shadow.")) {
            value += 3;
        }

        // Protection
        if (c.hasKeyword("Indestructible")) {
            value += 70;
        }
        if (c.hasKeyword("Prevent all damage that would be dealt to CARDNAME.")) {
            value += 60;
        }
        if (c.hasKeyword("Prevent all combat damage that would be dealt to CARDNAME.")) {
            value += 50;
        }
        if (c.hasKeyword("Shroud")) {
            value += 30;
        }
        if (c.hasKeyword("Hexproof")) {
            value += 35;
        }
        if (c.hasStartOfKeyword("Protection")) {
            value += 20;
        }
        if (c.hasStartOfKeyword("PreventAllDamageBy")) {
            value += 10;
        }
        value += c.getKeywordMagnitude("Absorb") * 11;

        // Bad keywords
        if (c.hasKeyword("Defender") || c.hasKeyword("CARDNAME can't attack.")) {
            value -= (power * 9) + 40;
        }
        if (c.hasKeyword("CARDNAME can't block.")) {
            value -= 10;
        }
        if (c.hasKeyword("CARDNAME attacks each turn if able.")) {
            value -= 10;
        }
        if (c.hasKeyword("CARDNAME can block only creatures with flying.")) {
            value -= toughness * 5;
        }

        if (c.hasStartOfKeyword("When CARDNAME is dealt damage, destroy it.")) {
            value -= (toughness - 1) * 9;
        }

        if (c.hasKeyword("CARDNAME can't attack or block.")) {
            value = 50 + (c.getCMC() * 5); // reset everything - useless
        }
        if (c.hasKeyword("CARDNAME doesn't untap during your untap step.")) {
            if (c.isTapped()) {
                value = 50 + (c.getCMC() * 5); // reset everything - useless
            } else {
                value -= 50;
            }
        }
        if (c.hasKeyword("At the beginning of the end step, destroy CARDNAME.")) {
            value -= 50;
        }
        if (c.hasKeyword("At the beginning of the end step, exile CARDNAME.")) {
            value -= 50;
        }
        if (c.hasKeyword("At the beginning of the end step, sacrifice CARDNAME.")) {
            value -= 50;
        }
        if (c.hasStartOfKeyword("At the beginning of your upkeep, CARDNAME deals")) {
            value -= 20;
        }
        if (c.hasStartOfKeyword("At the beginning of your upkeep, destroy CARDNAME unless you pay")) {
            value -= 20;
        }
        if (c.hasStartOfKeyword("At the beginning of your upkeep, sacrifice CARDNAME unless you pay")) {
            value -= 20;
        }
        if (c.hasStartOfKeyword("Upkeep:")) {
            value -= 20;
        }
        if (c.hasStartOfKeyword("Cumulative upkeep")) {
            value -= 30;
        }
        if (c.hasStartOfKeyword("(Echo unpaid)")) {
            value -= 10;
        }
        if (c.hasStartOfKeyword("Fading")) {
            value -= 20; // not used atm
        }
        if (c.hasStartOfKeyword("Vanishing")) {
            value -= 20; // not used atm
        }
        if (c.getSVar("Targeting").equals("Dies")) {
            value -= 25;
        }
        if (c.getSVar("SacrificeEndCombat").equals("True")) {
            value -= 40;
        }

        for (final SpellAbility sa : c.getSpellAbilities()) {
            if (sa.isAbility()) {
                value += 10;
            }
        }
        if (!c.getManaAbility().isEmpty()) {
            value += 10;
        }

        if (c.isUntapped()) {
            value += 1;
        }

        // paired creatures are more valuable because they grant a bonus to the other creature
        if (c.isPaired()) {
            value += 14;
        }

        return value;

    } // evaluateCreature

    // returns null if list.size() == 0
    /**
     * <p>
     * getBestAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getBestAI(final CardList list) {
        // Get Best will filter by appropriate getBest list if ALL of the list
        // is of that type
        if (list.getNotType("Creature").size() == 0) {
            return CardFactoryUtil.getBestCreatureAI(list);
        }

        if (list.getNotType("Land").size() == 0) {
            return CardFactoryUtil.getBestLandAI(list);
        }

        // TODO - Once we get an EvaluatePermanent this should call
        // getBestPermanent()
        return CardFactoryUtil.getMostExpensivePermanentAI(list);
    }

    /**
     * getBestCreatureAI.
     * 
     * @param list
     *            the list
     * @return the card
     */
    public static Card getBestCreatureAI(final CardList list) {
        CardList all = list;
        all = all.getType("Creature");
        Card biggest = null;

        if (all.size() != 0) {
            biggest = all.get(0);

            for (int i = 0; i < all.size(); i++) {
                if (CardFactoryUtil.evaluateCreature(biggest) < CardFactoryUtil.evaluateCreature(all.get(i))) {
                    biggest = all.get(i);
                }
            }
        }
        return biggest;
    }

    // This selection rates tokens higher
    /**
     * <p>
     * getBestCreatureToBounceAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getBestCreatureToBounceAI(final CardList list) {
        final int tokenBonus = 40;
        CardList all = list;
        all = all.getType("Creature");
        Card biggest = null; // returns null if list.size() == 0
        int biggestvalue = 0;
        int newvalue = 0;

        if (all.size() != 0) {
            biggest = all.get(0);

            for (int i = 0; i < all.size(); i++) {
                biggestvalue = CardFactoryUtil.evaluateCreature(biggest);
                if (biggest.isToken()) {
                    biggestvalue += tokenBonus; // raise the value of tokens
                }
                newvalue = CardFactoryUtil.evaluateCreature(all.get(i));
                if (all.get(i).isToken()) {
                    newvalue += tokenBonus; // raise the value of tokens
                }
                if (biggestvalue < newvalue) {
                    biggest = all.get(i);
                }
            }
        }
        return biggest;
    }

    /**
     * <p>
     * getWorstAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getWorstAI(final CardList list) {
        return CardFactoryUtil.getWorstPermanentAI(list, false, false, false, false);
    }

    // returns null if list.size() == 0
    /**
     * <p>
     * getWorstCreatureAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getWorstCreatureAI(final CardList list) {
        CardList all = list;
        all = all.getType("Creature");
        // get smallest creature
        Card smallest = null;

        if (all.size() != 0) {
            smallest = all.get(0);

            for (int i = 0; i < all.size(); i++) {
                if (CardFactoryUtil.evaluateCreature(smallest) > CardFactoryUtil.evaluateCreature(all.get(i))) {
                    smallest = all.get(i);
                }
            }
        }
        return smallest;
    }

    /**
     * <p>
     * getWorstPermanentAI.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @param biasEnch
     *            a boolean.
     * @param biasLand
     *            a boolean.
     * @param biasArt
     *            a boolean.
     * @param biasCreature
     *            a boolean.
     * @return a {@link forge.Card} object.
     */
    public static Card getWorstPermanentAI(final CardList list, final boolean biasEnch, final boolean biasLand,
            final boolean biasArt, final boolean biasCreature) {
        if (list.size() == 0) {
            return null;
        }
        System.out.println("getWorstPermanentAI: " + list);

        if (biasEnch && (list.getType("Enchantment").size() > 0)) {
            return CardFactoryUtil.getCheapestPermanentAI(list.getType("Enchantment"), null, false);
        }

        if (biasArt && (list.getType("Artifact").size() > 0)) {
            return CardFactoryUtil.getCheapestPermanentAI(list.getType("Artifact"), null, false);
        }

        if (biasLand && (list.getType("Land").size() > 0)) {
            return CardFactoryUtil.getWorstLand(list.getType("Land"));
        }

        if (biasCreature && (list.getType("Creature").size() > 0)) {
            return CardFactoryUtil.getWorstCreatureAI(list.getType("Creature"));
        }

        if (list.getType("Land").size() > 6) {
            return CardFactoryUtil.getWorstLand(list.getType("Land"));
        }

        if ((list.getType("Artifact").size() > 0) || (list.getType("Enchantment").size() > 0)) {
            return CardFactoryUtil.getCheapestPermanentAI(list.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return c.isArtifact() || c.isEnchantment();
                }
            }), null, false);
        }

        if (list.getType("Creature").size() > 0) {
            return CardFactoryUtil.getWorstCreatureAI(list.getType("Creature"));
        }

        // Planeswalkers fall through to here, lands will fall through if there
        // aren't very many
        return CardFactoryUtil.getCheapestPermanentAI(list, null, false);
    }


    /**
     * <p>
     * inputDestroyNoRegeneration.
     * </p>
     * 
     * @param choices
     *            a {@link forge.CardList} object.
     * @param message
     *            a {@link java.lang.String} object.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputDestroyNoRegeneration(final CardList choices, final String message) {
        final Input target = new Input() {
            private static final long serialVersionUID = -6637588517573573232L;

            @Override
            public void showMessage() {
                CMatchUI.SINGLETON_INSTANCE.showMessage(message);
                ButtonUtil.disableAll();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (choices.contains(card)) {
                    Singletons.getModel().getGameAction().destroyNoRegeneration(card);
                    this.stop();
                }
            }
        };
        return target;
    } // inputDestroyNoRegeneration()

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

        final Cost cost = new Cost(sourceCard, manaCost, true);
        class AbilityUnearth extends AbilityActivated {
            public AbilityUnearth(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityUnearth(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                final SpellAbilityRestriction restrict = new SpellAbilityRestriction();
                restrict.setZone(ZoneType.Graveyard);
                restrict.setSorcerySpeed(true);
                res.setRestrictions(restrict);
                return res;
            }

            private static final long serialVersionUID = -5633945565395478009L;

            @Override
            public void resolve() {
                final Card card = Singletons.getModel().getGameAction().moveToPlay(sourceCard);

                card.addIntrinsicKeyword("At the beginning of the end step, exile CARDNAME.");
                card.addIntrinsicKeyword("Haste");
                card.setUnearthed(true);
            }

            @Override
            public boolean canPlayAI() {
                if (Singletons.getModel().getGameState().getPhaseHandler().getPhase().isAfter(PhaseType.MAIN1)
                        || Singletons.getModel().getGameState().getPhaseHandler().isPlayerTurn(AllZone.getHumanPlayer())) {
                    return false;
                }
                return ComputerUtil.canPayCost(this);
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
        final SpellAbility morphDown = new Spell(sourceCard) {
            private static final long serialVersionUID = -1438810964807867610L;

            @Override
            public void resolve() {
                sourceCard.turnFaceDown();

                sourceCard.comesIntoPlay();

                Singletons.getModel().getGameAction().moveToPlay(sourceCard);
            }

            @Override
            public boolean canPlay() {
                //Lands do not have SpellPermanents.
                if (sourceCard.isLand()) {
                    return (AllZone.getZoneOf(sourceCard).is(ZoneType.Hand) || sourceCard.hasKeyword("May be played"))
                            && PhaseHandler.canCastSorcery(sourceCard.getController());
                }
                else {
                    return sourceCard.getSpellPermanent().canPlay();
                }
            }

        };

        morphDown.setManaCost("3");
        morphDown.setDescription("(You may cast this face down as a 2/2 creature for 3.)");
        morphDown.setStackDescription("Morph - Creature 2/2");

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
     * @param a
     *            a int.
     * @param d
     *            a int.
     * @return a {@link forge.card.spellability.AbilityActivated} object.
     */
    public static AbilityStatic abilityMorphUp(final Card sourceCard, final Cost cost, final int a, final int d) {
        final AbilityStatic morphUp = new AbilityStatic(sourceCard, cost, null) {

            @Override
            public void resolve() {
                if (sourceCard.turnFaceUp()) {
                    // Run triggers
                    final Map<String, Object> runParams = new TreeMap<String, Object>();
                    runParams.put("Card", sourceCard);
                    AllZone.getTriggerHandler().runTrigger(TriggerType.TurnFaceUp, runParams);
                }
            }

            @Override
            public boolean canPlay() {
                return sourceCard.getController().equals(this.getActivatingPlayer()) && sourceCard.isFaceDown()
                        && AllZoneUtil.isCardInPlay(sourceCard);
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

        AbilityFactory af = new AbilityFactory();
        SpellAbility cycle = af.getAbility(sb.toString(), sourceCard);
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

        AbilityFactory af = new AbilityFactory();
        SpellAbility cycle = af.getAbility(sb.toString(), sourceCard);
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
        final Cost abCost = new Cost(sourceCard, transmuteCost, true);
        class AbilityTransmute extends AbilityActivated {
            public AbilityTransmute(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityTransmute(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
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
                return super.canPlay() && PhaseHandler.canCastSorcery(sourceCard.getController());
            }

            @Override
            public void resolve() {
                final CardList cards = sourceCard.getController().getCardsIn(ZoneType.Library);
                final CardList sameCost = new CardList();

                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).getManaCost().getCMC() == sourceCard.getManaCost().getCMC()) {
                        sameCost.add(cards.get(i));
                    }
                }

                if (sameCost.size() == 0) {
                    return;
                }

                final Object o = GuiUtils.chooseOneOrNone("Select a card", sameCost.toArray());
                if (o != null) {
                    // ability.setTargetCard((Card)o);

                    sourceCard.getController().discard(sourceCard, this);
                    final Card c1 = (Card) o;

                    Singletons.getModel().getGameAction().moveToHand(c1);

                }
                sourceCard.getController().shuffle();
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
     * abilitySuspend.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param suspendCost
     *            a {@link java.lang.String} object.
     * @param suspendCounters
     *            a int.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility abilitySuspend(final Card sourceCard, final String suspendCost, final int suspendCounters) {
        // be careful with Suspend ability, it will not hit the stack
        final SpellAbility suspend = new AbilityStatic(sourceCard, suspendCost) {
            @Override
            public boolean canPlay() {
                if (!(this.getRestrictions().canPlay(sourceCard, this))) {
                    return false;
                }

                if (sourceCard.isInstant() || sourceCard.hasKeyword("Flash")) {
                    return true;
                }

                return PhaseHandler.canCastSorcery(sourceCard.getOwner());
            }

            @Override
            public boolean canPlayAI() {
                return true;
                // Suspend currently not functional for the AI,
                // seems to be an issue with regaining Priority after Suspension
            }

            @Override
            public void resolve() {
                final Card c = Singletons.getModel().getGameAction().exile(sourceCard);
                c.addCounter(Counters.TIME, suspendCounters);
            }
        };
        final StringBuilder sbDesc = new StringBuilder();
        sbDesc.append("Suspend ").append(suspendCounters).append(" - ").append(suspendCost);
        suspend.setDescription(sbDesc.toString());

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard.getName()).append(" suspending for ");
        sbStack.append(suspendCounters).append(" turns.)");
        suspend.setStackDescription(sbStack.toString());

        suspend.getRestrictions().setZone(ZoneType.Hand);
        return suspend;
    } // abilitySuspend()

    /**
     * <p>
     * eqPumpEquip.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param power
     *            a int.
     * @param tough
     *            a int.
     * @param extrinsicKeywords
     *            an array of {@link java.lang.String} objects.
     * @param abCost
     *            a {@link forge.card.cost.Cost} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility eqPumpEquip(final Card sourceCard, final int power, final int tough,
            final String[] extrinsicKeywords, final Cost abCost) {
        final Target target = new Target(sourceCard, "Select target creature you control",
                "Creature.YouCtrl".split(","));
        class AbilityEquip extends AbilityActivated {
            public AbilityEquip(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityEquip(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -4960704261761785512L;

            @Override
            public void resolve() {
                final Card targetCard = this.getTargetCard();
                if (AllZoneUtil.isCardInPlay(targetCard) && targetCard.canBeTargetedBy(this)) {

                    if (sourceCard.isEquipping()) {
                        final Card crd = sourceCard.getEquipping().get(0);
                        if (crd.equals(targetCard)) {
                            return;
                        }

                        sourceCard.unEquipCard(crd);
                    }
                    sourceCard.equipCard(targetCard);
                }
            }

            // An animated artifact equipmemt can't equip a creature
            @Override
            public boolean canPlay() {
                return super.canPlay() && !sourceCard.isCreature()
                        && PhaseHandler.canCastSorcery(sourceCard.getController());
            }

            @Override
            public boolean canPlayAI() {
                return (this.getCreature().size() != 0) && !sourceCard.isEquipping();
            }

            @Override
            public void chooseTargetAI() {
                final Card target = CardFactoryUtil.getBestCreatureAI(this.getCreature());
                this.setTargetCard(target);
            }

            CardList getCreature() {
                CardList list = AllZone.getComputerPlayer().getCardsIn(ZoneType.Battlefield);
                list = list.getTargetableCards(this).filter(new CardListFilter() {
                    @Override
                    public boolean addCard(final Card c) {
                        return c.isCreature()
                                && (CombatUtil.canAttack(c) || (CombatUtil.canAttackNextTurn(c)
                                && Singletons.getModel().getGameState().getPhaseHandler().is(PhaseType.MAIN2)))
                                && (((c.getNetDefense() + tough) > 0) || sourceCard.getName().equals("Skullclamp"));
                    }
                });

                // Is there at least 1 Loxodon Punisher and/or Goblin Gaveleer
                // to target
                CardList equipMagnetList = list;
                equipMagnetList = equipMagnetList.getEquipMagnets();

                if (!equipMagnetList.isEmpty() && (tough >= 0)) {
                    return equipMagnetList;
                }

                // This equipment is keyword only
                if ((power == 0) && (tough == 0)) {
                    list = list.filter(new CardListFilter() {
                        @Override
                        public boolean addCard(final Card c) {
                            final ArrayList<String> extKeywords = new ArrayList<String>(Arrays
                                    .asList(extrinsicKeywords));
                            for (final String s : extKeywords) {

                                // We want to give a new keyword
                                if (!c.hasKeyword(s)) {
                                    return true;
                                }
                            }
                            // no new keywords:
                            return false;
                        }
                    });
                }

                return list;
            } // getCreature()
        }
        final SpellAbility equip = new AbilityEquip(sourceCard, abCost, target); // equip ability

        String costDesc = abCost.toString();
        // get rid of the ": " at the end
        costDesc = costDesc.substring(0, costDesc.length() - 2);

        final StringBuilder sbDesc = new StringBuilder();
        sbDesc.append("Equip");
        if (!abCost.isOnlyManaCost()) {
            sbDesc.append(" -");
        }
        sbDesc.append(" ").append(costDesc);
        if (!abCost.isOnlyManaCost()) {
            sbDesc.append(".");
        }
        equip.setDescription(sbDesc.toString());

        return equip;
    } // eqPumpEquip()

    /**
     * <p>
     * eqPumpOnEquip.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param power
     *            a int.
     * @param tough
     *            a int.
     * @param extrinsicKeywords
     *            an array of {@link java.lang.String} objects.
     * @param abCost
     *            a {@link forge.card.cost.Cost} object.
     * @return a {@link forge.Command} object.
     */
    public static Command eqPumpOnEquip(final Card sourceCard, final int power, final int tough,
            final String[] extrinsicKeywords, final Cost abCost) {

        final Command onEquip = new Command() {

            private static final long serialVersionUID = 8130682765214560887L;

            @Override
            public void execute() {
                if (sourceCard.isEquipping()) {
                    final Card crd = sourceCard.getEquipping().get(0);

                    for (int i = 0; i < extrinsicKeywords.length; i++) {
                        // prevent Flying, Flying
                        if (!(extrinsicKeywords[i].equals("none")) && (!crd.hasKeyword(extrinsicKeywords[i]))) {
                            crd.addExtrinsicKeyword(extrinsicKeywords[i]);
                        }
                    }

                    crd.addSemiPermanentAttackBoost(power);
                    crd.addSemiPermanentDefenseBoost(tough);
                }
            } // execute()
        }; // Command

        return onEquip;
    } // eqPumpOnEquip

    /**
     * <p>
     * eqPumpUnEquip.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param power
     *            a int.
     * @param tough
     *            a int.
     * @param extrinsicKeywords
     *            an array of {@link java.lang.String} objects.
     * @param abCost
     *            a {@link forge.card.cost.Cost} object.
     * @return a {@link forge.Command} object.
     */
    public static Command eqPumpUnEquip(final Card sourceCard, final int power, final int tough,
            final String[] extrinsicKeywords, final Cost abCost) {

        final Command onUnEquip = new Command() {

            private static final long serialVersionUID = 5783423127748320501L;

            @Override
            public void execute() {
                if (sourceCard.isEquipping()) {
                    final Card crd = sourceCard.getEquipping().get(0);

                    for (final String extrinsicKeyword : extrinsicKeywords) {
                        crd.removeExtrinsicKeyword(extrinsicKeyword);
                    }

                    crd.addSemiPermanentAttackBoost(-1 * power);
                    crd.addSemiPermanentDefenseBoost(-1 * tough);

                }

            } // execute()
        }; // Command

        return onUnEquip;
    } // eqPumpUnEquip

    /**
     * <p>
     * entersBattleFieldWithCounters.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param type
     *            a {@link forge.Counters} object.
     * @param n
     *            a int.
     * @return a {@link forge.Command} object.
     */
    public static Command entersBattleFieldWithCounters(final Card c, final Counters type, final int n) {
        final Command addCounters = new Command() {
            private static final long serialVersionUID = 4825430555490333062L;

            @Override
            public void execute() {
                c.addCounter(type, n);
            }
        };
        return addCounters;
    }

    /**
     * <p>
     * fading.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param power
     *            a int.
     * @return a {@link forge.Command} object.
     */
    public static Command fading(final Card sourceCard, final int power) {
        return entersBattleFieldWithCounters(sourceCard, Counters.FADE, power);
    } // fading

    /**
     * <p>
     * vanishing.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param power
     *            a int.
     * @return a {@link forge.Command} object.
     */
    public static Command vanishing(final Card sourceCard, final int power) {
        return entersBattleFieldWithCounters(sourceCard, Counters.TIME, power);
    } // vanishing

    /**
     * <p>
     * abilitySoulshift.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param manacost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.Command} object.
     */
    public static Command abilitySoulshift(final Card sourceCard, final String manacost) {
        final Command soulshift = new Command() {
            private static final long serialVersionUID = -4960704261761785512L;

            @Override
            public void execute() {
                AllZone.getStack().add(CardFactoryUtil.soulshiftTrigger(sourceCard, manacost));
            }

        };

        return soulshift;
    } // abilitySoulshift()

    /**
     * <p>
     * soulshiftTrigger.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param manacost
     *            a {@link java.lang.String} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility soulshiftTrigger(final Card sourceCard, final String manacost) {
        final SpellAbility desc = new Ability(sourceCard, "0") {
            @Override
            public void resolve() {
                final CardList cards = sourceCard.getController().getCardsIn(ZoneType.Graveyard);
                final CardList sameCost = new CardList();
                final int cost = CardUtil.getConvertedManaCost(manacost);
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).getManaCost().getCMC() <= cost && cards.get(i).isType("Spirit")) {
                        sameCost.add(cards.get(i));
                    }
                }

                if (sameCost.size() == 0) {
                    return;
                }

                if (sourceCard.getController().isHuman()) {
                    final StringBuilder question = new StringBuilder();
                    question.append("Return target Spirit card with converted mana cost ");
                    question.append(manacost).append(" or less from your graveyard to your hand?");

                    if (GameActionUtil.showYesNoDialog(sourceCard, question.toString())) {
                        final Object o = GuiUtils.chooseOneOrNone("Select a card", sameCost.toArray());
                        if (o != null) {

                            final Card c1 = (Card) o;
                            Singletons.getModel().getGameAction().moveToHand(c1);
                        }
                    }
                } else {
                    // Wiser choice should be here
                    Card choice = null;
                    choice = CardFactoryUtil.getBestCreatureAI(sameCost);

                    if (!(choice == null)) {
                        Singletons.getModel().getGameAction().moveToHand(choice);
                    }
                }
            } // resolve()
        }; // SpellAbility desc

        // The spell description below fails to appear in the card detail panel
        final StringBuilder sbDesc = new StringBuilder();
        sbDesc.append("Soulshift ").append(manacost);
        sbDesc.append(" - When this permanent is put into a graveyard from play, ");
        sbDesc.append("you may return target Spirit card with converted mana cost ");
        sbDesc.append(manacost).append(" or less from your graveyard to your hand.");
        desc.setDescription(sbDesc.toString());

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(sourceCard.getName()).append(" - Soulshift ").append(manacost);
        desc.setStackDescription(sbStack.toString());

        return desc;
    } // soulshiftTrigger()

    // CardList choices are the only cards the user can successful select
    /**
     * <p>
     * inputTargetSpecific.
     * </p>
     * 
     * @param spell
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param choices
     *            a {@link forge.CardList} object.
     * @param message
     *            a {@link java.lang.String} object.
     * @param targeted
     *            a boolean.
     * @param free
     *            a boolean.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputTargetSpecific(final SpellAbility spell, final CardList choices, final String message,
            final boolean targeted, final boolean free) {
        return CardFactoryUtil.inputTargetSpecific(spell, choices, message, Command.BLANK, targeted, free);
    }

    // CardList choices are the only cards the user can successful select
    /**
     * <p>
     * inputTargetSpecific.
     * </p>
     * 
     * @param spell
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param choices
     *            a {@link forge.CardList} object.
     * @param message
     *            a {@link java.lang.String} object.
     * @param paid
     *            a {@link forge.Command} object.
     * @param targeted
     *            a boolean.
     * @param free
     *            a boolean.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputTargetSpecific(final SpellAbility spell, final CardList choices, final String message,
            final Command paid, final boolean targeted, final boolean free) {
        final Input target = new Input() {
            private static final long serialVersionUID = -1779224307654698954L;

            @Override
            public void showMessage() {
                CMatchUI.SINGLETON_INSTANCE.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.stop();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (targeted && !card.canBeTargetedBy(spell)) {
                    CMatchUI.SINGLETON_INSTANCE
                            .showMessage("Cannot target this card (Shroud? Protection?).");
                } else if (choices.contains(card)) {
                    spell.setTargetCard(card);
                    if (spell.getManaCost().equals("0") || free) {
                        this.setFree(false);
                        AllZone.getStack().add(spell);
                        this.stop();
                    } else {
                        this.stopSetNext(new InputPayManaCost(spell));
                    }

                    paid.execute();
                }
            } // selectCard()
        };
        return target;
    } // inputTargetSpecific()

    // CardList choices are the only cards the user can successful select
    /**
     * <p>
     * inputTargetChampionSac.
     * </p>
     * 
     * @param crd
     *            a {@link forge.Card} object.
     * @param spell
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param choices
     *            a {@link forge.CardList} object.
     * @param message
     *            a {@link java.lang.String} object.
     * @param targeted
     *            a boolean.
     * @param free
     *            a boolean.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputTargetChampionSac(final Card crd, final SpellAbility spell, final CardList choices,
            final String message, final boolean targeted, final boolean free) {
        final Input target = new Input() {
            private static final long serialVersionUID = -3320425330743678663L;

            @Override
            public void showMessage() {
                CMatchUI.SINGLETON_INSTANCE.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                Singletons.getModel().getGameAction().sacrifice(crd, null);
                this.stop();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (choices.contains(card)) {
                    if (card == spell.getSourceCard()) {
                        Singletons.getModel().getGameAction().sacrifice(spell.getSourceCard(), null);
                        this.stop();
                    } else {
                        spell.getSourceCard().setChampionedCard(card);
                        Singletons.getModel().getGameAction().exile(card);

                        this.stop();

                        // Run triggers
                        final HashMap<String, Object> runParams = new HashMap<String, Object>();
                        runParams.put("Card", spell.getSourceCard());
                        runParams.put("Championed", card);
                        AllZone.getTriggerHandler().runTrigger(TriggerType.Championed, runParams);
                    }
                }
            } // selectCard()
        };
        return target;
    } // inputTargetSpecific()

    /**
     * <p>
     * inputEquipCreature.
     * </p>
     * 
     * @param equip
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputEquipCreature(final SpellAbility equip) {
        final Input runtime = new Input() {
            private static final long serialVersionUID = 2029801495067540196L;

            @Override
            public void showMessage() {
                // get all creatures you control
                final CardList list = AllZoneUtil.getCreaturesInPlay(AllZone.getHumanPlayer());

                this.stopSetNext(CardFactoryUtil.inputTargetSpecific(equip, list, "Select target creature to equip",
                        true, false));
            }
        }; // Input
        return runtime;
    }

    /**
     * <p>
     * masterOfTheWildHuntInputTargetCreature.
     * </p>
     * 
     * @param spell
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param choices
     *            a {@link forge.CardList} object.
     * @param paid
     *            a {@link forge.Command} object.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input masterOfTheWildHuntInputTargetCreature(final SpellAbility spell, final CardList choices,
            final Command paid) {
        final Input target = new Input() {
            private static final long serialVersionUID = -1779224307654698954L;

            @Override
            public void showMessage() {
                final StringBuilder sb = new StringBuilder();
                sb.append("Select target wolf to damage for ").append(spell.getSourceCard());
                CMatchUI.SINGLETON_INSTANCE.showMessage(sb.toString());
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.stop();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (choices.size() == 0) {
                    this.stop();
                }
                if (choices.contains(card)) {
                    spell.setTargetCard(card);
                    paid.execute();
                    this.stop();
                }
            } // selectCard()
        };
        return target;
    } // masterOfTheWildHuntInputTargetCreature()

    /**
     * <p>
     * modularInput.
     * </p>
     * 
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param card
     *            a {@link forge.Card} object.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input modularInput(final SpellAbility ability, final Card card) {
        final Input modularInput = new Input() {

            private static final long serialVersionUID = 2322926875771867901L;

            @Override
            public void showMessage() {
                CMatchUI.SINGLETON_INSTANCE.showMessage("Select target artifact creature");
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.stop();
            }

            @Override
            public void selectCard(final Card card2, final PlayerZone zone) {
                if (card2.isCreature() && card2.isArtifact() && zone.is(ZoneType.Battlefield)
                        && card.canBeTargetedBy(ability)) {
                    ability.setTargetCard(card2);
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Put ").append(card.getCounters(Counters.P1P1));
                    sb.append(" +1/+1 counter/s from ").append(card);
                    sb.append(" on ").append(card2);
                    ability.setStackDescription(sb.toString());
                    AllZone.getStack().add(ability);
                    this.stop();
                }
            }
        };
        return modularInput;
    }

    /**
     * <p>
     * getHumanCreatureAI.
     * </p>
     * 
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.CardList} object.
     */
    public static CardList getHumanCreatureAI(final SpellAbility spell, final boolean targeted) {
        CardList creature = AllZoneUtil.getCreaturesInPlay(AllZone.getHumanPlayer());
        if (targeted) {
            creature = creature.getTargetableCards(spell);
        }
        return creature;
    }

    /**
     * <p>
     * getHumanCreatureAI.
     * </p>
     * 
     * @param toughness
     *            a int.
     * @param spell
     *            a {@link forge.Card} object.
     * @param targeted
     *            a boolean.
     * @return a {@link forge.CardList} object.
     */
    public static CardList getHumanCreatureAI(final int toughness, final SpellAbility spell, final boolean targeted) {
        CardList creature = AllZone.getHumanPlayer().getCardsIn(ZoneType.Battlefield);
        creature = creature.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                if (targeted) {
                    return c.isCreature() && (c.getNetDefense() <= toughness) && c.canBeTargetedBy(spell);
                } else {
                    return c.isCreature() && (c.getNetDefense() <= toughness);
                }
            }
        });
        return creature;
    } // AI_getHumanCreature()

    /**
     * <p>
     * targetHumanAI.
     * </p>
     * 
     * @return a {@link forge.CommandArgs} object.
     */
    public static CommandArgs targetHumanAI() {
        return new CommandArgs() {
            private static final long serialVersionUID = 8406907523134006697L;

            @Override
            public void execute(final Object o) {
                final SpellAbility sa = (SpellAbility) o;
                sa.setTargetPlayer(AllZone.getHumanPlayer());
            }
        };
    } // targetHumanAI()

    /**
     * <p>
     * getNumberOfPermanentsByColor.
     * </p>
     * 
     * @param color
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getNumberOfPermanentsByColor(final String color) {
        final CardList cards = AllZoneUtil.getCardsIn(ZoneType.Battlefield);

        final CardList coloredPerms = new CardList();

        for (int i = 0; i < cards.size(); i++) {
            if (CardUtil.getColors(cards.get(i)).contains(color)) {
                coloredPerms.add(cards.get(i));
            }
        }
        return coloredPerms.size();
    }

    /**
     * <p>
     * getNumberOfManaSymbolsControlledByColor.
     * </p>
     * 
     * @param colorAbb
     *            a {@link java.lang.String} object.
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @return a int.
     */
    public static int getNumberOfManaSymbolsControlledByColor(final String colorAbb, final Player player) {
        final CardList cards = player.getCardsIn(ZoneType.Battlefield);
        return CardFactoryUtil.getNumberOfManaSymbolsByColor(colorAbb, cards);
    }

    /**
     * <p>
     * getNumberOfManaSymbolsByColor.
     * </p>
     * 
     * @param colorAbb
     *            a {@link java.lang.String} object.
     * @param cards
     *            a {@link forge.CardList} object.
     * @return a int.
     */
    public static int getNumberOfManaSymbolsByColor(final String colorAbb, final CardList cards) {
        int count = 0;
        for (int i = 0; i < cards.size(); i++) {
            final Card c = cards.get(i);
            if (!c.isToken()) {
                String manaCost = c.getManaCost().toString();
                manaCost = manaCost.trim();
                count += CardFactoryUtil.countOccurrences(manaCost, colorAbb);
            }
        }
        return count;
    }

    /**
     * <p>
     * multiplyManaCost.
     * </p>
     * 
     * @param manacost
     *            a {@link java.lang.String} object.
     * @param multiplier
     *            a int.
     * @return a {@link java.lang.String} object.
     */
    public static String multiplyManaCost(final String manacost, final int multiplier) {
        if (multiplier == 0) {
            return "";
        }
        if (multiplier == 1) {
            return manacost;
        }

        final String[] tokenized = manacost.split("\\s");
        final StringBuilder sb = new StringBuilder();

        if (Character.isDigit(tokenized[0].charAt(0))) {
            // manacost starts with
            // "colorless" number
            // cost
            int cost = Integer.parseInt(tokenized[0]);
            cost = multiplier * cost;
            tokenized[0] = "" + cost;
            sb.append(tokenized[0]);
        } else {
            for (int i = 0; i < multiplier; i++) {
                // tokenized[0] = tokenized[0] + " " + tokenized[0];
                sb.append((" "));
                sb.append(tokenized[0]);
            }
        }

        for (int i = 1; i < tokenized.length; i++) {
            for (int j = 0; j < multiplier; j++) {
                // tokenized[i] = tokenized[i] + " " + tokenized[i];
                sb.append((" "));
                sb.append(tokenized[i]);

            }
        }

        String result = sb.toString();
        System.out.println("result: " + result);
        result = result.trim();
        return result;
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

        if (AllZone.getZoneOf(target) == null) {
            return false; // for tokens that disappeared
        }

        final Card source = ability.getSourceCard();
        final Target tgt = ability.getTarget();
        if (tgt != null) {
            // Reconfirm the Validity of a TgtValid, or if the Creature is still
            // a Creature
            if (tgt.doesTarget()
                    && !target.isValid(tgt.getValidTgts(), ability.getActivatingPlayer(), ability.getSourceCard())) {
                return false;
            }

            // Check if the target is in the zone it needs to be in to be
            // targeted
            if (!AllZone.getZoneOf(target).is(tgt.getZone())) {
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

    /**
     * <p>
     * isColored.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean isColored(final Card c) {
        return c.isWhite() || c.isBlue() || c.isBlack() || c.isRed() || c.isGreen();
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
        if (!CardFactoryUtil.isCounterable(c)) {
            return false;
        }
        //TODO: Add code for Autumn's Veil here

        return true;
    }

    // returns the number of equipments named "e" card c is equipped by
    /**
     * <p>
     * Gets the number of equipments with a given name that a given card is
     * equipped by.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param name
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int hasNumberEquipments(final Card card, final String name) {
        if (!card.isEquipped()) {
            return 0;
        }

        CardList list = new CardList(card.getEquippedBy());
        list = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.getName().equals(name);
            }

        });

        return list.size();

    }

    /**
     * <p>
     * getExternalZoneActivationCards.
     * </p>
     * 
     * @param activator
     *            a {@link forge.game.player.Player} object.
     * @return a {@link forge.CardList} object.
     */
    public static CardList getExternalZoneActivationCards(final Player activator) {
        final CardList cl = new CardList();
        final Player opponent = activator.getOpponent();

        cl.addAll(CardFactoryUtil.getActivateablesFromZone(activator.getZone(ZoneType.Graveyard), activator));
        cl.addAll(CardFactoryUtil.getActivateablesFromZone(activator.getZone(ZoneType.Exile), activator));
        cl.addAll(CardFactoryUtil.getActivateablesFromZone(activator.getZone(ZoneType.Library), activator));
        cl.addAll(CardFactoryUtil.getActivateablesFromZone(activator.getZone(ZoneType.Command), activator));
        cl.addAll(CardFactoryUtil.getActivateablesFromZone(opponent.getZone(ZoneType.Exile), activator));
        cl.addAll(CardFactoryUtil.getActivateablesFromZone(opponent.getZone(ZoneType.Graveyard), activator));

        return cl;
    }

    /**
     * <p>
     * getActivateablesFromZone.
     * </p>
     * 
     * @param zone
     *            a PlayerZone object.
     * @param activator
     *            a {@link forge.game.player.Player} object.
     * @return a boolean.
     */
    public static CardList getActivateablesFromZone(final PlayerZone zone, final Player activator) {

        CardList cl = new CardList(zone.getCards());

        // Only check the top card of the library
        if (zone.is(ZoneType.Library) && !cl.isEmpty()) {
            cl = new CardList(cl.get(0));
        }

        if (activator.isPlayer(zone.getPlayer())) {
            cl = cl.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    if (zone.is(ZoneType.Graveyard)) {
                        if (c.hasUnearth()) {
                            return true;
                        }
                    }
                    if (c.hasKeyword("You may look at this card.")) {
                        return true;
                    }

                    if (c.isLand()
                            && (c.hasKeyword("May be played") || c.hasKeyword("May be played without paying its mana cost"))) {
                        return true;
                    }

                    for (final SpellAbility sa : c.getSpellAbility()) {
                        final ZoneType restrictZone = sa.getRestrictions().getZone();
                        if (zone.is(restrictZone)) {
                            return true;
                        }

                        if (sa.isSpell()
                                && (c.hasKeyword("May be played") || c.hasKeyword("May be played without paying its mana cost")
                                        || (c.hasStartOfKeyword("Flashback") && zone.is(ZoneType.Graveyard)))
                                && restrictZone.equals(ZoneType.Hand)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        } else {
            // the activator is not the owner of the card
            cl = cl.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {

                    if (c.hasStartOfKeyword("May be played by your opponent")
                            || c.hasKeyword("Your opponent may look at this card.")) {
                        return true;
                    }
                    return false;
                }
            });
        }
        return cl;
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
    public static String[] parseMath(final String[] l) {
        final String[] m = { "none" };
        if (l.length > 1) {
            m[0] = l[1];
        }

        return m;
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
    public static int playerXCount(final ArrayList<Player> players, final String s, final Card source) {
        if (players.size() == 0) {
            return 0;
        }

        final String[] l = s.split("/");
        final String[] m = CardFactoryUtil.parseMath(l);

        int n = 0;

        // count valid cards on the battlefield
        if (l[0].contains("Valid")) {
            final String restrictions = l[0].replace("Valid ", "");
            final String[] rest = restrictions.split(",");
            CardList cardsonbattlefield = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
            cardsonbattlefield = cardsonbattlefield.getValidCards(rest, players.get(0), source);

            n = cardsonbattlefield.size();

            return CardFactoryUtil.doXMath(n, m, source);
        }

        final String[] sq;
        sq = l[0].split("\\.");

        if (sq[0].contains("CardsInHand")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Hand).size(), m, source);
            }
        }

        if (sq[0].contains("DomainPlayer")) {
            final CardList someCards = new CardList();
            someCards.addAll(players.get(0).getCardsIn(ZoneType.Battlefield));
            final String[] basic = { "Forest", "Plains", "Mountain", "Island", "Swamp" };

            for (int i = 0; i < basic.length; i++) {
                if (!someCards.getType(basic[i]).isEmpty()) {
                    n++;
                }
            }
            return CardFactoryUtil.doXMath(n, m, source);
        }

        if (sq[0].contains("CardsInLibrary")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Library).size(), m, source);
            }
        }

        if (sq[0].contains("CardsInGraveyard")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Graveyard).size(), m, source);
            }
        }
        if (sq[0].contains("LandsInGraveyard")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Graveyard).getType("Land").size(), m,
                        source);
            }
        }

        if (sq[0].contains("CreaturesInPlay")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(AllZoneUtil.getCreaturesInPlay(players.get(0)).size(), m, source);
            }
        }

        if (sq[0].contains("CardsInPlay")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Battlefield).size(), m, source);
            }
        }

        if (sq[0].contains("LifeTotal")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getLife(), m, source);
            }
        }

        if (sq[0].contains("TopOfLibraryCMC")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getCardsIn(ZoneType.Library, 1).getTotalConvertedManaCost(),
                        m, source);
            }
        }

        if (sq[0].contains("LandsPlayed")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getNumLandsPlayed(), m, source);
            }
        }

        if (sq[0].contains("CardsDrawn")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getNumDrawnThisTurn(), m, source);
            }
        }

        if (sq[0].contains("AttackersDeclared")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(players.get(0).getAttackersDeclaredThisTurn(), m, source);
            }
        }

        if (sq[0].equals("DamageDoneToPlayerBy")) {
            if (players.size() > 0) {
                return CardFactoryUtil.doXMath(source.getDamageDoneToPlayerBy(players.get(0).getName()), m, source);
            }
        }

        return CardFactoryUtil.doXMath(n, m, source);
    }

    /**
     * parseSVar TODO - flesh out javadoc for this method.
     * 
     * @param hostCard
     *            the Card with the SVar on it
     * @param amount
     *            a String
     * @return the calculated number
     */
    public static int parseSVar(final Card hostCard, final String amount) {
        int num = 0;
        if (amount == null) {
            return num;
        }

        try {
            num = Integer.valueOf(amount);
        } catch (final NumberFormatException e) {
            num = CardFactoryUtil.xCount(hostCard, hostCard.getSVar(amount).split("\\$")[1]);
        }

        return num;
    }

    /**
     * <p>
     * Parse non-mana X variables.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param s
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int xCount(final Card c, final String s) {
        int n = 0;

        final Player cardController = c.getController();
        final Player oppController = cardController.getOpponent();

        final String[] l = s.split("/");
        final String[] m = CardFactoryUtil.parseMath(l);

        // accept straight numbers
        if (l[0].contains("Number$")) {
            final String number = l[0].replace("Number$", "");
            if (number.equals("ChosenNumber")) {
                return CardFactoryUtil.doXMath(c.getChosenNumber(), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(number), m, c);
            }
        }

        if (l[0].startsWith("SVar$")) {
            final String sVar = l[0].replace("SVar$", "");
            return CardFactoryUtil.doXMath(CardFactoryUtil.xCount(c, c.getSVar(sVar)), m, c);
        }

        // Manapool
        if (l[0].contains("ManaPool")) {
            final String color = l[0].split(":")[1];
            if (color.equals("All")) {
                return c.getController().getManaPool().totalMana();
            } else {
                return c.getController().getManaPool().getAmountOfColor(color);
            }
        }

        // count valid cards in the garveyard
        if (l[0].contains("ValidGrave")) {
            String restrictions = l[0].replace("ValidGrave ", "");
            restrictions = restrictions.replace("Count$", "");
            final String[] rest = restrictions.split(",");
            CardList cards = AllZoneUtil.getCardsIn(ZoneType.Graveyard);
            cards = cards.getValidCards(rest, cardController, c);

            n = cards.size();

            return CardFactoryUtil.doXMath(n, m, c);
        }
        // count valid cards on the battlefield
        if (l[0].contains("Valid")) {
            String restrictions = l[0].replace("Valid ", "");
            restrictions = restrictions.replace("Count$", "");
            final String[] rest = restrictions.split(",");
            CardList cardsonbattlefield = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
            cardsonbattlefield = cardsonbattlefield.getValidCards(rest, cardController, c);

            n = cardsonbattlefield.size();

            return CardFactoryUtil.doXMath(n, m, c);
        }

        if (l[0].contains("ImprintedCardPower")) {
            if (c.getImprinted().size() > 0) {
                return c.getImprinted().get(0).getNetAttack();
            }
        }

        if (l[0].contains("ImprintedCardToughness")) {
            if (c.getImprinted().size() > 0) {
                return c.getImprinted().get(0).getNetDefense();
            }
        }

        if (l[0].contains("ImprintedCardManaCost")) {
            if (c.getImprinted().get(0).getCMC() > 0) {
                return c.getImprinted().get(0).getCMC();
            }
        }

        if (l[0].contains("GreatestPowerYouControl")) {
            final CardList list = AllZoneUtil.getCreaturesInPlay(c.getController());
            int highest = 0;
            for (final Card crd : list) {
                if (crd.getNetAttack() > highest) {
                    highest = crd.getNetAttack();
                }
            }
            return highest;
        }

        if (l[0].contains("GreatestPowerYouDontControl")) {
            final CardList list = AllZoneUtil.getCreaturesInPlay(c.getController().getOpponent());
            int highest = 0;
            for (final Card crd : list) {
                if (crd.getNetAttack() > highest) {
                    highest = crd.getNetAttack();
                }
            }
            return highest;
        }

        if (l[0].contains("HighestCMCRemembered")) {
            final CardList list = new CardList();
            int highest = 0;
            for (final Object o : c.getRemembered()) {
                if (o instanceof Card) {
                    list.add(AllZoneUtil.getCardState((Card) o));
                }
            }
            for (final Card crd : list) {
                if (crd.getCMC() > highest) {
                    highest = crd.getCMC();
                }
            }
            return highest;
        }

        if (l[0].contains("RememberedSumPower")) {
            final CardList list = new CardList();
            for (final Object o : c.getRemembered()) {
                if (o instanceof Card) {
                    list.add(AllZoneUtil.getCardState((Card) o));
                }
            }
            return list.getTotalCreaturePower();
        }

        if (l[0].contains("RememberedSize")) {
            return c.getRemembered().size();
        }

        final String[] sq;
        sq = l[0].split("\\.");

        if (sq[0].contains("xPaid")) {
            return CardFactoryUtil.doXMath(c.getXManaCostPaid(), m, c);
        }

        if (sq[0].contains("xLifePaid")) {
            if (c.getController().isHuman()) {
                return c.getXLifePaid();
            } else {
                // copied for xPaid
                // not implemented for Compy
                // int dam = ComputerUtil.getAvailableMana().size()-
                // CardUtil.getConvertedManaCost(c);
                // if (dam < 0) dam = 0;
                // return dam;
                return 0;
            }
        }

        if (sq[0].equals("YouDrewThisTurn")) {
            return CardFactoryUtil.doXMath(c.getController().getNumDrawnThisTurn(), m, c);
        }
        if (sq[0].equals("OppDrewThisTurn")) {
            return CardFactoryUtil.doXMath(c.getController().getOpponent().getNumDrawnThisTurn(), m, c);
        }

        if (sq[0].equals("StormCount")) {
            return CardFactoryUtil.doXMath(AllZone.getStack().getCardsCastThisTurn().size() - 1, m, c);
        }

        if (sq[0].equals("DamageDoneThisTurn")) {
            return CardFactoryUtil.doXMath(c.getDamageDoneThisTurn(), m, c);
        }

        if (sq[0].contains("RegeneratedThisTurn")) {
            return CardFactoryUtil.doXMath(c.getRegeneratedThisTurn(), m, c);
        }

        CardList someCards = new CardList();

        // Complex counting methods

        // TriggeringObjects
        if (sq[0].startsWith("Triggered")) {
            return CardFactoryUtil.doXMath((Integer) c.getTriggeringObject(sq[0].substring(9)), m, c);
        }

        // Count$Domain
        if (sq[0].contains("Domain")) {
            someCards.addAll(cardController.getCardsIn(ZoneType.Battlefield));
            final String[] basic = { "Forest", "Plains", "Mountain", "Island", "Swamp" };

            for (int i = 0; i < basic.length; i++) {
                if (!someCards.getType(basic[i]).isEmpty()) {
                    n++;
                }
            }
            return CardFactoryUtil.doXMath(n, m, c);
        }

        // Count$OpponentDom
        if (sq[0].contains("OpponentDom")) {
            someCards.addAll(cardController.getOpponent().getCardsIn(ZoneType.Battlefield));
            final String[] basic = { "Forest", "Plains", "Mountain", "Island", "Swamp" };

            for (int i = 0; i < basic.length; i++) {
                if (!someCards.getType(basic[i]).isEmpty()) {
                    n++;
                }
            }
            return CardFactoryUtil.doXMath(n, m, c);
        }

        // Count$ColoredCreatures *a DOMAIN for creatures*
        if (sq[0].contains("ColoredCreatures")) {
            someCards.addAll(cardController.getCardsIn(ZoneType.Battlefield));
            someCards = someCards.filter(CardListFilter.CREATURES);

            final String[] colors = { "green", "white", "red", "blue", "black" };

            for (final String color : colors) {
                if (someCards.getColor(color).size() > 0) {
                    n++;
                }
            }
            return CardFactoryUtil.doXMath(n, m, c);
        }

        // Count$YourStartingLife
        if (sq[0].contains("YourStartingLife")) {
            return CardFactoryUtil.doXMath(cardController.getStartingLife(), m, c);
        }

        // Count$OppStartingLife
        if (sq[0].contains("OppStartingLife")) {
            return CardFactoryUtil.doXMath(oppController.getStartingLife(), m, c);
        }

        // Count$YourLifeTotal
        if (sq[0].contains("YourLifeTotal")) {
            return CardFactoryUtil.doXMath(cardController.getLife(), m, c);
        }

        // Count$OppLifeTotal
        if (sq[0].contains("OppLifeTotal")) {
            return CardFactoryUtil.doXMath(oppController.getLife(), m, c);
        }

        //  Count$TargetedLifeTotal (targeted player's life total)
        if (sq[0].contains("TargetedLifeTotal")) {
            for (final SpellAbility sa : c.getCharacteristics().getSpellAbility()) {
                final SpellAbility parent = AbilityFactory.findParentsTargetedPlayer(sa);
                if (parent != null) {
                    if (parent.getTarget() != null) {
                        for (final Object tgtP : parent.getTarget().getTargetPlayers()) {
                            if (tgtP instanceof Player) {
                                return CardFactoryUtil.doXMath(((Player) tgtP).getLife(), m, c);
                            }
                        }
                    }
                }
            }
        }

        if (sq[0].contains("LifeYouLostThisTurn")) {
            return CardFactoryUtil.doXMath(cardController.getLifeLostThisTurn(), m, c);
        }

        if (sq[0].contains("LifeOppLostThisTurn")) {
            return CardFactoryUtil.doXMath(cardController.getOpponent().getLifeLostThisTurn(), m, c);
        }

        if (sq[0].equals("TotalDamageDoneByThisTurn")) {
            return CardFactoryUtil.doXMath(c.getTotalDamageDoneBy(), m, c);
        }

        // Count$YourPoisonCounters
        if (sq[0].contains("YourPoisonCounters")) {
            return CardFactoryUtil.doXMath(cardController.getPoisonCounters(), m, c);
        }

        // Count$OppPoisonCounters
        if (sq[0].contains("OppPoisonCounters")) {
            return CardFactoryUtil.doXMath(oppController.getPoisonCounters(), m, c);
        }

        // Count$OppDamageThisTurn
        if (sq[0].contains("OppDamageThisTurn")) {
            return CardFactoryUtil.doXMath(c.getController().getOpponent().getAssignedDamage(), m, c);
        }

        // Count$YourDamageThisTurn
        if (sq[0].contains("YourDamageThisTurn")) {
            return CardFactoryUtil.doXMath(c.getController().getAssignedDamage(), m, c);
        }

        // Count$YourTypeDamageThisTurn Type
        if (sq[0].contains("OppTypeDamageThisTurn")) {
            final String[] type = sq[0].split(" ");
            return CardFactoryUtil.doXMath(c.getController().getOpponent().getAssignedDamage(type[1]), m, c);
        }

        // Count$YourTypeDamageThisTurn Type
        if (sq[0].contains("YourTypeDamageThisTurn")) {
            final String[] type = sq[0].split(" ");
            return CardFactoryUtil.doXMath(c.getController().getAssignedDamage(type[1]), m, c);
        }

        if (sq[0].contains("YourLandsPlayed")) {
            return CardFactoryUtil.doXMath(c.getController().getNumLandsPlayed(), m, c);
        }

        // Count$HighestLifeTotal
        if (sq[0].contains("HighestLifeTotal")) {
            return CardFactoryUtil.doXMath(
                    Math.max(AllZone.getHumanPlayer().getLife(), AllZone.getComputerPlayer().getLife()), m, c);
        }

        // Count$LowestLifeTotal
        if (sq[0].contains("LowestLifeTotal")) {
            return CardFactoryUtil.doXMath(
                    Math.min(AllZone.getHumanPlayer().getLife(), AllZone.getComputerPlayer().getLife()), m, c);
        }

        // Count$TopOfLibraryCMC
        if (sq[0].contains("TopOfLibraryCMC")) {
            final CardList topcard = cardController.getCardsIn(ZoneType.Library, 1);
            return CardFactoryUtil.doXMath(topcard.getTotalConvertedManaCost(), m, c);
        }

        // Count$EnchantedControllerCreatures
        if (sq[0].contains("EnchantedControllerCreatures")) {
            CardList enchantedControllerInPlay = new CardList();
            if (c.getEnchantingCard() != null) {
                enchantedControllerInPlay = c.getEnchantingCard().getController().getCardsIn(ZoneType.Battlefield);
                enchantedControllerInPlay = enchantedControllerInPlay.getType("Creature");
            }
            return enchantedControllerInPlay.size();
        }

        // Count$LowestLibrary
        if (sq[0].contains("LowestLibrary")) {
            return Math.min(AllZone.getHumanPlayer().getZone(ZoneType.Library).size(),
                    AllZone.getComputerPlayer().getZone(ZoneType.Library).size());
        }

        // Count$Chroma.<mana letter>
        if (sq[0].contains("Chroma")) {
            return CardFactoryUtil.doXMath(
                    CardFactoryUtil.getNumberOfManaSymbolsControlledByColor(sq[1], cardController), m, c);
        }

        // Count$Hellbent.<numHB>.<numNotHB>
        if (sq[0].contains("Hellbent")) {
            if (cardController.hasHellbent()) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c); // Hellbent
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c); // not
                                                                               // Hellbent
            }
        }

        // Count$Metalcraft.<numMC>.<numNotMC>
        if (sq[0].contains("Metalcraft")) {
            if (cardController.hasMetalcraft()) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // Count$FatefulHour.<numFH>.<numNotFH>
        if (sq[0].contains("FatefulHour")) {
            if (cardController.getLife() <= 5) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // Count$wasCastFrom<Zone>.<true>.<false>
        if (sq[0].startsWith("wasCastFrom")) {
            final String strZone = sq[0].substring(11);
            final ZoneType realZone = ZoneType.smartValueOf(strZone);
            if (c.getCastFrom() == realZone) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        if (sq[0].contains("Threshold")) {
            if (cardController.hasThreshold()) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        if (sq[0].contains("Landfall")) {
            if (cardController.hasLandfall()) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        if (sq[0].contains("GraveyardWithGE20Cards")) {
            if (Math.max(AllZone.getHumanPlayer().getZone(ZoneType.Graveyard).size(),
                    AllZone.getComputerPlayer().getZone(ZoneType.Graveyard).size()) >= 20) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        if (sq[0].startsWith("Devoured")) {
            final String validDevoured = l[0].split(" ")[1];
            final Card csource = c;
            CardList cl = c.getDevoured();

            cl = cl.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card cdev) {
                    return cdev.isValid(validDevoured.split(","), csource.getController(), csource);
                }
            });

            return CardFactoryUtil.doXMath(cl.size(), m, c);
        }

        // Count$CardPower
        if (sq[0].contains("CardPower")) {
            return CardFactoryUtil.doXMath(c.getNetAttack(), m, c);
        }
        // Count$CardToughness
        if (sq[0].contains("CardToughness")) {
            return CardFactoryUtil.doXMath(c.getNetDefense(), m, c);
        }
        // Count$CardPowerPlusToughness
        if (sq[0].contains("CardSumPT")) {
            return CardFactoryUtil.doXMath((c.getNetAttack() + c.getNetDefense()), m, c);
        }
        // Count$SumPower_valid
        if (sq[0].contains("SumPower")) {
            final String[] restrictions = l[0].split("_");
            final String[] rest = restrictions[1].split(",");
            CardList cardsonbattlefield = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
            CardList filteredCards = cardsonbattlefield.getValidCards(rest, cardController, c);
            int sumPower = 0;
            for (int i = 0; i < filteredCards.size(); i++) {
                sumPower += filteredCards.get(i).getManaCost().getCMC();
            }
            return sumPower;
        }
        // Count$CardManaCost
        if (sq[0].contains("CardManaCost")) {
            if (sq[0].contains("Equipped") && c.isEquipping()) {
                return CardFactoryUtil.doXMath(CardUtil.getConvertedManaCost(c.getEquipping().get(0)), m, c);
            } else {
                return CardFactoryUtil.doXMath(CardUtil.getConvertedManaCost(c), m, c);
            }
        }
        // Count$SumCMC_valid
        if (sq[0].contains("SumCMC")) {
            final String[] restrictions = l[0].split("_");
            final String[] rest = restrictions[1].split(",");
            CardList cardsonbattlefield = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
            CardList filteredCards = cardsonbattlefield.getValidCards(rest, cardController, c);
            return CardListUtil.sumCMC(filteredCards);
        }
        // Count$CardNumColors
        if (sq[0].contains("CardNumColors")) {
            return CardFactoryUtil.doXMath(CardUtil.getColors(c).size(), m, c);
        }
        // Count$ChosenNumber
        if (sq[0].contains("ChosenNumber")) {
            return CardFactoryUtil.doXMath(c.getChosenNumber(), m, c);
        }
        // Count$CardCounters.<counterType>
        if (sq[0].contains("CardCounters")) {
            return CardFactoryUtil.doXMath(c.getCounters(Counters.getType(sq[1])), m, c);
        }
        // Count$TimesKicked
        if (sq[0].contains("TimesKicked")) {
            return CardFactoryUtil.doXMath(c.getMultiKickerMagnitude(), m, c);
        }
        if (sq[0].contains("NumCounters")) {
            final int num = c.getCounters(Counters.getType(sq[1]));
            return CardFactoryUtil.doXMath(num, m, c);
        }

        // Count$IfMainPhase.<numMain>.<numNotMain> // 7/10
        if (sq[0].contains("IfMainPhase")) {
            final PhaseHandler cPhase = Singletons.getModel().getGameState().getPhaseHandler();
            if (cPhase.getPhase().isMain() && cPhase.getPlayerTurn().equals(cardController)) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // Count$M12Empires.<numIf>.<numIfNot>
        if (sq[0].contains("AllM12Empires")) {
            boolean has = AllZoneUtil.isCardInPlay("Crown of Empires", c.getController());
            has &= AllZoneUtil.isCardInPlay("Scepter of Empires", c.getController());
            has &= AllZoneUtil.isCardInPlay("Throne of Empires", c.getController());
            if (has) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // Count$ThisTurnEntered <ZoneDestination> <ZoneOrigin> <Valid>
        // or
        // Count$ThisTurnEntered <ZoneDestination> <Valid>
        if (sq[0].contains("ThisTurnEntered")) {
            final String[] workingCopy = l[0].split("_");
            ZoneType destination, origin;
            String validFilter;

            destination = ZoneType.smartValueOf(workingCopy[1]);
            if (workingCopy[2].equals("from")) {
                origin = ZoneType.smartValueOf(workingCopy[3]);
                validFilter = workingCopy[4];
            } else {
                origin = null;
                validFilter = workingCopy[2];
            }

            final CardList res = CardUtil.getThisTurnEntered(destination, origin, validFilter, c);

            return CardFactoryUtil.doXMath(res.size(), m, c);
        }

        // Count$AttackersDeclared
        if (sq[0].contains("AttackersDeclared")) {
            return CardFactoryUtil.doXMath(cardController.getAttackersDeclaredThisTurn(), m, c);
        }

        // Count$ThisTurnCast <Valid>
        // Count$LastTurnCast <Valid>
        if (sq[0].contains("ThisTurnCast") || sq[0].contains("LastTurnCast")) {

            final String[] workingCopy = l[0].split("_");
            final String validFilter = workingCopy[1];

            CardList res;

            if (workingCopy[0].contains("This")) {
                res = CardUtil.getThisTurnCast(validFilter, c);
            } else {
                res = CardUtil.getLastTurnCast(validFilter, c);
            }

            final int ret = CardFactoryUtil.doXMath(res.size(), m, c);
            return ret;
        }

        // Count$Morbid.<True>.<False>
        if (sq[0].startsWith("Morbid")) {
            final CardList res = CardUtil.getThisTurnEntered(ZoneType.Graveyard, ZoneType.Battlefield, "Creature", c);
            if (res.size() > 0) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // Generic Zone-based counting
        // Count$QualityAndZones.Subquality

        // build a list of cards in each possible specified zone

        // if a card was ever written to count two different zones,
        // make sure they don't get added twice.
        boolean mf = false, my = false, mh = false;
        boolean of = false, oy = false, oh = false;

        if (sq[0].contains("YouCtrl")) {
            if (!mf) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Battlefield));
                mf = true;
            }
        }

        if (sq[0].contains("InYourYard")) {
            if (!my) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Graveyard));
                my = true;
            }
        }

        if (sq[0].contains("InYourLibrary")) {
            if (!my) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Library));
                my = true;
            }
        }

        if (sq[0].contains("InYourHand")) {
            if (!mh) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Hand));
                mh = true;
            }
        }

        if (sq[0].contains("OppCtrl")) {
            if (!of) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Battlefield));
                of = true;
            }
        }

        if (sq[0].contains("InOppYard")) {
            if (!oy) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Graveyard));
                oy = true;
            }
        }

        if (sq[0].contains("InOppHand")) {
            if (!oh) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Hand));
                oh = true;
            }
        }

        if (sq[0].contains("OnBattlefield")) {
            if (!mf) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Battlefield));
            }
            if (!of) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Battlefield));
            }
        }

        if (sq[0].contains("InAllYards")) {
            if (!my) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Graveyard));
            }
            if (!oy) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Graveyard));
            }
        }

        if (sq[0].contains("SpellsOnStack")) {
            someCards.addAll(AllZoneUtil.getCardsIn(ZoneType.Stack));
        }

        if (sq[0].contains("InAllHands")) {
            if (!mh) {
                someCards.addAll(cardController.getCardsIn(ZoneType.Hand));
            }
            if (!oh) {
                someCards.addAll(oppController.getCardsIn(ZoneType.Hand));
            }
        }

        //  Count$InTargetedHand (targeted player's cards in hand)
        if (sq[0].contains("InTargetedHand")) {
            for (final SpellAbility sa : c.getCharacteristics().getSpellAbility()) {
                final SpellAbility parent = AbilityFactory.findParentsTargetedPlayer(sa);
                if (parent != null) {
                    if (parent.getTarget() != null) {
                        for (final Object tgtP : parent.getTarget().getTargetPlayers()) {
                            if (tgtP instanceof Player) {
                                someCards.addAll(((Player) tgtP).getCardsIn(ZoneType.Hand));
                            }
                        }
                    }
                }
            }
        }

        // filter lists based on the specified quality

        // "Clerics you control" - Count$TypeYouCtrl.Cleric
        if (sq[0].contains("Type")) {
            someCards = someCards.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    if (c.isType(sq[1])) {
                        return true;
                    }

                    return false;
                }
            });
        }

        // "Named <CARDNAME> in all graveyards" - Count$NamedAllYards.<CARDNAME>

        if (sq[0].contains("Named")) {
            if (sq[1].equals("CARDNAME")) {
                sq[1] = c.getName();
            }

            someCards = someCards.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    if (c.getName().equals(sq[1])) {
                        return true;
                    }

                    return false;
                }
            });
        }

        // Refined qualities

        // "Untapped Lands" - Count$UntappedTypeYouCtrl.Land
        if (sq[0].contains("Untapped")) {
            someCards = someCards.filter(CardListFilter.UNTAPPED);
        }

        if (sq[0].contains("Tapped")) {
            someCards = someCards.filter(CardListFilter.TAPPED);
        }

        // "White Creatures" - Count$WhiteTypeYouCtrl.Creature
        if (sq[0].contains("White")) {
            someCards = someCards.filter(CardListFilter.WHITE);
        }

        if (sq[0].contains("Blue")) {
            someCards = someCards.filter(CardListFilter.BLUE);
        }

        if (sq[0].contains("Black")) {
            someCards = someCards.filter(CardListFilter.BLACK);
        }

        if (sq[0].contains("Red")) {
            someCards = someCards.filter(CardListFilter.RED);
        }

        if (sq[0].contains("Green")) {
            someCards = someCards.filter(CardListFilter.GREEN);
        }

        if (sq[0].contains("Multicolor")) {
            someCards = someCards.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return (CardUtil.getColors(c).size() > 1);
                }
            });
        }

        if (sq[0].contains("Monocolor")) {
            someCards = someCards.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return (CardUtil.getColors(c).size() == 1);
                }
            });
        }

        // Count$CardMulticolor.<numMC>.<numNotMC>
        if (sq[0].contains("CardMulticolor")) {
            if (CardUtil.getColors(c).size() > 1) {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
            } else {
                return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
            }
        }

        // 1/10 - Count$MaxCMCYouCtrl
        if (sq[0].contains("MaxCMC")) {
            int mmc = 0;
            int cmc = 0;
            for (int i = 0; i < someCards.size(); i++) {
                cmc = someCards.getCard(i).getManaCost().getCMC();
                if (cmc > mmc) {
                    mmc = cmc;
                }
            }

            return CardFactoryUtil.doXMath(mmc, m, c);
        }

        n = someCards.size();

        return CardFactoryUtil.doXMath(n, m, c);
    }

    private static int doXMath(final int num, final String m, final Card c) {
        if (m.equals("none")) {
            return num;
        }

        final String[] s = m.split("\\.");
        int secondaryNum = 0;

        try {
            if (s.length == 2) {
                secondaryNum = Integer.parseInt(s[1]);
            }
        } catch (final Exception e) {
            secondaryNum = CardFactoryUtil.xCount(c, c.getSVar(s[1]));
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
     * doXMath.
     * </p>
     * 
     * @param num
     *            a int.
     * @param m
     *            an array of {@link java.lang.String} objects.
     * @param c
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int doXMath(final int num, final String[] m, final Card c) {
        if (m.length == 0) {
            return num;
        }

        return CardFactoryUtil.doXMath(num, m[0], c);
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
    public static int handlePaid(final CardList paidList, final String string, final Card source) {
        if (paidList == null) {
            if (string.contains(".")) {
                final String[] splitString = string.split("\\.", 2);
                return CardFactoryUtil.doXMath(0, splitString[1], source);
            } else {
                return 0;
            }
        }
        if (string.startsWith("Amount")) {
            if (string.contains(".")) {
                final String[] splitString = string.split("\\.", 2);
                return CardFactoryUtil.doXMath(paidList.size(), splitString[1], source);
            } else {
                return paidList.size();
            }

        }
        if (string.contains("Valid")) {
            final String[] m = { "none" };

            String valid = string.replace("Valid ", "");
            final String[] l;
            l = valid.split("/"); // separate the specification from any math
            valid = l[0];
            if (l.length > 1) {
                m[0] = l[1];
            }
            final CardList list = paidList.getValidCards(valid, source.getController(), source);
            return CardFactoryUtil.doXMath(list.size(), m, source);
        }

        int tot = 0;
        for (final Card c : paidList) {
            tot += CardFactoryUtil.xCount(c, string);
        }

        return tot;
    }

    /**
     * <p>
     * inputUntapUpToNType.
     * </p>
     * 
     * @param n
     *            a int.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputUntapUpToNType(final int n, final String type) {
        final Input untap = new Input() {
            private static final long serialVersionUID = -2167059918040912025L;

            private final int stop = n;
            private int count = 0;

            @Override
            public void showMessage() {
                final StringBuilder sb = new StringBuilder();
                sb.append("Select a ").append(type).append(" to untap");
                CMatchUI.SINGLETON_INSTANCE.showMessage(sb.toString());
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.stop();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (card.isType(type) && zone.is(ZoneType.Battlefield)) {
                    card.untap();
                    this.count++;
                    if (this.count == this.stop) {
                        this.stop();
                    }
                }
            } // selectCard()
        };

        return untap;
    }

    /**
     * <p>
     * getMostProminentCardName.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getMostProminentCardName(final CardList list) {

        if (list.size() == 0) {
            return "";
        }

        final Map<String, Integer> map = new HashMap<String, Integer>();

        for (final Card c : list) {
            final String name = c.getName();
            if (!map.containsKey(name)) {
                map.put(name, 1);
            } else {
                map.put(name, map.get(name) + 1);
            }
        } // for

        int max = 0;
        String maxName = "";

        for (final Entry<String, Integer> entry : map.entrySet()) {
            final String type = entry.getKey();
            // Log.debug(type + " - " + entry.getValue());

            if (max < entry.getValue()) {
                max = entry.getValue();
                maxName = type;
            }
        }
        return maxName;
    }

    /**
     * <p>
     * getMostProminentCreatureType.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getMostProminentCreatureType(final CardList list) {

        if (list.size() == 0) {
            return "";
        }

        final Map<String, Integer> map = new HashMap<String, Integer>();

        for (final Card c : list) {
            final ArrayList<String> typeList = c.getType();

            for (final String var : typeList) {
                if (CardUtil.isACreatureType(var)) {
                    if (!map.containsKey(var)) {
                        map.put(var, 1);
                    } else {
                        map.put(var, map.get(var) + 1);
                    }
                }
            }
        } // for

        int max = 0;
        String maxType = "";

        for (final Entry<String, Integer> entry : map.entrySet()) {
            final String type = entry.getKey();
            // Log.debug(type + " - " + entry.getValue());

            if (max < entry.getValue()) {
                max = entry.getValue();
                maxType = type;
            }
        }

        return maxType;
    }

    /**
     * <p>
     * getMostProminentColor.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getMostProminentColor(final CardList list) {

        final Map<String, Integer> map = new HashMap<String, Integer>();

        for (final Card c : list) {
            final ArrayList<String> colorList = CardUtil.getColors(c);

            for (final String color : colorList) {
                if (color.equals("colorless")) {
                    // nothing to do
                } else if (!map.containsKey(color)) {
                    map.put(color, 1);
                } else {
                    map.put(color, map.get(color) + 1);
                }
            }
        } // for

        int max = 0;
        String maxColor = "";

        for (final Entry<String, Integer> entry : map.entrySet()) {
            final String color = entry.getKey();
            Log.debug(color + " - " + entry.getValue());

            if (max < entry.getValue()) {
                max = entry.getValue();
                maxColor = color;
            }
        }

        return maxColor;
    }

    /**
     * <p>
     * Get the total cost to pay for an attacker c, due to cards like
     * Propaganda, Ghostly Prison, Collective Restraint, ...
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getPropagandaCost(final Card c) {
        int cost = 0;

        final CardList list = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
        for (final Card card : list) {
            if (card.hasStartOfKeyword("Creatures can't attack unless their controller pays")) {
                final int keywordPosition = card
                        .getKeywordPosition("Creatures can't attack unless their controller pays");
                final String parse = card.getKeyword().get(keywordPosition).toString();
                final String[] k = parse.split(":");

                final String[] restrictions = k[1].split(",");
                if (!c.isValid(restrictions, card.getController(), card)) {
                    continue;
                }

                final String costString = k[2];
                if (costString.equals("X")) {
                    cost += CardFactoryUtil.xCount(card, card.getSVar("X"));
                } else if (costString.equals("Y")) {
                    cost += CardFactoryUtil.xCount(card, card.getSVar("Y"));
                } else {
                    cost += Integer.parseInt(k[2]);
                }
            }
        }

        final String s = Integer.toString(cost);

        return s;
    }

    /**
     * <p>
     * getUsableManaSources.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @return a int.
     */
    public static int getUsableManaSources(final Player player) {
        CardList list = player.getCardsIn(ZoneType.Battlefield);
        list = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                for (final AbilityMana am : c.getAIPlayableMana()) {
                    if (am.canPlay()) {
                        return true;
                    }
                }
                return false;
            }
        });

        return list.size();
    }

    /**
     * <p>
     * makeTokenSaproling.
     * </p>
     * 
     * @param controller
     *            a {@link forge.game.player.Player} object.
     * @return a {@link forge.CardList} object.
     */
    public static CardList makeTokenSaproling(final Player controller) {
        return CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", controller, "G", new String[] { "Creature",
                "Saproling" }, 1, 1, new String[] { "" });
    }

    /**
     * <p>
     * makeToken.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @param imageName
     *            a {@link java.lang.String} object.
     * @param controller
     *            a {@link forge.game.player.Player} object.
     * @param manaCost
     *            a {@link java.lang.String} object.
     * @param types
     *            an array of {@link java.lang.String} objects.
     * @param baseAttack
     *            a int.
     * @param baseDefense
     *            a int.
     * @param intrinsicKeywords
     *            an array of {@link java.lang.String} objects.
     * @return a {@link forge.CardList} object.
     */
    public static CardList makeToken(final String name, final String imageName, final Player controller,
            final String manaCost, final String[] types, final int baseAttack, final int baseDefense,
            final String[] intrinsicKeywords) {
        final CardList list = new CardList();
        final Card c = new Card();
        c.setName(name);
        c.setImageName(imageName);

        // c.setController(controller);
        // c.setOwner(controller);

        // TODO - most tokens mana cost is 0, this needs to be fixed
        // c.setManaCost(manaCost);
        c.addColor(manaCost);
        c.setToken(true);

        for (final String t : types) {
            c.addType(t);
        }

        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);

        final int multiplier = AllZoneUtil.getTokenDoublersMagnitude(controller);
        for (int i = 0; i < multiplier; i++) {
            Card temp = CardFactoryUtil.copyStats(c);

            for (final String kw : intrinsicKeywords) {
                if (kw.startsWith("HIDDEN")) {
                    temp.addExtrinsicKeyword(kw);
                    // extrinsic keywords won't survive the copyStats treatment
                } else {
                    temp.addIntrinsicKeyword(kw);
                }
            }
            temp.setOwner(controller);
            temp.setToken(true);
            CardFactoryUtil.parseKeywords(temp, temp.getName());
            temp = CardFactoryUtil.postFactoryKeywords(temp);
            Singletons.getModel().getGameAction().moveToPlay(temp);
            list.add(temp);
        }
        return list;
    }

    /**
     * <p>
     * copyTokens.
     * </p>
     * 
     * @param tokenList
     *            a {@link forge.CardList} object.
     * @return a {@link forge.CardList} object.
     */
    public static CardList copyTokens(final CardList tokenList) {
        final CardList list = new CardList();

        for (int tokenAdd = 0; tokenAdd < tokenList.size(); tokenAdd++) {
            final Card thisToken = tokenList.getCard(tokenAdd);

            final ArrayList<String> tal = thisToken.getType();
            final String[] tokenTypes = new String[tal.size()];
            tal.toArray(tokenTypes);

            final List<String> kal = thisToken.getIntrinsicKeyword();
            final String[] tokenKeywords = new String[kal.size()];
            kal.toArray(tokenKeywords);
            final CardList tokens = CardFactoryUtil.makeToken(thisToken.getName(), thisToken.getImageName(),
                    thisToken.getController(), thisToken.getManaCost().toString(), tokenTypes, thisToken.getBaseAttack(),
                    thisToken.getBaseDefense(), tokenKeywords);

            for (final Card token : tokens) {
                token.setColor(thisToken.getColor());
            }

            list.addAll(tokens);
        }

        return list;
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
    public static ArrayList<Ability> getBushidoEffects(final Card c) {
        final ArrayList<String> keywords = c.getKeyword();
        final ArrayList<Ability> list = new ArrayList<Ability>();

        final Card crd = c;

        for (final String kw : keywords) {
            if (kw.contains("Bushido")) {
                final String[] parse = kw.split(" ");
                final String s = parse[1];
                final int magnitude = Integer.parseInt(s);

                final Ability ability = new Ability(c, "0") {
                    @Override
                    public void resolve() {
                        final Command untilEOT = new Command() {

                            private static final long serialVersionUID = 3014846051064254493L;

                            @Override
                            public void execute() {
                                if (AllZoneUtil.isCardInPlay(crd)) {
                                    crd.addTempAttackBoost(-1 * magnitude);
                                    crd.addTempDefenseBoost(-1 * magnitude);
                                }
                            }
                        };

                        AllZone.getEndOfTurn().addUntil(untilEOT);

                        crd.addTempAttackBoost(magnitude);
                        crd.addTempDefenseBoost(magnitude);
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append(c);
                sb.append(" - (Bushido) gets +");
                sb.append(magnitude);
                sb.append("/+");
                sb.append(magnitude);
                sb.append(" until end of turn.");
                ability.setStackDescription(sb.toString());

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

    /**
     * getWorstLand
     * <p/>
     * This function finds the worst land a player has in play based on: worst
     * 1. tapped, basic land 2. tapped, non-basic land 3. untapped, basic land
     * 4. untapped, non-basic land best
     * <p/>
     * This is useful when the AI needs to find one of its lands to sacrifice
     * 
     * @param player
     *            - AllZone.getHumanPlayer() or AllZone.getComputerPlayer()
     * @return the worst land found based on the description above
     */
    public static Card getWorstLand(final Player player) {
        final CardList lands = AllZoneUtil.getPlayerLandsInPlay(player);
        return CardFactoryUtil.getWorstLand(lands);
    } // end getWorstLand

    /**
     * <p>
     * getWorstLand.
     * </p>
     * 
     * @param lands
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getWorstLand(final CardList lands) {
        Card worstLand = null;
        // first, check for tapped, basic lands
        for (int i = 0; i < lands.size(); i++) {
            final Card tmp = lands.get(i);
            if (tmp.isTapped() && tmp.isBasicLand()) {
                worstLand = tmp;
            }
        }
        // next, check for tapped, non-basic lands
        if (worstLand == null) {
            for (int i = 0; i < lands.size(); i++) {
                final Card tmp = lands.get(i);
                if (tmp.isTapped()) {
                    worstLand = tmp;
                }
            }
        }
        // next, untapped, basic lands
        if (worstLand == null) {
            for (int i = 0; i < lands.size(); i++) {
                final Card tmp = lands.get(i);
                if (tmp.isUntapped() && tmp.isBasicLand()) {
                    worstLand = tmp;
                }
            }
        }
        // next, untapped, non-basic lands
        if (worstLand == null) {
            for (int i = 0; i < lands.size(); i++) {
                final Card tmp = lands.get(i);
                if (tmp.isUntapped()) {
                    worstLand = tmp;
                }
            }
        }
        return worstLand;
    } // end getWorstLand

    // may return null
    /**
     * <p>
     * getRandomCard.
     * </p>
     * 
     * @param list
     *            a {@link forge.CardList} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getRandomCard(final CardList list) {
        if (list.size() == 0) {
            return null;
        }

        final int index = CardFactoryUtil.random.nextInt(list.size());
        return list.get(index);
    }

    /**
     * <p>
     * playLandEffects.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public static void playLandEffects(final Card c) {
        final Player player = c.getController();

        // > 0 because land amount isn't incremented until after playLandEffects
        final boolean extraLand = player.getNumLandsPlayed() > 0;

        if (extraLand) {
            final CardList fastbonds = player.getCardsIn(ZoneType.Battlefield, "Fastbond");
            for (final Card f : fastbonds) {
                final SpellAbility ability = new Ability(f, "0") {
                    @Override
                    public void resolve() {
                        f.getController().addDamage(1, f);
                    }
                };
                ability.setStackDescription("Fastbond - Deals 1 damage to you.");

                AllZone.getStack().addSimultaneousStackEntry(ability);

            }
        }
    }

    /**
     * <p>
     * isNegativeCounter.
     * </p>
     * 
     * @param c
     *            a {@link forge.Counters} object.
     * @return a boolean.
     */
    public static boolean isNegativeCounter(final Counters c) {
        return (c == Counters.AGE) || (c == Counters.BLAZE) || (c == Counters.BRIBERY) || (c == Counters.DOOM)
                || (c == Counters.ICE) || (c == Counters.M1M1) || (c == Counters.M0M2) || (c == Counters.M0M1)
                || (c == Counters.TIME);
    }

    /**
     * <p>
     * Copies stats like power, toughness, etc.
     * </p>
     * 
     * @param sim
     *            a {@link java.lang.Object} object.
     * @return a {@link forge.Card} object.
     */
    public static Card copyStats(final Card sim) {
        final Card c = new Card();

        c.setFlipCard(sim.isFlipCard());
        c.setDoubleFaced(sim.isDoubleFaced());
        c.setCurSetCode(sim.getCurSetCode());

        final CardCharacteristicName origState = sim.getCurState();
        for (final CardCharacteristicName state : sim.getStates()) {
            c.addAlternateState(state);
            c.setState(state);
            sim.setState(state);
            CardFactoryUtil.copyCharacteristics(sim, c);
        }

        sim.setState(origState);
        c.setState(origState);

        return c;
    } // copyStats()

    /**
     * Copy characteristics.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public static void copyCharacteristics(final Card from, final Card to) {
        to.setBaseAttack(from.getBaseAttack());
        to.setBaseDefense(from.getBaseDefense());
        to.setBaseLoyalty(from.getBaseLoyalty());
        to.setBaseAttackString(from.getBaseAttackString());
        to.setBaseDefenseString(from.getBaseDefenseString());
        to.setIntrinsicKeyword(from.getIntrinsicKeyword());
        to.setName(from.getName());
        to.setType(from.getCharacteristics().getType());
        to.setText(from.getSpellText());
        to.setManaCost(from.getManaCost());
        to.setColor(from.getColor());
        to.setCardColorsOverridden(from.isCardColorsOverridden());
        to.setSVars(from.getSVars());
        to.setSets(from.getSets());
        to.setIntrinsicAbilities(from.getIntrinsicAbilities());

        to.setImageName(from.getImageName());
        to.setImageFilename(from.getImageFilename());
        to.setTriggers(from.getTriggers());
        to.setReplacementEffects(from.getReplacementEffects());
        to.setStaticAbilityStrings(from.getStaticAbilityStrings());

    }

    /**
     * Copy characteristics.
     * 
     * @param from
     *            the from
     * @param stateToCopy
     *            the state to copy
     * @param to
     *            the to
     */
    public static void copyState(final Card from, final CardCharacteristicName stateToCopy, final Card to) {

        // copy characteristics not associated with a state
        to.setBaseLoyalty(from.getBaseLoyalty());
        to.setBaseAttackString(from.getBaseAttackString());
        to.setBaseDefenseString(from.getBaseDefenseString());
        to.setText(from.getSpellText());

        // get CardCharacteristics for desired state
        CardCharacteristics characteristics = from.getState(stateToCopy);
        to.getCharacteristics().copy(characteristics);
        // handle triggers and replacement effect through Card class interface
        to.setTriggers(characteristics.getTriggers());
        to.setReplacementEffects(characteristics.getReplacementEffects());
    }

    public static void copySpellAbility(SpellAbility from, SpellAbility to) {
        to.setDescription(from.getDescription());
        to.setStackDescription(from.getDescription());
        if (from.getAbilityFactory() != null) {
            to.setAbilityFactory(new AbilityFactory(from.getAbilityFactory()));
        }
        if (from.getSubAbility() != null) {
            to.setSubAbility(from.getSubAbility().getCopy());
        }
        if (from.getRestrictions() != null) {
            to.setRestrictions(from.getRestrictions());
        }
        if (from.getConditions() != null) {
            to.setConditions(from.getConditions());
        }

        for (String sVar : from.getSVars()) {
            to.setSVar(sVar, from.getSVar(sVar));
        }
    }

    public static void correctAbilityChainSourceCard(final SpellAbility sa, final Card card) {

        sa.setSourceCard(card);
        if (sa.getAbilityFactory() != null) {

            sa.getAbilityFactory().setHostCard(card);
        }
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
        final ArrayList<String> ia = card.getIntrinsicAbilities();
        if (ia.size() > 0) {
            for (int i = 0; i < ia.size(); i++) {
                final AbilityFactory af = new AbilityFactory();
                // System.out.println(cardName);
                final SpellAbility sa = af.getAbility(ia.get(i), card);
                if (sa.getAbilityFactory().getMapParams().containsKey("SetAsKicked")) {
                    sa.setKickerAbility(true);
                }
                card.addSpellAbility(sa);

                /*final String bbCost = card.getSVar("Buyback");
                if (!bbCost.equals("")) {
                    final SpellAbility bbSA = sa.copy();
                    final String newCost = CardUtil.addManaCosts(card.getManaCost().toString(), bbCost);
                    if (bbSA.getPayCosts() != null) {
                        // create new Cost
                        bbSA.setPayCosts(new Cost(card, newCost, false));
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Buyback ").append(bbCost).append(" (You may pay an additional ").append(bbCost);
                    sb.append(" as you cast this spell. If you do, put this card into your hand as it resolves.)");
                    bbSA.setDescription(sb.toString());
                    bbSA.setIsBuyBackAbility(true);

                    card.addSpellAbility(bbSA);
                }*/
            }

        }
    }

    /**
     * <p>
     * postFactoryKeywords.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public static Card postFactoryKeywords(final Card card) {
        // this function should handle any keywords that need to be added after
        // a spell goes through the factory
        // Cards with Cycling abilities
        // -1 means keyword "Cycling" not found

        // TODO - certain cards have two different kicker types, kicker will
        // need
        // to be written differently to handle this
        // TODO - kicker costs can only be mana right now i think?
        // TODO - this kicker only works for pemanents. maybe we can create an
        // optional cost class for buyback, kicker, that type of thing
        final int kicker = CardFactoryUtil.hasKeyword(card, "Kicker");
        if (kicker != -1) {
            final SpellAbility kickedSpell = new Spell(card) {
                private static final long serialVersionUID = -1598664196463358630L;

                @Override
                public void resolve() {
                    card.setKicked(true);
                    Singletons.getModel().getGameAction().moveToPlay(card);
                }
            };
            final String parse = card.getKeyword().get(kicker).toString();
            card.removeIntrinsicKeyword(parse);

            final String[] k = parse.split(":");
            final String kickerCost = k[1];

            final ManaCost mc = new ManaCost(card.getManaCost().toString());
            mc.combineManaCost(kickerCost);

            kickedSpell.setKickerAbility(true);
            kickedSpell.setManaCost(mc.toString());
            kickedSpell.setAdditionalManaCost(kickerCost);

            final StringBuilder desc = new StringBuilder();
            desc.append("Kicker ").append(kickerCost).append(" (You may pay an additional ");
            desc.append(kickerCost).append(" as you cast this spell.)");

            kickedSpell.setDescription(desc.toString());

            final StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" (Kicked)");
            kickedSpell.setStackDescription(sb.toString());

            card.addSpellAbility(kickedSpell);
        }

        if (CardFactoryUtil.hasKeyword(card, "Multikicker") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Multikicker");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                final String[] k = parse.split("kicker ");

                final SpellAbility sa = card.getSpellAbility()[0];
                sa.setIsMultiKicker(true);
                sa.setMultiKickerManaCost(k[1]);
            }
        }

        if (CardFactoryUtil.hasKeyword(card, "Replicate") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Replicate");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                final String[] k = parse.split("cate ");

                final SpellAbility sa = card.getSpellAbility()[0];
                sa.setIsReplicate(true);
                sa.setReplicateManaCost(k[1]);
            }
        }

        final int evokeKeyword = CardFactoryUtil.hasKeyword(card, "Evoke");
        if (evokeKeyword != -1) {
            final SpellAbility evokedSpell = new Spell(card) {
                private static final long serialVersionUID = -1598664196463358630L;

                @Override
                public void resolve() {
                    card.setEvoked(true);
                    Singletons.getModel().getGameAction().moveToPlay(card);
                }

                @Override
                public boolean canPlayAI() {
                    if (!SpellPermanent.checkETBEffects(card, this, null)) {
                        return false;
                    }
                    return super.canPlayAI();
                }
            };
            final String parse = card.getKeyword().get(evokeKeyword).toString();
            card.removeIntrinsicKeyword(parse);

            final String[] k = parse.split(":");
            final String evokedCost = k[1];

            evokedSpell.setManaCost(evokedCost);

            final StringBuilder desc = new StringBuilder();
            desc.append("Evoke ").append(evokedCost);
            desc.append(" (You may cast this spell for its evoke cost. ");
            desc.append("If you do, when it enters the battlefield, sacrifice it.)");

            evokedSpell.setDescription(desc.toString());

            final StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" (Evoked)");
            evokedSpell.setStackDescription(sb.toString());

            card.addSpellAbility(evokedSpell);
        }

        if (CardFactoryUtil.hasKeyword(card, "Cycling") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Cycling");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String manacost = k[1];

                card.addSpellAbility(CardFactoryUtil.abilityCycle(card, manacost));
            }
        } // Cycling

        while (CardFactoryUtil.hasKeyword(card, "TypeCycling") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "TypeCycling");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String type = k[1];
                final String manacost = k[2];

                card.addSpellAbility(CardFactoryUtil.abilityTypecycle(card, manacost, type));
            }
        } // TypeCycling

        if (CardFactoryUtil.hasKeyword(card, "Transmute") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Transmute");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String manacost = k[1];

                card.addSpellAbility(CardFactoryUtil.abilityTransmute(card, manacost));
            }
        } // transmute

        // Sol's Soulshift fix
        int shiftPos = CardFactoryUtil.hasKeyword(card, "Soulshift");
        while (shiftPos != -1) {
            final int n = shiftPos;
            final String parse = card.getKeyword().get(n).toString();

            final String[] k = parse.split(":");
            final String manacost = k[1];

            card.addDestroyCommand(CardFactoryUtil.abilitySoulshift(card, manacost));
            shiftPos = CardFactoryUtil.hasKeyword(card, "Soulshift", n + 1);
        } // Soulshift

        if (CardFactoryUtil.hasKeyword(card, "Echo") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Echo");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String manacost = k[1];

                card.setEchoCost(manacost);

                final Command intoPlay = new Command() {

                    private static final long serialVersionUID = -7913835645603984242L;

                    @Override
                    public void execute() {
                        card.addExtrinsicKeyword("(Echo unpaid)");
                    }
                };
                card.addComesIntoPlayCommand(intoPlay);

            }
        } // echo

        if (CardFactoryUtil.hasKeyword(card, "Suspend") != -1) {
            // Suspend:<TimeCounters>:<Cost>
            final int n = CardFactoryUtil.hasKeyword(card, "Suspend");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                card.setSuspend(true);
                final String[] k = parse.split(":");

                final int timeCounters = Integer.parseInt(k[1]);
                final String cost = k[2];
                card.addSpellAbility(CardFactoryUtil.abilitySuspend(card, cost, timeCounters));
            }
        } // Suspend

        int xCount = card.getManaCost().getShardCount(ManaCostShard.X);
        if (xCount > 0) {
            final SpellAbility sa = card.getSpellAbility()[0];
            sa.setIsXCost(true);
            sa.setXManaCost(Integer.toString(xCount));
        } // X

        int cardnameSpot = CardFactoryUtil.hasKeyword(card, "CARDNAME is ");
        if (cardnameSpot != -1) {
            String color = "1";
            while (cardnameSpot != -1) {
                if (cardnameSpot != -1) {
                    final String parse = card.getKeyword().get(cardnameSpot).toString();
                    card.removeIntrinsicKeyword(parse);
                    color += " "
                            + InputPayManaCostUtil.getShortColorString(parse.replace("CARDNAME is ", "").replace(".",
                                    ""));
                    cardnameSpot = CardFactoryUtil.hasKeyword(card, "CARDNAME is ");
                }
            }
            card.addColor(color);
        }

        if (CardFactoryUtil.hasKeyword(card, "Fading") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Fading");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();

                final String[] k = parse.split(":");
                final int power = Integer.parseInt(k[1]);

                card.addComesIntoPlayCommand(CardFactoryUtil.fading(card, power));
            }
        } // Fading

        if (CardFactoryUtil.hasKeyword(card, "Vanishing") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Vanishing");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();

                final String[] k = parse.split(":");
                final int power = Integer.parseInt(k[1]);

                card.addComesIntoPlayCommand(CardFactoryUtil.vanishing(card, power));
            }
        } // Vanishing

        // AddCost
        if (!card.getSVar("FullCost").equals("")) {
            final SpellAbility[] abilities = card.getSpellAbility();
            if ((abilities.length > 0) && abilities[0].isSpell()) {
                final String altCost = card.getSVar("FullCost");
                final Cost abCost = new Cost(card, altCost, abilities[0].isAbility());
                abilities[0].setPayCosts(abCost);
            }
        }

        // AltCost
        if (!card.getSVar("AltCost").equals("")) {
            final SpellAbility[] abilities = card.getSpellAbility();
            if ((abilities.length > 0) && abilities[0].isSpell()) {
                String altCost = card.getSVar("AltCost");
                final HashMap<String, String> mapParams = new HashMap<String, String>();
                String altCostDescription = "";
                final String[] altCosts = altCost.split("\\|");

                for (int aCnt = 0; aCnt < altCosts.length; aCnt++) {
                    altCosts[aCnt] = altCosts[aCnt].trim();
                }

                for (final String altCost2 : altCosts) {
                    final String[] aa = altCost2.split("\\$");

                    for (int aaCnt = 0; aaCnt < aa.length; aaCnt++) {
                        aa[aaCnt] = aa[aaCnt].trim();
                    }

                    if (aa.length != 2) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("StaticEffectFactory Parsing Error: Split length of ");
                        sb.append(altCost2).append(" in ").append(card.getName()).append(" is not 2.");
                        throw new RuntimeException(sb.toString());
                    }

                    mapParams.put(aa[0], aa[1]);
                }

                altCost = mapParams.get("Cost");

                if (mapParams.containsKey("Description")) {
                    altCostDescription = mapParams.get("Description");
                }

                final SpellAbility sa = abilities[0];
                final SpellAbility altCostSA = sa.copy();

                final Cost abCost = new Cost(card, altCost, altCostSA.isAbility());
                altCostSA.setPayCosts(abCost);

                final StringBuilder sb = new StringBuilder();

                if (!altCostDescription.equals("")) {
                    sb.append(altCostDescription);
                } else {
                    sb.append("You may ").append(abCost.toStringAlt());
                    sb.append(" rather than pay ").append(card.getName()).append("'s mana cost.");
                }

                final SpellAbilityRestriction restriction = new SpellAbilityRestriction();
                restriction.setRestrictions(mapParams);
                if (!mapParams.containsKey("ActivationZone")) {
                    restriction.setZone(ZoneType.Hand);
                }
                altCostSA.setRestrictions(restriction);
                altCostSA.setDescription(sb.toString());
                altCostSA.setBasicSpell(false);

                card.addSpellAbility(altCostSA);
            }
        }

        if (card.hasKeyword("Delve")) {
            card.getSpellAbilities().get(0).setIsDelve(true);
        }

        if (card.hasStartOfKeyword("Haunt")) {
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

            final Trigger haunterDies = forge.card.trigger.TriggerHandler
                    .parseTrigger(sbHaunter.toString(), card, true);

            final Ability haunterDiesWork = new Ability(card, "0") {
                @Override
                public void resolve() {
                    this.getTargetCard().addHauntedBy(card);
                    Singletons.getModel().getGameAction().exile(card);
                }
            };
            haunterDiesWork.setDescription(hauntDescription);

            final Input target = new Input() {
                private static final long serialVersionUID = 1981791992623774490L;

                @Override
                public void showMessage() {
                    CMatchUI.SINGLETON_INSTANCE.showMessage("Choose target creature to haunt.");
                    ButtonUtil.disableAll();
                }

                @Override
                public void selectCard(final Card c, final PlayerZone zone) {
                    if (!zone.is(ZoneType.Battlefield) || !c.isCreature()) {
                        return;
                    }
                    if (c.canBeTargetedBy(haunterDiesWork)) {
                        haunterDiesWork.setTargetCard(c);
                        AllZone.getStack().add(haunterDiesWork);
                        this.stop();
                    } else {
                        CMatchUI.SINGLETON_INSTANCE
                                .showMessage("Cannot target this card (Shroud? Protection?).");
                    }
                }
            };

            final Ability haunterDiesSetup = new Ability(card, "0") {
                @Override
                public void resolve() {
                    final CardList creats = AllZoneUtil.getCreaturesInPlay();
                    for (int i = 0; i < creats.size(); i++) {
                        if (!creats.get(i).canBeTargetedBy(this)) {
                            creats.remove(i);
                            i--;
                        }
                    }
                    if (creats.size() == 0) {
                        return;
                    }

                    // need to do it this way because I don't know quite how to
                    // make TriggerHandler respect BeforePayMana.
                    if (card.getController().isHuman()) {
                        AllZone.getInputControl().setInput(target);
                    } else {
                        // AI choosing what to haunt
                        final CardList oppCreats = creats.getController(AllZone.getHumanPlayer());
                        if (oppCreats.size() != 0) {
                            haunterDiesWork.setTargetCard(CardFactoryUtil.getWorstCreatureAI(oppCreats));
                        } else {
                            haunterDiesWork.setTargetCard(CardFactoryUtil.getWorstCreatureAI(creats));
                        }
                        AllZone.getStack().add(haunterDiesWork);
                    }
                }
            };

            haunterDies.setOverridingAbility(haunterDiesSetup);

            // Second, create the trigger that runs when the haunted creature
            // dies
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

            final Ability haunterUnExiledWork = new Ability(card, "0") {
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
                final AbilityFactory af = new AbilityFactory();
                final String abString = card.getSVar(hauntSVarName).replace("AB$", "SP$")
                        .replace("Cost$ 0", "Cost$ " + card.getManaCost())
                        + " | SpellDescription$ " + abilityDescription;

                final SpellAbility sa = af.getAbility(abString, card);
                card.addSpellAbility(sa);
            }

            card.addTrigger(hauntedDies);
            card.addTrigger(haunterUnExiled);
        }

        if (card.hasKeyword("Provoke")) {
            final String actualTrigger = "Mode$ Attacks | ValidCard$ Card.Self | "
                    + "OptionalDecider$ You | Execute$ ProvokeAbility | Secondary$ True | TriggerDescription$ "
                    + "When this attacks, you may have target creature defending player "
                    + "controls untap and block it if able.";
            final String abString = "DB$ MustBlock | ValidTgts$ Creature.YouDontCtrl | "
                    + "TgtPrompt$ Select target creature defending player controls | SubAbility$ DBUntap";
            final String dbString = "DB$ Untap | Defined$ Targeted";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, card, true);
            card.addTrigger(parsedTrigger);
            card.setSVar("ProvokeAbility", abString);
            card.setSVar("DBUntap", dbString);
        }

        if (card.hasKeyword("Epic")) {
            final SpellAbility origSA = card.getSpellAbilities().get(0);

            final SpellAbility newSA = new Spell(card, origSA.getPayCosts(), origSA.getTarget()) {
                private static final long serialVersionUID = -7934420043356101045L;

                @Override
                public void resolve() {

                    String name = card.toString() + " Epic";
                    if (card.getController().getCardsIn(ZoneType.Battlefield, name).isEmpty()) {
                        // Create Epic emblem
                        final Card eff = new Card();
                        eff.setName(card.toString() + " Epic");
                        eff.addType("Effect"); // Or Emblem
                        eff.setToken(true); // Set token to true, so when leaving
                                            // play it gets nuked
                        eff.addController(card.getController());
                        eff.setOwner(card.getController());
                        eff.setImageName(card.getImageName());
                        eff.setColor(card.getColor());
                        eff.setImmutable(true);
                        eff.setEffectSource(card);
    
                        eff.addStaticAbility("Mode$ CantBeCast | ValidCard$ Card | Caster$ You "
                                + "| Description$ For the rest of the game, you can't cast spells.");
                        
                        eff.setSVar("EpicCopy", "AB$ CopySpell | Cost$ 0 | Defined$ EffectSource");
    
                        final Trigger copyTrigger = forge.card.trigger.TriggerHandler.parseTrigger(
                                "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | Execute$ EpicCopy | TriggerDescription$ "
                                        + "At the beginning of each of your upkeeps, copy " + card.toString()
                                        + " except for its epic ability.", eff, false);
    
                        eff.addTrigger(copyTrigger);
    
                        AllZone.getTriggerHandler().suppressMode(TriggerType.ChangesZone);
                        Singletons.getModel().getGameAction().moveToPlay(eff);
                        AllZone.getTriggerHandler().clearSuppression(TriggerType.ChangesZone);
                    }

                    if (card.getController().isHuman()) {
                        Singletons.getModel().getGameAction().playSpellAbilityNoStack(origSA, false);
                    } else {
                        ComputerUtil.playNoStack(origSA);
                    }
                }
            };
            newSA.setDescription(origSA.getDescription());

            origSA.setPayCosts(null);
            origSA.setManaCost("0");

            card.clearSpellAbility();
            card.addSpellAbility(newSA);
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
            final AbilityFactory af = new AbilityFactory();
            final SpellAbility sa = af.getAbility(abilityStr.toString(), card);
            card.addSpellAbility(sa);
            // add ability to instrinic strings so copies/clones create the ability also
            card.getIntrinsicAbilities().add(abilityStr.toString());
        }

        for (String kw : card.getKeyword()) {

            if (kw.startsWith("ETBReplacement")) {
                String[] splitkw = kw.split(":");
                ReplacementLayer layer = ReplacementLayer.smartValueOf(splitkw[1]);
                AbilityFactory af = new AbilityFactory();
                SpellAbility repAb = af.getAbility(card.getSVar(splitkw[2]), card);
                String desc = repAb.getDescription();
                setupETBReplacementAbility(repAb);

                String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield | Description$ " + desc;
                if (splitkw.length == 4) {
                    if (splitkw[3].contains("Optional")) {
                        repeffstr += " | Optional$ True";
                    }
                }

                ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, card);
                re.setLayer(layer);
                re.setOverridingAbility(repAb);

                card.addReplacementEffect(re);
            }
        }

        return card;
    }

    public static void setupETBReplacementAbility(SpellAbility sa) {
        SpellAbility tailend = sa;
        while (tailend.getSubAbility() != null) {
            tailend = tailend.getSubAbility();
        }

        class ETBReplacementMove extends AbilitySub {
            private static final long serialVersionUID = 704771599662730112L;

            /**
             * TODO: Write javadoc for Constructor.
             * @param sourceCard
             *         the source card
             * @param tgt
             *         the target
             */
            public ETBReplacementMove(Card sourceCard, Target tgt) {
                super(sourceCard, tgt);
            }

            @Override
            public void resolve() {
                forge.Singletons.getModel().getGameAction().moveToPlay(((Card) this.getReplacingObject("Card")));
            }

            /* (non-Javadoc)
             * @see forge.card.spellability.AbilitySub#chkAIDrawback()
             */
            @Override
            public boolean chkAIDrawback() {
                return false;
            }

            /* (non-Javadoc)
             * @see forge.card.spellability.AbilitySub#getCopy()
             */
            @Override
            public AbilitySub getCopy() {
                // TODO Auto-generated method stub
                return new ETBReplacementMove(getSourceCard(), null);
            }

            /* (non-Javadoc)
             * @see forge.card.spellability.AbilitySub#doTrigger(boolean)
             */
            @Override
            public boolean doTrigger(boolean mandatory) {
                // TODO Auto-generated method stub
                return false;
            }

        }

        tailend.setSubAbility(new ETBReplacementMove(sa.getSourceCard(), null));
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
        final ArrayList<String> a = c.getKeyword();
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).toString().startsWith(k)) {
                return i;
            }
        }

        return -1;
    }

    // Sol's Soulshift fix
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
    static final int hasKeyword(final Card c, final String k, final int startPos) {
        final ArrayList<String> a = c.getKeyword();
        for (int i = startPos; i < a.size(); i++) {
            if (a.get(i).toString().startsWith(k)) {
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
                public void execute() {
                    final CardList lands = AllZoneUtil.getPlayerLandsInPlay(card.getController());
                    lands.remove(card);
                    if (!(lands.size() <= 2)) {
                        // it enters the battlefield this way, and should not
                        // fire triggers
                        card.setTapped(true);
                    }
                }
            });
        }
        if (CardFactoryUtil.hasKeyword(card, "CARDNAME enters the battlefield tapped unless you control a") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card,
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
                public void execute() {
                    final CardList clICtrl = card.getOwner().getCardsIn(ZoneType.Battlefield);

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
        if (CardFactoryUtil.hasKeyword(card, "Sunburst") != -1) {
            final Command sunburstCIP = new Command() {
                private static final long serialVersionUID = 1489845860231758299L;

                @Override
                public void execute() {
                    if (card.isCreature()) {
                        card.addCounter(Counters.P1P1, card.getSunburstValue());
                    } else {
                        card.addCounter(Counters.CHARGE, card.getSunburstValue());
                    }

                }
            };

            final Command sunburstLP = new Command() {
                private static final long serialVersionUID = -7564420917490677427L;

                @Override
                public void execute() {
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
                public void execute() {
                    final CardList cardsInPlay = AllZoneUtil.getCardsIn(ZoneType.Battlefield).getType("World");
                    cardsInPlay.remove(card);
                    for (int i = 0; i < cardsInPlay.size(); i++) {
                        Singletons.getModel().getGameAction().sacrificeDestroy(cardsInPlay.get(i));
                    }
                } // execute()
            }; // Command
            card.addComesIntoPlayCommand(intoPlay);
        }

        if (CardFactoryUtil.hasKeyword(card, "Morph") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Morph");
            if (n != -1) {
                card.setPrevType(card.getType());

                final String parse = card.getKeyword().get(n).toString();
                card.setCanMorph(true);

                final String[] k = parse.split(":");
                final Cost cost = new Cost(card, k[1], true);

                final int attack = card.getBaseAttack();
                final int defense = card.getBaseDefense();

                card.addSpellAbility(CardFactoryUtil.abilityMorphDown(card));

                card.turnFaceDown();

                card.addSpellAbility(CardFactoryUtil.abilityMorphUp(card, cost, attack, defense));

                card.turnFaceUp();
            }
        } // Morph

        if (CardFactoryUtil.hasKeyword(card, "Unearth") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Unearth");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");

                final String manacost = k[1];

                card.addSpellAbility(CardFactoryUtil.abilityUnearth(card, manacost));
                card.setUnearth(true);
            }
        } // unearth

        if (CardFactoryUtil.hasKeyword(card, "Madness") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Madness");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                card.setMadnessCost(k[1]);
            }
        } // madness

        if (CardFactoryUtil.hasKeyword(card, "Miracle") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Miracle");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                card.setMiracleCost(k[1]);
            }
        } // miracle

        if (CardFactoryUtil.hasKeyword(card, "Devour") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Devour");
            if (n != -1) {

                final String parse = card.getKeyword().get(n).toString();
                // card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                final String magnitude = k[1];

                final int multiplier = Integer.parseInt(magnitude);
                // final String player = card.getController();
                final int[] numCreatures = new int[1];

                final Command intoPlay = new Command() {
                    private static final long serialVersionUID = -7530312713496897814L;

                    @Override
                    public void execute() {
                        final CardList creats = AllZoneUtil.getCreaturesInPlay(card.getController());
                        creats.remove(card);
                        // System.out.println("Creats size: " + creats.size());

                        card.clearDevoured();
                        if (card.getController().isHuman()) {
                            if (creats.size() > 0) {
                                final List<Object> selection = GuiUtils.getOrderChoices("Devour", "Devouring", false, (Object[]) creats.toArray());
                                numCreatures[0] = selection.size();

                                for (Object o : selection) {
                                    Card dinner = (Card) o;
                                    card.addDevoured(dinner);
                                    Singletons.getModel().getGameAction().sacrifice(dinner, null);
                                }
                            }
                        } // human
                        else {
                            int count = 0;
                            for (int i = 0; i < creats.size(); i++) {
                                final Card c = creats.get(i);
                                if ((c.getNetAttack() <= 1) && ((c.getNetAttack() + c.getNetDefense()) <= 3)) {
                                    card.addDevoured(c);
                                    Singletons.getModel().getGameAction().sacrifice(c, null);
                                    count++;
                                }
                                // is this needed?
                                AllZone.getComputerPlayer().getZone(ZoneType.Battlefield).updateObservers();
                            }
                            numCreatures[0] = count;
                        }
                        final int totalCounters = numCreatures[0] * multiplier;

                        card.addCounter(Counters.P1P1, totalCounters);

                    }
                };
                card.addComesIntoPlayCommand(intoPlay);
            }
        } // Devour

        if (CardFactoryUtil.hasKeyword(card, "Modular") != -1) {
            final int n = CardFactoryUtil.hasKeyword(card, "Modular");
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();

                final int m = Integer.parseInt(parse.substring(8));

                card.addIntrinsicKeyword("etbCounter:P1P1:" + m + ":no Condition: ");

                final SpellAbility ability = new Ability(card, "0") {
                    @Override
                    public void resolve() {
                        final Card card2 = this.getTargetCard();
                        card2.addCounter(Counters.P1P1, this.getSourceCard().getCounters(Counters.P1P1));
                    } // resolve()
                };

                card.addDestroyCommand(new Command() {
                    private static final long serialVersionUID = 304026662487997331L;

                    @Override
                    public void execute() {
                        // Target as Modular is Destroyed
                        if (card.getController().isComputer()) {
                            CardList choices = AllZone.getComputerPlayer().getCardsIn(ZoneType.Battlefield);
                            choices = choices.filter(new CardListFilter() {
                                @Override
                                public boolean addCard(final Card c) {
                                    return c.isCreature() && c.isArtifact();
                                }
                            });
                            if (choices.size() != 0) {
                                ability.setTargetCard(CardFactoryUtil.getBestCreatureAI(choices));

                                if (ability.getTargetCard() != null) {
                                    ability.setStackDescription("Put " + card.getCounters(Counters.P1P1)
                                            + " +1/+1 counter/s from " + card + " on " + ability.getTargetCard());
                                    AllZone.getStack().addSimultaneousStackEntry(ability);

                                }
                            }
                        } else {
                            AllZone.getInputControl().setInput(CardFactoryUtil.modularInput(ability, card));
                        }
                    }
                });
            }
        } // Modular

        /*
         * WARNING: must keep this keyword processing before etbCounter keyword
         * processing.
         */
        final int graft = CardFactoryUtil.hasKeyword(card, "Graft");
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

        final int bloodthirst = CardFactoryUtil.hasKeyword(card, "Bloodthirst");
        if (bloodthirst != -1) {
            final String numCounters = card.getKeyword().get(bloodthirst).split(" ")[1];

            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = -1849308549161972508L;

                @Override
                public void execute() {
                    if (card.getController().getOpponent().getAssignedDamage() > 0) {
                        int toAdd = -1;
                        if (numCounters.equals("X")) {
                            toAdd = card.getController().getOpponent().getAssignedDamage();
                        } else {
                            toAdd = Integer.parseInt(numCounters);
                        }
                        card.addCounter(Counters.P1P1, toAdd);
                    }
                }

            });
        } // bloodthirst

        final int storm = card.getKeywordAmount("Storm");
        for (int i = 0; i < storm; i++) {
            final StringBuilder trigScript = new StringBuilder(
                    "Mode$ SpellCast | ValidCard$ Card.Self | Execute$ Storm "
                            + "| TriggerDescription$ Storm (When you cast this spell, "
                            + "copy it for each spell cast before it this turn.)");

            card.setSVar("Storm", "AB$CopySpell | Cost$ 0 | Defined$ TriggeredSpellAbility | Amount$ StormCount");
            card.setSVar("StormCount", "Count$StormCount");
            final Trigger stormTrigger = TriggerHandler.parseTrigger(trigScript.toString(), card, true);

            card.addTrigger(stormTrigger);
        } // Storm

    }

} // end class CardFactoryUtil
