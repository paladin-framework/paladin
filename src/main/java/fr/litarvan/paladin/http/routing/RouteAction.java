package fr.litarvan.paladin.http.routing;

import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

@FunctionalInterface
public interface RouteAction
{
    Object call(Request request, Response response) throws Exception;
}
