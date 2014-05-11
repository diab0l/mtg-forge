package forge.menu;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.toolbox.FDisplayObject;
import forge.util.Utils;

public class FMenuTab extends FDisplayObject {
    private static final FSkinFont FONT = FSkinFont.get(12);
    private static final FSkinColor SEL_BACK_COLOR = FSkinColor.get(Colors.CLR_ACTIVE);
    private static final FSkinColor SEL_BORDER_COLOR = FDropDown.BORDER_COLOR;
    private static final FSkinColor SEL_FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor FORE_COLOR = SEL_FORE_COLOR.alphaColor(0.5f);
    private static final FSkinColor SEPARATOR_COLOR = SEL_FORE_COLOR.alphaColor(0.3f);
    private static final float PADDING = Utils.scaleMin(2);
    private static final float SEPARATOR_WIDTH = Utils.scaleX(1);

    private final FMenuBar menuBar;
    private final FDropDown dropDown;

    private String text;
    private float minWidth;

    public FMenuTab(String text0, FMenuBar menuBar0, FDropDown dropDown0) {
        menuBar = menuBar0;
        dropDown = dropDown0;
        setText(text0);
    }

    @Override
    public boolean tap(float x, float y, int count) {
        if (dropDown.isVisible()) {
            dropDown.hide();
        }
        else {
            dropDown.show();
        }
        return true;
    }

    public void setText(String text0) {
        text = text0;
        minWidth = FONT.getFont().getBounds(text).width;
        menuBar.revalidate();
    }

    @Override
    public void setVisible(boolean visible0) {
        if (isVisible() == visible0) { return; }
        super.setVisible(visible0);
        if (!visible0) {
            dropDown.hide();
        }
        menuBar.revalidate();
    }

    public float getMinWidth() {
        return minWidth;
    }

    @Override
    public void draw(Graphics g) {
        float x, y, w, h;

        FSkinColor foreColor;
        if (dropDown.isVisible()) {
            x = PADDING; //round so lines show up reliably
            y = PADDING;
            w = getWidth() - 2 * x + 1;
            h = getHeight() - y + 1;

            g.startClip(x, y, w, h);
            g.fillRect(SEL_BACK_COLOR, x, y, w, h);
            g.drawRect(2, SEL_BORDER_COLOR, x, y, w, h);
            g.endClip();

            foreColor = SEL_FORE_COLOR;
        }
        else { 
            foreColor = FORE_COLOR;
        }

        //draw right separator
        x = getWidth();
        y = getHeight() / 4;
        g.drawLine(SEPARATOR_WIDTH, SEPARATOR_COLOR, x, y, x, getHeight() - y);

        x = PADDING;
        y = PADDING;
        w = getWidth() - 2 * PADDING;
        h = getHeight() - 2 * PADDING;
        g.drawText(text, FONT, foreColor, x, y, w, h, false, HAlignment.CENTER, true);
    }
}
