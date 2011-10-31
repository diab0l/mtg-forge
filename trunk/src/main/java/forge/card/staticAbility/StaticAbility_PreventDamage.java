package forge.card.staticAbility;

import java.util.HashMap;

import forge.Card;
import forge.GameEntity;
import forge.card.cardFactory.CardFactoryUtil;

/**
 * The Class StaticAbility_PreventDamage.
 */
public class StaticAbility_PreventDamage {

    /**
     * TODO Write javadoc for this method.
     * 
     * @param stAb
     *            a StaticAbility
     * @param source
     *            the source
     * @param target
     *            the target
     * @param damage
     *            the damage
     * @param isCombat
     *            the is combat
     * @return the int
     */
    public static int applyPreventDamageAbility(final StaticAbility stAb, final Card source, final GameEntity target,
            final int damage, final boolean isCombat) {
        final HashMap<String, String> params = stAb.getMapParams();
        final Card hostCard = stAb.getHostCard();
        int restDamage = damage;

        if (params.containsKey("Source")
                && !source.isValid(params.get("Source").split(","), hostCard.getController(), hostCard)) {
            return restDamage;
        }

        if (params.containsKey("Target")
                && !target.isValid(params.get("Target").split(","), hostCard.getController(), hostCard)) {
            return restDamage;
        }

        if (params.containsKey("CombatDamage") && params.get("CombatDamage").equals("True") && !isCombat) {
            return restDamage;
        }

        if (params.containsKey("CombatDamage") && params.get("CombatDamage").equals("False") && isCombat) {
            return restDamage;
        }

        if (params.containsKey("MaxDamage") && (Integer.parseInt(params.get("MaxDamage")) < damage)) {
            return restDamage;
        }

        // no amount means all
        if (!params.containsKey("Amount") || params.get("Amount").equals("All")) {
            return 0;
        }

        if (params.get("Amount").matches("[0-9][0-9]?")) {
            restDamage = restDamage - Integer.parseInt(params.get("Amount"));
        } else {
            restDamage = restDamage - CardFactoryUtil.xCount(hostCard, hostCard.getSVar(params.get("Amount")));
        }

        if (restDamage < 0) {
            return 0;
        }

        return restDamage;
    }

}
