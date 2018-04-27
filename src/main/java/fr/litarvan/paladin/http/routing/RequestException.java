package fr.litarvan.paladin.http.routing;

public class RequestException extends Exception
{
    public RequestException(String message)
    {
        super(message);
    }

    public RequestException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
