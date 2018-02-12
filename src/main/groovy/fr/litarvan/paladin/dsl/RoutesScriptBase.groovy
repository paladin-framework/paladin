package fr.litarvan.paladin.dsl

import fr.litarvan.paladin.http.Controller

abstract class RoutesScriptBase extends Script
{
    void get(String path, Closure action)
    {

    }

    void post(String path, Closure action)
    {

    }

    void put(String path, Closure action)
    {

    }

    void delete(String path, Closure action)
    {

    }

    void all(String path, List<Closure> actions)
    {

    }

    void group(String path, Closure action)
    {

    }

    def <T extends Controller> T controller(Class<T> controller)
    {
        return null
    }
}
