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
    private String threadName = "";
    static KaaClient kaaClient;
    static int profile = 0;
    static LogSender sender;
    static String target = "re/he8CmJGoDDitu4mmaF9SnD/Q=";
    
    KaaController (String name) {
        threadName = name;   
        System.out.println("Creating " +  threadName );
    }
    
    @Override
    public void run() {
        //Thread thisThread = Thread.currentThread();
        //while (thread == thisThread){
            kaaStart();
            //Led led = new Led();zz
            attachUser();
            sender = new LogSender("LogSender");
            
            //sendProfileSingleTarget();
            //sendProfileAll();
            CassandraConnector.getData();
        //}
            
    }
    
    
    public void start(){  
        if (thread == null)
        {
            System.out.println("Starting " +  threadName );
            thread = new Thread (this, threadName);
            thread.start();
        }
        
    }
    public void stop(){
        System.out.println("Stopping " +  threadName );
        thread = null;
    }
    

    
    /*
    public boolean isAlive() {   
        if (thread.isAlive()) {
           return true;
        }
        else {
           return false;
        }
    }
    
    public boolean getState() {
        State state = thread.getState();
        if (state == null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public String getCurrentState() {
        try {
            return thread.getState().toString();
        } catch(Exception e) {
            return e.getMessage();
        }
    }
    */
    public void kaaStart(){
         kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {          
                kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
                //LogSender log1= new LogSender("Thread-2");
                //log1.start();
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
        String macStr = "";
        try {
            NetworkInterface netInf = NetworkInterface.getNetworkInterfaces().nextElement();
            byte[] mac = netInf.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macStr = sb.toString();
            //System.out.println(sb.toString());
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
    
    public static void sendProfileSingleTarget(){
        List<String> FQNs = new LinkedList<>();
        FQNs.add(ChangeProfile.class.getName());
        List <String> devices = CassandraConnector.getDevices();            
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
                for (String s : devices){
                
                tecf.sendEvent(ctc, s);
                System.out.println("Registration request sent to admin.");
                }
                
                // Assume the target variable is one of the received in the findEventListeners method
               
               
            }   
        
            @Override
            public void onRequestFailed() {
                System.out.println("Send profile request failed");
            }
        });
    }
    
}
