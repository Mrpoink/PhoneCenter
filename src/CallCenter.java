import java.util.*;
import java.util.concurrent.*;
import static java.lang.Thread.sleep;

public class CallCenter {

    private static final int CUSTOMERS_PER_AGENT = 5;
    private static final int NUMBER_OF_AGENTS = 3;
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;

    // Shared resources
    private static final Queue<Customer> waitQueue = new LinkedList<>();
    private static final Queue<Customer> serveQueue = new LinkedList<>();

    // Synchronization
    private static final Semaphore lock = new Semaphore(1);
    private static final Semaphore waitItems = new Semaphore(0);
    private static final Semaphore serveItems = new Semaphore(0);

    public static class Agent implements Runnable {
        private final int ID;

        public Agent(int i) {
            ID = i;
        }

        public void serve(int CID) {
            System.out.println("Agent " + ID + " is serving customer " + CID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void run() {
            for (int i = 0; i < CUSTOMERS_PER_AGENT; i++) {
                try {
                    serveItems.acquire(); // Wait until a customer is in the serveQueue
                    lock.acquire();
                    Customer cust = serveQueue.poll(); // Remove from serveQueue
                    lock.release();

                    if (cust != null) {
                        serve(cust.ID);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static class Greeter implements Runnable {
        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void run() {
            for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
                try {
                    waitItems.acquire(); // Wait until a customer is in the waitQueue
                    lock.acquire();
                    Customer cust = waitQueue.poll(); // Remove from waitQueue
                    lock.release();

                    if (cust != null) {
                        greet(cust.ID);

                        lock.acquire();
                        serveQueue.add(cust); // Add to serveQueue
                        lock.release();

                        System.out.println("Customer " + cust.ID + " placed in serve queue.");
                        serveItems.release(); // Signal a customer is ready for serving
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static class Customer implements Runnable {
        private final int ID;

        public Customer(int id) {
            ID = id;
        }

        public void run() {
            try {
                lock.acquire();
                waitQueue.add(this); // Add to waitQueue
                System.out.println("Customer " + ID + " entered wait queue.");
                lock.release();
                waitItems.release(); // Signal a customer is waiting to be greeted
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CUSTOMERS + NUMBER_OF_AGENTS + 1);

        // Start the greeter
        executor.execute(new Greeter());

        // Start the agents
        for (int i = 1; i <= NUMBER_OF_AGENTS; i++) {
            executor.execute(new Agent(i));
        }

        // Start the customers
        for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++) {
            executor.execute(new Customer(i));
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 200)); // simulate random arrival
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("Call center simulation completed.");
    }
}
