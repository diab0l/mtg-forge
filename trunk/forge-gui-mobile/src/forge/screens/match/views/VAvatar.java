package forge.screens.match.views;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import forge.Forge.Graphics;
import forge.assets.FSkin;
import forge.game.player.Player;
import forge.screens.match.FControl;
import forge.toolbox.FDisplayObject;
import forge.util.Utils;

public class VAvatar extends FDisplayObject {
    public static final float WIDTH = Utils.AVG_FINGER_WIDTH;
    public static final float HEIGHT = Utils.AVG_FINGER_HEIGHT;

    private final Player player;
    private final TextureRegion image;

    public VAvatar(Player player0) {
        player = player0;
        image = FSkin.getAvatars().get(player.getLobbyPlayer().getAvatarIndex());
        setSize(WIDTH, HEIGHT);
    }

    @Override
    public boolean tap(float x, float y, int count) {
        FControl.getInputProxy().selectPlayer(player, null);
        return true;
    }

    @Override
    public void draw(Graphics g) {
        float w = getWidth();
        float h = getHeight();
        g.drawImage(image, 0, 0, w, h);
    }
}
