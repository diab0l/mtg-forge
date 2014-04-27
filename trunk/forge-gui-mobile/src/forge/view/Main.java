package forge.view;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

import forge.Forge;

public class Main extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT > 8) { //use dual-side portrait mode if supported
            this.setRequestedOrientation(7);
        }

        initialize(new Forge(getClipboard()), false);
    }
}
