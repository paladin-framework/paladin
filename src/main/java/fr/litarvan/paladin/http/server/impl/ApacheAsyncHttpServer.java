package fr.litarvan.paladin.http.server.impl;

import fr.litarvan.paladin.Paladin;
import fr.litarvan.paladin.http.server.PaladinHttpServer;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.util.concurrent.TimeUnit;

public class ApacheAsyncHttpServer implements PaladinHttpServer
{
    private Paladin paladin;
    private int port;

    private HttpServer server;
    private SSLContext ssl;

    public ApacheAsyncHttpServer(Paladin paladin, int port)
    {
        this.paladin = paladin;
        this.port = port;
    }

    @Override
    public void loadSSLCert(File keystore, char[] secret) throws GeneralSecurityException, IOException
    {
        this.ssl = SSLContexts.custom()
            .loadKeyMaterial(keystore, secret, secret)
            .build();
    }

    @Override
    public void start() throws IOException
    {
        this.server = ServerBootstrap.bootstrap()
            .setListenerPort(port)
            .setServerInfo("Paladin/" + Paladin.VERSION)
            .setIOReactorConfig(IOReactorConfig.DEFAULT)
            .setSslContext(ssl)
            .setExceptionLogger(ex ->
            {
                ex.printStackTrace();
            })
            .registerHandler("*", new HttpRequestHandler(this.paladin))
            .create();

        server.start();
    }

    @Override
    public void waitFor() throws InterruptedException
    {
        if (server == null)
        {
            return;
        }

        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public void shutdown()
    {
        if (server == null)
        {
            return;
        }

        server.shutdown(5, TimeUnit.SECONDS);
    }

    @Override
    public String getAddress()
    {
        return getServer().getEndpoint().getAddress().toString();
    }

    public HttpServer getServer()
    {
        return server;
    }
}
