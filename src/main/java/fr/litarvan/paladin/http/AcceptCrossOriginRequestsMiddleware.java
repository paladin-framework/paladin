package fr.litarvan.paladin.http;

import fr.litarvan.paladin.AfterEvent;
import fr.litarvan.paladin.BeforeEvent;
import fr.litarvan.paladin.http.routing.Route;

public class AcceptCrossOriginRequestsMiddleware extends Middleware
{
    @Override
    public void before(BeforeEvent event, Request request, Response response, Route route) {}

    @Override
    public void after(AfterEvent event, Request request, Response response, Route route)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (request.getMethod() == HttpMethod.OPTIONS)
        {
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "origin, x-csrftoken, content-type, token, accept, authorization");

            event.setResult(true);
        }
    }
}
