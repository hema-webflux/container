package hema.container.resolves;

import hema.container.Resolver;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

class MapResolver implements Resolver,Facade {

    private final Resolver resolver;

    MapResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> datasource) {

        Object value = resolver.resolve(concrete, parameter, datasource);

        if (value instanceof String && isJsonObject(value)) {
            return new JSONObject((String) value).toMap();
        }

        if (!(value instanceof Map<?, ?>)) {
            throw new ResolveException("Can't resolve map: " + value);
        }

        return value;
    }

    @Override
    public String getFacadeAccessor() {
        return "map";
    }
}
