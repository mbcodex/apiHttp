package mbTest;


import java.util.ArrayList;
import java.util.List;
import mbTest.utilities.CommonHelper;

public class threadHelper {
	static class MyRunnable implements Runnable {
	    private final long countUntil;

	    MyRunnable(long countUntil) {
	        this.countUntil = countUntil;
	    }

	    @Override
	    public void run() {
	        long sum = 0;
	        for (long i = 1; i < countUntil; i++) {
	            sum += i;
	        }
	        System.out.println(sum);
	    }
	}

	public static void main(String[] args) {
		// We will store the threads so that we can check if they are done
        List<Thread> threads = new ArrayList<Thread>();
        // We will create 500 threads
        for (int i = 0; i < 500; i++) {
            Runnable task = new threadHelper.MyRunnable(10000000L + i);
            Thread worker = new Thread(task);
            // We can set the name of the thread
            worker.setName(String.valueOf(i));
            // Start the thread, never call method run() direct
            worker.start();
            // Remember the thread for later usage
            threads.add(worker);
        }
        int running = 0;
        do {
            running = 0;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    running++;
                }
            }
            System.out.println("We have " + running + " running threads. ");
        } while (running > 0);

	}

}
