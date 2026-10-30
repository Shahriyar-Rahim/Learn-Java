package library.persistence.json;

import library.exception.PersistenceException;

/**
 * Pure Java recursive descent JSON parser.
 */
public class JsonParser {
    private final String src;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    public JsonParser(String src) {
        this.src = src != null ? src : "";
    }

    public static JsonValue parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return JsonNull.INSTANCE;
        }
        return new JsonParser(json).parseValue();
    }

    public JsonValue parseValue() {
        skipWhitespace();
        if (pos >= src.length()) {
            return JsonNull.INSTANCE;
        }
        char c = peek();
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> new JsonString(parseString());
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> {
                if (c == '-' || Character.isDigit(c)) {
                    yield parseNumber();
                }
                throw error("Unexpected character: '" + c + "'");
            }
        };
    }

    private JsonObject parseObject() {
        expect('{');
        JsonObject obj = new JsonObject();
        skipWhitespace();
        if (peek() == '}') {
            advance();
            return obj;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("Expected string key in object");
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonValue val = parseValue();
            obj.put(key, val);
            skipWhitespace();
            char next = peek();
            if (next == '}') {
                advance();
                break;
            } else if (next == ',') {
                advance();
            } else {
                throw error("Expected ',' or '}' in object, found '" + next + "'");
            }
        }
        return obj;
    }

    private JsonArray parseArray() {
        expect('[');
        JsonArray array = new JsonArray();
        skipWhitespace();
        if (peek() == ']') {
            advance();
            return array;
        }
        while (true) {
            JsonValue val = parseValue();
            array.add(val);
            skipWhitespace();
            char next = peek();
            if (next == ']') {
                advance();
                break;
            } else if (next == ',') {
                advance();
            } else {
                throw error("Expected ',' or ']' in array, found '" + next + "'");
            }
        }
        return array;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = advance();
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (pos >= src.length()) throw error("Unterminated escape sequence");
                char esc = advance();
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > src.length()) throw error("Incomplete unicode escape");
                        String hex = src.substring(pos, pos + 4);
                        pos += 4;
                        col += 4;
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw error("Invalid unicode escape \\u" + hex);
                        }
                    }
                    default -> throw error("Invalid escape char: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw error("Unterminated string");
    }

    private JsonNumber parseNumber() {
        int start = pos;
        if (peek() == '-') advance();
        while (pos < src.length() && Character.isDigit(peek())) {
            advance();
        }
        boolean isFloating = false;
        if (pos < src.length() && peek() == '.') {
            isFloating = true;
            advance();
            while (pos < src.length() && Character.isDigit(peek())) {
                advance();
            }
        }
        if (pos < src.length() && (peek() == 'e' || peek() == 'E')) {
            isFloating = true;
            advance();
            if (pos < src.length() && (peek() == '+' || peek() == '-')) advance();
            while (pos < src.length() && Character.isDigit(peek())) {
                advance();
            }
        }
        String numStr = src.substring(start, pos);
        try {
            if (isFloating) {
                return new JsonNumber(Double.parseDouble(numStr));
            } else {
                return new JsonNumber(Long.parseLong(numStr));
            }
        } catch (NumberFormatException e) {
            throw error("Malformed number: " + numStr);
        }
    }

    private JsonBoolean parseBoolean() {
        if (match("true")) return JsonBoolean.TRUE;
        if (match("false")) return JsonBoolean.FALSE;
        throw error("Expected boolean literal");
    }

    private JsonNull parseNull() {
        if (match("null")) return JsonNull.INSTANCE;
        throw error("Expected null literal");
    }

    private boolean match(String token) {
        if (src.startsWith(token, pos)) {
            pos += token.length();
            col += token.length();
            return true;
        }
        return false;
    }

    private void skipWhitespace() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
                col++;
            } else if (c == '\n') {
                pos++;
                line++;
                col = 1;
            } else {
                break;
            }
        }
    }

    private char peek() {
        if (pos >= src.length()) return '\0';
        return src.charAt(pos);
    }

    private char advance() {
        if (pos >= src.length()) return '\0';
        char c = src.charAt(pos++);
        col++;
        return c;
    }

    private void expect(char expected) {
        skipWhitespace();
        char c = advance();
        if (c != expected) {
            throw error("Expected '" + expected + "', found '" + c + "'");
        }
    }

    private PersistenceException error(String msg) {
        return new PersistenceException("JSON parse error at line " + line + ", column " + col + ": " + msg);
    }
}
