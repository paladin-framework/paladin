package fr.litarvan.paladin.http.routing;

public class ParameterMissingException extends Exception
{
    public ParameterMissingException(String message)
    {
        super(message);
    }
}
