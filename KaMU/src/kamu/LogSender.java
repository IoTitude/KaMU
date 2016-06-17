package kamu;

import java.util.Random;
import java.util.logging.Level;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.schema.sample.logging.LogData24;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSender implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(KaMU.class);
    private Thread thread;
    private final String threadName; 
   
    LogSender (String name) {
        threadName = name;   
        System.out.println("Creating " +  threadName );
    }
    
     public void start(){                
        if (thread == null){
            System.out.println("Starting " +  threadName );
            thread = new Thread (this, threadName);
            thread.start();
        }    
    }
          
    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while (thread == thisThread){
            try {
                sendLog();   
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(LogSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       
    }
    
    public static void sleep(int ms) throws InterruptedException{
        int sleepytime = ms;
        Thread.sleep(sleepytime);
    }
    
    public boolean isAlive() {
        if (thread == null) {      
            return false;
        } 
        else {
            return true;
        }
    }
    
    public void stop(){
        System.out.println("Stopping " +  threadName );
        thread = null;
    }
    
    
    public static void sendLog() throws InterruptedException{
        KaaController.kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
            
        while (KaMU.conn && KaaController.profile != 0){                     
            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            Random random = new Random();
            LogData24 log = new LogData24(KaaController.getMac(), 0.1, 0.1, 0.1, 0.1, 0.1);
            KaaController.kaaClient.addLogRecord(log);

            //Led.ledtoggle(2500);//////UNCOMMENT WHEN RUNNING IN RASPBERRY WITH LED INSTALLED

            LOG.info("Log record {} sent", log.toString());
            sleep(20000);              
        }
        KaaController.kaaClient.stop();
    }  
}