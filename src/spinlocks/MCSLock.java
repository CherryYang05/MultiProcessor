package spinlocks;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/*
 * @Author         : Cherry
 * @Date           : 2022-10-10 10:14:07
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 14:29:49
 * @FilePath       : MCSLock.java
 * @Description    : MCS 队列锁
 */
public class MCSLock {

    private static class QNode {
        volatile AtomicBoolean locked = new AtomicBoolean(false);
        volatile QNode next = null;
    }

    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public MCSLock() {
        tail = new AtomicReference<>(null);
        myNode = new ThreadLocal<>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    /**
     * 详细解释 MCS 加锁的过程：
     */
    public void lock() {
        QNode qnode = myNode.get();
        QNode pred = tail.getAndSet(qnode);
        if (pred != null) {
            qnode.locked.set(true);;
            pred.next = qnode;
            while (qnode.locked.get()) {
                // Thread.yield();
                // System.out.println("当前线程 " + Thread.currentThread().getName() + " 的前一个线程未释放锁，自旋等待");
            }
        }
        // System.out.println("线程 " + Thread.currentThread().getName() + " 已获得锁～～");
    }

    /**
     * MCS 释放锁的过程：检查当前结点是否为链表最后一个结点，如果不是最后一个结点，且 qnode.next 为空，
     * 说明下一个结点已经准备申请锁，但是结点的链接还未完成，这时当前线程释放锁需要自旋等待，直到下一个线程
     * 将链表链接完成。
     * 
     * 最后一句：qnode.next = null 是必要的。若线程释放锁后不将 next 指针置空，则下列这种线程执行情况
     * 将导致线程忙等：A.lock() -> B.lock() -> A.unlock() -> A.lock() -> B.unlock() -> A.unlock() -> A.lock()，
     * 即当线程队列形成回路时，释放锁时分支 if (qnode.next == null) 将不会进入正确的代码块执行，
     * 导致 tail 不会被置为 null，当下一次申请锁时，就会导致无限空转
     */
    public void unlock() {
        QNode qnode = myNode.get();
        if (qnode.next == null) {
            // System.out.println("这里卡死。。。");
            if (tail.compareAndSet(qnode, null)) {
                return;
            }
            while (qnode.next == null) {
                // System.out.println("线程 " + Thread.currentThread().getName() + " 未链接好，正在等待链接。。。");
            }
        }
        qnode.next.locked.set(false);
        qnode.next = null;              // 这句是必要的
        // System.out.println("线程 " + Thread.currentThread().getName() + " 已释放锁##");
    }
}
