package fr.litarvan.paladin;

import fr.litarvan.paladin.dsl.ConfigScriptBase;
import fr.litarvan.paladin.dsl.RoutesScriptBase;
import fr.litarvan.paladin.http.Controller;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PaladinBuilder
{
    private File configFolder;
    private File routesFile;
    private String controllersAt;

    private PaladinBuilder()
    {
        this.controllersAt = "app.controllers";
    }

    public static PaladinBuilder create()
    {
        return new PaladinBuilder();
    }

    public PaladinBuilder setConfigFolder(File configFolder)
    {
        if (!configFolder.exists())
        {
            throw new IllegalArgumentException("Can't find config folder '" + configFolder + "' in filesystem");
        }

        this.configFolder = configFolder;
        return this;
    }

    public PaladinBuilder setConfigFolder(String configFolder)
    {
        return this.setConfigFolder(getFile(configFolder));
    }

    public PaladinBuilder setRoutesFile(File routesFile)
    {
        if (!routesFile.exists())
        {
            throw new IllegalArgumentException("Can't find routes file '" + routesFile + "' in filesystem");
        }

        this.routesFile = routesFile;
        return this;
    }

    public PaladinBuilder setRoutesFile(String path)
    {
        return setRoutesFile(getFile(path));
    }

    public PaladinBuilder setControllersAt(String controllersAt)
    {
        this.controllersAt = controllersAt;
        return this;
    }

    public Paladin build()
    {
        List<Config> configs = new ArrayList<>();

        if (configFolder == null)
        {
            throw new IllegalStateException("Config folder not defined, use PaladinBuilder#setConfigFolder");
        }

        for (File file : configFolder.listFiles())
        {
            Object obj = evaluate(file, ConfigScriptBase.class, ctx -> {});

            if (!(obj instanceof LinkedHashMap))
            {
                throw new RuntimeException(file.getAbsolutePath() + " does not return a map");
            }

            LinkedHashMap config = (LinkedHashMap) obj;
            String name = file.getName();

            name = name.substring(0, name.indexOf('.'));

            configs.add(new Config(name, config));
        }

        ConfigManager configManager = new ConfigManager(configs);
        Paladin paladin = new Paladin(configManager);

        Object object = configManager.at(controllersAt);

        if (object == null)
        {
            throw new IllegalArgumentException("Can't find config entry #" + controllersAt);
        }

        if (!(object instanceof Map))
        {
            throw new IllegalArgumentException("#" + controllersAt + " isn't a map");
        }

        Map<?, ?> controllers = (Map) object;

        controllers.forEach((k, v) -> {
            if (!(k instanceof String))
            {
                throw new IllegalArgumentException("Controllers map (#" + controllersAt + ") contains non-String key '" + k.toString() + "'");
            }

            if (!(v instanceof Class))
            {
                throw new IllegalArgumentException("#" + controllersAt + "." + k + " isn't a class");
            }

            Class cl = (Class) v;

            if (!Controller.class.isAssignableFrom(cl))
            {
                throw new IllegalArgumentException("#" + controllersAt + "." + k + " (" + cl.getName() + ") doesn't extend " + Controller.class.getName());
            }

            paladin.addController((String) k, (Class<? extends Controller>) v);
        });

        if (routesFile != null)
        {
            evaluate(routesFile, RoutesScriptBase.class, binding -> binding.setProperty("router", paladin.getRouter()));
        }

        return paladin;
    }

    protected Object evaluate(File script, Class<? extends Script> base, Consumer<Binding> bindingProvider)
    {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(base.getName());

        GroovyShell shell = new GroovyShell(config);
        bindingProvider.accept(shell.getContext());

        try
        {
            return shell.evaluate(script);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (CompilationFailedException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected File getFile(String path)
    {
        URL resource = PaladinBuilder.class.getResource((path.startsWith("/") ? "" : "/") + path);

        if (resource == null)
        {
            return new File(path);
        }

        try
        {
            return new File(resource.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Unexpected error while loading file '" + path + "'", e);
        }
    }
}
