package library.persistence.json;

public class JsonNumber extends JsonValue {
    private final long longValue;
    private final double doubleValue;
    private final boolean isFloating;

    public JsonNumber(long longValue) {
        this.longValue = longValue;
        this.doubleValue = longValue;
        this.isFloating = false;
    }

    public JsonNumber(double doubleValue) {
        this.doubleValue = doubleValue;
        this.longValue = (long) doubleValue;
        this.isFloating = true;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    public long getLongValue() {
        return longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public boolean isFloating() {
        return isFloating;
    }

    @Override
    public String toString() {
        return isFloating ? Double.toString(doubleValue) : Long.toString(longValue);
    }
}
