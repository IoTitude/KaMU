package kamu;

import java.net.NetworkInterface;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.schema.sample.event.kamu.ChangeProfile;
import org.kaaproject.kaa.schema.sample.event.kamu.KaMUEventClassFamily;
import org.kaaproject.kaa.schema.sample.event.kamu.RegisterDevice;
import org.kaaproject.kaa.schema.sample.event.kamu.RegistrationAnswer;

public class KaaController implements Runnable{
    private Thread thread;
    private final String threadName;
    static KaaClient kaaClient;
    static int profile;
    static LogSender sender;
    
    KaaController (String name) {
        threadName = name;   
        System.out.println("Creating " +  threadName );
    }
    
    public void start(){  
        if (thread == null)
        {
            kaaStart(); 
            System.out.println("Starting " +  threadName );
            thread = new Thread (this, threadName);
            thread.start();
            attachUser();
            sender = new LogSender("LogSender");
            //Led led = new Led(); //////UNCOMMENT WHEN RUNNING IN RASPBERRY WITH LED INSTALLED  
        }
        
    }
    
    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while (thread == thisThread){
                
        }       
    }
    
    public boolean isAlive() {
        if (thread != null) {      
            return true;
        } 
        else {
            return false;
        }
    }
     
    public void stop(){
        System.out.println("Stopping " +  threadName );
        thread = null;
    }
   
    public void kaaStart(){
         kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {          
                kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
            }

            @Override
            public void onStopped() {
             
            }
        });
        kaaClient.start();
    }
    
    public void attachUser() {
         kaaClient.attachUser("asd", "asd", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                System.out.println("Attach response " + response.getResult());   
                if (response.getResult() == SyncResponseResultType.SUCCESS){
                    onUserAttached();
                }
                else{
                    kaaClient.stop();
                    System.out.println("Stopped");
                }
            }
        });
    }
    
    public static void onUserAttached(){
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        tecf.addListener(new KaMUEventClassFamily.Listener() {
            @Override
            public void onEvent(ChangeProfile event, String source) {
                System.out.println("ChangeProfile event");
                profile = event.getProfileID();
                System.out.println("Got ID: " + profile + " From: " + source);
                try{
                    sender.start();
                }catch (Exception e){
                    System.out.println("Log send failed");
                }
            }   

            @Override
            public void onEvent(RegisterDevice event, String source) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onEvent(RegistrationAnswer event, String source) {
                System.out.println(event.getIsRegistered());
            }
        });
    }
    
    public static String getMac(){
        String macStr;
        try {
            NetworkInterface netInf = NetworkInterface.getNetworkInterfaces().nextElement();
            byte[] mac = netInf.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macStr = sb.toString();
            return macStr;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }
}
