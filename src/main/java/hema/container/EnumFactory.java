package hema.container;

import hema.web.inflector.Inflector;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class EnumFactory {

    private final Inflector inflector;

    EnumFactory(final Inflector inflector) {
        this.inflector = inflector;
    }

    Enum<?> make(final Class<? extends Enum<?>> clazz, final Object scope, final Map<String, Object> datasource) throws BindingResolutionException {

        Enum<?>[] constants = clazz.getEnumConstants();

        if (constants.length == 0) {
            fails("The enumeration class [%s] must define members.", clazz);
        }

        String constant = Objects.isNull(scope) ? findEnumConstant(clazz, datasource) : (String) scope;

        Optional<? extends Enum<?>> first = findFirstEnumConstant(constant, constants);

        if (first.isEmpty()) {
            fails("No enum constant found for enum class [%s].", clazz);
        }

        return first.orElse(null);
    }

    private Optional<? extends Enum<?>> findFirstEnumConstant(final String name, final Enum<?>[] constants) {
        return Arrays.stream(constants).filter(constant -> constant.name().equals(name)).findFirst();
    }

    private String findEnumConstant(final Class<? extends Enum<?>> enumClazz, final Map<String, Object> datasource) {

        String name = enumClazz.getSimpleName();

        if (datasource.containsKey(name.toLowerCase())) {
            return (String) datasource.get(name.toLowerCase());
        }

        if (datasource.containsKey(inflector.snake(name, "#"))) {
            return (String) datasource.get(inflector.snake(name, "#"));
        }

        return name;
    }

    private void fails(final String message, final Class<? extends Enum<?>> clazz) throws BindingResolutionException {
        throw new BindingResolutionException(String.format(message, clazz.getSimpleName()));
    }
}
