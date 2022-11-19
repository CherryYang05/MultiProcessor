package spinlocks;
/*
 * @Author         : Cherry
 * @Date           : 2022-10-06 22:22:08
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 14:29:46
 * @FilePath       : BackoffLock.java
 * @Description    : BackoffLock 易于实现，且性能优于 TASLock，但是性能与 minDelay 和 MaxDalay 的选取密切相关。但是会产生 cache 一致性流量和临界区利用率低的问题。
 */
import java.util.concurrent.atomic.AtomicBoolean;

public class BackoffLock {

    AtomicBoolean state = new AtomicBoolean(false);

    private static final int minDelay = 10;
    private static final int maxDelay = 1000;

    public void lock() {
        Backoff backoff = new Backoff(minDelay, maxDelay);
        while (true) {
            while (state.get()) {};
            if (!state.getAndSet(true)) {
                return;
            } else {
                backoff.backoff();
            }
        }
    }

    public void unlock() {
        state.set(false);
    }
}
