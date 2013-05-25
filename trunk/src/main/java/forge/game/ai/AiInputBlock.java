package forge.game.ai;

import java.util.List;

import forge.Card;
import forge.control.input.InputPassPriorityBase;
import forge.game.GameState;
import forge.game.phase.CombatUtil;
import forge.game.player.Player;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class AiInputBlock extends InputPassPriorityBase {
    private final GameState game;
    /**
     * TODO: Write javadoc for Constructor.
     * @param game
     * @param player
     */
    public AiInputBlock(Player human) {
        super(human);
        this.game = human.getGame();
    }

    private static final long serialVersionUID = -2253562658069995572L;

    @Override
    public void showMessage() {
        // TODO Auto-generated method stub
        final List<Card> blockers = player.getCreaturesInPlay();
        game.setCombat(ComputerUtilBlock.getBlockers(player, game.getCombat(), blockers));
        CombatUtil.orderMultipleCombatants(game);
        pass();
    }
}
