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
package forge.card.spellability;

import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.CardLists;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostPayment;
import forge.card.staticability.StaticAbility;
import forge.error.BugReporter;
import forge.game.Game;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.Expressions;

/**
 * <p>
 * Abstract Spell class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Spell extends SpellAbility implements java.io.Serializable, Cloneable {

    /** Constant <code>serialVersionUID=-7930920571482203460L</code>. */
    private static final long serialVersionUID = -7930920571482203460L;

    private boolean castFaceDown = false;

    /**
     * <p>
     * Constructor for Spell.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     */
    public Spell(final Card sourceCard) {
        this(sourceCard, new Cost(sourceCard.getManaCost(), false));
    }
    public Spell(final Card sourceCard, final Cost abCost) {
        super(sourceCard, abCost);

        this.setStackDescription(sourceCard.getSpellText());
        this.getRestrictions().setZone(ZoneType.Hand);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPlay() {
        final Game game = getActivatingPlayer().getGame();
        if (game.getStack().isSplitSecondOnStack()) {
            return false;
        }

        final Card card = this.getSourceCard();

        Player activator = this.getActivatingPlayer();
        if (activator == null) {
            activator = this.getSourceCard().getController();
        }

        if (!(card.isInstant() || activator.canCastSorcery() || card.hasKeyword("Flash")
               || this.getRestrictions().isInstantSpeed()
               || activator.hasKeyword("You may cast nonland cards as though they had flash.")
               || card.hasStartOfKeyword("You may cast CARDNAME as though it had flash."))) {
            return false;
        }

        if (!this.getRestrictions().canPlay(card, this)) {
            return false;
        }

        // for uncastables like lotus bloom, check if manaCost is blank
        if (isBasicSpell() && card.getManaCost().isNoCost()) {
            return false;
        }

        if (this.getPayCosts() != null) {
            if (!CostPayment.canPayAdditionalCosts(this.getPayCosts(), this)) {
                return false;
            }
        }

        return checkOtherRestrictions();
    } // canPlay()
    
    public boolean checkOtherRestrictions() {
        final Card source = this.getSourceCard();
        Player activator = getActivatingPlayer();
        final Game game = activator.getGame();
        // CantBeCast static abilities
        final List<Card> allp = new ArrayList<Card>(game.getCardsIn(ZoneType.listValueOf("Battlefield,Command")));
        allp.add(source);
        for (final Card ca : allp) {
            final ArrayList<StaticAbility> staticAbilities = ca.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                if (stAb.applyAbility("CantBeCast", source, activator)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPlayAI() {
        final Card card = this.getSourceCard();
        final Game game = getActivatingPlayer().getGame();
        if (card.getSVar("NeedsToPlay").length() > 0) {
            final String needsToPlay = card.getSVar("NeedsToPlay");
            List<Card> list = game.getCardsIn(ZoneType.Battlefield);

            list = CardLists.getValidCards(list, needsToPlay.split(","), card.getController(), card);
            if (list.isEmpty()) {
                return false;
            }
        }
        if (card.getSVar("NeedsToPlayVar").length() > 0) {
            final String needsToPlay = card.getSVar("NeedsToPlayVar");
            int x = 0;
            int y = 0;
            String sVar = needsToPlay.split(" ")[0];
            String comparator = needsToPlay.split(" ")[1];
            String compareTo = comparator.substring(2);
            try {
                x = Integer.parseInt(sVar);
            } catch (final NumberFormatException e) {
                x = CardFactoryUtil.xCount(card, card.getSVar(sVar));
            }
            try {
                y = Integer.parseInt(compareTo);
            } catch (final NumberFormatException e) {
                y = CardFactoryUtil.xCount(card, card.getSVar(compareTo));
            }
            if (!Expressions.compare(x, comparator, y)) {
                return false;
            }
        }

        return super.canPlayAI();
    }


    /** {@inheritDoc} */
    @Override
    public final Object clone() {
        try {
            return super.clone();
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Spell : clone() error, " + ex);
        }
    }

    @Override
    public boolean isSpell() { return true; }
    @Override
    public boolean isAbility() { return false; }


    /**
     * <p>
     * canPlayFromEffectAI.
     * </p>
     *
     * @param mandatory
     *            can the controller chose not to play the spell
     * @param withOutManaCost
     *            is the spell cast without paying mana
     * @return a boolean.
     */
    public boolean canPlayFromEffectAI(boolean mandatory, boolean withOutManaCost) {
        return canPlayAI();
    }

    /**
     * @return the castFaceDown
     */
    public boolean isCastFaceDown() {
        return castFaceDown;
    }

    /**
     * @param faceDown the castFaceDown to set
     */
    public void setCastFaceDown(boolean faceDown) {
        this.castFaceDown = faceDown;
    }

}
