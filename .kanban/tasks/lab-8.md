---

kanban-plugin: basic

---

## Backlog

- [ ] Review the full project PDF and note any missing dependencies (e.g., Redis for caching if needed, or stick with Caffeine) #Prep

- [ ] Decide on caching solution (in-memory Caffeine vs external Redis based on scalability needs) #DSA

- [ ] Ensure database has proper indexing for frequent queries (posts by date, comments by post ID, etc.) #Database

## Todo

- [ ] fix view tracking, it occurs once per cache #bug

- [ ] Confirm the secured Spring Boot backend is running locally with JWT/OAuth2/Spring Security enabled (Prep)

- [ ] [[Enable Spring Boot Actuator + Micrometer Prometheus for metrics exposure (Epic 5 – US 5.1)]]

- [ ] [[Add `@EnableAsync` and configure a custom `ThreadPoolTaskExecutor` bean with optimal settings (Epic 2 – US 2.1)]]

- [ ] Run initial CPU, memory, thread, and latency profiling session on the existing secured backend (Epic 1 – US 1.1)

- [ ] Identify key bottlenecks in post retrieval, comment loading, user analytics, feed aggregation, notification dispatch APIs (Epic 1 – US 1.1)

- [ ] Record baseline performance metrics: average response time, CPU usage %, memory footprint, GC pauses, thread usage (Epic 1 – US 1.1 / US 1.2)

- [ ] Create a bottleneck summary report with findings, screenshots, flame graphs, and highlighted optimization areas (Epic 1 – US 1.2)

- [ ] Refactor long-running operations (analytics, feed aggregation, notification dispatch) using `@Async`, `CompletableFuture`, or `ExecutorService` (Epic 2 – US 2.1)

- [ ] Configure thread pools for optimal concurrency and handle async exceptions properly (Epic 2 – US 2.1)

- [ ] Propagate security context in async methods (`DelegatingSecurityContextExecutor` or MODE_INHERITABLETHREADLOCAL) (Epic 2 – US 2.1)

- [ ] Ensure system remains responsive during multiple simultaneous authenticated requests (Epic 2 – US 2.1)

- [ ] Run concurrent API tests with Postman/JMeter → verify stability, no data loss/corruption, reduced avg response time vs baseline (Epic 2 – US 2.2)

- [ ] Replace non-thread-safe collections with `ConcurrentHashMap`, `CopyOnWriteArrayList`, `ConcurrentLinkedQueue` etc. for shared resources (Epic 3 – US 3.1)

- [ ] Add synchronized blocks, `ReentrantLock`, or other primitives where concurrent modification occurs (Epic 3 – US 3.1)

- [ ] Verify thread safety via concurrent test scenarios (e.g. multi-threaded JUnit tests) – no race conditions or corruption (Epic 3 – US 3.1)

- [ ] Test and tune thread pool configurations (vary core/max/queue size) under different loads (Epic 3 – US 3.2)

- [ ] Track CPU and memory utilization during tuning and document optimal configuration with justification (Epic 3 – US 3.2)

- [ ] Optimize data retrieval, sorting, searching, filtering with efficient DSA (Comparator, HashSet, parallel streams where appropriate) (Epic 4 – US 4.1)

- [ ] Implement caching (`@Cacheable` with Caffeine/Redis) or in-memory indexing for feeds, trending posts, analytics (Epic 4 – US 4.1)

- [ ] Optimize JPA/Hibernate queries (fetch joins, `@EntityGraph`, pagination, database indexing) (Epic 4 – US 4.1)

- [ ] Demonstrate reduced query/execution latency through metrics (e.g. sorting 10k posts: 200ms → 90ms) (Epic 4 – US 4.1)

- [ ] Measure and compare before-and-after performance for algorithmic changes (charts/tables) (Epic 4 – US 4.2)

- [ ] Document execution time improvements and summarize algorithmic findings in report (Epic 4 – US 4.2)

- [ ] Add custom Micrometer metrics (`@Timed`, `Counter`, `Gauge`, `@Observed`) for key endpoints/services (Epic 5 – US 5.1)

- [ ] Integrate metrics visualization (Grafana, log exports, Actuator chart exports) (Epic 5 – US 5.1)

- [ ] Add profiling workflow (tools/steps/commands) to development documentation (Epic 5 – US 5.1)

- [ ] Run full before-and-after load tests to collect optimized metrics and validate improvements (Epic 5 – US 5.2)

- [ ] Create final optimization report: screenshots, test results, metric comparisons (tables/charts), methodology, Postman/browser demos (Epic 5 – US 5.2)

## Work in progress

- [ ] Set up a Postman collection or JMeter script with authenticated requests to simulate concurrent load (50–200 users) (Epic 2 – US 2.2 / Epic 5 – US 5.2)

- [ ] Install and verify profiling tools: VisualVM, JProfiler (or alternative), Java Flight Recorder, async-profiler (Epic 1 – US 1.1)

## Completed


%% kanban:settings
```
{"kanban-plugin":"basic"}
```
%%
