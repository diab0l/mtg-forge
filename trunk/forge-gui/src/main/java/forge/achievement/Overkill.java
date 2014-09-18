package forge.achievement;

import org.w3c.dom.Element;

import forge.assets.FSkinProp;
import forge.game.Game;
import forge.game.player.Player;

public class Overkill extends Achievement {
    public Overkill(int bronze0, int silver0, int gold0, int mythic0) {
        super("Overkill", "Win a game with opponent at",
            String.format("%d life.", bronze0), bronze0,
            String.format("%d life", silver0), silver0,
            String.format("%d life", gold0), gold0,
            String.format("%d life", mythic0), mythic0,
            FSkinProp.IMG_OVERKILL);
    }

    @Override
    protected int evaluate(Player player, Game game) {
        if (player.getOutcome().hasWon()) {
            Player opponent = getSingleOpponent(player);
            if (opponent != null && opponent.getLife() < 0) {
                return opponent.getLife();
            }
        }
        return 0;
    }

    @Override
    public String getSubTitle() {
        if (best < 0) {
            return "Best: " + best + " Life";
        }
        return null;
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);

        //perform conversion to handle data from old format before supporting negative thresholds
        if (best > 0) {
            best = -best;
        }
        if (current > 0) {
            current = -current;
        }
    }
}
