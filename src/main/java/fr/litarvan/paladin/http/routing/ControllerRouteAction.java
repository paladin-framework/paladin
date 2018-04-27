package fr.litarvan.paladin.http.routing;

import fr.litarvan.paladin.http.Controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ControllerRouteAction extends InjectableRouteAction
{
    private Controller controller;
    private Method method;

    public ControllerRouteAction(Controller controller, Method method)
    {
        this.controller = controller;
        this.method = method;
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
        return method.getParameterTypes();
    }

    @Override
    protected Annotation[] getAnnotations()
    {
        return method.getDeclaredAnnotations();
    }
}
