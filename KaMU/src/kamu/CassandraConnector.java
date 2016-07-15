package kamu;

import com.datastax.driver.core.Cluster;  
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;  
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
public class CassandraConnector {

    public CassandraConnector() {
    }
    
    public static Map<String, Double> getData(List<String> parameters, int deviceID){ 
        Map<String, Double> data = new HashMap<>();   
        data.put("ominaissahkojohtavuus", 0.0);
        data.put("virtausnopeus", 0.0);
        data.put("vedenpinta", 0.0);
        data.put("paine", 0.0);
        data.put("lampotila", 0.0);
        Cluster cluster;
        Session session;   
        
        try {
            cluster = Cluster.builder().addContactPoint("198.211.127.190").withCredentials("admin", "challenge2016").build();
            session = cluster.connect("mittadata");

            ResultSet results = session.execute("SELECT * FROM simdatagen WHERE id = " + deviceID + " ALLOW FILTERING;");
            for (Row row : results) {  
                for (String parameter : parameters) {
                    if (data.containsKey(parameter)) {
                        data.put(parameter, row.getDouble(parameter));
                    }
                }
            }

        }
        catch (NoHostAvailableException e){
            System.out.println(e.getErrors());
        }    
        
        return data;
    }  
}
