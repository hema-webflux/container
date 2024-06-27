package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;

class ArrayResolver implements Resolver {

    private final Resolver resolver;

    public ArrayResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object array = resolver.resolve(concrete, parameter, datasource);

        if (array instanceof String) {

            if (isStringArray((String) array)) {
                return ((String) array).substring(1, ((String) array).length() - 1).split(",");
            }

            if (!isStringArray((String) array) && isSplit((String) array)) {
                return ((String) array).split(",");
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
