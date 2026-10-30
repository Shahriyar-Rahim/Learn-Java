package library.persistence.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Serializes JsonValue AST into valid JSON text with indentation or compact formatting.
 */
public final class JsonWriter {

    private JsonWriter() {
    }

    public static String format(JsonValue value) {
        StringWriter sw = new StringWriter();
        try {
            write(value, sw, 0, true);
        } catch (IOException ignored) {
        }
        return sw.toString();
    }

    public static String formatCompact(JsonValue value) {
        StringWriter sw = new StringWriter();
        try {
            write(value, sw, 0, false);
        } catch (IOException ignored) {
        }
        return sw.toString();
    }

    public static void write(JsonValue value, Writer writer, int indentLevel, boolean pretty) throws IOException {
        if (value == null || value.isNull()) {
            writer.write("null");
        } else if (value.isBoolean()) {
            writer.write(value.toString());
        } else if (value.isNumber()) {
            writer.write(value.toString());
        } else if (value.isString()) {
            writeString(value.asString(), writer);
        } else if (value.isArray()) {
            writeArray(value.asArray(), writer, indentLevel, pretty);
        } else if (value.isObject()) {
            writeObject(value.asObject(), writer, indentLevel, pretty);
        }
    }

    private static void writeString(String str, Writer writer) throws IOException {
        writer.write('"');
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> writer.write("\\\"");
                case '\\' -> writer.write("\\\\");
                case '\b' -> writer.write("\\b");
                case '\f' -> writer.write("\\f");
                case '\n' -> writer.write("\\n");
                case '\r' -> writer.write("\\r");
                case '\t' -> writer.write("\\t");
                default -> {
                    if (c < 0x20) {
                        writer.write(String.format("\\u%04x", (int) c));
                    } else {
                        writer.write(c);
                    }
                }
            }
        }
        writer.write('"');
    }

    private static void writeArray(JsonArray array, Writer writer, int indent, boolean pretty) throws IOException {
        int size = array.size();
        if (size == 0) {
            writer.write("[]");
            return;
        }
        writer.write('[');
        if (pretty) writer.write('\n');
        for (int i = 0; i < size; i++) {
            if (pretty) indent(writer, indent + 1);
            write(array.get(i), writer, indent + 1, pretty);
            if (i < size - 1) {
                writer.write(',');
            }
            if (pretty) writer.write('\n');
        }
        if (pretty) indent(writer, indent);
        writer.write(']');
    }

    private static void writeObject(JsonObject obj, Writer writer, int indent, boolean pretty) throws IOException {
        Map<String, JsonValue> map = obj.asMap();
        if (map.isEmpty()) {
            writer.write("{}");
            return;
        }
        writer.write('{');
        if (pretty) writer.write('\n');
        int i = 0;
        int size = map.size();
        for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
            if (pretty) indent(writer, indent + 1);
            writeString(entry.getKey(), writer);
            writer.write(pretty ? ": " : ":");
            write(entry.getValue(), writer, indent + 1, pretty);
            if (++i < size) {
                writer.write(',');
            }
            if (pretty) writer.write('\n');
        }
        if (pretty) indent(writer, indent);
        writer.write('}');
    }

    private static void indent(Writer writer, int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.write("  ");
        }
    }
}
