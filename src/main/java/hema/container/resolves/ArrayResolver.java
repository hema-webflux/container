package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

class ArrayResolver implements Resolver, Reflector {

    private final Resolver resolver;

    private final ResolverFactory resolverFactory;

    public ArrayResolver(Resolver resolver, ResolverFactory resolverFactory) {
        this.resolver = resolver;
        this.resolverFactory = resolverFactory;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object array = resolver.resolve(concrete, parameter, datasource);

        if (array instanceof String) {

            if (isStringArray((String) array)) {
                return ((String) array).substring(1, ((String) array).length() - 1).split(",");
            }

            if (!isStringArray((String) array) && isSplit((String) array)) {

                String[] elements = ((String) array).split(",");

                Class<?> kind = parameter.getType().getComponentType();

                if (resolverFactory.isPrimitive(kind)) {

                    return Stream.of(elements).map(element -> {

                        if (isInteger(kind)) {
                            return Integer.parseInt(element);
                        } else if (isLong(kind)) {
                            return Long.parseLong(element);
                        } else if (isDouble(kind)) {
                            return Double.parseDouble(element);
                        } else if (isFloat(kind)) {
                            return Float.parseFloat(element);
                        } else if (isShort(kind)) {
                            return Short.parseShort(element);
                        } else if (isByte(kind)) {
                            return Byte.parseByte(element);
                        }

                        return element;
                    }).toArray();
                }

                return elements;
            }
        }

        if (array instanceof Collection<?>) {
            return ((Collection<?>) array).toArray();
        }

        if (!(array instanceof Object[])) {
            throw new BindingResolutionException("A Array text must begin with '[' ");
        }

        return array;
    }

    private boolean isStringArray(String value) {
        return (value.startsWith("[") && value.endsWith("]")) || value.matches("^\\\\[.*\\\\]$");
    }

    private boolean isSplit(String value) {
        return value.contains(",") || value.contains("|");
    }

}
