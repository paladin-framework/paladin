package fr.litarvan.paladin;

import java.util.HashMap;
import java.util.Map;

public class Session
{
    private long expirationTime;
    private String token;
    private Map<String, Object> data;
    private Map<Class<?>, Object> classData;

    public Session(long expireAfter, String token)
    {
        this.expirationTime = expireAfter;
        this.token = token;
        this.data = new HashMap<>();
        this.classData = new HashMap<>();
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

    public Map<String, Object> getData()
    {
        return data;
    }

    public Map<Class<?>, Object> getClassData()
    {
        return classData;
    }

    public <T> T get(String key)
    {
        return (T) getData().get(key);
    }

    public <T> T get(Class<T> type)
    {
        return (T) getClassData().get(type);
    }

    public void set(String key, Object value)
    {
        getData().put(key, value);
    }

    public <T> void set(Class<T> type, T value)
    {
        getClassData().put(type, value);
    }

    public boolean has(String key)
    {
        return getData().containsKey(key);
    }

    public boolean has(Class<?> key)
    {
        return getClassData().containsKey(key);
    }

    public void delete(String key)
    {
        getData().remove(key);
    }

    public void delete(Class<?> key)
    {
        getClassData().remove(key);
    }
}
