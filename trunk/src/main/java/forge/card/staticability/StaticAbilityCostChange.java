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
package forge.card.staticability;

import java.util.HashMap;

import forge.Card;
import forge.card.ability.AbilityUtils;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.mana.ManaCostShard;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * The Class StaticAbility_CantBeCast.
 */
public class StaticAbilityCostChange {

    /**
     * Applies applyRaiseCostAbility ability.
     * 
     * @param staticAbility
     *            a StaticAbility
     * @param sa
     *            the SpellAbility
     * @param originalCost
     *            a ManaCost
     */
    public static void applyRaiseCostAbility(final StaticAbility staticAbility, final SpellAbility sa, final ManaCostBeingPaid manaCost) {
        final HashMap<String, String> params = staticAbility.getMapParams();
        final Card hostCard = staticAbility.getHostCard();
        final Player activator = sa.getActivatingPlayer();
        final Card card = sa.getSourceCard();
        final String amount = params.get("Amount");


        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return;
        }

        if (params.containsKey("Activator") && ((activator == null)
                || !activator.isValid(params.get("Activator"), hostCard.getController(), hostCard))) {
            return;
        }

        if (params.containsKey("Type")) {
            if (params.get("Type").equals("Spell")) {
                if (!sa.isSpell()) {
                    return;
                }
            } else if (params.get("Type").equals("Ability")) {
                if (!(sa instanceof AbilityActivated)) {
                    return;
                }
            } else if (params.get("Type").equals("NonManaAbility")) {
                if (!(sa instanceof AbilityActivated) || sa.isManaAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("Flashback")) {
                if (!sa.isFlashBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("MorphUp")) {
                if (!sa.isMorphUp()) {
                    return;
                }
            }
        }
        if (params.containsKey("AffectedZone") && !card.isInZone(ZoneType.smartValueOf(params.get("AffectedZone")))) {
            return;
        }
        if (params.containsKey("ValidTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (Card target : sa.getTargets().getTargetCards()) {
                if (target.isValid(params.get("ValidTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        if (params.containsKey("ValidSpellTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (SpellAbility target : sa.getTargets().getTargetSpells()) {
                Card targetCard = target.getSourceCard();
                if (targetCard.isValid(params.get("ValidSpellTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        int value = 0;
        try {
            value = Integer.parseInt(amount);
        }
        catch(NumberFormatException nfe) {
            if ("Min3".equals(amount)) {
                int cmc = manaCost.getConvertedManaCost();
                if (cmc < 3) {
                    value = 3 - cmc;
                }
            }
            else {
                value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar(amount));
            }
        }
        /*
        if ("X".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X"));
        } else if ("Y".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("Y"));
        } else if ("Min3".equals(amount)) {
            int cmc = manaCost.getConvertedManaCost();
            if (cmc < 3) {
                value = 3 - cmc;
            }
        } else {
            value = AbilityUtils.calculateAmount(card, amount, sa);
            //value = Integer.valueOf(amount);
        }
        */

        if (!params.containsKey("Color")) {
            manaCost.increaseColorlessMana(value);
            if (manaCost.toString().equals("0") && params.containsKey("MinMana")) {
                manaCost.increaseColorlessMana(Integer.valueOf(params.get("MinMana")));
            }
        } else {
            if (params.get("Color").equals("W")) {
                manaCost.increaseShard(ManaCostShard.WHITE, value);
            } else if (params.get("Color").equals("B")) {
                manaCost.increaseShard(ManaCostShard.BLACK, value);
            } else if (params.get("Color").equals("U")) {
                manaCost.increaseShard(ManaCostShard.BLUE, value);
            } else if (params.get("Color").equals("R")) {
                manaCost.increaseShard(ManaCostShard.RED, value);
            } else if (params.get("Color").equals("G")) {
                manaCost.increaseShard(ManaCostShard.GREEN, value);
            }
        }
    }

    /**
     * Applies applyReduceCostAbility ability.
     * 
     * @param staticAbility
     *            a StaticAbility
     * @param sa
     *            the SpellAbility
     * @param originalCost
     *            a ManaCost
     */
    public static void applyReduceCostAbility(final StaticAbility staticAbility, final SpellAbility sa, final ManaCostBeingPaid manaCost) {
        //Can't reduce zero cost
        if (manaCost.toString().equals("0")) {
            return;
        }
        final HashMap<String, String> params = staticAbility.getMapParams();
        final Card hostCard = staticAbility.getHostCard();
        final Player activator = sa.getActivatingPlayer();
        final Card card = sa.getSourceCard();
        final String amount = params.get("Amount");


        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return;
        }
        if (params.containsKey("Activator") && ((activator == null)
                || !activator.isValid(params.get("Activator"), hostCard.getController(), hostCard))) {
            return;
        }
        if (params.containsKey("Type")) {
            if (params.get("Type").equals("Spell")) {
                if (!sa.isSpell()) {
                    return;
                }
            } else if (params.get("Type").equals("Ability")) {
                if (!(sa instanceof AbilityActivated)) {
                    return;
                }
            } else if (params.get("Type").equals("Buyback")) {
                if (!sa.isBuyBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("Cycling")) {
                if (!sa.isCycling()) {
                    return;
                }
            } else if (params.get("Type").equals("Equip")) {
                if (!(sa instanceof AbilityActivated) || !sa.hasParam("Equip")) {
                    return;
                }
            } else if (params.get("Type").equals("Flashback")) {
                if (!sa.isFlashBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("MorphDown")) {
                if (!sa.isSpell() || !((Spell) sa).isCastFaceDown()) {
                    return;
                }
            }
        }
        if (params.containsKey("ValidTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (Card target : sa.getTargets().getTargetCards()) {
                if (target.isValid(params.get("ValidTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        if (params.containsKey("AffectedZone") && !card.isInZone(ZoneType.smartValueOf(params.get("AffectedZone")))) {
            return;
        }
        int value = 0;
        if ("X".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X"));
        } else if ("AffectedX".equals(amount)) {
            value = CardFactoryUtil.xCount(card, hostCard.getSVar("AffectedX"));
        } else {
            value = Integer.valueOf(amount);
        }

        if (!params.containsKey("Color")) {
            manaCost.decreaseColorlessMana(value);
            if (manaCost.toString().equals("0") && params.containsKey("MinMana")) {
                manaCost.increaseColorlessMana(Integer.valueOf(params.get("MinMana")));
            }
        } else {
            if (params.get("Color").equals("W")) {
                manaCost.decreaseShard(ManaCostShard.WHITE, value);
            } else if (params.get("Color").equals("B")) {
                manaCost.decreaseShard(ManaCostShard.BLACK, value);
            } else if (params.get("Color").equals("G")) {
                manaCost.decreaseShard(ManaCostShard.GREEN, value);
            }
        }
    }
}
