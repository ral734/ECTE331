import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 6: runs the Task 3 scenario under each priority-management mode
 * (NONE / INHERITANCE / CEILING) multiple times, measures SafetyMonitor's
 * waiting time and overall scenario response time, averages the results,
 * and writes them to results.csv for charting.
 */
public class Task6_PerformanceEvaluation {

    private static final int RUNS_PER_MODE = 10;

    public static void main(String[] args) throws Exception {
        RobotTaskThread.warmUp();
        List<Long> noneWaits = new ArrayList<>();
        List<Long> inheritWaits = new ArrayList<>();
        List<Long> ceilingWaits = new ArrayList<>();
        List<Long> noneResponse = new ArrayList<>();
        List<Long> inheritResponse = new ArrayList<>();
        List<Long> ceilingResponse = new ArrayList<>();

        for (int i = 1; i <= RUNS_PER_MODE; i++) {
            System.out.println("Run " + i + "/" + RUNS_PER_MODE + " - NONE");
            runOnce(MotorController.PriorityMode.NONE, 0, noneWaits, noneResponse);
            System.out.println("Run " + i + "/" + RUNS_PER_MODE + " - INHERITANCE");
            runOnce(MotorController.PriorityMode.INHERITANCE, 0, inheritWaits, inheritResponse);
            System.out.println("Run " + i + "/" + RUNS_PER_MODE + " - CEILING");
            runOnce(MotorController.PriorityMode.CEILING, Thread.MAX_PRIORITY, ceilingWaits, ceilingResponse);
        }

        System.out.println();
        System.out.println("=== SUMMARY (averages over " + RUNS_PER_MODE + " runs) ===");
        printSummary("NONE (baseline)", noneWaits, noneResponse);
        printSummary("INHERITANCE", inheritWaits, inheritResponse);
        printSummary("CEILING", ceilingWaits, ceilingResponse);

        try (PrintWriter csv = new PrintWriter(new FileWriter("results.csv"))) {
            csv.println("mode,run,waitMillis,responseMillis");
            writeCsv(csv, "NONE", noneWaits, noneResponse);
            writeCsv(csv, "INHERITANCE", inheritWaits, inheritResponse);
            writeCsv(csv, "CEILING", ceilingWaits, ceilingResponse);
        }
        System.out.println("\nRaw results written to results.csv");
    }

    private static void runOnce(MotorController.PriorityMode mode, int ceiling,
                                 List<Long> waits, List<Long> responses) throws Exception {
        EventLogger logger = new EventLogger("task6_log.txt", false); // file only, no console spam
        MotorController controller = new MotorController(mode, ceiling, logger);

        RobotTaskThread lowLogger = new RobotTaskThread("Logger", Thread.MIN_PRIORITY,
                controller, 0, 4000, 1, logger, true); 
        RobotTaskThread highSafety = new RobotTaskThread("SafetyMonitor", Thread.MAX_PRIORITY,
                controller, 0, 200, 1, logger, false);
        CpuBoundThread mediumPlanner = new CpuBoundThread("MotionPlanner", Thread.NORM_PRIORITY, 3500, logger);

        long scenarioStart = System.nanoTime();
        lowLogger.start();
        Thread.sleep(300);
        highSafety.start();
        Thread.sleep(500);
        mediumPlanner.start();

        lowLogger.join();
        highSafety.join();
        mediumPlanner.join();
        long responseMillis = (System.nanoTime() - scenarioStart) / 1_000_000;

        waits.add(highSafety.getLastWaitMillis());
        responses.add(responseMillis);
        logger.close();
    }

    private static void printSummary(String label, List<Long> waits, List<Long> responses) {
        System.out.printf("%-20s avg wait=%.1f ms   avg response=%.1f ms%n",
                label, average(waits), average(responses));
    }

    private static double average(List<Long> values) {
        long sum = 0;
        for (long v : values) sum += v;
        return values.isEmpty() ? 0 : (double) sum / values.size();
    }

    private static void writeCsv(PrintWriter csv, String mode, List<Long> waits, List<Long> responses) {
        for (int i = 0; i < waits.size(); i++) {
            csv.println(mode + "," + (i + 1) + "," + waits.get(i) + "," + responses.get(i));
        }
    }
}