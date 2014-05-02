package forge.game.event;

import forge.LobbyPlayer;
import forge.game.player.Player;

public class GameEventPlayerControl extends GameEvent {
    public final Player player;
    public final LobbyPlayer oldController;
    public final LobbyPlayer newController;

    public GameEventPlayerControl(Player p, LobbyPlayer old, LobbyPlayer new1) {
        player = p;
        oldController = old;
        newController = new1;
    }

    @Override
    public <T> T visit(IGameEventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}