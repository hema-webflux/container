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
