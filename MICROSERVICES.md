# Microservices

All services must receive the same `JWT_SECRET`. The configured development defaults already match.
Gym Management and Trainer Workload also use the same ActiveMQ credentials.

Start the applications in this order:

```bash
cd /Users/matlab/IdeaProjects/gym-management
docker compose up -d activemq postgres
```

```bash
cd /Users/matlab/IdeaProjects/gym-management
./gradlew -p discovery-server bootRun
```

```bash
cd /Users/matlab/IdeaProjects/trainer-workload-service
./gradlew bootRun
```

```bash
cd /Users/matlab/IdeaProjects/gym-management
./gradlew bootRun
```

Service addresses:

- Eureka dashboard: `http://localhost:8761`
- ActiveMQ console: `http://localhost:8161`
- Gym management API: `http://localhost:8080`
- Trainer workload API: `http://localhost:8081/api/v1/trainer-workloads`
- Trainer workload Swagger: `http://localhost:8081/swagger-ui/index.html`

Training operations:

- `POST /api/v1/trainings` adds a training and publishes an `ADD` workload event.
- `DELETE /api/v1/trainings/{trainingId}` cancels a non-past training and publishes a `DELETE` workload event.
- The legacy `POST /api/v1/trainings/add` route remains available.

The services do not make a synchronous REST call for workload updates. Gym Management publishes persistent JSON messages to `trainer.workload.events` through Spring `JmsTemplate`. Trainer Workload consumes them through `@JmsListener`, validates the payload, and records each event ID so duplicate delivery cannot add the duration twice.

The consumer uses transactional JMS sessions and ActiveMQ exponential redelivery. Invalid or repeatedly failing messages are sent to `ActiveMQ.DLQ` after three redeliveries. `app.messaging.consumer-concurrency` defaults to `2-6`, so one instance can run multiple consumers and additional service instances can compete on the same queue.

`application-local.yml`/`application-local.yaml` connect to `tcp://localhost:61616`. The `prod` profiles connect to `tcp://activemq:61616`; all broker settings can be overridden with `ACTIVEMQ_BROKER_URL`, `ACTIVEMQ_USER`, and `ACTIVEMQ_PASSWORD`.

The workload service uses an H2 database with the relational model `trainers -> workload_years -> workload_months` plus processed event IDs. The database console is intentionally disabled; workload data is read through the secured REST API.

Run the quality gates with:

```bash
cd /Users/matlab/IdeaProjects/gym-management && ./gradlew check
cd /Users/matlab/IdeaProjects/trainer-workload-service && ./gradlew check
```
