package kamu;

import java.net.InetAddress;
import static org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel.LOG;

public class KaMU {
    static boolean conn;
    public static void main(String[] args) throws InterruptedException {

        KaaController controller = new KaaController("KaaController");
        
        while (true){
            if (conn) {
                //Led.ledtoggle(0);
                controller.start();
                
                //if (KaaController.profile !=0 ){
                 //   KaaController.sender.start();
                //}
            }
           
            try{
                conn = InetAddress.getByName("192.168.142.46").isReachable(1000);
            } catch (Exception e){
                if (KaaController.sender.isAlive()){
                    KaaController.sender.stop();
                    controller.stop();
                }     
                conn = false;
                //Led.ledtoggle(500);
                LOG.info("Connection to Kaa server failed: " + e.getMessage());
                    
            }
               
           
            Thread.sleep(1000);
        }
        
       }
    
}