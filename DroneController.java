import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controls altitude estimation via Triple Modular Redundancy (TMR)
 * majority voting across three sensors, and monitors system reliability.
 */
public class DroneController {

    private final Sensor sensorA;
    private final Sensor sensorB;
    private final Sensor sensorC;

    private int previousAltitude = 100; // starting fallback altitude
    private int consecutiveFailures = 0;

    private final PrintWriter logWriter;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DroneController(Sensor a, Sensor b, Sensor c, String logFileName) throws IOException {
        this.sensorA = a;
        this.sensorB = b;
        this.sensorC = c;
        this.logWriter = new PrintWriter(new FileWriter(logFileName, true));
    }

    private void log(String message) {
        String entry = "[" + timestampFormat.format(new Date()) + "] " + message;
        logWriter.println(entry);
        logWriter.flush();
    }

    /**
     * Runs one full sensing + voting cycle across all three sensors.
     *
     * @return the resulting altitude for this cycle
     * @throws SystemReliabilityException if two consecutive reliability
     *         failures occur, forcing SAFE MODE.
     */
    public int processCycle() throws SystemReliabilityException {
        List<String> validIds = new ArrayList<>();
        List<Integer> validValues = new ArrayList<>();
        List<String> outlierIds = new ArrayList<>();
        Sensor[] sensors = { sensorA, sensorB, sensorC };

        for (Sensor s : sensors) {
            try {
                int value = s.readSensor();
                if (value < 0 || value > 200) {
                    System.out.println("Sensor " + s.getId() + " -> CORRUPTED reading: " + value);
                    log("CORRUPTED READING: Sensor " + s.getId() + " produced out-of-range value " + value);
                    outlierIds.add(s.getId());
                } else {
                    System.out.println("Sensor " + s.getId() + " -> " + value);
                    validIds.add(s.getId());
                    validValues.add(value);
                }
            } catch (SensorReadException e) {
                System.out.println("Sensor " + s.getId() + " -> FAILURE (" + e.getMessage() + ")");
                log("SENSOR FAILURE: " + e.getMessage());
                outlierIds.add(s.getId());
            }
        }

        int finalAltitude;
        boolean reliabilityFailure = false;

        if (validValues.size() < 2) {
            reliabilityFailure = true;
            System.out.println("RELIABILITY STATUS: FAILURE - fewer than 2 valid readings.");
            log("RELIABILITY FAILURE: fewer than 2 valid readings. Outliers=" + outlierIds);
            finalAltitude = previousAltitude;
        } else {
            Integer majorityValue = findMajorityAndLogOutlier(validIds, validValues);
            if (majorityValue != null) {
                finalAltitude = majorityValue;
                previousAltitude = finalAltitude;
                System.out.println("VOTING DECISION: Majority value = " + finalAltitude);
                log("MAJORITY DECISION: value=" + finalAltitude + " agreeingSensors outlier(s)=" + outlierIds);
            } else if (validValues.size() == 3) {
                finalAltitude = previousAltitude;
                System.out.println("VOTING DECISION: All 3 sensors disagree. Fallback to previous altitude = " + finalAltitude);
                log("FALLBACK DECISION: all three valid readings differ " + validValues + ". Using previous altitude " + finalAltitude);
            } else {
                reliabilityFailure = true;
                finalAltitude = previousAltitude;
                System.out.println("RELIABILITY STATUS: FAILURE - no majority found (2 valid readings disagree).");
                log("RELIABILITY FAILURE: no majority found. Valid readings=" + validValues + " sensors=" + validIds);
            }
        }

        if (reliabilityFailure) {
            consecutiveFailures++;
            System.out.println("Consecutive reliability failures: " + consecutiveFailures);
            if (consecutiveFailures >= 2) {
                log("SAFE MODE ACTIVATED after " + consecutiveFailures + " consecutive reliability failures.");
                System.out.println("*** SAFE MODE ACTIVATED ***");
                throw new SystemReliabilityException("Two consecutive reliability failures. Entering SAFE MODE.");
            }
        } else {
            consecutiveFailures = 0;
        }

        return finalAltitude;
    }

    /** Finds two valid readings that agree; logs the third (if any) as an outlier. */
    private Integer findMajorityAndLogOutlier(List<String> ids, List<Integer> values) {
        for (int i = 0; i < values.size(); i++) {
            for (int j = i + 1; j < values.size(); j++) {
                if (values.get(i).intValue() == values.get(j).intValue()) {
                    if (values.size() == 3) {
                        for (int k = 0; k < values.size(); k++) {
                            if (k != i && k != j) {
                                System.out.println("OUTLIER DETECTED: Sensor " + ids.get(k));
                                log("OUTLIER DETECTED: Sensor " + ids.get(k) + " disagreed with majority value " + values.get(i));
                            }
                        }
                    }
                    return values.get(i);
                }
            }
        }
        return null;
    }

    public void close() {
        logWriter.close();
    }
}