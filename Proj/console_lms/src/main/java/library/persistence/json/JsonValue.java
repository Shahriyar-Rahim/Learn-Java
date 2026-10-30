package library.persistence.json;

import java.util.*;

/**
 * Lightweight, 100% Pure Java JSON Model.
 */
public abstract class JsonValue {

    public boolean isObject() { return false; }
    public boolean isArray() { return false; }
    public boolean isString() { return false; }
    public boolean isNumber() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isNull() { return false; }

    public JsonObject asObject() {
        if (isObject()) return (JsonObject) this;
        throw new IllegalStateException("Not a JsonObject: " + this);
    }

    public JsonArray asArray() {
        if (isArray()) return (JsonArray) this;
        throw new IllegalStateException("Not a JsonArray: " + this);
    }

    public String asString() {
        if (isString()) return ((JsonString) this).getValue();
        throw new IllegalStateException("Not a JsonString: " + this);
    }

    public long asLong() {
        if (isNumber()) return ((JsonNumber) this).getLongValue();
        throw new IllegalStateException("Not a JsonNumber: " + this);
    }

    public int asInt() {
        return (int) asLong();
    }

    public boolean asBoolean() {
        if (isBoolean()) return ((JsonBoolean) this).getValue();
        throw new IllegalStateException("Not a JsonBoolean: " + this);
    }

    public static JsonNull NULL = new JsonNull();

    public static JsonString string(String s) {
        return s == null ? null : new JsonString(s);
    }

    public static JsonNumber number(long n) {
        return new JsonNumber(n);
    }

    public static JsonBoolean bool(boolean b) {
        return b ? JsonBoolean.TRUE : JsonBoolean.FALSE;
    }

    public static JsonObject object() {
        return new JsonObject();
    }

    public static JsonArray array() {
        return new JsonArray();
    }
}
