package forge.toolbox;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import forge.Forge.Graphics;

public abstract class FContainer extends FDisplayObject {
    private final ArrayList<FDisplayObject> children = new ArrayList<FDisplayObject>();

    public <T extends FDisplayObject> T add(T child) {
        children.add(child);
        return child;
    }

    public <T extends FDisplayObject> boolean remove(T child) {
        return children.remove(child);
    }

    public void clear() {
        children.clear();
    }

    public int getChildCount() {
        return children.size();
    }

    public Iterable<FDisplayObject> getChildren() {
        return children;
    }

    protected void drawBackground(Graphics g) {
    }

    public void draw(Graphics g) {
        drawBackground(g);
        for (FDisplayObject child : children) {
            if (child.isVisible()) {
                g.draw(child);
            }
        }
        drawOverlay(g);
    }

    protected void drawOverlay(Graphics g) {
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, y, width, height);
        doLayout(width, height);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        doLayout(width, height);
    }

    public void revalidate() {
        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) { return; } //don't revalidate if size not set yet
        doLayout(w, h);
    }

    protected abstract void doLayout(float width, float height);

    @Override
    public void buildTouchListeners(float screenX, float screenY, ArrayList<FDisplayObject> listeners) {
        if (isEnabled() && contains(getLeft() + screenToLocalX(screenX), getTop() + screenToLocalY(screenY))) {
            for (int i = children.size() - 1; i >= 0; i--) {
                children.get(i).buildTouchListeners(screenX, screenY, listeners);
            }
            listeners.add(this);
        }
    }

    public Vector2 getChildRelativePosition(FDisplayObject child) {
        return getChildRelativePosition(child, 0, 0);
    }

    private Vector2 getChildRelativePosition(FDisplayObject child, float offsetX, float offsetY) {
        for (FDisplayObject c : children) { //check direct children first
            if (child == c) {
                return new Vector2(c.getLeft() + offsetX, c.getTop() + offsetY);
            }
        }
        for (FDisplayObject c : children) { //check each child's children next if possible
            if (c instanceof FContainer) {
                Vector2 pos = ((FContainer)c).getChildRelativePosition(child, c.getLeft() + offsetX, c.getTop() + offsetY);
                if (pos != null) {
                    return pos;
                }
            }
        }
        return null;
    }
}
