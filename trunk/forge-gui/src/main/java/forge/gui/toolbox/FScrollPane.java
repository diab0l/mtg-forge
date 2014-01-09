package forge.gui.toolbox;

import java.awt.Component;

import javax.swing.ScrollPaneConstants;

import forge.gui.toolbox.FSkin.SkinnedScrollPane;

/** 
 * A very basic extension of JScrollPane to centralize common styling changes.
 *
 */
@SuppressWarnings("serial")
public class FScrollPane extends SkinnedScrollPane {
    /**
     * A very basic extension of JScrollPane to centralize common styling changes.
     * This constructor assumes "as needed" for horizontal and vertical scroll policies.
     * 
     * @param c0 {@link java.awt.Component}
     */
    public FScrollPane(final Component c0) {
        this(c0, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * A very basic extension of JScrollPane to centralize common styling changes.
     * 
     * @param c0 &emsp; Viewport component.
     * @param vertical0 &emsp; Vertical scroll bar policy
     * @param horizontal0 &emsp; Horizontal scroll bar policy
     */
    public FScrollPane(final Component c0, final int vertical0, final int horizontal0) {
        super(c0, vertical0, horizontal0);
        getVerticalScrollBar().setUnitIncrement(16);
        getViewport().setOpaque(false);
        this.setBorder(new FSkin.LineSkinBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        setOpaque(false);
    }
}
