package WorkStealingThreadPool;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 * <p>
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class Processor implements Runnable {
    private final WorkStealingThreadPool pool;
    private final int id;
    protected volatile boolean running = true;

    /**
     * constructor for this class
     * <p>
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     * <p>
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id   - the processor id (every processor need to have its own unique
     *             id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }

    protected int getId() {
        return this.id;
    }

    protected WorkStealingThreadPool getPool() {
        return this.pool;
    }

    @Override
    public void run() {
        while (running) {
            Task<?> task = this.pool.queueArr[id].pollFirst();
            if (task != null) {
                task.handle(this);
            } else {
                if (!steal()) {
                    task = this.pool.queueArr[id].pollFirst();
                    if (task != null) {
                        task.handle(this);
                    } else {
                        try {
                            pool.vm.await(this.pool.vm.getVersion());
                        } catch (InterruptedException e) {
                            running = false;
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }

    protected boolean steal() {
        boolean ans = false;
        int counter = 0;
        for (int i = moduloThreadAmount(id); i != id; moduloThreadAmount(i)) {
            i = moduloThreadAmount(i);
            if (this.pool.queueArr[i].size() > 1 && (i != id)) {
                for (int j = 0; j < (this.pool.queueArr[i].size() / 2); j++) {
                    counter++;
                    Task<?> t = this.pool.queueArr[i].pollLast();
                    if (t != null) this.pool.queueArr[id].addFirst(t);
                }
                ans = true;
            }
        }
        return ans;
    }

    protected int moduloThreadAmount(int i) {
        i++;
        if (i == this.pool.getAmount()) i = 0;
        return i;
    }
}
