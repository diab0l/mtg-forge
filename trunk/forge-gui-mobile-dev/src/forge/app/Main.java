package forge.app;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglClipboard;

import forge.Forge;

public class Main {
    public static void main(String[] args) {
        new LwjglApplication(new Forge(new LwjglClipboard(), "../forge-gui/"), "Forge", 320, 480, true);
    }
}
