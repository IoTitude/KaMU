package kamu;

import java.net.InetAddress;
import static org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel.LOG;

public class KaMU {
    static boolean conn;
    static KaaController controller = new KaaController("KaaController");
    public static void main(String[] args) {
        while (true){
            if (conn) {
                //Led.ledtoggle(0);//////UNCOMMENT WHEN RUNNING IN RASPBERRY WITH LED INSTALLED
                controller.start();
            }           
            try{
                conn = InetAddress.getByName("192.168.142.46").isReachable(1000);
            } catch (Exception e){
                if (KaaController.sender.isAlive()){
                    KaaController.sender.stop();
                    controller.stop();
                }     
                conn = false;
                //Led.ledtoggle(500);//////UNCOMMENT WHEN RUNNING IN RASPBERRY WITH LED INSTALLED
                LOG.info("Connection to Kaa server failed: " + e.getMessage());                    
            }          
        }     
    }  
}