package library.persistence.json;

public class JsonString extends JsonValue {
    private final String value;

    public JsonString(String value) {
        this.value = value != null ? value : "";
    }

    @Override
    public boolean isString() {
        return true;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
