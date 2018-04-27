package fr.litarvan.paladin.http;

import java.util.HashMap;
import java.util.Map;

public class Cookie
{
    public static final String KEY_EXPIRES = "Expires";
    public static final String KEY_MAX_AGE = "Max-Age";
    public static final String KEY_DOMAIN = "Domain";
    public static final String KEY_PATH = "Path";
    public static final String KEY_SECURE = "Secure";
    public static final String KEY_HTTP_ONLY = "HttpOnly";
    public static final String KEY_SAME_SITE = "SameSite";

    public static final String VALUE_STRICT = "Strict";
    public static final String VALUE_LAX = "Lax";

    public final String name;
    public final String value;
    public final Map<String, String> params;

    public Cookie(String name, String value)
    {
        this.name = name;
        this.value = value;
        this.params = new HashMap<>();
    }

    public void set(String key, String value)
    {
        params.put(key, value);
    }

    public void set(String key)
    {
        set(key, null);
    }
}
