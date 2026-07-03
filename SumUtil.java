/** Utility class providing the loop-based summation used by all six functions. */
public class SumUtil {
    /** Computes 0 + 1 + ... + n using a loop (not the closed-form formula). */
    public static int sumTo(int n) {
        int total = 0;
        for (int i = 0; i <= n; i++) {
            total += i;
        }
        return total;
    }
}