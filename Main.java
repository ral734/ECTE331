/**
 * Runs the ThreadA/ThreadB synchronization scenario for a high number of
 * iterations, checking every time that the final shared-variable values
 * match the analytically predicted constants (see report Part a). If the
 * synchronization were incorrect, non-deterministic OS thread scheduling
 * would eventually cause a wrong interleaving on at least one iteration —
 * running many iterations is what gives confidence the implementation is
 * correct, not just lucky on a single run.
 */
public class Main {

    static final int EXPECTED_A1 = 125250;
    static final int EXPECTED_B1 = 31375;
    static final int EXPECTED_B2 = 145350;
    static final int EXPECTED_A2 = 190500;
    static final int EXPECTED_B3 = 270700;
    static final int EXPECTED_A3 = 350900;

    public static void main(String[] args) throws InterruptedException {
        int iterations = 20000;
        int mismatches = 0;

        long start = System.nanoTime();
        for (int iter = 1; iter <= iterations; iter++) {
            boolean verbose = (iter == 1); // full trace for the first iteration only

            SharedState state = new SharedState();
            Signal afterA1 = new Signal();
            Signal afterB2 = new Signal();
            Signal afterA2 = new Signal();
            Signal afterB3 = new Signal();

            ThreadA threadA = new ThreadA(state, afterA1, afterB2, afterA2, afterB3, verbose);
            ThreadB threadB = new ThreadB(state, afterA1, afterB2, afterA2, afterB3, verbose);

            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();

            boolean ok = state.a1 == EXPECTED_A1 && state.b1 == EXPECTED_B1
                    && state.b2 == EXPECTED_B2 && state.a2 == EXPECTED_A2
                    && state.b3 == EXPECTED_B3 && state.a3 == EXPECTED_A3;

            if (!ok) {
                mismatches++;
                System.out.println("Iteration " + iter + " MISMATCH: A1=" + state.a1 + " B1=" + state.b1
                        + " B2=" + state.b2 + " A2=" + state.a2 + " B3=" + state.b3 + " A3=" + state.a3);
            }

            if (iter == 1 || iter % 5000 == 0) {
                System.out.println("Iteration " + iter + ": A1=" + state.a1 + " B1=" + state.b1
                        + " B2=" + state.b2 + " A2=" + state.a2 + " B3=" + state.b3 + " A3=" + state.a3
                        + (ok ? " (matches expected)" : " (MISMATCH)"));
            }
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("\n=== RESULT ===");
        System.out.println("Ran " + iterations + " iterations in " + elapsedMs + " ms.");
        System.out.println("Expected: A1=" + EXPECTED_A1 + " B1=" + EXPECTED_B1 + " B2=" + EXPECTED_B2
                + " A2=" + EXPECTED_A2 + " B3=" + EXPECTED_B3 + " A3=" + EXPECTED_A3);
        System.out.println("Mismatches: " + mismatches + " / " + iterations);
        System.out.println(mismatches == 0
                ? "All iterations matched the expected values -- synchronization is correct."
                : "Synchronization FAILED on " + mismatches + " iteration(s).");
    }
}