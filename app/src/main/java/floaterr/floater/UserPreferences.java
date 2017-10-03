package floaterr.floater;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String KEY = "users_settings";
    private static final String KEY_WIDTH = "key_width";
    private static final String KEY_HEIGHT = "key_height";
    private static SharedPreferences prefs = null;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        }
    }

    public static void saveWindowSize(int width, int height) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WIDTH, width);
        editor.putInt(KEY_HEIGHT, height);
        editor.apply();
    }

    public static int getWindowWidth() {
        return prefs.getInt(KEY_WIDTH, 400);
    }

    public static int getWindowHeight() {
        return prefs.getInt(KEY_HEIGHT, 400);
    }
}
