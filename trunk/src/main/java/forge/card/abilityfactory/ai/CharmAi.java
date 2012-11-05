package forge.card.abilityfactory.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import forge.card.abilityfactory.SpellAiLogic;
import forge.card.abilityfactory.effects.CharmEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;
import forge.util.MyRandom;

public class CharmAi extends SpellAiLogic {
    
    @Override
    public boolean canPlayAI(Player ai, java.util.Map<String,String> params, SpellAbility sa) {
        final Random r = MyRandom.getRandom();

        final int num = Integer.parseInt(params.containsKey("CharmNum") ? params.get("CharmNum") : "1");
        final int min = params.containsKey("MinCharmNum") ? Integer.parseInt(params.get("MinCharmNum")) : num;
        boolean timingRight = sa.isTrigger(); //is there a reason to play the charm now?

        List<AbilitySub> chooseFrom = CharmEffect.makePossibleOptions(sa, params);
        List<AbilitySub> chosenList = chooseOptionsAi(ai, timingRight, chooseFrom, num, min);
        
        if (chosenList == null || chosenList.isEmpty()) {
            return false;
        }        

        // prevent run-away activations - first time will always return true
        return r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());
    }

    public static List<AbilitySub> chooseOptionsAi(final Player ai, boolean playNow, List<AbilitySub> choices, int num, int min) {
        List<AbilitySub> chosenList = new ArrayList<AbilitySub>();

        for (int i = 0; i < num; i++) {
            AbilitySub thisPick = null;
            for (SpellAbility sub : choices) {
                sub.setActivatingPlayer(ai);
                if (!playNow && sub.canPlayAI()) {
                    thisPick = (AbilitySub) sub;
                    choices.remove(sub);
                    playNow = true;
                    break;
                }
                if ((playNow || i < num - 1) && sub.doTrigger(false)) {
                    thisPick = (AbilitySub) sub;
                    choices.remove(sub);
                    break;
                }
            }
            if (thisPick != null)
                chosenList.add(thisPick);
        }
        return chosenList.size() >= min ? chosenList : null;
    }
}