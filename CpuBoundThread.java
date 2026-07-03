/**
 * A thread that consumes CPU for a fixed duration without touching the
 * shared resource. Used to simulate a medium-priority task competing for
 * CPU time during the priority-inversion demonstration (Task 3).
 */
public class CpuBoundThread extends Thread {
    private final long durationMillis;
    private final EventLogger logger;

    public CpuBoundThread(String name, int priority, long durationMillis, EventLogger logger) {
        super(name);
        setPriority(priority);
        this.durationMillis = durationMillis;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.log(getName() + " starting CPU-bound work (priority=" + getPriority()
                + "), does not touch MotorController");
        long end = System.nanoTime() + durationMillis * 1_000_000L;
        long x = 0;
        while (System.nanoTime() < end) {
            x++;
        }
        logger.log(getName() + " finished CPU-bound work.");
    }
}