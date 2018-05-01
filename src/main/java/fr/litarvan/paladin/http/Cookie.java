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

    private String name;
    private String value;
    private Map<String, String> params;

    public Cookie(String name, String value)
    {
        this.name = name;
        this.value = value;
        this.params = new HashMap<>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setParam(String key, String value)
    {
        params.put(key, value);
    }

    public void setParam(String key)
    {
        setParam(key, null);
    }

    public String getParam(String key)
    {
        return params.get(key);
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    @Override
    public String toString()
    {
        return name + "=" + value;
    }
}
