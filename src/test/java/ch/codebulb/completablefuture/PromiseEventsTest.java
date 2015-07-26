package ch.codebulb.completablefuture;

import static ch.codebulb.completablefuture.PromiseTestUtil.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class PromiseEventsTest {
    private static final long WAIT_MILIS = 0;
    private final List<PromiseTestUtil.Completion> completions = new ArrayList<>();
    
    public void testFulfill() {
        // see BasicTest#testPromiseExplicitlyFulfilled()
    }
    
    @Test
    public void testFulfilWithRunnable() {
        // 1a - build the task
        final CompletableFuture<String> future = new CompletableFuture<>();
        // 1b - define task result processing
        future.thenRun(() -> print("Future explicitly fulfilled"));
        
        // 2 - start the task
        sleep(WAIT_MILIS);
        future.complete("Future explicitly fulfilled");
        
        // TEST --- assertions -- remove from production code
        assertEquals(completions, completed("Future explicitly fulfilled"));
    }
    
    @Test
    public void testReject() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> log(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected"));
            
            // TEST --- assertions -- remove from production code
            try {
                promise.get();
                fail();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                Assert.assertEquals(PromiseTestUtil.MyPromiseRejectedException.class, ex.getCause().getClass());
            }
            assertEquals(completions, exception("Promise rejected"));
        });
    }
    
    @Test
    public void testRejectWithoutListener() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected"));
            
            // TEST --- assertions -- remove from production code
            try {
                promise.get();
                fail();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                Assert.assertEquals(PromiseTestUtil.MyPromiseRejectedException.class, ex.getCause().getClass());
            }
            
            assertEquals(completions);
        });
    }
    
    @Test
    public void testFulfill2Listeners() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> print(it));
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"), completed("Future explicitly fulfilled"));
        });
    }
    
    @Test
    public void testChainCallbacks() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenApply(it -> transform(it)).thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions,
                    completed("Future explicitly fulfilled"), completed("Future explicitly fulfilled TRANSFORMED"));
        });
    }
    
    @Test
    public void testChainCallbacksNaive() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> { print(transform(it));} );
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions,
                    completed("Future explicitly fulfilled"), completed("Future explicitly fulfilled TRANSFORMED"));
        });
    }
    
    @Test
    public void testChainCombinedCallbacks() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.handle((it, err) -> {
            if (it != null) {
                return transform(it);
            }
            else {
                return log(err);
            }
        }).whenComplete((it, err) -> {
            if (it != null) {
                print(it);
            }
            else {
                log(err);
            }
        });
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions,
                    completed("Future explicitly fulfilled"), completed("Future explicitly fulfilled TRANSFORMED"));
        });
    }
    
    @Test
    public void testChainCallbacksEventuallyRejected() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.<String> thenApply(it -> {throw new RuntimeException("Promise rejected");})
                .thenApply(it -> transform(it))
                .thenApply(it -> transform(it))
                .whenComplete((it, err) -> {
                    if (it != null) {
                        print(it);
                    } else {
                        logAccidentalException((CompletionException) err);
                    }
                });
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.complete("Future explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            promise.join();
            assertEquals(completions,
                    exception("Promise rejected"));
        });
    }
    
    @Test
    public void testExceptionHandlingWithCallbacks() throws InterruptedException, ExecutionException {
        String output = CompletableFuture.supplyAsync(() -> {
            // do stuff
            return "Promise";
        }).whenComplete((it, err) -> {
            if (it != null) {
                print(it);
            }
            else {
                log(err);
            }
        }).get();
        
        // TEST --- assertions -- remove from production code
        Assert.assertEquals("Promise", output);
        assertEquals(completions, completed("Promise"));
    }
    
    @Test
    public void testRejectWithoutRecover() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> log(it)).thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected"));

            // TEST --- assertions -- remove from production code
            assertEquals(completions, exception("Promise rejected"));
        });
    }
    
    @Test
    public void testRejectWithRecover() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> fix(it)).thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected"));

            // TEST --- assertions -- remove from production code
            assertEquals(completions, exception("Promise rejected"), completed("Recovered"));
        });
    }
    
    @Test
    public void testChainCallbacksWithCompose() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.thenAccept(it -> print(it)).thenCompose(it1 -> promise2);
        promise2.thenAccept(it2 -> print(it2));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions,
                    completed("Future 1 explicitly fulfilled"), completed("Future 2 explicitly fulfilled"));
        });
    }
    
    private void print(String input) {
        assertNotNull(input);
        completions.add(completed(input));
        // do something with input, e.g. print it
    }
    
    private String transform(String input) {
        assertNotNull(input);
        completions.add(completed(input));
        input = input + " TRANSFORMED";
        
        return input;
    }
    
    private String log(Throwable ex) {
        assertNotNull(ex);
        Assert.assertEquals(MyPromiseRejectedException.class, ex.getClass());
        completions.add(exception(ex.getMessage()));
        throw new RuntimeException(ex);
    }
    
    private String logAccidentalException(CompletionException ex) {
        assertNotNull(ex);
        assertTrue(ex.getCause() instanceof RuntimeException);
        completions.add(exception(ex.getCause().getMessage()));
        throw new RuntimeException(ex);
    }
    
    private String fix(Throwable ex) {
        Assert.assertNotNull(ex);
        Assert.assertEquals(MyPromiseRejectedException.class, ex.getClass());
        completions.add(exception(ex.getMessage()));
        return "Recovered";
    }
}
