package fr.litarvan.paladin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteStreams;

public class PaladinConfig
{
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private ObjectNode content;

    public PaladinConfig(ObjectNode content)
    {
        this.content = content;
    }

    public <T> T get(String key, Class<T> type)
    {
        JsonNode value = content.get(key);

        if (value == null)
        {
            return null;
        }

        try
        {
            return mapper.treeToValue(value, type);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException("Unexpected JSON Config error", e);
        }
    }

    void set(String key, Object value)
    {
        content.set(key, mapper.valueToTree(value));
    }

    public static PaladinConfig load(Class<?> app, String path) throws IOException
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

        if (file.exists() && !file.canRead())
        {
            System.err.println("Can't read config file '" + file.getAbsolutePath() + "'");
            System.exit(1);
        }

        if (!file.exists())
        {
            if (!file.createNewFile() || !file.canWrite())
            {
                System.err.println("Config file '" + file.getAbsolutePath() + "' doesn't exist but can't write default config in it");
                System.exit(1);
            }

            InputStream in = app.getResourceAsStream("/config.default.json");

            if (in == null)
            {
                in = Paladin.class.getResourceAsStream("/config.default.json");
            }

            ByteStreams.copy(in, new FileOutputStream(file));
        }

        return new PaladinConfig((ObjectNode) mapper.readTree(file));
    }
}
