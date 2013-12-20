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
package forge.game.cost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CounterType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.input.InputSelectCards;

/**
 * The Class CostRemoveCounter.
 */
public class CostRemoveCounter extends CostPartWithList {
    // SubCounter<Num/Counter/{Type/TypeDescription/Zone}>

    // Here are the cards that have RemoveCounter<Type>
    // Ion Storm, Noviken Sages, Ghave, Guru of Spores, Power Conduit (any
    // Counter is tough),
    // Quillspike, Rift Elemental, Sage of Fables, Spike Rogue

    /** 
     * TODO: Write javadoc for this type.
     *
     */
    public static final class InputSelectCardToRemoveCounter extends InputSelectCards {
        private static final long serialVersionUID = 2685832214519141903L;

        private final Map<Card,Integer> cardsChosen;
        private final CounterType counterType;
        private final List<Card> validChoices;

        public InputSelectCardToRemoveCounter(int cntCounters, CounterType cType, List<Card> validCards) {
            super(cntCounters, cntCounters);
            this.validChoices = validCards;
            counterType = cType;
            cardsChosen = cntCounters > 1 ? new HashMap<Card, Integer>() : null; 
        }

        @Override
        protected boolean selectEntity(Card c) {
            if (!isValidChoice(c)) {
                return false;
            }

            int tc = getTimesSelected(c);
            if( tc == 0)
                selected.add(c);
            else
                cardsChosen.put(c, tc+1);

            onSelectStateChanged(c, true);
            return true;
        }

        @Override
        protected boolean hasEnoughTargets() {
            return hasAllTargets();
        }

        @Override
        protected boolean hasAllTargets() {
            int sum = getDistibutedCounters();
            return sum >= max;
        }

        protected String getMessage() {
            return max == Integer.MAX_VALUE
                ? String.format(message, getDistibutedCounters())
                : String.format(message, max - getDistibutedCounters());
        }

        private int getDistibutedCounters() {
            int sum = 0;
            for(Card c : selected) {
                sum += max == 1 || cardsChosen.get(c) == null ? 1 : cardsChosen.get(c).intValue();
            }
            return sum;
        }

        @Override
        protected final boolean isValidChoice(Card choice) {
            return validChoices.contains(choice) && choice.getCounters(counterType) > getTimesSelected(choice);
        }

        public int getTimesSelected(Card c) {
            return selected.contains(c) ? max == 1 || cardsChosen.get(c) == null ? 1 : cardsChosen.get(c).intValue() : 0;
        }
    }

    @Override
    public final boolean payHuman(final SpellAbility ability, final Player activator) {
        final String amount = this.getAmount();
        final Card source = ability.getSourceCard();
        Integer c = this.convertAmount();
        final String type = this.getType();

        String sVarAmount = ability.getSVar(amount);
        cntRemoved = 1;
        if (c != null)  
            cntRemoved = c.intValue();
        else if (!"XChoice".equals(sVarAmount)) {
            cntRemoved = AbilityUtils.calculateAmount(source, amount, ability);
        }

        if (this.payCostFromSource()) {
            int maxCounters = source.getCounters(this.counter);
            if (amount.equals("All"))
                cntRemoved = maxCounters;
            else if ( c == null && "XChoice".equals(sVarAmount)) { 
                cntRemoved = chooseXValue(source, ability, maxCounters);
            }

            if (maxCounters < cntRemoved) 
                return false;
            cntRemoved = cntRemoved >= 0 ? cntRemoved : maxCounters;
            source.setSVar("CostCountersRemoved", Integer.toString(cntRemoved));
            executePayment(ability, source);
            return true;
        } else if (type.equals("OriginalHost")) {
            int maxCounters = ability.getOriginalHost().getCounters(this.counter);
            if (amount.equals("All")) {
                cntRemoved = maxCounters;
            }
            if (maxCounters < cntRemoved) 
                return false;
            cntRemoved = cntRemoved >= 0 ? cntRemoved : maxCounters;
            source.setSVar("CostCountersRemoved", Integer.toString(cntRemoved));
            executePayment(ability, ability.getOriginalHost());
            return true;
        }

        List<Card> validCards = CardLists.getValidCards(activator.getCardsIn(getZone()), type.split(";"), activator, source);
        if (this.getZone().equals(ZoneType.Battlefield)) {
            final InputSelectCardToRemoveCounter inp = new InputSelectCardToRemoveCounter(cntRemoved, getCounter(), validCards);
            inp.setMessage("Remove %d " + getCounter().getName() + " counters from " + getDescriptiveType());
            inp.setCancelAllowed(true);
            inp.showAndWait();
            if(inp.hasCancelled())
                return false;

            // Have to hack here: remove all counters minus one, without firing any triggers,
            // triggers will fire when last is removed by executePayment.
            // They don't care how many were removed anyway
            int sum = 0;
            for(Card crd : inp.getSelected()) {
                int removed = inp.getTimesSelected(crd);
                sum += removed;
                if(removed < 2) continue;
                int oldVal = crd.getCounters().get(getCounter()).intValue();
                crd.getCounters().put(getCounter(), Integer.valueOf(oldVal - removed + 1));
            }
            source.setSVar("CostCountersRemoved", Integer.toString(sum));
            cntRemoved = 1;
            return executePayment(ability, inp.getSelected());

        } 

        // Rift Elemental only - always removes 1 counter, so there will be no code for N counters.
        List<Card> suspended = new ArrayList<Card>();
        for(Card crd : validCards)
            if(crd.getCounters( getCounter()) > 0 )
                suspended.add(crd);

        if(suspended.isEmpty())
            return false;

        
        final Card card = GuiChoose.oneOrNone("Remove counter(s) from a card in " + getZone(), suspended);
        if( null == card)
            return false;
        
        source.setSVar("CostCountersRemoved", "1");
        return executePayment(ability, card);
    }

