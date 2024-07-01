package hema.container;

import hema.container.resolves.Resolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

class Application implements Container, Resolver {

    private final ReplacerBinding aliasable;

    private final Factory<Resolver, Parameter> factory;

    Application(ReplacerBinding aliasBinding, Factory<Resolver, Parameter> factory) {
        this.aliasable = aliasBinding;
        this.factory = factory;
    }

    @Override
    public <T> Replacer when(Class<T> concrete) {
        return aliasable.addConcreteBinding(concrete.getName());
    }

    /**
     * Resolve the give type from the container.
     *
     * @param clazz      Resolve class.
     * @param parameters Datasource: mysql or request data collection.
     *
     * @return Object
     *
     * @throws BindingResolutionException -
     */
    @Override
    public <T> T make(Class<T> clazz, Map<String, Object> parameters) throws BindingResolutionException {
        return resolveConstructor(clazz, parameters);
    }

    private <T> T resolveConstructor(Class<T> concrete, Map<String, Object> parameters) throws BindingResolutionException {

        Constructor<?> constructor = findDefaultConstructor(concrete.getDeclaredConstructors());

        if (Objects.isNull(constructor)) {
            throw new BindingResolutionException("No default constructor found.");
        }

        try {

            List<Object> instances = Stream.of(constructor.getParameters())
                    .map(dependency -> resolve(concrete, dependency, parameters))
                    .toList();

            return concrete.cast(constructor.newInstance(instances.toArray()));
        } catch (InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | BindingResolutionException e) {
            throw new BindingResolutionException(e.getMessage());
        }
    }

    /**
     * When there are multiple constructors, look for the default constructor with @Autowired annotations.
     *
     * @param constructors Reflector constructor collections.
     *
     * @return Constructor or null.
     */
    private Constructor<?> findDefaultConstructor(Constructor<?>[] constructors) {

        if (constructors.length == 1) {
            return constructors[0];
        }

        for (Constructor<?> constructor : constructors) {
            Annotation annotation = constructor.getAnnotation(Autowired.class);
            if (annotation != null) {
                return constructor;
            }
        }

        return null;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {
        return factory.make(parameter).resolve(concrete, parameter, datasource);
    }
}
