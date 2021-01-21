package top.scraft.picman2.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;
import top.scraft.picman2.server.data.LibContentDetails;
import top.scraft.picman2.server.data.LibDetails;
import top.scraft.picman2.server.data.PictureDetail;
import top.scraft.picman2.server.data.PictureUpdateResult;
import top.scraft.picman2.server.data.Result;
import top.scraft.picman2.server.data.ServerInfo;
import top.scraft.picman2.server.data.UpdatePictureRequest;
import top.scraft.picman2.server.data.UserDetail;
import top.scraft.picman2.utils.FileUtils;
import top.scraft.picman2.utils.Utils;

public class ServerController {

    public static final int API_VERSION = 1;
    private static ServerController instance = null;

    private final Gson gson = new Gson();
    private final MediaType jsonType = MediaType.parse("application/json;charset=utf-8");
    private final String userAgent = "Picman2 Android";
    private final SharedPreferences sharedPreferences;
    private final CookieManager cookieManager;
    private final OkHttpClient httpClient;

    @Getter
    private UserDetail user = null;
    @Getter
    private ServerState state = ServerState.CONNECT_ERROR;

    public static ServerController getInstance(Context appContext) {
        if (instance == null) {
            instance = new ServerController(appContext);
        }
        return instance;
    }

    private ServerController(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        cookieManager = new CookieManager(sharedPreferences);
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(cookieManager)
                .build();
    }

    public void setPmst(String host, String pmst) {
        cookieManager.setPmst(host, pmst);
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
        if (!url.startsWith("http")) {
            if (!url.startsWith("/")) {
                throw new IllegalArgumentException("请求路径错误 " + url);
            }
            url = getServer() + url;
        }
        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", userAgent)
                .url(url);
        String csrf = cookieManager.getCsrf();
        if (csrf != null) {
            builder.addHeader("X-XSRF-TOKEN", csrf);
        }
        return builder;
    }

    /**
     * 发起请求
     *
     * @param api     api路径
     * @param handler 处理回调
     */
    @EverythingIsNonNull
    private void get(String api, ServerRequestHandler handler) {
        request(requestBuilder(api), handler);
    }

    @EverythingIsNonNull
    private void request(Request.Builder req, ServerRequestHandler handler) {
        try (Response response = httpClient
                .newCall(req.build())
                .execute()) {
            handler.handle(response.code(), response.body(), null);
        } catch (IOException e) {
            e.printStackTrace();
            handler.handle(-1, null, e);
        }
    }

