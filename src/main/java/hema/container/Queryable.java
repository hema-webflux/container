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

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> data) {

        if (aliasable.hasAlias(concrete)) {
            return getValueForAlias(concrete, parameter, data);
        }

        System.out.println(guessMapKey(parameter, data));
        System.out.println(data);
        System.out.println(aliasable.hasAlias(concrete));
        return data.get(guessMapKey(parameter, data));
    }

    @SuppressWarnings("unchecked")
    private <T> Object getValueForAlias(Class<T> concrete, Parameter parameter, Map<String, Object> data) {

        String alias = aliasable.getAlias(concrete, parameter);

        if (alias.contains(".")) {
            Object aliasValue = findNestedValue(alias, data);

            Object defaultValue = data.get(guessMapKey(parameter, data));

            if (isJsonObject(defaultValue)) {
                JSONObject json = new JSONObject((String) defaultValue);
                json.append(alias, aliasValue);
                return json.toMap();
            } else if (defaultValue instanceof Map<?, ?>) {
                ((Map<String, Object>) defaultValue).put(alias, aliasValue);
                return defaultValue;
            }

            return aliasValue;
        }

        alias = guessMapKey(parameter, data);

        return data.get(alias);
    }

    private String guessMapKey(Parameter parameter, Map<String, Object> data) {

        if (data.containsKey(parameter.getName())) {
            return parameter.getName();
        }

        return data.containsKey(parameter.getName().toLowerCase())
                ? parameter.getName().toLowerCase()
                : inflector.snake(parameter.getName(), "#");
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
