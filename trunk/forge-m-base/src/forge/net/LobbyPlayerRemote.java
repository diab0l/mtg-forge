package forge.net;

import forge.game.Game;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerController;
import forge.net.client.INetClient;
import forge.net.protocol.toclient.ChatPacketClt;

public class LobbyPlayerRemote extends LobbyPlayer {
    
    private final INetClient connection;
    
    public LobbyPlayerRemote(String name, INetClient netClient) { // This is actually a doubtful idea - this means 1 window per player.
        super(name);
        connection = netClient;
    }

    @Override
    protected PlayerType getType() {
        return PlayerType.REMOTE;
    }

    /* (non-Javadoc)
     * @see forge.game.player.LobbyPlayer#getPlayer(forge.game.GameState)
     */
    @Override
    public Player createIngamePlayer(Game gameState) {
        // Cannot create remote players yet 
        throw new UnsupportedOperationException("method is not implemented");
    }

    @Override
    public void hear(LobbyPlayer player, String message) {
        connection.send(new ChatPacketClt(player.getName(), message));
    }

    /* (non-Javadoc)
     * @see forge.game.player.LobbyPlayer#createControllerFor(forge.game.player.Player)
     */
    @Override
    public PlayerController createControllerFor(Player p) {
        // Cannot create remote players yet 
        throw new UnsupportedOperationException("method is not implemented");
    }
}