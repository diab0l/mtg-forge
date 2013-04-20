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
import forge.GameEntity;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;

/**
 * The Class StaticAbility_CantBeCast.
 */
public class StaticAbilityCantAttackBlock {

    /**
     * TODO Write javadoc for this method.
     * 
     * @param stAb
     *            a StaticAbility
     * @param card
     *            the card
     * @return a Cost
     */
    public static boolean applyCantAttackAbility(final StaticAbility stAb, final Card card, final GameEntity target) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();

        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return false;
        }

        if (params.containsKey("Target")
                && !target.isValid(params.get("Target").split(","), hostCard.getController(), hostCard)) {
            return false;
        }

        return true;
    }

    /**
     * TODO Write javadoc for this method.
     * 
     * @param stAb
     *            a StaticAbility
     * @param card
     *            the card
     * @return a Cost
     */
    public static Cost getAttackCost(final StaticAbility stAb, final Card card, final GameEntity target) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();

        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return null;
        }

        if (params.containsKey("Target")
                && !target.isValid(params.get("Target").split(","), hostCard.getController(), hostCard)) {
            return null;
        }
        String costString = params.get("Cost");
        if ("X".equals(costString)) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X")));
        } else if ("Y".equals(costString)) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar("Y")));
        } else if (params.containsKey("References")) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar(params.get("References"))));
        }

        final Cost cost = new Cost(costString, true);

        return cost;
    }

    /**
     * TODO Write javadoc for this method.
     * 
     * @param stAb
     *            a StaticAbility
     * @param blocker
     *            the card
     * @return a Cost
     */
    public static Cost getBlockCost(final StaticAbility stAb, final Card blocker, final GameEntity attacker) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();

        if (params.containsKey("ValidCard")
                && !blocker.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return null;
        }
        
        if (params.containsKey("Attacker") && attacker != null
                && !attacker.isValid(params.get("Attacker").split(","), hostCard.getController(), hostCard)) {
            return null;
        }
        String costString = params.get("Cost");
        if ("X".equals(costString)) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X")));
        } else if ("Y".equals(costString)) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar("Y")));
        } else if (params.containsKey("References")) {
            costString = Integer.toString(CardFactoryUtil.xCount(hostCard, hostCard.getSVar(params.get("References"))));
        }

        final Cost cost = new Cost(costString, true);

        return cost;
    }

}
