package forge.screens.match.views;

import com.badlogic.gdx.graphics.Color;

import forge.Forge.Graphics;
import forge.toolbox.FContainer;

public class VLog extends FContainer {

    @Override
    protected void doLayout(float width, float height) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void drawBackground(Graphics g) {
        float w = getWidth();
        float h = getHeight();
        g.fillRect(Color.MAGENTA, 0, 0, w, h);
    }
}
