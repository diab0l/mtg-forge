package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import forge.Card;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;

public class FightEffect extends SpellAbilityEffect {

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        List<Card> fighters = getFighters(sa);

        if (fighters.size() > 1) {
            sb.append(fighters.get(0) + " fights " + fighters.get(1));
        }
        else {
            sb.append(fighters.get(0) + " fights unknown");
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        List<Card> fighters = getFighters(sa);

        if (fighters.size() < 2 || !fighters.get(0).isInPlay()
                || !fighters.get(1).isInPlay()) {
            return;
        }

        int dmg2 = fighters.get(1).getNetAttack();
        fighters.get(1).addDamage(fighters.get(0).getNetAttack(), fighters.get(0));
        fighters.get(0).addDamage(dmg2, fighters.get(1));
    }

    private static List<Card> getFighters(SpellAbility sa) {
        final List<Card> fighterList = new ArrayList<Card>();

        Card fighter1 = null;
        Card fighter2 = null;
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        List<Card> tgts = null;
        if (tgt != null) {
            tgts = Lists.newArrayList(sa.getTargets().getTargetCards());
            if (tgts.size() > 0) {
                fighter1 = tgts.get(0);
            }
        }
        if (sa.hasParam("Defined")) {
            List<Card> defined = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa);
            // Allow both fighters to come from defined list if first fighter not already found
            if (!defined.isEmpty()) {
                if (defined.size() > 1 && fighter1 == null) {
                    fighter1 = defined.get(0);
                    fighter2 = defined.get(1);
                }
                else {
                    fighter2 = defined.get(0);
                }
            }
        } else if (tgts.size() > 1) {
            fighter2 = tgts.get(1);
        }

        if (fighter1 != null) {
            fighterList.add(fighter1);
        }
        if (fighter2 != null) {
            fighterList.add(fighter2);
        }

        return fighterList;
    }

}
