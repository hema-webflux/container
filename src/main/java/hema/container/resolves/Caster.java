package hema.container.resolves;

import hema.container.BindingResolutionException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

interface Caster<T extends AnnotatedElement> {

    default Object castValue(Class<?> clazz, String value) throws NumberFormatException {

        if (isInt(clazz)) {
            return Integer.parseInt(value);
        } else if (isLong(clazz)) {
            return Long.parseLong(value);
        } else if (isFloat(clazz)) {
            return Float.parseFloat(value);
        } else if (isDouble(clazz)) {
            return Double.parseDouble(value);
        } else if (isBoolean(clazz)) {
            return Boolean.parseBoolean(value);
        } else if (isShort(clazz)) {
            return Short.parseShort(value);
        } else if (isByte(clazz)) {
            return Byte.parseByte(value);
        }

        return value;
    }

    default boolean canAutoBoxing(Class<?> parameter, Class<?> resolved) {
        return (isInt(parameter) || isInt(resolved)) ||
                (isLong(parameter) || isLong(resolved)) ||
                (isFloat(parameter) || isFloat(resolved)) ||
                (isDouble(parameter) || isDouble(resolved)) ||
                (isShort(parameter) || isShort(resolved)) ||
                (isBoolean(parameter) || isBoolean(resolved)) ||
                (isByte(parameter) || isByte(resolved));
    }

    private boolean isInt(Class<?> kind) {
        return kind.equals(int.class) || kind.equals(Integer.class);
    }

    private boolean isLong(Class<?> kind) {
        return kind.equals(long.class) || kind.equals(Long.class);
    }

    private boolean isFloat(Class<?> kind) {
        return kind.equals(float.class) || kind.equals(Float.class);
    }

    private boolean isDouble(Class<?> kind) {
        return kind.equals(double.class) || kind.equals(Float.class);
    }

    private boolean isShort(Class<?> kind) {
        return kind.equals(short.class) || kind.equals(Short.class);
    }

    private boolean isBoolean(Class<?> kind) {
        return kind.equals(boolean.class) || kind.equals(Boolean.class);
    }

    private boolean isByte(Class<?> kind) {
        return kind.equals(byte.class) || kind.equals(Byte.class);
    }

    private boolean isChar(Class<?> kind) {
        return kind.equals(char.class) || kind.equals(Character.class);
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
