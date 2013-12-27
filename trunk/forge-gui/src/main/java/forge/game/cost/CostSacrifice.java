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
 * The Class CostSacrifice.
 */
public class CostSacrifice extends CostPartWithList {

    /**
     * Instantiates a new cost sacrifice.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostSacrifice(final String amount, final String type, final String description) {
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
        sb.append("Sacrifice ");

        final Integer i = this.convertAmount();

        if (this.payCostFromSource()) {
            sb.append(this.getType());
        } else {
            final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
            if (i != null) {
                sb.append(Cost.convertIntAndTypeToWords(i, desc));
            } else {
                sb.append(Cost.convertAmountTypeToWords(this.getAmount(), desc));
            }
        }
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

        // You can always sac all
        if (!this.payCostFromSource()) {
            // If the sacrificed type is dependant on an annoucement, can't necesarily rule out the CanPlay call
            boolean needsAnnoucement = ability.hasParam("Announce") && this.getType().contains(ability.getParam("Announce"));

            List<Card> typeList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));
            typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);
            final Integer amount = this.convertAmount();

            if (activator.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
                typeList = CardLists.getNotType(typeList, "Creature");
            }

            if (!needsAnnoucement && (amount != null) && (typeList.size() < amount)) {
                return false;
            }

            // If amount is null, it's either "ALL" or "X"
            // if X is defined, it needs to be calculated and checked, if X is
            // choice, it can be Paid even if it's 0
        }
        else {
            if (!source.isInPlay()) {
                return false;
            }
            else if (source.isCreature() && activator.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
                return false;
            }
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
    public final boolean payHuman(final SpellAbility ability, final Player activator) {
        final String amount = this.getAmount();
        final Card source = ability.getSourceCard();
        final String type = this.getType();

        List<Card> list = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));
        list = CardLists.getValidCards(list, type.split(";"), activator, source);
        if (activator.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
            list = CardLists.getNotType(list, "Creature");
        }

        if (this.payCostFromSource()) {
            if (source.getController() == ability.getActivatingPlayer() && source.isInPlay()) {
                return activator.getController().confirmPayment(this, "Sacrifice " + source.getName() + "?") && executePayment(ability, source);
            }
        }
        else if (amount.equals("All")) {
            return executePayment(ability, list);
        }
        else {
            Integer c = this.convertAmount();
            if (c == null) {
                // Generalize this
                if (ability.getSVar(amount).equals("XChoice")) {
                    c = chooseXValue(source, ability, list.size());
                } else {
                    c = AbilityUtils.calculateAmount(source, amount, ability);
                }
            }
            if (0 == c.intValue()) {
                return true;
            }

            InputSelectCardsFromList inp = new InputSelectCardsFromList(c, c, list);
            inp.setMessage("Select a " + this.getDescriptiveType() + " to sacrifice (%d left)");
            inp.setCancelAllowed(true);
            inp.showAndWait();
            if ( inp.hasCancelled() )
                return false;

            return executePayment(ability, inp.getSelected());
        }
        return false;
    }

    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        targetCard.getGame().getAction().sacrifice(targetCard, ability);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "Sacrificed";
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPart#decideAIPayment(forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public PaymentDecision decideAIPayment(Player ai, SpellAbility ability, Card source) {

        if (this.payCostFromSource()) {
            return new PaymentDecision(source);
        }
        if (this.getAmount().equals("All")) {
            /*List<Card> typeList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Battlefield));
            typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);
            if (activator.hasKeyword("You can't sacrifice creatures to cast spells or activate abilities.")) {
                typeList = CardLists.getNotType(typeList, "Creature");
            }*/
            // Does the AI want to use Sacrifice All?
            return null;
        }

        Integer c = this.convertAmount();
        if (c == null) {
            if (ability.getSVar(this.getAmount()).equals("XChoice")) {
                return null;
            }

            c = AbilityUtils.calculateAmount(source, this.getAmount(), ability);
        }
        List<Card> list = ComputerUtil.chooseSacrificeType(ai, this.getType(), source, ability.getTargetCard(), c);
        return new PaymentDecision(list);
    }

    // Inputs
    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
