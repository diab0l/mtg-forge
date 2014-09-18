package forge.achievement;

import forge.assets.FSkinProp;
import forge.game.Game;
import forge.game.player.Player;

public class TotalGameWins extends Achievement {
    public TotalGameWins(int bronze0, int silver0, int gold0, int mythic0) {
        super("Total Game Wins", null,
            String.format("Win %d games", bronze0), bronze0,
            String.format("Win %d games", silver0), silver0,
            String.format("Win %d games", gold0), gold0,
            String.format("Win %d games", mythic0), mythic0,
            FSkinProp.IMG_TOTAL_GAME_WINS);
    }

    @Override
    protected int evaluate(Player player, Game game) {
        if (player.getOutcome().hasWon()) {
            return current + 1;
        }
        return current;
    }

    @Override
    public String getSubTitle() {
        return Integer.toString(current);
    }
}
