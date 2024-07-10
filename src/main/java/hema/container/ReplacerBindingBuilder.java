package hema.container;

import hema.web.inflector.Inflector;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class ReplacerBindingBuilder implements Replacer {

    private final Inflector inflector;

    private String concrete = null;

    private Map<String, Map<String, String>> replacers = null;

    ReplacerBindingBuilder(Inflector inflector) {
        this.inflector = inflector;
    }

    /**
     * Alias a parameter to a different name.
     *
     * @param parameter Parameter name.
     * @param replacer  Parameter alias.
     */
    @Override
    public Replacer replacer(final String parameter, final String replacer) {
        if (parameter.equals(replacer)) {
            throw new LogicException(String.format("[%s] is replacer to itself.", parameter));
        }

        if (isNull()) {
            this.replacers = new ConcurrentHashMap<>();
        }

        if (!replacers.containsKey(concrete)) {
            Map<String, String> replacers = new ConcurrentHashMap<>();
            replacers.put(parameter.trim(), replacer.trim());

            this.replacers.put(concrete, replacers);

            return this;
        }

        Map<String, String> replacers = this.replacers.get(concrete);

        replacers.put(parameter, replacer);

        return this;
    }

    /**
     * Determines whether the alias container is bound to the object.
     *
     * @param reflect .
     *
     * @return boolean.
     */
    @Override
    public <T> boolean hasReplacerAlias(Class<T> reflect, Parameter parameter) {

        if (isNull()) {
            return false;
        }

        if (replacers.isEmpty()) {
            return false;
        }

        if (!replacers.containsKey(reflect.getName())) {
            return false;
        }

        return Objects.nonNull(getReplacerAlias(reflect, parameter));
    }

    private boolean isNull() {
        return Objects.isNull(replacers);
    }

    /**
     * Get the replacers bound to constructor parameters.
     *
     * @param reflect  Abstract name.
     * @param parameter Clazz constructor parameter name.
     *
     * @return Parameter alias.
     */
    public <T> String getReplacerAlias(final Class<T> reflect, final Parameter parameter) {

        Map<String, String> replacers = this.replacers.get(reflect.getName());

        if (replacers.containsKey(parameter.getName())) {
            return replacers.get(parameter.getName());
        }

        if (replacers.containsKey(reflect.getName().toLowerCase())) {
            return replacers.get(reflect.getName().toLowerCase());
        }

        return replacers.get(inflector.snake(reflect.getName(), "#"));
    }

    /**
     * Add a alias binding to the aliasable.
     *
     * @param concrete .
     *
     * @return Aliasable.
     */
    Replacer addConcreteBinding(final String concrete) {
        this.concrete = concrete;
        return this;
    }
}
