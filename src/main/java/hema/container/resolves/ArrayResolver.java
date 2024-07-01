package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;

class ArrayResolver implements Resolver, Caster<Class<?>> {

    private final Resolver resolver;

    private final ResolverFactory resolverFactory;

    public ArrayResolver(Resolver resolver, ResolverFactory resolverFactory) {
        this.resolver = resolver;
        this.resolverFactory = resolverFactory;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object resolved = resolver.resolve(concrete, parameter, datasource);

        if (resolved instanceof String) {

            if (isStringArray((String) resolved)) {
                String stringArray = (String) resolved;
                return stringArray.substring(1, stringArray.length() - 1).split(",");
            } else if (!isStringArray((String) resolved) && isSplit((String) resolved)) {

                Class<?> type     = parameter.getType().getComponentType();
                String[] elements = resolved.toString().split(",");

                if (resolverFactory.isPrimitive(type)) {
                    Object carry = Array.newInstance(type, elements.length);
                    IntStream.range(0, elements.length).forEach(index -> {
                        Array.set(carry, index, castValueToNumber(type, elements[index]));
                    });
                    return carry;
                }

                return elements;
            }

        }

        if (resolved instanceof Collection<?>) {
            return ((Collection<?>) resolved).toArray();
        }

        if (!(resolved instanceof Object[])) {
            throw new BindingResolutionException("A Array text must begin with '[' ");
        }

        return resolved;
    }

    private boolean isStringArray(String value) {
        return (value.startsWith("[") && value.endsWith("]")) || value.matches("^\\\\[.*\\\\]$");
    }

    private boolean isSplit(String value) {
        return value.contains(",") || value.contains("|");
    }

    @Override
    public Object castValueToNumber(Class<?> clazz, String value) throws BindingResolutionException {

        Object result;

        try {
            result = castValue(clazz, value);
        } catch (NumberFormatException e) {
            throw new BindingResolutionException("Cannot cast " + value + " to " + clazz.getSimpleName());
        }

        return result;
    }
}
