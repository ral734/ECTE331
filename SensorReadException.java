import java.io.IOException;

/**
 * Thrown when a sensor fails to produce a reading during a sensing cycle.
 */
public class SensorReadException extends IOException {
    public SensorReadException(String message) {
        super(message);
    }
}