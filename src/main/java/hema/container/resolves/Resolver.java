package hema.container.resolves;

import java.lang.reflect.Parameter;
import java.util.Map;

public interface Resolver extends Aware {

    /**
     * Resolve any object.
     *
     * @param concrete   -
     * @param parameter  -
     * @param datasource -
     *
     * @return Any.
     */
    <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource);
}
