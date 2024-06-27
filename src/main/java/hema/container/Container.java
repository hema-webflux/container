package hema.container;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Map;

public interface Container {

    /**
     * Create instance.
     *
     * @param clazz      Resolve class.
     * @param parameters Datasource: mysql or request data collection.
     *
     * @return Clazz instance.
     *
     * @throws BindingResolutionException .
     */
    <T> T make(Class<T> clazz, Map<String, Object> parameters) throws BindingResolutionException;

    /**
     * Define a parameter alias binding.
     * @param concrete
     * @return Aliasable
     */
    <T> Replacer when(Class<T> concrete);

    /**
     * When there are multiple constructors, look for the default constructor with @Autowired annotations.
     *
     * @param constructors Reflector constructor collections.
     *
     * @return Constructor or null.
     */
    default Constructor<?> findDefaultConstructor(final Constructor<?>[] constructors) {

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
