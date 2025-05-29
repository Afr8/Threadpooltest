package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
public class BrokerController {

    private final RestTemplate restTemplate;
    private final String serviceBaseUrl = "http://localhost:8082/";

    public BrokerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/broker/slow")
    public ResponseEntity<String> handleSlowRequest(@RequestHeader HttpHeaders headers) {
        System.out.println("Received at: " + java.time.LocalTime.now());
        return forwardRequest("/slow", headers);
    }

    @GetMapping("/broker/fast")
    public ResponseEntity<String> handleFastRequest(@RequestHeader HttpHeaders headers) {
        System.out.println("Received at: " + java.time.LocalTime.now());
        return forwardRequest("/fast", headers);
    }

    private ResponseEntity<String> forwardRequest(String servicePath, HttpHeaders headers) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(serviceBaseUrl + servicePath, HttpMethod.GET, entity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Service unavailable: " + e.getMessage());
        }
    }
}
