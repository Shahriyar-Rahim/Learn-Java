package library.persistence.json;

public class JsonNull extends JsonValue {
    public static final JsonNull INSTANCE = new JsonNull();

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String toString() {
        return "null";
    }
}
