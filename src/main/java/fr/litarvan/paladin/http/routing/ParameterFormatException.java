package fr.litarvan.paladin.http.routing;

public class ParameterFormatException extends Exception
{
    public ParameterFormatException(String message)
    {
        super(message);
    }

    public ParameterFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
