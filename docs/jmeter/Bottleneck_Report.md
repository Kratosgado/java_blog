# Bottleneck Report: Blog API Performance

## 1. Executive Summary

This report presents the findings from the initial performance profiling of the Blog API backend. The load test was executed using JMeter to simulate concurrent user activity across key endpoints, including authentication, post retrieval, search, comments, and dashboard analytics.

The objective of this profiling is to identify performance bottlenecks, specifically focusing on response latency, throughput, and error rates under load, to provide data-driven recommendations for optimization.

## 2. Test Methodology & Environment

- **Tool:** Apache JMeter 5.6.3
- **Target:** `localhost:8080` (Spring Boot REST API)
- **Test Duration:** ~300 seconds (implied by throughput and sample counts)
- **Metrics Collected:** Response Latency (Average, Min, Max), Standard Deviation, Error Percentage, Throughput (Requests/sec), and Data Transfer Rates.
- **Data Source:** `jmeter/initial_bottleneck.csv`

## 3. Profiling Data Analysis

The following table summarizes the performance of the tested endpoints, sorted by Average Response Time (Latency) in descending order to highlight the slowest operations.

| Endpoint                                | Avg Latency (ms) | Max Latency (ms) | Error %    | Throughput (Req/s) | Avg Bytes     |
| :-------------------------------------- | :--------------- | :--------------- | :--------- | :----------------- | :------------ |
| **GET /posts/trending (v2)**            | **9,313**        | 20,551           | 1.626%     | 0.67               | 6,965.0       |
| **GET /posts (list)**                   | **8,991**        | 16,226           | 1.190%     | 0.68               | 6,712.3       |
| POST /auth/login                        | 5,132            | 12,855           | 1.176%     | 0.69               | 765.7         |
| GET /posts/search (v1)                  | 4,392            | 11,643           | 0.000%     | 0.67               | 572.0         |
| GET /posts/{id}                         | 4,382            | 12,737           | 0.806%     | 0.67               | 2,767.8       |
| GET /posts/slug/{slug}                  | 4,306            | 14,208           | 0.402%     | 0.68               | 2,890.6       |
| GET /posts/category/{id}/optimized (v2) | 4,057            | 15,760           | 0.826%     | 0.67               | 4,960.5       |
| GET /dashboard/posts/distribution       | 3,821            | 12,673           | 2.262%     | 0.61               | 572.5         |
| GET /dashboard/recent                   | 3,802            | 12,122           | 1.914%     | 0.59               | **228,295.4** |
| GET /dashboard/engagement               | 3,641            | 11,762           | **3.241%** | 0.60               | 2,365.2       |
| GET /posts/tag/{id}/optimized (v2)      | 3,636            | 12,106           | 0.417%     | 0.66               | 883.5         |
| GET /dashboard/analytics                | 3,466            | 13,593           | **3.070%** | 0.63               | 1,645.4       |
| GET /comments/user/{id}                 | 3,143            | 13,521           | 1.702%     | 0.65               | 2,943.9       |
| GET /dashboard/stats                    | 3,130            | 12,290           | 0.000%     | 0.63               | 561.0         |
| POST /comments                          | 3,125            | 12,643           | 0.866%     | 0.64               | 774.4         |
| GET /comments/post/{id}                 | 2,980            | 14,118           | 1.674%     | 0.66               | 629.4         |
| GET /dashboard/user/stats               | 2,920            | 13,844           | 0.437%     | 0.63               | 558.3         |
| **TOTAL**                               | **4,417**        | **20,551**       | **1.245%** | **10.87**          | **14,088.0**  |

### Key Observations

1. **High Overall Latency:** The global average response time is **4.4 seconds**, with maximums reaching over **20 seconds**. This indicates severe performance degradation under the tested load.
2. **Low Throughput:** The total throughput is only **~10.8 requests per second**, which is exceptionally low for a typical Spring Boot application, suggesting threads are blocked or waiting on slow resources (likely the database).
3. **Error Rates:** Several endpoints exhibit error rates above 1%, peaking at **3.24%** for `/dashboard/engagement` and **3.07%** for `/dashboard/analytics`. These are timeouts after connection was closed.

## 4. Identified Bottleneck Areas

Based on the data, the following areas require immediate optimization focus:

### Bottleneck 1: Complex Post Queries (Trending & List)

- **Endpoints:** `GET /posts/trending (v2)` (Avg: 9.3s) and `GET /posts (list)` (Avg: 8.9s).
- **Analysis:** These are the slowest endpoints in the system. The high latency suggests that the underlying database queries are inefficient.
  - `trending` likely involves complex sorting (e.g., by views/likes) and date filtering.
  - `list` might be suffering from N+1 query problems (fetching tags, categories, or authors for each post) or missing pagination/indexes.
