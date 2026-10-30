package library.persistence.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JsonArray extends JsonValue implements Iterable<JsonValue> {
    private final List<JsonValue> list = new ArrayList<>();

    @Override
    public boolean isArray() {
        return true;
    }

    public JsonArray add(JsonValue value) {
        list.add(value == null ? JsonValue.NULL : value);
        return this;
    }

    public JsonArray add(String value) {
        return add(value == null ? JsonValue.NULL : new JsonString(value));
    }

    public JsonArray add(long value) {
        return add(new JsonNumber(value));
    }

    public JsonValue get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public List<JsonValue> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return list.iterator();
    }
}
