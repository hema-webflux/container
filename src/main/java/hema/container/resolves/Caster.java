package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

interface Caster<T extends AnnotatedElement> {

    default Object castValue(Class<?> clazz, String value) throws NumberFormatException {

        if (clazz == int.class || clazz == Integer.class) {
            return Integer.parseInt(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return Long.parseLong(value);
        } else if (clazz == float.class || clazz == Float.class) {
            return Float.parseFloat(value);
        } else if (clazz == double.class || clazz == Double.class) {
            return Double.parseDouble(value);
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (clazz == short.class || clazz == Short.class) {
            return Short.parseShort(value);
        } else if (clazz == byte.class || clazz == Byte.class) {
            return Byte.parseByte(value);
        }

        return value;
    }

    Object castValueToNumber(T clazz, String value) throws BindingResolutionException;


    default Object getDefaultValue(final Parameter parameter) {
        return switch (parameter.getType().getName()) {
            case "int", "java.lang.Integer", "short", "java.lang.Short", "byte", "java.lang.Byte" -> 0;
            case "long", "java.lang.Long" -> 0L;
            case "float", "java.lang.Float" -> 0f;
            case "double", "java.lang.Double" -> 0D;
            case "boolean", "java.lang.Boolean" -> false;
            default -> "";
        };
    }

}
