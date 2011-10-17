package forge.card.staticAbility;

import java.util.HashMap;

import forge.Card;
import forge.Player;

public class StaticAbility_CantBeCast {

    /**
     * 
     * TODO Write javadoc for this method.
     * @param stAb a StaticAbility
     */
    public static boolean applyCantBeCastAbility(final StaticAbility stAb, Card card, Player activator) {
        HashMap<String, String> params = stAb.getMapParams();
        Card hostCard = stAb.getHostCard();
        
        if(params.containsKey("ValidCard") && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return false;
        }
        
        if(params.containsKey("Caster") && !activator.isValid(params.get("Caster"), hostCard.getController(), hostCard)) {
            return false;
        }
        
        return true;
    }

}
