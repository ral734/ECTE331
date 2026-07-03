/**
 * Task 1 & 2: runs the three robotic-arm threads with distinct priorities,
 * all sharing one MotorController, printing thread execution, resource
 * access, and timestamps. The MotorController's synchronized acquire/
 * release plus the "occupied" check prove mutual exclusion holds.
 */
public class Task1_BasicDemo {
    public static void main(String[] args) throws Exception {
        EventLogger logger = new EventLogger("task1_log.txt");
        MotorController controller = new MotorController(MotorController.PriorityMode.NONE, 0, logger);

        RobotTaskThread safetyMonitor = new RobotTaskThread("SafetyMonitor", Thread.MAX_PRIORITY,
                controller, 300, 200, 5, logger, false);
        RobotTaskThread motionPlanner = new RobotTaskThread("MotionPlanner", Thread.NORM_PRIORITY,
                controller, 300, 200, 5, logger, false);
        RobotTaskThread loggerThread = new RobotTaskThread("Logger", Thread.MIN_PRIORITY,
                controller, 300, 200, 5, logger, false);

        safetyMonitor.start();
        motionPlanner.start();
        loggerThread.start();

        safetyMonitor.join();
        motionPlanner.join();
        loggerThread.join();

        logger.log("All threads finished. No 'RACE CONDITION DETECTED' line above => mutual exclusion held.");
        logger.close();
    }
}