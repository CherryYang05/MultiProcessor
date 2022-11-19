/*
 * @Author         : Cherry
 * @Date           : 2022-10-13 15:45:04
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 15:17:48
 * @FilePath       : TOLock.java
 * @Description    : 时限队列锁，指定一个获得锁而准备等待的最大时间，若超时，该线程将它的结点标记为已放弃。线程在队列中的后继将会得知正在自旋的结点已经被放弃，于是在被放弃结点的前驱上自旋。这有一个好处：后继线程能够重用被放弃的结点。而被放弃的结点只能在下一次运行时重新申请锁加入队列。
 */
package spinlocks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TOLock {
    
    /*
     * 时限队列锁结点 QNode 的 pred 有3种类型的取值：
     * 1. null：初始状态，未获得锁或已释放锁；
     * 2. AVAILABLE：一个静态结点，表示对应结点已释放锁，申请锁成功；
     * 3. QNode：pred 域为 null 的前驱结点，表示对应结点因超时放弃锁请求，在放弃请求时才会设置这个值
     */
    static class QNode {
        public volatile QNode pred = null;
    }

    static QNode AVAILABLE = new QNode();   // 静态结点，表示对应结点已释放锁
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public TOLock() {
        tail = new AtomicReference<>(null);
        myNode = new ThreadLocal<>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    /*
     * 申请锁时，创建一个 pred 域为 null 的新结点并加入到链表尾部，若原先链表为空或前驱结点已释放锁，则获得锁。否则，在尝试时间内，找到 pred 域为 null 的前驱结点，等待它释放锁。若在等待前驱结点释放锁的过程中超时，就尝试从链表中删除这个结点，要分这个结点是否有后继两种情况。
     */
    public boolean tryLock(long time, TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        long patience = TimeUnit.MILLISECONDS.convert(time, unit);
        QNode qnode = new QNode();
        myNode.set(qnode);
        qnode.pred = null;
        QNode myPred = tail.getAndSet(qnode);
        // 如果前驱结点为空（即当前结点为初始结点）或前驱结点的 pred 为 AVAILABLE，则表明前驱结点已经释放锁，那么当前结点获得锁
        if (myPred == null || myPred.pred == AVAILABLE) {
            return true;
        }

        // 若未获得锁，找到在队列中的最近的前驱结点，并在其上自旋等待前驱 QNode 结点的 pred 域被改变
        while (System.currentTimeMillis() - startTime < patience) {
            QNode predPred = myPred.pred;
            if (predPred == AVAILABLE) {
                return true;
            } else if (predPred != null) {
                myPred = predPred;
            }
        }

        // 如果超时，则在 tail 上尝试从链表中删除其 QNode，如果 compareAndSet 运行失败，则表示当前结点已经有了后继结点，那么就需要将当前结点的 pred 域设置为前驱结点（方便后续结点获取到正在获得锁的结点）
        if (!tail.compareAndSet(qnode, myPred)) {
            qnode.pred = myPred;
        }
        return false;        
    }
    
    /*
     * 释放锁时，检查该结点是否有后继，若无后继可直接把 tail 设置为 null，否则将该结点的 pred 域指向 AVAILABLE
     */
    public void unlock() {
        QNode qnode = myNode.get();
        if (!tail.compareAndSet(qnode, null)) {
            qnode.pred = AVAILABLE;
        }
    }

    public boolean lock() {
        return tryLock(10, TimeUnit.MILLISECONDS);
    }
}
