package hema.container;

import hema.web.inflector.Inflector;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

final class Queryable implements Resolver {

    private final Aliasable aliasable;
    private final Inflector inflector;

    public Queryable(Aliasable aliasable, Inflector inflector) {
        this.aliasable = aliasable;
        this.inflector = inflector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Object resolve(final Class<T> concrete, final Parameter parameter, final Map<String, Object> data) {

        String alias = aliasable.hasAlias(concrete) ? aliasable.getAlias(concrete, parameter) : parameter.getName();

        if (alias.contains(".")) {
            Object aliasValue = findNestedValue(alias, data);

            alias = data.containsKey(alias) ? alias : inflector.snake(alias, "#");

            Object result = data.get(alias);

            if (isJsonObject(result)) {
                JSONObject json = new JSONObject((String) result);
                json.append(alias, aliasValue);
                return json.toMap();
            } else if (result instanceof Map<?, ?>) {
                ((Map<String, Object>) result).put(alias, aliasValue);
                return result;
            }

            return aliasValue;
        }

        alias = data.containsKey(alias) ? alias : inflector.snake(alias, "#");

        return data.get(alias);
    }

    /**
     * Find Nested value by dot.
     *
     * @param alias      exp: user.contact.phone
     * @param datasource exp: {user={contact={phone=123456789}}}
     *
     * @return Last value.
     */
    @SuppressWarnings("unchecked")
    private Object findNestedValue(final String alias, final Map<String, Object> datasource) {

        int dotPlaceholder = alias.indexOf(".");
        if (dotPlaceholder == -1) {
            return datasource.get(alias);
        }

        String currentKey    = alias.substring(0, dotPlaceholder);
        String remainingKeys = alias.substring(dotPlaceholder + 1);

        Object current = datasource.get(currentKey);

        if (current instanceof Map<?, ?>) {
            return findNestedValue(remainingKeys, (Map<String, Object>) current);
        }

        if (isJsonObject(current)) {
            return findNestedValue(remainingKeys, new JSONObject((String) current).toMap());
        }

        return current;
    }
}
