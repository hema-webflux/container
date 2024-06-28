package hema.container.resolves;

import hema.container.*;
import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class ResolverFactory implements Factory<Resolver, Parameter> {

    private final ApplicationContext context;

    private final Map<String, Resolver> resolvedInstance;

    ResolverFactory(ApplicationContext context, Map<String, Resolver> resolvedInstance) {
        this.context = context;
        this.resolvedInstance = resolvedInstance;
    }

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

        Resolver query = context.getBean(Resolver.class);

        Resolver resolver = null;

        if (isDeclaredClass(parameter)) {
            resolver = new ClassResolver(context, query, context.getBean(Container.class), this);
        } else if (parameter.getType().isEnum()) {
            resolver = new EnumResolver(query, context.getBean(Inflector.class));
        } else if (isPrimitive(parameter)) {
            resolver = new PrimitiveResolver(query);
        } else if (parameter.getType().isArray()) {
            resolver = new ArrayResolver(query);
        } else if (parameter.getType().equals(Map.class)) {
            resolver = new MapResolver(query);
        }

        if (Objects.isNull(resolver)) {
            throw new ResolveException("Can't resolve " + parameter.getType().getName());
        }

        if (resolvedInstance.containsKey(resolver.getFacadeAccessor())) {
            return resolvedInstance.get(resolver.getFacadeAccessor());
        }

        resolvedInstance.put(resolver.getFacadeAccessor(), resolver);

        return resolver;
    }

    /**
     * Determines whether a given parameter type is a custom class or not.
     *
     * @param parameter Constructor parameter object.
     *
     * @return boolean
     */
    boolean isDeclaredClass(final Parameter parameter) {
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