    @Nullable
    @EverythingIsNonNull
    private <T> Result<T> request(Request.Builder req, Type type) {
        try (Response response = httpClient
                .newCall(req.build())
                .execute()) {
            if (response.isSuccessful() && (response.body() != null)) {
                return gson.fromJson(response.body().charStream(), type);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO Exception Handler
        }
        return null;
    }

    /**
     * 更新状态
     *
     * @return
     */
    public boolean updateState() {
        if (!state.valid) {
            ServerInfo info = getServerInfo();
            if (info != null) {
                if (info.getApiVersion() == ServerController.API_VERSION) {
                    state = ServerState.NOT_LOGIN;
                    user = null;
                } else {
                    this.state = ServerState.VERSION_MISMATCH;
                }
            } else {
                this.state = ServerState.CONNECT_ERROR;
            }
        }
        if (state.valid) {
            user = getUserDetail();
            state = user == null ? ServerState.NOT_LOGIN : ServerState.LOGIN;
        }
        return this.state.valid;
    }

    private ServerInfo getServerInfo() {
        AtomicReference<ServerInfo> atomicReference = new AtomicReference<>(null);
        get("/api/", (code, body, e) -> {
            if (code == 200 && body != null) {
                Result<ServerInfo> r = gson.fromJson(body.charStream(), new TypeToken<Result<ServerInfo>>() {
                }.getType());
                atomicReference.set(r.getData());
            }
        });
        return atomicReference.get();
    }

    public void logout() {
    }

    private UserDetail getUserDetail() {
        AtomicReference<UserDetail> result = new AtomicReference<>(null);
        get("/api/my", (code, body, e) -> {
            if ((body != null) && (code == 200)) {
                JsonObject root = JsonParser.parseReader(body.charStream()).getAsJsonObject();
                result.set(gson.fromJson(root, UserDetail.class));
            }
        });
        return result.get();
    }

    // piclib

    public List<LibDetails> getPiclibs() {
        AtomicReference<List<LibDetails>> result = new AtomicReference<>(null);
        get("/api/lib/", (code, body, e) -> {
            if (body != null) {
                Result<List<LibDetails>> r = gson.fromJson(body.charStream(),
                        new TypeToken<Result<List<LibDetails>>>() {
                        }.getType());
                if (r.getCode() == 200) {
                    result.set(r.getData());
                }
            }
        });
        return result.get();
    }

    /**
     * 创建图库
     *
     * @param name
     * @return
     */
    @Nullable
    @EverythingIsNonNull
    public Result<LibDetails> createLibrary(String name) {
        String api = "/api/lib/?name=" + name;
        RequestBody body = new FormBody.Builder().build();
        return request(requestBuilder(api).post(body), new TypeToken<Result<LibDetails>>() {
        }.getType());
    }

    /**
     * 删除图库
     *
     * @param lid
     * @return 错误信息, <tt>null</tt>为成功
     */
    @Nullable
    @EverythingIsNonNull
    public String deleteLibrary(long lid) {
        String api = String.format(Locale.ENGLISH, "/api/lib/%d", lid);
        Result<Object> result = request(requestBuilder(api).delete(), new TypeToken<Result<Object>>() {
        }.getType());
        if (result != null) {
            if (result.getCode() == 200) {
                return null;
            }
            return result.getMessage();
        }
        return "网络连接失败";
    }

    public List<LibContentDetails> getLibraryContentDetails(long lid) {
        AtomicReference<List<LibContentDetails>> atomicReference = new AtomicReference<>(null);
        get("/api/lib/" + lid + "/gallery", (code, body, e) -> {
            if (body != null) {
                Result<List<LibContentDetails>> r = gson.fromJson(body.charStream(),
                        new TypeToken<Result<List<LibContentDetails>>>() {
                        }.getType());
                if (r.getCode() == 200) {
                    atomicReference.set(r.getData());
                }
            }
        });
        return atomicReference.get();
    }

    // picture

    public PictureDetail getPictureMeta(@NonNull String pid) {
        AtomicReference<PictureDetail> reference = new AtomicReference<>(null);
        get(String.format(Locale.US, "/api/pic/%s", pid), (code, body, e) -> {
            if (body != null) {
                Result<PictureDetail> r = gson.fromJson(body.charStream(),
                        new TypeToken<Result<PictureDetail>>() {
                        }.getType());
                if (r.getCode() == 200) {
                    reference.set(r.getData());
                }
            }
        });
        return reference.get();
    }

    /**
     * 更新图片信息
     *
     * @return 是否需要上传图片文件, <tt>null</tt>代表请求失败
     */
    @Nullable
    @EverythingIsNonNull
    public Boolean updatePictureMeta(long lid, String pid, String description, Set<String> tags) {
        UpdatePictureRequest req = new UpdatePictureRequest();
        req.setDescription(description);
        req.setTags(tags);
        String path = String.format(Locale.ENGLISH, "/api/lib/%d/gallery/%s", lid, pid);
        RequestBody body = RequestBody.create(jsonType, gson.toJson(req));
        Result<PictureUpdateResult> result = request(requestBuilder(path).put(body),
                new TypeToken<Result<PictureUpdateResult>>(){}.getType());
        if (result != null && result.getCode() == 200) {
            return result.getData().isNeedUpload();
        }
        return null;
    }

    @EverythingIsNonNull
    public boolean uploadPicture(long lid, String pid, File picture) {
        String path = String.format(Locale.ENGLISH, "/api/lib/%d/gallery/%s/img", lid, pid);
        RequestBody file = RequestBody.create(Utils.mediaType(pid), picture);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", pid, file)
                .build();
        Result<Object> result = request(requestBuilder(path).post(requestBody),
                new TypeToken<Result<Object>>(){}.getType());
        return result != null && result.getCode() == 200;
    }

    @EverythingIsNonNull
    public boolean savePictureFile(long lid, String pid, File dst, boolean thumb) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        String api = String.format(Locale.ENGLISH, "/api/lib/%d/gallery/%s/%s", lid, pid, thumb ? "thumb" : "img");
        get(api, (code, body, e) -> {
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
