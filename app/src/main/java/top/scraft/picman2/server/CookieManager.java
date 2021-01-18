package top.scraft.picman2.server;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class CookieManager implements CookieJar {

    private final SharedPreferences sharedPreferences;
    private final HashMap<String, CookieSet> cookieStore = new HashMap<>();
    private String mainHost = null;

    CookieManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        String hPmst = sharedPreferences.getString("PMST_host", null);
        String vPmst = sharedPreferences.getString("PMST_val", null);
        if (hPmst != null && vPmst != null) {
            mainHost = hPmst;
            get(hPmst).add(new Cookie.Builder()
                    .domain(hPmst)
                    .name("PMST")
                    .value(vPmst)
                    .expiresAt(System.currentTimeMillis() + 2592000000L)
                    .build());
        }
    }

    private CookieSet get(String host) {
        CookieSet set = cookieStore.get(host);
        if (set == null) {
            set = new CookieSet();
            cookieStore.put(host, set);
        }
        return set;
    }

    @Override
    @EverythingIsNonNull
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies.size() > 0) {
//            Log.d("pmc", "save cookie " + url.host());
            get(url.host()).addAll(cookies);
        }
    }

    @Override
    @NonNull
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        CookieSet cookies = null;
        if (null != url) {
            cookies = cookieStore.get(url.host());
        }
        if (cookies != null) {
            return cookies.removeExpired().cookieList();
        }
        return new ArrayList<>();
    }

    void setPmst(String host, String pmst) {
//        Log.d("pmst", String.format("host=%s,pmst=%s", host, pmst));
        // TODO 刚登录不能实时生效
        mainHost = host;
        get(host).add(new Cookie.Builder()
                .domain(host).name("PMST")
                .value(pmst)
                .expiresAt(System.currentTimeMillis() + 2592000000L)
                .build());
        sharedPreferences.edit()
                .putString("PMST_host", host)
                .putString("PMST_val", pmst)
                .apply();
    }

    public String getCsrf() {
        if (mainHost != null) {
            Cookie c = get(mainHost).getCsrfCache();
            if (c != null) {
                return c.value();
            }
        }
        return null;
    }

}
