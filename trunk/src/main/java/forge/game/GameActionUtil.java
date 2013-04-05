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
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.CardPredicates.Presets;
import forge.CardUtil;
import forge.Command;
import forge.Constant;
import forge.CounterType;
import forge.FThreads;
import forge.Singletons;
import forge.card.ability.AbilityFactory;
import forge.card.ability.AbilityUtils;
import forge.card.ability.ApiType;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostDamage;
import forge.card.cost.CostDiscard;
import forge.card.cost.CostExile;
import forge.card.cost.CostPart;
import forge.card.cost.CostPartMana;
import forge.card.cost.CostPartWithList;
import forge.card.cost.CostPayLife;
import forge.card.cost.CostPutCounter;
import forge.card.cost.CostRemoveCounter;
import forge.card.cost.CostReturn;
import forge.card.cost.CostSacrifice;
import forge.card.cost.CostTapType;
import forge.card.cost.CostUtil;
import forge.card.mana.ManaCost;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityRestriction;
import forge.control.input.InputPayManaExecuteCommands;
import forge.control.input.InputPayment;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.event.CardDamagedEvent;
import forge.game.event.LifeLossEvent;
import forge.game.player.AIPlayer;
import forge.game.player.HumanPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.GuiDialog;
import forge.sound.SoundEffectType;


