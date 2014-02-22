package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Vector2;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;

public class FLabel extends FDisplayObject {
	public static class Builder {
        //========== Default values for FLabel are set here.
        private double     bldIconScaleFactor  = 0.8;
        private int        bldFontSize         = 14;
        private HAlignment bldFontAlign        = HAlignment.LEFT;
        private HAlignment bldIconAlignX       = HAlignment.LEFT;
        private Vector2    bldIconInsets       = new Vector2(0, 0);

        private boolean bldSelectable       = false;
        private boolean bldSelected         = false;
        private boolean bldOpaque           = false;
        private boolean bldIconInBackground = false;
        private boolean bldIconScaleAuto    = true;
        private boolean bldEnabled          = true;

        private String bldText;
        private FSkinImage bldIcon;
        private Runnable bldCommand;

        public FLabel build() { return new FLabel(this); }

        // Begin builder methods.
        public Builder text(final String s0) { this.bldText = s0; return this; }
        public Builder icon(final FSkinImage i0) { this.bldIcon = i0; return this; }
        public Builder fontAlign(final HAlignment a0) { this.bldFontAlign = a0; return this; }
        public Builder opaque(final boolean b0) { this.bldOpaque = b0; return this; }
        public Builder opaque() { opaque(true); return this; }
        public Builder selectable(final boolean b0) { this.bldSelectable = b0; return this; }
        public Builder selectable() { selectable(true); return this; }
        public Builder selected(final boolean b0) { this.bldSelected = b0; return this; }
        public Builder selected() { selected(true); return this; }
        public Builder command(final Runnable c0) { this.bldCommand = c0; return this; }
        public Builder fontSize(final int i0) { this.bldFontSize = i0; return this; }
        public Builder enabled(final boolean b0) { this.bldEnabled = b0; return this; }
        public Builder iconScaleAuto(final boolean b0) { this.bldIconScaleAuto = b0; return this; }
        public Builder iconScaleFactor(final double d0) { this.bldIconScaleFactor = d0; return this; }
        public Builder iconInBackground(final boolean b0) { this.bldIconInBackground = b0; return this; }
        public Builder iconInBackground() { iconInBackground(true); return this; }
        public Builder iconAlignX(final HAlignment a0) { this.bldIconAlignX = a0; return this; }
        public Builder iconInsets(final Vector2 v0) { this.bldIconInsets = v0; return this; }
    }

    // sets better defaults for button labels
    public static class ButtonBuilder extends Builder {
        public ButtonBuilder() {
            opaque();
        }
    }

    private static final FSkinColor clrText = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor clrMain = FSkinColor.get(Colors.CLR_INACTIVE);
    private static final FSkinColor d50 = clrMain.stepColor(-50);
    private static final FSkinColor d30 = clrMain.stepColor(-30);
    private static final FSkinColor d10 = clrMain.stepColor(-10);
    private static final FSkinColor l10 = clrMain.stepColor(10);
    private static final FSkinColor l20 = clrMain.stepColor(20);
    private static final FSkinColor l30 = clrMain.stepColor(30);

    private double iconScaleFactor;
    private FSkinFont font;
    private HAlignment fontAlign, iconAlignX;
    private Vector2 iconInsets;
    private boolean selectable, selected, opaque, iconInBackground, iconScaleAuto, enabled;

    private String text;
    private FSkinImage icon;
    private Runnable command;

    // Call this using FLabel.Builder()...
    protected FLabel(final Builder b0) {
    	iconScaleFactor = b0.bldIconScaleFactor;
    	font = FSkinFont.get(b0.bldFontSize);
    	fontAlign = b0.bldFontAlign;
    	iconAlignX = b0.bldIconAlignX;
    	iconInsets = b0.bldIconInsets;
    	selectable = b0.bldSelectable;
    	selected = b0.bldSelected;
    	opaque = b0.bldOpaque;
    	iconInBackground = b0.bldIconInBackground;
    	iconScaleAuto = b0.bldIconScaleAuto;
    	setEnabled(b0.bldEnabled);
    }

    public boolean getSelected() {
        return this.selected;
    }
    public void setSelected(final boolean b0) {
        this.selected = b0;
    }

    public boolean tap(float x, float y, int count) {
    	boolean handled = false;
    	if (selectable) {
    		setSelected(!selected);
    		handled = true;
    	}
        if (command != null) {
        	command.run();
    		handled = true;
        }
    	return handled;
    }

	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub
		
	}
}
