package spinlocks;

/*
 * @Author         : Cherry
 * @Date           : 2022-10-09 19:09:39
 * @LastEditors    : Cherry
 * @LastEditTime   : 2022-10-18 14:29:31
 * @FilePath       : CLHLock.java
 * @Description    : CLHLock 队列锁
 */
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CLHLock {

    private static class QNode {
        volatile AtomicBoolean locked = new AtomicBoolean(false);
    }

    AtomicReference<QNode> tail = null;
    ThreadLocal<QNode> myPred = null;
    ThreadLocal<QNode> myNode = null;

    public CLHLock() {
        tail = new AtomicReference<>(new QNode());
        myNode = new ThreadLocal<>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
        myPred = new ThreadLocal<>() {
            protected QNode initialValue() {
                return null;
            }
        };
    }

    /**
     * 详细解释一下 CLH 锁的工作原理：tail 为全局变量，在所有线程之间共享，每个线程有两个独立的局部变量，
     * 为 myPred 和 myNode，表示当前自己的 QNode 结点和前驱 QNode 结点。
     * 当 A 线程尝试获取锁时，首先获取 myNode 的值，表明当前的 QNode 结点，然后将结点内的 locked 字段设为 true，表明线程 A 不准备释放锁（即想要申请锁），然后取出 tail 的值（此时 tail 记录的是虚拟链表的尾部的 QNode，实际上只是一个变量，并不是链表）赋值给 myPred，表明当前 QNode 的前驱结点，并将当前结点赋值给 tail。在 A 线程获得锁之前，要在 myPred 上空转等待，等待 myPred.locked 为 false 即可获得锁。
     * 当锁申请成功时，tail 存放的结点是当前的 myNode 结点，myPred 是前驱结点，myNode 和 tail 存放的是同一个结点。若要释放锁时，将当前的结点的 locked 字段改为 false，表明释放该锁，然后将当前结点用前驱结点覆盖（myNode.set(myPred.get())），这句话非常重要，这样可以避免同一个线程重复加锁时导致自旋等待，此时 myNode 和 myPred 指向的 QNode 便交换了一下，
     */
    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked.set(true);
        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);
        while (pred.locked.get()) {
            // try {
            //     Thread.sleep(50);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            // System.out.println("线程 " + Thread.currentThread().getName() + " 在自旋等待...");
        }
        // System.out.println("线程 " + Thread.currentThread().getName() + " 获取到了锁！");
    }

    public void unlock() {
        QNode qnode = myNode.get();
        qnode.locked.set(false);;       // 通知后继线程
        myNode.set(myPred.get());       // 这句非常重要，避免同一个线程重复加锁导致自旋等待
        // System.out.println("线程 " + Thread.currentThread().getName() + " 释放了锁！");
    }
}
