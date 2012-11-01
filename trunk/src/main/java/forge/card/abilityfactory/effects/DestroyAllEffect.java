package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import forge.Card;
import forge.CardLists;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class DestroyAllEffect extends SpellEffect {
    
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(Map<String, String> params, SpellAbility sa) {
        
        final boolean noRegen = params.containsKey("NoRegen");
        final Card card = sa.getSourceCard();

        final Target tgt = sa.getTarget();
        Player targetPlayer = null;
        if (tgt != null) {
            for (final Object o : tgt.getTargets()) {
                if (o instanceof Player) {
                    targetPlayer = (Player) o;
                }
            }
        }

        String valid = "";

        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }

        // Ugh. If calculateAmount needs to be called with DestroyAll it _needs_
        // to use the X variable
        // We really need a better solution to this
        if (valid.contains("X")) {
            valid = valid.replace("X", Integer.toString(AbilityFactory.calculateAmount(card, "X", sa)));
        }

        List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);

        if (targetPlayer != null) {
            list = CardLists.filterControlledBy(list, targetPlayer);
        }

        list = AbilityFactory.filterListByType(list, valid, sa);

        final boolean remDestroyed = params.containsKey("RememberDestroyed");
        if (remDestroyed) {
            card.clearRemembered();
        }

        if (noRegen) {
            for (int i = 0; i < list.size(); i++) {
                if (Singletons.getModel().getGame().getAction().destroyNoRegeneration(list.get(i)) && remDestroyed) {
                    card.addRemembered(list.get(i));
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (Singletons.getModel().getGame().getAction().destroy(list.get(i)) && remDestroyed) {
                    card.addRemembered(list.get(i));
                }
            }
        }
    }

    /**
     * <p>
     * destroyAllStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noRegen
     *            a boolean.
     * @return a {@link java.lang.String} object.
     */
    protected String getStackDescription(java.util.Map<String,String> params, SpellAbility sa) {
    
        final StringBuilder sb = new StringBuilder();
        final String name = sa.getAbilityFactory().getHostCard().getName();
        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }
        final boolean noRegen = params.containsKey("NoRegen");
    
        if (params.containsKey("SpellDescription")) {
            sb.append(params.get("SpellDescription"));
        } else {
            final String conditionDesc = params.get("ConditionDescription");
            if (conditionDesc != null) {
                sb.append(conditionDesc).append(" ");
            }
    
            ArrayList<Card> tgtCards;
    
            final Target tgt = sa.getTarget();
            if (tgt != null) {
                tgtCards = tgt.getTargetCards();
            } else {
                tgtCards = new ArrayList<Card>();
                tgtCards.add(sa.getSourceCard());
            }
    
            sb.append(name).append(" - Destroy permanents.");
    
            if (noRegen) {
                sb.append(" They can't be regenerated");
            }
        }
        return sb.toString();
    }

} // end class AbilityFactory_Destroy