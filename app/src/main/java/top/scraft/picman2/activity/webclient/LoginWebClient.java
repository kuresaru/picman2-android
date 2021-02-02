package top.scraft.picman2.activity.webclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.scraft.picman2.activity.BrowserActivity;

public class LoginWebClient extends PicmanWebClient {

    private final Pattern patternPmst = Pattern.compile("PMST=([a-zA-Z0-9/+]+={0,2})");

    public LoginWebClient(BrowserActivity activity) {
        super(activity);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
//            Log.d("picman_webview_login", "cookie: " + cookie);
            if (url.matches("^https?://([^/]+)/#/$")) {
                Matcher pmst = patternPmst.matcher(cookie);
                Intent result = new Intent();
                if (pmst.find()) {
                    result.putExtra("HOST", Uri.parse(url).getHost());
                    result.putExtra("PMST", pmst.group(1));
                    activity.setResult(Activity.RESULT_OK, result);
                } else {
                    Toast.makeText(activity, "登录失败", Toast.LENGTH_SHORT).show();
                    activity.setResult(Activity.RESULT_CANCELED);
                }
                activity.finish();
            }
        }
    }

}
