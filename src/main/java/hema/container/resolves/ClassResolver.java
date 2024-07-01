package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Container;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;

class ClassResolver implements Resolver {

    private final ApplicationContext context;

    private final Container container;

    private final Resolver resolver;

    ClassResolver(ApplicationContext context, Container container, Resolver resolver) {
        this.context = context;
        this.container = container;
        this.resolver = resolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {
        if (concrete.isInterface()) {

            if (!context.containsBean(concrete.getName())) {
                throw new BindingResolutionException(String.format("Target [%s] is not instantiable.", concrete.getName()));
            }

            return concrete.cast(context.getBean(concrete.getName()));
        }

        if (context.containsBean(concrete.getName())) {
            Object bean = context.getBean(concrete.getName());

            if (concrete.isInstance(bean)) {
                return concrete.cast(bean);
            }
        }

        Object value = resolver.resolve(concrete, parameter, datasource);

        return container.make(parameter.getType(), (Map<String, Object>) value);
    }

}
