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

import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates.Presets;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

import java.util.List;

/**
 * The Class CostUntapType.
 */
public class CostUntapType extends CostPartWithList {

    public final boolean canUntapSource;
    
    /**
     * Instantiates a new cost untap type.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostUntapType(final String amount, final String type, final String description, boolean hasUntapInPrice) {
        super(amount, type, description);
        this.canUntapSource = !hasUntapInPrice;
    }

    @Override
    public boolean isReusable() { return true; }

    @Override
    public boolean isRenewable() { return true; }
    
    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Untap ");

        final Integer i = this.convertAmount();
        final String desc = this.getDescriptiveType();

        sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), " tapped " + desc));

        if (this.getType().contains("OppCtrl")) {
            sb.append(" an opponent controls");
        } else if (this.getType().contains("YouCtrl")) {
            sb.append(" you control");
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
        for (final Card c : this.getCardList()) {
            c.setTapped(true);
        }

        this.resetLists();
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
        final Card source = ability.getHostCard();
        List<Card> typeList = activator.getGame().getCardsIn(ZoneType.Battlefield);

        typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);

        if (!canUntapSource) {
            typeList.remove(source);
        }
        typeList = CardLists.filter(typeList, Presets.TAPPED);

        final Integer amount = this.convertAmount();
        if ((typeList.size() == 0) || ((amount != null) && (typeList.size() < amount))) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected Card doPayment(SpellAbility ability, Card targetCard) {
        targetCard.untap();
        return targetCard;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForLKIList() {
        return "Untapped";
    }
    @Override
    public String getHashForCardList() {
    	return "UntappedCards";
    }

    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }


}
