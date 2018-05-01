package fr.litarvan.paladin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.primitives.Primitives;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.Header;
import fr.litarvan.paladin.http.Middleware;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;
import fr.litarvan.paladin.http.routing.Route;
import fr.litarvan.paladin.http.routing.RouteNotFoundException;
import fr.litarvan.paladin.http.routing.Router;
import fr.litarvan.paladin.http.server.PaladinHttpServer;
import fr.litarvan.paladin.http.server.impl.ApacheAsyncHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GString;

public class Paladin
{
    private static final Logger log = LoggerFactory.getLogger("Paladin");

    public static final String VERSION = "0.0.2";
    public static final String PORT_AT = "http.port";
    public static final String AUTH_ALGORITHM_AT = "http.authAlgorithm";
    public static final long SESSION_DEFAULT_EXPIRATION_DELAY = 15 * 24 * 60 * 60 * 1000; // 15 days

    private App app;

    private ObjectMapper mapper;
    private Injector injector;

    private ConfigManager configManager;
    private Router router;
    private SessionManager sessionManager;
    private ExceptionHandler exceptionHandler;

    private Map<String, Controller> controllers;
    private Map<String, Middleware> middlewares;
    private List<Middleware> globalMiddlewares;

    public Paladin(Class<? extends App> app, Module... guiceModules)
    {
        this(app, new ConfigManager(), guiceModules);
    }

    public Paladin(Class<? extends App> app, ConfigManager configManager, Module... guiceModules)
    {
        this.mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(GString.class, new GStringSerializer());
        this.mapper.registerModule(module);

        this.configManager = configManager;
        this.router = new Router(this);
        this.sessionManager = new SessionManager(this, configManager.at(AUTH_ALGORITHM_AT), SESSION_DEFAULT_EXPIRATION_DELAY);
        this.exceptionHandler = new ExceptionHandler();

        this.controllers = new HashMap<>();
        this.middlewares = new HashMap<>();
        this.globalMiddlewares = new ArrayList<>();

        List<Module> modules = new ArrayList<>(Arrays.asList(guiceModules));
        modules.add(new PaladinGuiceModule(this));

        this.injector = Guice.createInjector(modules);
        this.app = injector.getInstance(app);
    }

    public void start()
    {
        int port = configManager.at(PORT_AT, -1);

        if (port == -1)
        {
            throw new IllegalStateException("Can't find valid http port at #" + PORT_AT);
        }

        start(new ApacheAsyncHttpServer(this, port));
    }

    public void start(PaladinHttpServer server)
    {
        log.info("Starting {} v{} using Paladin v{}", app.getName(), app.getVersion(), VERSION);
        app.onStart();

        try
        {
            server.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        log.info("--> Listening on {}", server.getAddress());
        System.out.println();

        try
        {
            server.waitFor();
        }
        catch (InterruptedException ignored)
        {
        }

        server.shutdown();
    }

    public void execute(Request request, Response response)
    {
        Object result = null;

        List<Middleware> middlewares = new ArrayList<>();
        middlewares.addAll(globalMiddlewares);

        Route route = getRouter().match(request);

        if (route == null)
        {
            result = exceptionHandler.handle(new RouteNotFoundException("Can't find any route matching '" + request.getMethod() + " " + request.getUri() + "'"), request, response);
        }
        else
        {
            middlewares.addAll(Arrays.asList(route.getMiddlewares()));
        }

        BeforeEvent before = new BeforeEvent();
        for (Middleware m : middlewares)
        {
            m.before(before, request, response, route);
        }

        if (before.isCancelled())
        {
            result = before.getResult();
        }
        else
        {
            if (route != null)
            {
                try
                {
                    result = route.getAction().call(request, response);
                }
                catch (Exception e)
                {
                    result = exceptionHandler.handle(e, request, response);
                }
            }

            AfterEvent after = new AfterEvent(result);

            for (Middleware m : middlewares)
            {
                m.after(after, request, response, route);
            }

            result = after.getResult();
        }

        if (result instanceof byte[])
        {
            response.setContent((byte[]) result);
        }
        else
        {
            if (result == null || Primitives.isWrapperType(result.getClass()) || result instanceof String)
            {
                result = new Result(result);
            }

            response.setContentType(Header.CONTENT_TYPE_JSON);

            try
            {
                response.setContentString(mapper.writeValueAsString(result));
            }
            catch (JsonProcessingException e)
            {
                exceptionHandler.handle(e, request, response);
            }
        }
    }

    public void addController(String name, Class<? extends Controller> controller)
    {
        this.controllers.put(name, this.injector.getInstance(controller));
    }

    public Controller getController(String name)
    {
        return this.controllers.get(name);
    }

    public void addMiddleware(String name, Class<? extends Middleware> middleware)
    {
        this.middlewares.put(name, this.injector.getInstance(middleware));
    }

    public Middleware getMiddleware(String name)
    {
        return this.middlewares.get(name);
    }

    public void addGlobalMiddleware(Class<? extends Middleware> middleware)
    {
        this.globalMiddlewares.add(this.injector.getInstance(middleware));
    }

    public Middleware[] getGlobalMiddlewares()
    {
        return this.globalMiddlewares.toArray(new Middleware[0]);
    }

    public App getApp()
    {
        return app;
    }

    public Injector getInjector()
    {
        return injector;
    }

    public ConfigManager getConfigManager()
    {
        return configManager;
    }

    public Router getRouter()
    {
        return router;
    }

    public SessionManager getSessionManager()
    {
        return sessionManager;
    }

    public ExceptionHandler getExceptionHandler()
    {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        this.exceptionHandler = exceptionHandler;
    }

    public ObjectMapper getJSONMapper()
    {
        return mapper;
    }

    protected static class Result
    {
        public final Object result;

        public Result(Object result)
        {
            this.result = result;
        }
    }
}
