package fr.litarvan.paladin;

import java.util.HashMap;
import java.util.Map;

import fr.litarvan.paladin.http.ISession;

public class Session implements ISession
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
}
