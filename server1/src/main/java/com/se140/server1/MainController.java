package com.se140.server1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MainController {

    @Autowired
    private RestTemplate restTemplate;

    private boolean isProcessing = false;

    @Value("${SERVER_ID:null}")  // The default value is "unknown"
    private String serverId;

    @GetMapping("/")
    public Map<String, Object> getInformation() {

        if (isProcessing) {
            // If the service is processing the request, return 429 Too Many Requests
            throw new RuntimeException("Service is busy. Please try again later.");
        }

        isProcessing = true;

        //create a response object
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> service1Data = new LinkedHashMap<>();

        // get service1 information
        try {
            service1Data.put("IP address information", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            service1Data.put("IP address information", "Unknown");
        }
        service1Data.put("list of running processes", execCommand("ps -ax"));
        service1Data.put("available disk space", execCommand("df"));
        service1Data.put("time since last boot", execCommand("uptime"));


        response.put("Service", service1Data);
        // get service2 information
        try {
            // get service2 information
//           String service2Url = "http://127.0.0.1:5000";  // service2 URL
            String service2Url = "http://service2:5000";  // service2 URL
            ResponseEntity<Map> service2Response = restTemplate.getForEntity(service2Url, Map.class);
            response.put("Service2", service2Response.getBody());
        } catch (Exception e) {
            response.put("Service2", "Error: " + e.getMessage());
        }

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // resume interrupted state
            } finally {
                isProcessing = false; // Request processing completed
            }
        }).start();


        if (serverId != null && !serverId.equals("null")) {
            response.put("Response Server 1 ID", serverId);
        }

        return response;
    }

    private String execCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopService() {
        try {
            Runtime.getRuntime().exec("docker-compose down");
            Runtime.getRuntime().exec("docker stop");
            return ResponseEntity.ok("All services are stopping...");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error stopping services: " + e.getMessage());
        }
    }


}
