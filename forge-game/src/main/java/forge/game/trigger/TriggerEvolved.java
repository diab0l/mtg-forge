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

import forge.game.card.Card;
import forge.game.spellability.SpellAbility;

/**
 * <p>
 * Trigger_Evolved class.
 * </p>
 * 
 * @author Forge
 * @version $Id: TriggerEvolved.java 17802 2012-10-31 08:05:14Z Max mtg $
 */
public class TriggerEvolved extends Trigger {

    /**
     * <p>
     * Constructor for Trigger_Evolved.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @param host
     *            a {@link forge.game.card.Card} object.
     * @param intrinsic
     *            the intrinsic
     */
    public TriggerEvolved(final java.util.Map<String, String> params, final Card host, final boolean intrinsic) {
        super(params, host, intrinsic);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean performTest(final java.util.Map<String, Object> runParams2) {
        final Card sac = (Card) runParams2.get("Card");
        if (this.mapParams.containsKey("ValidCard")) {
            if (!sac.isValid(this.mapParams.get("ValidCard").split(","), this.getHostCard().getController(),
                    this.getHostCard())) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTriggeringObjects(final SpellAbility sa) {
        sa.setTriggeringObject("Card", this.getRunParams().get("Card"));
    }

    @Override
    public String getImportantStackObjects(SpellAbility sa) {
        StringBuilder sb = new StringBuilder();
        sb.append("Evolved: ").append(this.getRunParams().get("Card"));
        return sb.toString();
    }
}
