package hema.container.resolves;

import hema.container.*;
import hema.container.annotation.Entity;
import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class ResolverFactory implements Factory<Resolver, Parameter> {

    private final ApplicationContext context;

    ResolverFactory(ApplicationContext context) {
        this.context = context;
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
    @SuppressWarnings("unchecked")
    public Resolver make(Parameter parameter) throws BindingResolutionException {

        Resolver query = context.getBean(Resolver.class);

        Resolver resolverFacade = null;

        if (parameter.getType().isEnum()) {
            resolverFacade = new EnumResolver(query, context.getBean(Inflector.class));
        } else if (isPrimitive(parameter.getType())) {
            resolverFacade = new PrimitiveResolver(query);
        } else if (parameter.getType().isArray()) {
            ArrayResolver.genericArrayFactory factory = new ArrayResolver.genericArrayFactory();
            resolverFacade = new ArrayResolver(query, this, factory);
            factory.caster((Caster<Class<?>>) resolverFacade);
        } else if (parameter.getType().equals(Map.class)) {
            resolverFacade = new MapResolver(query);
        } else if (isDeclaredClass(parameter)) {
            resolverFacade = new ClassResolver(context, context.getBean(Container.class), new MapResolver(query));
        }

        if (Objects.isNull(resolverFacade)) {
            throw new ResolveException("Can't resolve " + parameter.getType().getName());
        }

        return resolverFacade;
    }

    /**
     * Determines whether a given parameter type is a custom class or not.
     *
     * @param parameter Constructor parameter object.
     *
     * @return boolean
     */
    boolean isDeclaredClass(Parameter parameter) {
        return Objects.nonNull(parameter.getType().getDeclaredAnnotation(Entity.class)) || parameter.getType().isInterface();
    }

    /**
     * Determines if this Class object represents a primitive type or void.
     *
     * @param kind Reflect class.
     *
     * @return boolean
     */
    boolean isPrimitive(Class<?> kind) {
        return kind.isPrimitive() || standardTypes.contains(kind.getName());
    }
}
