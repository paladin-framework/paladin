package fr.litarvan.paladin.http.routing;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import fr.litarvan.paladin.Session;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

public abstract class InjectableRouteAction implements RouteAction
{
    private String[] requiredParams = null;
    private String[] optionalParams = null;

    @Override
    public Object call(Request request, Response response) throws Exception
    {
        Class[] types = getTypes();
        Object[] results = new Object[types.length];

        if (requiredParams == null && optionalParams == null)
        {
            Annotation[] annotations = getAnnotations();

            for (Annotation annotation : annotations)
            {
                if (annotation.annotationType() == RequestParams.class)
                {
                    requiredParams = ((RequestParams) annotation).required();
                    optionalParams = ((RequestParams) annotation).optional();
                    break;
                }
            }
        }

        int i = 0;

        if (requiredParams != null)
        {
            for (; i < requiredParams.length + optionalParams.length; i++)
            {
                boolean optional = i >= requiredParams.length;

                String name = !optional ? requiredParams[i] : optionalParams[i - requiredParams.length];

                Class optType = null;

                if (optional && name.contains(":")) {
                    String[] split = name.split(":");

                    optType = parseClass(split[0]);
                    name = split[1];

                }
                
                String result = request.getParam(name);

                if (result == null && !optional)
                {
                    throw new ParameterMissingException("Missing parameter '" + name + "'");
                }

                results[i] = parse(name, result, optType != null ? optType : types[i]);

                if (types[i] == Optional.class)
                {
                    results[i] = Optional.ofNullable(results[i]);
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
    
    protected Object parse(String name, String value, Class type) throws ParameterFormatException, IllegalArgumentException
    {
        if (value == null)
        {
            return null;
        }
        else if (type == Object.class || type == String.class)
        {
            return value;
        }
        else if (type == byte.class)
        {
            try
            {
                return Byte.parseByte(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Byte.SIZE + "-bits integer number");
            }
        }
        else if (type == short.class)
        {
            try
            {
                return Short.parseShort(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Short.SIZE + "-bits integer number");
            }
        }
        else if (type == int.class)
        {
            try
            {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Integer.SIZE + "-bits integer number");
            }
        }
        else if (type == float.class)
        {
            try
            {
                return Float.parseFloat(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Float.SIZE + "-bits floating number");
            }
        }
        else if (type == long.class)
        {
            try
            {
                return Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Long.SIZE + "-bits integer number");
            }
        }
        else if (type == double.class)
        {
            try
            {
                return Double.parseDouble(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' should be a " + Double.SIZE + "-bits floating number");
            }
        }
        else if (type == BigDecimal.class)
        {
            try
            {
                return new BigDecimal(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' isn't a valid decimal number");
            }
        }
        else if (type == BigInteger.class)
        {
            try
            {
                return new BigInteger(value);
            }
            catch (NumberFormatException e)
            {
                throw new ParameterFormatException("Parameter '" + name + "' isn't a valid number");
            }
        }
        else if (type.isEnum())
        {
            try
            {
                return Enum.valueOf(type, value);
            }
            catch (NumberFormatException e)
            {
            	throw new ParameterFormatException("Parameter '" + name + "' isn't a valid");
            }
        }
        else
        {
        	try
        	{
        		return type.cast(value);
        	}
        	catch (ClassCastException e)
        	{
        		throw new ParameterFormatException("Parameter '" + name + "' isn't a valid");
        	}
        }
    }

    protected Class parseClass(String name) throws ClassNotFoundException
    {
        switch (name)
        {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "bigdecimal":
            	return BigDecimal.class;
            case "biginteger":
            	return BigInteger.class;
            default:
                return Class.forName(name.contains(".") ? name : "java.lang." + name);
        }
    }

    protected abstract Object call(Object[] args) throws Exception;
    protected abstract Class[] getTypes();
    protected abstract Annotation[] getAnnotations();
}
