package hema.container.resolves;

import hema.container.BindingResolutionException;
import hema.container.Resolver;
import hema.web.inflector.Inflector;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class EnumResolver implements Resolver {

    private final Resolver resolve;

    private final Inflector inflector;

    EnumResolver(Resolver resolve, Inflector inflector) {
        this.resolve = resolve;
        this.inflector = inflector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {

        Class<? extends Enum<?>> enumerable = (Class<? extends Enum<?>>) concrete;

        Enum<?>[] constants = enumerable.getEnumConstants();

        if (constants.length == 0) {
            failedBindingResolutionException("The enumeration class [%s] must define members.", enumerable.getName());
        }

        Object value = resolve.resolve(concrete, parameter, datasource);

        String constantName = Objects.isNull(value) ? getDefaultEnumValue(enumerable, datasource) : (String) value;

        Optional<? extends Enum<?>> first = Arrays.stream(constants)
                .filter(constant -> constant.name().equals(constantName))
                .findFirst();

        if (first.isEmpty()) {
            failedBindingResolutionException("No enum constant found for enum class [%s].", enumerable.getName());
        }

        return first.orElse(null);
    }

    /**
     * If the enumeration value is not found in the data source, retrieve the default enumeration item.
     *
     * @param enumerable Enum class object.
     * @param datasource data
     *
     * @return Enum item.
     */
    private String getDefaultEnumValue(Class<? extends Enum<?>> enumerable, Map<String, Object> datasource) {

        String name = enumerable.getSimpleName();

        if (datasource.containsKey(name)) {
            return (String) datasource.get(name);
        }

        if (datasource.containsKey(name.toLowerCase())) {
            return (String) datasource.get(name.toLowerCase());
        }

        if (datasource.containsKey(inflector.snake(name, "#"))) {
            return (String) datasource.get(inflector.snake(name, "#"));
        }

        return name;
    }

    private void failedBindingResolutionException(String message, String concrete) throws BindingResolutionException {
        throw new BindingResolutionException(String.format(message, concrete));
    }
}
