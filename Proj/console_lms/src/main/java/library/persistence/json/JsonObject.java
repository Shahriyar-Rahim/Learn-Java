package library.persistence.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonObject extends JsonValue {
    private final Map<String, JsonValue> map = new LinkedHashMap<>();

    @Override
    public boolean isObject() {
        return true;
    }

    public JsonObject put(String key, JsonValue value) {
        if (key != null) {
            map.put(key, value == null ? JsonValue.NULL : value);
        }
        return this;
    }

    public JsonObject put(String key, String value) {
        return put(key, value == null ? JsonValue.NULL : new JsonString(value));
    }

    public JsonObject put(String key, long value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, int value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, boolean value) {
        return put(key, value ? JsonBoolean.TRUE : JsonBoolean.FALSE);
    }

    public JsonValue get(String key) {
        return map.get(key);
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public String getString(String key, String defaultValue) {
        JsonValue val = get(key);
        if (val != null && val.isString()) {
            return val.asString();
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        JsonValue val = get(key);
        if (val != null && val.isNumber()) {
            return val.asInt();
        }
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        JsonValue val = get(key);
        if (val != null && val.isNumber()) {
            return val.asLong();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        JsonValue val = get(key);
        if (val != null && val.isBoolean()) {
            return val.asBoolean();
        }
        return defaultValue;
    }

    public JsonObject getObject(String key) {
        JsonValue val = get(key);
        return (val != null && val.isObject()) ? val.asObject() : null;
    }

    public JsonArray getArray(String key) {
        JsonValue val = get(key);
        return (val != null && val.isArray()) ? val.asArray() : null;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Map<String, JsonValue> asMap() {
        return Collections.unmodifiableMap(map);
    }

    public int size() {
        return map.size();
    }
}
