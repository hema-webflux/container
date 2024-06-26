package hema.container;

import java.lang.reflect.Parameter;
import java.util.Set;

class ResolverFactory implements Factory {

    private Application container;

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

    @Override
    public Resolver make(Parameter parameter) {

        if (isPrimitive(parameter)) {
            return container::resolvePrimitive;
        } else if (isDeclaredClass(parameter)) {
            return container::resolveClass;
        } else if (parameter.getType().isEnum()) {
            return container::resolveEnum;
        }

        throw new RuntimeException();
    }

    @Override
    public void setContainer(Container container) {
        this.container = (Application) container;
    }

    /**
     * Determines whether a given parameter type is a custom class or not.
     *
     * @param parameter Constructor parameter object.
     *
     * @return boolean
     */
    private boolean isDeclaredClass(final Parameter parameter) {
        return (isCustomClass(parameter) || parameter.getType().isInterface()) && !parameter.getType().isEnum();
    }

    private boolean isCustomClass(final Parameter parameter) {
        return !parameter.getType().isPrimitive() && !parameter.getType().getName().startsWith("java.lang");
    }

    /**
     * Determines if this Class object represents a primitive type or void.
     *
     * @param parameter Constructor parameter object.
     *
     * @return boolean
     */
    private boolean isPrimitive(Parameter parameter) {
        return parameter.getType().isPrimitive() || standardTypes.contains(parameter.getType().getName());
    }
}
