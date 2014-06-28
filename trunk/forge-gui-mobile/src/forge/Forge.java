package forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Clipboard;

import forge.assets.AssetsDownloader;
import forge.assets.FSkin;
import forge.assets.FSkinFont;
import forge.error.BugReporter;
import forge.error.ExceptionHandler;
import forge.model.FModel;
import forge.properties.ForgeConstants;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.screens.FScreen;
import forge.screens.SplashScreen;
import forge.screens.home.HomeScreen;
import forge.screens.match.FControl;
import forge.sound.MusicPlayer;
import forge.sound.SoundSystem;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FGestureAdapter;
import forge.toolbox.FOptionPane;
import forge.toolbox.FOverlay;
import forge.util.Callback;
import forge.util.FileUtil;
import forge.util.Utils;

public class Forge implements ApplicationListener {
    public static final String CURRENT_VERSION = "1.5.21.009";

    private static final ApplicationListener app = new Forge();
    private static Clipboard clipboard;
    private static Runnable onExit;
    private static int screenWidth;
    private static int screenHeight;
    private static Graphics graphics;
    private static FScreen currentScreen;
    private static SplashScreen splashScreen;
    private static KeyInputAdapter keyInputAdapter;
    private static MusicPlayer musicPlayer;
    private static final SoundSystem soundSystem = new SoundSystem();
    private static final Stack<FScreen> screens = new Stack<FScreen>();

    public static ApplicationListener getApp(Clipboard clipboard0, String assetDir0, Runnable onExit0) {
        if (GuiBase.getInterface() == null) {
            clipboard = clipboard0;
            onExit = onExit0;
            GuiBase.setInterface(new GuiMobile(assetDir0));
        }
        return app;
    }

    private Forge() {
    }

