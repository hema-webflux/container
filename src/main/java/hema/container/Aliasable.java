package hema.container;

import java.lang.reflect.Parameter;

public interface Aliasable {
    /**
     * Alias a parameter to a different name.
     *
     * @param parameter Parameter name.
     * @param alias     Parameter alias.
     */
    Aliasable alias(String parameter,String alias);

    /**
     * Determines whether aliases are bound to constructor parameters of the given clazz.
     *
     * @param concrete Abstract name.
     *
     * @return boolean
     */
    <T> boolean hasAlias( Class<T> concrete);

    /**
     * Get the aliases bound to constructor parameters.
     *
     * @param concrete  Abstract name.
     * @param parameter Clazz constructor parameter name.
     *
     * @return Parameter alias.
     */
    <T> String getAlias(Class<T> concrete, Parameter parameter);
}
