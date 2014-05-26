package forge.toolbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.screens.FScreen;
import forge.util.Utils;

public class FList<E> extends FScrollPane implements Iterable<E> {
    public static final float PADDING = Utils.scaleMin(3);
    public static final FSkinColor FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    public static final FSkinColor PRESSED_COLOR = FSkinColor.get(Colors.CLR_ACTIVE).alphaColor(0.9f);
    public static final FSkinColor LINE_COLOR = FORE_COLOR.alphaColor(0.5f);

    private final List<E> items = new ArrayList<E>();
    private FSkinFont font;
    private ListItemRenderer<E> renderer;
    private int pressedIndex = -1;

    public FList() {
        initialize();
    }
    public FList(E[] itemArray) {
        for (E item : itemArray) {
            addItem(item);
        }
        initialize();
    }
    public FList(Iterable<E> items0) {
        for (E item : items0) {
            addItem(item);
        }
        initialize();
    }

    private void initialize() {
        font = FSkinFont.get(14);
        renderer = new DefaultListItemRenderer<E>();
    }

    public void addItem(E item) {
        items.add(item);
    }

    public void removeItem(E item) {
        items.remove(item);
    }

    @Override
    public void clear() {
        super.clear();
        items.clear();
    }

    public List<E> extractListData() {
        return new ArrayList<E>(items); //create copy to avoid modifying items
    }
    public void setListData(Iterable<E> items0) {
        clear();
        for (E item : items0) {
            addItem(item);
        }
        revalidate();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getCount() {
        return items.size();
    }

    public E getItemAt(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    public int getIndexOf(E item) {
        return items.indexOf(item);
    }

    public E getItemAtPoint(float x, float y) {
        return getItemAt(getIndexAtPoint(x, y));
    }

    public int getIndexAtPoint(float x, float y) {
        return (int)((getScrollTop() + y) / renderer.getItemHeight());
    }

    public ListItemRenderer<E> getListItemRenderer() {
        return renderer;
    }
    public void setListItemRenderer(ListItemRenderer<E> renderer0) {
        renderer = renderer0;
    }

    public FSkinFont getFont() {
        return font;
    }
    public void setFont(FSkinFont font0) {
        font = font0;
    }

    @Override
    protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
        return new ScrollBounds(visibleWidth, items.size() * renderer.getItemHeight());
    }

    @Override
    public boolean press(float x, float y) {
        pressedIndex = getIndexAtPoint(x, y);
        return true;
    }

    @Override
    public boolean release(float x, float y) {
        pressedIndex = -1;
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count) {
        int index = getIndexAtPoint(x, y);
        E item = getItemAt(index);
        if (item == null) { return false; }

        return renderer.tap(item, x, y - getItemTop(index), count);
    }

    private float getItemTop(int index) {
        return index * renderer.getItemHeight() - getScrollTop();
    }

    public void scrollIntoView(int index) {
        float itemTop = getItemTop(index);
        if (itemTop < 0) {
            setScrollTop(getScrollTop() + itemTop);
        }
        else {
            float itemBottom = itemTop + renderer.getItemHeight();
            if (itemBottom > getHeight()) {
                setScrollTop(getScrollTop() + itemBottom - getHeight());
            }
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        //support scrolling texture with list
        g.drawImage(FSkinTexture.BG_TEXTURE, -getScrollLeft(), -getScrollTop(), getScrollWidth(), getScrollHeight());
        g.fillRect(FScreen.TEXTURE_OVERLAY_COLOR, 0, 0, getWidth(), getHeight());
    }

    @Override
    public void draw(Graphics g) {
        float w = getWidth();
        float h = getHeight();
        g.startClip(0, 0, w, h);
        drawBackground(g);
        
        //draw only items that are visible
        if (!items.isEmpty()) {
            int startIndex = getIndexAtPoint(0, 0);
            float itemHeight = renderer.getItemHeight();
            boolean drawSeparators = drawLineSeparators();

            float y = Math.round(getItemTop(startIndex)); //round y so items don't flicker from rounding error
            float valueWidth = w - 2 * PADDING;
            float valueHeight = itemHeight - 2 * PADDING;

            for (int i = startIndex; i < items.size(); i++) {
                if (y > h) { break; }

                FSkinColor fillColor = getItemFillColor(i);
                if (fillColor != null) {
                    g.fillRect(fillColor, 0, y, w, itemHeight);
                }

                renderer.drawValue(g, items.get(i), font, FORE_COLOR, pressedIndex == i, PADDING, y + PADDING, valueWidth, valueHeight);

                y += itemHeight;

                if (drawSeparators) {
                    g.drawLine(1, LINE_COLOR, 0, y, w, y);
                }
            }
        }
        
        drawOverlay(g);
        g.endClip();
    }

    protected FSkinColor getItemFillColor(int index) {
        if (index == pressedIndex) {
            return FList.PRESSED_COLOR;
        }
        return null;
    }

    protected boolean drawLineSeparators() {
        return true;
    }

    public static abstract class ListItemRenderer<V> {
        public abstract float getItemHeight();
        public abstract boolean tap(V value, float x, float y, int count);
        public abstract void drawValue(Graphics g, V value, FSkinFont font, FSkinColor foreColor, boolean pressed, float x, float y, float w, float h);
    }

    public static class DefaultListItemRenderer<V> extends ListItemRenderer<V> {
        @Override
        public float getItemHeight() {
            return Utils.AVG_FINGER_HEIGHT;
        }

        @Override
        public boolean tap(V value, float x, float y, int count) {
            return false;
        }

        @Override
        public void drawValue(Graphics g, V value, FSkinFont font, FSkinColor color, boolean pressed, float x, float y, float w, float h) {
            g.drawText(value.toString(), font, color, x, y, w, h, false, HAlignment.LEFT, true);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return items.iterator();
    }
}
