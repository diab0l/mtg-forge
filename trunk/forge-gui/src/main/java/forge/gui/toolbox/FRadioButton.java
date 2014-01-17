package forge.gui.toolbox;

import forge.gui.toolbox.FSkin.SkinnedRadioButton;

/** 
 * A custom instance of JRadioButton using Forge skin properties.
 */
@SuppressWarnings("serial")
public class FRadioButton  extends SkinnedRadioButton {
    /** */
    public FRadioButton() {
        this("");
    }

    /** @param s0 &emsp; {@link java.lang.String} */
    public FRadioButton(String s0) {
        super();
        this.setText(s0);
        this.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        this.setFont(FSkin.getFont(14));
        this.setOpaque(false);
        this.setFocusable(false);
    }
}
