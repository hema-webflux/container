package hema.container;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

class Container implements Factory, Reflector {

    private final ApplicationContext context;

    private final AliasBinding aliasable;

    private final Queryable queryable;

    private static final String[] standardTypes = {
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Character",
            "java.lang.Boolean"
    };

    private static final String[] specialTypes = {"null", "NULL", "undefined", "NaN", "nil"};

    Container(ApplicationContext context, AliasBinding aliasBinding, Queryable queryable) {
        this.context = context;
        this.aliasable = aliasBinding;
        this.queryable = queryable;
    }

    @Override
    public <T> T make(final Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {
        return resolve(clazz, parameters);
    }

    @Override
    public <T> Aliasable when(Class<T> concrete) {
        return aliasable.addConcreteBinding(concrete.getName());
    }

    private <T> T resolve(Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {

        Constructor<?> constructor = findDefaultConstructor(clazz.getDeclaredConstructors());

        if (Objects.isNull(constructor)) {
            throw new BindingResolutionException("No default constructor found.");
        }

        List<Object> instances = resolveDependencies(clazz, constructor.getParameters(), parameters);

        try {
            return clazz.cast(constructor.newInstance(instances.toArray()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BindingResolutionException(e.getMessage());
        }
    }

    private <T> List<Object> resolveDependencies(final Class<T> concrete, final Parameter[] dependencies, final Map<String, Object> datasource) {

        List<Object> result = new LinkedList<>();

        Stream.of(dependencies).forEach(dependency -> {
            Object value = createDefaultResolver(dependency)
                    .resolve(concrete, dependency, datasource);
            result.add(value);
        });

        return result;
    }

    private boolean isValidSpecialType(Object value) {

        if (value == null) {
            return true;
        }

        for (String type : specialTypes) {
            if (type.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private Resolver createDefaultResolver(Parameter parameter) {
        if (isPrimitive(parameter)) {
            return this.primitiveResolver();
        } else if (isDeclaredClass(parameter)) {
            return this::resolveClass;
        } else if (parameter.getType().isEnum()) {
            return this.enumResolver();
        } else if (parameter.getType().isArray()) {
            return this.arrayResolver();
        }
    }

    private interface Resolver {
        <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource);
    }

    public <T> Object resolveClass(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
        if (concrete.isInterface()) {

            if (!context.containsBean(concrete.getName())) {
                notInstantiable(concrete.getName());
            }

            return concrete.cast(context.getBean(concrete.getName()));
        }

        if (context.containsBean(concrete.getName())) {
            Object bean = context.getBean(concrete.getName());

            if (concrete.isInstance(bean)) {
                return concrete.cast(bean);
            }
        }

        if (concrete.isInstance(value)) {
            return value;
        }

        if (isJson(value)) {
            Map<String, Object> serial = new JSONObject((String) value).toMap();
            value = make(concrete, serial);
        } else if (value instanceof Map<?, ?>) {
            value = make(concrete, (Map<String, Object>) value);
        } else {
            value = make(concrete, datasource);
        }

        return value;
    }

    /**
     * If the target interface cannot be instantiated, an exception is throw.
     *
     * @param concrete abstract.
     */
    void notInstantiable(final String concrete) {
        throw new BindingResolutionException(String.format("Target [%s] is not instantiable.", concrete));
    }

    private Resolver enumResolver() {
        return new Resolver() {
            @Override
            public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
                return null;
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
        };
    }

    private Resolver primitiveResolve() {
        return new Resolver() {
            @Override
            public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
                Object value = queryable.value(concrete, parameter, datasource);

                if (isValidSpecialType(value)) {
                    value = getDefaultValue(parameter);
                } else if (isConvertibleToNumber(value)) {
                    value = castAttributeAsNumber(parameter, (String) value);
                }

                return value;
            }

            /**
             * Set Default Value for Built-in Types.
             *
             * @return Converted Value
             */
            Object getDefaultValue(final Parameter parameter) {
                return switch (parameter.getType().getName()) {
                    case "int", "java.lang.Integer", "short", "java.lang.Short", "byte", "java.lang.Byte" -> 0;
                    case "long", "java.lang.Long" -> 0L;
                    case "float", "java.lang.Float" -> 0f;
                    case "double", "java.lang.Double" -> 0D;
                    case "boolean", "java.lang.Boolean" -> false;
                    default -> "";
                };
            }

            Object castAttributeAsNumber(final Parameter parameter, String value) {
                try {
                    return switch (parameter.getType().getName()) {
                        case "int", "java.lang.Integer" -> Integer.parseInt(value);
                        case "long", "java.lang.Long" -> Long.parseLong(value);
                        case "float", "java.lang.Float" -> Float.parseFloat(value);
                        case "double", "java.lang.Double" -> Double.parseDouble(value);
                        case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
                        case "short", "java.lang.Short" -> Short.parseShort(value);
                        case "byte", "java.lang.Byte" -> Byte.parseByte(value);
                        default -> throw new IllegalStateException("Unexpected value: " + parameter.getName());
                    };
                } catch (NumberFormatException e) {
                    return getDefaultValue(parameter);
                }
            }
        };
    }

    private Resolver arrayResolver() {
        return new Resolver() {
            @Override
            public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {
                return null;
            }
        };
    }

    @Override
    public boolean isStandard(Parameter parameter) {

        for (String type : standardTypes) {
            if (type.equals(parameter.getType().getName())) {
                return true;
            }
        }
        return false;
    }
}
