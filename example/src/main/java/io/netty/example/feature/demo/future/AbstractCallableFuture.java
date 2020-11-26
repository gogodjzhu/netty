package io.netty.example.feature.demo.future;


import java.util.concurrent.*;

public abstract class AbstractCallableFuture implements Future<Integer>, Callable<Integer> {

    private Thread thread;

    private Integer result;

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
