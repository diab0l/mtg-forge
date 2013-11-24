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

import java.util.Map;

import forge.card.ability.AbilityFactory;
import forge.card.ability.ApiType;
import forge.card.ability.SpellAbilityAi;
import forge.card.ability.SpellAbilityEffect;
import forge.card.ability.effects.ChangeZoneAllEffect;
import forge.card.ability.effects.ChangeZoneEffect;
import forge.card.ability.effects.ManaEffect;
import forge.card.ability.effects.ManaReflectedEffect;
import forge.card.cardfactory.CardFactory;
import forge.card.cost.Cost;
import forge.game.card.Card;
import forge.game.player.Player;

/**
 * <p>
 * Abstract Ability_Sub class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class AbilitySub extends SpellAbility implements java.io.Serializable {
    /** Constant <code>serialVersionUID=4650634415821733134L</code>. */
    private static final long serialVersionUID = 4650634415821733134L;

    private SpellAbility parent = null;

    /**
     * <p>
     * Setter for the field <code>parent</code>.
     * </p>
     * 
     * @param parent
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public final void setParent(final SpellAbility parent) {
        this.parent = parent;
    }

    /**
     * <p>
     * Getter for the field <code>parent</code>.
     * </p>
     * 
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    @Override
    public final SpellAbility getParent() {
        return this.parent;
    }



    /** {@inheritDoc} */
    @Override
    public boolean canPlay() {
        // this should never be on the Stack by itself
        return false;
    }

    
    private final SpellAbilityEffect effect;
    private final SpellAbilityAi ai;

    /**
     * @return the ai
     */
    public SpellAbilityAi getAi() {
        return ai;
    }

    public AbilitySub(ApiType api0, final Card ca, final TargetRestrictions tgt, Map<String, String> params0) {
        super(ca, Cost.Zero);
        this.setTargetRestrictions(tgt);

        api = api0;
        params = params0;
        ai = api.getAi();
        effect = api.getSpellEffect();

        if (effect instanceof ManaEffect || effect instanceof ManaReflectedEffect) {
            this.setManaPart(new AbilityManaPart(ca, params));
        }

        if (effect instanceof ChangeZoneEffect || effect instanceof ChangeZoneAllEffect) {
            AbilityFactory.adjustChangeZoneTarget(params, this);
        }
    }

    public AbilitySub getCopy() {
        TargetRestrictions t = getTargetRestrictions() == null ? null : new TargetRestrictions(getTargetRestrictions());
        AbilitySub res = new AbilitySub(api, getSourceCard(), t, params);
        CardFactory.copySpellAbility(this, res);
        return res;
    }

    @Override
    public String getStackDescription() {
        return effect.getStackDescriptionWithSubs(params, this);
    }

    @Override
    public boolean canPlayAI() {
        return ai.canPlayAIWithSubs(getActivatingPlayer(), this);
    }

    @Override
    public void resolve() {
        effect.resolve(this);
    }

    @Override
    public boolean doTrigger(final boolean mandatory, Player aiPlayer) {
        return ai.doTriggerAI(aiPlayer, this, mandatory);
    }
}
