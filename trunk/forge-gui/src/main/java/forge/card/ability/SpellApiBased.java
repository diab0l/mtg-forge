package forge.card.ability;

import java.util.Map;

import forge.Card;
import forge.card.ability.effects.ChangeZoneAllEffect;
import forge.card.ability.effects.ChangeZoneEffect;
import forge.card.ability.effects.ManaEffect;
import forge.card.ability.effects.ManaReflectedEffect;
import forge.card.cost.Cost;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.Spell;
import forge.card.spellability.TargetRestrictions;

public class SpellApiBased extends Spell {
    private static final long serialVersionUID = -6741797239508483250L;
    private final SpellAbilityEffect effect;
    private final SpellAbilityAi ai;

    public SpellApiBased(ApiType api0, Card sourceCard, Cost abCost, TargetRestrictions tgt, Map<String, String> params0) {
        super(sourceCard, abCost);
        this.setTargetRestrictions(tgt);
        
        params = params0;
        api = api0;
        effect = api.getSpellEffect();
        ai = api.getAi();

        if (effect instanceof ManaEffect || effect instanceof ManaReflectedEffect) {
            this.setManaPart(new AbilityManaPart(sourceCard, params));
        }

        if (effect instanceof ChangeZoneEffect || effect instanceof ChangeZoneAllEffect) {
            AbilityFactory.adjustChangeZoneTarget(params, this);
        }
    }

    @Override
    public String getStackDescription() {
        return effect.getStackDescriptionWithSubs(params, this);
    }

    /* (non-Javadoc)
     * @see forge.card.spellability.SpellAbility#resolve()
     */

    @Override
    public boolean canPlayAI() {
        return ai.canPlayAIWithSubs(getActivatingPlayer(), this) && super.canPlayAI();
    }

    @Override
    public void resolve() {
        effect.resolve(this);
    }

    @Override
    public boolean canPlayFromEffectAI(final boolean mandatory, final boolean withOutManaCost) {
        boolean chance = false;
        if (withOutManaCost) {
            chance = ai.doTriggerNoCostWithSubs(this.getActivatingPlayer(), this, mandatory);
        } else {
            chance = ai.doTriggerAI(this.getActivatingPlayer(), this, mandatory);
        }
        return chance && super.canPlayAI();
    }
}
