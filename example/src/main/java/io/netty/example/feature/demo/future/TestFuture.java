package io.netty.example.feature.demo.future;

import java.util.Random;

public class TestFuture {

    public static void main(String[] args) throws InterruptedException {
        RollingDiceFuture rollingDiceFuture = new RollingDiceFuture();
        Integer result = rollingDiceFuture.get();
        System.out.println(result);

        // AbstractCallableFuture 跟 RollingDiceFuture做的事情是一样的, 不同在于前者通过暴露一个call()方法给子类实现
        // 达到这个Future做的事情是不固定(这里刚好还是抛骰子)
        AbstractCallableFuture rollingDiceCallableFuture = new AbstractCallableFuture() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(3000);
                return new Random(System.currentTimeMillis()).nextInt(6) + 1;
            }
        };
        result = rollingDiceCallableFuture.get();
        System.out.println(result);
    }

}
