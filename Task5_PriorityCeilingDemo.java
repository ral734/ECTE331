public class Task5_PriorityCeilingDemo {
    public static void main(String[] args) throws Exception {
        RobotTaskThread.warmUp();
        EventLogger logger = new EventLogger("task5_log.txt");
        MotorController controller = new MotorController(MotorController.PriorityMode.CEILING, Thread.MAX_PRIORITY, logger);

        RobotTaskThread lowLogger = new RobotTaskThread("Logger", Thread.MIN_PRIORITY,
                controller, 0, 4000, 1, logger, true); 
        RobotTaskThread highSafety = new RobotTaskThread("SafetyMonitor", Thread.MAX_PRIORITY,
                controller, 0, 200, 1, logger, false);
        CpuBoundThread mediumPlanner = new CpuBoundThread("MotionPlanner", Thread.NORM_PRIORITY, 3500, logger);

        lowLogger.start();
        Thread.sleep(300);
        highSafety.start();
        Thread.sleep(500);
        mediumPlanner.start();

        lowLogger.join();
        highSafety.join();
        mediumPlanner.join();

        logger.log("RESULT: SafetyMonitor waited " + highSafety.getLastWaitMillis()
                + " ms for the MotorController (priority ceiling enabled).");
        logger.close();
    }
}