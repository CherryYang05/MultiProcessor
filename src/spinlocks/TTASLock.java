package spinlocks;

/*
 * @Author         : Cherry
 * @Date           : 2022-10-06 21:58:36
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 14:29:33
 * @FilePath       : TTASLock.java
 * @Description    : Test-Test-And-Set 锁
 */
import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock {

    AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (true) {
            while (state.get()) {};
            if (!state.getAndSet(true))
                return;
        }
    }

    public void unlock() {
        state.set(false);
    }
}
