package hema.container;

import java.lang.reflect.Parameter;

public interface Replacer {
    /**
     * Register a custom parameter value replacer.
     *
     * @param parameter Parameter name.
     * @param replacer     Parameter alias.
     */
    Replacer replacer(String parameter, String replacer);

    /**
     * Determines whether replacer are bound to constructor parameters of the given clazz.
     *
     * @param concrete Abstract name.
     *
     * @return boolean
     */
    <T> boolean hasReplacerAlias(Class<T> concrete);

    /**
     * Get the replacer bound to constructor parameters.
     *
     * @param concrete  Abstract name.
     * @param parameter Clazz constructor parameter name.
     *
     * @return Parameter replacer alias.
     */
    <T> String getReplacerAlias(Class<T> concrete, Parameter parameter);
}
