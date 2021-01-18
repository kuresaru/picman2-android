package top.scraft.picman2.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import okhttp3.Cookie;

public class CookieSet extends AbstractSet<Cookie> {

    private static final String LOG_TAG = "CookieSet";
    private final HashMap<Cookie, String> map = new HashMap<>();
    @Getter
    private Cookie csrfCache = null;

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        if (o instanceof String) {
            return map.containsValue(o);
        }
        return false;
    }

    @NonNull
    @Override
    public Iterator<Cookie> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(Cookie cookie) {
        if (!contains(cookie.name())) {
            map.put(cookie, cookie.name());
//            Log.d(LOG_TAG, "Set-Cookie: " + cookie.toString());
            if (cookie.name().equals("XSRF-TOKEN")) {
                csrfCache = cookie;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        if (o instanceof Cookie) {
            Cookie cookie = (Cookie) o;
            if (contains(cookie.name())) {
                Iterator<Map.Entry<Cookie, String>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Cookie, String> entry = iterator.next();
                    if (entry.getValue().equals(cookie.name())) {
                        iterator.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Cookie> c) {
        boolean ret = false;
        for (Cookie cookie : c) {
            ret = add(cookie) || ret;
        }
        return ret;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void clear() {
        map.clear();
    }

    public CookieSet removeExpired() {
        Iterator<Cookie> itr = iterator();
        while (itr.hasNext()) {
            Cookie c = itr.next();
            if (c.expiresAt() < System.currentTimeMillis()) {
                itr.remove();
            }
        }
        return this;
    }

    public List<Cookie> cookieList() {
        return new ArrayList<>(this);
    }

    @Nullable
    public Cookie get(@NonNull String name) {
        for (Map.Entry<Cookie, String> entry : map.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
