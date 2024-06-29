package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;

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

        Object array = resolver.resolve(concrete, parameter, datasource);

        if (array instanceof String) {

            if (isStringArray((String) array)) {
                return make((String) array);
            } else if (!isStringArray((String) array) && isSplit((String) array)) {

                Class<?> type     = parameter.getType().getComponentType();
                String[] elements = array.toString().split(",");

                if (resolverFactory.isPrimitive(type)) {
                    return make(new Tuple<>() {
                        @Override
                        public Class<?> getLeft() {
                            return type;
                        }

                        @Override
                        public String[] getRight() {
                            return elements;
                        }
                    });
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

    String[] make(String value) {
        return value.substring(1, value.length() - 1).split(",");
    }

    Object make(Tuple<Class<?>, String[]> tuple) {

        Object carry = Array.newInstance(tuple.getLeft(), tuple.getRight().length);

        IntStream.range(0, tuple.getRight().length).forEach(index -> {
            Array.set(carry, index, castValueToNumber(tuple.getLeft(), tuple.getRight()[index]));
        });

        return carry;
    }

    interface Tuple<L, R> {
        L getLeft();

        R getRight();
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
