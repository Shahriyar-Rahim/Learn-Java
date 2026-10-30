package library.persistence.json;

public class JsonBoolean extends JsonValue {
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
