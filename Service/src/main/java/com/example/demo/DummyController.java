package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/fast")
    public String fastEndpoint() throws InterruptedException {
        Thread.sleep(200); // 200 Millisekunden warten
        return "Fast endpoint processed";
    }

    @GetMapping("/slow")
    public String slowEndpoint() throws InterruptedException {
        Thread.sleep(12000); // 12 Sekunden warten
        return "Slow endpoint processed";
    }
}
