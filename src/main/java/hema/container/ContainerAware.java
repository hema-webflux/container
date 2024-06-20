package hema.container;

import java.lang.reflect.Parameter;

interface ContainerAware {

    /**
     * If the target interface cannot be instantiated, an exception is throw.
     *
     * @param concrete abstract.
     */
    default void notInstantiable(final String concrete) {
        throw new BindingResolutionException(String.format("Target [%s] is not instantiable.", concrete));
    }

    default Object castAttributeAsNumber(final Parameter parameter, String value) {
        try {
            return switch (parameter.getType().getName()) {
                case "int", "java.lang.Integer" -> Integer.parseInt(value);
                case "long", "java.lang.Long" -> Long.parseLong(value);
                case "float", "java.lang.Float" -> Float.parseFloat(value);
                case "double", "java.lang.Double" -> Double.parseDouble(value);
                case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
                case "short", "java.lang.Short" -> Short.parseShort(value);
                case "byte", "java.lang.Byte" -> Byte.parseByte(value);
                default -> throw new IllegalStateException("Unexpected value: " + parameter.getName());
            };
        } catch (NumberFormatException e) {
            return getDefaultValue(parameter);
        }
    }

    /**
     * Set Default Value for Built-in Types.
     *
     * @param parameter
     *
     * @return Converted Value
     */
    default Object getDefaultValue(final Parameter parameter) {
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
