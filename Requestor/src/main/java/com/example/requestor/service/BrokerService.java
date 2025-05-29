package com.example.requestor.service;

import com.example.requestor.model.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;

@Service
public class BrokerService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private final RestTemplate restTemplate;
    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
    private Metrics lastMetrics;
    private final ExecutorService executorService;

    public BrokerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Konfigurierbarer Thread-Pool, z.B. mit der Anzahl der zu sendenden Anfragen
        // oder einer anderen passenden Größe, um Ressourcen zu schonen.
        // Für 50 Anfragen könnte ein Pool von 50 Threads sinnvoll sein, wenn die
        // externen Aufrufe blockierend sind.
        this.executorService = Executors.newFixedThreadPool(50);
    }

    public Metrics triggerRequests(String type, String path, int count) {
        responseTimes.clear();
        AtomicLong successfulRequests = new AtomicLong(0);
        AtomicLong failedRequests = new AtomicLong(0);
        long testStartTime = System.currentTimeMillis();

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(() -> {
                sendRequest(path, successfulRequests, failedRequests);
                return null;
            });
        }

        try {
            // Alle Aufgaben ausführen und auf deren Abschluss warten
            List<Future<Void>> futures = executorService.invokeAll(tasks, 60, TimeUnit.SECONDS); // Timeout für alle
                                                                                                 // Tasks
            for (Future<Void> future : futures) {
                try {
                    future.get(); // Auf das Ergebnis warten (oder Exception prüfen)
                } catch (CancellationException e) {
                    logger.warn("A task was cancelled: {}", path);
                    failedRequests.incrementAndGet(); // Zähle gecancelte Tasks als fehlgeschlagen
                } catch (ExecutionException e) {
                    logger.error("Exception during request execution for path {}: {}", path, e.getCause().getMessage());
                    // Fehler wird bereits in sendRequest behandelt und gezählt
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Request triggering was interrupted for path {}: {}", path, e.getMessage());
            // Verbleibende, nicht gestartete Tasks als fehlgeschlagen werten, falls
            // erforderlich
            // Hier gehen wir davon aus, dass invokeAll entweder alle startet oder eine
            // Exception wirft
        }

        long testEndTime = System.currentTimeMillis();
        long totalDurationAllRequestsMs = testEndTime - testStartTime;
        this.lastMetrics = calculateMetrics(type, successfulRequests.get(), failedRequests.get(),
                totalDurationAllRequestsMs);
        return this.lastMetrics;
    }

    private void sendRequest(String servicePath, AtomicLong successfulRequests, AtomicLong failedRequests) {
        long requestStartTime = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Start-Time", String.valueOf(requestStartTime));
        headers.set("Content-Type", "application/json");

        String url = "https://localhost:8081/api/apps/citizen-engage/summary";
        String payload = """
                {
                "models": ["openai"],
                "text": "Die Südbrücke durchquert das Erholungsgebiet Rheinaue. Dieses wird jedoch durch den Verkehrslärm (Tempo 100) sowie höhere Schadstoffmengen belastet. Die Sinnhaftigkeit von Tempo 100 auf der Südbrücke ist angesichts der Länge der Tempozone äußerst fragwürdig. In Richtung Osten muss am Aufstieg Ramersdorf wieder auf Tempo 60 heruntergebremst werden, Richtung Königswinter auf 80. In Richtung Westen auf Tempo 50 bzw. noch Tempo 70 entlang der Rheinaue Richtung Bad Godesberg."
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                long requestEndTime = System.currentTimeMillis();
                responseTimes.add(requestEndTime - requestStartTime);
                successfulRequests.incrementAndGet();
                logger.debug("Request to {} successful, duration: {} ms", url, (requestEndTime - requestStartTime));
            } else {
                failedRequests.incrementAndGet();
                logger.warn("Request to {} failed with status: {}", url, responseEntity.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            failedRequests.incrementAndGet();
            logger.error("Request to {} failed with HttpStatusCodeException {}: {}", url, e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            logger.error("Request to {} failed with Exception: {}", url, e.getMessage(), e);
        }
    }

    private Metrics calculateMetrics(String type, long successful, long failed, long totalDurationAllRequestsMs) {
        if (responseTimes.isEmpty()) {
            return new Metrics(type, OffsetDateTime.now(), successful, failed, 0, 0, 0.0, totalDurationAllRequestsMs);
        }
        // Stelle sicher, dass responseTimes nicht leer ist, bevor auf Streams
        // Operationen ausgeführt werden
        List<Long> currentResponseTimes = new ArrayList<>(responseTimes); // Kopie für Thread-Sicherheit bei Berechnung
        if (currentResponseTimes.isEmpty() && successful == 0) { // Wenn keine erfolgreichen Requests, dann sind
                                                                 // Metriken 0
            return new Metrics(type, OffsetDateTime.now(), successful, failed, 0, 0, 0.0, totalDurationAllRequestsMs);
        }

        long min = currentResponseTimes.stream().min(Long::compareTo).orElse(0L);
        long max = currentResponseTimes.stream().max(Long::compareTo).orElse(0L);
        double avg = currentResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new Metrics(type, OffsetDateTime.now(), successful, failed, min, max, avg, totalDurationAllRequestsMs);
    }

    public Metrics getMetrics() {
        return this.lastMetrics; // Kann null sein, wenn noch keine Requests getriggert wurden
    }

    // Es ist eine gute Praxis, den ExecutorService herunterzufahren, wenn die
    // Anwendung beendet wird.
    @PreDestroy
    public void shutdownExecutor() {
        logger.info("Shutting down BrokerService ExecutorService");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
