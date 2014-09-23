package forge.achievement;

import forge.game.Game;
import forge.game.player.Player;

public class MatchWinStreak extends Achievement {
    public MatchWinStreak(int bronze0, int silver0, int gold0, int mythic0) {
        super("MatchWinStreak", "Match Win Streak", null,
            String.format("Win %d matches in a row", bronze0), bronze0,
            String.format("Win %d matches in a row", silver0), silver0,
            String.format("Win %d matches in a row", gold0), gold0,
            String.format("Win %d matches in a row", mythic0), mythic0);
    }

    @Override
    protected int evaluate(Player player, Game game) {
        if (game.getMatch().isMatchOver()) {
            if (game.getMatch().isWonBy(player.getLobbyPlayer())) {
                return current + 1;
            }
            return 0; //reset if player didn't win
        }
        return current;
    }

    @Override
    public String getSubTitle() {
        return "Best: " + best + " Active: " + current;
    }
}