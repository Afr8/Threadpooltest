document.addEventListener('DOMContentLoaded', () => {
    const slowRequestsButton = document.getElementById('startSlowRequests');
    const fastRequestsButton = document.getElementById('startFastRequests');
    const statusText = document.getElementById('statusText');
    const rawJsonOutput = document.getElementById('rawJsonOutput');
    const chartCanvas = document.getElementById('performanceChart');
    let performanceChart = null;

    const METRICS_URL = 'http://localhost:8080/metrics';
    const SLOW_REQUESTS_URL = 'http://localhost:8080/trigger-slow-requests';
    const FAST_REQUESTS_URL = 'http://localhost:8080/trigger-fast-requests';

    async function fetchData(url, statusMessage) {
        statusText.textContent = statusMessage;
        rawJsonOutput.textContent = ''; // Clear previous JSON output
        try {
            const response = await fetch(url);
            if (!response.ok) {
                const errorBody = await response.text();
                console.error('Fetch error details:', errorBody);
                throw new Error(
                    `HTTP error! status: ${response.status} - ${response.statusText}`,
                );
            }
            // For trigger endpoints, we don't expect a JSON body, just success.
            // For metrics endpoint, we expect JSON.
            if (url === METRICS_URL) {
                return await response.json();
            }
            return {success: true}; // Indicate trigger success
        } catch (error) {
            console.error('Fetch error:', error);
            statusText.textContent = `Fehler: ${error.message}`;
            return null;
        }
    }

    function updateChart(metricsData) {
        if (!metricsData) {
            statusText.textContent = 'Keine Daten zum Anzeigen im Diagramm.';
            // Optionally clear or hide the chart canvas
            if (performanceChart) {
                performanceChart.destroy();
                performanceChart = null;
            }
            chartCanvas.style.display = 'none'; // Hide canvas if no data
            return;
        }

        chartCanvas.style.display = 'block'; // Show canvas if there is data
        rawJsonOutput.textContent = JSON.stringify(metricsData, null, 2);

        const chartTitle = `Antwortzeiten (Anfragen)`;

        const chartData = {
            labels: [
                'Minimale Dauer (ms)',
                'Durchschnittliche Dauer (ms)',
                'Maximale Dauer (ms)',
            ],
            datasets: [
                {
                    label: chartTitle,
                    data: [
                        metricsData.minDurationMs,
                        metricsData.avgDurationMs,
                        metricsData.maxDurationMs,
                    ],
                    backgroundColor: [
                        'rgba(54, 162, 235, 0.6)', // Blue for Min
                        'rgba(75, 192, 192, 0.6)', // Green for Avg
                        'rgba(255, 99, 132, 0.6)', // Red for Max
                    ],
                    borderColor: [
                        'rgba(54, 162, 235, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(255, 99, 132, 1)',
                    ],
                    borderWidth: 1,
                },
            ],
        };

        if (performanceChart) {
            performanceChart.destroy(); // Destroy existing chart before creating a new one
        }

        performanceChart = new Chart(chartCanvas, {
            type: 'bar',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Zeit (ms)',
                        },
                    },
                },
                plugins: {
                    legend: {
                        display: true, // Ensure this is not causing issues if Chart.js version changed
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: chartTitle,
                    },
                },
            },
        });
        statusText.textContent = `Metriken für ${metricsData.type} Anfragen geladen.`;
    }

    if (slowRequestsButton) {
        slowRequestsButton.addEventListener('click', async () => {
            statusText.textContent = 'Starte langsame Anfragen...';
            const triggerResult = await fetchData(
                SLOW_REQUESTS_URL,
                'Sende langsame Anfragen...',
            );
            if (triggerResult && triggerResult.success) {
                statusText.textContent =
                    'Langsame Anfragen gestartet, hole Metriken...';
                const metrics = await fetchData(
                    METRICS_URL,
                    'Hole Metriken...',
                );
                if (metrics) {
                    updateChart(metrics);
                } else {
                    statusText.textContent =
                        'Fehler beim Abrufen der Metriken für langsame Anfragen.';
                    updateChart(null); // Clear chart on error
                }
            } else {
                statusText.textContent =
                    'Fehler beim Starten der langsamen Anfragen.';
                updateChart(null); // Clear chart on error
            }
        });
    } else {
        console.error('Button mit ID "startSlowRequests" nicht gefunden.');
        statusText.textContent =
            'Fehler: Button für langsame Anfragen nicht gefunden.';
    }

    if (fastRequestsButton) {
        fastRequestsButton.addEventListener('click', async () => {
            statusText.textContent = 'Starte schnelle Anfragen...';
            const triggerResult = await fetchData(
                FAST_REQUESTS_URL,
                'Sende schnelle Anfragen...',
            );
            if (triggerResult && triggerResult.success) {
                statusText.textContent =
                    'Schnelle Anfragen gestartet, hole Metriken...';
                const metrics = await fetchData(
                    METRICS_URL,
                    'Hole Metriken...',
                );
                if (metrics) {
                    updateChart(metrics);
                } else {
                    statusText.textContent =
                        'Fehler beim Abrufen der Metriken für schnelle Anfragen.';
                    updateChart(null); // Clear chart on error
                }
            } else {
                statusText.textContent =
                    'Fehler beim Starten der schnellen Anfragen.';
                updateChart(null); // Clear chart on error
            }
        });
    } else {
        console.error('Button mit ID "startFastRequests" nicht gefunden.');
        statusText.textContent =
            'Fehler: Button für schnelle Anfragen nicht gefunden.';
    }

    // Initial empty chart or message
    statusText.textContent = 'Bereit. Bitte einen Testlauf starten.';
    updateChart(null); // Ensure chart is initially hidden or cleared
});
