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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import forge.ai.ComputerUtil;
import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.input.InputSelectCards;
import forge.gui.input.InputSelectCardsFromList;

/**
 * The Class CostExile.
 */
public class CostExile extends CostPartWithList {
    // Exile<Num/Type{/TypeDescription}>
    // ExileFromHand<Num/Type{/TypeDescription}>
    // ExileFromGrave<Num/Type{/TypeDescription}>
    // ExileFromTop<Num/Type{/TypeDescription}> (of library)
    // ExileSameGrave<Num/Type{/TypeDescription}>

        /** 
     * TODO: Write javadoc for this type.
     *
     */

    private ZoneType from = ZoneType.Battlefield;
    private boolean sameZone = false;

    /**
     * Gets the from.
     * 
     * @return the from
     */
    public final ZoneType getFrom() {
        return this.from;
    }

    /**
     * Instantiates a new cost exile.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     * @param from
     *            the from
     */
    public CostExile(final String amount, final String type, final String description, final ZoneType from) {
        super(amount, type, description);
        if (from != null) {
            this.from = from;
        }
    }

    public CostExile(final String amount, final String type, final String description, final ZoneType from, final boolean sameZone) {
        this(amount, type, description, from);
        this.sameZone = sameZone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        final Integer i = this.convertAmount();
        sb.append("Exile ");

        if (this.payCostFromSource()) {
            sb.append(this.getType());
            if (!this.from.equals(ZoneType.Battlefield)) {
                sb.append(" from your ").append(this.from);
            }
            return sb.toString();
        } else if (this.getType().equals("All")) {
            sb.append(" all cards from your ").append(this.from);
            return sb.toString();
        }

        if (this.from.equals(ZoneType.Battlefield)) {
            final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();

            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), desc));
            if (!this.payCostFromSource()) {
                sb.append(" you control");
            }
            return sb.toString();
        }

        if (i != null) {
            sb.append(i);
        } else {
            sb.append(this.getAmount());
        }
        if (!this.getType().equals("Card")) {
            sb.append(" " + this.getType());
        }
        sb.append(" card");
        if ((i == null) || (i > 1)) {
            sb.append("s");
        }

        if (this.sameZone) {
            sb.append(" from the same ");
        } else {
            sb.append(" from your ");
        }

        sb.append(this.from);

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
        final Game game = activator.getGame();

        String type = this.getType();
        if (type.equals("All")) {
            return true; // this will always work
        }
        else if (type.contains("FromTopGrave")) {
            type = type.replace("FromTopGrave", "");
        }

        List<Card> list;
        if (this.from.equals(ZoneType.Stack)) {
            list = new ArrayList<Card>();
            for (SpellAbilityStackInstance si : game.getStack()) {
                list.add(si.getSourceCard());
            }
        }
        else if (this.sameZone) {
            list = new ArrayList<Card>(game.getCardsIn(this.from));
        }
        else {
            list = new ArrayList<Card>(activator.getCardsIn(this.from));
        }

        if (this.payCostFromSource()) {
            return list.contains(source);
        }

        list = CardLists.getValidCards(list, type.split(";"), activator, source);

        final Integer amount = this.convertAmount();
        if ((amount != null) && (list.size() < amount)) {
            return false;
        }

        if (this.sameZone && amount != null) {
            boolean foundPayable = false;
            List<Player> players = game.getPlayers();
            for (Player p : players) {
                if (CardLists.filter(list, CardPredicates.isController(p)).size() >= amount) {
                    foundPayable = true;
                    break;
                }
            }
            if (!foundPayable) {
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
        final Game game = activator.getGame(); 

        Integer c = this.convertAmount();
        String type = this.getType();
        boolean fromTopGrave = false;
        if (type.contains("FromTopGrave")) {
            type = type.replace("FromTopGrave", "");
            fromTopGrave = true;
        }

        List<Card> list;
        if (this.from.equals(ZoneType.Stack)) {
            list = new ArrayList<Card>();
            for (SpellAbilityStackInstance si : game.getStack()) {
                list.add(si.getSourceCard());
            }
        }
        else if (this.sameZone) {
            list = new ArrayList<Card>(game.getCardsIn(this.from));
        }
        else {
            list = new ArrayList<Card>(activator.getCardsIn(this.from));
        }

        if (this.payCostFromSource()) {
            return source.getZone() == activator.getZone(from) 
                    && activator.getController().confirmPayment(this, "Exile " + source.getName() + "?") 
                    && executePayment(ability, source);
        }

        if (type.equals("All")) {
            for (final Card card : list) {
                executePayment(ability, card);
            }
            return true;
        }
        list = CardLists.getValidCards(list, type.split(";"), activator, source);
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(source, ability, list.size());
            }
            else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        if (this.from == ZoneType.Battlefield || this.from == ZoneType.Hand) {
            InputSelectCards inp = new InputSelectCardsFromList(c, c, list);
            inp.setMessage("Exile %d card(s) from your" + from);
            inp.setCancelAllowed(true);
            inp.showAndWait();
            return !inp.hasCancelled() && executePayment(ability, inp.getSelected());
        }

        if (this.from == ZoneType.Stack) { return exileFromStack(ability, c); }
        if (this.from == ZoneType.Library) { return exileFromTop(ability, activator, c); }
        if (fromTopGrave) { return exileFromTopGraveType(ability, c, list); }
        if (!this.sameZone) { return exileFromMiscZone(ability, c, list); }

        List<Player> players = game.getPlayers();
        List<Player> payableZone = new ArrayList<Player>();
        for (Player p : players) {
            List<Card> enoughType = CardLists.filter(list, CardPredicates.isOwner(p));
            if (enoughType.size() < c) {
                list.removeAll(enoughType);
            }
            else {
                payableZone.add(p);
            }
        }
        return exileFromSame(list, c, payableZone);
    }

    // Inputs

    // Exile<Num/Type{/TypeDescription}>
    // ExileFromHand<Num/Type{/TypeDescription}>
    // ExileFromGrave<Num/Type{/TypeDescription}>
    // ExileFromTop<Num/Type{/TypeDescription}> (of library)
    // ExileSameGrave<Num/Type{/TypeDescription}>

    private boolean exileFromSame(List<Card> list, int nNeeded, List<Player> payableZone) {
        if (nNeeded == 0) {
            return true;
        }

        final Player p = GuiChoose.oneOrNone(String.format("Exile from whose %s?", getFrom()), payableZone);
        if (p == null) {
            return false;
        }

        List<Card> typeList = CardLists.filter(list, CardPredicates.isOwner(p));

        for (int i = 0; i < nNeeded; i++) {
            if (typeList.isEmpty()) {
                return false;
            }

            final Card c = GuiChoose.oneOrNone("Exile from " + getFrom(), typeList);

            if (c != null) {
                typeList.remove(c);
                executePayment(null, c);
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * TODO: Write javadoc for Constructor.
     * @param payment
     * @param sa
     * @param type
     * @param nNeeded
     * @param part
     */
    private boolean exileFromStack(SpellAbility sa, int nNeeded) {
        if (nNeeded == 0) {
            return true;
        }

        final Game game = sa.getActivatingPlayer().getGame();
        ArrayList<SpellAbility> saList = new ArrayList<SpellAbility>();
        ArrayList<String> descList = new ArrayList<String>();

        for (SpellAbilityStackInstance si : game.getStack()) {
            final Card stC = si.getSourceCard();
            final SpellAbility stSA = si.getSpellAbility().getRootAbility();
            if (stC.isValid(getType().split(";"), sa.getActivatingPlayer(), sa.getSourceCard()) && stSA.isSpell()) {
                saList.add(stSA);
                if (stC.isCopiedSpell()) {
                    descList.add(stSA.getStackDescription() + " (Copied Spell)");
                } else {
                    descList.add(stSA.getStackDescription());
                }
            }
        }

        for (int i = 0; i < nNeeded; i++) {
            if (saList.isEmpty()) {
                return false;
            }

            //Have to use the stack descriptions here because some copied spells have no description otherwise
            final String o = GuiChoose.oneOrNone("Exile from " + getFrom(), descList);

            if (o != null) {
                final SpellAbility toExile = saList.get(descList.indexOf(o));
                final Card c = toExile.getSourceCard();

                saList.remove(toExile);
                descList.remove(o);

                if (!c.isCopiedSpell()) {
                    executePayment(sa, c);
                }
                else {
                    addToList(c);
                }
                final SpellAbilityStackInstance si = game.getStack().getInstanceFromSpellAbility(toExile);
                game.getStack().remove(si);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private  boolean exileFromTop(final SpellAbility sa, final Player payer, final int nNeeded) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Exile ").append(nNeeded).append(" cards from the top of your library?");
        final List<Card> list = payer.getCardsIn(ZoneType.Library, nNeeded);

        if (list.size() > nNeeded) {
            // I don't believe this is possible
            return false;
        }

        if (!payer.getController().confirmPayment(this, "Exile " + nNeeded + " card" + (nNeeded == 1 ? "" : "s") +
                " from the top of your library?")) {
            return false;
        }

        final Iterator<Card> itr = list.iterator();
        while (itr.hasNext()) {
            executePayment(sa, itr.next());
        }
        return true;
    }

    private boolean exileFromMiscZone(SpellAbility sa, int nNeeded, List<Card> typeList) {
        for (int i = 0; i < nNeeded; i++) {
            if (typeList.isEmpty()) {
                return false;
            }

            final Card c = GuiChoose.oneOrNone("Exile from " + getFrom(), typeList);

            if (c != null) {
                typeList.remove(c);
                executePayment(sa, c);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean exileFromTopGraveType(SpellAbility sa, int nNeeded, List<Card> typeList) {
        Collections.reverse(typeList);
        for (int i = 0; i < nNeeded; i++) {
            if (typeList.isEmpty()) {
                return false;
            }
            final Card c = typeList.get(0);
            typeList.remove(c);
            executePayment(sa, c);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        targetCard.getGame().getAction().exile(targetCard);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        // TODO Auto-generated method stub
        return "Exiled";
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPart#decideAIPayment(forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public PaymentDecision decideAIPayment(Player ai, SpellAbility ability, Card source) {
        if (this.payCostFromSource()) {
            return new PaymentDecision(source);
        }

        if (this.getType().equals("All")) {
            return new PaymentDecision(new ArrayList<Card>(ai.getCardsIn(this.getFrom())));
        }
        else if (this.getType().contains("FromTopGrave")) {
            return null;
        }

        Integer c = this.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(this.getAmount());
            // Generalize this
            if (sVar.equals("XChoice")) {
                return null;
            }
            c = AbilityUtils.calculateAmount(source, this.getAmount(), ability);
        }

        if (this.from.equals(ZoneType.Library)) {
            return new PaymentDecision(ai.getCardsIn(ZoneType.Library, c));
        }
        else if (this.sameZone) {
            // TODO Determine exile from same zone for AI
            return null;
        }
        else {
            List<Card> chosen = ComputerUtil.chooseExileFrom(ai, this.getFrom(), this.getType(), source, ability.getTargetCard(), c);
            return null == chosen ? null : new PaymentDecision(chosen);
        }
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPart#payAI(forge.card.cost.PaymentDecision, forge.game.player.AIPlayer, forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public boolean payAI(PaymentDecision decision, Player ai, SpellAbility ability, Card source) {
        for (final Card c : decision.cards) {
            executePayment(ability, c);
            if (this.from.equals(ZoneType.Stack)) {
                ArrayList<SpellAbility> spells = c.getSpellAbilities();
                for (SpellAbility spell : spells) {
                    if (c.isInZone(ZoneType.Exile)) {
                        final SpellAbilityStackInstance si = ai.getGame().getStack().getInstanceFromSpellAbility(spell);
                        ai.getGame().getStack().remove(si);
                    }
                }
            }
        }
        return true;
    }
}
