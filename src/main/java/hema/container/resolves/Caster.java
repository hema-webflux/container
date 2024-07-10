package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

interface Caster<T extends AnnotatedElement, P> {

    Object castValueToNumber(T reflect, P value) throws BindingResolutionException;

    default Object castValue(Class<?> reflect, String value) throws NumberFormatException {

        if (isInt(reflect)) {
            return Integer.parseInt(value);
        } else if (isLong(reflect)) {
            return Long.parseLong(value);
        } else if (isFloat(reflect)) {
            return Float.parseFloat(value);
        } else if (isDouble(reflect)) {
            return Double.parseDouble(value);
        } else if (isBoolean(reflect)) {
            return Boolean.parseBoolean(value);
        } else if (isShort(reflect)) {
            return Short.parseShort(value);
        } else if (isByte(reflect)) {
            return Byte.parseByte(value);
        }

        return value;
    }

    default boolean isInt(Class<?> reflect) {
        return reflect.equals(int.class) || reflect.equals(Integer.class);
    }

    default boolean isLong(Class<?> reflect) {
        return reflect.equals(long.class) || reflect.equals(Long.class);
    }

    default boolean isFloat(Class<?> reflect) {
        return reflect.equals(float.class) || reflect.equals(Float.class);
    }

    default boolean isDouble(Class<?> reflect) {
        return reflect.equals(double.class) || reflect.equals(Float.class);
    }

    default boolean isShort(Class<?> reflect) {
        return reflect.equals(short.class) || reflect.equals(Short.class);
    }

    default boolean isBoolean(Class<?> reflect) {
        return reflect.equals(boolean.class) || reflect.equals(Boolean.class);
    }

    default boolean isByte(Class<?> reflect) {
        return reflect.equals(byte.class) || reflect.equals(Byte.class);
    }

    default boolean isChar(Class<?> reflect) {
        return reflect.equals(char.class) || reflect.equals(Character.class);
    }


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
