# Issue 1

- [x] <font size="5"> Missing Endpoint: Get Training Types (Req #17). GET /training-types → returns list of { trainingType,
  trainingTypeId }. I could not find such endpoint in your repo.
</font>

# Issue 2

- [ ] <font size="2"> Two-level logging not implemented (Note #17).
  The task requires two dedicated logging levels: </font>
- <font size="5"> <b>Level 1</b> — Transaction-level logging (transactionId):
  "generate transactionId by which you can track all operations for this transaction; the same transactionId can later
  be passed to downstream services"
  There is no MDC-based transactionId generated per request. No MDC.put("transactionId", UUID.randomUUID()) filter
  exists. </font>
  <br></br> 
- <font size="5"> <b>Level 2</b> — REST call details logging:
  "which endpoint was called, which request came, and service response — 200 or error and response message"
  There is no request/response interceptor or filter that logs incoming method+URL and outgoing HTTP status. Only
  individual log.info(...) lines inside service methods exist — not structured request-level logging. </font>

# Issue 3

- [ ] SecurityConfig.corsConfigurationSource() lists ["GET", "POST", "PUT", "DELETE", "OPTIONS"] — PATCH is missing.
  WebConfig correctly includes PATCH, but Spring Security's CORS takes precedence. This could cause cross-origin PATCH
  calls to the activate/deactivate endpoints to fail from browsers.
- 