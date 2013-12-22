package forge.gui.player;

import forge.game.Game;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerController;
import forge.game.player.PlayerType;
import forge.gui.FNetOverlay;
import forge.gui.GuiDisplayUtil;

public class LobbyPlayerHuman extends LobbyPlayer {
    public LobbyPlayerHuman(String name) {
        super(name);
    }

    @Override
    public PlayerType getType() {
        return PlayerType.HUMAN;
    }

    @Override
    public PlayerController createControllerFor(Player human) {
        return new PlayerControllerHuman(human.getGame(), human, this);
    }
    
    @Override
    public Player getPlayer(Game game) {
        Player player = new Player(GuiDisplayUtil.personalizeHuman(getName()), game);
        player.setFirstController(new PlayerControllerHuman(game, player, this));
        return player;
    }

    public void hear(LobbyPlayer player, String message) {
        FNetOverlay.SINGLETON_INSTANCE.addMessage(player.getName(), message);
    }
}