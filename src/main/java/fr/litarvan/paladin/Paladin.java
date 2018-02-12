package fr.litarvan.paladin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import fr.litarvan.paladin.dsl.RoutesScriptBase;

public class Paladin
{
    private static Paladin paladin;

    private Paladin()
    {
    }

    public static Paladin get()
    {
        if (paladin == null)
        {
            paladin = new Paladin();
        }

        return paladin;
    }

    public Paladin routes(String path)
    {
        try
        {
            return routes(new File(Paladin.class.getResource((path.startsWith("/") ? "" : "/") + path).toURI()));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Can't find given routes file '" + path + "'");
        }
    }

    public Paladin routes(File script)
    {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(RoutesScriptBase.class.getName());

        GroovyShell shell = new GroovyShell(config);
        try
        {
            shell.evaluate(script);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (CompilationFailedException e)
        {
            e.printStackTrace();
        }

        return this;
    }

    public void start()
    {
        System.out.println("Hey");
    }
}
