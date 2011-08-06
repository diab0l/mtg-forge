
package forge;


import java.awt.Dimension;

import javax.swing.JEditorPane;


public class NonScrollingJEditorPane extends JEditorPane {
    private static final long serialVersionUID = 5361467616843296999L;
    
    public NonScrollingJEditorPane() {
        super();
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    
    @Override
    public void setSize(Dimension d) {
        if(d.width < getParent().getSize().width) {
            d.width = getParent().getSize().width;
        }
        super.setSize(d);
    }
}
