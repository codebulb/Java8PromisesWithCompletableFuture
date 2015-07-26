package ch.codebulb.completablefuture;

import java.util.List;
import java.util.Objects;
import org.junit.Assert;

public class PromiseTestUtil {
    private PromiseTestUtil() {}
    
    public static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static class MyPromiseRejectedException extends Exception {
        public MyPromiseRejectedException(String message) {
            super(message);
        }
    }
    
    public static class MyPromiseRejectedRuntimeException extends RuntimeException {
        public MyPromiseRejectedRuntimeException(String message) {
            super(message);
        }
    }
    
    public static Completion completed(String text) {
        return new Completion(text, null);
    }
    
    public static Completion exception(String exceptionText) {
        return new Completion(null, exceptionText);
    }
    
    public static class Completion {
        private final String text;
        private final String exceptionText;

        public Completion(String text, String exceptionText) {
            this.text = text;
            this.exceptionText = exceptionText;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.text);
            hash = 89 * hash + Objects.hashCode(this.exceptionText);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Completion other = (Completion) obj;
            if (!Objects.equals(this.text, other.text)) {
                return false;
            }
            if (!Objects.equals(this.exceptionText, other.exceptionText)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Completion{" + "text=" + text + ", exceptionText=" + exceptionText + '}';
        }
    }
    
    public static void assertEquals(List<Completion> actual, Completion... expected) {
        Assert.assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i], actual.get(i));
        }
    }
}
