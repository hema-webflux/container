package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

class PrimitiveResolver implements Resolver {

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

    private Object castValueToNumber(final Parameter parameter, String value) throws BindingResolutionException {
        try {
            return switch (parameter.getType().getName()) {
                case "int", "java.lang.Integer" -> Integer.parseInt(value);
                case "long", "java.lang.Long" -> Long.parseLong(value);
                case "float", "java.lang.Float" -> Float.parseFloat(value);
                case "double", "java.lang.Double" -> Double.parseDouble(value);
                case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
                case "short", "java.lang.Short" -> Short.parseShort(value);
                case "byte", "java.lang.Byte" -> Byte.parseByte(value);
                default -> new BindingResolutionException(String.format(
                        "Unresolvable dependency resolving [%s] in class [%s]",
                        parameter.getName(),
                        parameter.getClass().getName())
                );
            };
        } catch (NumberFormatException e) {
            return getDefaultValue(parameter);
        }
    }

    private Object getDefaultValue(final Parameter parameter) {
        return switch (parameter.getType().getName()) {
            case "int", "java.lang.Integer", "short", "java.lang.Short", "byte", "java.lang.Byte" -> 0;
            case "long", "java.lang.Long" -> 0L;
            case "float", "java.lang.Float" -> 0f;
            case "double", "java.lang.Double" -> 0D;
            case "boolean", "java.lang.Boolean" -> false;
            default -> "";
        };
    }
}
