package huabanshou.tiktok.usecase.container;

import hema.web.inflector.Inflector;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

final class Application implements Factory, ContainerAware, Reflector {

    private final ApplicationContext context;

    private final Inflector inflector;

    private final EnumFactory enumFactory;

    private Map<String, String> aliases;

    private static final String[] standardTypes = {
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Character",
            "java.lang.Boolean",
            };

    private static final String[] specialTypes = {"null", "NULL", "undefined", "NaN", "nil"};

    Application(ApplicationContext context, Inflector inflector, EnumFactory enumFactory, Map<String, String> aliases) {
        this.context = context;
        this.inflector = inflector;
        this.enumFactory = enumFactory;
        this.aliases = aliases;
    }

    @Override
    public <T> T make(final Class<T> clazz, final Map<String, Object> parameters) throws BindingResolutionException {

        if (clazz.isInterface()) {

            if (!context.containsBean(clazz.getName())) {
                notInstantiable(clazz.getName());
            }

            return clazz.cast(context.getBean(clazz.getName()));
        }

        if (context.containsBean(clazz.getName())) {
            Object bean = context.getBean(clazz.getName());

            if (clazz.isInstance(bean)) {
                return clazz.cast(bean);
            }
        }

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length == 1) {
            Constructor<?> constructor = constructors[0];

            return this.build(clazz, constructor, parameters);
        }

        Constructor<?> constructor = findDefaultConstructor(constructors);

        if (Objects.isNull(constructor)) {
            fails("No default constructor found.");
        }

        return this.build(clazz, constructor, parameters);
    }

    @Override
    public void alias(String abstracted, String alias) {

        if (abstracted.equals(alias)){
            
        }

    }

    private String getAlias(String clazz) {
        return this.aliases.getOrDefault(clazz, clazz);
    }

    private <T> T build(final Class<T> clazz, Constructor<?> constructor, final Map<String, Object> row) throws BindingResolutionException {

        List<Object> instances = this.resolveDependencies(constructor.getParameters(), row);

        T concrete = null;

        try {
            concrete = clazz.cast(constructor.newInstance(instances.toArray()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            fails(e.getMessage());
        }

        return concrete;
    }

    private void fails(final String message) throws BindingResolutionException {
        throw new BindingResolutionException(message);
    }

    @SuppressWarnings("unchecked")
    private List<Object> resolveDependencies(final Parameter[] dependencies, final Map<String, Object> datasource) {

        List<Object> result = new LinkedList<>();

        for (Parameter dependency : dependencies) {

            String paramName = datasource.containsKey(dependency.getName())
                    ? dependency.getName()
                    : inflector.snake(dependency.getName(), "#");

            Object value = datasource.get(paramName);

            if (isPrimitive(dependency)) {

                if (isValidSpecialType(value)) {
                    value = getDefaultValue(dependency);
                } else if (isConvertibleToNumber(value)) {
                    value = getDefaultValue(dependency);
                }
            }

            if ((isDeclaredClass(dependency) || dependency.getType().isInterface()) && !dependency.getType().isEnum()) {

                if (isJson(value)) {
                    Map<String, Object> serial = new JSONObject(value).toMap();
                    value = this.make(dependency.getType(), serial);
                } else if (value instanceof Map<?, ?>) {
                    value = this.make(dependency.getType(), (Map<String, Object>) value);
                } else {
                    value = this.make(dependency.getType(), datasource);
                }
            }

            if (dependency.getType().isEnum()) {
                value = this.enumFactory.make((Class<? extends Enum<?>>) dependency.getType(), datasource);
            }

            result.add(value);
        }

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
