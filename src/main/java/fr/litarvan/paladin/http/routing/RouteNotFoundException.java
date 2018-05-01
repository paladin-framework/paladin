package fr.litarvan.paladin.http.routing;

public class RouteNotFoundException extends RequestException
{
    public RouteNotFoundException(String message)
    {
        super(message);
    }
}
