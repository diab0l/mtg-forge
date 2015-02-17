package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Rectangle;

import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.screens.FScreen;
import forge.screens.match.MatchController;
import forge.util.Utils;

public abstract class FDialog extends FOverlay {
    private static final FSkinFont TITLE_FONT = FSkinFont.get(12);
    private static final FSkinColor TITLE_BACK_COLOR = FSkinColor.get(Colors.CLR_THEME2);
    private static final FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);
    public static final float TITLE_HEIGHT = Math.round(Utils.AVG_FINGER_HEIGHT * 0.6f);
    public static final float INSETS = Utils.scale(10);
    private static int openDialogCount = 0;

    public static boolean isDialogOpen() {
        return openDialogCount > 0;
    }

    private Titlebar lblTitlebar;
    private float totalHeight;

    protected FDialog(String title) {
        lblTitlebar = add(new Titlebar(title));
    }

    @Override
    protected final void doLayout(float width, float height) {
        width -= 2 * INSETS;

        float contentHeight = layoutAndGetHeight(width, height - TITLE_HEIGHT - 2 * INSETS);
        totalHeight = contentHeight + TITLE_HEIGHT;
        float y = (height - totalHeight) / 2;

        lblTitlebar.setBounds(INSETS, y, width, TITLE_HEIGHT);

        //shift all children into position below titlebar
        float dy = lblTitlebar.getBottom();
        for (FDisplayObject child : getChildren()) {
            if (child != lblTitlebar) {
                child.setLeft(child.getLeft() + INSETS);
                child.setTop(child.getTop() + dy);
            }
        }
    }

    @Override
    public void setVisible(boolean visible0) {
        if (this.isVisible() == visible0) { return; }

        if (visible0) {
            openDialogCount++;
            MatchController.instance.cancelAwaitNextInput(); //ensure "Waiting for opponent..." prompt doesn't appear while dialog awaiting input
        }
        else if (openDialogCount > 0) {
            openDialogCount--;
        }
        super.setVisible(visible0);
    }

    protected abstract float layoutAndGetHeight(float width, float maxHeight);

    private boolean isPointWithinDialog(float x, float y) {
        Rectangle bounds = new Rectangle(lblTitlebar.getLeft(), lblTitlebar.getTop(), lblTitlebar.getWidth(), totalHeight);
        return bounds.contains(x, y);
    }

    @Override
    public boolean longPress(float x, float y) {
        return !isPointWithinDialog(x, y); //don't handle long press by default if pressed inside dialog
    }

    @Override
    protected void drawBackground(Graphics g) {
        super.drawBackground(g);

        float x = lblTitlebar.getLeft();
        float y = lblTitlebar.getTop();
        float w = lblTitlebar.getWidth();
        float h = totalHeight;
        g.drawImage(FSkinTexture.BG_TEXTURE, x, y, w, h);
        g.fillRect(FScreen.TEXTURE_OVERLAY_COLOR, x, y, w, h);

        //draw custom background behind titlebar
        g.fillRect(TITLE_BACK_COLOR, x, y, w, TITLE_HEIGHT);
    }

    @Override
    protected void drawOverlay(Graphics g) {
        super.drawOverlay(g);

        float x = lblTitlebar.getLeft();
        float y = lblTitlebar.getTop();
        float w = lblTitlebar.getWidth();
        float h = totalHeight;

        //draw border around dialog
        g.drawRect(1, BORDER_COLOR, x, y, w, h);

        //draw bottom border of titlebar
        y += TITLE_HEIGHT;
        g.drawLine(1, BORDER_COLOR, x, y, x + w, y);
    }

    private class Titlebar extends FLabel {
        private Titlebar(String title) {
            super(new FLabel.Builder().text(title)
                    .icon(FSkinImage.FAVICON.getTextureRegion() != null ? FSkinImage.FAVICON : null) //don't show icon if on SplashScreen
                    .font(TITLE_FONT).align(HAlignment.LEFT));
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY, boolean moreVertical) {
            for (FDisplayObject child : FDialog.this.getChildren()) {
                child.setLeft(child.getLeft() + deltaX);
                child.setTop(child.getTop() + deltaY);
            }
            return true;
        }

        @Override
        public boolean panStop(float x, float y) {
            //ensure titlebar in view after stopping panning so it's accessible
            float dx = 0;
            float dy = 0;
            float maxLeft = FDialog.this.getWidth() - lblTitlebar.getHeight();
            if (lblTitlebar.getLeft() > maxLeft) {
                dx = maxLeft - lblTitlebar.getLeft();
            }
            else {
                float minRight = lblTitlebar.getHeight();
                if (lblTitlebar.getRight() < minRight) {
                    dx = minRight - lblTitlebar.getRight();
                }
            }
            float maxTop = FDialog.this.getHeight() - lblTitlebar.getHeight();
            if (lblTitlebar.getTop() > maxTop) {
                dy = maxTop - lblTitlebar.getTop();
            }
            else {
                float minBottom = lblTitlebar.getHeight();
                if (lblTitlebar.getBottom() < minBottom) {
                    dy = minBottom - lblTitlebar.getBottom();
                }
            }
            if (dx != 0 || dy != 0) {
                for (FDisplayObject child : FDialog.this.getChildren()) {
                    child.setLeft(child.getLeft() + dx);
                    child.setTop(child.getTop() + dy);
                }
            }
            return true;
        }
    }
}
