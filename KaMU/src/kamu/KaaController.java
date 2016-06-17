package kamu;

import java.net.NetworkInterface;
import java.util.LinkedList;
import java.util.List;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
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
    static BaasBoxController baas;
    
    KaaController (String name) {
        threadName = name;   
        System.out.println("Creating " +  threadName );
    }
    
    public void start(){  
        if (thread == null)
        {
            //System.out.println(getMac());
            kaaStart(); 
            System.out.println("Starting " +  threadName );
            thread = new Thread (this, threadName);
            thread.start();
            String session = baas.logIn();
            List<String> hashes = baas.getAdminHashes(session);
            attachUser();
            sender = new LogSender("LogSender");
            sendRegistrationRequest(hashes);
            //sendProfileAll();
            
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
    
    public static void sendProfileAll(){
        //List<String> FQNs = new LinkedList<>();
        //FQNs.add(ChangeProfile.class.getName());
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();

        tecf.sendEventToAll(new ChangeProfile(1));
        System.out.println("Change profile request sent");
        //LogData log = new LogData("asdmacasd", "asdhashasd");
                //kaaClient.addLogRecord(log);
}
    
    public static void sendRegistrationRequest(List<String> hashes){
        
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        
        for (String target : hashes) {
            RegisterDevice ctc = new RegisterDevice(getMac(), kaaClient.getEndpointKeyHash());
            tecf.sendEvent(ctc, target);
            System.out.println(target + " target");
        }

        /*
        List<String> FQNs = new LinkedList<>();
        FQNs.add(ChangeProfile.class.getName());         
        final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
        final KaMUEventClassFamily tecf = eventFamilyFactory.getKaMUEventClassFamily();
        kaaClient.findEventListeners(FQNs, new FindEventListenersCallback() {
            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
                if (kaaClient.isAttachedToUser()) {
                    System.out.println("kaaClient is attached to user");
                }
                else {
                    System.out.println("kaaClient is NOT attached to user");
                }
                
                RegisterDevice ctc = new RegisterDevice(getMac(), kaaClient.getEndpointKeyHash());
                for (String target : hashes){
                
                tecf.sendEvent(ctc, target);
                System.out.println("Registration request sent to admin.");
                }
                
                // Assume the target variable is one of the received in the findEventListeners method
               
               
            }   
        
            @Override
            public void onRequestFailed() {
                System.out.println("Send profile request failed");
            }
});
*/
            
    }
    
    
}
