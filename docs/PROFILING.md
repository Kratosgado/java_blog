# Profiling and Metrics Guide

This guide describes how to monitor the runtime behavior of the Blog Backend using the integrated metrics system.

## Overview

The application exposes runtime metrics including:

- **CPU Usage**: System CPU load.
- **Memory Usage**: JVM Heap memory usage.
- **Throughput**: Requests per second (RPS).
- **Latency**: Average response time per 5-second interval.

Profiling is in 2 forms, depending on the profiling tool you use:

- **External Tools(VisualVM and Jmeter)**:
- **Internal Visual**: A simple dashboard is available at: `http://localhost:8080/metrics.html`

## Profiling Tools

Profiling tools are available for:

- **VisualVM**: VisualVM is a Java-based profiling tool that provides a graphical interface for analyzing the performance of Java applications. It allows you to monitor CPU usage, memory usage, and thread activity.
- **JMeter**: JMeter is a popular open-source tool for load testing and performance testing. It allows you to simulate user behavior and measure response times, throughput, and other performance metrics.

## Procedure

1. Run the application, VisualVM and Jmeter.
2. To use a default admin token for authenticated endpoints like the dashboard, Login as admin and copy the token from the response.
3. Load the jmeter test plan from ./jmeter/blog-api-test-plan.jmx
4. Set the Authorization header with the token from step 2.
5. Switch to the blog server's process in VisualVM to start recording.
6. Switch to the jmeter and run the test plan.

## Accessing Metrics

### 1. Metrics Dashboard (Visual)

A simple dashboard is available at:
`http://localhost:8080/metrics.html`

_Note: You must provide a valid JWT token (Bearer token) to fetch the metrics._

### 2. Metrics API (JSON)

The raw metrics history can be accessed via the API:
`GET /api/v1/admin/metrics`

**Response Format:**

```json
{
  "cpuUsage": [{"timestamp": 1234567890, "value": 0.45}, ...],
  "memoryUsage": [{"timestamp": 1234567890, "value": 256.5}, ...],
  "activeRequests": [{"timestamp": 1234567890, "value": 12.5}, ...], // RPS
  "responseTime": [{"timestamp": 1234567890, "value": 45.2}, ...]    // ms
}
```

### 3. Actuator Endpoints

Standard Spring Boot Actuator endpoints are also available:

- `GET /actuator/health`: System health.
- `GET /actuator/info`: Application info.
- `GET /actuator/metrics`: List of available metrics.
- `GET /actuator/prometheus`: Prometheus-formatted metrics.

## Logs

Metrics are also logged to the console every 5 seconds by `MetricsServiceImpl`:
`INFO ... Metrics: CPU=0.45%, Mem=256.00MB, RPS=12.00, Latency=45.00ms`
