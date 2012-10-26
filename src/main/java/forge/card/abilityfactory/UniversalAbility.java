package forge.card.abilityfactory;

import java.util.Map;

import forge.Card;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.Target;

public class UniversalAbility extends AbilityActivated {
    private final SpellEffect effect;
    private final Map<String,String> params;
    private final SpellAiLogic ai;
    
    private static final long serialVersionUID = -4183793555528531978L;

    public UniversalAbility(Card sourceCard, Cost abCost, Target tgt, Map<String,String> params0, SpellEffect effect0, SpellAiLogic ai0) {
        super(sourceCard, abCost, tgt);
        params = params0;
        effect = effect0;
        ai = ai0;
    }
    
    @Override
    public String getStackDescription() {
        return effect.getStackDescription(params, this);
    }
    
    /* (non-Javadoc)
     * @see forge.card.spellability.AbilityActivated#getCopy()
     */
    @Override
    public AbilityActivated getCopy() {
        Target tgt = getTarget() == null ? null : new Target(getTarget());
        AbilityActivated res = new UniversalAbility(getSourceCard(), getPayCosts(), tgt, params, effect, ai);
        CardFactoryUtil.copySpellAbility(this, res);
        return res;
    }

    /* (non-Javadoc)
     * @see forge.card.spellability.SpellAbility#resolve()
     */
    @Override
    public void resolve() {
        effect.resolve(params, this);
    }
    
    @Override
    public boolean canPlayAI() {
        return ai.canPlayAI(getActivatingPlayer(), params, this);
    }        

    @Override
    public boolean doTrigger(final boolean mandatory) {
        return ai.doTriggerAI(this.getActivatingPlayer(), params, this, mandatory);
    }
    
}