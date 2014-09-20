package forge.achievement;

import forge.assets.FSkinProp;
import forge.game.Game;
import forge.game.player.Player;

public class TotalMatchWins extends Achievement {
    public TotalMatchWins(int bronze0, int silver0, int gold0, int mythic0) {
        super("Total Match Wins", null,
            String.format("Win %d matches", bronze0), bronze0,
            String.format("Win %d matches", silver0), silver0,
            String.format("Win %d matches", gold0), gold0,
            String.format("Win %d matches", mythic0), mythic0,
            FSkinProp.IMG_TOTAL_MATCH_WINS);
    }

    @Override
    protected int evaluate(Player player, Game game) {
        if (game.getMatch().isMatchOver()) {
            if (game.getMatch().isWonBy(player.getLobbyPlayer())) {
                return current + 1;
            }
        }
        return current;
    }

    @Override
    public String getSubTitle() {
        return Integer.toString(current);
    }
}