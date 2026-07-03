/**
 * Thread A's sequence: FuncA1 -> FuncA2 -> FuncA3.
 * FuncA1 has no dependency. FuncA2 needs B2 (from Thread B). FuncA3 needs
 * B3 (from Thread B). Synchronized via Signal gates, not sleep/busy-wait.
 */
public class ThreadA extends Thread {
    private final SharedState state;
    private final Signal afterA1, afterB2, afterA2, afterB3;
    private final boolean verbose;

    public ThreadA(SharedState state, Signal afterA1, Signal afterB2,
                    Signal afterA2, Signal afterB3, boolean verbose) {
        super("ThreadA");
        this.state = state;
        this.afterA1 = afterA1;
        this.afterB2 = afterB2;
        this.afterA2 = afterA2;
        this.afterB3 = afterB3;
        this.verbose = verbose;
    }

    @Override
    public void run() {
        // FuncA1: A1 = sum(0..500). No dependency, runs immediately.
        state.a1 = SumUtil.sumTo(500);
        if (verbose) System.out.println("FuncA1 done: A1=" + state.a1);
        afterA1.signal(); // unblocks ThreadB's FuncB2

        // FuncA2: A2 = B2 + sum(0..300). Depends on FuncB2.
        try {
            afterB2.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        state.a2 = state.b2 + SumUtil.sumTo(300);
        if (verbose) System.out.println("FuncA2 done: A2=" + state.a2);
        afterA2.signal(); // unblocks ThreadB's FuncB3

        // FuncA3: A3 = B3 + sum(0..400). Depends on FuncB3.
        try {
            afterB3.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        state.a3 = state.b3 + SumUtil.sumTo(400);
        if (verbose) System.out.println("FuncA3 done: A3=" + state.a3);
    }
}