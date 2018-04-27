package fr.litarvan.paladin.http.routing;

import groovy.lang.Closure;

import java.lang.annotation.Annotation;

public class ClosureRouteAction extends InjectableRouteAction
{
    private Closure closure;

    public ClosureRouteAction(Closure closure)
    {
        this.closure = closure;
    }

    @Override
    protected Object call(Object[] args) throws Exception
    {
        return closure.call(args);
    }

    @Override
    protected Class[] getTypes()
    {
        return closure.getParameterTypes();
    }

    @Override
    protected Annotation[] getAnnotations()
    {
        return new Annotation[0];
    }
}
