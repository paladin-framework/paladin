package fr.litarvan.paladin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PaladinConfig
{
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private Map<String, ? super Object> content;

    public PaladinConfig(Map<String, ? super Object> content)
    {
        this.content = content;
    }

    public <T> T get(String key, Class<T> type)
    {
        return get(key);
    }

    public <T> T get(String key)
    {
        return (T) content.get(key);
    }

    void set(String key, Object value)
    {
        content.put(key, value);
    }

    public static PaladinConfig load(String path) throws IOException
    {
        if (path == null)
        {
            path = "./";
        }

        if (path.endsWith("/"))
        {
            path += "config.json";
        }

        File file = new File(path);
        file.getParentFile().mkdirs();

        if (!file.canRead())
        {
            System.err.println("Can't read config file '" + file.getAbsolutePath() + "'");
            System.exit(1);
        }

        if (!file.exists())
        {
            System.err.println("Config file '" + file.getAbsolutePath() + "' doesn't exist but can't write default config in it");
            System.exit(1);
        }

        return new PaladinConfig(mapper.readValue(file, new TypeReference<HashMap<String, ?>>() {}));
    }
}
