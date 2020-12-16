package pl.edu.pw.aasd;

public class SafeThread extends Thread {

    public static void run(SafeRunnable runner, int offset) {
        new Thread(() -> {
            try {
                Thread.sleep(offset);
                runner.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
    }

    public static void run(SafeRunnable runner) {
        run(runner, 0);
    }

    public interface SafeRunnable {
        void run() throws Throwable;
    }
}
