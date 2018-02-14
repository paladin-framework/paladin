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

    void get(String path, String controller)
    {

    }

    void get(String path)
    {

    }

    void post(String path, Closure action)
    {

    }

    void post(String path, Controller controller)
    {

    }

    void post(String path, String controller)
    {

    }

    void post(String path)
    {

    }

    void put(String path, Closure action)
    {

    }

    void put(String path, Controller controller)
    {

    }

    void put(String path, String controller)
    {

    }

    void put(String path)
    {

    }

    void delete(String path, Closure action)
    {

    }

    void delete(String path, Controller controller)
    {

    }

    void delete(String path, String controller)
    {

    }

    void delete(String path)
    {

    }

    void all(String path, List<Closure> actions)
    {

    }

    void all(String path, Controller controller)
    {

    }

    void all(String path, String controller)
    {

    }

    void all(String path)
    {

    }

    void group(String path, Closure action)
    {

    }

    void group(String path, Closure action, Map<String, ?> options)
    {

    }

    def <T extends Controller> T controller(Class<T> type)
    {
        return null
    }

    def <T extends Controller> T controller(String name)
    {
        return null
    }
}
