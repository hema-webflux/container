package hema.container;

import hema.web.inflector.Inflector;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

class Query implements Resolver {

    private final Replacer replacer;

    private final Inflector inflector;

    public Query(Replacer replacer, Inflector inflector) {
        this.replacer = replacer;
        this.inflector = inflector;
    }

    @Override
    public <T> Object resolve(Class<T> concrete, Parameter parameter, Map<String, Object> data) {

        if (replacer.hasReplacerAlias(concrete)) {
            return getValueForAlias(concrete, parameter, data);
        }

        return data.get(guessParameterQueryName(parameter, data));
    }

    /**
     * If the replacement alias received contains a dot, it indicates that it points to nested data.
     *
     * @param concrete  -
     * @param parameter -
     * @param data      -
     *
     * @return Nested value or normal value.
     */
    @SuppressWarnings("unchecked")
    private <T> Object getValueForAlias(Class<T> concrete, Parameter parameter, Map<String, Object> data) {

        final String replacerAlias = replacer.getReplacerAlias(concrete, parameter);

        if (replacerAlias.contains(".")) {
            Object aliasValue = findNestedValue(replacerAlias, data);

            Object defaultValue = data.get(guessParameterQueryName(parameter, data));

            if (isJsonObject(defaultValue)) {
                JSONObject json = new JSONObject((String) defaultValue);
                return json.append(replacerAlias, aliasValue).toMap();
            } else if (defaultValue instanceof Map<?, ?>) {
                ((Map<String, Object>) defaultValue).put(replacerAlias, aliasValue);
                return defaultValue;
            }

            return aliasValue;
        }

        return data.get(guessParameterQueryName(parameter, data));
    }

    /**
     * Guess parameter name the query.
     *
     * @param parameter Reflection constructor parameter object.
     * @param data      Original data.
     *
     * @return Guess name.
     */
    private String guessParameterQueryName(Parameter parameter, Map<String, Object> data) {

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
