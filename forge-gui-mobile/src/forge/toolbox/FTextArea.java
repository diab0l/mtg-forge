package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Vector2;

import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.TextRenderer;

public class FTextArea extends FScrollPane {
    private String text;
    private FSkinFont font;
    private HAlignment alignment;
    private Vector2 insets;
    private FSkinColor textColor;
    private final TextRenderer renderer = new TextRenderer(true);

    public FTextArea() {
        this("");
    }
    public FTextArea(String text0) {
        text = text0;
        font = FSkinFont.get(14);
        alignment = HAlignment.LEFT;
        insets = new Vector2(1, 1); //prevent text getting cut off by clip
        textColor = FLabel.DEFAULT_TEXT_COLOR;
    }

    public String getText() {
        return text;
    }
    public void setText(String text0) {
        text = text0;
        revalidate();
    }

    public HAlignment getAlignment() {
        return alignment;
    }
    public void setAlignment(HAlignment alignment0) {
        alignment = alignment0;
    }

    public FSkinFont getFont() {
        return font;
    }
    public void setFont(FSkinFont font0) {
        font = font0;
        revalidate();
    }

    public FSkinColor getTextColor() {
        return textColor;
    }
    public void setTextColor(FSkinColor textColor0) {
        textColor = textColor0;
    }

    public float getPreferredHeight(float width) {
        return renderer.getWrappedBounds(text, font, width - 2 * insets.x).height
                + font.getLineHeight() - font.getCapHeight() //need to account for difference in line and cap height
                + 2 * insets.y;
    }

    @Override
    protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
        return new ScrollBounds(visibleWidth, getPreferredHeight(visibleWidth));
    }

    @Override
    protected void drawBackground(Graphics g) {
        renderer.drawText(g, text, font, textColor, insets.x - getScrollLeft(), insets.y - getScrollTop(), getScrollWidth() - 2 * insets.x, getScrollHeight() - 2 * insets.y, 0, getHeight(), true, alignment, false);
    }
}
