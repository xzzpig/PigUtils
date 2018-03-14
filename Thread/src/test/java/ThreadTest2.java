import org.junit.Test;

public class ThreadTest2 {
    @Test
    public void testTimeout() {
        Object obj = new Object();
        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (obj) {

                obj.notifyAll();
            }
        }).start();
        synchronized (obj) {
            try {
                obj.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("aaa");
    }
}