    @Override
    public void create() {
        //install our error handler
        ExceptionHandler.registerErrorHandling();

        graphics = new Graphics();
        splashScreen = new SplashScreen();
        musicPlayer = new MusicPlayer(ForgeConstants.MUSIC_DIR + "menus");

        String skinName;
        if (FileUtil.doesFileExist(ForgeConstants.MAIN_PREFS_FILE)) {
            skinName = new ForgePreferences().getPref(FPref.UI_SKIN);
        }
        else {
            skinName = "default"; //use default skin if preferences file doesn't exist yet
        }
        FSkin.loadLight(skinName, splashScreen);

        //load model on background thread (using progress bar to report progress)
        FThreads.invokeInBackgroundThread(new Runnable() {
            @Override
            public void run() {
                //see if app or assets need updating
                AssetsDownloader.checkForUpdates(splashScreen);

                FModel.initialize(splashScreen.getProgressBar());

                splashScreen.getProgressBar().setDescription("Loading fonts...");
                FSkinFont.preloadAll();

                splashScreen.getProgressBar().setDescription("Finishing startup...");

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        afterDbLoaded();
                    }
                });
            }
        });
    }

    private void afterDbLoaded() {
        Gdx.graphics.setContinuousRendering(false); //save power consumption by disabling continuous rendering once assets loaded

        FSkin.loadFull(splashScreen);

        musicPlayer.play(); //start background music

        Gdx.input.setInputProcessor(new MainInputProcessor());
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
        openScreen(new HomeScreen());
        splashScreen = null;
    }

    public static Clipboard getClipboard() {
        return clipboard;
    }

    public static void showMenu() {
        if (currentScreen == null) { return; }
        endKeyInput(); //end key input before menu shown
        if (FOverlay.getTopOverlay() == null) { //don't show menu if overlay open
            currentScreen.showMenu();
        }
    }

    public static void back() {
        if (screens.size() < 2) {
            exit(); //prompt to exit if attempting to go back from home screen
            return;
        }
        currentScreen.onClose(new Callback<Boolean>() {
            @Override
            public void run(Boolean result) {
                if (result) {
                    screens.pop();
                    setCurrentScreen(screens.lastElement());
                }
            }
        });
    }

    public static void exit() {
        FOptionPane.showConfirmDialog("Are you sure you wish to exit Forge?", "Exit Forge", "Exit", "Cancel", new Callback<Boolean>() {
            @Override
            public void run(Boolean result) {
                if (result) {
                    Gdx.app.exit();
                }
            }
        });
    }

    public static void openScreen(final FScreen screen0) {
        if (currentScreen == screen0) { return; }

        if (currentScreen == null) {
            screens.push(screen0);
            setCurrentScreen(screen0);
            return;
        }

        currentScreen.onSwitchAway(new Callback<Boolean>() {
            @Override
            public void run(Boolean result) {
                if (result) {
                    screens.push(screen0);
                    setCurrentScreen(screen0);
                }
            }
        });
    }

    public static FScreen getCurrentScreen() {
        return currentScreen;
    }

    private static void setCurrentScreen(FScreen screen0) {
        try {
            endKeyInput(); //end key input before switching screens
            Animation.endAll(); //end all active animations before switching screens
    
            currentScreen = screen0;
            currentScreen.setSize(screenWidth, screenHeight);
            currentScreen.onActivate();
        }
        catch (Exception ex) {
            graphics.end();
            BugReporter.reportException(ex);
        }
    }

    public static SoundSystem getSoundSystem() {
        return soundSystem;
    }

    public static MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    @Override
    public void render() {
        try {
            Animation.advanceAll();
    
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen.

            FContainer screen = currentScreen;
            if (screen == null) {
                screen = splashScreen;
                if (screen == null) { 
                    return;
                }
            }

            graphics.begin(screenWidth, screenHeight);
            screen.draw(graphics);
            for (FOverlay overlay : FOverlay.getOverlays()) {
                overlay.setSize(screenWidth, screenHeight); //update overlay sizes as they're rendered
                overlay.draw(graphics);
            }
            graphics.end();
        }
        catch (Exception ex) {
            graphics.end();
            BugReporter.reportException(ex);
        }
    }

    @Override
    public void resize(int width, int height) {
        try {
            screenWidth = width;
            screenHeight = height;
            if (currentScreen != null) {
                currentScreen.setSize(width, height);
            }
            else if (splashScreen != null) {
                splashScreen.setSize(width, height);
            }
        }
        catch (Exception ex) {
            graphics.end();
            BugReporter.reportException(ex);
        }
    }

    @Override
    public void pause() {
        FControl.pause();
    }

    @Override
    public void resume() {
        FControl.resume();
    }

    @Override
    public void dispose() {
        if (currentScreen != null) {
            FOverlay overlay = FOverlay.getTopOverlay();
            while (overlay != null) {
                overlay.hide();
                overlay = FOverlay.getTopOverlay();
            }
            currentScreen.onClose(null);
            currentScreen = null;
        }
        screens.clear();
        graphics.dispose();
        musicPlayer.dispose();
        FControl.dispose();

        if (onExit != null) {
            onExit.run();
        }
    }

    //log message to Forge.log file
    public static void log(Object message) {
        System.out.println(message);
    }

    public static void startKeyInput(KeyInputAdapter adapter) {
        if (keyInputAdapter == adapter) { return; }
        if (keyInputAdapter != null) {
            keyInputAdapter.onInputEnd(); //make sure previous adapter is ended
        }
        keyInputAdapter = adapter;
        Gdx.input.setOnscreenKeyboardVisible(true);
    }

    public static boolean endKeyInput() {
        if (keyInputAdapter == null) { return false; }
        keyInputAdapter.onInputEnd();
        keyInputAdapter = null;
        MainInputProcessor.keyTyped = false;
        MainInputProcessor.lastKeyTyped = '\0';
        Gdx.input.setOnscreenKeyboardVisible(false);
        return true;
    }

    public static abstract class KeyInputAdapter {
        public abstract FDisplayObject getOwner();
        public abstract boolean allowTouchInput();
        public abstract boolean keyTyped(char ch);
        public abstract boolean keyDown(int keyCode);
        public abstract void onInputEnd();

        //also allow handling of keyUp but don't require it
        public boolean keyUp(int keyCode) { return false; }

        public static boolean isCtrlKeyDown() {
            return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
        }
        public static boolean isShiftKeyDown() {
            return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
        }
        public static boolean isAltKeyDown() {
            return Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
        }
        public static boolean isModifierKey(int keyCode) {
            switch (keyCode) {
            case Keys.CONTROL_LEFT:
            case Keys.CONTROL_RIGHT:
            case Keys.SHIFT_LEFT:
            case Keys.SHIFT_RIGHT:
            case Keys.ALT_LEFT:
            case Keys.ALT_RIGHT:
                return true;
            }
            return false;
        }
    }

    private static class MainInputProcessor extends FGestureAdapter {
        private static final ArrayList<FDisplayObject> potentialListeners = new ArrayList<FDisplayObject>();
        private static char lastKeyTyped;
        private static boolean keyTyped;

        @Override
        public boolean keyDown(int keyCode) {
            if (keyCode == Keys.MENU) {
                showMenu();
                return true;
            }
            if (keyInputAdapter == null) {
                if (KeyInputAdapter.isModifierKey(keyCode)) {
                    return false; //don't process modifiers keys for unknown adapter
                }
                //if no active key input adapter, give current screen or overlay a chance to handle key
                FContainer container = FOverlay.getTopOverlay();
                if (container == null) {
                    container = currentScreen;
                    if (container == null) {
                        return false;
                    }
                }
                return container.keyDown(keyCode);
            }
            return keyInputAdapter.keyDown(keyCode);
        }

        @Override
        public boolean keyUp(int keyCode) {
            keyTyped = false; //reset on keyUp
            if (keyInputAdapter != null) {
                return keyInputAdapter.keyUp(keyCode);
            }
            return false;
        }

        @Override
        public boolean keyTyped(char ch) {
            if (keyInputAdapter != null) {
                if (ch >= ' ' && ch <= '~') { //only process this event if character is printable
                    //prevent firing this event more than once for the same character on the same key down, otherwise it fires too often
                    if (lastKeyTyped != ch || !keyTyped) {
                        keyTyped = true;
                        lastKeyTyped = ch;
                        return keyInputAdapter.keyTyped(ch);
                    }
                }
            }
            return false;
        }

        private void updatePotentialListeners(int x, int y) {
            potentialListeners.clear();
            if (currentScreen != null) { //base potential listeners on object containing touch down point
                FOverlay overlay = FOverlay.getTopOverlay();
                if (overlay != null) { //let top overlay handle gestures if any is open
                    overlay.buildTouchListeners(x, y, potentialListeners);
                }
                else {
                    currentScreen.buildTouchListeners(x, y, potentialListeners);
                }
            }
        }

        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            updatePotentialListeners(x, y);
            if (keyInputAdapter != null) {
                if (!keyInputAdapter.allowTouchInput() || !potentialListeners.contains(keyInputAdapter.getOwner())) {
                    endKeyInput(); //end key input and suppress touch event if needed
                    potentialListeners.clear();
                }
            }
            return super.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean press(float x, float y) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.press(listener.screenToLocalX(x), listener.screenToLocalY(y))) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean release(float x, float y) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.release(listener.screenToLocalX(x), listener.screenToLocalY(y))) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean longPress(float x, float y) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.longPress(listener.screenToLocalX(x), listener.screenToLocalY(y))) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean tap(float x, float y, int count) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.tap(listener.screenToLocalX(x), listener.screenToLocalY(y), count)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean fling(float velocityX, float velocityY) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.fling(velocityX, velocityY)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY, boolean moreVertical) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.pan(listener.screenToLocalX(x), listener.screenToLocalY(y), deltaX, deltaY, moreVertical)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean panStop(float x, float y) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.panStop(listener.screenToLocalX(x), listener.screenToLocalY(y))) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        @Override
        public boolean zoom(float x, float y, float amount) {
            try {
                for (FDisplayObject listener : potentialListeners) {
                    if (listener.zoom(listener.screenToLocalX(x), listener.screenToLocalY(y), amount)) {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) {
                BugReporter.reportException(ex);
                return true;
            }
        }

        //mouseMoved and scrolled events for desktop version
        private int mouseMovedX, mouseMovedY;

        @Override
        public boolean mouseMoved(int x, int y) {
            mouseMovedX = x;
            mouseMovedY = y;
            return true;
        }

        @Override
        public boolean scrolled(int amount) {
            updatePotentialListeners(mouseMovedX, mouseMovedY);

            if (KeyInputAdapter.isCtrlKeyDown()) { //zoom in or out based on amount
                return zoom(mouseMovedX, mouseMovedY, -Utils.AVG_FINGER_WIDTH * amount);
            }

            boolean handled;
            if (KeyInputAdapter.isShiftKeyDown()) {
                handled = pan(mouseMovedX, mouseMovedY, -Utils.AVG_FINGER_WIDTH * amount, 0, false);
            }
            else {
                handled = pan(mouseMovedX, mouseMovedY, 0, -Utils.AVG_FINGER_HEIGHT * amount, true);
            }
            if (panStop(mouseMovedX, mouseMovedY)) {
                handled = true;
            }
            return handled;
        }
    }

    public static abstract class Animation {
        private static final List<Animation> activeAnimations = new ArrayList<Animation>();

        public void start() {
            if (activeAnimations.contains(this)) { return; } //prevent starting the same animation multiple times

            activeAnimations.add(this);
            if (activeAnimations.size() == 1) { //if first animation being started, ensure continuous rendering turned on
                Gdx.graphics.setContinuousRendering(true);
            }
        }

        private static void advanceAll() {
            if (activeAnimations.isEmpty()) { return; }

            float dt = Gdx.graphics.getDeltaTime();
            for (int i = 0; i < activeAnimations.size(); i++) {
                if (!activeAnimations.get(i).advance(dt)) {
                    activeAnimations.remove(i);
                    i--;
                }
            }

            if (activeAnimations.isEmpty()) { //when all animations have ended, turn continuous rendering back off
                Gdx.graphics.setContinuousRendering(false);
            }
        }

        private static void endAll() {
            if (activeAnimations.isEmpty()) { return; }

            activeAnimations.clear();
            Gdx.graphics.setContinuousRendering(false);
        }

        //return true if animation should continue, false to stop the animation
        protected abstract boolean advance(float dt);
    }
}
