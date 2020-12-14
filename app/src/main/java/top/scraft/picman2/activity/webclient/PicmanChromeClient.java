package top.scraft.picman2.activity.webclient;

import android.util.Log;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

public class PicmanChromeClient extends WebChromeClient {

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.i("PicmanChromeClient", String.format("(%s/%s:%d) %s",
                consoleMessage.messageLevel(),
                consoleMessage.sourceId(),
                consoleMessage.lineNumber(),
                consoleMessage.message()));
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
        return super.onJsAlert(webView, s, s1, jsResult);
    }

    @Override
    public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
        return super.onJsPrompt(webView, s, s1, s2, jsPromptResult);
    }

}
