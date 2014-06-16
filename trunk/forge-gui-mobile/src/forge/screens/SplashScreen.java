package forge.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Graphics;
import forge.assets.FSkinFont;
import forge.assets.FSkinTexture;
import forge.toolbox.FContainer;
import forge.toolbox.FProgressBar;

public class SplashScreen extends FContainer {
    private TextureRegion background;
    private final FProgressBar progressBar;
    private FSkinFont disclaimerFont;

    public SplashScreen() {
        progressBar = new FProgressBar();
        progressBar.setDescription("Welcome to Forge");
    }

    public FProgressBar getProgressBar() {
        return progressBar;
    }

    public void setBackground(TextureRegion background0) {
        background = background0;
    }

    @Override
    protected void doLayout(float width, float height) {
    }

    @Override
    protected void drawBackground(Graphics g) {
        if (background == null) { return; }

        g.drawImage(FSkinTexture.BG_TEXTURE, 0, 0, getWidth(), getHeight());
 
        float x, y, w, h;
        float backgroundRatio = background.getRegionWidth() / background.getRegionHeight();
        float screenRatio = getWidth() / getHeight();
        if (backgroundRatio > screenRatio) {
            x = 0;
            w = getWidth();
            h = getWidth() * backgroundRatio;
            y = (getHeight() - h) / 2;
        }
        else {
            y = 0;
            h = getHeight();
            w = getHeight() / backgroundRatio;
            x = (getWidth() - w) / 2;
        }
        g.drawImage(background, x, y, w, h);

        y += h * 295f / 450f;
        if (disclaimerFont == null) {
            disclaimerFont = FSkinFont.get(9);
        }
        float disclaimerHeight = 30f / 450f * h;
        String disclaimer = "Forge is not affiliated in any way with Wizards of the Coast.\n"
                + "Forge is open source software, released under the GNU Public License.";
        g.drawText(disclaimer, disclaimerFont, FProgressBar.SEL_FORE_COLOR,
                x, y, w, disclaimerHeight, true, HAlignment.CENTER, true);

        float padding = 20f / 450f * w;
        float pbHeight = 57f / 450f * h;
        y += 78f / 450f * h;
        progressBar.setBounds(x + padding, y, w - 2 * padding, pbHeight);
        g.draw(progressBar);
    }
}
