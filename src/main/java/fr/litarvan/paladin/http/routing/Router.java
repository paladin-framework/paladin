package fr.litarvan.paladin.http.routing;

import fr.litarvan.paladin.Paladin;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.HttpMethod;
import fr.litarvan.paladin.http.Request;
import groovy.lang.Closure;
import groovy.lang.GString;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Router
{
    private Paladin paladin;
    private List<Route> routes;

    public Router(Paladin paladin)
    {
        this.paladin = paladin;
        this.routes = new ArrayList<>();
    }

    public Route match(Request request)
    {
        // TODO: Implement correctly !

        for (Route route : routes)
        {
            if (route.getPath().equalsIgnoreCase(request.getUri()) && route.getMethod() == request.getMethod())
            {
                return route;
            }
        }

        return null;
    }

    public RouteAction createAction(HttpMethod httpMethod, String path, Object action)
    {
        if (action == null)
        {
            throw new IllegalArgumentException("No action defined for route " + httpMethod + " " + path);
        }

        if (action instanceof Closure)
        {
            return new ClosureRouteAction((Closure) action);
        }

        if (action instanceof RouteAction)
        {
            return (RouteAction) action;
        }

        Controller controller = null;
        String method = null;

        if (action instanceof String || action instanceof GString)
        {
            String[] split = action.toString().split(":");
            String name = split[0];

            controller = paladin.getController(name);
            method = split.length > 1 ? split[1] : null;

            if (controller == null)
            {
                throw new IllegalArgumentException("Can't find controller with name '" + name + "'");
            }
        }

        if (action instanceof Controller)
        {
            controller = (Controller) action;
        }

        if (controller == null)
        {
            throw new IllegalArgumentException("Unexpected action type '" + action.getClass().getName() + "'");
        }

        if (method == null)
        {
            method = path.substring(path.lastIndexOf('/') + 1, path.length());
        }

        Method result = null;
        String secondName = httpMethod.name().toLowerCase() + Character.toUpperCase(method.charAt(0)) + method.substring(1);

        for (Method m : controller.getClass().getMethods())
        {
            if (m.getName().equals(method) || m.getName().equals(secondName))
            {
                result = m;
            }
        }

        if (result == null)
        {
            String className = controller.getClass().getName();
            throw new IllegalArgumentException("Can't find method " + className + "#" + method + " or " + className + "#" + secondName);
        }

        return new ControllerRouteAction(controller, result);
    }

    public void register(Route route)
    {
        this.routes.add(route);
    }

    public List<Route> getRoutes()
    {
        return routes;
    }
}
