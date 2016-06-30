package kamu;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.kaaproject.kaa.schema.sample.event.kamu.RegistrationAnswer;
import org.kaaproject.kaa.schema.sample.event.kamu.RegistrationRequest;
import org.kaaproject.kaa.schema.sample.event.kamu.RestartDevice;
import org.kaaproject.kaa.schema.sample.event.kamu.UpdateDevice;

public class KaaController implements Runnable{
    private static Thread thread;
    private static String threadName;
    static KaaClient kaaClient;
    static int profile;
    static LogSender sender;
    static BaasBoxController baas;
    static boolean isRegistered = false;
    static String version = "0.4";
    static String[] args;
    static String session = baas.logIn();
    static List<String> hashes = baas.getAdminHashes(session);
    static int deviceID;
    
    KaaController (String name) {
        threadName = name;   
        System.out.println("Creating " +  threadName );
    }
    
    public void start(){  
        if (thread == null)
        {
            System.out.println("Version " + version);
            kaaStart(); 
            System.out.println(getMac());
            System.out.println(kaaClient.getEndpointKeyHash());
            System.out.println("Starting " +  threadName );
            thread = new Thread (this, threadName);
            thread.start();
            attachUser();
            
            
            //Led led = new Led(); //////UNCOMMENT WHEN RUNNING IN RASPBERRY WITH LED INSTALLED  
        }
        
    }
    
    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while (thread == thisThread){
            if (!isRegistered) {
                try {
                    sendRegistrationRequest(hashes);
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KaaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
     
    public static void stop(){
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
            public void onEvent(RegistrationAnswer event, String source) {
                if (!event.getIsRegistered()) {
                    try {
                        System.out.println("Error registering device. Retrying...");
                        isRegistered = event.getIsRegistered();
                        Thread.sleep(10000);
                        sendRegistrationRequest(hashes); 
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KaaController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (event.getIsRegistered()) {
                    System.out.println("Device registered successfully");
                    sender = new LogSender("LogSender");
                    isRegistered = event.getIsRegistered();
                    deviceID = event.getDeviceID();
                    System.out.println("Device ID: " + deviceID);
                }
            }

            @Override
            public void onEvent(RegistrationRequest event, String source) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onEvent(UpdateDevice event, String source) {
                String version = event.getVersion();
                System.out.println(event.getVersion());
                int delay = event.getDelay();
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Update test");
                                Process proc = Runtime.getRuntime().exec("java -jar /home/h9073/Documents/Repos/literate-guide/TestUpdater/dist/TestUpdater.jar update " + version);
                            } catch (Exception ex) {
                                Logger.getLogger(KaaController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }, 
                delay);
            }

            @Override
            public void onEvent(RestartDevice event, String source) {
                int delay = event.getDelay();
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Reset test");
                                Process proc = Runtime.getRuntime().exec("java -jar /home/h9073/Documents/Repos/literate-guide/TestUpdater/dist/TestUpdater.jar restart");
                            } catch (Exception ex) {
                                Logger.getLogger(KaaController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }, 
                delay);
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
    
    public static void sendRegistrationRequest(List<String> hashes){
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        for (String target : hashes) {
            RegistrationRequest ctc = new RegistrationRequest(getMac(), kaaClient.getEndpointKeyHash(), version);
            tecf.sendEvent(ctc, target);
            System.out.println("Registration request sent to " + target);
        }        
    }
}