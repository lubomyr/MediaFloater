package floaterr.floater;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import static floaterr.floater.NameKeys.KEY_IMAGE;
import static floaterr.floater.NameKeys.KEY_VIDEO;

public class MainActivity extends Activity {
    private Uri imageUri;
    private Uri videoUri;
    private final int REQUEST_OVERLAY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                imageUri = intent.getData();
            } else if (type.startsWith("video/")) {
                videoUri = intent.getData();
            } else {
                videoUri = intent.getData();
            }
        }
        checkDrawOverlayPermission();
    }

    private void runService() {
        Intent intent = new Intent(MainActivity.this, FloatingWindow.class);
        if (imageUri != null)
            intent.putExtra(KEY_IMAGE, imageUri);
        if (videoUri != null)
            intent.putExtra(KEY_VIDEO, videoUri);
        startService(intent);
        finish();
    }

    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            } else {
                runService();
            }
        } else {
            runService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    runService();
                }
            }
        }
    }
}
