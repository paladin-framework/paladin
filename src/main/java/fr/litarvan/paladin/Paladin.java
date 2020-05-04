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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GString;

public class Paladin
{
    private static final Logger log = LoggerFactory.getLogger("Paladin");

    public static final String VERSION = "1.0.0";

    private Object app;
    private PaladinApp appInfo;
    private PaladinConfig config;

    private ObjectMapper mapper;
    private Injector injector;

    private Router router;
    private ISessionManager sessionManager;
    private ExceptionHandler exceptionHandler;

    private Map<String, Controller> controllers;
    private Map<String, Middleware> middlewares;
    private List<Middleware> globalMiddlewares;

    public Paladin(Class<?> app, PaladinConfig config, ISessionManager sessionManager, Module... guiceModules)
    {
        this.mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(GString.class, new GStringSerializer());
        this.mapper.registerModule(module);

        this.router = new Router(this);
        this.sessionManager = sessionManager;
        this.exceptionHandler = new ExceptionHandler();

        this.controllers = new HashMap<>();
        this.middlewares = new HashMap<>();
        this.globalMiddlewares = new ArrayList<>();

        List<Module> modules = new ArrayList<>(Arrays.asList(guiceModules));
        modules.add(new PaladinGuiceModule(this));

        this.config = config;

        this.injector = Guice.createInjector(modules);
        this.app = injector.getInstance(app);
        this.appInfo = app.getDeclaredAnnotation(PaladinApp.class);
    }

    public void start()
    {
        start(new ApacheAsyncHttpServer(this, config.get("port", int.class)));
    }

    public void start(PaladinHttpServer server)
    {
        log.info("Starting {} v{} by '{}' using Paladin v{}", appInfo.name(), appInfo.version(), appInfo.author(), VERSION);
        executeCallback(OnStart.class, app);

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

        executeCallback(OnStop.class, app);
        server.shutdown();
    }

    public void executeCallback(Class<? extends Annotation> annotation, Object object)
    {
        for (Method method : object.getClass().getMethods())
        {
            if (method.isAnnotationPresent(annotation))
            {
                boolean isProtected = method.isAccessible();

                if (isProtected)
                {
                    method.setAccessible(true);
                }

                try
                {
                    method.invoke(object);
                }
                catch (IllegalAccessException ignored)
                {
                    // Can't happen
                }
                catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e.getTargetException());
                }

                if (isProtected)
                {
                    method.setAccessible(false);
                }
            }
        }
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

        boolean crashed = false;

        BeforeEvent before = new BeforeEvent();
        for (Middleware m : middlewares)
        {
			try
			{
				m.before(before, request, response, route);
			}
			catch (Exception e)
			{
				result = exceptionHandler.handle(e, request, response);
				crashed = true;
			}
        }

        if (!crashed && before.isCancelled())
        {
            result = before.getResult();
        }
        else if (!crashed)
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
				try
				{
					m.after(after, request, response, route);
				}
				catch (Exception e)
				{
					result = exceptionHandler.handle(e, request, response);
					crashed = true;
				}
            }

            if (!crashed)
			{
				result = after.getResult();
			}
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

    public Object getApp()
    {
        return app;
    }

    public PaladinApp getAppInfo()
    {
        return appInfo;
    }

    public PaladinConfig getConfig()
    {
        return config;
    }

    public Injector getInjector()
    {
        return injector;
    }

    public Router getRouter()
    {
        return router;
    }

    public ISessionManager getSessionManager()
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
