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
        StringBuilder sb = new StringBuilder();
        sb.append("Return ");

        Integer i = convertAmount();
        String pronoun = "its";

        if (getThis()) {
            sb.append(type);
        } else {
            String desc = typeDescription == null ? type : typeDescription;
            if (i != null) {
                sb.append(Cost.convertIntAndTypeToWords(i, desc));
                if (i > 1) {
                    pronoun = "their";
                }
            } else {
                sb.append(Cost.convertAmountTypeToWords(amount, desc));
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
        if (!getThis()) {
            CardList typeList = activator.getCardsIn(Zone.Battlefield);
            typeList = typeList.getValidCards(getType().split(";"), activator, source);

            Integer amount = convertAmount();
            if (amount != null && typeList.size() < amount) {
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
        for (Card c : list)
            AllZone.getGameAction().moveToHand(c);
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
        String amount = getAmount();
        Integer c = convertAmount();
        Player activator = ability.getActivatingPlayer();
        CardList list = activator.getCardsIn(Zone.Battlefield);
        if (c == null) {
            String sVar = source.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = CostUtil.chooseXValue(source, list.size());
            } else {
                c = AbilityFactory.calculateAmount(source, amount, ability);
            }
        }
        if (getThis()) {
            CostUtil.setInput(CostReturn.returnThis(ability, payment, this));
        } else {
            CostUtil.setInput(CostReturn.returnType(ability, getType(), payment, this, c));
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
        resetList();
        if (getThis()) {
            list.add(source);
        } else {
            Integer c = convertAmount();
            if (c == null) {
                c = AbilityFactory.calculateAmount(source, amount, ability);
            }

            list = ComputerUtil.chooseReturnType(getType(), source, ability.getTargetCard(), c);
            if (list == null) {
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
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            private CardList typeList;
            private int nReturns = 0;

            @Override
            public void showMessage() {
                if (nNeeded == 0) {
                    done();
                }

                StringBuilder msg = new StringBuilder("Return ");
                int nLeft = nNeeded - nReturns;
                msg.append(nLeft).append(" ");
                msg.append(type);
                if (nLeft > 1) {
                    msg.append("s");
                }

                typeList = sa.getActivatingPlayer().getCardsIn(Zone.Battlefield);
                typeList = typeList.getValidCards(type.split(";"), sa.getActivatingPlayer(), sa.getSourceCard());
                AllZone.getDisplay().showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }

            @Override
            public void selectButtonCancel() {
                cancel();
            }

            @Override
            public void selectCard(final Card card, final PlayerZone zone) {
                if (typeList.contains(card)) {
                    nReturns++;
                    part.addToList(card);
                    AllZone.getGameAction().moveToHand(card);
                    typeList.remove(card);
                    // in case nothing else to return
                    if (nReturns == nNeeded) {
                        done();
                    } else if (typeList.size() == 0) {
                        // happen
                        cancel();
                    } else {
                        showMessage();
                    }
                }
            }

            public void done() {
                stop();
                part.addListToHash(sa, "Returned");
                payment.paidCost(part);
            }

            public void cancel() {
                stop();
                payment.cancelCost();
            }
        };

        return target;
    }// returnType()

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
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;

            @Override
            public void showMessage() {
                Card card = sa.getSourceCard();
                if (card.getController().isHuman() && AllZoneUtil.isCardInPlay(card)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(card.getName());
                    sb.append(" - Return to Hand?");
                    Object[] possibleValues = { "Yes", "No" };
                    Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
                            possibleValues[0]);
                    if (choice.equals(0)) {
                        part.addToList(card);
                        AllZone.getGameAction().moveToHand(card);
                        stop();
                        part.addListToHash(sa, "Returned");
                        payment.paidCost(part);
                    } else {
                        stop();
                        payment.cancelCost();
                    }
                }
            }
        };

        return target;
    }// input_sacrifice()
}
