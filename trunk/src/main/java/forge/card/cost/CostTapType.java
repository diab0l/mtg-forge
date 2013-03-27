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
package forge.card.cost;

import java.util.ArrayList;
import java.util.List;
import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.FThreads;
import forge.Singletons;
import forge.card.ability.AbilityUtils;
import forge.card.spellability.SpellAbility;
import forge.control.input.InputPayment;
import forge.game.GameState;
import forge.game.ai.ComputerUtil;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.view.ButtonUtil;

/**
 * The Class CostTapType.
 */
public class CostTapType extends CostPartWithList {

    /** 
     * TODO: Write javadoc for this type.
     *
     */
    public static final class InputPayCostTapType extends InputPayCostBase {
        private final CostTapType tapType;
        private final int nCards;
        private final List<Card> cardList;
        private static final long serialVersionUID = 6438988130447851042L;
        private int nTapped = 0;

        /**
         * TODO: Write javadoc for Constructor.
         * @param sa
         * @param tapType
         * @param nCards
         * @param cardList
         * @param payment
         */
        public InputPayCostTapType(CostTapType tapType, int nCards, List<Card> cardList) {
            this.tapType = tapType;
            this.nCards = nCards;
            this.cardList = cardList;
        }

        @Override
        public void showMessage() {

            final int left = nCards - this.nTapped;
            showMessage("Select a " + tapType.getDescription() + " to tap (" + left + " left)");
            ButtonUtil.enableOnlyCancel();
            if (nCards == 0) {
                this.done();
            }
        }



        @Override
        public void selectCard(final Card card) {
            Zone zone = Singletons.getModel().getGame().getZoneOf(card);
            if (zone.is(ZoneType.Battlefield) && cardList.contains(card) && card.isUntapped()) {
                // send in List<Card> for Typing
                tapType.executePayment(null, card);
                cardList.remove(card);

                this.nTapped++;

                if (this.nTapped == nCards) {
                    this.done();
                } else if (cardList.size() == 0) {
                    // happen
                    this.cancel();
                } else {
                    this.showMessage();
                }
            }
        }
    }

    /**
     * Instantiates a new cost tap type.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostTapType(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    @Override
    public boolean isReusable() { return true; }


    /**
     * Gets the description.
     * 
     * @return the description
     */
    public final String getDescription() {
        return this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Tap ");

        final Integer i = this.convertAmount();
        final String desc = this.getDescription();

        sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), "untapped " + desc));

        sb.append(" you control");

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#refund(forge.Card)
     */
    @Override
    public final void refund(final Card source) {
        for (final Card c : this.getList()) {
            c.setTapped(false);
        }

        this.getList().clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability, final Card source, final Player activator, final Cost cost, final GameState game) {
        List<Card> typeList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));

        typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);

        if (cost.hasTapCost()) {
            typeList.remove(source);
        }
        typeList = CardLists.filter(typeList, Presets.UNTAPPED);

        final Integer amount = this.convertAmount();
        if ((typeList.size() == 0) || ((amount != null) && (typeList.size() < amount))) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#payAI(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final void payAI(final AIPlayer ai, final SpellAbility ability, final Card source, final CostPayment payment, final GameState game) {
        for (final Card c : this.getList()) {
            c.tap();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean payHuman(final SpellAbility ability, final GameState game) {
        List<Card> typeList = new ArrayList<Card>(ability.getActivatingPlayer().getCardsIn(ZoneType.Battlefield));
        typeList = CardLists.getValidCards(typeList, this.getType().split(";"), ability.getActivatingPlayer(), ability.getSourceCard());
        typeList = CardLists.filter(typeList, Presets.UNTAPPED);
        final String amount = this.getAmount();
        final Card source = ability.getSourceCard();
        Integer c = this.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = CostUtil.chooseXValue(source, ability, typeList.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        InputPayment inp = new InputPayCostTapType(this, c, typeList);
        FThreads.setInputAndWait(inp);
        return inp.isPaid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#decideAIPayment(forge.card.spellability.SpellAbility
     * , forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean decideAIPayment(final AIPlayer ai, final SpellAbility ability, final Card source, final CostPayment payment) {
        final boolean tap = payment.getCost().hasTapCost();
        final String amount = this.getAmount();
        Integer c = this.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            if (sVar.equals("XChoice")) {
                List<Card> typeList =
                        CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), this.getType().split(";"), ability.getActivatingPlayer(), ability.getSourceCard());
                typeList = CardLists.filter(typeList, Presets.UNTAPPED);
                c = typeList.size();
                source.setSVar("ChosenX", "Number$" + Integer.toString(c));
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        this.setList(ComputerUtil.chooseTapType(ai, this.getType(), source, tap, c));

        if (this.getList() == null) {
            System.out.println("Couldn't find a valid card to tap for: " + source.getName());
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public void executePayment(SpellAbility ability, Card targetCard) {
        addToList(targetCard);
        targetCard.tap();
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "Tapped";
    }

    // Inputs

}
