package hema.container.resolves;

import hema.container.*;
import hema.web.inflector.Inflector;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ResolverFactory implements Function<Parameter, Resolver> {

    private final ApplicationContext context;

    public ResolverFactory(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Resolver apply(Parameter parameter) {
        Resolver query = context.getBean(Resolver.class);

        Resolver resolverFacade = null;

        if (parameter.getType().isEnum()) {
            resolverFacade = new EnumResolver(query, context.getBean(Inflector.class));
        } else if (PrimitiveResolver.isPrimitive(parameter.getType())) {
            resolverFacade = new PrimitiveResolver(query);
        } else if (parameter.getType().isArray()) {
            resolverFacade = new ArrayResolver(query);
        } else if (parameter.getType().equals(Map.class)) {
            resolverFacade = new MapResolver(query);
        } else if (ClassResolver.isDeclaredClass(parameter.getType())) {
            resolverFacade = new ClassResolver(context, context.getBean(Container.class), new MapResolver(query));
        }

        if (Objects.isNull(resolverFacade)) {
            throw new ResolveException("Can't resolve " + parameter.getType().getName());
        }

        return resolverFacade;
    }
}
