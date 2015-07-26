package ch.codebulb.completablefuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CompletableFutureUtil {
    private CompletableFutureUtil() {}
    
    /**
     * Returns a new CompletableFuture that is completed when all of
     * the given CompletableFutures complete.  If any of the given
     * CompletableFutures complete exceptionally, then the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause. The results,
     * if any, of the given CompletableFutures are reflected in
     * the returned CompletableFuture. If no CompletableFutures are
     * provided, returns a CompletableFuture completed with the value
     * {@code null}.
     *
     * <p>Among the applications of this method is to await completion
     * of a set of independent CompletableFutures before continuing a
     * program, as in: {@code CompletableFutureUtil.allOf(c1, c2,
     * c3).join();}.
     *
     * @param cfs the CompletableFutures
     * @return a new CompletableFuture that is completed when all of the
     * given CompletableFutures complete
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    // adapted from http://www.nurkiewicz.com/2013/05/java-8-completablefuture-in-action.html
    public static <T> CompletableFuture<List<T>> allOf(CompletableFuture<T>... cfs) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(cfs);
        return allDoneFuture.thenApply(it ->
            Arrays.stream(cfs).
                map(future -> future.join()).
                collect(Collectors.toList())
        );
    }
    
    /**
     * Returns a new CompletableFuture that is completed when any of
     * the given CompletableFutures complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  If no CompletableFutures
     * are provided, returns an incomplete CompletableFuture.
     *
     * @param cfs the CompletableFutures
     * @return a new CompletableFuture that is completed with the
     * result or exception of any of the given CompletableFutures when
     * one completes
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    public static <T> CompletableFuture<T> anyOf(CompletableFuture<T>... cfs) {
        return (CompletableFuture<T>) CompletableFuture.anyOf(cfs);
    }
}
