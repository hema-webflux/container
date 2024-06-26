package hema.container;

import hema.web.inflector.Inflector;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

class Application implements Container, Resolver, InitializingBean {

    private final ApplicationContext app;

    private final AliasBinding aliasable;

    private final Factory factory;

    private final Resolver queryable;

    private final Inflector inflector;

    Application(ApplicationContext context, AliasBinding aliasBinding, Factory factory, Resolver queryable, Inflector inflector) {
        this.app = context;
        this.aliasable = aliasBinding;
        this.factory = factory;
        this.queryable = queryable;
        this.inflector = inflector;
    }

    @Override
    public <T> Aliasable when(Class<T> concrete) {
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
    public <T> T make(final Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {
        return resolve(clazz, parameters);
    }

    private <T> T resolve(Class<T> concrete, final Map<String, Object> parameters) throws BindingResolutionException {

        Constructor<?> constructor = findDefaultConstructor(concrete.getDeclaredConstructors());

        if (Objects.isNull(constructor)) {
            throw new BindingResolutionException("No default constructor found.");
        }

        List<Object> instances = Stream.of(constructor.getParameters())
                .map(dependency -> resolve(concrete, dependency, parameters))
                .toList();

        try {
            return concrete.cast(constructor.newInstance(instances.toArray()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BindingResolutionException(e.getMessage());
        }
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
        return factory.make(parameter).resolve(concrete, parameter, datasource);
    }

    /**
     * Resolve a non-class hinted primitive dependency.
     *
     * @param concrete   Parameter type.
     * @param parameter  Constructor parameter object.
     * @param datasource Data source.
     *
     * @return object
     */
    <T> Object resolvePrimitive(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object value = queryable.resolve(concrete, parameter, datasource);

        if (Objects.isNull(value)) {
            return getDefaultValue(parameter);
        }

        if (value instanceof String && match((String) value)) {
            return castValueToNumber(parameter, (String) value);
        }

        return value;
    }

    private boolean match(String value) {
        return value.matches("-?\\d+") || value.matches("-?\\d+(\\.\\d+)?") || value.matches("true|false");
    }

    private Object castValueToNumber(final Parameter parameter, String value) throws BindingResolutionException {
        try {
            return switch (parameter.getType().getName()) {
                case "int", "java.lang.Integer" -> Integer.parseInt(value);
                case "long", "java.lang.Long" -> Long.parseLong(value);
                case "float", "java.lang.Float" -> Float.parseFloat(value);
                case "double", "java.lang.Double" -> Double.parseDouble(value);
                case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
                case "short", "java.lang.Short" -> Short.parseShort(value);
                case "byte", "java.lang.Byte" -> Byte.parseByte(value);
                default -> String.format(
                        "Unresolvable dependency resolving [%s] in class [%s]",
                        parameter.getName(),
                        parameter.getClass().getName()
                );
            };
        } catch (NumberFormatException e) {
            return getDefaultValue(parameter);
        }
    }

    /**
     * Set Default Value for Built-in Types.
     *
     * @return Converted Value
     */
    private Object getDefaultValue(final Parameter parameter) {
        return switch (parameter.getType().getName()) {
            case "int", "java.lang.Integer", "short", "java.lang.Short", "byte", "java.lang.Byte" -> 0;
            case "long", "java.lang.Long" -> 0L;
            case "float", "java.lang.Float" -> 0f;
            case "double", "java.lang.Double" -> 0D;
            case "boolean", "java.lang.Boolean" -> false;
            default -> "";
        };
    }

    /**
     * Resolve a class based dependency from the container.
     *
     * @param concrete   Class type.
     * @param parameter  Constructor parameter object.
     * @param datasource Data source.
     *
     * @return Class instance.
     *
     * @throws BindingResolutionException -
     */
    @SuppressWarnings("unchecked")
    <T> Object resolveClass(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {
        if (concrete.isInterface()) {

            if (!app.containsBean(concrete.getName())) {
                throw new BindingResolutionException(String.format("Target [%s] is not instantiable.", concrete.getName()));
            }

            return concrete.cast(app.getBean(concrete.getName()));
        }

        if (app.containsBean(concrete.getName())) {
            Object bean = app.getBean(concrete.getName());

            if (concrete.isInstance(bean)) {
                return concrete.cast(bean);
            }
        }

        Object value = queryable.resolve(concrete, parameter, datasource);

        if (concrete.isInstance(value)) {
            return value;
        }

        return make(concrete, (Map<String, Object>) value);
    }

    /**
     * Resolve a enum based dependency from the datasource.
     *
     * @param concrete   Class type.
     * @param parameter  Constructor parameter object.
     * @param datasource Data source.
     *
     * @return Enum<?>
     *
     * @throws BindingResolutionException -
     */
    @SuppressWarnings("unchecked")
    <T> Object resolveEnum(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) throws BindingResolutionException {

        Class<? extends Enum<?>> enumerable = (Class<? extends Enum<?>>) concrete;

        Enum<?>[] constants = enumerable.getEnumConstants();

        if (constants.length == 0) {
            failedBindingResolutionException("The enumeration class [%s] must define members.", enumerable.getName());
        }

        Object value = queryable.resolve(concrete, parameter, datasource);

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


    @Override
    public void afterPropertiesSet() {
        factory.setContainer(this);
    }

    private void failedBindingResolutionException(String message, String concrete) throws BindingResolutionException {
        throw new BindingResolutionException(String.format(message, concrete));
    }
}
