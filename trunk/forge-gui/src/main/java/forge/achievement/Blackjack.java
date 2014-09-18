package forge.achievement;

import forge.assets.FSkinProp;
import forge.game.Game;
import forge.game.player.GameLossReason;
import forge.game.player.Player;

public class Blackjack extends Achievement {
    private static final int THRESHOLD = 21;

    public Blackjack(int silver0, int gold0, int mythic0) {
        super("Blackjack", "Win a game from your commander dealing",
            String.format("%d combat damage", THRESHOLD), THRESHOLD,
            String.format("%d combat damage", silver0), silver0,
            String.format("%d combat damage", gold0), gold0,
            String.format("%d combat damage", mythic0), mythic0,
            FSkinProp.IMG_BLACKJACK);
    }

    @Override
    protected int evaluate(Player player, Game game) {
        if (player.getOutcome().hasWon()) {
            for (Player p : game.getRegisteredPlayers()) {
                if (p.isOpponentOf(player) && p.getOutcome().lossState == GameLossReason.CommanderDamage) {
                    Integer damage = p.getCommanderDamage().get(player.getCommander());
                    if (damage != null && damage >= THRESHOLD) {
                        return damage;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public String getSubTitle() {
        if (best > 0) {
            return "Best: " + best + " Damage";
        }
        return null;
    }
}
