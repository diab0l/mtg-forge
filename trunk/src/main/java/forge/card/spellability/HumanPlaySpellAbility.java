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
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;

import forge.Card;
import forge.ITargetable;
import forge.card.CardType;
import forge.card.ability.AbilityUtils;
import forge.card.cost.CostPartMana;
import forge.card.cost.CostPayment;
import forge.game.Game;
import forge.game.zone.Zone;

/**
 * <p>
 * SpellAbility_Requirements class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class HumanPlaySpellAbility {
    private final SpellAbility ability;
    private final CostPayment payment;

    public HumanPlaySpellAbility(final SpellAbility sa, final CostPayment cp) {
        this.ability = sa;
        this.payment = cp;
    }


    public final void playAbility(boolean mayChoseTargets, boolean isFree, boolean skipStack) {
        final Game game = ability.getActivatingPlayer().getGame();

        // used to rollback
        Zone fromZone = null;
        int zonePosition = 0;

        final Card c = this.ability.getSourceCard();
        if (this.ability instanceof Spell && !c.isCopiedSpell()) {
            fromZone = game.getZoneOf(c);
            zonePosition = fromZone.getCards().indexOf(c);
            this.ability.setSourceCard(game.getAction().moveToStack(c));
        }

        // freeze Stack. No abilities should go onto the stack while I'm filling requirements.
        game.getStack().freezeStack();

        // This line makes use of short-circuit evaluation of boolean values, that is each subsequent argument 
        // is only executed or evaluated if the first argument does not suffice to determine the value of the expression
        boolean prerequisitesMet = this.announceValuesLikeX()
                && this.announceType()
                && ( !mayChoseTargets || setupTargets() ) // if you can choose targets, then do choose them.
                && ( isFree || this.payment.payCost(game) );


        if (!prerequisitesMet) {
            rollbackAbility(fromZone, zonePosition);
            return;
        }


        if (isFree || this.payment.isFullyPaid()) {
            if (skipStack) {
                game.getStack().unfreezeStack();
                AbilityUtils.resolve(this.ability);
            } else {
                this.enusureAbilityHasDescription(this.ability);
                this.ability.getActivatingPlayer().getManaPool().clearManaPaid(this.ability, false);
                game.getStack().addAndUnfreeze(this.ability);
            }
    
            // no worries here. The same thread must resolve, and by this moment ability will have been resolved already
            // Triggers haven't resolved yet ??
            if (mayChoseTargets) {
                clearTargets(ability);
            }
        }

    }
    
    private final boolean setupTargets() {
        // Skip to paying if parent ability doesn't target and has no subAbilities.
        // (or trigger case where its already targeted)
        SpellAbility beingTargeted = ability;
        do { 
            TargetRestrictions tgt = beingTargeted.getTargetRestrictions();
            if( tgt != null && tgt.doesTarget()) {
                clearTargets(beingTargeted);
                final TargetSelection select = new TargetSelection(beingTargeted);
                if (!select.chooseTargets(null) ) {
                    return false;
                }
            }
            beingTargeted = beingTargeted.getSubAbility();
        } while (beingTargeted != null);
        return true;
    }

    public final void clearTargets(SpellAbility ability) {
        TargetRestrictions tg = ability.getTargetRestrictions();
        if (tg != null) {
            ability.resetTargets();
            tg.calculateStillToDivide(ability.getParam("DividedAsYouChoose"), ability.getSourceCard(), ability);
        }
    }
    
    private void rollbackAbility(Zone fromZone, int zonePosition) { 
        // cancel ability during target choosing
        final Game game = ability.getActivatingPlayer().getGame();
        final Card c = ability.getSourceCard();

        if (fromZone != null) { // and not a copy
            // add back to where it came from
            game.getAction().moveTo(fromZone, c, zonePosition >= 0 ? Integer.valueOf(zonePosition) : null);
        }

        clearTargets(ability);

        this.ability.resetOnceResolved();
        this.payment.refundPayment();
        game.getStack().clearFrozen();
    }
    

    private boolean announceValuesLikeX() {
        // Announcing Requirements like Choosing X or Multikicker
        // SA Params as comma delimited list
        String announce = ability.getParam("Announce");
        if (announce != null) {
            for(String aVar : announce.split(",")) {
                String varName = aVar.trim();

                boolean isX = "X".equalsIgnoreCase(varName);
                CostPartMana manaCost = ability.getPayCosts().getCostMana();
                boolean allowZero = !isX || manaCost == null || manaCost.canXbe0();

                Integer value = ability.getActivatingPlayer().getController().announceRequirements(ability, varName, allowZero);
                if ( null == value )
                    return false;

                ability.setSVar(varName, value.toString());
                if( "Multikicker".equals(varName) ) {
                    ability.getSourceCard().setKickerMagnitude(value);
                } else {
                    ability.getSourceCard().setSVar(varName, value.toString());
                }
            }
        }
        return true;
    }

    private boolean announceType() {
     // Announcing Requirements like choosing creature type or number
        String announce = ability.getParam("AnnounceType");
        if (announce != null) {
            for(String aVar : announce.split(",")) {
                String varName = aVar.trim();
                if ("CreatureType".equals(varName)) {
                    String choice = ability.getActivatingPlayer().getController().chooseSomeType("Creature", 
                            ability.getParam("AILogic"), CardType.getCreatureTypes(), new ArrayList<String>());
                    ability.getSourceCard().setChosenType(choice);
                }
                if ("ChooseNumber".equals(varName)) {
                    int min = Integer.parseInt(ability.getParam("Min"));
                    int max = Integer.parseInt(ability.getParam("Max"));
                    int i = ability.getActivatingPlayer().getController().chooseNumber(ability,
                            "Choose a number", min, max);
                    ability.getSourceCard().setChosenNumber(i);
                }
            }
        }
        return true;
    }

    private void enusureAbilityHasDescription(SpellAbility ability) {
        if (!StringUtils.isBlank(ability.getStackDescription())) 
            return;
            
        // For older abilities that don't setStackDescription set it here
        final StringBuilder sb = new StringBuilder();
        sb.append(ability.getSourceCard().getName());
        if (ability.getTargetRestrictions() != null) {
            final Iterable<ITargetable> targets = ability.getTargets().getTargets();
            if (!Iterables.isEmpty(targets)) {
                sb.append(" - Targeting ");
                for (final ITargetable o : targets) {
                    sb.append(o.toString()).append(" ");
                }
            }
        }

        ability.setStackDescription(sb.toString());
    }
}