    private final CounterType counter;
    private final ZoneType zone;
    private int cntRemoved;
    

    /**
     * Gets the counter.
     * 
     * @return the counter
     */
    public final CounterType getCounter() {
        return this.counter;
    }

    /**
     * @return the zone
     */
    private ZoneType getZone() {
        return zone;
    }

    /**
     * Instantiates a new cost remove counter.
     * 
     * @param amount
     *            the amount
     * @param counter
     *            the counter
     * @param type
     *            the type
     * @param description
     *            the description
     * @param zone the zone.
     */
    public CostRemoveCounter(final String amount, final CounterType counter, final String type, final String description, ZoneType zone) {
        super(amount, type, description);

        this.counter = counter;
        this.zone = zone;
    }

    @Override
    public boolean isReusable() { return true; }

    @Override
    public boolean isUndoable() { return true; }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        if (this.counter == CounterType.LOYALTY) {
            sb.append("-").append(this.getAmount());
        } else {
            sb.append("Remove ");
            final Integer i = this.convertAmount();
            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), this.counter.getName() + " counter"));

            if (this.getAmount().equals("All")) {
                sb.append("s");
            }

            sb.append(" from ");

            if (this.payCostFromSource()) {
                sb.append(this.getType());
            } else {
                final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
                sb.append(desc);
            }
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#refund(forge.Card)
     */
    @Override
    public final void refund(final Card source) {
        int refund = this.getList().size() == 1 ? this.cntRemoved : 1; // is wrong for Ooze Flux and Novijen Sages
        for (final Card c : this.getList()) {
            c.addCounter(this.counter, refund, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability) {
        final CounterType cntrs = this.getCounter();
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        final String type = this.getType();

        final Integer amount = this.convertAmount();
        if (this.payCostFromSource()) {
            if ((amount != null) && ((source.getCounters(cntrs) - amount) < 0)) {
                return false;
            }
        }
        else {
            List<Card> typeList;
            if (type.equals("OriginalHost")) {
                typeList = Lists.newArrayList(ability.getOriginalHost());
            } else {
                typeList = CardLists.getValidCards(activator.getCardsIn(this.getZone()), type.split(";"), activator, source);
            }
            if (amount != null) {
                for (Card c : typeList) {
                    if (c.getCounters(cntrs) - amount >= 0) {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#payAI(forge.card.cost.PaymentDecision, forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public boolean payAI(PaymentDecision decision, Player ai, SpellAbility ability, Card source) {
        final String amount = this.getAmount();
        Integer c = this.convertAmount();
        if (c == null) {
            if (amount.equals("All")) {
                cntRemoved = source.getCounters(this.counter);
            } else {
                cntRemoved = AbilityUtils.calculateAmount(source, amount, ability);
            }
        } else 
            cntRemoved = c.intValue();

        if (this.payCostFromSource()) {
            executePayment(ability, source);
        } else {
            for (final Card card : decision.cards) {
                executePayment(ability, card);
            }
        }
        source.setSVar("CostCountersRemoved", Integer.toString(cntRemoved));
        return true;
    }

    @Override
    protected void doPayment(SpellAbility ability, Card targetCard){
        targetCard.subtractCounter(this.getCounter(), cntRemoved);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "CounterRemove";
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPart#decideAIPayment(forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public PaymentDecision decideAIPayment(Player ai, SpellAbility ability, Card source) {
        final String amount = this.getAmount();
        Integer c = this.convertAmount();
        final String type = this.getType();

        if (c == null) {
            final String sVar = ability.getSVar(amount);
            if (sVar.equals("XChoice")) {
                return null;
            }
            if (amount.equals("All")) {
                c = source.getCounters(this.counter);
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (!this.payCostFromSource()) {
            List<Card> typeList;
            if (type.equals("OriginalHost")) {
                typeList = Lists.newArrayList(ability.getOriginalHost());
            } else {
                typeList = CardLists.getValidCards(ai.getCardsIn(this.getZone()), type.split(";"), ai, source);
            }
            for (Card card : typeList) {
                if (card.getCounters(this.getCounter()) >= c) {
                    return new PaymentDecision(card);
                }
            }
            return null;
        }

        if (c > source.getCounters(this.getCounter())) {
            System.out.println("Not enough " + this.counter + " on " + source.getName());
            return null;
        }
        this.cntRemoved = c;
        return new PaymentDecision(source);
    }
}
