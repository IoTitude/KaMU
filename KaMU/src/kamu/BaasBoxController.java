/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kamu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author h3694
 */
public class BaasBoxController {
    
    
    
    
    public static String logIn() {
        try {
            // Data parameters for POST method
            String urlParameters = "username=admin&password=admin&appcode=1234567890";
            byte[] postData = urlParameters.getBytes();
            // URL to connect to
            String urlStr = "http://192.168.112.60/login";
            // Creating HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            // Request method. SetDoOutput must be declared to be able to POST/PUT output (parameters)
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            // Writer for data parameters
            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write(postData);
            // Readers for reading result
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            // Convert stringBuilder to string and create a JSON object based on that string, then create another JSON object to get specific data
            String jsonText = stringBuilder.toString();
            JSONObject json = new JSONObject(jsonText);
            JSONObject data = json.getJSONObject("data");
            // Get BaasBox session id. Needed for other queries
            String session = data.getString("X-BB-SESSION");
            // Get all documents
            //getDocuments(session, "Devices");
            // Get admin device hashes
            //getAdminHashes(session);
            // Get measuring unit hashes
            //getDeviceHashes(session);
            return session;
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    public static JSONObject getDocuments(String session, String collection) {
        try {
            // Get documents from specific collection
            String urlStr = "http://192.168.112.60/document/" + collection;
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            // SetRequestProperty sets needed headers
            conn.setRequestProperty("X-BB-SESSION", session);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            String jsonText = stringBuilder.toString();
            JSONObject json = new JSONObject(jsonText);
            // Return result JSON object. We parse needed data from this later
            return json;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public static List getAdminHashes(String session) {
        // Get documents from Admin collection
        JSONObject json = getDocuments(session, "Admin");
        JSONArray data = json.getJSONArray("data");
        List<String> hashes = new ArrayList<>();
        // Iterate through JSON array and add each hash to list. This list can be returned
        for (int i = 0; i < data.length(); i++) {
            JSONObject hashObj = data.getJSONObject(i);
            String hash = hashObj.getString("hash");
            hashes.add(hash);
        }
        return hashes;
    }
    
}
