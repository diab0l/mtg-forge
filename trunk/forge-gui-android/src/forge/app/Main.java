package forge.app;

import java.io.File;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

import forge.Forge;
import forge.interfaces.IDeviceAdapter;
import forge.util.FileUtil;

public class Main extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup portrait orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT > 8) { //use dual-side portrait mode if supported
            this.setRequestedOrientation(7);
        }

        AndroidAdapter adapter = new AndroidAdapter();

        //establish assets directory
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Gdx.app.error("Forge", "Can't access external storage");
            adapter.exit();
            return;
        }
        String assetsDir = Environment.getExternalStorageDirectory() + "/Forge/";
        if (!FileUtil.ensureDirectoryExists(assetsDir)) {
            Gdx.app.error("Forge", "Can't access external storage");
            adapter.exit();
            return;
        }

        initialize(Forge.getApp(new AndroidClipboard(), adapter, assetsDir));
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();

        //ensure app doesn't stick around
        //ActivityManager am = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
        //am.killBackgroundProcesses(getApplicationContext().getPackageName());

        
    }*/

    //special clipboard that words on Android
    private class AndroidClipboard implements com.badlogic.gdx.utils.Clipboard {
        private final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        @Override
        public String getContents() {
            if (cm.getPrimaryClip().getItemCount() > 0) {
                try {
                    return cm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return "";
        }

        @Override
        public void setContents(String contents0) {
            cm.setPrimaryClip(ClipData.newPlainText("Forge", contents0));
        }
    }

    private class AndroidAdapter implements IDeviceAdapter {
        private final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        @Override
        public boolean isConnectedToInternet() {
            NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        public boolean isConnectedToWifi() {
            NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifi.isConnected();
        }

        @Override
        public String getDownloadsDir() {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        }

        @Override
        public boolean openFile(String filename) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //ensure this task isn't linked to this application
                Uri uri = Uri.fromFile(new File(filename));
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                intent.setDataAndType(uri, type);
                startActivity(intent);
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void exit() {
            // Replace the current task with one that is excluded from the recent
            // apps and that will finish itself immediately. It's critical that this
            // activity get launched in the task that you want to hide.
            final Intent relaunch = new Intent(Main.this, Exiter.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK // CLEAR_TASK requires this
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK // finish everything else in the task
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // hide (remove, in this case) task from recents
            startActivity(relaunch);
        }
    }
}
