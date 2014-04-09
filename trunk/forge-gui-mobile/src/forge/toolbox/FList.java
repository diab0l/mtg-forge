package forge.toolbox;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.screens.FScreen;
import forge.toolbox.FEvent.FEventHandler;
import forge.util.Utils;

public class FList<E> extends FScrollPane {
    public static final float INSETS_FACTOR = 0.025f;
    private static final float GROUP_HEADER_HEIGHT = Math.round(Utils.AVG_FINGER_HEIGHT * 0.6f);
    private static final FSkinColor FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor PRESSED_COLOR = FSkinColor.get(Colors.CLR_ACTIVE).alphaColor(0.9f);
    public static final FSkinColor LINE_COLOR = FORE_COLOR.alphaColor(0.5f);

    private final List<ListGroup> groups = new ArrayList<ListGroup>();
    private FSkinFont font;
    private ListItemRenderer<E> renderer;

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

    public void addGroup(String groupName) {
        groups.add(add(new ListGroup(groupName)));
    }

    public void addItem(E item) {
        addItem(item, 0);
    }
    public void addItem(E item, int groupIndex) {
        if (groups.isEmpty()) {
            addGroup(null);
        }
        if (groupIndex > groups.size()) {
            groupIndex = groups.size() - 1;
        }
        groups.get(groupIndex).addItem(new ListItem(item));
    }

    public void removeItem(E item) {
        for (ListGroup group : groups) {
            for (ListItem groupItem : group.items) {
                if (groupItem.value == item) {
                    group.removeItem(groupItem);
                    if (group.items.isEmpty()) {
                        groups.remove(group);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        groups.clear();
    }

    public void setListData(Iterable<E> items0) {
        clear();
        for (E item : items0) {
            addItem(item);
        }
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public int getCount() {
        int count = 0;
        for (ListGroup group : groups) {
            count += group.items.size();
        }
        return count;
    }

    public E getItemAt(int index) {
        int count = 0;
        for (ListGroup group : groups) {
            for (ListItem item : group.items) {
                if (index == count) {
                    return item.value;
                }
                count++;
            }
        }
        return null;
    }

    public int getIndexOf(E value) {
        int count = 0;
        for (ListGroup group : groups) {
            for (ListItem item : group.items) {
                if (item.value == value) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public void setListItemRenderer(ListItemRenderer<E> renderer0) {
        renderer = renderer0;
    }

    @Override
    protected void drawBackground(Graphics g) {
        //support scrolling texture with list
        g.drawImage(FSkinTexture.BG_TEXTURE, -getScrollLeft(), -getScrollTop(), getScrollWidth(), getScrollHeight());
        g.fillRect(FScreen.TEXTURE_OVERLAY_COLOR, 0, 0, getWidth(), getHeight());
    }

    @Override
    protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
        float y = 0;
        float groupHeight;

        for (ListGroup group : groups) {
            groupHeight = group.getPreferredHeight();
            group.setBounds(0, y, visibleWidth, groupHeight);
            y += groupHeight;
        }

        return new ScrollBounds(visibleWidth, y);
    }

    private class ListGroup extends FContainer {
        private final FLabel header;
        private final List<ListItem> items = new ArrayList<ListItem>();

        private boolean isCollapsed;

        private ListGroup(String name0) {
            if (name0 == null) {
                header = null;
            }
            else {
                header = add(new FLabel.ButtonBuilder().text(name0).command(new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        isCollapsed = !isCollapsed;
                        FList.this.revalidate();
                    }
                }).build());
            }
        }

        public void addItem(ListItem item) {
            items.add(item);
            add(item);
        }

        public boolean removeItem(ListItem item) {
            if (items.remove(item)) {
                remove(item);
                return true;
            }
            return false;
        }

        public float getPreferredHeight() {
            float height = 0;
            if (header != null) {
                height += GROUP_HEADER_HEIGHT;
            }
            if (!isCollapsed) {
                height += renderer.getItemHeight() * items.size();
            }
            return height;
        }

        @Override
        protected void doLayout(float width, float height) {
            float y = 0;
            if (header != null) {
                header.setBounds(0, y, width, GROUP_HEADER_HEIGHT);
                y += GROUP_HEADER_HEIGHT;
            }

            float itemHeight = renderer.getItemHeight();

            for (ListItem item : items) {
                item.setBounds(0, y, width, itemHeight);
                y += itemHeight;
            }
        }
    }

    private class ListItem extends FDisplayObject {
        private final E value;
        private boolean pressed;

        private ListItem(E value0) {
            value = value0;
        }

        public boolean press(float x, float y) {
            pressed = true;
            return true;
        }

        public boolean release(float x, float y) {
            pressed = false;
            return true;
        }

        public boolean tap(float x, float y, int count) {
            return renderer.tap(value, x, y, count);
        }

        @Override
        public final void draw(Graphics g) {
            float w = getWidth();
            float h = renderer.getItemHeight();

            if (pressed) {
                g.fillRect(PRESSED_COLOR, 0, 0, w, h);
            }

            renderer.drawValue(g, value, font, FORE_COLOR, w, h); //-1 to not account for border

            g.drawLine(1, LINE_COLOR, 0, h, w, h);
        }
    }

    public static abstract class ListItemRenderer<V> {
        public abstract float getItemHeight();
        public abstract boolean tap(V value, float x, float y, int count);
        public abstract void drawValue(Graphics g, V value, FSkinFont font, FSkinColor foreColor, float width, float height);
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
        public void drawValue(Graphics g, V value, FSkinFont font, FSkinColor color, float width, float height) {
            float x = width * INSETS_FACTOR;
            g.drawText(value.toString(), font, color, x, 0, width - 2 * x, height, false, HAlignment.LEFT, true);
        }
    }
}
