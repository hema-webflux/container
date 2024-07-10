package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Container;
import hema.container.annotation.Entity;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

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
    public <T> Object resolve(Class<T> reflect, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {
        if (reflect.isInterface()) {

            if (!context.containsBean(reflect.getName())) {
                throw new BindingResolutionException(String.format("Target [%s] is not instantiable.", reflect.getName()));
            }

            return reflect.cast(context.getBean(reflect.getName()));
        }

        if (context.containsBean(reflect.getName())) {
            Object bean = context.getBean(reflect.getName());

            if (reflect.isInstance(bean)) {
                return reflect.cast(bean);
            }
        }

        Object value = resolver.resolve(reflect, parameter, datasource);

        return container.make(parameter.getType(), (Map<String, Object>) value);
    }

    /**
     * Determines whether a given parameter type is a custom class or not.
     *
     * @param clazz Constructor parameter object.
     *
     * @return boolean
     */
    static boolean isDeclaredClass(Class<?> clazz) {
        return Objects.nonNull(clazz.getDeclaredAnnotation(Entity.class)) || clazz.isInterface();
    }

}
