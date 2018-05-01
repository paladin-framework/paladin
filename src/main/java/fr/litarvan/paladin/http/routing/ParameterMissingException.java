package fr.litarvan.paladin.http.routing;

public class ParameterMissingException extends RequestException
{
    public ParameterMissingException(String message)
    {
        super(message);
    }
}
