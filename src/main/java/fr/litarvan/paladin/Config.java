package fr.litarvan.paladin;

import java.util.LinkedHashMap;

public class Config
{
    private String name;
    private LinkedHashMap config;

    public Config(String name, LinkedHashMap config)
    {
        this.name = name;
        this.config = config;
    }

    public String getName()
    {
        return name;
    }

    public <T> T get(Object key)
    {
        return (T) config.get(key);
    }

    public <T> T at(String path)
    {
        return at(path, null);
    }

    public <T> T at(String path, T def)
    {
        String[] split = path.split("\\.");
        LinkedHashMap map = config;

        for (int i = 0; i < split.length - 1; i++)
        {
            Object result = map.get(split[i]);

            if (result == null)
            {
                return null;
            }

            if (!(result instanceof LinkedHashMap))
            {
                throw new IllegalArgumentException("Field '" + split[i] + "' isn't an map");
            }
        }

        T result = (T) map.get(split[split.length - 1]);

        if (result == null)
        {
            return def;
        }

        return result;
    }
}
