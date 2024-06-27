package hema.container.resolves;

import hema.container.Aware;
import hema.container.Factory;
import org.json.JSONObject;

import java.util.Map;

class ConverterFactory implements Factory<Converter<?>, Object> {
    @Override
    public Converter<?> make(Object parameter) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private static class MapConverter implements Converter<Map<String, Object>>, Aware {

        @Override
        public Map<String, Object> convert(Object value) {

            if (isJsonObject(value)) {
                return new JSONObject((String) value).toMap();
            }

            if (!(value instanceof Map<?, ?>)) {
                throw new ResolveException("Cannot convert " + value + " to Map");
            }

            return (Map<String, Object>) value;
        }
    }
}
