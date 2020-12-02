package pl.edu.pw.aasd.promise;

public final class ValueOfException<V> {
    final Object object;
    final boolean is_value;

    ValueOfException(Object object, boolean value) {
        this.object = object;
        this.is_value = value;
    }

    static <V> ValueOfException<V> value(V value) {
        return new ValueOfException<V>(value, true);
    }

    static <V, E extends Throwable> ValueOfException<V> exception(E exception) {
        return new ValueOfException<V>(exception, false);
    }

    public boolean isValue() {
        return this.is_value;
    }

    public V value() {
        if (!this.is_value) {
            throw new RuntimeException("ValueOfException: accessing value when there is exception");
        }

        return (V) object;
    }

    public boolean isException() {
        return !this.is_value;
    }

    public Throwable exception() {
        if (!this.is_value) {
            throw new RuntimeException("ValueOfException: accessing exception when there is value");
        }

        return (Throwable) object;
    }
}