package top.scraft.picman2.utils;

import android.app.Activity;
import android.widget.Toast;

import okhttp3.MediaType;

public class Utils {

    public static MediaType mediaType(String pid) {
        MediaType mediaType;
        String pidLower = pid.toLowerCase();
        if (pidLower.endsWith(".gif")) {
            mediaType = MediaType.parse("image/gif");
        } else if (pidLower.endsWith(".png")) {
            mediaType = MediaType.parse("image/png");
        } else {
            mediaType = MediaType.parse("image/jpeg");
        }
        return mediaType;
    }

    public static void toastThread(Activity activity, String message) {
        activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
    }

}
