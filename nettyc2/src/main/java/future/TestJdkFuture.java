package future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("cal");
                Thread.sleep(1000);
                return 50;
            }
        });
        log.debug("wait");
        log.debug("{}",future.get());

    }
}
