package hema.container;

import hema.web.inflector.Inflector;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

class AliasBinding implements Aliasable {

    private final Map<String, Map<String, String>> aliases;

    private final Inflector inflector;

    private String concrete = null;

    public AliasBinding(Map<String, Map<String, String>> aliases, Inflector inflector) {
        this.aliases = aliases;
        this.inflector = inflector;
    }

    /**
     * Alias a parameter to a different name.
     *
     * @param parameter Parameter name.
     * @param alias     Parameter alias.
     */
    @Override
    public Aliasable alias(String parameter, String alias) {
        if (parameter.equals(alias)) {
            throw new LogicException(String.format("[%s] is aliased to itself.", parameter));
        }

        if (!aliases.containsKey(concrete)) {
            Map<String, String> aliases = new HashMap<>();
            aliases.put(parameter, alias);

            this.aliases.put(concrete, aliases);

            return this;
        }

        Map<String, String> aliases = this.aliases.get(concrete);

        aliases.put(parameter, alias);

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
    public <T> boolean hasAlias(Class<T> concrete) {
        return aliases.containsKey(concrete.getName());
    }

    /**
     * Get the aliases bound to constructor parameters.
     *
     * @param concrete  Abstract name.
     * @param parameter Clazz constructor parameter name.
     *
     * @return Parameter alias.
     */
    public <T> String getAlias(Class<T> concrete, Parameter parameter) {

        Map<String, String> aliases = this.aliases.get(concrete.getName());

        if (aliases.containsKey(parameter.getName())) {
            return aliases.get(parameter.getName());
        }

        if (aliases.containsKey(concrete.getName().toLowerCase())) {
            return aliases.get(concrete.getName().toLowerCase());
        }

        return aliases.get(inflector.snake(concrete.getName(), "#"));
    }

    /**
     * Add a alias binding to the aliasable.
     *
     * @param concrete .
     *
     * @return Aliasable.
     */
    Aliasable addConcreteBinding(final String concrete) {
        this.concrete = concrete;
        return this;
    }
}
