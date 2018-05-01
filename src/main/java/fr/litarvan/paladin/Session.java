package fr.litarvan.paladin;

import java.util.HashMap;
import java.util.Map;

public class Session
{
    private long expirationTime;
    private String token;
    private Map<Class<?>, Object> data;

    public Session(long expireAfter, String token)
    {
        this.expirationTime = expireAfter;
        this.token = token;
        this.data = new HashMap<>();
    }

    public long getExpirationTime()
    {
        return expirationTime;
    }

    public boolean isExpired()
    {
        return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
    }

    public String getToken()
    {
        return token;
    }

    public Map<Class<?>, Object> getData()
    {
        return data;
    }

    public <T> T get(Class<T> type)
    {
        return (T) getData().get(type);
    }

    public <T> T getAt(Class<T> type)
    {
        return get(type);
    }

    public <T> void set(Class<T> type, T value)
    {
        getData().put(type, value);
    }

    public <T> void putAt(Class<T> type, T value)
    {
        set(type, value);
    }

    public boolean has(Class<?> key)
    {
        return getData().containsKey(key);
    }

    public void delete(Class<?> key)
    {
        getData().remove(key);
    }
}
