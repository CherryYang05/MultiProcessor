package spinlocks;
import java.util.Random;

/**
 * 争用锁时进程的指数退避算法
 */
public class Backoff {
    int minDelay, maxDelay;     // 限定最大和最小时延，避免无意义的过小的后退以及无限制后退
    int limit;                  // 当前的时延限制
    Random random = null;

    /**
     * @description: 构造器
     * @return      {*}
     */    
    public Backoff(int min, int max) {
        minDelay = min;
        maxDelay = max;
        limit = min;
        random = new Random();    
    }

    /**
     * @description: 在 0 和 limit 之间随机选取一个值进行退避，
     *               然后倍乘 limit，但是不能超过 maxDelay
     * @return      {*}
     */    
    public void backoff() {
        int delay = random.nextInt(limit);
        limit = Math.max(maxDelay, limit * 2);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
