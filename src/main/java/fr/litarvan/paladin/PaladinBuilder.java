package fr.litarvan.paladin;

import com.google.inject.Module;
import fr.litarvan.paladin.dsl.ConfigScriptBase;
import fr.litarvan.paladin.dsl.RoutesScriptBase;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.Middleware;

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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PaladinBuilder
{
    private Class<? extends App> app;
    private File configFolder;
    private File routesFile;
    private String controllersAt;
    private String middlewaresAt;
    private String globalMiddlewaresAt;
    private List<Module> modules;

    private PaladinBuilder(Class<? extends App> app)
    {
        this.app = app;
        this.controllersAt = "app.controllers";
        this.middlewaresAt = "app.routeMiddlewares";
        this.globalMiddlewaresAt = "app.globalMiddlewares";
        this.modules = new ArrayList<>();
    }

    public static PaladinBuilder create(Class<? extends App> app)
    {
        return new PaladinBuilder(app);
    }

    public PaladinBuilder addModule(Module... module)
    {
        this.modules.addAll(Arrays.asList(module));
        return this;
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

    public PaladinBuilder setMiddlewaresAt(String middlewaresAt)
    {
        this.middlewaresAt = middlewaresAt;
        return this;
    }

    public PaladinBuilder setGlobalMiddlewaresAt(String globalMiddlewaresAt)
    {
        this.globalMiddlewaresAt = globalMiddlewaresAt;
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
        Paladin paladin = new Paladin(app, configManager, modules.toArray(new Module[0]));

        loadMap(configManager, controllersAt, Controller.class, paladin::addController);
        loadMap(configManager, middlewaresAt, Middleware.class, paladin::addMiddleware);
        loadArray(configManager, globalMiddlewaresAt, Middleware.class, paladin::addGlobalMiddleware);

        if (routesFile != null)
        {
            evaluate(routesFile, RoutesScriptBase.class, binding -> binding.setProperty("router", paladin.getRouter()));
        }

        return paladin;
    }

    protected <T> void loadMap(ConfigManager configManager, String at, Class<T> type, BiConsumer<String, Class<T>> action)
    {
        Object object = configManager.at(at);

        if (object == null)
        {
            throw new IllegalArgumentException("Can't find config entry #" + at);
        }

        if (!(object instanceof Map))
        {
            throw new IllegalArgumentException("#" + at + " isn't a map");
        }

        Map<?, ?> objects = (Map) object;

        objects.forEach((k, v) -> {
            if (!(k instanceof String))
            {
                throw new IllegalArgumentException("Map (#" + at + ") contains non-String key '" + k.toString() + "'");
            }

            if (!(v instanceof Class))
            {
                throw new IllegalArgumentException("#" + at + "." + k + " isn't a class");
            }

            Class cl = (Class) v;

            if (!type.isAssignableFrom(cl))
            {
                throw new IllegalArgumentException("#" + at + "." + k + " (" + cl.getName() + ") doesn't extend " + type.getName());
            }

            action.accept((String) k, (Class<T>) v);
        });
    }

    protected <T> void loadArray(ConfigManager configManager, String at, Class<T> type, Consumer<Class<T>> action)
    {
        Object object = configManager.at(at);

        if (object == null)
        {
            throw new IllegalArgumentException("Can't find config entry #" + at);
        }

        if (!object.getClass().isArray())
        {
            throw new IllegalArgumentException("#" + at + " isn't an array");
        }

        Object[] objects = (Object[]) object;

        for (int i = 0; i < objects.length; i++)
        {
            Object value = objects[i];
            if (!(value instanceof Class))
            {
                throw new IllegalArgumentException("#" + at + "[" + i + "] isn't a class");
            }

            Class cl = (Class) value;

            if (!type.isAssignableFrom(cl))
            {
                throw new IllegalArgumentException("#" + at + "[" + i + "] (" + cl.getName() + ") doesn't extend " + type.getName());
            }

            action.accept((Class<T>) value);
        }
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
