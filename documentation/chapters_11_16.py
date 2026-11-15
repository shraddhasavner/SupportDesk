def get_chapters_11_to_16_html(assets):
    s_notif = assets['s_notif']
    s_workload = assets['s_workload']

    return f"""
<!-- ========================================== -->
<!-- CHAPTER 11: FRONTEND & BACKEND INTEGRATION -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 11 — Frontend SPA Architecture &amp; API Integration</h1>

<h2>11.1 The In-Memory Session Architecture (`auth.js`)</h2>
<p>Modern web security best practices strongly advise against storing sensitive bearer tokens in browser `localStorage` or `sessionStorage` due to vulnerability to Cross-Site Scripting (XSS) extraction attacks. SupportDesk implements a secure <strong>In-Memory Closure Pattern</strong>:</p>

<div class="code-container">
    <div class="code-header"><span>auth.js</span><span>In-Memory Private Closure</span></div>
    <pre class="code-block">
const AuthService = (function() {{
    // Private in-memory variables protected inside closure
    let _token = null;
    let _user = null;

    return {{
        getToken() {{ return _token; }},
        setToken(token) {{ _token = token; }},
        getUser() {{ return _user; }},
        setUser(user) {{ _user = user; }},
        isAuthenticated() {{ return Boolean(_token &amp;&amp; _user); }},
        clear() {{ _token = null; _user = null; }}
    }};
}})();
    </pre>
</div>

<div class="figure-container">
    <img src="{s_notif}" class="figure-img" alt="In-App Notifications">
    <div class="figure-caption">Figure 11.1: Live In-App Notification Dropdown tracking assigned tickets and status events</div>
</div>

<div class="figure-container">
    <img src="{s_workload}" class="figure-img" alt="Technician Workload">
    <div class="figure-caption">Figure 11.2: Technician Workload Analytics Table displaying pending vs resolved ticket distribution</div>
</div>

<!-- ========================================== -->
<!-- CHAPTER 12: EXCEPTION HANDLING & VALIDATION -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 12 — Input Validation &amp; Centralized Exception Handling</h1>

<h2>12.1 Centralized Exception Handling Architecture</h2>
<p>SupportDesk intercepts all controller exceptions using a centralized `@RestControllerAdvice` component, guaranteeing uniform JSON structures and eliminating default Whitelabel HTML error leaks:</p>

<div class="code-container">
    <div class="code-header"><span>Standard API Error Response Schema</span><span>Uniform JSON Contract</span></div>
    <pre class="code-block">
{{
    "timestamp": "2026-10-24T13:40:00.123",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for one or more fields",
    "validationErrors": {{
        "title": "Title is required",
        "priority": "Priority is required (LOW, MEDIUM, HIGH, CRITICAL)"
    }}
}}
    </pre>
</div>

<h2>12.2 Critical Code Analysis: Data Leakage Protection (`GlobalExceptionHandler.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>GlobalExceptionHandler.java</span><span>Defensive Sanitization</span></div>
    <pre class="code-block">
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity&lt;Map&lt;String, Object&gt;&gt; handleDataIntegrityViolation(DataIntegrityViolationException ex) {{
    // Server logs the real SQL and constraint error for engineers
    logger.error("Database constraint violation occurred: ", ex);
    // Client receives sanitized error without leaking SQL table or column details
    return buildErrorResponse(
        HttpStatus.CONFLICT,
        "Database constraint violation: The operation could not be completed due to conflicting data."
    );
}}

@ExceptionHandler(Exception.class)
public ResponseEntity&lt;Map&lt;String, Object&gt;&gt; handleGeneralException(Exception ex) {{
    logger.error("Unhandled server exception encountered: ", ex);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected internal server error occurred. Please contact support or try again later."
    );
}}
    </pre>
</div>

<!-- ========================================== -->
<!-- CHAPTER 13: TESTING -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 13 — Automated Testing &amp; Verification Suite</h1>

<h2>13.1 Test Architecture &amp; Methodology (परीक्षण वास्तुकला)</h2>
<p>SupportDesk is safeguarded by <strong>185 automated tests</strong> spanning unit, integration, and security regression layers:</p>

<table class="data-table">
    <thead>
        <tr>
            <th>Test Class</th>
            <th>Layer Tested</th>
            <th>Tests</th>
            <th>Primary Verifications</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>`JwtUtilTests.java`</td>
            <td>Security Unit</td>
            <td>8</td>
            <td>Token creation, claims decoding, expiration rejection, password fingerprinting.</td>
        </tr>
        <tr>
            <td>`AuthControllerTests.java`</td>
            <td>Web / Security</td>
            <td>5</td>
            <td>Login success, bad credentials, inactive user rejection, zero password leakage.</td>
        </tr>
        <tr>
            <td>`PasswordChangeTokenInvalidationTests.java`</td>
            <td>Security Integration</td>
            <td>6</td>
            <td>Old token revocation on password change, new token works, other users unaffected.</td>
        </tr>
        <tr>
            <td>`SecurityAccessAndRbacTests.java`</td>
            <td>Security / RBAC</td>
            <td>10</td>
            <td>401 unauthenticated, 403 role forbidden, identity spoofing prevention, ticket ownership.</td>
        </tr>
        <tr>
            <td>`EmployeeControllerTests.java`</td>
            <td>Web Controller</td>
            <td>16</td>
            <td>CRUD endpoints, soft delete, password preservation, self-deactivation protection.</td>
        </tr>
        <tr>
            <td>`TicketControllerTests.java`</td>
            <td>Web Controller</td>
            <td>13</td>
            <td>Ticket creation, technician assignment, status transitions, SLA endpoints.</td>
        </tr>
        <tr>
            <td>`GlobalExceptionHandlerTests.java`</td>
            <td>Web Exception</td>
            <td>18</td>
            <td>Fault simulation, HTTP 400/401/403/404/405/409/500, data leakage sanitization.</td>
        </tr>
        <tr>
            <td>`TicketServiceTests.java`</td>
            <td>Service Unit</td>
            <td>11</td>
            <td>SLA calculation for all 4 priorities, overdue identification, assignment audit.</td>
        </tr>
        <tr>
            <td>`DashboardServiceTests.java`</td>
            <td>Service Unit</td>
            <td>7</td>
            <td>Summary metrics, technician workload aggregations, date-range reports.</td>
        </tr>
        <tr>
            <td>Other Integration &amp; Repositories</td>
            <td>Various</td>
            <td>91</td>
            <td>CORS tests, static resource access, JPA query methods, entity constraints.</td>
        </tr>
        <tr>
            <td><strong>TOTAL VERIFIED SUITE</strong></td>
            <td><strong>Full Application</strong></td>
            <td><strong>185</strong></td>
            <td><strong>185 Passed, 0 Failures, 0 Errors, 0 Skipped (BUILD SUCCESS)</strong></td>
        </tr>
    </tbody>
</table>

<h2>13.2 How to Execute Tests (टेस्ट कैसे चलाएं)</h2>
<div class="code-container">
    <div class="code-header"><span>Command Line</span><span>Maven Test Commands</span></div>
    <pre class="code-block">
# 1. Run the entire automated test suite:
.\mvnw.cmd test

# 2. Run focused security and token revocation tests:
.\mvnw.cmd test "-Dtest=PasswordChangeTokenInvalidationTests,JwtUtilTests,AuthControllerTests,SecurityAccessAndRbacTests"

# 3. Run ticket and SLA service tests:
.\mvnw.cmd test "-Dtest=TicketServiceTests,TicketControllerTests"
    </pre>
</div>

<!-- ========================================== -->
<!-- CHAPTER 14: SETUP & RUNNING -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 14 — Setup, Configuration &amp; Deployment Guide</h1>

<h2>14.1 Prerequisites (पूर्वापेक्षाएँ)</h2>
<ul>
    <li><strong>Java Development Kit (JDK):</strong> Version 17 LTS or higher</li>
    <li><strong>Build Tool:</strong> Apache Maven 3.8+ (or included Maven Wrapper `mvnw.cmd`)</li>
    <li><strong>Relational Database:</strong> MySQL Server 8.0 running on `localhost:3306`</li>
    <li><strong>Web Browser:</strong> Google Chrome, Microsoft Edge, or Mozilla Firefox</li>
</ul>

<h2>14.2 MySQL Database Initialization</h2>
<div class="code-container">
    <div class="code-header"><span>MySQL Command Line</span><span>Database Creation</span></div>
    <pre class="code-block">
-- Connect to MySQL:
mysql -u root -p

-- Create the database (Character set utf8mb4 for full Unicode support):
CREATE DATABASE supportdesk_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify creation:
SHOW DATABASES;
    </pre>
</div>

<h2>14.3 Launching the Application</h2>
<div class="code-container">
    <div class="code-header"><span>Terminal</span><span>Spring Boot Bootstrapping</span></div>
    <pre class="code-block">
# In project root: D:\Kamlesh_September_Oct_Pro_Java\SupportDesk
.\mvnw.cmd spring-boot:run

# Application starts on port 8082:
# Open browser at: http://localhost:8082/login.html
# Default Admin Login: admin.main@supportdesk.com / Pass123
    </pre>
</div>

<!-- ========================================== -->
<!-- CHAPTER 15: TROUBLESHOOTING -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 15 — Production Troubleshooting &amp; Debugging</h1>

<table class="data-table">
    <thead><tr><th>Observed Issue</th><th>Confirmed Root Cause</th><th>Resolution Step</th></tr></thead>
    <tbody>
        <tr>
            <td><strong>HTTP 401 vs 403 Confusion</strong></td>
            <td>401 means "Unauthenticated" (missing or expired JWT); 403 means "Forbidden" (authenticated, but lacking required role).</td>
            <td>Provide valid credentials in `POST /api/auth/login` for 401; verify role permissions in `SecurityConfig` for 403.</td>
        </tr>
        <tr>
            <td><strong>CORS Cross-Origin Failure</strong></td>
            <td>Frontend running on different port (e.g. 3000) not listed in `app.cors.allowed-origins`.</td>
            <td>Add origin to `application.properties` or supply `APP_CORS_ALLOWED_ORIGINS` environment variable.</td>
        </tr>
        <tr>
            <td><strong>Token Suddenly Invalidated</strong></td>
            <td>The user's password was changed, triggering HMAC fingerprint mismatch in `JwtAuthenticationFilter`.</td>
            <td>Expected security behavior: Re-authenticate with new password to receive fresh token.</td>
        </tr>
        <tr>
            <td><strong>Database Connection Refused</strong></td>
            <td>MySQL service is stopped or port 3306 is blocked.</td>
            <td>Verify MySQL status in Windows Services or run `Start-Service MySQL80`.</td>
        </tr>
    </tbody>
</table>

<!-- ========================================== -->
<!-- CHAPTER 16: LIMITATIONS & FUTURE ROADMAP -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 16 — Technical Limitations &amp; Future Roadmap</h1>

<div class="bilingual-box">
    <div class="bilingual-header">Truthful Architectural Audit (सच्ची तकनीकी समीक्षा)</div>
    <div class="english-part">
        <strong>Current Limitations:</strong>
        1. <strong>Database Migrations:</strong> Currently uses Hibernate `update` in dev and `validate` in prod. Recommended upgrade: Flyway or Liquibase for versioned SQL migrations.
        2. <strong>Real-time Updates:</strong> In-app notifications rely on client-side polling rather than WebSocket/STOMP push.
        3. <strong>Distributed Token Blacklist:</strong> The HMAC password fingerprint invalidates tokens on password change, but explicit single-device logout relies on client-side token deletion. Multi-node distributed logout could benefit from Redis.
        4. <strong>Cloud Deployment:</strong> Tested locally and container-ready; not currently deployed to a cloud cluster (AWS/GCP).
    </div>
    <div class="hindi-part">
        <strong>Hinglish:</strong> एक ईमानदार डेवलपर इंटरव्यू में कभी झूठे दावे नहीं करता। हमारे प्रोजेक्ट में ये वास्तविक सीमाएँ हैं: अभी इसमें डेटाबेस माइग्रेशन टूल (Flyway) नहीं है, रीयल-टाइम अपडेट्स के लिए WebSocket के बजाय पोलिंग है, और यह लोकल सर्वर पर पूरी तरह टेस्टेड है पर एडब्ल्यूएस/क्लाउड पर डिप्लॉयड नहीं है। भविष्य में इन्हें अपग्रेड किया जा सकता है।
    </div>
</div>
"""
