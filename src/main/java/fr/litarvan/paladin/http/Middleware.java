package fr.litarvan.paladin.http;

import fr.litarvan.paladin.AfterEvent;
import fr.litarvan.paladin.BeforeEvent;
import fr.litarvan.paladin.http.routing.Route;

public abstract class Middleware
{
    public abstract void before(BeforeEvent event, Request request, Response response, Route route);
    public abstract void after(AfterEvent event, Request request, Response response, Route route);
}
