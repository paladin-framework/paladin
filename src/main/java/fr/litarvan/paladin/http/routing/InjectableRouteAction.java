package fr.litarvan.paladin.http.routing;

import fr.litarvan.paladin.Session;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

import java.lang.annotation.Annotation;

public abstract class InjectableRouteAction implements RouteAction
{
    private String[] requestParams = null;
    private String[] optionalParams = null;

    @Override
    public Object call(Request request, Response response) throws Exception
    {
        Class[] types = getTypes();
        Object[] results = new Object[types.length];

        if (requestParams == null && optionalParams == null)
        {
            Annotation[] annotations = getAnnotations();

            for (Annotation annotation : annotations)
            {
                if (annotation.annotationType() == RequestParams.class)
                {
                    requestParams = ((RequestParams) annotation).required();
                    optionalParams = ((RequestParams) annotation).optional();
                    break;
                }
            }
        }

        int i = 0;

        if (requestParams != null)
        {
            for (; i < requestParams.length; i++)
            {
                String name = requestParams[i];
                String result = request.getParam(name);

                if (result == null)
                {
                    boolean optional = false;

                    for (String param : optionalParams)
                    {
                        if (param.equalsIgnoreCase(name))
                        {
                            optional = true;
                            break;
                        }
                    }

                    if (!optional)
                    {
                        throw new ParameterMissingException("Missing parameter '" + name + "'");
                    }
                }

                if (result == null)
                {
                    results[i] = null;
                }
                else if (types[i] == Object.class || types[i] == String.class)
                {
                    results[i] = result;
                }
                else if (types[i] == byte.class)
                {
                    try
                    {
                        results[i] = Byte.parseByte(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Byte.SIZE + "-bits integer number");
                    }
                }
                else if (types[i] == short.class)
                {
                    try
                    {
                        results[i] = Short.parseShort(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Short.SIZE + "-bits integer number");
                    }
                }
                else if (types[i] == int.class)
                {
                    try
                    {
                        results[i] = Integer.parseInt(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Integer.SIZE + "-bits integer number");
                    }
                }
                else if (types[i] == float.class)
                {
                    try
                    {
                        results[i] = Float.parseFloat(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Float.SIZE + "-bits floating number");
                    }
                }
                else if (types[i] == long.class)
                {
                    try
                    {
                        results[i] = Long.parseLong(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Long.SIZE + "-bits integer number");
                    }
                }
                else if (types[i] == double.class)
                {
                    try
                    {
                        results[i] = Double.parseDouble(result);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ParameterFormatException("Parameter '" + name + "' should be a " + Double.SIZE + "-bits floating number");
                    }
                }
            }
        }

        Session session = request.getSession();

        for (; i < results.length; i++)
        {
            Class type = types[i];
            Object result = null;

            if (type == Session.class)
            {
                result = session;
            }
            else if (type == Request.class)
            {
                result = request;
            }
            else if (type == Response.class)
            {
                result = response;
            }

            if (result == null)
            {
                result = session.get(type);
            }

            if (result == null)
            {
                try
                {
                    result = request.getPaladin().getInjector().getInstance(type);
                }
                catch (Exception ignored)
                {
                }
            }

            results[i] = result;
        }

        return call(results);
    }

    protected abstract Object call(Object[] args) throws Exception;
    protected abstract Class[] getTypes();
    protected abstract Annotation[] getAnnotations();
}