- **Recommendation:** Analyze the SQL execution plans for these queries. Ensure proper indexing on sorting columns (e.g., `created_at`, `views`). Verify that `@EntityGraph` or `JOIN FETCH` is used correctly to prevent N+1 issues. Review the caching strategy; if these are cached, the cache might be thrashing or the initial load is too expensive.

### Bottleneck 2: Authentication Overhead

- **Endpoint:** `POST /auth/login` (Avg: 5.1s).
- **Analysis:** A 5-second average for login is unacceptable for user experience. This is typically caused by the BCrypt password hashing algorithm being configured with a work factor (cost) that is too high for the current hardware, or inefficient database lookups for the user record.
- **Recommendation:** Review the BCrypt cost factor (standard is usually 10-12). Ensure the `username` or `email` column used for lookup has a unique index.

### Bottleneck 3: Dashboard Analytics & Engagement (High Error Rates)

- **Endpoints:** `GET /dashboard/engagement` (3.24% error rate) and `GET /dashboard/analytics` (3.07% error rate).
- **Analysis:** While not the absolute slowest, these endpoints have the highest failure rates. Dashboard queries typically involve heavy aggregations (GROUP BY, COUNT, SUM) across large datasets (posts, comments, views). The errors are likely due to database query timeouts or connection pool exhaustion as these heavy queries tie up connections.
- **Recommendation:** These queries must be optimized. Consider materialized views, background aggregation jobs, or utilizing the caching layer more aggressively for dashboard data. Ensure appropriate indexes exist for the date ranges and grouping columns used in these analytics.

### Bottleneck 4: Massive Payload Size on Recent Activity

- **Endpoint:** `GET /dashboard/recent`
- **Analysis:** While the latency (3.8s) is slightly below the average, the **Avg. Bytes is massive: 228,295.4 bytes (~228 KB) per request**. This is significantly larger than any other endpoint (the next highest is ~6.9 KB).
- **Recommendation:** The `/dashboard/recent` endpoint is returning too much data. It is likely missing pagination or returning full entity objects (including large text content) instead of lightweight DTO summaries. Implement strict pagination (e.g., limit to top 10 items) and ensure only necessary fields (id, title, timestamp) are returned.

## 5. VisualVM Profiling Analysis

![VisualVM Profiling](initial_bottleneck_graph.png)

The VisualVM screenshot captured during the load test provides critical insights into the JVM's behavior:

1. **CPU Usage (Top Left):** CPU usage is extremely volatile, spiking rapidly between 20% and 80% throughout the test duration. This "sawtooth" pattern often indicates that threads are frequently blocking (e.g., waiting for database I/O) and then suddenly waking up to process data. It confirms that the application is not CPU-bound, but rather I/O bound.
2. **Heap Memory (Top Right):** The heap usage shows a classic, aggressive "sawtooth" pattern, rapidly climbing to nearly 6 GB (the max heap size is ~6.2 GB) before dropping sharply due to Garbage Collection (GC). The frequency and amplitude of these spikes indicate a very high object allocation rate. The application is generating a massive number of short-lived objects (likely large result sets from the database or large JSON payloads like the 228KB from `/dashboard/recent`), putting significant pressure on the Garbage Collector.
3. **Threads (Bottom Right):** The thread count jumps from a baseline to a sustained peak of 85 live threads (with 80 daemon threads) at the start of the test and remains flat. This indicates the application quickly saturated its available thread pool (likely the Tomcat HTTP worker threads or HikariCP database connections) and remained at capacity for the duration of the test, causing subsequent requests to queue and experience the high latencies observed in the JMeter data.

## 6. Conclusion & Next Steps

The current system exhibits significant performance bottlenecks, primarily related to database query efficiency, massive object allocation, and payload sizes. The low throughput, high latency, and VisualVM data indicate that the application threads are spending most of their time waiting for the database and generating excessive garbage.

**Immediate Action Items:**

1. **Database Profiling:** Enable slow query logging in PostgreSQL and analyze the queries generated by the `trending`, `list`, and `dashboard` endpoints.
2. **Fix Payload Size & Memory Pressure:** Immediately refactor `GET /dashboard/recent` to return a paginated, lightweight DTO to reduce the massive object allocation rate seen in VisualVM.
3. **Review N+1 Queries:** Inspect the JPA mappings and repository methods for `Post` retrieval to ensure related entities (Tags, Categories, Users) are fetched efficiently using `@EntityGraph`.
4. **Check Connection Pool:** Verify the HikariCP connection pool settings. The thread graph suggests the pool might be saturated. Increasing the pool size might help, but only _after_ optimizing the slow queries that are holding the connections open.
