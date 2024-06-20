package hema.container;

import hema.web.inflector.Inflector;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

class Container implements Factory, ContainerAware, Reflector {

    private final ApplicationContext context;

    private final Inflector inflector;

    private final EnumFactory factory;

    private final Map<String, String> aliases;

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

    Container(ApplicationContext context, Inflector inflector, EnumFactory enumFactory, Map<String, String> aliases) {
        this.context = context;
        this.inflector = inflector;
        this.factory = enumFactory;
        this.aliases = aliases;
    }

    @Override
    public <T> T make(final Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {
        return resolve(clazz, parameters);
    }

    private <T> T resolve(Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {

        Constructor<?> constructor = findDefaultConstructor(clazz.getDeclaredConstructors());

        if (Objects.isNull(constructor)) {
            throw new BindingResolutionException("No default constructor found.");
        }

        List<Object> instances = resolveDependencies(constructor.getParameters(), parameters);

        try {
            return clazz.cast(constructor.newInstance(instances.toArray()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BindingResolutionException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> resolveDependencies(final Parameter[] dependencies, final Map<String, Object> datasource) {

        List<Object> result = new LinkedList<>();

        Stream<Parameter> stream = Stream.of(dependencies);
        stream.forEach(dependency -> {

            Object value = findValue(dependency, datasource);

            if (isPrimitive(dependency)) {
                if (isValidSpecialType(value)) {
                    value = getDefaultValue(dependency);
                } else if (isConvertibleToNumber(value)) {
                    value = castAttributeAsNumber(dependency, (String) value);
                }

            } else if (isDeclaredClass(dependency)) {
                value = resolveClass(dependency.getType(), value, datasource);
            } else if (dependency.getType().isEnum()) {
                value = factory.make((Class<? extends Enum<?>>) dependency.getType(), datasource);
            }

            result.add(value);
        });

        return result;
    }

    private Object findValue(final Parameter parameter, final Map<String, Object> sources) {

        String alias = parameter.getName();

        alias = aliases.getOrDefault(alias, alias);

        alias = sources.containsKey(alias) ? alias : inflector.snake(alias, "#");

        return sources.get(alias);
    }

    @SuppressWarnings("unchecked")
    private <T> Object resolveClass(final Class<T> clazz, Object value, final Map<String, Object> datasource) throws BindingResolutionException {

        if (clazz.isInstance(value)) {
            return value;
        }

        boolean hasBean = context.containsBean(clazz.getName());

        if (clazz.isInterface()) {

            if (!hasBean) {
                notInstantiable(clazz.getName());
            }

            return clazz.cast(context.getBean(clazz.getName()));
        }

        if (hasBean) {
            Object bean = context.getBean(clazz.getName());

            if (clazz.isInstance(bean)) {
                return clazz.cast(bean);
            }
        }

        if (isJson(value)) {
            Map<String, Object> serial = new JSONObject().toMap();
            value = make(clazz, serial);
        } else if (value instanceof Map<?, ?>) {
            value = make(clazz, (Map<String, Object>) value);
        } else {
            value = make(clazz, datasource);
        }

        return value;
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

    @Override
    public void alias(String parameter, String alias) {

        if (parameter.equals(alias)) {
            throw new LogicException(String.format("[%s] is aliased to itself.", parameter));
        }

        this.aliases.put(parameter, alias);
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
