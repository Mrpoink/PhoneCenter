package Project3;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CallCenter {

    private static final int CUSTOMERS_PER_AGENT = 5;
    private static final int NUMBER_OF_AGENTS = 3;
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;
    private static final int NUMBER_OF_THREADS = 10;

    // Shared queues
    private static final BlockingQueue<Customer> waitQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Customer> serveQueue = new LinkedBlockingQueue<>();

    public static class Agent implements Runnable {
        private final int ID;

        public Agent(int i) {
            ID = i;
        }

        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void run() {
            for (int i = 0; i < CUSTOMERS_PER_AGENT; i++) {
                try {
                    Customer customer = serveQueue.take();
                    serve(customer.getID());
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
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void run() {
            for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
                try {
                    Customer customer = waitQueue.take();
                    greet(customer.getID());
                    serveQueue.put(customer);
                    System.out.println("Customer " + customer.getID() + " added to serve queue.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static class Customer implements Runnable {
        private final int ID;

        public Customer(int i) {
            ID = i;
        }

        public int getID() {
            return ID;
        }

        public void run() {
            try {
                waitQueue.put(this);
                System.out.println("Customer " + ID + " added to wait queue.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        // Start greeter
        executor.execute(new Greeter());

        // Start agents
        for (int i = 1; i <= NUMBER_OF_AGENTS; i++) {
            executor.execute(new Agent(i));
        }

        // Start customers with staggered arrival
        for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++) {
            executor.execute(new Customer(i));
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Simulation completed.");
    }
}

