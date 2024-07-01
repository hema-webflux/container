package hema.container.resolves;

import org.json.JSONObject;

interface Aware {

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
