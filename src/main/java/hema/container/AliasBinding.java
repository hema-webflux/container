package hema.container;

import hema.web.inflector.Inflector;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AliasBinding implements Replacer {

    private final Map<String, Map<String, String>> replacers;

    private final Inflector inflector;

    private String concrete = null;

     AliasBinding(Map<String, Map<String, String>> replacers,Inflector inflector) {
        this.replacers = replacers;
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

        if (!replacers.containsKey(concrete)) {
            Map<String, String> replacers = new ConcurrentHashMap<>();
            replacers.put(parameter, replacer);

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
     * @param concrete .
     *
     * @return boolean.
     */
    @Override
    public <T> boolean hasReplacerAlias(final Class<T> concrete) {
        return replacers.containsKey(concrete.getName());
    }

    /**
     * Get the replacers bound to constructor parameters.
     *
     * @param concrete  Abstract name.
     * @param parameter Clazz constructor parameter name.
     *
     * @return Parameter alias.
     */
    public <T> String getReplacerAlias(final Class<T> concrete, final Parameter parameter) {
        Map<String, String> replacers = this.replacers.get(concrete.getName());

        if (replacers.containsKey(parameter.getName())) {
            return replacers.get(parameter.getName());
        }

        if (replacers.containsKey(concrete.getName().toLowerCase())) {
            return replacers.get(concrete.getName().toLowerCase());
        }

        return replacers.get(inflector.snake(concrete.getName(), "#"));
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
