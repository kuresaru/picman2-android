package top.scraft.picman2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import okhttp3.*;
import top.scraft.picman2.data.UserDetail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerController {

    private final Gson gson = new Gson();
    private final String userAgent = "Picman2 Android";
    private OkHttpClient httpClient;
    private SharedPreferences sharedPreferences;
    private String jsessionid = "";
    private String sact;

    public ServerController(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
        sact = sharedPreferences.getString("sact", "");
        jsessionid = sharedPreferences.getString("jsessionid", "");
    }

    public void setSact(String sact) {
        this.sact = sact;
        sharedPreferences.edit().putString("sact", sact).apply();
    }

    public void setJsessionid(String jsessionid) {
        this.jsessionid = jsessionid;
        sharedPreferences.edit().putString("jsessionid", jsessionid).apply();
    }

    private String getServer() {
        final String defValue = "https://localhost";
        String server = sharedPreferences.getString("server", defValue);
        if (server == null) {
            server = defValue;
        } else if (!server.startsWith("http")) {
            server = "https://" + server;
        }
        return server;
    }

    private Request.Builder requestBuilder(String url) {
        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", userAgent)
                .url(url);
        builder.addHeader("Cookie", "JSESSIONID=" + jsessionid);
        return builder;
    }

    /**
     * 发起请求，返回401时尝试登录后再重新请求
     *
     * @param api     api路径
     * @param handler 处理回调
     */
    private void request(String api, ServerRequestHandler handler) {
        try (Response response = httpClient
                .newCall(requestBuilder(getServer() + api).build())
                .execute()) {
            int code = response.code();
            if (code == 401 && sact.length() == 32) {
                // 401未登录，尝试登录
                try (Response response1 = httpClient.newCall(new Request.Builder()
                        .url(getServer() + "/api/sac_login")
                        .post(new FormBody.Builder().build())
                        .addHeader("User-Agent", userAgent)
                        .addHeader("Cookie", "SACT=" + sact)
                        .build()).execute()) {
                    if (response1.code() == 200) {
                        // 取新的jsessionid
                        List<String> cookies = response1.headers("Set-Cookie");
                        Pattern pattern = Pattern.compile("JSESSIONID=([0-9a-fA-F]{32})");
                        boolean login = false;
                        for (String cookie : cookies) {
                            Matcher matcher = pattern.matcher(cookie);
                            if (matcher.find()) {
                                setJsessionid(matcher.group(1));
                                login = true;
                                break;
                            }
                        }
                        if (login) {
                            // 登录成功 再试
                            try (Response response2 = httpClient
                                    .newCall(requestBuilder(getServer() + api).build())
                                    .execute()) {
                                handler.handle(response2.code(), response2.body(), null);
                            } catch (IOException e) {
                                e.printStackTrace();
                                handler.handle(-3, null, e);
                            }
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.handle(-2, null, e);
                    return;
                }
            }
            // 非401直接处理 或登录失败返回原结果
            handler.handle(code, response.body(), null);
        } catch (IOException e) {
            e.printStackTrace();
            handler.handle(-1, null, e);
        }
    }

    @Deprecated
    private Response requestGet(String url) throws IOException {
        return httpClient.newCall(requestBuilder(url).build()).execute();
    }

    public boolean ping() {
        String url = getServer() + "/api/ping";
        try (Response response = requestGet(url)) {
            if (response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    return body.string().equals("pong");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        request("/api/sac_logout", (code, body, e) -> {});
    }

    public UserDetail getUserDetail() {
        AtomicReference<UserDetail> result = new AtomicReference<>(null);
        request("/api/user/detail", (code, body, e) -> {
            if (body != null) {
                result.set(gson.fromJson(body.charStream(), UserDetail.class));
            }
        });
        return result.get();
    }

}
