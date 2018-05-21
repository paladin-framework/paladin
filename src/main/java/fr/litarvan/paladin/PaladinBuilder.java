package fr.litarvan.paladin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Module;
import fr.litarvan.paladin.dsl.ConfigScriptBase;
import fr.litarvan.paladin.dsl.RoutesScriptBase;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.Middleware;

public class PaladinBuilder
{
    private Class<?> app;
    private List<Module> modules;
    private String configFolder;
    private String[] argv;

    private PaladinBuilder(Class<?> app)
    {
        this.app = app;
        this.configFolder = "/config";
        this.argv = new String[]{};
    }

    public PaladinBuilder create(Class<?> app)
    {
        return new PaladinBuilder(app);
    }

    public void addModule(Module module)
    {
        modules.add(module);
    }

    public void setConfigFolder(String folder)
    {
        this.configFolder = folder;
    }

    public void loadCommandLineArguments(String[] argv)
    {
        this.argv = argv;
    }

    public Paladin build()
    {
        LinkedHashMap appConfig = loadConfig("app");
        Paladin paladin = new Paladin(app, new PaladinConfig(), modules.toArray(new Module[0]));

        loadMap(appConfig, "controllers", Controller.class, paladin::addController);
        loadMap(appConfig, "routeMiddlewares", Middleware.class, paladin::addMiddleware);
        loadArray(appConfig, "globalMiddlewares", Middleware.class, paladin::addGlobalMiddleware);

        paladin.getSessionManager().setExpirationDelay((Long) appConfig.get("sessionDuration"));

        evaluate(load(configFolder + "/routes.config.groovy"), RoutesScriptBase.class, binding -> binding.setProperty("router", paladin.getRouter()));

        return paladin;
    }

    protected String load(String path)
    {
        InputStream in;
        File file = new File(path);

        if (file.exists())
        {
            try
            {
                in = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                throw new IllegalArgumentException("Unexcepted error while reading file '" + file.getAbsolutePath() + "'", e);
            }
        }
        else
        {
            in = app.getResourceAsStream((path.startsWith("/") ? "" : "/") + path);

            if (in == null)
            {
                throw new IllegalArgumentException("Can't find required file '" + path + "' neither in jar or filesystem");
            }
        }

        try
        {
            return new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Can't read required file '" + path + "'", e);
        }
    }

    protected LinkedHashMap loadConfig(String name)
    {
        String path = configFolder + "/" + name + ".config.groovy";

        Object obj = evaluate(load(path), ConfigScriptBase.class, ctx -> {});

        if (!(obj instanceof LinkedHashMap))
        {
            throw new RuntimeException(path + " does not return a map");
        }

        return (LinkedHashMap) obj;
    }

    protected <T> void loadMap(LinkedHashMap map, String key, Class<T> type, BiConsumer<String, Class<T>> action)
    {
        Object object = map.get(key);

        if (object == null)
        {
            throw new IllegalArgumentException("Can't find key '" + key + "' in config");
        }

        if (!(object instanceof Map))
        {
            throw new IllegalArgumentException("Config value with key '" + key + "' isn't a map");
        }

        Map<?, ?> objects = (Map) object;

        objects.forEach((k, v) -> {
            if (!(k instanceof String))
            {
                throw new IllegalArgumentException("Map (" + key + ") contains non-String key '" + k.toString() + "'");
            }

            if (!(v instanceof Class))
            {
                throw new IllegalArgumentException("#" + key + "." + k + " isn't a class");
            }

            Class cl = (Class) v;

            if (!type.isAssignableFrom(cl))
            {
                throw new IllegalArgumentException("#" + key + "." + k + " (" + cl.getName() + ") doesn't extend " + type.getName());
            }

            action.accept((String) k, (Class<T>) v);
        });
    }

    protected <T> void loadArray(LinkedHashMap map, String key, Class<T> type, Consumer<Class<T>> action)
    {
        Object object = map.get(key);

        if (object == null)
        {
            throw new IllegalArgumentException("Can't find key '" + key + "' in config");
        }

        if (!object.getClass().isArray())
        {
            throw new IllegalArgumentException("Config value with key '" + key + "' isn't an array");
        }

        Object[] objects = (Object[]) object;

        for (int i = 0; i < objects.length; i++)
        {
            Object value = objects[i];
            if (!(value instanceof Class))
            {
                throw new IllegalArgumentException("#" + key + "[" + i + "] isn't a class");
            }

            Class cl = (Class) value;

            if (!type.isAssignableFrom(cl))
            {
                throw new IllegalArgumentException("#" + key + "[" + i + "] (" + cl.getName() + ") doesn't extend " + type.getName());
            }

            action.accept((Class<T>) value);
        }
    }

    protected Object evaluate(String script, Class<? extends Script> base, Consumer<Binding> bindingProvider)
    {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(base.getName());

        GroovyShell shell = new GroovyShell(config);
        bindingProvider.accept(shell.getContext());

        try
        {
            return shell.evaluate(script);
        }
        catch (CompilationFailedException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected InputStream loadFile(String path)
    {
        File file = new File(path);

        if (file.exists())
        {
            try
            {
                return new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                // Can't happen
                return null;
            }
        }

        return PaladinBuilder.class.getResourceAsStream((path.startsWith("/") ? "" : "/") + path);
    }
}
