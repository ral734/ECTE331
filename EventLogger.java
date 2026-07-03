import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** Thread-safe console + file logger with elapsed-time timestamps. */
public class EventLogger {
    private final PrintWriter writer;
    private final long startNanos = System.nanoTime();
    private final boolean echoToConsole;

    public EventLogger(String fileName) throws IOException {
        this(fileName, true);
    }

    public EventLogger(String fileName, boolean echoToConsole) throws IOException {
        writer = new PrintWriter(new FileWriter(fileName, true));
        this.echoToConsole = echoToConsole;
    }

    /** Logs a message, prefixed with elapsed time since this logger was created. */
    public synchronized void log(String message) {
        double elapsedMs = (System.nanoTime() - startNanos) / 1_000_000.0;
        String line = String.format("[t+%8.2fms] %s", elapsedMs, message);
        if (echoToConsole) {
            System.out.println(line);
        }
        writer.println(line);
        writer.flush();
    }

    public void close() {
        writer.close();
    }
}