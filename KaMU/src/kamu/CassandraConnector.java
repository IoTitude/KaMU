package kamu;

import com.datastax.driver.core.Cluster;  
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;  
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import java.util.ArrayList;
import java.util.List;
 
public class CassandraConnector {
    
    
    
 
    public static List<String> getDevices() {
        
        Cluster cluster;
        Session session;
        List<String> devices = new ArrayList<>();
        
        try {
            cluster = Cluster.builder().addContactPoint("").withCredentials("cassandra", "cassandra").build();
            session = cluster.connect("kaa");
            
            ResultSet results = session.execute("SELECT hash FROM admin1 where isadmin = true ALLOW FILTERING");
            for (Row row : results) {
                //System.out.format("%s\n", row.getString("hash"));
                devices.add(row.getString("hash"));
                
                cluster.close();
                
            }
            return devices;
        }
        catch (NoHostAvailableException e){
            System.out.println(e.getErrors());
            return null;
        }
    }
    
    public static double getData(){
        double data = 0;   
        Cluster cluster;
        Session session;      
        
        try {
            cluster = Cluster.builder().addContactPoint("").withCredentials("", "").build();
            session = cluster.connect("mittadata");
            
            ResultSet results = session.execute("SELECT vedenpinta FROM simdatagen");
            for (Row row : results) {
                //System.out.format("%s\n", row.getString("hash"));
                data = row.getDouble("vedenpinta");
                System.out.println(data);
                cluster.close();
                
            }
            return data;
        }
        catch (NoHostAvailableException e){
            System.out.println(e.getErrors());
            return 0;
        }
        
    }
    
}
