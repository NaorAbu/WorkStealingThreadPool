package WorkStealingThreadPool;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an abstract class that represents a task that may be executed using the
 * {@link WorkStealingThreadPool}
 * @param <R> the task result type
 */
public abstract class Task<R> {
    private Processor handler;
    private volatile boolean didStart = false;
    Runnable callback;
    private Deferred<R> def = new Deferred<R>();

    /**
     * start handling the task - note that this method is protected, a handler
     * cannot call it directly but instead must use the
     * {@link #handle(bgu.spl.a2.Processor)} method
     */
    protected abstract void start();

    /**
     * start/continue handling the task
     * <p>
     * this method should be called by a processor in order to start this task
     * or continue its execution in the case where it has been already started,
     * any sub-tasks / child-tasks of this task should be submitted to the queue
     * of the handler that handles it currently
     * <p>
     * IMPORTANT: this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * @param handler the handler that wants to handle the task
     */
    /*package*/
    final void handle(Processor handler) {
        this.handler = handler;
        if (didStart && callback != null) {
            callback.run();
        } else {
            this.didStart = true;
            this.start();
        }
    }

    /**
     * This method schedules a new task (a child of the current task) to the
     * same processor which currently handles this task.
     *
     * @param task the task to execute
     */
    protected final void spawn(Task<?>... task) {
        for (Task t : task) {
            this.handler.getPool().submitTo(t, handler.getId());
        }
    }

    /**
     * add a callback to be executed once *all* the given tasks results are
     * resolved
     * @param tasks
     * @param callback the callback to execute once all the results are resolved
     */
    protected final void whenResolved(Collection<? extends Task<?>> tasks, Runnable callback) {
        this.callback = callback;
        AtomicInteger counter = new AtomicInteger(tasks.size());
        for (Task t : tasks) {
            t.getResult().whenResolved(() -> {
                if (counter.decrementAndGet() == 0) {
                    handler.getPool().submitToEnd(this, handler.getId());
                }
            });
        }
    }

    /**
     * resolve the internal result - should be called by the task derivative
     * once it is done.
     *
     * @param result - the task calculated result
     */
    protected final void complete(R result) {
        def.resolve(result);
    }

    /**
     * @return this task deferred result
     */
    public final Deferred<R> getResult() {
        return def;
    }
}
