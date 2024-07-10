package hema.container.resolves;

import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

class MapResolver implements Resolver {

    private final Resolver resolver;

    MapResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> reflect, Parameter parameter, Map<String, Object> datasource) throws ResolveException {

        Object value = resolver.resolve(reflect, parameter, datasource);

        if (value instanceof String && isJsonObject(value)) {
            return new JSONObject((String) value).toMap();
        }

        if (!(value instanceof Map<?, ?>)) {
            throw new ResolveException(String.format("The [%s] must be a map or json.", value));
        }

        return value;
    }

}
