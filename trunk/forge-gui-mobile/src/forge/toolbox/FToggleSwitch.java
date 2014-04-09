package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FEvent.FEventType;

public class FToggleSwitch extends FDisplayObject {
    private static final FSkinColor ACTIVE_COLOR = FSkinColor.get(Colors.CLR_ACTIVE);
    private static final FSkinColor PRESSED_COLOR = ACTIVE_COLOR.stepColor(-30);
    private static final FSkinColor INACTIVE_COLOR = FSkinColor.get(Colors.CLR_INACTIVE);
    private static final FSkinColor FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final float INSETS = 2;
    private static final float PADDING = 3;

    private FSkinFont font;
    private final String offText, onText;
    private boolean toggled, pressed;
    private FEventHandler changedHandler;

    public FToggleSwitch() {
        this("Off", "On");
    }

    public FToggleSwitch(final String offText0, final String onText0) {
        offText = offText0;
        onText = onText0;
        font = FSkinFont.get(14);
    }

    public void setFontSize(int fontSize0) {
        font = FSkinFont.get(fontSize0);
    }

    public boolean isToggled() {
        return toggled;
    }
    public void setToggled(boolean b0) {
        setToggled(b0, false);
    }
    private void setToggled(boolean b0, boolean raiseChangedEvent) {
        if (toggled == b0) { return; }
        toggled = b0;

        if (raiseChangedEvent && changedHandler != null) {
            changedHandler.handleEvent(new FEvent(this, FEventType.CHANGE, b0));
        }
    }

    public FEventHandler getChangedHandler() {
        return changedHandler;
    }
    public void setChangedHandler(FEventHandler changedHandler0) {
        changedHandler = changedHandler0;
    }

    public float getAutoSizeWidth(float height) {
        float width;
        float onTextWidth = font.getFont().getBounds(onText).width;
        float offTextWidth = font.getFont().getBounds(offText).width;
        if (onTextWidth > offTextWidth) {
            width = onTextWidth;
        }
        else {
            width = offTextWidth;
        }
        width += 2 * (PADDING + INSETS + 1);
        width += height - PADDING; //leave room for switch to move
        return width;
    }

    @Override
    public final boolean press(float x, float y) {
        pressed = true;
        return true;
    }

    @Override
    public final boolean release(float x, float y) {
        pressed = false;
        return true;
    }

    @Override
    public final boolean tap(float x, float y, int count) {
        setToggled(!toggled, true);
        return true;
    }

    //support dragging finger left or right to toggle on/off
    @Override
    public final boolean pan(float x, float y, float deltaX, float deltaY) {
        if (contains(getLeft() + x, getTop() + y)) {
            if (x < getHeight()) {
                setToggled(false, true);
                return true;
            }
            if (x > getWidth() - getHeight()) {
                setToggled(true, true);
                return true;
            }
            pressed = true;
        }
        else {
            pressed = false;
        }
        return false;
    }

    @Override
    public final boolean panStop(float x, float y) {
        if (pressed) {
            pressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY) {
        return Math.abs(velocityX) > Math.abs(velocityY); //handle fling if more horizontal than vertical
    }

    @Override
    public void draw(Graphics g) {
        float x = 1; //leave a pixel so border displays in full
        float y = 1;
        float w = getWidth() - 2 * x;
        float h = getHeight() - 2 * x;

        g.fillRect(INACTIVE_COLOR, x, y, w, h);
        g.drawRect(1, FORE_COLOR, x, y, w, h);

        final String text;
        float switchWidth = w - h + PADDING;
        if (toggled) {
            x = w - switchWidth + 1;
            text = onText;
        }
        else {
            text = offText;
        }
        x += INSETS;
        y += INSETS;
        h -= 2 * INSETS;
        w = switchWidth - 2 * INSETS;
        g.fillRect(pressed ? PRESSED_COLOR : ACTIVE_COLOR, x, y, w, h);

        x += PADDING;
        w -= 2 * PADDING;
        g.drawText(text, font, FORE_COLOR, x, y, w, h, false, HAlignment.CENTER, true);
    }
}
