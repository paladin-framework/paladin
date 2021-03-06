package fr.litarvan.paladin.dsl

import fr.litarvan.paladin.http.HttpMethod
import fr.litarvan.paladin.http.Middleware
import fr.litarvan.paladin.http.routing.Route
import fr.litarvan.paladin.http.routing.Router

abstract class RoutesScriptBase extends Script
{
    def groupStack = new ArrayList()

    void get(String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        route(HttpMethod.GET, path, actionOrOptions, options)
    }

    void post(String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        route(HttpMethod.POST, path, actionOrOptions, options)
    }

    void put(String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        route(HttpMethod.PUT, path, actionOrOptions, options)
    }

    void delete(String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        route(HttpMethod.DELETE, path, actionOrOptions, options)
    }

    void all(String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        get(path, actionOrOptions, options)
        post(path, actionOrOptions, options)
        put(path, actionOrOptions, options)
        delete(path, actionOrOptions, options)
    }

    void group(pathOrBody = null, body, Map<String, ?> options = [:])
    {
        if (pathOrBody instanceof Closure) {
            options = body
            body = pathOrBody
        }

        if (pathOrBody != null)
        {
            options['path'] = pathOrBody
        }

        groupStack.add(options)
        body.call()
        groupStack.remove(groupStack.size() - 1)
    }

    void route(HttpMethod method, String path, actionOrOptions = null, Map<String, ?> options = [:])
    {
        if (actionOrOptions instanceof Map)
        {
            options = actionOrOptions
        }
        else if (actionOrOptions != null)
        {
            options['action'] = actionOrOptions
        }

        route(method, path, options)
    }

    void route(HttpMethod method, String path, Map<String, ?> options)
    {
        def resultPath = ""
        groupStack.forEach({
            if (it['path'] != null)
            {
                resultPath += it['path']
            }

            options = it + options
        })

        resultPath += path

        if (options['middleware'] instanceof String) {
            options['middleware'] = [options['middleware'] as String]
        }

        def router = (getProperty('router') as Router)

        def action = (getProperty("router") as Router).createAction(method, resultPath, options['action'])
        def middlewares = (options['middleware'] as String[] ?: new String[0])
            .collect({ router.paladin.getMiddleware(it) })
            .toArray(new Middleware[0])

        router.register new Route(method, resultPath, middlewares, action)
    }
}
