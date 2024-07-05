package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

class ArrayResolver implements Resolver, Caster<Class<?>, String> {

    private final Resolver resolver;

    public ArrayResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object resolved = resolver.resolve(concrete, parameter, datasource);

        if (resolved instanceof String) {

            if (isStringArray((String) resolved)) {
                String stringArray = (String) resolved;
                return stringArray.substring(1, stringArray.length() - 1).split(",");
            } else if (!isStringArray((String) resolved) && isSplit((String) resolved)) {

                Class<?> reflect = parameter.getType().getComponentType();

                if (PrimitiveResolver.isPrimitive(reflect)) {
                    return make(reflect, resolved.toString().split(","), (value) -> castValue(reflect, (String) value));
                }

                return resolved.toString().split(",");
            }

        }

        if (resolved instanceof Collection<?>) {
            return ((Collection<?>) resolved).toArray();
        }

        if (canAutoBoxing(parameter, resolved)) {
            return make(parameter.getType().getComponentType(), (Object[]) resolved, (value) -> value);
        }

        if (ClassResolver.isDeclaredClass(parameter.getType().getComponentType())) {

        }

        return resolved;
    }

    <T> Object make(Class<?> reflect, T[] value, Function<Object, Object> closure) {
        Object elements = Array.newInstance(reflect, value.length);

        IntStream.range(0, value.length).forEach(i -> Array.set(elements, i, closure.apply(value[i])));

        return elements;
    }

    boolean canAutoBoxing(Parameter parameter, Object resolved) {

        Class<?> parameterType = parameter.getType().getComponentType();
        Class<?> resolvedType  = resolved.getClass().getComponentType();

        return (isInt(parameterType) || isInt(resolvedType)) ||
                (isLong(parameterType) || isLong(resolvedType)) ||
                (isFloat(parameterType) || isFloat(resolvedType)) ||
                (isDouble(parameterType) || isDouble(resolvedType)) ||
                (isShort(parameterType) || isShort(resolvedType)) ||
                (isBoolean(parameterType) || isBoolean(resolvedType)) ||
                (isByte(parameterType) || isByte(resolvedType));
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
