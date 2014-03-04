package forge;

import java.util.ArrayList;
import java.util.Stack;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import forge.assets.FSkin;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FImage;
import forge.screens.FScreen;
import forge.screens.SplashScreen;
import forge.screens.home.HomeScreen;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FProgressBar;
import forge.utils.Constants;

public class Forge implements ApplicationListener {
    private static Forge game;
    private static int screenWidth;
    private static int screenHeight;
    private static SpriteBatch batch;
    private static ShapeRenderer shapeRenderer;
    private static FScreen currentScreen;
    private static StaticData magicDb;
    private static SplashScreen splashScreen;
    private static final Stack<FScreen> screens = new Stack<FScreen>();

    public Forge() {
        if (game != null) {
            throw new RuntimeException("Cannot initialize Forge more than once");
        }
        game = this;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        splashScreen = new SplashScreen();
        FSkin.loadLight("journeyman", splashScreen);

        // Loads card database on background thread (using progress bar to report progress)
        new Thread(new Runnable() {
            @Override
            public void run() {
                final FProgressBar bar = splashScreen.getProgressBar();
                final CardStorageReader.ProgressObserver progressBarBridge = new CardStorageReader.ProgressObserver() {
                    @Override
                    public void setOperationName(final String name, final boolean usePercents) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                bar.setDescription(name);
                                bar.setPercentMode(usePercents);
                            }
                        });
                    }

                    @Override
                    public void report(final int current, final int total) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                bar.setMaximum(total);
                                bar.setValue(current);
                            }
                        });
                    }
                };
                final CardStorageReader reader = new CardStorageReader(Constants.CARD_DATA_DIR, progressBarBridge, null);
                magicDb = new StaticData(reader, Constants.EDITIONS_DIR, Constants.BLOCK_DATA_DIR);
                
                bar.setDescription("Opening main window...");

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        afterDbLoaded();
                    }
                });
            }
        }).start();
    }

    private void afterDbLoaded() {
        Gdx.graphics.setContinuousRendering(false); //save power consumption by disabling continuous rendering once assets loaded

        FSkin.loadFull(splashScreen);

        Gdx.input.setInputProcessor(new FGestureDetector());
        openScreen(new HomeScreen());
        splashScreen = null;
    }

    public static void showMenu() {
        if (currentScreen == null) { return; }
        currentScreen.showMenu();
    }

    public static void back() {
        if (screens.size() < 2) { return; } //don't allow going back from initial screen
        screens.pop();
        setCurrentScreen(screens.lastElement());
    }

    public static void openScreen(FScreen screen0) {
        if (currentScreen == screen0) { return; }
        screens.push(screen0);
        setCurrentScreen(screen0);
        screen0.onOpen();
    }

    private static void setCurrentScreen(FScreen screen0) {
        currentScreen = screen0;
        currentScreen.setSize(screenWidth, screenHeight);
    }

    public static StaticData getMagicDb() {
        return magicDb;
    }

    @Override
    public void render () {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // Clear the screen.

        FContainer screen = currentScreen;
        if (screen == null) {
            screen = splashScreen;
            if (screen == null) { 
                return;
            }
        }

        batch.begin();
        Graphics g = new Graphics();
        screen.draw(g);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        if (currentScreen != null) {
            currentScreen.setSize(width, height);
        }
        else if (splashScreen != null) {
            splashScreen.setSize(width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose () {
        currentScreen = null;
        screens.clear();
        batch.dispose();
        shapeRenderer.dispose();
    }

    private static class FGestureDetector extends GestureDetector {
        private static final ArrayList<FDisplayObject> potentialListeners = new ArrayList<FDisplayObject>();

        @Override
        public boolean touchUp(float x, float y, int pointer, int button) {
            for (FDisplayObject listener : potentialListeners) {
                if (listener.touchUp(x, y)) {
                    break;
                }
            }
            return super.touchUp(x, y, pointer, button);
        }

        private FGestureDetector() {
            super(new GestureListener() {
                @Override
                public boolean touchDown(float x, float y, int pointer, int button) {
                    potentialListeners.clear();
                    if (currentScreen != null) { //base potential listeners on object containing touch down point
                        currentScreen.buildTouchListeners(x, y, potentialListeners);
                    }
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.touchDown(x, y)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean tap(float x, float y, int count, int button) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.tap(x, y, count)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean longPress(float x, float y) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.longPress(x, y)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean fling(float velocityX, float velocityY, int button) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.fling(velocityX, velocityY)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean pan(float x, float y, float deltaX, float deltaY) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.pan(x, y, deltaX, deltaY)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean panStop(float x, float y, int pointer, int button) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.panStop(x, y)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean zoom(float initialDistance, float distance) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.zoom(initialDistance, distance)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                    for (FDisplayObject listener : potentialListeners) {
                        if (listener.pinch(initialPointer1, initialPointer2, pointer1, pointer2)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public static class Graphics {
        private Rectangle bounds;
        private int failedClipCount;

        private Graphics() {
            bounds = new Rectangle(0, 0, screenWidth, screenHeight);
        }

        public void startClip() {
            startClip(0, 0, bounds.width, bounds.height);
        }
        public void startClip(float x, float y, float w, float h) {
            batch.flush(); //must flush batch to prevent other things not rendering
            if (!ScissorStack.pushScissors(new Rectangle(adjustX(x), adjustY(y, h), w, h))) {
                failedClipCount++; //tracked failed clips to prevent calling popScissors on endClip
            }
        }
        public void endClip() {
            if (failedClipCount == 0) {
                batch.flush(); //must flush batch to ensure stuffed rendered during clip respects that clip
                ScissorStack.popScissors();
            }
            else {
                failedClipCount--;
            }
        }

        public void draw(FDisplayObject displayObj) {
            if (displayObj.getWidth() <= 0 || displayObj.getHeight() <= 0) {
                return;
            }

            final Rectangle parentBounds = bounds;
            bounds = new Rectangle(parentBounds.x + displayObj.getLeft(), parentBounds.y + displayObj.getTop(), displayObj.getWidth(), displayObj.getHeight());

            if (bounds.overlaps(parentBounds)) { //avoid drawing object if it's not within visible region
                displayObj.draw(this);
            }

            bounds = parentBounds;
        }

        public void drawLine(FSkinColor skinColor, float x1, float y1, float x2, float y2) {
            drawLine(skinColor.getColor(), x1, y1, x2, y2);
        }
        public void drawLine(Color color, float x1, float y1, float x2, float y2) {
            batch.end(); //must pause batch while rendering shapes

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(color);
            shapeRenderer.line(adjustX(x1), adjustY(y1, 0), adjustX(x2), adjustY(y2, 0));
            shapeRenderer.end();

            batch.begin();
        }

        public void drawRect(FSkinColor skinColor, float x, float y, float w, float h) {
            drawRect(skinColor.getColor(), x, y, w, h);
        }
        public void drawRect(Color color, float x, float y, float w, float h) {
            batch.end(); //must pause batch while rendering shapes

            //adjust width/height so rectangle covers equivalent filled area
            w = Math.round(w - 1);
            h = Math.round(h - 1);

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(color);

            //must user 4 line() calls rather than rect() since rect() leaves corner unfilled
            x = adjustX(x);
            float y2 = adjustY(y, h);
            float x2 = x + w;
            y = y2 + h;
            shapeRenderer.line(x, y, x, y2);
            shapeRenderer.line(x, y2, x2 + 1, y2); //+1 prevents corner not being filled
            shapeRenderer.line(x2, y2, x2, y);
            shapeRenderer.line(x2 + 1, y, x, y); //+1 prevents corner not being filled

            shapeRenderer.end();
            batch.begin();
        }

        public void fillRect(FSkinColor skinColor, float x, float y, float w, float h) {
            fillRect(skinColor.getColor(), x, y, w, h);
        }
        public void fillRect(Color color, float x, float y, float w, float h) {
            batch.end(); //must pause batch while rendering shapes

            boolean needBlending = (color.a != 0);
            if (needBlending) { //enable blending so alpha colored shapes work properly
                Gdx.gl.glEnable(GL20.GL_BLEND);
            }

            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(adjustX(x), adjustY(y, h), w, h);
            shapeRenderer.end();

            if (needBlending) {
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }

            batch.begin();
        }

        public void fillTriangle(FSkinColor skinColor, float x1, float y1, float x2, float y2, float x3, float y3) {
            fillTriangle(skinColor.getColor(), x1, y1, x2, y2, x3, y3);
        }
        public void fillTriangle(Color color, float x1, float y1, float x2, float y2, float x3, float y3) {
            batch.end(); //must pause batch while rendering shapes

            boolean needBlending = (color.a != 0);
            if (needBlending) { //enable blending so alpha colored shapes work properly
                Gdx.gl.glEnable(GL20.GL_BLEND);
            }

            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(color);
            shapeRenderer.triangle(adjustX(x1), adjustY(y1, 0), adjustX(x2), adjustY(y2, 0), adjustX(x3), adjustY(y3, 0));
            shapeRenderer.end();

            if (needBlending) {
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }

            batch.begin();
        }

        public void fillGradientRect(FSkinColor skinColor1, FSkinColor skinColor2, boolean vertical, float x, float y, float w, float h) {
            fillGradientRect(skinColor1.getColor(), skinColor2.getColor(), vertical, x, y, w, h);
        }
        public void fillGradientRect(FSkinColor skinColor1, Color color2, boolean vertical, float x, float y, float w, float h) {
            fillGradientRect(skinColor1.getColor(), color2, vertical, x, y, w, h);
        }
        public void fillGradientRect(Color color1, FSkinColor skinColor2, boolean vertical, float x, float y, float w, float h) {
            fillGradientRect(color1, skinColor2.getColor(), vertical, x, y, w, h);
        }
        public void fillGradientRect(Color color1, Color color2, boolean vertical, float x, float y, float w, float h) {
            batch.end(); //must pause batch while rendering shapes

            boolean needBlending = (color1.a != 0 || color2.a != 0);
            if (needBlending) { //enable blending so alpha colored shapes work properly
                Gdx.gl.glEnable(GL20.GL_BLEND);
            }

            Color topLeftColor = color1;
            Color topRightColor = vertical ? color1 : color2;
            Color bottomLeftColor = vertical ? color2 : color1;
            Color bottomRightColor = color2;

            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.rect(adjustX(x), adjustY(y, h), w, h, bottomLeftColor, bottomRightColor, topRightColor, topLeftColor);
            shapeRenderer.end();

            if (needBlending) {
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }

            batch.begin();
        }

        public void drawImage(FImage image, float x, float y, float w, float h) {
            image.draw(this, x, y, w, h);
        }
        public void drawImage(Texture image, float x, float y, float w, float h) {
            batch.draw(image, adjustX(x), adjustY(y, h), w, h);
        }
        public void drawImage(TextureRegion image, float x, float y, float w, float h) {
            batch.draw(image, adjustX(x), adjustY(y, h), w, h);
        }

        public void drawText(String text, FSkinFont skinFont, FSkinColor skinColor, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
            drawText(text, skinFont.getFont(), skinColor.getColor(), x, y, w, h, wrap, horzAlignment, centerVertically);
        }
        public void drawText(String text, FSkinFont skinFont, Color color, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
            drawText(text, skinFont.getFont(), color, x, y, w, h, wrap, horzAlignment, centerVertically);
        }
        public void drawText(String text, BitmapFont font, Color color, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
            font.setColor(color);
            if (wrap) {
                float textHeight = font.getWrappedBounds(text, w).height;
                if (h > textHeight && centerVertically) {
                    y += (h - textHeight) / 2;
                }
                font.drawWrapped(batch, text, adjustX(x), adjustY(y, 0), w, horzAlignment);
            }
            else {
                float textHeight = font.getMultiLineBounds(text).height;
                if (h > textHeight && centerVertically) {
                    y += (h - textHeight) / 2;
                }
                font.drawMultiLine(batch, text, adjustX(x), adjustY(y, 0), w, horzAlignment);
            }
        }

        private float adjustX(float x) {
            return x + bounds.x;
        }

        private float adjustY(float y, float height) {
            return screenHeight - y - bounds.y - height; //flip y-axis
        }
    }
}
