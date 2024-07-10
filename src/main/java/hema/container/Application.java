package hema.container;

import hema.container.resolves.Resolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

class Application implements Container {

    private final ReplacerBindingBuilder aliasable;

    private final Function<Parameter, Resolver> builder;

    public Application(ReplacerBindingBuilder aliasBinding, Function<Parameter, Resolver> builder) {
        this.aliasable = aliasBinding;
        this.builder = builder;
    }

    @Override
    public <T> Replacer when(Class<T> concrete) {
        return aliasable.addConcreteBinding(concrete.getName());
    }

    /**
     * Resolve the give type from the container.
     *
     * @param reflect    Resolve class.
     * @param parameters Datasource: mysql or request data collection.
     *
     * @return Object
     *
     * @throws BindingResolutionException -
     */
    @Override
    public <T> T make(Class<T> reflect, Map<String, Object> parameters) throws BindingResolutionException {

        Constructor<?> constructor = resolveDefaultConstructor(reflect.getDeclaredConstructors());

        return build(reflect, parameters, constructor);
    }

    private <T> T build(Class<T> reflect, Map<String, Object> parameters, Constructor<?> constructor) throws BindingResolutionException {

        if (Objects.isNull(constructor)) {
            throw new BindingResolutionException("No default constructor found.");
        }

        try {

            List<Object> instances = Stream.of(constructor.getParameters())
                    .map(dependency -> builder.apply(dependency).resolve(reflect, dependency, parameters))
                    .toList();

            return reflect.cast(constructor.newInstance(instances.toArray()));
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
    private Constructor<?> resolveDefaultConstructor(Constructor<?>[] constructors) {

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
}
