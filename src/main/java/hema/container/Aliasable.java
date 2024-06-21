package hema.container;

public interface Aliasable {
    /**
     * Alias a parameter to a different name.
     *
     * @param parameter Parameter name.
     * @param alias     Parameter alias.
     */
    Aliasable alias(String parameter, String alias);
}
