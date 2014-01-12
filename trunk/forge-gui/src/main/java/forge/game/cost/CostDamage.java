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

import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;

/**
 * The Class CostPayLife.
 */
public class CostDamage extends CostPart {

    public CostDamage(final String amount) {
        this.setAmount(amount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Pay ").append(this.getAmount()).append(" Life");
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
        return true;
    }
    
    @Override
    public boolean payAsDecided(Player payer, PaymentDecision decision, SpellAbility sa) {
        return payer.addDamage(decision.c, sa.getSourceCard());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final PaymentDecision payHuman(final SpellAbility ability, final Player activator) {
        final String amount = this.getAmount();
        final int life = activator.getLife();
        final Card source = ability.getSourceCard();

        Integer c = this.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(source, ability, life);
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (activator.canPayLife(c) && activator.getController().confirmPayment(this, "Pay " + c + " Life?")) {
            return PaymentDecision.number(c);
        }
        return null;
    }

    @Override
    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
