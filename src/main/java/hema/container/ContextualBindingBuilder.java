package hema.container;

import hema.web.inflector.Inflector;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.Map;

class ContextualBindingBuilder implements Contextual {

    private final Replacer replacer;

    private final Inflector inflector;

    private Map<String, Object> sourceData = null;

    public ContextualBindingBuilder(Replacer replacer, Inflector inflector) {
        this.replacer = replacer;
        this.inflector = inflector;
    }

    @Override
    public <T> Object resolve(Class<T> reflect, Parameter parameter, Map<String, Object> data) {

        if (sourceData == null) {
            sourceData = data;
        }

        if (replacer.hasReplacerAlias(reflect, parameter)) {
            return getValueForAlias(reflect, parameter, sourceData);
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
    private <T> Object getValueForAlias(Class<T> concrete, Parameter parameter, Map<String, Object> data) {

        final String replacerAlias = replacer.getReplacerAlias(concrete, parameter);

        if (replacerAlias.contains(".")) {
            return findNestedValue(replacerAlias, data);
        }

        return data.containsKey(replacerAlias)
                ? data.get(replacerAlias)
                : data.get(guessParameterQueryName(parameter, data));
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
    private Object findNestedValue(String alias, Map<String, Object> datasource) {

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
