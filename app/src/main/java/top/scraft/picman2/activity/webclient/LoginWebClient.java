package top.scraft.picman2.activity.webclient;

import android.app.Activity;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import lombok.RequiredArgsConstructor;
import top.scraft.picman2.activity.BrowserActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class LoginWebClient extends WebViewClient {

    private final BrowserActivity activity;
    private final Pattern pattern = Pattern.compile("SACT=([0-9A-Fa-f]{32})");

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

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

}
