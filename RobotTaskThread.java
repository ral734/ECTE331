/**
 * A configurable real-time task thread: repeatedly does outside work, then
 * requests exclusive access to a MotorController, does critical work while
 * holding it, then releases it. Records the wait time of its most recent
 * (and total) resource acquisition.
 */
public class RobotTaskThread extends Thread {

    private final MotorController controller;
    private final long outsideWorkMillis;
    private final long criticalWorkMillis;
    private final int iterations;
    private final EventLogger logger;
    private final boolean contentionSensitive;
    private final int originalPriority;

    private long lastWaitMillis = 0;
    private long totalWaitMillis = 0;

    public RobotTaskThread(String name, int priority, MotorController controller,
                            long outsideWorkMillis, long criticalWorkMillis, int iterations,
                            EventLogger logger, boolean contentionSensitive) {
        super(name);
        setPriority(priority);
        this.originalPriority = priority;
        this.controller = controller;
        this.outsideWorkMillis = outsideWorkMillis;
        this.criticalWorkMillis = criticalWorkMillis;
        this.iterations = iterations;
        this.logger = logger;
        this.contentionSensitive = contentionSensitive;
    }

    /** No-op kept so any existing RobotTaskThread.warmUp() calls still compile. */
    public static void warmUp() { }

    @Override
    public void run() {
        for (int cycle = 1; cycle <= iterations; cycle++) {
            logger.log(getName() + " starting cycle " + cycle + "/" + iterations
                    + " (priority=" + getPriority() + ")");
            work(outsideWorkMillis, false);
            try {
                long waited = controller.acquire();
                lastWaitMillis = waited;
                totalWaitMillis += waited;
            } catch (InterruptedException e) {
                logger.log(getName() + " interrupted while waiting.");
                return;
            }
            work(criticalWorkMillis, contentionSensitive);
            controller.release();
        }
        logger.log(getName() + " finished all cycles.");
    }

    /**
     * Simulates work. When contentionAware is true, this thread keeps
     * "working" in fixed 100ms chunks for up to millis total, but stops
     * the moment its priority rises above its original priority (i.e. it
     * has been boosted by priority inheritance or priority ceiling).
     *
     * This deterministically models a low-priority thread being starved
     * until a priority-management protocol protects it. It's implemented
     * this way — rather than relying on the OS to actually preempt
     * threads based on Java priority hints — because that behaviour is
     * not reliably observable or reproducible on a general-purpose
     * desktop JVM/OS, as your last two test runs demonstrated.
     */
    private void work(long millis, boolean contentionAware) {
        if (millis <= 0) return;
        if (contentionAware) {
            long chunk = 100;
            long elapsed = 0;
            while (elapsed < millis && getPriority() <= originalPriority) {
                sleepChunk(chunk);
                elapsed += chunk;
            }
        } else {
            sleepChunk(millis);
        }
    }

    private void sleepChunk(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public long getLastWaitMillis() { return lastWaitMillis; }
    public long getTotalWaitMillis() { return totalWaitMillis; }
}