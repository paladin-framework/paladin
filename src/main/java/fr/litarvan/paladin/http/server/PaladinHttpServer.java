package fr.litarvan.paladin.http.server;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PaladinHttpServer
{
    void start() throws IOException;
    void loadSSLCert(File file, char[] secret) throws GeneralSecurityException, IOException;
    void waitFor() throws InterruptedException;
    void shutdown();

    String getAddress();
}
