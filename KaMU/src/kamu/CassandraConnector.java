package kamu;

import com.datastax.driver.core.Cluster;  
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;  
import com.datastax.driver.core.exceptions.NoHostAvailableException;
 
public class CassandraConnector {
    
    public static double getData(){
        double data = 0;   
        Cluster cluster;
        Session session;      
        
        try {
            cluster = Cluster.builder().addContactPoint("198.211.127.190").withCredentials("admin", "challenge2016").build();
            session = cluster.connect("mittadata");
            
            ResultSet results = session.execute("SELECT vedenpinta FROM simdatagen");
            for (Row row : results) {                
                data = row.getDouble("vedenpinta");
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
