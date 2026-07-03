import java.util.Random;

/**
 * Entry point: runs repeated sensing/voting cycles until SAFE MODE
 * is triggered or a cycle cap is reached.
 */
public class Main {
    public static void main(String[] args) {
        Sensor sensorA = new Sensor("A", 100);
        Sensor sensorB = new Sensor("B", 100);
        Sensor sensorC = new Sensor("C", 100);

        String logFileName = "log_" + new Random().nextInt(100000) + ".txt";
        System.out.println("Logging to: " + logFileName);

        try {
            DroneController controller = new DroneController(sensorA, sensorB, sensorC, logFileName);
            try {
                int cycle = 1;
                while (cycle <= 50) { // safety cap so the demo terminates
                    System.out.println("\n--- Cycle " + cycle + " ---");
                    int altitude = controller.processCycle();
                    System.out.println("Current altitude: " + altitude + " m");
                    cycle++;
                }
                System.out.println("\nCompleted 50 cycles without SAFE MODE.");
            } catch (SystemReliabilityException e) {
                System.out.println("\nSYSTEM ENTERED SAFE MODE: " + e.getMessage());
            } finally {
                controller.close();
            }
        } catch (Exception e) {
            System.out.println("Fatal error initializing drone controller: " + e.getMessage());
        }
    }
}