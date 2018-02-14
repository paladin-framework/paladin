package fr.litarvan.paladin.dsl

import fr.litarvan.paladin.http.Controller

abstract class RoutesScriptBase extends Script
{
    void get(String path, Closure action)
    {

    }

    void get(String path, Controller controller)
    {

    }

    void post(String path, Closure action)
    {

    }

    void post(String path, Controller controller)
    {

    }

    void put(String path, Closure action)
    {

    }

    void put(String path, Controller controller)
    {

    }

    void delete(String path, Closure action)
    {

    }

    void delete(String path, Controller controller)
    {

    }

    void all(String path, List<Closure> actions)
    {

    }

    void all(String path, Controller controller)
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
