package top.scraft.picman2.activity.webclient;


import android.util.Log;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import lombok.RequiredArgsConstructor;
import top.scraft.picman2.activity.BrowserActivity;

@RequiredArgsConstructor
public class PicmanWebClient extends WebViewClient {

    protected final BrowserActivity activity;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("picman_webview", "load url " + url);
        return false;
    }

}
