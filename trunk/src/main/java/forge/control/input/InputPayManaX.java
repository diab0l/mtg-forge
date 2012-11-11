package forge.control.input;

import forge.Card;
import forge.Singletons;
import forge.card.cost.CostMana;
import forge.card.cost.CostPayment;
import forge.card.mana.ManaCost;
import forge.card.spellability.SpellAbility;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.match.CMatchUI;
import forge.view.ButtonUtil;

public class InputPayManaX extends InputPayMana { 
    private static final long serialVersionUID = -6900234444347364050L;
    private int xPaid = 0;
    private final String strX;
    private String colorsPaid;
    private ManaCost manaCost;
    private final CostMana costMana; 
    private final CostPayment payment;
    private final SpellAbility sa;

    
    public InputPayManaX(final SpellAbility sa0, final CostPayment payment0, final CostMana costMana0) 
    {
        sa = sa0;
        payment = payment0;
        xPaid = 0;
        colorsPaid = sa.getSourceCard().getColorsPaid();
        costMana = costMana0;
        strX = Integer.toString(costMana.getXMana());
        manaCost = new ManaCost(strX);
    }
    
    @Override
    public void showMessage() {
        if ((xPaid == 0 && costMana.isxCantBe0()) || !this.manaCost.toString().equals(strX)) {
            ButtonUtil.enableOnlyCancel();
            // only cancel if partially paid an X value
            // or X is 0, and x can't be 0
        } else {
            ButtonUtil.enableAll();
        }
        
        StringBuilder msg = new StringBuilder("Pay X Mana Cost for ");
        msg.append(sa.getSourceCard().getName()).append("\n").append(this.xPaid);
        msg.append(" Paid so far.");
        if (costMana.isxCantBe0()) {
            msg.append(" X Can't be 0.");
        }
        
        CMatchUI.SINGLETON_INSTANCE.showMessage(msg.toString());
    }

    // selectCard
    @Override
    public void selectCard(final Card card, final PlayerZone zone) {
        this.manaCost = InputPayManaCostUtil.activateManaAbility(sa, card, this.manaCost);
        if (this.manaCost.isPaid()) {
            if (!this.colorsPaid.contains(this.manaCost.getColorsPaid())) {
                this.colorsPaid += this.manaCost.getColorsPaid();
            }
            this.manaCost = new ManaCost(strX);
            this.xPaid++;
        }

        if (Singletons.getModel().getMatch().getInput().getInput() == this) {
            this.showMessage();
        }
    }

    @Override
    public void selectButtonCancel() {
        this.stop();
        payment.cancelCost();
        Singletons.getControl().getPlayer().getZone(ZoneType.Battlefield).updateObservers();
    }

    @Override
    public void selectButtonOK() {
        this.stop();
        payment.getCard().setXManaCostPaid(this.xPaid);
        payment.paidCost(costMana);
        payment.getCard().setColorsPaid(this.colorsPaid);
        payment.getCard().setSunburstValue(this.colorsPaid.length());
    }

    @Override
    public void selectManaPool(String color) {
        this.manaCost = InputPayManaCostUtil.activateManaAbility(color, sa, this.manaCost);
        if (this.manaCost.isPaid()) {
            if (!this.colorsPaid.contains(this.manaCost.getColorsPaid())) {
                this.colorsPaid += this.manaCost.getColorsPaid();
            }
            this.manaCost = new ManaCost(strX);
            this.xPaid++;
        }

        if (Singletons.getModel().getMatch().getInput().getInput() == this) {
            this.showMessage();
        }
    }
    
    /* (non-Javadoc)
     * @see forge.control.input.Input#isClassUpdated()
     */
    @Override
    public void isClassUpdated() {}
}