# Issue 1

- [x] <font size="5"> Missing Endpoint: Get Training Types (Req #17). GET /training-types → returns list of { trainingType,
  trainingTypeId }. I could not find such endpoint in your repo.
</font>

# Issue 2

- [x] <font size="2"> Two-level logging implemented (Note #17).
  The task requires two dedicated logging levels: </font>
- <font size="5"> <b>Level 1</b> — Transaction-level logging (transactionId):
  "generate transactionId by which you can track all operations for this transaction; the same transactionId can later
  be passed to downstream services"
  `TransactionIdFilter` now stores `transactionId` in MDC and returns it as `X-Transaction-Id`. </font>
  <br></br>
- <font size="5"> <b>Level 2</b> — REST call details logging:
  "which endpoint was called, which request came, and service response — 200 or error and response message"
  `RestLoggingFilter` now logs method, URI, sanitized request/response bodies, status, and duration. </font>

# Issue 3

- [x] SecurityConfig.corsConfigurationSource() lists PATCH for activate/deactivate endpoints.
  WebConfig and Spring Security CORS both allow PATCH.
