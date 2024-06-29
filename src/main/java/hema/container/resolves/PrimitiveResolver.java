package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

class PrimitiveResolver implements Resolver, Caster<Parameter> {

    private final Resolver resolver;

    PrimitiveResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object value = resolver.resolve(concrete, parameter, datasource);

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
}
