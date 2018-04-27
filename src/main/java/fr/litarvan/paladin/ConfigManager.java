package fr.litarvan.paladin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager
{
    private List<Config> configs;

    public ConfigManager()
    {
        this(new ArrayList<>());
    }

    public ConfigManager(List<Config> configs)
    {
        this.configs = configs;
    }

    public Config get(String name)
    {
        for (Config config : configs)
        {
            if (config.getName().equalsIgnoreCase(name))
            {
                return config;
            }
        }

        return null;
    }

    public <T> T at(String path)
    {
        return at(path, null);
    }

    public <T> T at(String path, T def)
    {
        int index = path.indexOf(".");
        Config config = get(path.substring(0, index));

        return config == null ? null : config.at(path.substring(index + 1), def);
    }

    public void addConfig(Config config)
    {
        configs.add(config);
    }

    public List<Config> getConfigs()
    {
        return configs;
    }
}
