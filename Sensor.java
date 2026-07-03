import java.util.Random;

/**
 * Represents one redundant altitude sensor (A, B, or C) used by the
 * Triple Modular Redundancy (TMR) drone controller.
 */
public class Sensor {

    private final String id;
    private final int baselineValue;
    private final Random random = new Random();

    public Sensor(String id, int baselineValue) {
        this.id = id;
        this.baselineValue = baselineValue;
    }

    public String getId() {
        return id;
    }

    /**
     * Simulates a single altitude reading from this sensor.
     * chance 0-14  -> throws SensorReadException (sensor failure)
     * chance 15-29 -> returns a corrupted value outside [0,200]
     * chance 30-99 -> returns a valid value within [0,200]
     *
     * @return the simulated altitude reading (may be corrupted / out of range)
     * @throws SensorReadException if the sensor fails to read
     */
    public int readSensor() throws SensorReadException {
        int chance = random.nextInt(100); // 0-99 inclusive

        if (chance < 15) {
            throw new SensorReadException("Sensor " + id + " failed to produce a reading (chance=" + chance + ").");
        } else if (chance < 30) {
            // Corrupted: force a value clearly outside the valid [0,200] range.
            int corrupted = 201 + random.nextInt(100); // 201-300
            return corrupted;
        } else {
            // Valid reading: baseline +/- a small offset, clamped to [0,200].
            int reading = baselineValue + random.nextInt(21) - 10; // baseline-10 .. baseline+10
            if (reading < 0) reading = 0;
            if (reading > 200) reading = 200;
            return reading;
        }
    }
}