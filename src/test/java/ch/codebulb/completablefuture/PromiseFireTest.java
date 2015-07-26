package ch.codebulb.completablefuture;

import static ch.codebulb.completablefuture.PromiseTestUtil.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class PromiseFireTest {
    private static final long WAIT_MILIS = 0;
    private final List<Completion> completions = new ArrayList<>();
    
    public void testSimpleFulfill() {
        // see BasicTest#testPromiseExplicitlyFulfilled()
    }
    
    @Test
    public void testFulfilAndReject() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> log(it));
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.complete("Future explicitly fulfilled"));
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            assertFalse(promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected")));
            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"));
        });
    }
    
    @Test
    public void testFulfilTwice() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.complete("Future explicitly fulfilled"));
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            assertFalse(promise.complete("Future explicitly fulfilled 2"));
            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"));
        });
    }
    
    @Test
    public void testFulfilTwiceWith2Listeners() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> print(it));
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.complete("Future explicitly fulfilled"));
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            assertFalse(promise.complete("Future explicitly fulfilled 2"));
            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"), completed("Future explicitly fulfilled"));
        });
    }
    
    @Test
    public void testRejectAndFulfil() {
        // 1a - build the task
        final CompletableFuture<String> future = new CompletableFuture<>();
        // 1b - define task result processing
        future.thenAccept(it -> print(it));
        future.exceptionally(it -> log(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(future.completeExceptionally(new MyPromiseRejectedException("Promise rejected")));
            assertTrue(future.isDone());
            assertTrue(future.isCompletedExceptionally());
            
            assertFalse(future.complete("Future explicitly fulfilled"));
            
            // TEST --- assertions -- remove from production code
            assertEquals(completions, exception("Promise rejected"));
        });
    }
    
    @Test
    public void testFulfilAndRejectForced() {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> log(it));
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.complete("Future explicitly fulfilled"));
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            promise.obtrudeException(new MyPromiseRejectedException("Promise rejected"));
            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"));
            try {
                promise.get();
                fail();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                Assert.assertEquals(MyPromiseRejectedException.class, ex.getCause().getClass());
            }
        });
    }
    
    @Test
    public void testFulfilTwiceForced() throws InterruptedException, ExecutionException {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.exceptionally(it -> log(it));
        promise.thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.complete("Future explicitly fulfilled"));
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            promise.obtrudeValue("Future explicitly fulfilled 2");
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future explicitly fulfilled"));
            try {
                Assert.assertEquals("Future explicitly fulfilled 2", promise.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
    @Test
    public void testRejectAndFulfilForced() throws InterruptedException, ExecutionException {
        // 1a - build the task
        final CompletableFuture<String> promise = new CompletableFuture<>();
        // 1b - define task result processing
        promise.thenAccept(it -> print(it));
        promise.exceptionally(it -> log(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            assertTrue(promise.completeExceptionally(new MyPromiseRejectedException("Promise rejected")));
            assertTrue(promise.isDone());
            assertTrue(promise.isCompletedExceptionally());
            
            promise.obtrudeValue("Future explicitly fulfilled");
            assertTrue(promise.isDone());
            assertFalse(promise.isCompletedExceptionally());
            
            // TEST --- assertions -- remove from production code
            assertEquals(completions, exception("Promise rejected"));
            try {
                Assert.assertEquals("Future explicitly fulfilled", promise.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
    private void print(String input) {
        assertNotNull(input);
        completions.add(completed(input));
        // do something with input, e.g. print it
    }
    
    private String log(Throwable ex) {
        assertNotNull(ex);
        Assert.assertEquals(MyPromiseRejectedException.class, ex.getClass());
        completions.add(exception(ex.getMessage()));
        throw new RuntimeException(ex);
    }
}