/**
 * <p>
 * GameActionUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class GameActionUtil {

    
    /** 
     * TODO: Write javadoc for this type.
     *
     */
    private static final class AbilityDestroy extends Ability {
        private final Card affected;
        private final boolean canRegenerate;

        public AbilityDestroy(Card sourceCard, Card affected, boolean canRegenerate) {
            super(sourceCard, ManaCost.ZERO);
            this.affected = affected;
            this.canRegenerate = canRegenerate;
        }

        @Override
        public void resolve() {
            final GameState game = Singletons.getModel().getGame(); 
            if ( canRegenerate )
                game.getAction().destroy(affected, this);
            else
                game.getAction().destroyNoRegeneration(affected, this);
        }
    }

    private static final class CascadeAbility extends Ability {
        private final Player controller;
        private final Card cascCard;

        /**
         * TODO: Write javadoc for Constructor.
         * @param sourceCard
         * @param manaCost
         * @param controller
         * @param cascCard
         */
        private CascadeAbility(Card sourceCard, ManaCost manaCost, Player controller, Card cascCard) {
            super(sourceCard, manaCost);
            this.controller = controller;
            this.cascCard = cascCard;
        }

        @Override
        public void resolve() {
            final GameState game = Singletons.getModel().getGame(); 
            final List<Card> topOfLibrary = controller.getCardsIn(ZoneType.Library);
            final List<Card> revealed = new ArrayList<Card>();

            if (topOfLibrary.size() == 0) {
                return;
            }

            Card cascadedCard = null;
            Card crd;
            int count = 0;
            while (cascadedCard == null) {
                crd = topOfLibrary.get(count++);
                revealed.add(crd);
                if ((!crd.isLand() && (crd.getManaCost().getCMC() < cascCard.getManaCost().getCMC()))) {
                    cascadedCard = crd;
                }

                if (count == topOfLibrary.size()) {
                    break;
                }

            } // while
            GuiChoose.oneOrNone("Revealed cards:", revealed);

            if (cascadedCard != null) {
                Player p = cascadedCard.getController();
                // returns boolean, but spell resolution stays inside the method anyway (for now)
                if ( p.getController().playCascade(cascadedCard, cascCard) )
                    revealed.remove(cascadedCard);
            }
            CardLists.shuffle(revealed);
            for (final Card bottom : revealed) {
                game.getAction().moveToBottomOfLibrary(bottom);
            }
        }
    }

    private static final class CascadeExecutor implements Command {
        private final Card c;
        private final GameState game;
        private final Player controller;
        
        private static final long serialVersionUID = -845154812215847505L;

        /**
         * TODO: Write javadoc for Constructor.
         * @param controller
         * @param c
         */
        private CascadeExecutor(Player controller, Card c, final GameState game) {
            this.controller = controller;
            this.c = c;
            this.game = game;
        }

        @Override
        public void execute() {
            if (!c.isCopiedSpell()) {
                final List<Card> maelstromNexii = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Maelstrom Nexus"));

                for (final Card nexus : maelstromNexii) {
                    if (CardUtil.getThisTurnCast("Card.YouCtrl", nexus).size() == 1) {
                        this.doCascade(c, controller);
                    }
                }
            }

            for (String keyword : c.getKeyword()) {
                if (keyword.equals("Cascade")) {
                    this.doCascade(c, controller);
                }
            }
        } // execute()

        void doCascade(final Card c, final Player controller) {
            final Card cascCard = c;

            final Ability ability = new CascadeAbility(c, ManaCost.ZERO, controller, cascCard);
            final StringBuilder sb = new StringBuilder();
            sb.append(c).append(" - Cascade.");
            ability.setStackDescription(sb.toString());
            ability.setActivatingPlayer(controller);

            game.getStack().addSimultaneousStackEntry(ability);

        }
    }
    
    /** 
     * TODO: Write javadoc for this type.
     *
     */
    private static final class RippleAbility extends Ability {
        private final Player controller;
        private final int rippleCount;
        private final Card rippleCard;
    
        /**
         * TODO: Write javadoc for Constructor.
         * @param sourceCard
         * @param manaCost
         * @param controller
         * @param rippleCount
         * @param rippleCard
         */
        private RippleAbility(Card sourceCard, ManaCost manaCost, Player controller, int rippleCount,
                Card rippleCard) {
            super(sourceCard, manaCost);
            this.controller = controller;
            this.rippleCount = rippleCount;
            this.rippleCard = rippleCard;
        }
    
        @Override
        public void resolve() {
            final List<Card> topOfLibrary = controller.getCardsIn(ZoneType.Library);
            final List<Card> revealed = new ArrayList<Card>();
            int rippleNumber = rippleCount;
            if (topOfLibrary.size() == 0) {
                return;
            }
    
            // Shouldn't Have more than Ripple 10, seeing as no
            // cards exist with a ripple greater than 4
            final int rippleMax = 10;
            final Card[] rippledCards = new Card[rippleMax];
            Card crd;
            if (topOfLibrary.size() < rippleNumber) {
                rippleNumber = topOfLibrary.size();
            }
    
            for (int i = 0; i < rippleNumber; i++) {
                crd = topOfLibrary.get(i);
                revealed.add(crd);
                if (crd.getName().equals(rippleCard.getName())) {
                    rippledCards[i] = crd;
                }
            } // for
            GuiChoose.oneOrNone("Revealed cards:", revealed);
            for (int i = 0; i < rippleMax; i++) {
                if (rippledCards[i] != null) {
                    Player p = rippledCards[i].getController();
    
                    if (p.isHuman()) {
                        if (GuiDialog.confirm(rippledCards[i], "Cast " + rippledCards[i].getName() + "?")) {
                            ((HumanPlayer)p).playCardWithoutManaCost(rippledCards[i]);
                            revealed.remove(rippledCards[i]);
                        }
                    } else {
                        AIPlayer ai = (AIPlayer) p;
                        SpellAbility saPlayed = ai.getAi().chooseAndPlaySa(rippledCards[i].getBasicSpells(), false, true);
                        if ( saPlayed != null )
                            revealed.remove(rippledCards[i]);
                    }
                }
            }
            CardLists.shuffle(revealed);
            for (final Card bottom : revealed) {
                Singletons.getModel().getGame().getAction().moveToBottomOfLibrary(bottom);
            }
        }
    
    }

    /** 
     * TODO: Write javadoc for this type.
     *
     */
    private static final class RippleExecutor implements Command {
        private final Player controller;
        private final Card c;
        private static final long serialVersionUID = -845154812215847505L;

        /**
         * TODO: Write javadoc for Constructor.
         * @param controller
         * @param c
         */
        private RippleExecutor(Player controller, Card c) {
            this.controller = controller;
            this.c = c;
        }

        @Override
        public void execute() {

            final List<Card> thrummingStones = controller.getCardsIn(ZoneType.Battlefield, "Thrumming Stone");
            for (int i = 0; i < thrummingStones.size(); i++) {
                c.addExtrinsicKeyword("Ripple:4");
            }

            for (String parse : c.getKeyword()) {
                if (parse.startsWith("Ripple")) {
                    final String[] k = parse.split(":");
                    this.doRipple(c, Integer.valueOf(k[1]), controller);
                }
            }
        } // execute()

        void doRipple(final Card c, final int rippleCount, final Player controller) {
            final Card rippleCard = c;

            if (controller.isComputer() || GuiDialog.confirm(c, "Activate Ripple for " + c + "?")) {

                final Ability ability = new RippleAbility(c, ManaCost.ZERO, controller, rippleCount, rippleCard);
                final StringBuilder sb = new StringBuilder();
                sb.append(c).append(" - Ripple.");
                ability.setStackDescription(sb.toString());
                ability.setDescription(sb.toString());
                ability.setActivatingPlayer(controller);

                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

            }
        }
    }

    private GameActionUtil() {
        throw new AssertionError();
    }

    /**
     * <p>
     * executePlayCardEffects.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void executePlayCardEffects(final SpellAbility sa) {
        // (called in MagicStack.java)

        final GameState game = Singletons.getModel().getGame(); 
        final Command cascade = new CascadeExecutor(sa.getActivatingPlayer(), sa.getSourceCard(), game);
        cascade.execute();
        final Command ripple = new RippleExecutor(sa.getActivatingPlayer(), sa.getSourceCard());
        ripple.execute();
    }

    private static int getAmountFromPart(CostPart part, Card source, SpellAbility sourceAbility) {
        String amountString = part.getAmount();
        return StringUtils.isNumeric(amountString) ? Integer.parseInt(amountString) : AbilityUtils.calculateAmount(source, amountString, sourceAbility);
    }
    
    /**
     * TODO: Write javadoc for this method.
     * @param part
     * @param source
     * @param sourceAbility
     * @return
     */
    private static int getAmountFromPartX(CostPart part, Card source, SpellAbility sourceAbility) {
        String amountString = part.getAmount();
        return StringUtils.isNumeric(amountString) ? Integer.parseInt(amountString) : CardFactoryUtil.xCount(source, source.getSVar(amountString));
    }

    /**
     * <p>
     * payCostDuringAbilityResolve.
     * </p>
     * 
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param cost
     *            a {@link forge.card.cost.Cost} object.
     * @param paid
     *            a {@link forge.Command} object.
     * @param unpaid
     *            a {@link forge.Command} object.
     * @param sourceAbility TODO
     */
    public static boolean payCostDuringAbilityResolve(final SpellAbility ability, final Cost cost, SpellAbility sourceAbility, final GameState game) {
        
        // Only human player pays this way
        final Player p = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        Card current = null; // Used in spells with RepeatEach effect to distinguish cards, Cut the Tethers
        if (!source.getRemembered().isEmpty()) {
            if (source.getRemembered().get(0) instanceof Card) { 
                current = (Card) source.getRemembered().get(0);
            }
        }
        if (!source.getImprinted().isEmpty()) {
            current = source.getImprinted().get(0);
        }
        
        final List<CostPart> parts =  cost.getCostParts();
        ArrayList<CostPart> remainingParts =  new ArrayList<CostPart>(cost.getCostParts());
        CostPart costPart = null;
        if (!parts.isEmpty()) {
            costPart = parts.get(0);
        }
        final String orString = sourceAbility == null ? "" : " (or: " + sourceAbility.getStackDescription() + ")";
        
        if (parts.isEmpty() || costPart.getAmount().equals("0")) {
            return GuiDialog.confirm(source, "Do you want to pay 0?" + orString);
        }
        
        //the following costs do not need inputs
        for (CostPart part : parts) {
            boolean mayRemovePart = true;
            
            if (part instanceof CostPayLife) {
                final int amount = getAmountFromPart(part, source, sourceAbility);
                if (!p.canPayLife(amount))
                    return false;

                if (false == GuiDialog.confirm(source, "Do you want to pay " + amount + " life?" + orString))
                    return false;

                p.payLife(amount, null);
            }

            else if (part instanceof CostDamage) {
                int amount = getAmountFromPartX(part, source, sourceAbility);
                if (!p.canPayLife(amount))
                    return false;

                if (false == GuiDialog.confirm(source, "Do you want " + source + " to deal " + amount + " damage to you?"))
                    return false;
                
                p.addDamage(amount, source);
            }

            else if (part instanceof CostPutCounter) {
                CounterType counterType = ((CostPutCounter) part).getCounter();
                int amount = getAmountFromPartX(part, source, sourceAbility);
                
                if (false == source.canHaveCountersPlacedOnIt(counterType)) {
                    String message = String.format("Won't be able to pay upkeep for %s but it can't have %s counters put on it.", source, counterType.getName());
                    p.getGame().getGameLog().add("ResolveStack", message, 2);
                    return false;
                }
                
                String plural = amount > 1 ? "s" : "";
                if (false == GuiDialog.confirm(source, "Do you want to put " + amount + " " + counterType.getName() + " counter" + plural + " on " + source + "?")) 
                    return false;
                
                source.addCounter(counterType, amount, false);
            }

            else if (part instanceof CostRemoveCounter) {
                CounterType counterType = ((CostRemoveCounter) part).getCounter();
                int amount = getAmountFromPartX(part, source, sourceAbility);
                String plural = amount > 1 ? "s" : "";
                
                if (!part.canPay(sourceAbility))
                    return false;

                if ( false == GuiDialog.confirm(source, "Do you want to remove " + amount + " " + counterType.getName() + " counter" + plural + " from " + source + "?"))
                    return false;

                source.subtractCounter(counterType, amount);
            }

            else if (part instanceof CostExile) {
                if ("All".equals(part.getType())) {
                    if (false == GuiDialog.confirm(source, "Do you want to exile all cards in your graveyard?"))
                        return false;
                        
                    List<Card> cards = new ArrayList<Card>(p.getCardsIn(ZoneType.Graveyard));
                    for (final Card card : cards) {
                        p.getGame().getAction().exile(card);
                    }
                } else {
                    CostExile costExile = (CostExile) part;
                    ZoneType from = costExile.getFrom();
                    List<Card> list = CardLists.getValidCards(p.getCardsIn(from), part.getType().split(";"), p, source);
                    final int nNeeded = AbilityUtils.calculateAmount(source, part.getAmount(), ability);
                    if (list.size() < nNeeded)
                        return false;

                    // replace this with input
                    for (int i = 0; i < nNeeded; i++) {
                        final Card c = GuiChoose.oneOrNone("Exile from " + from, list);
                        if (c == null)
                            return false;
                            
                        list.remove(c);
                        p.getGame().getAction().exile(c);
                    }
                }
            }

            else if (part instanceof CostSacrifice) {
                int amount = Integer.parseInt(((CostSacrifice)part).getAmount());
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType().split(";"), p, source);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "sacrifice");
                if(!hasPaid) return false;
            }
            
            else if (part instanceof CostReturn) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType().split(";"), p, source);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "return to hand");
                if(!hasPaid) return false;
            }

            else if (part instanceof CostDiscard) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Hand), part.getType().split(";"), p, source);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "discard");
                if(!hasPaid) return false;
            }
            
            else if (part instanceof CostTapType) {
                List<Card> list = CardLists.getValidCards(p.getCardsIn(ZoneType.Battlefield), part.getType().split(";"), p, source);
                list = CardLists.filter(list, Presets.UNTAPPED);
                int amount = getAmountFromPartX(part, source, sourceAbility);
                boolean hasPaid = payCostPart(sourceAbility, (CostPartWithList)part, amount, list, "tap");
                if(!hasPaid) return false;
            }
            
            else if (part instanceof CostPartMana ) {
                if (!((CostPartMana) part).getManaToPay().equals("0")) // non-zero costs require input
                    mayRemovePart = false; 
            } else
                throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - An unhandled type of cost was met: " + part.getClass());

            if( mayRemovePart )
                remainingParts.remove(part);
        }


        if (remainingParts.isEmpty()) {
            return true;
        }
        if (remainingParts.size() > 1) {
            throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - Too many payment types - " + source);
        }
        costPart = remainingParts.get(0);
        // check this is a mana cost
        if (!(costPart instanceof CostPartMana ))
            throw new RuntimeException("GameActionUtil.payCostDuringAbilityResolve - The remaining payment type is not Mana.");

        InputPayment toSet = current == null 
                ? new InputPayManaExecuteCommands(p, source + "\r\n", ability.getManaCost())
                : new InputPayManaExecuteCommands(p, source + "\r\n" + "Current Card: " + current + "\r\n" , ability.getManaCost());
        FThreads.setInputAndWait(toSet);
        return toSet.isPaid();
    }

    private static boolean payCostPart(SpellAbility sourceAbility, CostPartWithList cpl, int amount, List<Card> list, String actionName) {
        if (list.size() < amount) return false;                     // unable to pay (not enough cards)

        InputSelectCards inp = new InputSelectCardsFromList(amount, amount, list);
        inp.setMessage("Select %d " + cpl.getDescriptiveType() + " card(s) to " + actionName);
        inp.setCancelAllowed(true);
        
        FThreads.setInputAndWait(inp);
        if( inp.hasCancelled() || inp.getSelected().size() != amount)
            return false;

        for(Card c : inp.getSelected()) {
            cpl.executePayment(sourceAbility, c);
        }
        cpl.reportPaidCardsTo(sourceAbility);
        return true;
    }

    // not restricted to combat damage, not restricted to dealing damage to
    // creatures/players
    /**
     * <p>
     * executeDamageDealingEffects.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageDealingEffects(final Card source, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (source.hasKeyword("Lifelink")) {
            source.getController().gainLife(damage, source);
        }

    }

    // not restricted to combat damage, restricted to dealing damage to
    // creatures
    /**
     * <p>
     * executeDamageToCreatureEffects.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @param affected
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageToCreatureEffects(final Card source, final Card affected, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (affected.hasStartOfKeyword("When CARDNAME is dealt damage, destroy it.")) {
            final Ability ability = new AbilityDestroy(source, affected, true);
            final Ability ability2 = new AbilityDestroy(source, affected, false);

            final StringBuilder sb = new StringBuilder();
            sb.append(affected).append(" - destroy");
            ability.setStackDescription(sb.toString());
            ability2.setStackDescription(sb.toString());
            final int amount = affected.getAmountOfKeyword("When CARDNAME is dealt damage, destroy it. It can't be regenerated.");

            for (int i = 0; i < amount; i++) {
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability2);
            }
            final int amount2 = affected.getAmountOfKeyword("When CARDNAME is dealt damage, destroy it.");

            for (int i = 0; i < amount2; i++) {
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);
            }
        }

        // Play the Damage sound
        Singletons.getModel().getGame().getEvents().post(new CardDamagedEvent());
    }

    // this is for cards like Sengir Vampire
    /**
     * <p>
     * executeVampiricEffects.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public static void executeVampiricEffects(final Card c) {
        if (!c.isInPlay()) return;

        for (final String kw : c.getKeyword()) {
            if(!kw.startsWith("Whenever a creature dealt damage by CARDNAME this turn is put into a graveyard, put")) {
                continue;
            }
            final Card thisCard = c;

            final Ability ability2 = new Ability(c, ManaCost.ZERO) {
                @Override
                public void resolve() {
                    CounterType counter = CounterType.P1P1;
                    if (kw.contains("+2/+2")) {
                        counter = CounterType.P2P2;
                    }
                    if (thisCard.isInPlay()) {
                        thisCard.addCounter(counter, 1, true);
                    }
                }
            }; // ability2

            final StringBuilder sb = new StringBuilder();
            sb.append(c.getName());
            if (kw.contains("+2/+2")) {
                sb.append(" - gets a +2/+2 counter");
            } else {
                sb.append(" - gets a +1/+1 counter");
            }
            ability2.setStackDescription(sb.toString());

            Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability2);

        }
    }

    // not restricted to just combat damage, restricted to players
    /**
     * <p>
     * executeDamageToPlayerEffects.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param c
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeDamageToPlayerEffects(final Player player, final Card c, final int damage) {
        if (damage <= 0) {
            return;
        }

        c.getDamageHistory().registerDamage(player);

        // Play the Life Loss sound
        Singletons.getModel().getGame().getEvents().post(new LifeLossEvent());
    }

    // restricted to combat damage, restricted to players
    /**
     * <p>
     * executeCombatDamageToPlayerEffects.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param c
     *            a {@link forge.Card} object.
     * @param damage
     *            a int.
     */
    public static void executeCombatDamageToPlayerEffects(final Player player, final Card c, final int damage) {

        if (damage <= 0) {
            return;
        }

        if (c.hasStartOfKeyword("Poisonous")) {
            final int keywordPosition = c.getKeywordPosition("Poisonous");
            final String parse = c.getKeyword().get(keywordPosition).toString();
            final String[] k = parse.split(" ");
            final int poison = Integer.parseInt(k[1]);
            final Card crd = c;

            final Ability ability = new Ability(c, ManaCost.ZERO) {
                @Override
                public void resolve() {
                    final Player player = crd.getController();
                    final Player opponent = player.getOpponent();
                    opponent.addPoisonCounters(poison, c);
                }
            };

            final StringBuilder sb = new StringBuilder();
            sb.append(c);
            sb.append(" - Poisonous: ");
            sb.append(c.getController().getOpponent());
            sb.append(" gets ");
            sb.append(poison);
            sb.append(" poison counter");
            if (poison != 1) {
                sb.append("s");
            }
            sb.append(".");

            ability.setStackDescription(sb.toString());

            for (String kw : c.getKeyword()) {
                if (kw.startsWith("Poisonous")) {
                    Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);
                }
            }
        }

        if (c.getName().equals("Scalpelexis")) {
            GameActionUtil.playerCombatDamageScalpelexis(c);
        }
        if (c.isEnchantedBy("Celestial Mantle")) {
            GameActionUtil.executeCelestialMantle(c);
        }

        c.getDamageHistory().registerCombatDamage(player);

        // Play the Life Loss sound
        Singletons.getModel().getGame().getEvents().post(SoundEffectType.LifeLoss);
    } // executeCombatDamageToPlayerEffects

    /**
     * <p>
     * executeCelestialMantle.
     * </p>
     * 
     * @param enchanted
     *            a {@link forge.Card} object.
     */
    private static void executeCelestialMantle(final Card enchanted) {
        final ArrayList<Card> auras = enchanted.getEnchantedBy();
        for (final Card aura : auras) {
            if (aura.getName().equals("Celestial Mantle")) {
                final Ability doubleLife = new Ability(aura, ManaCost.ZERO) {
                    @Override
                    public void resolve() {
                        final int life = enchanted.getController().getLife();
                        enchanted.getController().setLife(life * 2, aura);
                    }
                };
                doubleLife.setStackDescription(aura.getName() + " - " + enchanted.getController()
                        + " doubles his or her life total.");

                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(doubleLife);

            }
        }
    }

    /**
     * <p>
     * playerCombatDamageScalpelexis.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    private static void playerCombatDamageScalpelexis(final Card c) {
        final Player player = c.getController();
        final Player opponent = player.getOpponent();

        if (c.getNetAttack() > 0) {
            final Ability ability = new Ability(c, ManaCost.ZERO) {
                @Override
                public void resolve() {

                    final List<Card> libList = new ArrayList<Card>(opponent.getCardsIn(ZoneType.Library));
                    int count = 0;
                    int broken = 0;
                    for (int i = 0; i < libList.size(); i = i + 4) {
                        Card c1 = null;
                        Card c2 = null;
                        Card c3 = null;
                        Card c4 = null;
                        if (i < libList.size()) {
                            c1 = libList.get(i);
                        } else {
                            broken = 1;
                        }
                        if ((i + 1) < libList.size()) {
                            c2 = libList.get(i + 1);
                        } else {
                            broken = 1;
                        }
                        if ((i + 2) < libList.size()) {
                            c3 = libList.get(i + 2);
                        } else {
                            broken = 1;
                        }
                        if ((i + 3) < libList.size()) {
                            c4 = libList.get(i + 3);
                        } else {
                            broken = 1;
                        }
                        if (broken == 0) {
                            if ((c1.getName().contains(c2.getName()) || c1.getName().contains(c3.getName())
                                    || c1.getName().contains(c4.getName()) || c2.getName().contains(c3.getName())
                                    || c2.getName().contains(c4.getName()) || c3.getName().contains(c4.getName()))) {
                                count = count + 1;
                            } else {
                                broken = 1;
                            }
                        }

                    }
                    count = (count * 4) + 4;
                    int max = count;
                    if (libList.size() < count) {
                        max = libList.size();
                    }

                    for (int j = 0; j < max; j++) {
                        final Card c = libList.get(j);
                        Singletons.getModel().getGame().getAction().exile(c);
                    }
                }
            }; // ability

            final StringBuilder sb = new StringBuilder();
            sb.append("Scalpelexis - ").append(opponent);
            sb.append(" exiles the top four cards of his or her library. ");
            sb.append("If two or more of those cards have the same name, repeat this process.");
            ability.setStackDescription(sb.toString());
            ability.setDescription(sb.toString());

            Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

        }
    }

    /** stores the Command. */
    private static Command umbraStalker = new Command() {
        private static final long serialVersionUID = -3500747003228938898L;

        @Override
        public void execute() {
            // get all creatures
            final List<Card> cards = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Umbra Stalker"));
            for (final Card c : cards) {
                final Player player = c.getController();
                final List<Card> grave = player.getCardsIn(ZoneType.Graveyard);
                final int pt = CardFactoryUtil.getNumberOfManaSymbolsByColor("B", grave);
                c.setBaseAttack(pt);
                c.setBaseDefense(pt);
            }
        } // execute()
    };

    /** Constant <code>oldManOfTheSea</code>. */
    private static Command oldManOfTheSea = new Command() {
        private static final long serialVersionUID = 8076177362922156784L;

        @Override
        public void execute() {
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Old Man of the Sea"));
            for (final Card oldman : list) {
                if (!oldman.getGainControlTargets().isEmpty()) {
                    if (oldman.getNetAttack() < oldman.getGainControlTargets().get(0).getNetAttack()) {
                        final List<Command> coms = oldman.getGainControlReleaseCommands();
                        for (int i = 0; i < coms.size(); i++) {
                            coms.get(i).execute();
                        }
                    }
                }
            }
        }
    }; // Old Man of the Sea

    /** Constant <code>liuBei</code>. */
    private static Command liuBei = new Command() {

        private static final long serialVersionUID = 4235093010715735727L;

        @Override
        public void execute() {
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Liu Bei, Lord of Shu"));

            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {

                    final Card c = list.get(i);
                    if (this.getsBonus(c)) {
                        c.setBaseAttack(4);
                        c.setBaseDefense(6);
                    } else {
                        c.setBaseAttack(2);
                        c.setBaseDefense(4);
                    }

                }
            }
        } // execute()

        private boolean getsBonus(final Card c) {
            for (Card card : c.getController().getCardsIn(ZoneType.Battlefield)) {
                if (card.getName().equals("Guan Yu, Sainted Warrior")
                        || card.getName().equals("Zhang Fei, Fierce Warrior")) {
                    return true;
                }
            }
            return false;
        }

    }; // Liu_Bei

    /** Constant <code>Tarmogoyf</code>. */
    private static Command tarmogoyf = new Command() {
        private static final long serialVersionUID = 5895665460018262987L;

        @Override
        public void execute() {
            // get all creatures
            final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Tarmogoyf"));

            for (int i = 0; i < list.size(); i++) {
                final Card c = list.get(i);
                c.setBaseAttack(this.countDiffTypes());
                c.setBaseDefense(c.getBaseAttack() + 1);
            }

        } // execute()

        private int countDiffTypes() {
            final List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Graveyard);

            int count = 0;
            for (Card c : list) {
                if (c.isCreature()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isSorcery()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isInstant()) {
                    count++;
                    break;
                }
            }
            for (Card c : list) {
                if (c.isArtifact()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isEnchantment()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isLand()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isPlaneswalker()) {
                    count++;
                    break;
                }
            }

            for (Card c : list) {
                if (c.isTribal()) {
                    count++;
                    break;
                }
            }
            return count;
        }
    };

    /** Constant <code>commands</code>. */
    private final static HashMap<String, Command> commands = new HashMap<String, Command>();

    static {
        // Please add cards in alphabetical order so they are easier to find

        GameActionUtil.getCommands().put("Liu_Bei", GameActionUtil.liuBei);
        GameActionUtil.getCommands().put("Old_Man_of_the_Sea", GameActionUtil.oldManOfTheSea);
        GameActionUtil.getCommands().put("Tarmogoyf", GameActionUtil.tarmogoyf);
        GameActionUtil.getCommands().put("Umbra_Stalker", GameActionUtil.umbraStalker);

        // The commands above are in alphabetical order by cardname.
    }

    /**
     * Gets the commands.
     * 
     * @return the commands
     */
    public static HashMap<String, Command> getCommands() {
        return GameActionUtil.commands;
    }

    /**
     * Gets the st land mana abilities.
     * 
     * @return the stLandManaAbilities
     */
    public static void grantBasicLandsManaAbilities() {
        final HashMap<String, String> produces = new HashMap<String, String>();
        /*
         * for future use boolean naked =
         * AllZoneUtil.isCardInPlay("Naked Singularity"); boolean twist =
         * AllZoneUtil.isCardInPlay("Reality Twist"); //set up what they
         * produce produces.put("Forest", naked || twist ? "B" : "G");
         * produces.put("Island", naked == true ? "G" : "U"); if(naked)
         * produces.put("Mountain", "U"); else if(twist)
         * produces.put("Mountain", "W"); else produces.put("Mountain",
         * "R"); produces.put("Plains", naked || twist ? "R" : "W");
         * if(naked) produces.put("Swamp", "W"); else if(twist)
         * produces.put("Swamp", "G"); else produces.put("Swamp", "B");
         */
        produces.put("Forest", "G");
        produces.put("Island", "U");
        produces.put("Mountain", "R");
        produces.put("Plains", "W");
        produces.put("Swamp", "B");

        List<Card> lands = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
        lands = CardLists.filter(lands, Presets.LANDS);

        // remove all abilities granted by this Command
        for (final Card land : lands) {
            List<SpellAbility> manaAbs = Lists.newArrayList(land.getManaAbility());
            // will get comodification exception without a different list
            for (final SpellAbility sa : manaAbs) {
                if (sa.getType().equals("BasicLandTypeMana")) {
                    land.removeSpellAbility(sa);
                }
            }
        }

        // add all appropriate mana abilities based on current types
        for (final Card land : lands) {
            for (String landType : Constant.Color.BASIC_LANDS) {

                if (land.isType(landType)) {
                    final SpellAbility sa = AbilityFactory.getAbility("AB$ Mana | Cost$ T | Produced$ " + produces.get(landType)
                            + " | SpellDescription$ Add " + produces.get(landType) + " to your mana pool.", land);
                    sa.setType("BasicLandTypeMana");
                    land.addSpellAbility(sa);
                }
            }
        }

    } // stLandManaAbilities

    /**
     * <p>
     * getAlternativeCosts.
     * </p>
     * 
     * @param sa
     *            a SpellAbility.
     * @return an ArrayList<SpellAbility>.
     * get alternative costs as additional spell abilities
     */
    public static final ArrayList<SpellAbility> getAlternativeCosts(SpellAbility sa) {
        ArrayList<SpellAbility> alternatives = new ArrayList<SpellAbility>();
        Card source = sa.getSourceCard();
        if (!sa.isBasicSpell()) {
            return alternatives;
        }
        for (final String keyword : source.getKeyword()) {
            if (sa.isSpell() && keyword.startsWith("Flashback")) {
                final SpellAbility flashback = sa.copy();
                flashback.setFlashBackAbility(true);
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(ZoneType.Graveyard);
                flashback.setRestrictions(sar);

                // there is a flashback cost (and not the cards cost)
                if (!keyword.equals("Flashback")) {
                    final Cost fbCost = new Cost(source, keyword.substring(10), false);
                    flashback.setPayCosts(fbCost);
                }
                alternatives.add(flashback);
            }
            if (sa.isSpell() && keyword.equals("May be played without paying its mana cost")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(null);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostPartMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost(ManaCost.NO_COST);
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.equals("May be played by your opponent without paying its mana cost")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setZone(null);
                sar.setOpponentOnly(true);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostPartMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost(ManaCost.NO_COST);
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.startsWith("May be played without paying its mana cost and as though it has flash")) {
                final SpellAbility newSA = sa.copy();
                SpellAbilityRestriction sar = new SpellAbilityRestriction();
                sar.setVariables(sa.getRestrictions());
                sar.setInstantSpeed(true);
                newSA.setRestrictions(sar);
                final Cost cost = new Cost(source, "", false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostPartMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost(ManaCost.NO_COST);
                newSA.setDescription(sa.getDescription() + " (without paying its mana cost and as though it has flash)");
                alternatives.add(newSA);
            }
            if (sa.isSpell() && keyword.startsWith("Alternative Cost")) {
                final SpellAbility newSA = sa.copy();
                final Cost cost = new Cost(source, keyword.substring(17), false);
                if (newSA.getPayCosts() != null) {
                    for (final CostPart part : newSA.getPayCosts().getCostParts()) {
                        if (!(part instanceof CostPartMana)) {
                            cost.getCostParts().add(part);
                        }
                    }
                }
                newSA.setBasicSpell(false);
                newSA.setPayCosts(cost);
                newSA.setManaCost(ManaCost.NO_COST);
                String costString = cost.toSimpleString();
                if (costString.equals("")) {
                    costString = "0";
                }
                newSA.setDescription(sa.getDescription() + " (by paying " + costString + " instead of its mana cost)");
                alternatives.add(newSA);
            }
        }
        return alternatives;
    }

    public static Cost combineCosts(SpellAbility sa, String additionalCost) {
        final Cost newCost = new Cost(sa.getSourceCard(), additionalCost, false);
        Cost oldCost = sa.getPayCosts();
        return CostUtil.combineCosts(oldCost, newCost);
    }

    /**
     * <p>
     * getSpliceAbilities.
     * </p>
     * 
     * @param sa
     *            a SpellAbility.
     * @return an ArrayList<SpellAbility>.
     * get abilities with all Splice options
     */
    public static final ArrayList<SpellAbility> getSpliceAbilities(SpellAbility sa) {
        ArrayList<SpellAbility> newSAs = new ArrayList<SpellAbility>();
        ArrayList<SpellAbility> allSAs = new ArrayList<SpellAbility>();
        allSAs.add(sa);
        Card source = sa.getSourceCard();

        if (!sa.isSpell() || !source.isType("Arcane") || sa.getApi() == null) {
            return newSAs;
        }

        for (Card c : sa.getActivatingPlayer().getCardsIn(ZoneType.Hand)) {
            if (c.equals(source)) {
                continue;
            }
            for (String keyword : c.getKeyword()) {
                if (!keyword.startsWith("Splice")) {
                    continue;
                }
                String newSubSAString = c.getCharacteristics().getIntrinsicAbility().get(0);
                newSubSAString = newSubSAString.replace("SP", "DB");
                final AbilitySub newSubSA = (AbilitySub) AbilityFactory.getAbility(newSubSAString, c);
                ArrayList<SpellAbility> addSAs = new ArrayList<SpellAbility>();
                // Add the subability to all existing variants
                for (SpellAbility s : allSAs) {
                    //create a new spell copy
                    final SpellAbility newSA = s.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(combineCosts(newSA, keyword.substring(19)));
                    newSA.setManaCost(ManaCost.NO_COST);
                    newSA.setDescription(s.getDescription() + " (Splicing " + c + " onto it)");
                    newSA.addSplicedCards(c);

                    // copy all subAbilities
                    SpellAbility child = newSA;
                    while (child.getSubAbility() != null) {
                        AbilitySub newChild = child.getSubAbility().getCopy();
                        child.setSubAbility(newChild);
                        child.setActivatingPlayer(s.getActivatingPlayer());
                        child = newChild;
                    }

                    //add the spliced ability to the end of the chain
                    child.setSubAbility(newSubSA);

                    //set correct source and activating player to all the spliced abilities
                    child = newSubSA;
                    while (child != null) {
                        child.setSourceCard(source);
                        child.setActivatingPlayer(s.getActivatingPlayer());
                        child = child.getSubAbility();
                    }
                    newSAs.add(0, newSA);
                    addSAs.add(newSA);
                }
                allSAs.addAll(addSAs);
                break;
            }
        }

        return newSAs;
    }

    /**
     * get optional additional costs.
     * 
     * @param original
     *            the original sa
     * @return an ArrayList<SpellAbility>.
     */
    public static ArrayList<SpellAbility> getOptionalAdditionalCosts(final SpellAbility original) {
        final ArrayList<SpellAbility> abilities = new ArrayList<SpellAbility>();
        final ArrayList<SpellAbility> newAbilities = new ArrayList<SpellAbility>();
        final Card source = original.getSourceCard();
        abilities.add(original);
        if (!original.isSpell()) {
            return abilities;
        }

        // Buyback, Kicker
        for (String keyword : source.getKeyword()) {
            if (keyword.startsWith("Buyback")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, keyword.substring(8)));
                    newSA.setManaCost(ManaCost.NO_COST);
                    newSA.setDescription(sa.getDescription() + " (with Buyback)");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts("Buyback");
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("Kicker")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, keyword.substring(7)));
                    newSA.setManaCost(ManaCost.NO_COST);
                    final Cost cost = new Cost(source, keyword.substring(7), false);
                    newSA.setDescription(sa.getDescription() + " (Kicker " + cost.toSimpleString() + ")");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts(keyword);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("AlternateAdditionalCost")) {
                String costString1 = keyword.split(":")[1];
                String costString2 = keyword.split(":")[2];
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, costString1));
                    newSA.setManaCost(ManaCost.NO_COST);
                    final Cost cost1 = new Cost(source, costString1, false);
                    newSA.setDescription(sa.getDescription() + " (Additional cost " + cost1.toSimpleString() + ")");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                    //second option
                    final SpellAbility newSA2 = sa.copy();
                    newSA2.setBasicSpell(false);
                    newSA2.setPayCosts(GameActionUtil.combineCosts(newSA2, costString2));
                    newSA2.setManaCost(ManaCost.NO_COST);
                    final Cost cost2 = new Cost(source, costString2, false);
                    newSA2.setDescription(sa.getDescription() + " (Additional cost " + cost2.toSimpleString() + ")");
                    ArrayList<String> newoacs2 = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA2.setOptionalAdditionalCosts(newoacs2);
                    if (newSA2.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA2);
                    }
                }
                abilities.clear();
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            } else if (keyword.startsWith("Conspire")) {
                for (SpellAbility sa : abilities) {
                    final SpellAbility newSA = sa.copy();
                    newSA.setBasicSpell(false);
                    final String conspireCost = "tapXType<2/Creature.SharesColorWith/untapped creature you control"
                            + " that shares a color with " + source.getName() + ">";
                    newSA.setPayCosts(GameActionUtil.combineCosts(newSA, conspireCost));
                    newSA.setManaCost(ManaCost.NO_COST);
                    newSA.setDescription(sa.getDescription() + " (Conspire)");
                    ArrayList<String> newoacs = new ArrayList<String>();
                    newoacs.addAll(sa.getOptionalAdditionalCosts());
                    newSA.setOptionalAdditionalCosts(newoacs);
                    newSA.addOptionalAdditionalCosts(keyword);
                    if (newSA.canPlay()) {
                        newAbilities.add(newAbilities.size(), newSA);
                    }
                }
                abilities.addAll(0, newAbilities);
                newAbilities.clear();
            }
        }

        // Splice
        for (SpellAbility sa : abilities) {
            newAbilities.addAll(GameActionUtil.getSpliceAbilities(sa));
        }
        abilities.addAll(newAbilities);
        newAbilities.clear();

        return abilities;
    }

    /**
     * <p>
     * hasUrzaLands.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player.Player} object.
     * @return a boolean.
     */
    private static boolean hasUrzaLands(final Player p) {
        final List<Card> landsControlled = p.getCardsIn(ZoneType.Battlefield);
        return Iterables.any(landsControlled, CardPredicates.nameEquals("Urza's Mine"))
                && Iterables.any(landsControlled, CardPredicates.nameEquals("Urza's Tower"))
                && Iterables.any(landsControlled, CardPredicates.nameEquals("Urza's Power Plant"));
    }

    /**
     * <p>
     * generatedMana.
     * </p>
     * 
     * @param abMana
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    public static String generatedMana(final SpellAbility sa) {
        // Calculate generated mana here for stack description and resolving

        int amount = sa.hasParam("Amount") ? AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("Amount"), sa) : 1;

        AbilityManaPart abMana = sa.getManaPart();
        String baseMana;
        if (abMana.isComboMana()) {
            baseMana = abMana.getExpressChoice();
            if (baseMana.isEmpty()) {
                baseMana = abMana.getOrigProduced();
            }
        }
        else if (abMana.isAnyMana()) {
            baseMana = abMana.getExpressChoice();
            if (baseMana.isEmpty()) {
                baseMana = "Any";
            }
        }
        else if (sa.getApi() == ApiType.ManaReflected) {
            baseMana = abMana.getExpressChoice();
        }
        else {
            baseMana = abMana.mana();
        }

        if (sa.hasParam("Bonus")) {
            // For mana abilities that get a bonus
            // Bonus currently MULTIPLIES the base amount. Base Amounts should
            // ALWAYS be Base
            int bonus = 0;
            if (sa.getParam("Bonus").equals("UrzaLands")) {
                if (hasUrzaLands(sa.getActivatingPlayer())) {
                    bonus = Integer.parseInt(sa.getParam("BonusProduced"));
                }
            }

            amount += bonus;
        }

        if (sa.getSubAbility() != null) {
            // Mark SAs with subAbilities as undoable. These are generally things like damage, and other stuff
            // that's hard to track and remove
            sa.setUndoable(false);
        } else {      
            try {
                if ((sa.getParam("Amount") != null) && (amount != Integer.parseInt(sa.getParam("Amount")))) {
                    sa.setUndoable(false);
                }
            } catch (final NumberFormatException n) {
                sa.setUndoable(false);
            }
        }

        final StringBuilder sb = new StringBuilder();
        if (amount == 0) {
            sb.append("0");
        }
        else if (abMana.isComboMana()) {
            // amount is already taken care of in resolve method for combination mana, just append baseMana
            sb.append(baseMana);
        }
        else {
            try {
                // if baseMana is an integer(colorless), just multiply amount
                // and baseMana
                final int base = Integer.parseInt(baseMana);
                sb.append(base * amount);
            } catch (final NumberFormatException e) {
                for (int i = 0; i < amount; i++) {
                    if (i != 0) {
                        sb.append(" ");
                    }
                    sb.append(baseMana);
                }
            }
        }
        return sb.toString();
    }
} // end class GameActionUtil
