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
package forge.game.trigger;

import java.util.Map;

import forge.game.card.Card;
import forge.game.spellability.SpellAbility;

/**
 * <p>
 * Trigger_Blocks class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class TriggerBlocks extends Trigger {

    /**
     * <p>
     * Constructor for Trigger_Blocks.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @param host
     *            a {@link forge.game.card.Card} object.
     * @param intrinsic
     *            the intrinsic
     */
    public TriggerBlocks(final java.util.Map<String, String> params, final Card host, final boolean intrinsic) {
        super(params, host, intrinsic);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean performTest(final Map<String, Object> runParams2) {
        Card blocker = (Card) runParams2.get("Blocker");
        Card attacker = (Card) runParams2.get("Attacker");
        if (this.mapParams.containsKey("ValidCard")) {
            String validBlocker = this.mapParams.get("ValidCard");
            if (validBlocker.contains(".withLesserPower")) {
                // Have to check this here as triggering objects aren't set yet for AI combat trigger checks
                // so ValidCard$Creature.powerLTX where X:TriggeredAttacker$CardPower crashes with NPE
                validBlocker = validBlocker.replace(".withLesserPower", "");
                if (blocker.getCurrentPower() >= attacker.getCurrentPower()) {
                    return false;
                }
            }
            if (!matchesValid(runParams2.get("Blocker"), validBlocker.split(","), this.getHostCard())) {
                return false;
            }
        }
        if (this.mapParams.containsKey("ValidBlocked")) {
            String validBlocked = this.mapParams.get("ValidBlocked");
            if (validBlocked.contains(".withLesserPower")) {
                // Have to check this here as triggering objects aren't set yet for AI combat trigger checks
                // so ValidBlocked$Creature.powerLTX where X:TriggeredBlocker$CardPower crashes with NPE
                validBlocked = validBlocked.replace(".withLesserPower", "");
                if (blocker.getCurrentPower() <= attacker.getCurrentPower()) {
                    return false;
                }
            }
            if (!matchesValid(runParams2.get("Attacker"), validBlocked.split(","), this.getHostCard())) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTriggeringObjects(final SpellAbility sa) {
        sa.setTriggeringObject("Blocker", this.getRunParams().get("Blocker"));
        sa.setTriggeringObject("Attacker", this.getRunParams().get("Attacker"));
    }
}
