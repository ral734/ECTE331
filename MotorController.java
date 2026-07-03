/**
 * Shared critical resource representing the robotic arm's motor controller.
 * Access is mutually exclusive; the configured PriorityMode determines
 * which (if any) priority-management protocol is simulated.
 */
public class MotorController {

    public enum PriorityMode { NONE, INHERITANCE, CEILING }

    private final PriorityMode mode;
    private final int ceilingPriority;
    private final EventLogger logger;

    private boolean locked = false;
    private Thread holder = null;
    private int holderOriginalPriority = -1;

    // Used only to double-check mutual exclusion for Task 2.
    private volatile boolean occupied = false;

    public MotorController(PriorityMode mode, int ceilingPriority, EventLogger logger) {
        this.mode = mode;
        this.ceilingPriority = ceilingPriority;
        this.logger = logger;
    }

    /**
     * Blocks until the caller obtains exclusive access, applying the
     * configured priority-management strategy.
     * @return milliseconds the caller spent waiting
     */
    public synchronized long acquire() throws InterruptedException {
        Thread caller = Thread.currentThread();
        long start = System.nanoTime();

        while (locked) {
            if (mode == PriorityMode.INHERITANCE && holder != null
                    && caller.getPriority() > holder.getPriority()) {
                logger.log(String.format(
                    "PRIORITY INHERITANCE: %s (priority %d) temporarily raised to %d because it blocks %s (priority %d)",
                    holder.getName(), holder.getPriority(), caller.getPriority(),
                    caller.getName(), caller.getPriority()));
                holder.setPriority(caller.getPriority());
            }
            logger.log(caller.getName() + " BLOCKED waiting for MotorController (currently held by "
                    + (holder != null ? holder.getName() : "?") + ")");
            wait();
        }

        locked = true;
        holder = caller;
        holderOriginalPriority = caller.getPriority();

        if (mode == PriorityMode.CEILING && caller.getPriority() != ceilingPriority) {
            logger.log(String.format("PRIORITY CEILING: %s raised from %d to ceiling priority %d",
                    caller.getName(), caller.getPriority(), ceilingPriority));
            caller.setPriority(ceilingPriority);
        }

        long waitedMillis = (System.nanoTime() - start) / 1_000_000;
        logger.log(caller.getName() + " ACQUIRED MotorController (waited " + waitedMillis + " ms)");

        if (occupied) {
            logger.log("*** RACE CONDITION DETECTED: resource was already marked occupied! ***");
        }
        occupied = true;

        return waitedMillis;
    }

    /** Releases the resource, restoring the caller's priority if it had been changed. */
    public synchronized void release() {
        Thread caller = Thread.currentThread();
        occupied = false;

        if (caller.getPriority() != holderOriginalPriority) {
            logger.log(String.format("Restoring %s priority from %d back to %d",
                    caller.getName(), caller.getPriority(), holderOriginalPriority));
            caller.setPriority(holderOriginalPriority);
        }

        logger.log(caller.getName() + " RELEASED MotorController");
        locked = false;
        holder = null;
        notifyAll();
    }
}