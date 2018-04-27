package fr.litarvan.paladin.http.routing;

public class InjectionException extends Exception
{
    public InjectionException(String message)
    {
        super(message);
    }

    public InjectionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
