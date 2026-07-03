/**
 * A one-shot synchronization gate: one thread calls await() to block
 * (no busy-waiting, no sleep) until another thread calls signal().
 * Built on Java's monitor wait()/notifyAll(), guarded by a boolean flag
 * to protect against spurious wakeups.
 */
public class Signal {
    private boolean signaled = false;

    /** Blocks the calling thread until signal() has been called. */
    public synchronized void await() throws InterruptedException {
        while (!signaled) {
            wait();
        }
    }

    /** Wakes up any thread(s) currently blocked in await(). */
    public synchronized void signal() {
        signaled = true;
        notifyAll();
    }
}