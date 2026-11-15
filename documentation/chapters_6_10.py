def get_chapters_6_to_10_html(assets):
    d_life = assets['d_life']
    d_jwt = assets['d_jwt']
    s_login = assets['s_login']
    s_tickets = assets['s_tickets']
    s_create_t = assets['s_create_t']
    s_details = assets['s_details']
    s_assign = assets['s_assign']
    s_status = assets['s_status']
    s_comments = assets['s_comments']
    s_activity = assets['s_activity']
    s_overdue = assets['s_overdue']
    s_emp = assets['s_emp']
    s_create_emp = assets['s_create_emp']

    return f"""
<!-- ========================================== -->
<!-- CHAPTER 6: EMPLOYEE MANAGEMENT -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 6 — Employee Administration &amp; Access Boundaries</h1>

<h2>6.1 Feature Overview &amp; Business Rules (कर्मचारी प्रबंधन के नियम)</h2>
<p>The Employee Administration subsystem manages corporate user identities, role assignments, department categorization, and soft-delete lifecycle operations.</p>

<div class="bilingual-box">
    <div class="bilingual-header">Core Business Rules in EmployeeService (मुख्य व्यावसायिक नियम)</div>
    <div class="english-part">
        1. <strong>Duplicate Email Protection:</strong> Ensures corporate emails are globally unique. Throws `DuplicateEmailException` resulting in HTTP 409 Conflict.
        2. <strong>Admin Lockout Safeguard:</strong> Prevents administrators from deactivating their own accounts or demoting themselves from `ADMIN` role.
        3. <strong>Last Admin Protection:</strong> Prevents demoting or deactivating the last active administrator in the database.
        4. <strong>Safe Password Updates:</strong> When updating an employee profile, if the password field is left null or blank (`""`), the existing encoded BCrypt hash is preserved intact. If a new password is provided, it is validated (&ge; 6 characters) and re-encoded.
    </div>
    <div class="hindi-part">
        1. <strong>ईमेल की विशिष्टता (Unique Email):</strong> यदि कोई नया कर्मचारी पहले से मौजूद ईमेल से रजिस्टर करने की कोशिश करता है, तो सिस्टम `DuplicateEmailException` (HTTP 409) फेंकता है।
        2. <strong>एडमिन सेल्फ-लॉकआउट से बचाव:</strong> एडमिनिस्ट्रेटर खुद के अकाउंट को डीएक्टिवेट (सस्पेंड) नहीं कर सकता और न ही अपने रोल को कम कर सकता है।
        3. <strong>अंतिम एडमिन सुरक्षा:</strong> सिस्टम कंपनी के अंतिम सक्रिय एडमिन को डिलीट या डिमोट करने की अनुमति नहीं देता ताकि सिस्टम अनाथ (orphaned) न हो।
        4. <strong>सुरक्षित पासवर्ड अपडेट:</strong> प्रोफ़ाइल एडिट करते समय यदि पासवर्ड खाली छोड़ा जाए, तो पुराना पासवर्ड हैश सुरक्षित रहता है; बदलने पर ही नया BCrypt हैश बनता है।
    </div>
</div>

<h2>6.2 Genuine UI Screenshots (वास्तविक स्क्रीनशॉट)</h2>
<div class="figure-container">
    <img src="{s_emp}" class="figure-img" alt="Employee Directory">
    <div class="figure-caption">Figure 6.1: Real Employee Directory View displaying employee cards, roles, departments, and active statuses</div>
</div>

<div class="figure-container">
    <img src="{s_create_emp}" class="figure-img" alt="Create Employee Modal">
    <div class="figure-caption">Figure 6.2: Add Employee Modal with Name, Email, Department, Role selection, and Password fields</div>
</div>

<h2>6.3 Critical Code Analysis (`EmployeeService.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>EmployeeService.java</span><span>Lines 121–174</span></div>
    <pre class="code-block">
public Employee updateEmployee(Long id, UpdateEmployeeRequest updatedData) {{
    if (updatedData == null) {{
        throw new IllegalArgumentException("Updated employee data cannot be null.");
    }}

    Employee existing = getEmployeeById(id);

    // 1. Check duplicate email if changed
    String normalizedNewEmail = updatedData.getEmail() != null ? updatedData.getEmail().trim().toLowerCase() : "";
    if (!existing.getEmail().equalsIgnoreCase(normalizedNewEmail)) {{
        if (employeeRepository.existsByEmail(normalizedNewEmail)) {{
            throw new DuplicateEmailException("Email '" + updatedData.getEmail() + "' is already in use.");
        }}
        existing.setEmail(normalizedNewEmail);
    }}

    // 2. Prevent demoting the last active administrator or demoting oneself
    if (existing.getRole() == Role.ADMIN && updatedData.getRole() != null && updatedData.getRole() != Role.ADMIN && existing.isActive()) {{
        Employee currentEmployee = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
        if (currentEmployee != null && currentEmployee.getId().equals(id)) {{
            throw new IllegalArgumentException("Administrators cannot demote their own account from the ADMIN role.");
        }}
        List&lt;Employee&gt; activeAdmins = employeeRepository.findActiveEmployeesByRoleForUpdate(Role.ADMIN);
        if (activeAdmins.size() &lt;= 1) {{
            throw new IllegalStateException("Cannot demote the last active administrator account.");
        }}
    }}

    if (updatedData.getName() != null) existing.setName(updatedData.getName().trim());
    if (updatedData.getDepartment() != null) existing.setDepartment(updatedData.getDepartment().trim());
    if (updatedData.getRole() != null) existing.setRole(updatedData.getRole());

    // 3. Preserve password if blank; encode only if valid new password is provided
    if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {{
        String rawPassword = updatedData.getPassword().trim();
        if (rawPassword.length() &lt; 6) {{
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }}
        existing.setPassword(passwordEncoder.encode(rawPassword));
    }}

    return employeeRepository.save(existing);
}}
    </pre>
</div>

<table class="line-explain-table">
    <thead><tr><th>Code Line</th><th>Technical Explanation (तकनीकी व्याख्या)</th></tr></thead>
    <tbody>
        <tr>
            <td class="line-code">if (!existing.getEmail().equalsIgnoreCase(...))</td>
            <td>Checks if the user is attempting to modify their email address. If modified, queries `existsByEmail()` to prevent duplicate registrations across tenants.</td>
        </tr>
        <tr>
            <td class="line-code">if (currentEmployee.getId().equals(id))</td>
            <td>Checks if the currently authenticated user in `SecurityContextHolder` is attempting to demote themselves from `ADMIN` to `EMPLOYEE`, which would cause self-lockout.</td>
        </tr>
        <tr>
            <td class="line-code">if (activeAdmins.size() &lt;= 1)</td>
            <td>Locks the admin role if only one administrator remains active in the company, maintaining governance continuity.</td>
        </tr>
        <tr>
            <td class="line-code">if (updatedData.getPassword() != null &amp;&amp; !blank)</td>
            <td>Solves the classic beginner bug where updating an employee's phone number accidentally wipes out or overwrites their encrypted password hash with null.</td>
        </tr>
    </tbody>
</table>

<!-- ========================================== -->
<!-- CHAPTER 7: TICKET MANAGEMENT -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 7 — Ticket Lifecycle &amp; Assignment Workflow</h1>

<h2>7.1 Ticket Lifecycle State Machine (टिकट का जीवनचक्र)</h2>
<p>SupportDesk governs tickets through an explicit five-stage finite state machine:</p>

<div class="figure-container">
    <img src="{d_life}" class="figure-img" alt="Ticket Lifecycle">
    <div class="figure-caption">Figure 7.1: SupportDesk Finite State Machine and Priority-Based SLA Timelines</div>
</div>

<h2>7.2 Genuine UI Screenshots (वास्तविक टिकट प्रबंधन इंटरफ़ेस)</h2>
<div class="figure-container">
    <img src="{s_tickets}" class="figure-img" alt="All Support Tickets Page">
    <div class="figure-caption">Figure 7.2: Enterprise All Support Tickets View with SaaS metric summary cards and search/status filters</div>
</div>

<div class="figure-container">
    <img src="{s_create_t}" class="figure-img" alt="Create Ticket Modal">
    <div class="figure-caption">Figure 7.3: Create Ticket Modal showing Title, Category, Priority (SLA targets), and Description</div>
</div>

<div class="figure-container">
    <img src="{s_details}" class="figure-img" alt="Ticket Details Modal">
    <div class="figure-caption">Figure 7.4: Ticket Details Modal displaying metadata grid, SLA countdown, resolution status, and threaded comments</div>
</div>

<div class="figure-container">
    <img src="{s_assign}" class="figure-img" alt="Technician Assignment Controls">
    <div class="figure-caption">Figure 7.5: Staff Operations Panel: Assign Technician control with dynamic technician dropdown</div>
</div>

<div class="figure-container">
    <img src="{s_status}" class="figure-img" alt="Status Update and Resolution Section">
    <div class="figure-caption">Figure 7.6: Lifecycle Status Update and Remediation Notes documentation section</div>
</div>

<div class="figure-container">
    <img src="{s_activity}" class="figure-img" alt="Activity History Modal">
    <div class="figure-caption">Figure 7.7: Chronological Activity Audit Log showing actor attribution (`performedBy`) and timestamped events</div>
</div>

<h2>7.3 Critical Code Analysis: Technician Assignment (`TicketService.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>TicketService.java</span><span>Technician Assignment with Proper Actor Attribution</span></div>
    <pre class="code-block">
@Transactional
public Ticket assignTechnician(Long ticketId, Long technicianId, Employee performingActor) {{
    Ticket ticket = getTicketById(ticketId);
    Employee technician = employeeService.getEmployeeById(technicianId);

    // Enforce business constraint: Assignee must hold TECHNICIAN role
    if (technician.getRole() != Role.TECHNICIAN) {{
        throw new IllegalArgumentException("Employee " + technician.getName() + " does not have the TECHNICIAN role.");
    }}

    ticket.setAssignedTechnician(technician);
    ticket.setStatus(TicketStatus.ASSIGNED);
    Ticket savedTicket = ticketRepository.save(ticket);

    // Audit Logging: Record the authenticated administrator as the actor, NOT the technician
    Employee actor = performingActor != null ? performingActor : technician;
    logActivity(
        savedTicket,
        actor,
        ActivityType.TECHNICIAN_ASSIGNED,
        "Ticket assigned to technician " + technician.getName()
    );

    // Dispatch in-app notification to technician
    notificationService.createNotification(
        technician,
        savedTicket,
        "New Ticket Assigned",
        "You have been assigned ticket: " + savedTicket.getTicketNumber(),
        NotificationType.TICKET_ASSIGNED
    );

    return savedTicket;
}}
    </pre>
</div>

<!-- ========================================== -->
<!-- CHAPTER 8: SLA AND OVERDUE TRACKING -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 8 — Priority-Based SLA &amp; Overdue Detection Engine</h1>

<h2>8.1 What is an SLA and Why Does It Matter? (SLA क्या है?)</h2>
<div class="bilingual-box">
    <div class="bilingual-header">Service Level Agreement (SLA) Fundamentals</div>
    <div class="english-part">
        <strong>English:</strong> A Service Level Agreement (SLA) is a contractual resolution commitment between IT support and business operations. In SupportDesk, every ticket has an automated countdown deadline calculated at the exact moment of ticket creation based on its severity priority.
    </div>
    <div class="hindi-part">
        <strong>Hinglish:</strong> SLA (सर्विस लेवल एग्रीमेंट) का मतलब है कि किसी समस्या को हल करने में आईटी टीम अधिकतम कितना समय लेगी। उदाहरण के लिए, यदि कंपनी का मुख्य डेटाबेस या बिलिंग सिस्टम बंद हो गया (CRITICAL), तो उसे 4 घंटे में ठीक करना अनिवार्य है। यदि किसी को नया माउस चाहिए (LOW), तो उसके लिए 48 घंटे का समय होता है।
    </div>
</div>

<h2>8.2 Priority Tiers &amp; Rules (प्राथमिकता श्रेणियाँ)</h2>
<table class="data-table">
    <thead>
        <tr>
            <th>Priority</th>
            <th>SLA Hours</th>
            <th>Config Property</th>
            <th>Criteria &amp; Example</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><span class="badge badge-critical">CRITICAL</span></td>
            <td><strong>4 Hours</strong></td>
            <td>`app.sla.critical-hours=4`</td>
            <td>Production outage, server down, ERP unavailable, security breach.</td>
        </tr>
        <tr>
            <td><span class="badge" style="background:#fed7aa; color:#9a3412;">HIGH</span></td>
            <td><strong>8 Hours</strong></td>
            <td>`app.sla.high-hours=8`</td>
            <td>Department-level blocker without immediate workaround (VPN gateway failure).</td>
        </tr>
        <tr>
            <td><span class="badge" style="background:#fef08a; color:#854d0e;">MEDIUM</span></td>
            <td><strong>24 Hours</strong></td>
            <td>`app.sla.medium-hours=24`</td>
            <td>Single user operational issue (printer malfunction, email sync lag).</td>
        </tr>
        <tr>
            <td><span class="badge badge-emp">LOW</span></td>
            <td><strong>48 Hours</strong></td>
            <td>`app.sla.low-hours=48`</td>
            <td>Non-blocking queries, software upgrade requests, peripheral replacement.</td>
        </tr>
    </tbody>
</table>

<h2>8.3 Genuine UI Screenshot: Overdue SLA Tracking</h2>
<div class="figure-container">
    <img src="{s_overdue}" class="figure-img" alt="Overdue SLA Tracking">
    <div class="figure-caption">Figure 8.1: Overdue SLA Metric Card highlighted in red, indicating tickets exceeding resolution targets</div>
</div>

<h2>8.4 Overdue Detection Query (`TicketRepository.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>TicketRepository.java</span><span>JPQL Overdue Query</span></div>
    <pre class="code-block">
@Query("SELECT t FROM Ticket t WHERE t.slaDeadline &lt; :now AND t.status NOT IN " +
       "(com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED) " +
       "ORDER BY t.slaDeadline ASC")
List&lt;Ticket&gt; findOverdueTickets(@Param("now") LocalDateTime now);
    </pre>
</div>

<p><strong>Crucial Business Logic:</strong> Even if a ticket's deadline timestamp has passed, if the ticket status is `RESOLVED` or `CLOSED`, it is <em>NEVER</em> marked as overdue! This is thoroughly validated in `TicketServiceTests.testResolvedTicketNotOverdue()`.</p>

<!-- ========================================== -->
<!-- CHAPTER 9: SPRING SECURITY, JWT & RBAC -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 9 — Spring Security, JWT &amp; Password Revocation</h1>

<h2>9.1 Authentication Flow &amp; Token Revocation Architecture</h2>
<p>SupportDesk employs stateless JSON Web Tokens signed with HMAC-SHA256. To solve the vulnerability where stolen tokens remain valid after a password reset, SupportDesk embeds an <strong>HMAC-SHA256 Password Fingerprint</strong> (`pwd` claim) in every token:</p>

<div class="figure-container">
    <img src="{d_jwt}" class="figure-img" alt="JWT Flow">
    <div class="figure-caption">Figure 9.1: Stateless JWT Authentication &amp; Instant Password Revocation Architecture</div>
</div>

<h2>9.2 Genuine UI Screenshot: Corporate Sign-In Screen</h2>
<div class="figure-container">
    <img src="{s_login}" class="figure-img" alt="Login Page">
    <div class="figure-caption">Figure 9.2: Corporate Login Page with In-Memory Security indicator and Corporate Branding</div>
</div>

<h2>9.3 Critical Code Analysis: Password Fingerprint (`JwtUtil.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>JwtUtil.java</span><span>Stateless Password Fingerprinting</span></div>
    <pre class="code-block">
public String computePasswordFingerprint(String passwordHash) {{
    if (passwordHash == null || passwordHash.isBlank()) return "";
    try {{
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec =
                new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(passwordHash.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hmac).substring(0, 32);
    }} catch (Exception ex) {{
        return "";
    }}
}}

public boolean isTokenValidForPassword(String token, String currentPasswordHash) {{
    try {{
        DecodedJWT decoded = validateToken(token);
        String tokenPwdClaim = decoded.getClaim("pwd").asString();
        if (tokenPwdClaim != null && !tokenPwdClaim.isBlank()) {{
            String currentFingerprint = computePasswordFingerprint(currentPasswordHash);
            return tokenPwdClaim.equals(currentFingerprint);
        }}
        return true;
    }} catch (JWTVerificationException ex) {{
        return false;
    }}
}}
    </pre>
</div>

<!-- ========================================== -->
<!-- CHAPTER 10: REST API REFERENCE -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 10 — REST API Reference &amp; Request Contracts</h1>

<table class="data-table">
    <thead>
        <tr>
            <th>Method &amp; Endpoint</th>
            <th>Required Role</th>
            <th>Purpose</th>
            <th>Status Codes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>`POST /api/auth/login`</td>
            <td>`permitAll()`</td>
            <td>Validates credentials, returns JWT with role and identity.</td>
            <td>200 OK, 401 Unauthorized</td>
        </tr>
        <tr>
            <td>`GET /api/employees`</td>
            <td>`ADMIN`</td>
            <td>Lists all employees with optional role/dept filters.</td>
            <td>200 OK, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`POST /api/employees`</td>
            <td>`ADMIN`</td>
            <td>Registers a new employee with BCrypt password hashing.</td>
            <td>201 Created, 400 Bad Request, 409 Conflict</td>
        </tr>
        <tr>
            <td>`PUT /api/employees/{{id}}`</td>
            <td>`ADMIN` or Self</td>
            <td>Updates profile; optional password update with hash preservation.</td>
            <td>200 OK, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`PATCH /api/employees/{{id}}/deactivate`</td>
            <td>`ADMIN`</td>
            <td>Soft-deactivates an employee account.</td>
            <td>200 OK, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`GET /api/tickets`</td>
            <td>Authenticated</td>
            <td>Lists tickets (Admins/Techs view all; Employees view only own).</td>
            <td>200 OK, 401 Unauthorized</td>
        </tr>
        <tr>
            <td>`POST /api/tickets`</td>
            <td>Authenticated</td>
            <td>Creates a support ticket; auto-generates ticket # and SLA deadline.</td>
            <td>201 Created, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`GET /api/tickets/{{id}}`</td>
            <td>`ADMIN`, `TECHNICIAN`, or Owner</td>
            <td>Retrieves complete ticket metadata and resolution notes.</td>
            <td>200 OK, 403 Forbidden, 404 Not Found</td>
        </tr>
        <tr>
            <td>`PATCH /api/tickets/{{id}}/assign/{{techId}}`</td>
            <td>`ADMIN`</td>
            <td>Assigns a technician to a ticket; updates status to ASSIGNED.</td>
            <td>200 OK, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`PATCH /api/tickets/{{id}}/status`</td>
            <td>`ADMIN`, `TECHNICIAN`</td>
            <td>Transitions ticket lifecycle status (`IN_PROGRESS`, `CLOSED`).</td>
            <td>200 OK, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`PATCH /api/tickets/{{id}}/resolve`</td>
            <td>`ADMIN`, `TECHNICIAN`</td>
            <td>Records remediation notes and transitions status to `RESOLVED`.</td>
            <td>200 OK, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`GET /api/tickets/overdue`</td>
            <td>`ADMIN`, `TECHNICIAN`</td>
            <td>Queries tickets with past deadlines that are not resolved/closed.</td>
            <td>200 OK, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`POST /api/tickets/{{id}}/comments`</td>
            <td>`ADMIN`, `TECHNICIAN`, or Owner</td>
            <td>Posts a threaded comment or internal response to a ticket.</td>
            <td>201 Created, 400 Bad Request, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`GET /api/tickets/{{id}}/activities`</td>
            <td>`ADMIN`, `TECHNICIAN`, or Owner</td>
            <td>Retrieves the immutable audit log for the ticket.</td>
            <td>200 OK, 403 Forbidden, 404 Not Found</td>
        </tr>
        <tr>
            <td>`GET /api/dashboard/summary`</td>
            <td>`ADMIN`, `TECHNICIAN`</td>
            <td>Returns aggregated status counts and overdue metrics.</td>
            <td>200 OK, 403 Forbidden</td>
        </tr>
        <tr>
            <td>`GET /api/dashboard/technician-workload`</td>
            <td>`ADMIN`, `TECHNICIAN`</td>
            <td>Returns open, in-progress, and resolved counts per technician.</td>
            <td>200 OK, 403 Forbidden</td>
        </tr>
    </tbody>
</table>
"""
