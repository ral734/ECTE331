/**
 * Thread B's sequence: FuncB1 -> FuncB2 -> FuncB3.
 * FuncB1 has no dependency. FuncB2 needs A1 (from Thread A). FuncB3 needs
 * A2 (from Thread A). Synchronized via Signal gates, not sleep/busy-wait.
 */
public class ThreadB extends Thread {
    private final SharedState state;
    private final Signal afterA1, afterB2, afterA2, afterB3;
    private final boolean verbose;

    public ThreadB(SharedState state, Signal afterA1, Signal afterB2,
                    Signal afterA2, Signal afterB3, boolean verbose) {
        super("ThreadB");
        this.state = state;
        this.afterA1 = afterA1;
        this.afterB2 = afterB2;
        this.afterA2 = afterA2;
        this.afterB3 = afterB3;
        this.verbose = verbose;
    }

    @Override
    public void run() {
        // FuncB1: B1 = sum(0..250). No dependency on Thread A.
        state.b1 = SumUtil.sumTo(250);
        if (verbose) System.out.println("FuncB1 done: B1=" + state.b1);

        // FuncB2: B2 = A1 + sum(0..200). Depends on FuncA1.
        try {
            afterA1.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        state.b2 = state.a1 + SumUtil.sumTo(200);
        if (verbose) System.out.println("FuncB2 done: B2=" + state.b2);
        afterB2.signal(); // unblocks ThreadA's FuncA2

        // FuncB3: B3 = A2 + sum(0..400). Depends on FuncA2.
        try {
            afterA2.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        state.b3 = state.a2 + SumUtil.sumTo(400);
        if (verbose) System.out.println("FuncB3 done: B3=" + state.b3);
        afterB3.signal(); // unblocks ThreadA's FuncA3
    }
}