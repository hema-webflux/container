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

    private final Map<String, Facade> resolvedInstance;

    ResolverFactory(ApplicationContext context, Map<String, Facade> resolvedInstance) {
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

        Facade resolverFacade = null;

        if (parameter.getType().isEnum()) {
            resolverFacade = new EnumResolver(query, context.getBean(Inflector.class));
        } else if (isPrimitive(parameter)) {
            resolverFacade = new PrimitiveResolver(query);
        } else if (parameter.getType().isArray()) {
            resolverFacade = new ArrayResolver(query);
        } else if (parameter.getType().equals(Map.class)) {
            resolverFacade = new MapResolver(query);
        } else if (isDeclaredClass(parameter)) {
            resolverFacade = new ClassResolver(context, query, context.getBean(Container.class), this);
        }

        if (Objects.isNull(resolverFacade)) {
            throw new ResolveException("Can't resolve " + parameter.getType().getName());
        }

        if (resolvedInstance.containsKey(resolverFacade.getFacadeAccessor())) {
            return resolvedInstance.get(resolverFacade.getFacadeAccessor());
        }

        resolvedInstance.put(resolverFacade.getFacadeAccessor(), resolverFacade);

        return resolverFacade;
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
