package Project3;

/*
    You can import any additional package here.
 */
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class CallCenter {

    /*
       Total number of customers that each agent will serve in this simulation.
       (Note that an agent can only serve one customer at a time.)
     */
    private static final int CUSTOMERS_PER_AGENT = 5;

    /*
       Total number of agents.
     */
    private static final int NUMBER_OF_AGENTS = 3;

    /*
       Total number of customers to create for this simulation.
     */
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;

    /*
      Number of threads to use for this simulation.
     */

    private static final Queue<Customer> queue = new LinkedList<>();
    private static final Queue<Customer> wait = new LinkedList<>();

    private final static Semaphore empty = new Semaphore(NUMBER_OF_AGENTS);
    private final static Semaphore locky = new Semaphore(1);
    private final static Semaphore items = new Semaphore(0);


    /*
       The Agent class.
     */
    public static class Agent implements Runnable {
        //TODO: complete the agent class
        //The ID of the agent
        private final int ID;

        //Feel free to modify the constructor
        public Agent(int i) {
            ID = i;
        }

        /*
        Your implementation must call the method below to serve each customer.
        Do not modify this method.
         */
        public void serve(int CID) {
            System.out.println("Agent " + ID + " is serving customer " + CID);
            try {
                /*
                   Simulate busy serving a customer by sleeping for a random amount of time.
                */
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void run() {
            try {
                try {
                    locky.acquire();
                    empty.acquire();
                    Customer cust = queue.peek();
                    assert cust != null;
                    serve(cust.ID);
                    queue.remove(cust);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                locky.release();
            }
            empty.release();
        }
    }

        /*
            The greeter class.
         */
        public static class Greeter implements Runnable {
            //TODO: complete the Greeter class

            /*
               Your implementation must call the method below to serve each customer.
               Do not modify this method.
                */
            public void greet(int customerID) {
                System.out.println("Greeting customer " + customerID);
                try {
                    /*
                    Simulate busy serving a customer by sleeping for a random amount of time.
                    */
                    sleep(ThreadLocalRandom.current().nextInt(10, 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void run() {
                try {
                    try {
                        locky.acquire();
                        empty.acquire();
                        Customer cust = wait.peek();
                        assert cust != null;
                        greet(cust.ID);
                        wait.remove(cust);
                        queue.add(cust);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }finally{
                    locky.release();
                }
                empty.release();
            }
        }

        /*
            The customer class.
         */
        public static class Customer implements Runnable {
            //TODO: complete the Customer class
            //The ID of the customer.
            private final int ID;


            //Feel free to modify the constructor
            public Customer(int id) {
                ID = id;
            }

            public void run() {
                try{
                    try{
                        locky.acquire();
                        empty.acquire();
                        Customer cust = new Customer(ID);
                        wait.add(cust);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }finally{
                    locky.release();
                }
                empty.release();

            }
        }

        /*
            Create the greeter and agents tasks first, and then create the customer tasks.
            to simulate a random interval between customer calls, sleep for a random period after creating each customer task.
         */
        public static void main(String[] args) {
            //TODO: complete the main method
            Semaphore userlock = new Semaphore(NUMBER_OF_CUSTOMERS);
            ExecutorService es = Executors.newFixedThreadPool(23);
            for (int j = 0; j < NUMBER_OF_CUSTOMERS; j++) {
                System.out.println("j" + j);
                es.submit(new Customer(j));
                try {
                    sleep(ThreadLocalRandom.current().nextInt(0, 23));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            es.submit(new Greeter());
            for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
                System.out.println("i" +i);
                es.submit(new Agent(i));
            }
            es.shutdown();
        }

    }

