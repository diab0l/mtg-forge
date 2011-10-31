package forge.card.cost;

import javax.swing.JOptionPane;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.ButtonUtil;
import forge.Card;
import forge.CardList;
import forge.ComputerUtil;
import forge.Constant.Zone;
import forge.Player;
import forge.PlayerZone;
import forge.card.abilityFactory.AbilityFactory;
import forge.card.spellability.SpellAbility;
import forge.gui.input.Input;

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

        if (this.getThis()) {
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
     * @see forge.card.cost.CostPart#refund(forge.Card)
     */
    @Override
    public void refund(final Card source) {
        // TODO Auto-generated method stub

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
        if (!this.getThis()) {
            CardList typeList = activator.getCardsIn(Zone.Battlefield);
            typeList = typeList.getValidCards(this.getType().split(";"), activator, source);

            final Integer amount = this.convertAmount();
            if ((amount != null) && (typeList.size() < amount)) {
                return false;
            }
        } else if (!AllZoneUtil.isCardInPlay(source)) {
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
    public final void payAI(final SpellAbility ability, final Card source, final Cost_Payment payment) {
        for (final Card c : this.list) {
            AllZone.getGameAction().moveToHand(c);
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
    public final boolean payHuman(final SpellAbility ability, final Card source, final Cost_Payment payment) {
        final String amount = this.getAmount();
        Integer c = this.convertAmount();
        final Player activator = ability.getActivatingPlayer();
        final CardList list = activator.getCardsIn(Zone.Battlefield);
        if (c == null) {
            final String sVar = source.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = CostUtil.chooseXValue(source, list.size());
            } else {
                c = AbilityFactory.calculateAmount(source, amount, ability);
            }
        }
        if (this.getThis()) {
            CostUtil.setInput(CostReturn.returnThis(ability, payment, this));
        } else {
            CostUtil.setInput(CostReturn.returnType(ability, this.getType(), payment, this, c));
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
    public final boolean decideAIPayment(final SpellAbility ability, final Card source, final Cost_Payment payment) {
        this.resetList();
        if (this.getThis()) {
            this.list.add(source);
        } else {
            Integer c = this.convertAmount();
            if (c == null) {
                c = AbilityFactory.calculateAmount(source, this.getAmount(), ability);
            }

            this.list = ComputerUtil.chooseReturnType(this.getType(), source, ability.getTargetCard(), c);
            if (this.list == null) {
                return false;
            }
        }
        return true;
    }

    // Inputs

    /**
     * <p>
     * returnType.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @param payment
     *            a {@link forge.card.cost.Cost_Payment} object.
     * @param part
     *            TODO
     * @param nNeeded
     *            the n needed
     * @return a {@link forge.gui.input.Input} object.
     */
    public static Input returnType(final SpellAbility sa, final String type, final Cost_Payment payment,
            final CostReturn part, final int nNeeded) {
        final Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            private CardList typeList;
            private int nReturns = 0;

            @Override
            public void showMessage() {
                if (nNeeded == 0) {
                    this.done();
                }

                final StringBuilder msg = new StringBuilder("Return ");
                final int nLeft = nNeeded - this.nReturns;
                msg.append(nLeft).append(" ");
                msg.append(type);
                if (nLeft > 1) {
                    msg.append("s");
                }

                this.typeList = sa.getActivatingPlayer().getCardsIn(Zone.Battlefield);
                this.typeList = this.typeList.getValidCards(type.split(";"), sa.getActivatingPlayer(),
                        sa.getSourceCard());
                AllZone.getDisplay().showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                this.cancel();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (this.typeList.contains(card)) {
                    this.nReturns++;
                    part.addToList(card);
                    AllZone.getGameAction().moveToHand(card);
                    this.typeList.remove(card);
                    // in case nothing else to return
                    if (this.nReturns == nNeeded) {
                        this.done();
                    } else if (this.typeList.size() == 0) {
                        // happen
                        this.cancel();
                    } else {
                        this.showMessage();
                    }
                }
            }

            public void done() {
                this.stop();
                part.addListToHash(sa, "Returned");
                payment.paidCost(part);
            }

            public void cancel() {
                this.stop();
                payment.cancelCost();
            }
        };

        return target;
    } // returnType()

    /**
     * <p>
     * returnThis.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param payment
     *            a {@link forge.card.cost.Cost_Payment} object.
     * @param part
     *            TODO
     * @return a {@link forge.gui.input.Input} object.
     */
    public static Input returnThis(final SpellAbility sa, final Cost_Payment payment, final CostReturn part) {
        final Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;

            @Override
            public void showMessage() {
                final Card card = sa.getSourceCard();
                if (card.getController().isHuman() && AllZoneUtil.isCardInPlay(card)) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(card.getName());
                    sb.append(" - Return to Hand?");
                    final Object[] possibleValues = { "Yes", "No" };
                    final Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
                            possibleValues[0]);
                    if (choice.equals(0)) {
                        part.addToList(card);
                        AllZone.getGameAction().moveToHand(card);
                        this.stop();
                        part.addListToHash(sa, "Returned");
                        payment.paidCost(part);
                    } else {
                        this.stop();
                        payment.cancelCost();
                    }
                }
            }
        };

        return target;
    } // input_sacrifice()
}
