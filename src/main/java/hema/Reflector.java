package huabanshou.tiktok.usecase.container;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

interface Reflector {

    /**
     * Determines if this Class object represents a primitive type or void.
     *
     * @param parameter Constructor parameter.
     *
     * @return boolean
     */
    default boolean isPrimitive(Parameter parameter) {
        return parameter.getType().isPrimitive() || isStandard(parameter);
    }

    /**
     * Determines that a given type is a standard type.
     *
     * @param parameter
     *
     * @return boolean
     */
    boolean isStandard(final Parameter parameter);

    /**
     * Determines whether a given parameter type is a custom class or not.
     *
     * @param parameter
     *
     * @return boolean
     */
    default boolean isDeclaredClass(final Parameter parameter) {
        return !parameter.getType().isPrimitive() && !parameter.getType().getName().startsWith("java.lang");
    }

    /**
     * When there are multiple constructors, look for the default constructor with @Autowired annotations.
     *
     * @param constructors Reflector constructor collections.
     *
     * @return Constructor or null.
     */
    default Constructor<?> findDefaultConstructor(final Constructor<?>[] constructors) {

        for (Constructor<?> constructor : constructors) {
            Annotation annotation = constructor.getAnnotation(Autowired.class);
            if (annotation != null) {
                return constructor;
            }
        }

        return null;
    }

    /**
     * Determines that the given value can be generalized to a number.
     *
     * @param value Input value.
     *
     * @return boolean.
     */
    default boolean isConvertibleToNumber(final Object value) {

        if (!(value instanceof String numberValue)) {
            return false;
        }

        return numberValue.matches("-?\\d+") || numberValue.matches("-?\\d+(\\.\\d+)?") || numberValue.matches("true|false");
    }

    default boolean isJson(final Object value) {

        if (value instanceof String) {

            try {
                new JSONObject(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

}
