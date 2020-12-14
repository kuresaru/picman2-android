package top.scraft.picman2.activity.webclient;

import android.app.Activity;
import android.content.Intent;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebView;

import top.scraft.picman2.activity.BrowserActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginWebClient extends PicmanWebClient {

    private final Pattern pattern = Pattern.compile("SACT=([0-9A-Fa-f]{32})");

    public LoginWebClient(BrowserActivity activity) {
        super(activity);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            Matcher matcher = pattern.matcher(cookie);
            if (matcher.find()) {
                String sact = matcher.group(1);
                Intent result = new Intent();
                result.putExtra("SACT", sact);
                activity.setResult(Activity.RESULT_OK, result);
                activity.finish();
            }
        }
    }

}
