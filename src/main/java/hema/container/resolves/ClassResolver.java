package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Container;
import hema.container.Resolver;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Parameter;
import java.util.Map;

class ClassResolver implements Resolver {

    private final ApplicationContext context;

    private final Resolver resolver;

    private final Container container;

    ClassResolver(ApplicationContext context, Resolver resolver, Container container) {
        this.context = context;
        this.resolver = resolver;
        this.container = container;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
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

        if (concrete.isInstance(value)) {
            return value;
        }

        if (isJsonObject(value)) {
            value = new JSONObject((String) value).toMap();
        }

        return container.make(parameter.getType(), (Map<String, Object>) value);
    }

}
