package hema.container.resolves;

import hema.container.*;
import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;

record ResolverFactory(ApplicationContext applicationContext) implements Factory<Resolver, Parameter> {

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
    public Resolver make(Parameter parameter) throws BindingResolutionException {

        Resolver resolver = applicationContext.getBean(Resolver.class);

        if (isDeclaredClass(parameter)) {
            return new ClassResolver(
                    applicationContext,
                    resolver,
                    applicationContext.getBean(Container.class)
            );
        } else if (parameter.getType().isEnum()) {
            return new EnumResolver(resolver, applicationContext.getBean(Inflector.class));
        } else if (isPrimitive(parameter)) {
            return new PrimitiveResolver(resolver);
        } else if (parameter.getType().isArray()) {
            return new ArrayResolver(resolver);
        } else if (parameter.getType().equals(Map.class)) {
            return new MapResolver(resolver);
        }

        throw new BindingResolutionException("Cannot resolve " + parameter);
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
