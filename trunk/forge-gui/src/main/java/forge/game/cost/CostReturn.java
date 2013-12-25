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
import java.util.List;

import forge.ai.ComputerUtil;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.gui.input.InputSelectCardsFromList;

/**
 * The Class CostReturn.
 */
public class CostReturn extends CostPartWithList {
    // Return<Num/Type{/TypeDescription}>

    /**
     * Instantiates a new cost return.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostReturn(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Return ");

        final Integer i = this.convertAmount();
        String pronoun = "its";

        if (this.payCostFromSource()) {
            sb.append(this.getType());
        } else {
            final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
            if (i != null) {
                sb.append(Cost.convertIntAndTypeToWords(i, desc));
                if (i > 1) {
                    pronoun = "their";
                }
            } else {
                sb.append(Cost.convertAmountTypeToWords(this.getAmount(), desc));
            }

            sb.append(" you control");
        }
        sb.append(" to ").append(pronoun).append(" owner's hand");
        return sb.toString();
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
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        if (!this.payCostFromSource()) {
            boolean needsAnnoucement = ability.hasParam("Announce") && this.getType().contains(ability.getParam("Announce"));
            
            List<Card> typeList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));
            typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);

            final Integer amount = this.convertAmount();
            if (!needsAnnoucement && amount != null && typeList.size() < amount) {
                return false;
            }
        } else if (!source.isInPlay()) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean payHuman(final SpellAbility ability, final Player payer) {
        final String amount = this.getAmount();
        final Card source = ability.getSourceCard();
        Integer c = this.convertAmount();

        final List<Card> list = payer.getCardsIn(ZoneType.Battlefield);
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(source, ability, list.size());
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }
        if (this.payCostFromSource()) {
            final Card card = ability.getSourceCard();
            if (card.getController() == payer && card.isInPlay()) {
                return payer.getController().confirmPayment(this, "Return " + card.getName() + " to hand?") && executePayment(ability, card);
            }
        }
        else {
            List<Card> validCards = CardLists.getValidCards(ability.getActivatingPlayer().getCardsIn(ZoneType.Battlefield), this.getType().split(";"), ability.getActivatingPlayer(), ability.getSourceCard());

            InputSelectCardsFromList inp = new InputSelectCardsFromList(c, c, validCards);
            inp.setMessage("Return %d " + this.getType() + " " + this.getType() + " card(s) to hand");
            inp.showAndWait();
            if (inp.hasCancelled())
                return false;
            
            for(Card crd : inp.getSelected()) 
                executePayment(ability, crd);
            return true;
        }
       return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#decideAIPayment(forge.card.spellability.SpellAbility
     * , forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final PaymentDecision decideAIPayment(final Player ai, final SpellAbility ability, final Card source) {
        if (this.payCostFromSource())
            return new PaymentDecision(source);
        
        Integer c = this.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, this.getAmount(), ability);
        }

        List<Card> res = ComputerUtil.chooseReturnType(ai, this.getType(), source, ability.getTargetCard(), c);
        return res.isEmpty() ? null : new PaymentDecision(res);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        targetCard.getGame().getAction().moveToHand(targetCard);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "Returned";
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPart#payAI(forge.card.cost.PaymentDecision, forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public boolean payAI(PaymentDecision decision, Player ai, SpellAbility ability, Card source) {
        for (final Card c : decision.cards) {
            executePayment(ability, c);
        }
        return true;
    }

    // Inputs


}
