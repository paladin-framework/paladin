package fr.litarvan.paladin.http.routing;

import fr.litarvan.paladin.http.HttpMethod;
import fr.litarvan.paladin.http.Middleware;

public class Route
{
    private HttpMethod method;
    private String path;
    private Middleware[] middlewares;
    private RouteAction action;

    public Route(HttpMethod method, String path, Middleware[] middlewares, RouteAction action)
    {
        this.method = method;
        this.path = path;
        this.middlewares = middlewares;
        this.action = action;
    }

    public HttpMethod getMethod()
    {
        return method;
    }

    public String getPath()
    {
        return path;
    }

    public Middleware[] getMiddlewares()
    {
        return middlewares;
    }

    public RouteAction getAction()
    {
        return action;
    }
}
