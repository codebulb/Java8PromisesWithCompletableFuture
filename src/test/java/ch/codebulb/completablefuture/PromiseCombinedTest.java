package ch.codebulb.completablefuture;

import static ch.codebulb.completablefuture.PromiseTestUtil.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PromiseCombinedTest {
    private static final long WAIT_MILIS = 0;
    private final List<PromiseTestUtil.Completion> completions = new ArrayList<>();
    
    @Test
    public void testCombineAll() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        final CompletableFuture<Void> promiseCombined = 
                // Note that "it" is of type Void.
                CompletableFuture.allOf(promise1, promise2).thenAccept(it -> inform(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            promiseCombined.join();
            assertEquals(completions, completed("Completed"));
        });
    }
    
    @Test
    public void testCombineAllCustomized() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        final CompletableFuture<Void> promiseCombined = 
                CompletableFutureUtil.allOf(promise1, promise2).
                        thenAccept(all -> all.stream().forEach(it -> print(it)));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            promiseCombined.join();
            assertEquals(completions, completed("Future 1 explicitly fulfilled"), completed("Future 2 explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombine2Promises() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.thenCombine(promise2, (v1, v2) -> combine(v1, v2)).thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future 1 explicitly fulfilled + Future 2 explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombine2PromisesWithTerminalOperation() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.thenAcceptBoth(promise2, (v1, v2) -> {print(v1); print(v2);});
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future 1 explicitly fulfilled"), completed("Future 2 explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombine2PromisesWithRunnable() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.runAfterBoth(promise2, () -> print("Both Futures explicitly fulfilled"));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Both Futures explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombineAny() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        final CompletableFuture<Void> promiseCombined = 
                CompletableFuture.anyOf(promise1, promise2).thenAccept((it) -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise1.join(); // make sure promise1 gets fulfilled first
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            promiseCombined.join();
            assertEquals(completions, completed("Future 1 explicitly fulfilled as Object"));
        });
    }
    
    @Test
    public void testCombineAnyCustomized() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        final CompletableFuture<Void> promiseCombined = 
                CompletableFutureUtil.anyOf(promise1, promise2).thenAccept((it) -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise1.join(); // make sure promise1 gets fulfilled first
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            promiseCombined.join();
            assertEquals(completions, completed("Future 1 explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombineAnyOf2Promises() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.applyToEither(promise2, it -> transform(it)).thenAccept(it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise1.join(); // make sure promise1 gets fulfilled first
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future 1 explicitly fulfilled"), completed("Future 1 explicitly fulfilled TRANSFORMED"));
        });
    }
    
    @Test
    public void testCombineAnyOf2PromisesWithTerminalOperation() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.acceptEither(promise2, it -> print(it));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise1.join(); // make sure promise1 gets fulfilled first
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("Future 1 explicitly fulfilled"));
        });
    }
    
    @Test
    public void testCombineAnyOf2PromisesWithRunnable() {
        // 1a - build the task
        final CompletableFuture<String> promise1 = new CompletableFuture<>();
        final CompletableFuture<String> promise2 = new CompletableFuture<>();
        // 1b - define task result processing
        promise1.runAfterEither(promise2, () -> print("One of the two Futures explicitly fulfilled"));
        
        // 2 - start the task
        startThread(() -> {
            sleep(WAIT_MILIS);
            promise1.complete("Future 1 explicitly fulfilled");
            promise2.complete("Future 2 explicitly fulfilled");

            // TEST --- assertions -- remove from production code
            assertEquals(completions, completed("One of the two Futures explicitly fulfilled"));
        });
    }
    
    private void inform(Void input) {
        assertNull(input);
        completions.add(completed("Completed"));
        // do something with input, e.g. print it
    }
    
    private void print(Object input) {
        assertNotNull(input);
        completions.add(completed(input + " as Object"));
        // do something with input, e.g. print it
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
    
    private String combine(String input1, String input2) {
        assertNotNull(input1);
        assertNotNull(input2);
        
        return input1 + " + " + input2;
    }
}
