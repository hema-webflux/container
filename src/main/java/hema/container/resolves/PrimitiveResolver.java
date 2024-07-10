package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class PrimitiveResolver implements Resolver, Caster<Parameter, String> {

    private final Resolver resolver;

    private static final Set<String> standardTypes = Set.of(
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Character",
            "java.lang.Boolean"
    );

    PrimitiveResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> reflect, Parameter parameter, Map<String, Object> datasource) {

        Object value = resolver.resolve(reflect, parameter, datasource);

        if (Objects.isNull(value)) {
            return getDefaultValue(parameter);
        }

        if (value instanceof String && match((String) value)) {
            return castValueToNumber(parameter, (String) value);
        }

        return value;
    }

    private boolean match(String value) {
        return value.matches("-?\\d+") || value.matches("-?\\d+(\\.\\d+)?") || value.matches("true|false");
    }


    @Override
    public Object castValueToNumber(Parameter parameter, String value) throws BindingResolutionException {

        Object castValue;

        try {
            castValue = castValue(parameter.getType(), value);
        } catch (NumberFormatException e) {

            String message = String.format(
                    "Unresolvable dependency resolving [%s] in class [%s]",
                    parameter.getName(),
                    parameter.getClass().getName()
            );

            throw new BindingResolutionException(message);
        }

        return castValue;
    }

    /**
     * Determines if this Class object represents a primitive type or void.
     *
     * @param kind Reflect class.
     *
     * @return boolean
     */
    static boolean isPrimitive(Class<?> kind) {
        return kind.isPrimitive() || standardTypes.contains(kind.getName());
    }
}
