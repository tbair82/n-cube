package com.cedarsoftware.ncube;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kpartlow on 8/25/2014.
 */
public abstract class AbstractPersistenceProxy implements InvocationHandler
{
    protected Object _adapter;
    protected Map<Method, Method> methods = new HashMap<Method, Method>();

    public AbstractPersistenceProxy(Class service, Object adapter) {
        _adapter = adapter;

        Method[] declaredMethods = service.getDeclaredMethods();

        //  Verify all adapted methods are available in our proxied class.
        for (Method m : declaredMethods) {
            Class[] adaptedParameters = getAdaptedParameters(m.getParameterTypes());
            try
            {
                Method adaptedMethod = adapter.getClass().getMethod(m.getName(), adaptedParameters);
                methods.put(m, adaptedMethod);
            } catch (NoSuchMethodException e) {
                String s = buildDoesNotImplementMessage(m, adaptedParameters);
                throw new IllegalArgumentException(s, e);
            }
        }
    }

    public String buildDoesNotImplementMessage(Method m, Class[] adaptedParameters) {
        StringBuilder b = new StringBuilder(String.format("Adapter class '%s' does not implement: %s(", _adapter.getClass().getSimpleName(), m.getName()));
        for (Class c : adaptedParameters) {
            b.append(c.getSimpleName());
            b.append(",");
        }
        if (adaptedParameters.length > 1) {
            b.setLength(b.length()-1);
        }
        b.append(")");
        return b.toString();
    }

    public Class[] getAdaptedParameters(Class[] classes) {
        Class[] adaptedParameters = new Class[classes.length+1];
        adaptedParameters[0] = getAddedClass();
        System.arraycopy(classes, 0, adaptedParameters, 1, classes.length);
        return adaptedParameters;
    }

    public Object[] getAdaptedArguments(Object[] args, Object addedArgument) {
        Object[] adaptedArgs = new Object[args.length+1];
        adaptedArgs[0] = addedArgument;
        System.arraycopy(args, 0, adaptedArgs, 1, args.length);
        return adaptedArgs;
    }

    public abstract Class getAddedClass();
}