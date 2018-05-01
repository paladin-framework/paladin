package fr.litarvan.paladin;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.litarvan.paladin.http.Header;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;
import fr.litarvan.paladin.http.routing.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger("ExceptionHandler");

    public void handle(Exception exception, Request request, Response response)
    {
        // TODO: Save crash reports
        response.setContentType(Header.CONTENT_TYPE_JSON);

        try
        {
            response.setContentString(request.getPaladin().getJSONMapper().writeValueAsString(new ExceptionContent(exception.getClass().getSimpleName(), exception.getMessage())));
        }
        catch (JsonProcessingException e)
        {
            log.error("Couldn't serialize exception message !", e);
        }

        if (!(exception instanceof RequestException))
        {
            log.error("Exception thrown during uri call " + request.getUri(), exception);
            log.error("If you don't want this message to appear, the exception must extend RequestException");
        }

        // TODO: Proper report
    }

    public static class ExceptionContent
    {
        public final String error;
        public final String message;

        public ExceptionContent(String error, String message)
        {
            this.error = error;
            this.message = message;
        }
    }
}
