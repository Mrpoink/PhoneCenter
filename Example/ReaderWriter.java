import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderWriter {
    private static String sharedData = "Shared data";
    private static int numReaders = 0;
    private static ReentrantLock numReadersLock = new ReentrantLock();
    private static Semaphore dataLock = new Semaphore(1); //semaphore needs 1 permit to act as lock

    public static class Writer implements Runnable{
        public void run(){
            try{
                dataLock.acquire();
                //Acquire, add, release
                sharedData += "+";
            }catch(InterruptedException e){
                e.printStackTrace();
            }finally{
                dataLock.release();
            }
        }
    }
    public static class Readers implements Runnable{
        public void run(){
            numReadersLock.lock();
            try {
                numReaders++;
                if (numReaders == 1) { //First reader in system
                    try {
                        dataLock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }finally{
                numReadersLock.unlock();
            }
            System.out.println(sharedData);
            numReadersLock.lock();
            try {
                numReaders--;
                if (numReaders == 0) { //Last reader in system releases permit
                    dataLock.release();
                }
            }finally{
                numReadersLock.unlock();
            }
        }
    }

}
