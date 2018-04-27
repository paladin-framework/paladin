package fr.litarvan.paladin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.routing.Router;
import fr.litarvan.paladin.http.server.PaladinHttpServer;
import fr.litarvan.paladin.http.server.impl.ApacheAsyncHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Paladin
{
    private static final Logger log = LoggerFactory.getLogger("Paladin");

    public static final String VERSION = "1.0.0";
    public static final String PORT_AT = "http.port";
    public static final long SESSION_DEFAULT_EXPIRATION_DELAY = 15 * 24 * 60 * 60 * 1000; // 15 days

    private Injector injector;

    private ConfigManager configManager;
    private Router router;
    private SessionManager sessionManager;

    private Map<String, Controller> controllers;

    public Paladin()
    {
        this(new ConfigManager());
    }

    public Paladin(ConfigManager configManager)
    {
        this.configManager = configManager;
        this.router = new Router(this);
        this.sessionManager = new SessionManager(SESSION_DEFAULT_EXPIRATION_DELAY);

        this.controllers = new HashMap<>();

        this.injector = Guice.createInjector(new PaladinGuiceModule(this));
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
        log.info("Starting Paladin v{}", VERSION);

        try
        {
            server.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println();
        log.info("--> Listening on {}", server.getAddress()); // TODO: Server address
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

    public void addController(String name, Class<? extends Controller> controller)
    {
        this.controllers.put(name, this.injector.getInstance(controller));
    }

    public Controller getController(String name)
    {
        return this.controllers.get(name);
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
}
