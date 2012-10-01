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

import forge.AllZone;
import forge.Card;
import forge.CardList;
import forge.CardListUtil;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.control.input.Input;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.match.CMatchUI;
import forge.view.ButtonUtil;

/**
 * The Class CostReveal.
 */
public class CostReveal extends CostPartWithList {
    // Reveal<Num/Type/TypeDescription>

    /**
     * Instantiates a new cost reveal.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostReveal(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability, final Card source, final Player activator, final Cost cost) {
        CardList handList = activator.getCardsIn(ZoneType.Hand);
        final String type = this.getType();
        final Integer amount = this.convertAmount();

        if (this.getThis()) {
            if (!source.isInZone(ZoneType.Hand)) {
                return false;
            }
        } else if (this.getType().equals("Hand")) {
            return true;
        } else {
            if (ability.isSpell()) {
                handList.remove(source); // can't pay for itself
            }
            handList = CardListUtil.getValidCards(handList, type.split(";"), activator, source);
            if ((amount != null) && (amount > handList.size())) {
                // not enough cards in hand to pay
                return false;
            }
            System.out.println("revealcost - " + amount + type + handList);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#decideAIPayment(forge.card.spellability.SpellAbility
     * , forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean decideAIPayment(final SpellAbility ability, final Card source, final CostPayment payment) {
        final String type = this.getType();
        final Player activator = ability.getActivatingPlayer();
        CardList hand = activator.getCardsIn(ZoneType.Hand);
        this.resetList();

        if (this.getThis()) {
            if (!hand.contains(source)) {
                return false;
            }

            this.getList().add(source);
        } else if (this.getType().equals("Hand")) {
            this.setList(activator.getCardsIn(ZoneType.Hand));
            return true;
        } else {
            hand = CardListUtil.getValidCards(hand, type.split(";"), activator, source);
            Integer c = this.convertAmount();
            if (c == null) {
                final String sVar = ability.getSVar(this.getAmount());
                if (sVar.equals("XChoice")) {
                    c = hand.size();
                } else {
                    c = AbilityFactory.calculateAmount(source, this.getAmount(), ability);
                }
            }

            this.setList(ComputerUtil.discardNumTypeAI(c, type.split(";"), ability));
        }
        return this.getList() != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#payAI(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final void payAI(final SpellAbility ability, final Card source, final CostPayment payment) {
        GuiChoose.oneOrNone("Revealed cards:", this.getList());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean payHuman(final SpellAbility ability, final Card source, final CostPayment payment) {
        final Player activator = ability.getActivatingPlayer();
        final String amount = this.getAmount();
        this.resetList();

        if (this.getThis()) {
            this.addToList(source);
            payment.setPaidManaPart(this);
        } else if (this.getType().equals("Hand")) {
            this.setList(activator.getCardsIn(ZoneType.Hand));
            payment.setPaidManaPart(this);
        } else {
            Integer num = this.convertAmount();

            CardList handList = activator.getCardsIn(ZoneType.Hand);
            handList = CardListUtil.getValidCards(handList, this.getType().split(";"), activator, ability.getSourceCard());

            if (num == null) {
                final String sVar = ability.getSVar(amount);
                if (sVar.equals("XChoice")) {
                    num = CostUtil.chooseXValue(source, ability, handList.size());
                } else {
                    num = AbilityFactory.calculateAmount(source, amount, ability);
                }
            }
            if (num > 0) {
                CostUtil.setInput(CostReveal.inputRevealCost(this.getType(), handList, payment, this, ability, num));
                return false;
            } else {
                payment.setPaidManaPart(this);
            }
        }
        this.addListToHash(ability, "Revealed");
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Reveal ");

        final Integer i = this.convertAmount();

        if (this.getThis()) {
            sb.append(this.getType());
        } else if (this.getType().equals("Hand")) {
            return ("Reveal you hand");
        } else {
            final StringBuilder desc = new StringBuilder();

            if (this.getType().equals("Card")) {
                desc.append("Card");
            } else {
                desc.append(this.getTypeDescription() == null ? this.getType() : this.getTypeDescription()).append(
                        " card");
            }

            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), desc.toString()));
        }
        sb.append(" from your hand");

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#refund(forge.Card)
     */
    @Override
    public void refund(final Card source) {

    }

    // Inputs

    /**
     * <p>
     * input_discardCost.
     * </p>
     * 
     * @param discType
     *            a {@link java.lang.String} object.
     * @param handList
     *            a {@link forge.CardList} object.
     * @param payment
     *            a {@link forge.card.cost.CostPayment} object.
     * @param part
     *            TODO
     * @param sa
     *            TODO
     * @param nNeeded
     *            a int.
     * @return a {@link forge.control.input.Input} object.
     */
    public static Input inputRevealCost(final String discType, final CardList handList, final CostPayment payment,
            final CostReveal part, final SpellAbility sa, final int nNeeded) {
        final Input target = new Input() {
            private static final long serialVersionUID = -329993322080934435L;

            private int nReveal = 0;

            @Override
            public void showMessage() {
                if (nNeeded == 0) {
                    this.done();
                }

                /*if (handList.size() + this.nReveal < nNeeded) {
                    this.stop();
                }*/
                final StringBuilder type = new StringBuilder("");
                if (!discType.equals("Card")) {
                    type.append(" ").append(discType);
                }
                final StringBuilder sb = new StringBuilder();
                sb.append("Select a ");
                sb.append(part.getDescriptiveType());
                sb.append(" to reveal.");
                if (nNeeded > 1) {
                    sb.append(" You have ");
                    sb.append(nNeeded - this.nReveal);
                    sb.append(" remaining.");
                }
                CMatchUI.SINGLETON_INSTANCE.showMessage(sb.toString());
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.cancel();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (zone.is(ZoneType.Hand) && handList.contains(card)) {
                    // send in CardList for Typing
                    handList.remove(card);
                    part.addToList(card);
                    this.nReveal++;

                    // in case no more cards in hand
                    if (this.nReveal == nNeeded) {
                        this.done();
                    } else if (AllZone.getHumanPlayer().getZone(ZoneType.Hand).size() == 0) {
                        // really
                        // shouldn't
                        // happen
                        this.cancel();
                    } else {
                        this.showMessage();
                    }
                }
            }

            public void cancel() {
                this.stop();
                payment.cancelCost();
            }

            public void done() {
                this.stop();
                // "Inform" AI of the revealed cards
                part.addListToHash(sa, "Revealed");
                payment.paidCost(part);
            }
        };

        return target;
    } // input_discard()

}
