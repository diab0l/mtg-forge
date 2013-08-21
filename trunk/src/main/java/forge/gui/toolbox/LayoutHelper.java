package forge.gui.toolbox;

import javax.swing.JComponent;

/** 
 * Helper class for doing custom layout
 *
 */
public final class LayoutHelper {
    private final int parentWidth, parentHeight;
    private int x, y, lineBottom;
    
    public LayoutHelper(JComponent parent) {
        parentWidth = parent.getWidth();
        parentHeight = parent.getHeight();
    }
    
    /**
     * Layout component to fill remaining space of parent
     * @param comp
     */
    public void fill(final JComponent comp) {
        if (x >= parentWidth) {
            newLine();
        }
        include(comp, parentWidth - x, parentHeight - y);
    }
    
    /**
     * Layout component to fill remaining space of current line
     * @param comp
     * @param height
     */
    public void fillLine(final JComponent comp, int height) {
        if (x >= parentWidth) {
            newLine();
        }
        include(comp, parentWidth - x, height);
    }
    
    /**
     * Include component in layout with a percentage width and fixed height
     * @param comp
     * @param widthPercent
     * @param height
     */
    public void include(final JComponent comp, float widthPercent, int height) {
        include(comp, Math.round(parentWidth * widthPercent), height);
    }
    
    /**
     * Include component in layout with a fixed width and percentage height
     * @param comp
     * @param width
     * @param heightPercent
     */
    public void include(final JComponent comp, int width, float heightPercent) {
        include(comp, width, Math.round(parentHeight * heightPercent));
    }
    
    /**
     * Include component in layout with a percentage width and height
     * @param comp
     * @param widthPercent
     * @param heightPercent
     */
    public void include(final JComponent comp, float widthPercent, float heightPercent) {
        include(comp, Math.round(parentWidth * widthPercent), Math.round(parentHeight * heightPercent));
    }
    
    /**
     * Include component in layout with a fixed width and height
     * @param comp
     * @param width
     * @param height
     */
    public void include(final JComponent comp, int width, int height) {
        if (width <= 0 || height <= 0) { return; }
        
        if (x + width > parentWidth) {
            newLine();
            if (width > parentWidth) {
                width = parentWidth;
            }
        }
        if (y + height > parentHeight) {
            y = parentHeight - height;
            if (y >= parentHeight) { return; }
        }
        comp.setBounds(x, y, width, height);
        x += width + 3;
        if (y + height > lineBottom) {
            lineBottom = y + height;
        }
    }
    
    /**
     * Offset current layout helper position
     * @param dx
     * @param dy
     */
    public void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }
    
    /**
     * Start new line of layout
     */    
    public void newLine() {
        if (lineBottom == y) { return; }
        x = 0;
        y = lineBottom + 3;
        lineBottom = y;
    }
}
