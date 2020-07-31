package top.scraft.picman2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import top.scraft.picman2.R;
import top.scraft.picman2.activity.webclient.LoginWebClient;

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Intent request = getIntent();
        Intent result = new Intent();
        String url = null;
        String title = null;
        if (request != null) {
            url = request.getStringExtra("URL");
            title = request.getStringExtra("TITLE");
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
        CookieManager.getInstance().removeAllCookie();
        webView.loadUrl(url);
        webView.setWebViewClient(new LoginWebClient(this));

    }

}
