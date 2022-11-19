package spinlocks;

/*
 * @Author         : Cherry
 * @Date           : 2022-10-06 22:01:51
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-30 20:58:33
 * @FilePath       : TASLock.java
 * @Description    : Test-And-Set 锁
 */
import java.util.concurrent.atomic.AtomicBoolean;
public class TASLock {

    AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (state.getAndSet(true)) {}
    }

    public void unlock() {
        state.set(false);
    }
}
