package forge.toolbox;

import java.util.Stack;

import com.badlogic.gdx.math.Vector2;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;

public abstract class FOverlay extends FContainer {
    private static FSkinColor BACK_COLOR;
    private static final Stack<FOverlay> overlays = new Stack<FOverlay>();

    public FOverlay() {
        super.setVisible(false); //hide by default
    }

    public static FOverlay getTopOverlay() {
        if (overlays.isEmpty()) {
            return null;
        }
        return overlays.peek();
    }

    public static Iterable<FOverlay> getOverlays() {
        return overlays;
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean visible0) {
        if (this.isVisible() == visible0) { return; }

        if (visible0) {
            if (BACK_COLOR == null) { //wait to initialize back color until first overlay shown
                BACK_COLOR = FSkinColor.get(Colors.CLR_OVERLAY).alphaColor(0.5f);
            }
            overlays.push(this);
        }
        else {
            overlays.pop();
        }
        super.setVisible(visible0);
    }

    @Override
    public void drawBackground(Graphics g) {
        g.fillRect(BACK_COLOR, 0, 0, this.getWidth(), this.getHeight());
    }

    //override all gesture listeners to prevent passing to display objects behind it
    @Override
    public boolean press(float x, float y) {
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        return true;
    }

    @Override
    public boolean release(float x, float y) {
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count) {
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return true;
    }

    @Override
    public boolean panStop(float x, float y) {
        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return true;
    }
}