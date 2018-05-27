package WorkStealingThreadPool;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 */
public class VersionMonitor {
    private int version = 0;

    public int getVersion() {
        return version;
    }

    public synchronized void inc() {
        version++;
        notifyAll();
    }

    public synchronized void await(int version) throws InterruptedException {
        while (this.version <= version) {
            wait();
        }
    }
}
