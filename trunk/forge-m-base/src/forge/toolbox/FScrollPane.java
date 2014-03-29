package forge.toolbox;

import forge.Forge.Graphics;

public abstract class FScrollPane extends FContainer {
    private float scrollLeft, scrollTop;
    private ScrollBounds scrollBounds;

    protected FScrollPane() {
        scrollBounds = new ScrollBounds();
    }

    public float getScrollLeft() {
        return scrollLeft;
    }

    public float getScrollTop() {
        return scrollTop;
    }

    public float getScrollWidth() {
        return scrollBounds.width;
    }

    public float getScrollHeight() {
        return scrollBounds.height;
    }

    public float getMaxScrollLeft() {
        return getScrollWidth() - getWidth();
    }

    public float getMaxScrollTop() {
        return getScrollHeight() - getHeight();
    }

    public void setScrollPositions(float scrollLeft0, float scrollTop0) {
        if (scrollLeft0 < 0) {
            scrollLeft0 = 0;
        }
        else {
            float maxScrollLeft = getMaxScrollLeft();
            if (scrollLeft0 > maxScrollLeft) {
                scrollLeft0 = maxScrollLeft;
            }
        }
        if (scrollTop0 < 0) {
            scrollTop0 = 0;
        }
        else {
            float maxScrollTop = getMaxScrollTop();
            if (scrollTop0 > maxScrollTop) {
                scrollTop0 = maxScrollTop;
            }
        }
        float dx = scrollLeft - scrollLeft0;
        float dy = scrollTop - scrollTop0;
        if (dx == 0 && dy == 0) { return; } //do nothing if scroll didn't change

        scrollLeft = scrollLeft0;
        scrollTop = scrollTop0;

        //shift position of all children based on change in scroll positions
        for (FDisplayObject obj : getChildren()) {
            obj.setPosition(obj.getLeft() + dx, obj.getTop() + dy);
        }
    }

    @Override
    protected final void doLayout(float width, float height) {
        scrollBounds = layoutAndGetScrollBounds(width, height);
        scrollBounds.increaseWidthTo(width); //ensure scroll bounds extends at least to visible bounds
        scrollBounds.increaseHeightTo(height);

        //attempt to restore current scroll positions after reseting them in order to update positions of newly laid out stuff
        float oldScrollLeft = scrollLeft;
        float oldScrollTop = scrollTop;
        scrollLeft = 0;
        scrollTop = 0;
        setScrollPositions(oldScrollLeft, oldScrollTop);
    }

    protected abstract ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight);

    public static class ScrollBounds {
        private float width, height;

        protected ScrollBounds() {
            this(0, 0);
        }
        public ScrollBounds(float width0, float height0) {
            width = width0;
            height = height0;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        //increase width the given value if it's higher
        public void increaseWidthTo(float width0) {
            if (width0 > width) {
                width = width0;
            }
        }

        //increase height to the given value if it's higher
        public void increaseHeightTo(float height0) {
            if (height0 > height) {
                height = height0;
            }
        }
    }

    @Override
    public boolean fling(float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        setScrollPositions(scrollLeft - deltaX, scrollTop - deltaY);
        return true;
    }

    @Override
    public boolean panStop(float x, float y) {
        return true;
    }

    public final void draw(Graphics g) {
        g.startClip(0, 0, getWidth(), getHeight());
        super.draw(g);
        g.endClip();
    }
}
