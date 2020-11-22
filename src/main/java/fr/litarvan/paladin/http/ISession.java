package fr.litarvan.paladin.http;

import java.util.Map;

public interface ISession {

	String getToken();

	Map<Class<?>, Object> getData();

    default <T> T get(Class<T> type)
    {
        return (T) getData().get(type);
    }

    default <T> T getAt(Class<T> type)
    {
        return get(type);
    }

    default <T> void set(Class<T> type, T value)
    {
        getData().put(type, value);
    }

    default <T> void putAt(Class<T> type, T value)
    {
        set(type, value);
    }

    default boolean has(Class<?> key)
    {
        return getData().containsKey(key);
    }

    default void delete(Class<?> key)
    {
        getData().remove(key);
    }
}
