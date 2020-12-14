package top.scraft.picman2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

import top.scraft.picman2.R;
import top.scraft.picman2.activity.webclient.LoginWebClient;
import top.scraft.picman2.activity.webclient.PicmanChromeClient;
import top.scraft.picman2.activity.webclient.PicmanWebClient;
import top.scraft.picman2.server.ServerController;

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;
    private ServerController serverController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        serverController = ServerController.getInstance(this);

        Intent request = getIntent();
        Intent result = new Intent();
        String url = null;
        String title = null;
        String type = "NORMAL";
        if (request != null) {
            url = request.getStringExtra("URL");
            title = request.getStringExtra("TITLE");
            type = request.getStringExtra("TYPE");
        }
        if (url == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            result.putExtra("success", false);
            setResult(RESULT_CANCELED, result);
            finish();
            return;
        }
        if (title == null) {
            title = url;
        }

        setTitle(title);

        webView = findViewById(R.id.webview);

        if ("LOGIN".equals(type)) {
            webView.setWebViewClient(new LoginWebClient(this));
        } else {
            webView.setWebViewClient(new PicmanWebClient(this));
        }
        webView.setWebChromeClient(new PicmanChromeClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookies(null);
        cookieManager.flush();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(serverController.getServer(), "JSESSIONID=".concat(serverController.getJsessionid()));

        webView.loadUrl(url);

    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();
    }

}
