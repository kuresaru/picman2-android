package top.scraft.picman2.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.scraft.picman2.server.data.InfoResult;
import top.scraft.picman2.server.data.PicLibDetail;
import top.scraft.picman2.server.data.PictureDetail;
import top.scraft.picman2.server.data.UserDetail;
import top.scraft.picman2.utils.FileUtils;

public class ServerController {

    public static final int API_VERSION = 1;
    private static ServerController instance = null;

    @Getter
    private String testResult = "正在初始化";
    @Getter
    private boolean serverValid = false;

    private final Gson gson = new Gson();
    private final String userAgent = "Picman2 Android";
    private OkHttpClient httpClient;
    private SharedPreferences sharedPreferences;
    private String jsessionid = "";
    private String sact;

    public static ServerController getInstance(Context appContext) {
        if (instance == null) {
            instance = new ServerController(appContext);
        }
        return instance;
    }

    private ServerController(Context context) {
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

    public String getJsessionid() {
        return this.jsessionid;
    }

    public void setJsessionid(String jsessionid) {
        this.jsessionid = jsessionid;
        sharedPreferences.edit().putString("jsessionid", jsessionid).apply();
    }

    public String getServer() {
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

    public boolean test() {
        InfoResult info = getServerInfo();
        if (info != null) {
            if (info.getApiVersion() == ServerController.API_VERSION) {
                testResult = "服务器正常";
                serverValid = true;
            } else {
                testResult = "服务器版本不匹配";
                serverValid = false;
            }
        } else {
            testResult = "服务器连接失败";
            serverValid = false;
        }
        return serverValid;
    }

    private InfoResult getServerInfo() {
        AtomicReference<InfoResult> atomicReference = new AtomicReference<>(null);
        request("/api/info", (code, body, e) -> {
            if (code == 200 && body != null) {
                atomicReference.set(gson.fromJson(body.charStream(), InfoResult.class));
            }
        });
        return atomicReference.get();
    }

    public void logout() {
        request("/api/sac_logout", (code, body, e) -> {
        });
    }

    public UserDetail getUserDetail() {
        AtomicReference<UserDetail> result = new AtomicReference<>(null);
        request("/api/user/detail", (code, body, e) -> {
            if (body != null && (code == 200 || code == 401)) {
                result.set(gson.fromJson(body.charStream(), UserDetail.class));
            }
        });
        return result.get();
    }

    // piclib

    public List<PicLibDetail> getPiclibs() {
        AtomicReference<List<PicLibDetail>> result = new AtomicReference<>(null);
        request("/api/piclib/get_all", (code, body, e) -> {
            if (code == 200 && body != null) {
                JsonObject root = JsonParser.parseReader(body.charStream()).getAsJsonObject();
                if (root.get("success").getAsBoolean()) {
                    JsonArray libs = root.get("libs").getAsJsonArray();
                    result.set(gson.fromJson(libs, new TypeToken<List<PicLibDetail>>(){}.getType()));
                }
            }
        });
        return result.get();
    }

    public List<PictureDetail> getPiclibContent(int lid) {
        AtomicReference<List<PictureDetail>> atomicReference = new AtomicReference<>(null);
        request("/api/piclib/" + lid + "/content", (code, body, e) -> {
            if (code == 200 && body != null) {
                atomicReference.set(gson.fromJson(body.charStream(), new TypeToken<List<PictureDetail>>(){}.getType()));
            }
        });
        return atomicReference.get();
    }

    // picture

    public boolean savePictureThumb(int lid, @NonNull String pid, @NonNull File dst) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        request(String.format(Locale.US, "/api/picture/thumb/%d/%s", lid, pid), (code, body, e) -> {
            if (code == 200 && body != null) {
                try {
                    FileUtils.saveFileFromStream(body.byteStream(), dst);
                    atomicBoolean.set(true);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        return atomicBoolean.get();
    }

    public boolean savePictureFile(int lid, @NonNull String pid, @NonNull File dst) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        request(String.format(Locale.US, "/api/picture/get/%d/%s", lid, pid), (code, body, e) -> {
            if (code == 200 && body != null) {
                try {
                    FileUtils.saveFileFromStream(body.byteStream(), dst);
                    atomicBoolean.set(true);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        return atomicBoolean.get();
    }

}
