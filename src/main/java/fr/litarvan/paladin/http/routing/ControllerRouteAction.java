package fr.litarvan.paladin.http.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ControllerRouteAction extends InjectableRouteAction
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private Controller controller;
    private Method method;

    private Class[] types;
    private Annotation[] annotations;

    public ControllerRouteAction(Controller controller, Method method)
    {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public Object call(Request request, Response response) throws Exception
    {
        if (method.isAnnotationPresent(JsonBody.class)) {
            String content = request.getContentString();
            JsonNode tree = mapper.readTree(content);

            tree.fields().forEachRemaining(entry -> {
                request.getParams().put(entry.getKey(), entry.getValue().toString());
            });
        }

        return super.call(request, response);
    }

    @Override
    protected Object call(Object[] args) throws Exception
    {
        try
        {
            return method.invoke(controller, args);
        }
        catch (InvocationTargetException e)
        {
            throw (Exception) e.getCause();
        }
    }

    @Override
    protected Class[] getTypes()
    {
        return types == null ? types = method.getParameterTypes() : types;
    }

    @Override
    protected Annotation[] getAnnotations()
    {
        return annotations == null ? annotations = method.getDeclaredAnnotations() : annotations;
    }
}
