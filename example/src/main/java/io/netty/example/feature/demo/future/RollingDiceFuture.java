package io.netty.example.feature.demo.future;


import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 模拟一个简单的异步延迟结果, 这个结果的处理方式是固定的
 */
public class RollingDiceFuture implements Future<Integer> {

    // result 即是结果, 也标识当前Future的状态: 为null标识仍在等待结果, 不为null表示计算结束
    private Integer result;

    private final Thread thread;

    public RollingDiceFuture() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    result = new Random(System.currentTimeMillis()).nextInt(6) + 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.thread.start();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning && result != null) { // 存在竞态问题, 暂且不考虑细节
            //
            thread.interrupt();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return result == null;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public Integer get() throws InterruptedException {
        while (result == null) {
            Thread.sleep(500);
        }
        return result;
    }

    @Override
    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long start = System.currentTimeMillis();
        long timeoutMillis = start + unit.toMillis(timeout);
        while (result == null && System.currentTimeMillis() < timeoutMillis) {
            Thread.sleep(500);
        }
        if (result == null) {
            throw new TimeoutException();
        }
        return result;
    }
}
