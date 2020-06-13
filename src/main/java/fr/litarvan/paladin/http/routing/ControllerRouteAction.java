package fr.litarvan.paladin.http.routing;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.litarvan.paladin.http.Controller;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

public class ControllerRouteAction extends InjectableRouteAction
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private Controller controller;
    private Method method;
    private JsonBody jsonBody;

    private Class[] types;
    private Annotation[] annotations;

    public ControllerRouteAction(Controller controller, Method method)
    {
        this.controller = controller;
        this.method = method;

        if (method.isAnnotationPresent(JsonBody.class)) {
            this.jsonBody = method.getAnnotation(JsonBody.class);
        }
    }

    @Override
    public Object call(Request request, Response response) throws Exception
    {
        if (jsonBody != null) {
            String content = request.getContentString();
            JsonNode tree = mapper.readTree(content);

            if (tree != null) {
                if (jsonBody.parse()) {
                    tree.fields().forEachRemaining(entry -> {
                        JsonNode value = entry.getValue();
                        request.getParams().put(entry.getKey(), value.isTextual() ? value.textValue() : value.toString());
                    });
                } else {
                    request.getParams().put("__jBdy", tree.toString());
                }

            }
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
