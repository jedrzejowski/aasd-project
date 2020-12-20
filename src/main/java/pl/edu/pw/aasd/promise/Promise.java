package pl.edu.pw.aasd.promise;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Promise<T> extends PromiseSupport<T> {

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private Runnable fulfillmentAction;
    private Consumer<? super Throwable> exceptionHandler;

    /**
     * Creates a promise that will be fulfilled in future.
     */
    public Promise() {
        // Empty constructor
    }

    /**
     * Fulfills the promise with the provided value.
     *
     * @param value the fulfilled value that can be accessed using {@link #get()}.
     */
    @Override
    public void fulfill(T value) {
        super.fulfill(value);
        postFulfillment();
    }

    /**
     * Fulfills the promise with exception due to error in execution.
     *
     * @param exception the exception will be wrapped in {@link ExecutionException} when accessing the
     *                  value using {@link #get()}.
     */
    @Override
    public void fulfillExceptionally(Exception exception) {
        super.fulfillExceptionally(exception);
        handleException(exception);
        postFulfillment();
    }

    private void handleException(Exception exception) {
        if (exceptionHandler == null) {
            return;
        }
        exceptionHandler.accept(exception);
    }

    private void postFulfillment() {
        if (fulfillmentAction == null) {
            return;
        }
        fulfillmentAction.run();
    }

    /**
     * Executes the task using the executor in other thread and fulfills the promise returned once the
     * task completes either successfully or with an exception.
     *
     * @param task     the task that will provide the value to fulfill the promise.
     * @param executor the executor in which the task should be run.
     * @return a promise that represents the result of running the task provided.
     */
    public Promise<T> fulfillInAsync(final Callable<T> task, Executor executor) {
        executor.execute(() -> {
            try {
                fulfill(task.call());
            } catch (Exception ex) {
                fulfillExceptionally(ex);
            }
        });
        return this;
    }

    public Promise<T> fulfillInAsync(final Callable<T> task) {
        return fulfillInAsync(task, executor);
    }

    /**
     * Returns a new promise that, when this promise is fulfilled normally, is fulfilled with result
     * of this promise as argument to the action provided.
     *
     * @param action action to be executed.
     * @return a new promise.
     */
    public Promise<Void> thenAccept(Consumer<? super T> action) {
        var dest = new Promise<Void>();
        fulfillmentAction = new ConsumeAction(this, dest, action);
        return dest;
    }

    /**
     * Set the exception handler on this promise.
     *
     * @param exceptionHandler a consumer that will handle the exception occurred while fulfilling the
     *                         promise.
     * @return this
     */
    public Promise<T> onError(Consumer<? super Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Returns a new promise that, when this promise is fulfilled normally, is fulfilled with result
     * of this promise as argument to the function provided.
     *
     * @param func function to be executed.
     * @return a new promise.
     */
    public <V> Promise<V> thenApply(Function<? super T, V> func) {
        Promise<V> dest = new Promise<>();
        fulfillmentAction = new TransformAction<>(this, dest, func);
        return dest;
    }


    /**
     * Accesses the value from source promise and calls the consumer, then fulfills the destination
     * promise.
     */
    private class ConsumeAction implements Runnable {

        private final Promise<T> src;
        private final Promise<Void> dest;
        private final Consumer<? super T> action;

        private ConsumeAction(Promise<T> src, Promise<Void> dest, Consumer<? super T> action) {
            this.src = src;
            this.dest = dest;
            this.action = action;
        }

        @Override
        public void run() {
            try {
                action.accept(src.get());
                dest.fulfill(null);
            } catch (Throwable throwable) {
                dest.fulfillExceptionally((Exception) throwable.getCause());
            }
        }
    }

    /**
     * Accesses the value from source promise, then fulfills the destination promise using the
     * transformed value. The source value is transformed using the transformation function.
     */
    private class TransformAction<V> implements Runnable {

        private final Promise<T> src;
        private final Promise<V> dest;
        private final Function<? super T, V> func;

        private TransformAction(Promise<T> src, Promise<V> dest, Function<? super T, V> func) {
            this.src = src;
            this.dest = dest;
            this.func = func;
        }

        @Override
        public void run() {
            try {
                dest.fulfill(func.apply(src.get()));
            } catch (Throwable throwable) {
                dest.fulfillExceptionally((Exception) throwable.getCause());
            }
        }
    }


    public static <T> Promise<ValueOfException<T>[]> all(Promise<T>[] promises) {
        var promise = new Promise<ValueOfException<T>[]>();

        var array = new ValueOfException[promises.length];
        AtomicInteger done = new AtomicInteger();

        for (int i = 0; i < promises.length; i++) {
            final var my_i = i;
            promises[i].thenAccept(obj -> {
                synchronized (array) {
                    array[my_i] = ValueOfException.value(obj);
                    done.getAndIncrement();

                    if (done.get() == promises.length) {
                        promise.fulfill(array);
                    }
                }
            });

            promises[i].onError(throwable -> {
                synchronized (array) {
                    array[my_i] = ValueOfException.exception(throwable);
                    done.getAndIncrement();

                    if (done.get() == promises.length) {
                        promise.fulfill(array);
                    }
                }
            });
        }

        return promise;
    }

    public static <T> Promise<ValueOfException<T>[]> all(Stream<Promise<T>> promises) {
        return all((Promise[]) promises.toArray());
    }

    public static <T> Promise<T> fulfilled(T obj) {
        var promise = new Promise<T>();
        promise.fulfillInAsync(() -> obj);
        return promise;
    }
}