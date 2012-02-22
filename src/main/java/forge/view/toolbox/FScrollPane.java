package forge.view.toolbox;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

/** 
 * A very basic extension of JScrollPane to centralize common styling changes.
 *
 */
@SuppressWarnings("serial")
public class FScrollPane extends JScrollPane {
    /**
     * A very basic extension of JScrollPane to centralize common styling changes.
     * This constructor assumes "as needed" for horizontal and vertical scroll policies.
     * 
     * @param c0 {@link java.awt.Component}
     */
    public FScrollPane(final Component c0) {
        super(c0, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * A very basic extension of JScrollPane to centralize common styling changes.
     * 
     * @param c0 &emsp; Viewport component.
     * @param horizontal0 &emsp; Horizontal scroll bar policy
     * @param vertical0 &emsp; Vertical scroll bar policy
     */
    public FScrollPane(final Component c0, final int horizontal0, final int vertical0) {
        super(c0, horizontal0, vertical0);
        getVerticalScrollBar().setUnitIncrement(16);
        getViewport().setOpaque(false);
        setBorder(new LineBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS), 1));
        setOpaque(false);
    }
}
