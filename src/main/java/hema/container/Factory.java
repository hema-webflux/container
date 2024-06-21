package hema.container;

import java.util.Map;

public interface Factory {

    /**
     * Create instance.
     *
     * @param clazz      Resolve class.
     * @param parameters Datasource: mysql or request data collection.
     *
     * @return Clazz instance.
     *
     * @throws BindingResolutionException .
     */
    <T> T make(Class<T> clazz, Map<String, Object> parameters) throws BindingResolutionException;

    /**
     * Define a parameter alias binding.
     * @param concrete
     * @return Aliasable
     */
    <T> Aliasable when(Class<T> concrete);
}
