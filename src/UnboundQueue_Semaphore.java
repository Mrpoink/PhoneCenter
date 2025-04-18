import java.util.*;
import java.util.concurrent.*;

public class UnboundQueue_Semaphore {

    private final static Queue<Integer> buffer = new LinkedList<>();
//    private final static ReentrantLock lock = new ReentrantLock();
    private final static Semaphore items = new Semaphore(0);
    private final static int CAPACITY = 4;
    private final static Semaphore empty = new Semaphore(CAPACITY);
    private final static Semaphore locky = new Semaphore(1);


    public static class Producer implements Runnable{
        private int data;
        public Producer (int k) {this.data = k;}
        public void run(){
            try{
                try {
                    locky.acquire();
                    empty.acquire();
                    buffer.add(data);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }finally{
                locky.release();
            }
            items.release();
        }
    }

    public static class Consumer implements Runnable{
        public void run(){
            int data;
            try {
                items.acquire();
                locky.acquire();
                data = buffer.remove();
                System.out.println(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally{
                locky.release();
            }
            empty.release();

        }
    }

    public static void main(String[] args){
        ExecutorService es = Executors.newFixedThreadPool(23);

        for (int i =0; i < 10; i++){
            es.submit(new Consumer());
            es.submit(new Producer(i));

        }
        es.shutdown();
    }
}
