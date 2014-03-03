package forge.screens.match.views;

import com.badlogic.gdx.graphics.Color;

import forge.Forge.Graphics;
import forge.toolbox.FContainer;
import forge.utils.Utils;

public class VStack extends FContainer {
    public static final float WIDTH = Utils.AVG_FINGER_WIDTH;
    public static final float HEIGHT = WIDTH * Utils.CARD_ASPECT_RATIO;

    public VStack() {
        setSize(WIDTH, HEIGHT);
    }

    @Override
    protected void doLayout(float width, float height) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void drawBackground(Graphics g) {
        float w = getWidth();
        float h = getHeight();
        g.fillRect(Color.BLUE, 0, 0, w, h);
    }
}
