package ch.codebulb.completablefuture;

import static ch.codebulb.completablefuture.PromiseTestUtil.sleep;
import static ch.codebulb.completablefuture.PromiseTestUtil.startThread;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BasicTest {
    private static final long WAIT_MILIS = 0;
    private boolean called;
    
    @Test
    public void testFuture() {
        // 1 - build the task
        FutureTask<String> retrieveName = new FutureTask<>(() -> {
            sleep(WAIT_MILIS);
            return "Future";
        });
        
        // 2 - start the task
        startThread(() -> {
            retrieveName.run();
        
            // 3 - collect the result
            try {
                print(retrieveName.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }

            // TEST --- assertions -- remove from production code
            assertTrue(called);
        });
    }
    
    private static class RunnableCallback implements Runnable {
        private final Consumer<String> callback;
        
        public RunnableCallback(Consumer<String> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            sleep(WAIT_MILIS);
            callback.accept("FutureCallback");
        }
    }
    
    @Test
    public void testRunnableWithCallback() {
        // 1a - build the task; 1b - define task result processing
        // 2 - start the task
        startThread(() -> {
            new RunnableCallback(it -> print(it)).run();
            
            // TEST --- assertions -- remove from production code
            assertTrue(called);
        });
    }
    
    @Test
    public void testPromise() {
        // 1a - build the task;
        final CompletableFuture<String> retrieveName = CompletableFuture.supplyAsync(() -> {
            sleep(WAIT_MILIS);
            return "Promise";
        });
        // 1b - define task result processing
        retrieveName.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            try {
                retrieveName.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }

            // TEST --- assertions -- remove from production code
            assertTrue(called);
        });
        
    }
    
    @Test
    public void testPromiseExplicitlyFulfilled() {
        // 1a - build the task
        final CompletableFuture<String> future = new CompletableFuture<>();
        // 1b - define task result processing
        future.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            future.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertTrue(called);
        });
    }
    
    private void print(String input) {
        Assert.assertNotNull(input);
        called = true;
        // do something with input, e.g. print it
    }
}
