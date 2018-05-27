package WorkStealingThreadPool;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 */
public class Processor implements Runnable {
    private final WorkStealingThreadPool pool;
    private final int id;
    protected volatile boolean running = true;

    /**
     * constructor for this class
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
