package WorkStealingThreadPool;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class represents a deferred result i.e., an object that eventually will
 * be resolved to hold a result of some operation, the class allows for getting
 * the result once it is available and registering a callback that will be
 * called once the result is available.
 * @param <T> the result type
 */
public class Deferred<T> {
    private AtomicBoolean isResolved = new AtomicBoolean(false);
    private T ans;
    private Runnable callback;

    /**
     * @return the resolved value if such exists (i.e., if this object has been
     * {@link #resolve(java.lang.Object)}ed yet
     * @throws IllegalStateException in the case where this method is called and
     *                               this object is not yet resolved
     */
    public T get() {
        if (isResolved()) {
            return ans;
        } else {
            throw new IllegalStateException("not resolved yet");
        }
    }

    /**
     * @return true if this object has been resolved - i.e., if the method
     * {@link #resolve(java.lang.Object)} has been called on this object before.
     */
    public boolean isResolved() {
        return isResolved.get();
    }

    /**
     * resolve this deferred object - from now on, any call to the method
     * {@link #get()} should return the given value
     * <p>
     * Any callbacks that were registered to be notified when this object is
     * resolved via the {@link #whenResolved(java.lang.Runnable)} method should
     * be executed before this method returns
     *
     * @param value - the value to resolve this deferred object with
     * @throws IllegalStateException in the case where this object is already
     *                               resolved
     */
    public synchronized void resolve(T value) {
        if (!isResolved()) {
            isResolved.set(true);
            ans = value;
            if (callback != null) callback.run();
        } else {
            throw new IllegalStateException("Already resolved");
        }
    }

    /**
     * add a callback to be called when this object is resolved. if while
     * calling this method the object is already resolved - the callback should
     * be called immediately
     * @param callback the callback to be called when the deferred object is
     *                 resolved
     */
    public synchronized void whenResolved(Runnable callback) {
        if (isResolved.get()) {
            callback.run();
        } else {
            this.callback = callback;
        }
    }
}
