package hema.container;

import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

public interface Resolver {

    <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource);

    default boolean isJsonObject(final Object value) {

        if (value instanceof String) {

            try {
                new JSONObject(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }


}
