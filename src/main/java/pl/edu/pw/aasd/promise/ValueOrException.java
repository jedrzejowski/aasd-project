package pl.edu.pw.aasd.promise;

public final class ValueOrException<V> {
    final Object object;
    final boolean is_value;

    ValueOrException(Object object, boolean value) {
        this.object = object;
        this.is_value = value;
    }

    static <V> ValueOrException<V> value(V value) {
        return new ValueOrException<V>(value, true);
    }

    static <V, E extends Throwable> ValueOrException<V> exception(E exception) {
        return new ValueOrException<V>(exception, false);
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