package spinlocks;
/*
 * @Author         : Cherry
 * @Date           : 2022-10-09 16:20:31
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 14:29:34
 * @FilePath       : ALock.java
 * @Description    : 基于数组的队列锁
 */
import java.util.concurrent.atomic.AtomicInteger;

public class ALock {

    // mySLotIndex 是线程的局部变量，线程局部变量无需保存在共享存储器中，无需同步，不产生一致性流量
    ThreadLocal<Integer> mySlotIndex = new ThreadLocal<>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    AtomicInteger tail;
    volatile boolean[] flag;
    private int size;

    public ALock(int capacity) {
        size = capacity;
        tail = new AtomicInteger(0);
        flag = new boolean[capacity];
        // 为了避免假共享现象，可以将数组开大，让每一个项独占一个 cache 行
        // flag = new boolean[capacity * 4];
        flag[0] = true;
    }
    
    public void lock() {
        int slot = tail.getAndIncrement() % size;
        mySlotIndex.set(slot);
        while (!flag[slot]) {};     // 当前域为 false 则表明锁被占用，陷入空转等待
    }

    public void unlock() {
        int slot = mySlotIndex.get();
        flag[slot] = false;
        flag[(slot + 1) % size] = true;
    }
}
