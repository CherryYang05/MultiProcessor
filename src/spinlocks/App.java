package spinlocks;

public class App {

    private static int cnt = 0;

    private static final int TEST_TIMES = 10;

    public static final int THREAD_NUM = 12;
    private static final int CIRCLE_TIMES = 100_0000 / THREAD_NUM;

    private static int[] testRes = new int[TEST_TIMES];

    // static ALock lock = new ALock(THREAD_NUM);
    // static TASLock lock = new TASLock();
    // static TTASLock lock = new TTASLock();
    static BackoffLock lock = new BackoffLock();
    // static CLHLock lock = new CLHLock();
    // static MCSLock lock = new MCSLock();
    // static TOLock lock = new TOLock();
    // static SimpleReadWriteLock lock = new SimpleReadWriteLock();

    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= TEST_TIMES; i++) {
            System.out.println("============ Lock Test #" + i + "============");
            long startTime = System.currentTimeMillis();
            long endTime = 0, totalTime = 0;
            cnt = 0;
            // run_synchronized();
            run_lock();
            while (Thread.activeCount() > 1) {
                // System.out.println("Thread Num = " + Thread.activeCount());
            }
            endTime = System.currentTimeMillis();
            totalTime = endTime - startTime;
            testRes[i - 1] = (int) totalTime;
            System.out.println("The final count of CNT is: " + cnt);
            System.out.println("Time consume: " + totalTime + "ms\n");
        }
        System.out.println("Average Time Consume = " + getAverageFromArr(testRes) + "ms");
    }

    synchronized static public void run_lock() {
        System.out.println("Use Lock made by myself");
        System.out.println("Thread Num = " + THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            // try {
            // Thread.sleep(10);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < CIRCLE_TIMES;) {
                        // if (lock.lock()) {
                        //     cnt++;
                        //     j++;
                        //     lock.unlock();
                        // }
                        lock.lock();
                        cnt++;
                        j++;
                        lock.unlock();
                        // System.out.println("CNT = " + cnt);
                    }
                };
            }).start();
        }
    }

    synchronized static public void run_synchronized() {
        System.out.println("Use Synchronized");
        System.out.println("Thread Num = " + THREAD_NUM);
        Object lock = new Object();
        for (int i = 0; i < THREAD_NUM; i++) {
            new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < CIRCLE_TIMES; j++) {
                        // lock.lock();
                        synchronized (lock) {
                            cnt++;
                        }
                        // lock.unlock();
                        // System.out.println("CNT = " + cnt);
                    }
                };
            }).start();
        }
    }

    public static int getAverageFromArr(int[] arr) {
        int size = arr.length;
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += arr[i];
        }
        return sum / size;
    }
}
