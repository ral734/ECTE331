/**
 * Thrown when the system reliability has degraded to the point that
 * the drone must enter SAFE MODE and halt execution.
 */
public class SystemReliabilityException extends Exception {
    public SystemReliabilityException(String message) {
        super(message);
    }
}