package hema.container.resolves;

interface Reflector {

    default boolean isInteger(Class<?> kind) {
        return kind.equals(Integer.class);
    }

    default boolean isLong(Class<?> kind) {
        return kind.equals(Long.class);
    }

    default boolean isDouble(Class<?> kind) {
        return kind.equals(Double.class);
    }

    default boolean isFloat(Class<?> kind) {
        return kind.equals(Float.class);
    }

    default boolean isString(Class<?> kind) {
        return kind.equals(String.class);
    }

    default boolean isShort(Class<?> kind) {
        return kind.equals(Short.class);
    }

    default boolean isByte(Class<?> kind) {
        return kind.equals(Byte.class);
    }

    default boolean isChar(Class<?> kind) {
        return kind.equals(Character.class);
    }

}
