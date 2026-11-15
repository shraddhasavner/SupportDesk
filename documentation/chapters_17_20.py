def get_chapters_17_to_20_html():
    return """
<!-- ========================================== -->
<!-- CHAPTER 17: INTERVIEW PREPARATION (CORE 25) -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 17 — Core Technical Interview Questions &amp; Answers</h1>

<p>Here are the 25 most critical interview questions regarding SupportDesk with model answers in both English and Hinglish:</p>

<div class="qa-card">
    <div class="qa-question"><span>Q1. What is SupportDesk and what architectural problems did it solve?</span><span class="badge badge-admin">Core Architecture</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "SupportDesk is an IT help desk ticketing system built with Java 17, Spring Boot, Spring Security, and MySQL. It replaced ad-hoc email communications with a formal state-machine ticket lifecycle (`OPEN` &rarr; `ASSIGNED` &rarr; `IN_PROGRESS` &rarr; `RESOLVED` &rarr; `CLOSED`), an automated 4-tier SLA countdown engine, and strict role-based access control. Key architectural challenges solved include identity spoofing prevention, zero sensitive data leakage in JSON, and stateless JWT revocation upon password changes."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "SupportDesk ईमेल के ज़रिए होने वाली अव्यवस्था को हल करता है। यह टिकट्स का एक निश्चित जीवनचक्र तय करता है, प्राथमिकता अनुसार 4 घंटे से 48 घंटे की SLA समयसीमा निर्धारित करता है, और रोल-बेस्ड सुरक्षा लागू करता है ताकि कर्मचारी केवल अपनी टिकट देख सकें और एडमिन पूरा सिस्टम प्रबंधित कर सकें।"
    </div>
    <div class="qa-meta">Relevant Classes: `TicketService.java`, `SecurityConfig.java` | Confirmed tested: 185 tests</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q2. How is authentication implemented, and how are JWTs verified on each request?</span><span class="badge badge-tech">Security</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "Authentication is stateless using JSON Web Tokens signed with HMAC-SHA256 (`app.jwt.secret`). When a user submits credentials to `POST /api/auth/login`, `AuthService` verifies the BCrypt hash against the database and calls `JwtUtil.generateToken()`. On each subsequent request, `JwtAuthenticationFilter` intercepts the `Authorization: Bearer &lt;token&gt;` header, verifies the cryptographic signature, checks token expiration, confirms the password fingerprint matches the current DB hash, and loads the user into Spring Security's `SecurityContextHolder`."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "लॉगिन पर स्प्रिंग सिक्योरिटी पासवर्ड का BCrypt हैश चेक करती है और सही होने पर एक JWT टोकन देती है। हर अगले अनुरोध पर `JwtAuthenticationFilter` टोकन का सिग्नेचर, एक्सपायरी और पासवर्ड फिंगरप्रिंट चेक करके यूजर को ऑथेंटिकेट करता है।"
    </div>
    <div class="qa-meta">Relevant Classes: `JwtUtil.java`, `JwtAuthenticationFilter.java`, `AuthService.java`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q3. How did you solve the problem of invalidating JWTs after a password change without a database table?</span><span class="badge badge-critical">Advanced Security</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "Because JWTs are stateless, changing a user's password usually leaves existing active tokens valid until expiry. Rather than introducing a heavy database session table or distributed cache (Redis) that would break production `ddl-auto=validate`, I implemented a <strong>Stateless HMAC-SHA256 Password Fingerprint</strong>. In `JwtUtil`, when a token is issued, we compute a one-way HMAC-SHA256 hash of the employee's current BCrypt password hash and embed it as a `pwd` claim. When the user changes their password, a new BCrypt hash is generated. On subsequent requests, `JwtAuthenticationFilter` compares the token's `pwd` claim against the HMAC of the current DB hash; the mismatch causes immediate rejection (HTTP 401)."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "यह एक बहुत प्रभावशाली उत्तर है। सामान्यतः JWT स्टेटलेस होने के कारण पासवर्ड बदलने पर भी 24 घंटे तक चलता रहता है। मैंने बिना किसी नई डेटाबेस टेबल के, यूजर के पासवर्ड हैश का एक सुरक्षित HMAC फिंगरप्रिंट टोकन के अंदर `pwd` क्लेम में रख दिया। जब पासवर्ड बदलता है, तो नया हैश बनता है और पुराना टोकन तुरंत अमान्य (401) हो जाता है।"
    </div>
    <div class="qa-meta">Relevant Classes: `JwtUtil.computePasswordFingerprint()`, `JwtAuthenticationFilter.doFilterInternal()`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q4. How does the SLA calculation engine work?</span><span class="badge badge-tech">Business Logic</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "The SLA engine is implemented in `TicketService.calculateSlaDeadline()`. When a ticket is created, its priority determines the resolution target: CRITICAL is 4 hours, HIGH is 8 hours, MEDIUM is 24 hours, and LOW is 48 hours. The deadline timestamp (`slaDeadline`) is computed by adding these hours to `LocalDateTime.now()` and stored in the `tickets` table. To find overdue tickets, `TicketRepository.findOverdueTickets()` queries all tickets where `slaDeadline &lt; :now` and the status is neither `RESOLVED` nor `CLOSED`."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "टिकट बनते समय ही उसकी प्राथमिकता के आधार पर डेडलाइन तय हो जाती है (जैसे CRITICAL के लिए 4 घंटे)। डेटाबेस में यह समय सेव होता है। यदि समय बीत जाए और टिकट हल न हुआ हो, तो वह 'Overdue' की श्रेणी में आ जाता है।"
    </div>
    <div class="qa-meta">Relevant Classes: `TicketService.java`, `TicketRepository.java`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q5. How do you prevent identity spoofing in ticket creation and commenting?</span><span class="badge badge-critical">RBAC / Security</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "In `TicketController.java`, when an employee submits a ticket or posts a comment with an `employeeId` in the request body, the controller cross-verifies that ID against `securityUtils.getCurrentEmployee()`. If a non-admin user attempts to submit a ticket or comment under someone else's ID, the request is immediately rejected with HTTP 403 Forbidden: <em>'Identity spoofing rejected: You cannot create tickets on behalf of another employee.'</em>"
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "अगर कोई चालाक यूजर JSON रिक्वेस्ट बॉडी में किसी दूसरे कर्मचारी की ID भेजकर टिकट या कमेंट दर्ज करना चाहे, तो हमारा कंट्रोलर टोकन के अंदर मौजूद असली यूजर ID से उसकी तुलना करता है और मिसमैच होने पर 403 Forbidden एरर देता है।"
    </div>
    <div class="qa-meta">Tested in: `SecurityAccessAndRbacTests.testIdentitySpoofingCreateTicketRejected()`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q6. Why did you use `@RestControllerAdvice` and how does it prevent information leakage?</span><span class="badge badge-admin">Architecture</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "`GlobalExceptionHandler.java` uses `@RestControllerAdvice` to intercept exceptions across all controllers. For example, if a `DataIntegrityViolationException` occurs, we log the SQL details internally on the server, but return a sanitized client message: <em>'Database constraint violation: The operation could not be completed due to conflicting data.'</em> This prevents database table names, column constraints, SQL syntax, or internal file paths from ever leaking to external clients."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "ग्लोबल एक्सेप्शन हैंडलर पूरे एप्लिकेशन की गलतियों को पकड़ता है। प्रोडक्शन में कभी भी स्क्रीन पर SQL एरर या जावा का स्टैक ट्रेस नहीं दिखना चाहिए। हम सर्वर लॉग में असली एरर लिखते हैं, लेकिन यूजर को सिर्फ साफ़ और सुरक्षित संदेश (जैसे Bad Request, Conflict) भेजते हैं।"
    </div>
    <div class="qa-meta">Relevant Classes: `GlobalExceptionHandler.java`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q7. What is the difference between JPA and Hibernate in this project?</span><span class="badge badge-emp">Fundamentals</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "JPA (Jakarta Persistence API) is a specification/interface that defines how Java objects map to relational databases. Hibernate is the actual underlying implementation (the engine) that does the heavy lifting. In SupportDesk, we use Spring Data JPA repository interfaces, and Hibernate 7 automatically generates the SQL queries behind the scenes."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "JPA एक नियम पुस्तिका (इंटेरफ़ेस) है, जबकि हाइबरनेट (Hibernate) उस नियम पर काम करने वाला वास्तविक सॉफ्टवेयर (इंजन) है। हम कोड में JPA एनोटेशन्स (`@Entity`, `@ManyToOne`) लिखते हैं, और हाइबरनेट उन्हें SQL में बदलता है।"
    </div>
    <div class="qa-meta">Dependencies: `spring-boot-starter-data-jpa`, `hibernate-core 7.4.5`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q8. How does the employee update process avoid overwriting existing password hashes?</span><span class="badge badge-tech">Service Logic</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "In `EmployeeService.updateEmployee()`, we check if the updated data's password is non-null and not blank. If the administrator only updates the employee's department or phone number, leaving the password blank, the existing encoded BCrypt hash in `existing.getPassword()` is left completely untouched. A new BCrypt hash is only computed if a valid password of 6 or more characters is provided."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "अगर प्रोफ़ाइल एडिट करते समय पासवर्ड फील्ड खाली हो, तो कोड पुराने पासवर्ड हैश को वैसा ही छोड़ देता है। केवल तभी नया BCrypt हैश बनता है जब यूजर कम से कम 6 अक्षरों का नया पासवर्ड भरे।"
    </div>
    <div class="qa-meta">Tested in: `EmployeeServiceTests.testUpdateEmployeeProfileOnlyPreservesPasswordHash()`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q9. What is the N+1 problem, and how was it avoided in the dashboard?</span><span class="badge badge-critical">Database Performance</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "The N+1 problem occurs when an application executes 1 initial query to fetch N parent records, and then executes N separate queries to fetch related child records. In `DashboardService.java`, technician workload calculations previously threatened an N+1 pattern if technicians were queried in a loop. We optimized this using single aggregated queries (`countPendingByTechnicianId`, `countResolvedByTechnicianId`) and direct database count queries (`countByStatus`, `countOverdueTickets`), executing aggregations directly on MySQL."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "N+1 समस्या तब होती है जब डेटाबेस पर लूप चलाकर बार-बार क्वेरी भेजी जाती है। हमने डैशबोर्ड में लूप क्वेरी के बजाय सीधे MySQL से काउंट और एग्रीगेशन क्वेरीज़ चलाईं, जिससे डेटाबेस का लोड बहुत कम हो गया।"
    </div>
    <div class="qa-meta">Relevant Classes: `DashboardService.java`, `TicketRepository.java`</div>
</div>

<div class="qa-card">
    <div class="qa-question"><span>Q10. What is the role of `SecurityContextHolder` in Spring Security?</span><span class="badge badge-tech">Spring Security</span></div>
    <div class="qa-answer-en">
        <strong>English Answer:</strong> "`SecurityContextHolder` is where Spring Security stores details of the currently authenticated principal using a `ThreadLocal` storage strategy. In `JwtAuthenticationFilter`, once the JWT is validated, we construct a `UsernamePasswordAuthenticationToken` containing the user's `UserDetails` and authorities (`ROLE_ADMIN`, `ROLE_TECHNICIAN`, `ROLE_EMPLOYEE`) and set it into `SecurityContextHolder.getContext().setAuthentication(...)`."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "`SecurityContextHolder` एक थ्रेड-लोकल मेमोरी बॉक्स है जहाँ स्प्रिंग सिक्योरिटी वर्तमान में लॉग-इन यूजर की जानकारी और उसके रोल रखती है। कंट्रोलर में कहीं भी `SecurityContextHolder` से पता चल जाता है कि वर्तमान यूजर कौन है।"
    </div>
    <div class="qa-meta">Relevant Classes: `JwtAuthenticationFilter.java`, `SecurityUtils.java`</div>
</div>

<!-- ========================================== -->
<!-- CHAPTER 18: PRACTICAL MOCK INTERVIEW (80 Qs) -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 18 — Practical Mock Interview: 80 Questions Bank</h1>

<p>This chapter provides an exhaustive repository of 80 categorized questions: 30 Beginner, 30 Intermediate, and 20 Advanced questions with concise model answers.</p>

<h2>Section A: 30 Beginner-Level Questions &amp; Answers</h2>
<table class="data-table">
    <thead><tr><th>#</th><th>Beginner Question</th><th>Model Answer (English + Hinglish)</th></tr></thead>
    <tbody>
        <tr><td>1</td><td>What is Spring Boot?</td><td>An opinionated framework that simplifies Spring development by providing auto-configuration and embedded servers (स्प्रिंग डेवलपमेंट को आसान बनाने वाला टूल).</td></tr>
        <tr><td>2</td><td>What is Maven?</td><td>A build automation and dependency management tool using `pom.xml` (लाइब्रेरी और बिल्ड मैनेज करने वाला टूल).</td></tr>
        <tr><td>3</td><td>What port does SupportDesk run on?</td><td>Port 8082, configured in `application.properties` via `server.port=8082`.</td></tr>
        <tr><td>4</td><td>What is `@RestController`?</td><td>A combination of `@Controller` and `@ResponseBody`, returning JSON directly (डेटा को JSON में रिटर्न करने वाला कंट्रोलर).</td></tr>
        <tr><td>5</td><td>What is `@Autowired`?</td><td>Enables automatic dependency injection by Spring's IoC container (ऑब्जेक्ट को ऑटोमैटिक इंजेक्ट करना).</td></tr>
        <tr><td>6</td><td>What is an Entity in JPA?</td><td>A Java class mapped to a database table via `@Entity` (डेटाबेस टेबल का जावा रूप).</td></tr>
        <tr><td>7</td><td>What is `@Table` annotation?</td><td>Specifies the exact table name in the relational database (टेबल का नाम तय करना).</td></tr>
        <tr><td>8</td><td>What is `@Id` and `@GeneratedValue`?</td><td>Defines the primary key and its generation strategy, like `GenerationType.IDENTITY` (प्राइमरी की और ऑटो-इंक्रीमेंट).</td></tr>
        <tr><td>9</td><td>What is a DTO?</td><td>Data Transfer Object: carries data between client and server without exposing entity structures.</td></tr>
        <tr><td>10</td><td>What is BCrypt?</td><td>A salted password hashing function that is computationally resistant to brute-force attacks (पासवर्ड को सुरक्षित एनक्रिप्ट करने वाला अल्गोरिदम).</td></tr>
        <tr><td>11</td><td>What is the difference between GET and POST?</td><td>GET retrieves data safely without side-effects; POST creates a new resource on the server.</td></tr>
        <tr><td>12</td><td>What is the purpose of PUT vs PATCH?</td><td>PUT replaces a resource completely; PATCH applies partial modifications.</td></tr>
        <tr><td>13</td><td>What does HTTP 201 Created mean?</td><td>The request succeeded and led to the creation of a new resource (e.g., ticket created).</td></tr>
        <tr><td>14</td><td>What does HTTP 400 Bad Request mean?</td><td>Client sent malformed data or violated Bean Validation constraints.</td></tr>
        <tr><td>15</td><td>What does HTTP 401 Unauthorized mean?</td><td>Authentication is missing, token expired, or invalid credentials provided.</td></tr>
        <tr><td>16</td><td>What does HTTP 403 Forbidden mean?</td><td>User is authenticated, but lacks permission for that specific endpoint.</td></tr>
        <tr><td>17</td><td>What does HTTP 404 Not Found mean?</td><td>The requested resource ID or URL does not exist.</td></tr>
        <tr><td>18</td><td>What does HTTP 409 Conflict mean?</td><td>Conflict with current database state (e.g. duplicate email registration).</td></tr>
        <tr><td>19</td><td>What does HTTP 500 mean?</td><td>Internal server error: unhandled exception caught by global handler.</td></tr>
        <tr><td>20</td><td>What is an Enum in Java?</td><td>A special data type representing a fixed set of constants (`Role`, `TicketStatus`, `TicketPriority`).</td></tr>
        <tr><td>21</td><td>What are the three roles in SupportDesk?</td><td>`ADMIN`, `TECHNICIAN`, and `EMPLOYEE`.</td></tr>
        <tr><td>22</td><td>What are the 5 ticket statuses?</td><td>`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.</td></tr>
        <tr><td>23</td><td>What are the 4 priorities?</td><td>`CRITICAL` (4h), `HIGH` (8h), `MEDIUM` (24h), `LOW` (48h).</td></tr>
        <tr><td>24</td><td>What does `@Transactional` do?</td><td>Ensures all database operations inside the method either complete fully or roll back completely.</td></tr>
        <tr><td>25</td><td>What is `JpaRepository`?</td><td>Spring Data interface providing CRUD operations (`save`, `findById`, `findAll`, `delete`).</td></tr>
        <tr><td>26</td><td>What is Bean Validation?</td><td>Standard declarative annotations like `@NotBlank`, `@Size`, `@Email` to validate inputs.</td></tr>
        <tr><td>27</td><td>What is MockMvc?</td><td>Spring test utility that simulates HTTP requests to controllers without starting a real server.</td></tr>
        <tr><td>28</td><td>What database is used?</td><td>MySQL 8.0 on port 3306 with database name `supportdesk_db`.</td></tr>
        <tr><td>29</td><td>Where is the application entry point?</td><td>`SupportDeskApplication.java` in package `com.supportdesk`.</td></tr>
        <tr><td>30</td><td>How many automated tests exist?</td><td>185 passing tests with zero errors and zero failures.</td></tr>
    </tbody>
</table>

<h2>Section B: 30 Intermediate-Level Questions &amp; Answers</h2>
<table class="data-table">
    <thead><tr><th>#</th><th>Intermediate Question</th><th>Model Answer (English + Hinglish)</th></tr></thead>
    <tbody>
        <tr><td>31</td><td>How does `JwtAuthenticationFilter` intercept requests?</td><td>Extends `OncePerRequestFilter`, checks `Authorization: Bearer` header, validates token signature and expiry.</td></tr>
        <tr><td>32</td><td>What is the difference between `@Component`, `@Service`, and `@Repository`?</td><td>All are Spring beans; `@Service` designates business logic; `@Repository` handles data access with exception translation.</td></tr>
        <tr><td>33</td><td>How is CORS handled in SupportDesk?</td><td>Configured in `SecurityConfig.java` reading trusted origins from `app.cors.allowed-origins` property.</td></tr>
        <tr><td>34</td><td>Why is CSRF disabled in `SecurityConfig`?</td><td>Because authentication is stateless via Bearer JWTs without cookie-based session tracking.</td></tr>
        <tr><td>35</td><td>What is `SecurityUtils`?</td><td>A helper component that extracts the currently authenticated `Employee` from `SecurityContextHolder`.</td></tr>
        <tr><td>36</td><td>How does the ticket number get formatted?</td><td>Format `TCK-YYYYMMDD-XXXX` using current date and a 6-character unique hex or alphanumeric string.</td></tr>
        <tr><td>37</td><td>What happens if an inactive employee tries to log in?</td><td>`CustomUserDetailsService` loads `UserPrincipal` with `isEnabled()=false`, throwing `DisabledException`/`BadCredentialsException` (HTTP 401).</td></tr>
        <tr><td>38</td><td>What happens if an inactive employee tries to create a ticket?</td><td>`TicketService.createTicket()` checks `creator.isActive()`; if false, throws `IllegalArgumentException` (HTTP 400).</td></tr>
        <tr><td>39</td><td>Can a regular EMPLOYEE view other employees' tickets?</td><td>No. In `TicketController.getAllTickets()`, non-admin, non-technician users only receive tickets where `employee.id == currentUser.id`.</td></tr>
        <tr><td>40</td><td>Can an EMPLOYEE assign a technician?</td><td>No. Endpoint `/api/tickets/{id}/assign` requires the `ADMIN` role. Regular employees receive 403 Forbidden.</td></tr>
        <tr><td>41</td><td>Can an EMPLOYEE update a ticket status to RESOLVED?</td><td>No. Only `ADMIN` and `TECHNICIAN` can update status or submit resolution notes.</td></tr>
        <tr><td>42</td><td>What happens if an admin tries to assign a non-technician employee?</td><td>`TicketService` checks `technician.getRole() == Role.TECHNICIAN`; if false, throws HTTP 400 Bad Request.</td></tr>
        <tr><td>43</td><td>Who is recorded as the actor in technician assignment?</td><td>The authenticated administrator who executed the assignment, while the technician is recorded as the target.</td></tr>
        <tr><td>44</td><td>How are notifications generated?</td><td>`NotificationService.createNotification()` saves a record in `notifications` table linked to the recipient.</td></tr>
        <tr><td>45</td><td>How does the frontend know which tabs to show?</td><td>`app.js` inspects `AuthService.getUser().role` and toggles DOM visibility for admin and technician controls.</td></tr>
        <tr><td>46</td><td>Where is the JWT token stored on the frontend?</td><td>In a private JavaScript closure variable inside `auth.js`, never in `localStorage` or cookies.</td></tr>
        <tr><td>47</td><td>What happens when the browser page is refreshed?</td><td>In-memory state resets, prompting the user to sign in again cleanly.</td></tr>
        <tr><td>48</td><td>What is a derived query in Spring Data JPA?</td><td>A query generated by Spring from method names, like `findByPriority(TicketPriority p)`.</td></tr>
        <tr><td>49</td><td>What is JPQL?</td><td>Java Persistence Query Language: queries entities and their fields rather than raw database tables.</td></tr>
        <tr><td>50</td><td>How does `GlobalExceptionHandler` format field errors?</td><td>Collects all `FieldError` items from `BindingResult` into a `Map&lt;String, String&gt; validationErrors`.</td></tr>
        <tr><td>51</td><td>What is the purpose of `CustomAuthenticationEntryPoint`?</td><td>Intercepts unauthenticated requests at the filter boundary and returns uniform JSON with status 401.</td></tr>
        <tr><td>52</td><td>What is the purpose of `CustomAccessDeniedHandler`?</td><td>Intercepts unauthorized requests (authenticated but lacking role) and returns uniform JSON with status 403.</td></tr>
        <tr><td>53</td><td>How do you verify passwords match in BCrypt?</td><td>Using `passwordEncoder.matches(rawPassword, encodedHash)`.</td></tr>
        <tr><td>54</td><td>Can two employees share the same email?</td><td>No. `existsByEmail()` check enforces uniqueness, throwing `DuplicateEmailException` (HTTP 409).</td></tr>
        <tr><td>55</td><td>Can an admin deactivate themselves?</td><td>No. `EmployeeService` explicitly prohibits admin self-deactivation to prevent accidental lockout.</td></tr>
        <tr><td>56</td><td>Can an admin demote themselves from ADMIN to EMPLOYEE?</td><td>No. `EmployeeService` rejects self-demotion with an `IllegalArgumentException`.</td></tr>
        <tr><td>57</td><td>How are activity logs sorted?</td><td>Chronologically ascending by `createdAt` so history reads from creation to resolution.</td></tr>
        <tr><td>58</td><td>How are comments sorted?</td><td>Chronologically ascending by `createdAt` to display conversation flow.</td></tr>
        <tr><td>59</td><td>What is HikariCP?</td><td>High-performance JDBC connection pool used by default in Spring Boot.</td></tr>
        <tr><td>60</td><td>How does `@Transactional` handle exceptions?</td><td>By default, it rolls back for unchecked exceptions (`RuntimeException` and `Error`).</td></tr>
    </tbody>
</table>

<h2>Section C: 20 Advanced-Level Questions &amp; Answers</h2>
<table class="data-table">
    <thead><tr><th>#</th><th>Advanced Question</th><th>Model Answer (English + Hinglish)</th></tr></thead>
    <tbody>
        <tr><td>61</td><td>Explain the HMAC password fingerprint revocation mechanism.</td><td>Generates HMAC-SHA256 of the user's BCrypt hash and stores it in token claim `pwd`. A password change produces a new hash, causing fingerprint mismatch and immediate token rejection.</td></tr>
        <tr><td>62</td><td>Why not use a Redis token blacklist instead?</td><td>Redis adds infrastructure overhead and distributed state. The HMAC fingerprint is 100% stateless and requires zero database migrations or external cache services.</td></tr>
        <tr><td>63</td><td>What are the limitations of the HMAC password fingerprint?</td><td>It revokes all previous tokens upon password change, but does not support selective single-device logout.</td></tr>
        <tr><td>64</td><td>How do you handle SQL injection prevention in SupportDesk?</td><td>Spring Data JPA and Hibernate use JDBC prepared statements with parameterized inputs for all queries.</td></tr>
        <tr><td>65</td><td>How does the overdue query perform at scale?</td><td>`findOverdueTickets` filters by `slaDeadline &lt; :now` and indexed `status` column, avoiding full table scans.</td></tr>
        <tr><td>66</td><td>Why use Text Blocks in Spring Boot tests?</td><td>Text Blocks (`\"\"\"`) provide readable multiline JSON payloads directly in test methods without escaping quotes.</td></tr>
        <tr><td>67</td><td>How does `@Transactional(readOnly = true)` optimize performance?</td><td>Informs Hibernate not to track dirty entities for flush checks, reducing memory and CPU overhead.</td></tr>
        <tr><td>68</td><td>Explain Spring Security's `SecurityFilterChain` bean.</td><td>Configures `HttpSecurity` rules, disabling CSRF, setting `SessionCreationPolicy.STATELESS`, and registering `JwtAuthenticationFilter`.</td></tr>
        <tr><td>69</td><td>How does `UserPrincipal` bridge Employee and Spring Security?</td><td>Implements `UserDetails`, mapping `Employee.getEmail()` to username and `Employee.getRole()` to `GrantedAuthority`.</td></tr>
        <tr><td>70</td><td>What is the difference between Unit Tests and Integration Tests in your suite?</td><td>Unit tests (`TicketServiceTests`) test business logic in isolation; Integration tests (`SecurityAccessAndRbacTests`) use MockMvc with full Spring application context.</td></tr>
        <tr><td>71</td><td>How do you prevent Mass Assignment attacks?</td><td>By never binding request payloads directly to entities; request DTOs strictly define permissible fields.</td></tr>
        <tr><td>72</td><td>How do you prevent XSS attacks in SupportDesk?</td><td>Tokens are stored in private closure memory, and the frontend dynamically sets text using `.textContent` rather than raw `.innerHTML`.</td></tr>
        <tr><td>73</td><td>Why does `application-prod.properties` use `ddl-auto=validate`?</td><td>To prevent Hibernate from altering production database schemas dynamically at runtime, ensuring DBA schema governance.</td></tr>
        <tr><td>74</td><td>How would you scale SupportDesk horizontally?</td><td>Because authentication is stateless (JWT + HMAC fingerprint), multiple backend instances can sit behind a load balancer without sticky sessions.</td></tr>
        <tr><td>75</td><td>How would you migrate to WebSocket for real-time notifications?</td><td>Add `spring-boot-starter-websocket`, configure a STOMP broker, and publish events via `SimpMessagingTemplate` in `NotificationService`.</td></tr>
        <tr><td>76</td><td>What is the difference between pessimistic and optimistic locking?</td><td>Optimistic locking uses a `@Version` column; pessimistic locking uses database row locks (`SELECT ... FOR UPDATE`).</td></tr>
        <tr><td>77</td><td>Where is pessimistic locking used in SupportDesk?</td><td>In `EmployeeRepository.findActiveEmployeesByRoleForUpdate(Role.ADMIN)` to prevent race conditions during admin demotions.</td></tr>
        <tr><td>78</td><td>What happens if two admins assign the same ticket simultaneously?</td><td>The last transaction to commit updates the assigned technician and logs an activity entry; database integrity is maintained.</td></tr>
        <tr><td>79</td><td>How is password security verified in tests?</td><td>`jsonPath(\"$.employee\", not(hasKey(\"password\")))` explicitly tests that passwords and hashes are never serialized into responses.</td></tr>
        <tr><td>80</td><td>What was your personal proudest technical accomplishment in this project?</td><td>Devising and verifying the stateless HMAC password revocation mechanism that solved a critical security gap with zero database schema changes.</td></tr>
    </tbody>
</table>

<!-- ========================================== -->
<!-- CHAPTER 19: REVISION NOTES & CHEAT SHEET -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 19 — Quick Revision Notes &amp; Last-Day Checklist</h1>

<h2>19.1 One-Page Architecture Cheat Sheet (एक-पेज त्वरित संदर्भ)</h2>
<div class="bilingual-box">
    <div class="bilingual-header">SupportDesk at a Glance</div>
    <div class="english-part">
        <ul>
            <li><strong>Port &amp; Base URL:</strong> `http://localhost:8082` (API Base: `/api`)</li>
            <li><strong>Database:</strong> MySQL 8.0 (`supportdesk_db`), 5 Tables (`employees`, `tickets`, `ticket_comments`, `ticket_activities`, `notifications`)</li>
            <li><strong>Security:</strong> Stateless JWT (HMAC-SHA256), 24h expiry, BCrypt password hashing, HMAC `pwd` fingerprint token revocation</li>
            <li><strong>Roles:</strong> `ADMIN` (Full control), `TECHNICIAN` (Assigned tickets, resolve, workload), `EMPLOYEE` (Create, view own tickets)</li>
            <li><strong>SLA Tiers:</strong> `CRITICAL` (4h), `HIGH` (8h), `MEDIUM` (24h), `LOW` (48h)</li>
            <li><strong>Ticket Lifecycle:</strong> `OPEN` &rarr; `ASSIGNED` &rarr; `IN_PROGRESS` &rarr; `RESOLVED` &rarr; `CLOSED`</li>
            <li><strong>Key Classes:</strong> `TicketService`, `EmployeeService`, `JwtUtil`, `JwtAuthenticationFilter`, `GlobalExceptionHandler`</li>
            <li><strong>Test Suite:</strong> 185 tests, 0 failures, 0 errors, 100% passing build</li>
        </ul>
    </div>
</div>

<h2>19.2 Last-Day Interview Readiness Checklist (अंतिम दिन की चेकलिस्ट)</h2>
<ul>
    <li>[x] I can explain the 30-second and 3-minute elevator pitches fluently.</li>
    <li>[x] I can explain the difference between Entity and DTO with security justifications.</li>
    <li>[x] I know the 4 priority SLA hours: 4h, 8h, 24h, 48h.</li>
    <li>[x] I can explain how JWT password fingerprinting (`pwd` claim) revokes tokens without DB tables.</li>
    <li>[x] I know the 5 statuses: `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.</li>
    <li>[x] I can trace a request from `api.js` to MySQL through Controller, Service, and Repository.</li>
    <li>[x] I know the exact test count: 185 tests in JUnit 5 / Spring Boot Test.</li>
    <li>[x] I can explain why passwords are never returned in JSON (`@JsonIgnore` / DTO separation).</li>
</ul>

<!-- ========================================== -->
<!-- CHAPTER 20: PERSONAL PRACTICE PLAN -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 20 — Personal 14-Day Practice &amp; Mastery Plan</h1>

<p>This 14-day study plan is structured for working developers and students to achieve 100% interview confidence:</p>

<table class="data-table">
    <thead><tr><th>Day</th><th>Focus Topic</th><th>Files to Read &amp; Inspect</th><th>Daily Practice Goal</th></tr></thead>
    <tbody>
        <tr><td>Day 1</td><td>Project Introduction &amp; Pitch</td><td>`README.md`, Chapter 1</td><td>Practice 30s and 3m pitches out loud until natural.</td></tr>
        <tr><td>Day 2</td><td>Folder Structure &amp; Config</td><td>`pom.xml`, `application.properties`, Chapter 3</td><td>Explain `@SpringBootApplication` and profiles (`dev`/`prod`).</td></tr>
        <tr><td>Day 3</td><td>Entity Design &amp; Schema</td><td>`Employee.java`, `Ticket.java`, Chapter 5</td><td>Draw the ER diagram from memory on paper.</td></tr>
        <tr><td>Day 4</td><td>Entity vs DTO Separation</td><td>`CreateTicketRequest.java`, `LoginResponse.java`</td><td>Explain Mass Assignment vulnerability and password leakage.</td></tr>
        <tr><td>Day 5</td><td>Spring Security &amp; Filter Chain</td><td>`SecurityConfig.java`, `JwtAuthenticationFilter.java`</td><td>Explain the filter chain execution flow step by step.</td></tr>
        <tr><td>Day 6</td><td>JWT &amp; Password Revocation</td><td>`JwtUtil.java`, `PasswordChangeTokenInvalidationTests.java`</td><td>Explain HMAC-SHA256 password fingerprinting mechanism.</td></tr>
        <tr><td>Day 7</td><td>Employee Management</td><td>`EmployeeService.java`, `EmployeeController.java`</td><td>Explain duplicate email and admin lockout protection logic.</td></tr>
        <tr><td>Day 8</td><td>Ticket Lifecycle</td><td>`Ticket.java`, `TicketStatus.java`, Chapter 7</td><td>Draw the 5-state state machine diagram on paper.</td></tr>
        <tr><td>Day 9</td><td>SLA Engine &amp; Overdue Tracking</td><td>`TicketService.java`, `TicketRepository.java`</td><td>Explain how overdue tickets are queried in JPQL.</td></tr>
        <tr><td>Day 10</td><td>Comments &amp; Activity Audit</td><td>`TicketActivity.java`, `TicketComment.java`</td><td>Explain why the administrator is the actor in assignment.</td></tr>
        <tr><td>Day 11</td><td>Exception Handling &amp; Validation</td><td>`GlobalExceptionHandler.java`, Chapter 12</td><td>Explain how 400, 401, 403, 404, 409, 500 are handled.</td></tr>
        <tr><td>Day 12</td><td>Testing &amp; Verification</td><td>`SecurityAccessAndRbacTests.java`, Chapter 13</td><td>Run `.\mvnw.cmd test` and inspect surefire test reports.</td></tr>
        <tr><td>Day 13</td><td>Troubleshooting &amp; Limitations</td><td>Chapter 15, Chapter 16</td><td>Answer: "What are the project's limitations and future upgrades?"</td></tr>
        <tr><td>Day 14</td><td>Full Mock Interview Drill</td><td>Chapter 17, Chapter 18</td><td>Answer 20 random questions from the mock bank without notes.</td></tr>
    </tbody>
</table>

<!-- ========================================== -->
<!-- APPENDIX & GLOSSARY -->
<!-- ========================================== -->
<h1 class="chapter-title">Appendix &amp; Technical Terms Glossary</h1>

<h2>Key Technical Terminology (शब्दावली)</h2>
<div class="bilingual-box">
    <div class="bilingual-header">Glossary of Core Engineering Terms</div>
    <div class="english-part">
        <ul>
            <li><strong>Stateless:</strong> An architecture where the server retains no client session data between requests. Every request must carry all authentication credentials (e.g., Bearer JWT).</li>
            <li><strong>RBAC (Role-Based Access Control):</strong> Restricting system access to authorized users based on their assigned corporate roles (`ADMIN`, `TECHNICIAN`, `EMPLOYEE`).</li>
            <li><strong>BCrypt:</strong> An adaptive cryptographic hash function based on the Blowfish cipher incorporating salting and work factor iterations to defend against hardware brute-forcing.</li>
            <li><strong>HMAC (Hash-based Message Authentication Code):</strong> A cryptographic construction using a secret key and a hash function (SHA-256) to verify data integrity and authenticity.</li>
            <li><strong>SLA (Service Level Agreement):</strong> A defined time commitment for resolving technical support requests based on issue severity.</li>
            <li><strong>MockMvc:</strong> Part of the Spring Test framework providing support for testing Spring MVC controllers with mocked HTTP requests and response assertions.</li>
            <li><strong>Idempotent:</strong> An operation that produces the exact same state even if executed multiple times (e.g., `PUT`, `DELETE`, or seeding existing database accounts).</li>
        </ul>
    </div>
</div>

<h2>Verified Source File Reference Index</h2>
<table class="data-table">
    <thead><tr><th>Component</th><th>Source File Location</th><th>Primary Responsibility</th></tr></thead>
    <tbody>
        <tr><td>Application Entry</td><td>`src/main/java/com/supportdesk/SupportDeskApplication.java`</td><td>Bootstrap Spring Boot and Tomcat container.</td></tr>
        <tr><td>Security Filter</td><td>`src/main/java/com/supportdesk/security/JwtAuthenticationFilter.java`</td><td>Intercept requests and validate JWT tokens.</td></tr>
        <tr><td>JWT Utility</td><td>`src/main/java/com/supportdesk/security/JwtUtil.java`</td><td>Token generation, verification, and HMAC fingerprinting.</td></tr>
        <tr><td>Ticket Service</td><td>`src/main/java/com/supportdesk/service/TicketService.java`</td><td>Ticket creation, SLA calculation, and assignment.</td></tr>
        <tr><td>Employee Service</td><td>`src/main/java/com/supportdesk/service/EmployeeService.java`</td><td>Employee CRUD, email validation, password preservation.</td></tr>
        <tr><td>Exception Handler</td><td>`src/main/java/com/supportdesk/controller/GlobalExceptionHandler.java`</td><td>Centralized error translation and data sanitization.</td></tr>
        <tr><td>Client Application</td><td>`src/main/resources/static/index.html` &amp; `app.js`</td><td>Responsive Single-Page Application interface.</td></tr>
    </tbody>
</table>
"""
