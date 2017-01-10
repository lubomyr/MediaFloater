package floaterr.floater;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends Activity {
    private Uri imageUri;
    private Uri videoUri;

    enum MediaType {IMAGE, VIDEO}

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
            }
        }

        runService();
        finish();
    }

    private void runService() {
        Intent intent = new Intent(MainActivity.this, FloatingWindow.class);
        if (imageUri != null)
            intent.putExtra("image", imageUri);
        if (videoUri != null)
            intent.putExtra("video", videoUri);
        startService(intent);
    }


}
