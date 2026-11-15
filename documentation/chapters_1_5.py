def get_chapters_1_to_5_html(assets):
    d_arch = assets['d_arch']
    d_flow = assets['d_flow']
    d_erd = assets['d_erd']

    return f"""
<!-- ========================================== -->
<!-- COVER PAGE -->
<!-- ========================================== -->
<div class="cover-page">
    <div class="cover-header">
        <div class="cover-badge">Complete Technical Interview Guide &amp; Architecture Reference</div>
        <h1 class="cover-title">SupportDesk</h1>
        <div class="cover-subtitle">Enterprise IT Help Desk &amp; Support Ticket Management System</div>
        <div class="cover-hindi">
            <strong>शून्य से उन्नत स्तर तक गाइड (Zero to Advanced Guide):</strong> इस पुस्तक में मेरे वास्तविक प्रोजेक्ट कोड, डेटाबेस स्कीमा, REST API एंडपॉइंट्स, सुरक्षा आर्किटेक्चर और 185 स्वचालित टेस्ट्स के आधार पर आसान द्विभाषी (English + Hinglish) व्याख्या दी गई है।
        </div>
    </div>

    <div class="cover-features">
        <div class="cover-feature-item">
            <strong>Architecture &amp; Core:</strong><br>
            Java 17 LTS • Spring Boot 4 • Spring Data JPA • Hibernate 7 • MySQL 8
        </div>
        <div class="cover-feature-item">
            <strong>Security &amp; Auth:</strong><br>
            Spring Security 6 • Stateless JWT (HMAC-SHA256) • BCrypt Hashing • Token Revocation
        </div>
        <div class="cover-feature-item">
            <strong>Ticketing &amp; SLA:</strong><br>
            4-Tier Dynamic SLA Engine • Automated Overdue Tracking • Technician Assignment
        </div>
        <div class="cover-feature-item">
            <strong>Quality &amp; Testing:</strong><br>
            185 Automated Tests (0 Failures) • MockMvc Integration • Bean Validation • Global Exception Handling
        </div>
    </div>

    <div class="cover-footer">
        <div><strong>Project Path:</strong> D:\\Kamlesh_September_Oct_Pro_Java\\SupportDesk</div>
        <div><strong>Author:</strong> Software Engineer / Java Backend Candidate</div>
        <div><strong>Date:</strong> October 2026</div>
    </div>
</div>

<!-- ========================================== -->
<!-- TABLE OF CONTENTS -->
<!-- ========================================== -->
<h1 class="chapter-title">Table of Contents (विषय-सूची)</h1>
<div class="toc-container">
    <div class="toc-item"><span class="toc-chapter">Chapter 1 — Project Introduction &amp; Problem Solved (परियोजना परिचय)</span><span class="toc-page">03</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 2 — Technology Stack &amp; Justifications (प्रौद्योगिकी स्टैक)</span><span class="toc-page">05</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 3 — Project Folder Structure &amp; Configuration (फ़ोल्डर संरचना)</span><span class="toc-page">06</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 4 — Application Architecture &amp; Layered Flow (एप्लिकेशन आर्किटेक्चर)</span><span class="toc-page">09</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 5 — Database Schema, ERD &amp; SQL Queries (डेटाबेस और संबंध)</span><span class="toc-page">11</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 6 — Employee Administration &amp; Access Boundaries (कर्मचारी प्रबंधन)</span><span class="toc-page">13</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 7 — Ticket Lifecycle &amp; Assignment Workflow (टिकट प्रबंधन)</span><span class="toc-page">17</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 8 — Priority-Based SLA &amp; Overdue Detection Engine (SLA ट्रैकिंग)</span><span class="toc-page">22</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 9 — Spring Security, JWT &amp; Password Revocation (सुरक्षा एवं RBAC)</span><span class="toc-page">24</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 10 — REST API Reference &amp; Request Contracts (REST एंडपॉइंट्स)</span><span class="toc-page">26</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 11 — Frontend SPA Architecture &amp; API Integration (फ़्रंटएंड एकीकरण)</span><span class="toc-page">28</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 12 — Input Validation &amp; Centralized Exception Handling (अपवाद प्रबंधन)</span><span class="toc-page">30</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 13 — Automated Testing &amp; Verification Suite (स्वचालित परीक्षण)</span><span class="toc-page">31</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 14 — Setup, Configuration &amp; Deployment Guide (सेटअप एवं निष्पादन)</span><span class="toc-page">33</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 15 — Production Troubleshooting &amp; Debugging (समस्या निवारण)</span><span class="toc-page">34</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 16 — Technical Limitations &amp; Future Roadmap (सीमाएँ एवं सुधार)</span><span class="toc-page">35</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 17 — Core Technical Interview Questions &amp; Answers (साक्षात्कार प्रश्नोत्तर)</span><span class="toc-page">36</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 18 — Practical Mock Interview: 80 Questions Bank (मॉक इंटरव्यू बैंक)</span><span class="toc-page">40</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 19 — Quick Revision Notes &amp; Last-Day Checklist (त्वरित पुनरावलोकन)</span><span class="toc-page">46</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 20 — Personal 14-Day Practice &amp; Mastery Plan (14-दिवसीय अध्ययन योजना)</span><span class="toc-page">47</span></div>
    <div class="toc-item"><span class="toc-chapter">Appendix &amp; Technical Terms Glossary (शब्दावली एवं फ़ाइल सूची)</span><span class="toc-page">48</span></div>
</div>

<!-- ========================================== -->
<!-- CHAPTER 1: PROJECT INTRODUCTION -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 1 — Project Introduction &amp; Problem Solved</h1>

<h2>1.1 What is SupportDesk? (SupportDesk क्या है?)</h2>
<div class="bilingual-box">
    <div class="bilingual-header">Concept Definition (अवधारणा परिभाषा)</div>
    <div class="english-part">
        <strong>English:</strong> SupportDesk is an enterprise-grade IT Help Desk and Support Ticket Management System built with Java 17 LTS, Spring Boot 4, Spring Security, Spring Data JPA, and MySQL. It enables corporate employees to submit technical support requests, allows IT administrators to assign tickets to specialized technicians, enforces strict priority-based Service Level Agreements (SLAs), and maintains an immutable chronological activity history for transparency and compliance.
    </div>
    <div class="hindi-part">
        <strong>Hinglish:</strong> SupportDesk एक एंटरप्राइज आईटी हेल्प डेस्क और सपोर्ट टिकट प्रबंधन वेब एप्लिकेशन है जिसे जावा 17, स्प्रिंग बूट, स्प्रिंग सिक्योरिटी और MySQL पर बनाया गया है। इसमें कंपनी के कर्मचारी अपनी तकनीकी समस्याएं (जैसे लैपटॉप ख़राब होना, वीपीएन एरर) दर्ज करते हैं। एडमिन इन टिकट्स को तकनीशियनों को सौंपते हैं, और सिस्टम स्वचालित रूप से SLA समय-सीमा की निगरानी करता है ताकि कोई भी समस्या बिना समाधान के अटकी न रहे।
    </div>
</div>

<div class="real-life-box">
    <strong>Real-Life Workplace Example (वास्तविक कार्यस्थल उदाहरण):</strong><br>
    Imagine an office without a help desk: When an employee's laptop screen fails or SAP software crashes, they send an email to the IT group or walk over to IT desk. The email gets lost in someone's inbox, no technician takes ownership, the manager has no visibility, and three days later the employee is still blocked. With <strong>SupportDesk</strong>, the employee logs in, creates a ticket (`TCK-20261014-E02BCC`), selects "CRITICAL" priority, and the system immediately calculates a 4-hour SLA deadline. The admin assigns it to a technician, the technician resolves it, notes the fix, and the entire audit history is preserved in MySQL.
</div>

<h2>1.2 Who Uses SupportDesk? (इसके उपयोगकर्ता कौन हैं?)</h2>
<p>The platform defines three strictly segregated user personas governed by Spring Security Role-Based Access Control (RBAC):</p>
<table class="data-table">
    <thead>
        <tr>
            <th>Role</th>
            <th>Primary Responsibilities</th>
            <th>System Permissions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><span class="badge badge-emp">EMPLOYEE</span></td>
            <td>General corporate staff reporting hardware, software, network, or access issues.</td>
            <td>Can create tickets for themselves, view only their own ticket history, and post comments on their tickets. Forbidden from viewing other employees' tickets or accessing admin dashboards.</td>
        </tr>
        <tr>
            <td><span class="badge badge-tech">TECHNICIAN</span></td>
            <td>IT engineers and hardware/software specialists resolving assigned tasks.</td>
            <td>Can view all assigned tickets, update ticket lifecycle statuses (`IN_PROGRESS`, `RESOLVED`), submit resolution notes, post technical comments, and access the technician workload dashboard.</td>
        </tr>
        <tr>
            <td><span class="badge badge-admin">ADMIN</span></td>
            <td>IT Managers, Operations Leads, and System Administrators.</td>
            <td>Complete administrative authority: register and manage employee accounts, assign tickets to technicians, view executive analytics dashboards, generate date-range reports, and monitor SLA breaches.</td>
        </tr>
    </tbody>
</table>

<h2>1.3 How to Pitch This Project in Interviews (इंटरव्यू में प्रोजेक्ट कैसे समझाएं?)</h2>
<p>Interviewers frequently ask: <em>"Can you introduce your project in 30 seconds or 2 minutes?"</em> Use these proven scripts:</p>

<div class="qa-card">
    <div class="qa-question">
        <span>The 30-Second Elevator Pitch (30 सेकंड का त्वरित परिचय)</span>
        <span class="badge badge-admin">Interview Ready</span>
    </div>
    <div class="qa-answer-en">
        "I built <strong>SupportDesk</strong>, a full-stack IT service management platform engineered with Java 17 and Spring Boot. It streamlines workplace technical support by replacing chaotic email chains with structured ticket lifecycles, automated 4-tier SLA tracking, and role-based access control. The backend features stateless JWT authentication with token revocation on password change, clean layered architecture, and an automated test suite of 185 tests with zero failures."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "मैंने SupportDesk बनाया है, जो जावा 17 और स्प्रिंग बूट पर आधारित एक आईटी हेल्प डेस्क सिस्टम है। यह ऑफिस में ईमेल की अराजकता को ख़त्म करके टिकट्स का सुव्यवस्थित जीवनचक्र, 4-स्तरीय SLA ट्रैकिंग और रोल-बेस्ड एक्सेस कंट्रोल (RBAC) प्रदान करता है। सुरक्षा के लिए स्टेटलेस JWT और पासवर्ड बदलने पर टोकन इनवैलिडेशन लागू किया गया है, और इसे 185 ऑटोमेटेड टेस्ट्स के ज़रिए प्रमाणित किया गया है।"
    </div>
</div>

<div class="qa-card">
    <div class="qa-question">
        <span>The 3-Minute Comprehensive Technical Overview (3 मिनट का गहन तकनीकी विवरण)</span>
        <span class="badge badge-tech">Architecture Focused</span>
    </div>
    <div class="qa-answer-en">
        "SupportDesk solves the core operational challenge of tracking enterprise IT requests against rigid Service Level Agreements. On the backend, I implemented a Spring Boot 4 REST API utilizing Spring Data JPA and Hibernate for persistence against MySQL. Security is enforced through Spring Security with stateless JWTs. To prevent stolen token persistence, I devised an HMAC-SHA256 password fingerprinting mechanism in `JwtUtil` that immediately revokes tokens if the employee's password changes, requiring zero database schema alterations.
        <br><br>
        The business core in `TicketService` automatically calculates resolution deadlines across four priority tiers—from 4 hours for Critical down to 48 hours for Low priority. It tracks overdue status dynamically using index-optimized JPQL queries. Every state transition triggers an audit log in `ticket_activities` and dispatches in-app notifications.
        <br><br>
        On the client side, I built a responsive vanilla JavaScript Single-Page Application that manages authentication tokens purely in closure memory to protect against XSS and persistent storage theft. The application handles exceptions globally with `@RestControllerAdvice`, sanitizing error outputs to prevent SQL or stack trace leakage. I verified the entire application using MockMvc and JUnit 5 across 185 unit and integration tests."
    </div>
    <div class="qa-answer-hi">
        <strong>Hinglish Explanation:</strong> "इस विवरण में आप आर्किटेक्चर, सिक्योरिटी, बिजनेस लॉजिक और टेस्टिंग चारों मुख्य स्तंभों को कवर करते हैं। इंटरव्यूअर को स्पष्ट समझ आता है कि आपने सिर्फ ट्यूटोरियल फॉलो नहीं किया, बल्कि वास्तविक प्रोडक्शन सुरक्षा (जैसे HMAC फिंगरप्रिंट से टोकन अमान्य करना, SQL इंजेक्शन से बचाव, XSS से सुरक्षा के लिए इन-मेमोरी टोकन) और डेटाबेस ऑप्टिमाइज़ेशन को गहराई से समझा है।"
    </div>
</div>

<!-- ========================================== -->
<!-- CHAPTER 2: TECHNOLOGY STACK -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 2 — Technology Stack &amp; Justifications</h1>

<h2>2.1 The Verified Technology Ecosystem (प्रयुक्त तकनीकों का विश्लेषण)</h2>
<p>Every technology in SupportDesk was chosen intentionally to solve specific enterprise requirements:</p>

<table class="data-table">
    <thead>
        <tr>
            <th>Technology &amp; Version</th>
            <th>Role in SupportDesk</th>
            <th>Why We Chose It (Engineering Justification)</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>Java 17 LTS</strong></td>
            <td>Core Programming Language</td>
            <td>Long-Term Support release offering enhanced JVM performance, Text Blocks for multiline JSON test fixtures, records for immutable DTOs, and pattern matching.</td>
        </tr>
        <tr>
            <td><strong>Spring Boot 4.1.1</strong></td>
            <td>Backend Web Framework</td>
            <td>Rapid enterprise micro-framework providing embedded Apache Tomcat 11 servlet container, opinionated auto-configuration, and unified dependency management.</td>
        </tr>
        <tr>
            <td><strong>Spring Security 6</strong></td>
            <td>Security &amp; Authorization</td>
            <td>Filter-chain architecture that intercepts every HTTP request, enforces URL-based authorization, validates credentials via BCrypt, and prevents cross-site request attacks.</td>
        </tr>
        <tr>
            <td><strong>JSON Web Tokens (JWT)</strong><br>`com.auth0:java-jwt`</td>
            <td>Stateless Authentication</td>
            <td>Allows stateless horizontal scaling without server-side HTTP session storage. Encodes user ID, email, role, and a cryptographic password fingerprint directly into claims.</td>
        </tr>
        <tr>
            <td><strong>Spring Data JPA &amp; Hibernate 7</strong></td>
            <td>Object-Relational Mapping (ORM)</td>
            <td>Eliminates boilerplate JDBC code, manages entity lifecycles, guarantees database portability, and facilitates complex JPQL aggregations for dashboard metrics.</td>
        </tr>
        <tr>
            <td><strong>MySQL 8.0 Relational DB</strong></td>
            <td>Persistent Relational Storage</td>
            <td>ACID-compliant storage engine with strict foreign key constraints, transactional consistency for ticket lifecycle events, and index-optimized query execution.</td>
        </tr>
        <tr>
            <td><strong>Jakarta Bean Validation</strong><br>`hibernate-validator`</td>
            <td>Input Validation</td>
            <td>Standardized declarative validation annotations (`@NotBlank`, `@Size`, `@Email`, `@Pattern`) that intercept invalid payloads before hitting service logic.</td>
        </tr>
        <tr>
            <td><strong>Vanilla JavaScript (ES6+)</strong></td>
            <td>Frontend Client Architecture</td>
            <td>Ultra-lightweight Single-Page Application without heavy node_modules dependencies. Manages sessions strictly in memory to eliminate cookie/storage vulnerabilities.</td>
        </tr>
        <tr>
            <td><strong>JUnit 5 &amp; Spring Boot Test</strong></td>
            <td>Automated Test Framework</td>
            <td>MockMvc integration for end-to-end HTTP boundary testing, transactional rollbacks for data isolation, and comprehensive unit tests totaling 185 tests.</td>
        </tr>
    </tbody>
</table>

<div class="interview-tip-box">
    <strong>Interview Tip (साक्षात्कार युक्ति):</strong><br>
    When an interviewer asks: <em>"Why didn't you use React or Angular for the frontend?"</em><br>
    <strong>Best Answer:</strong> "The primary architectural focus of this project was engineering a robust, secure, and production-ready Spring Boot backend with fine-grained RBAC, token revocation, and JPA optimizations. By utilizing a clean Vanilla JavaScript SPA, I eliminated complex build tools and client-side dependencies while demonstrating mastery of core web standards (Fetch API, DOM manipulation, in-memory security patterns)."
</div>

<!-- ========================================== -->
<!-- CHAPTER 3: FOLDER STRUCTURE & ENTRY POINT -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 3 — Project Folder Structure &amp; Configuration</h1>

<h2>3.1 Complete Package Layout (पैकेज संरचना का विवरण)</h2>
<p>SupportDesk adheres to Spring Boot's recommended domain-by-layer convention under `com.supportdesk`:</p>

<div class="code-container">
    <div class="code-header"><span>SupportDesk Source Tree</span><span>Filesystem Layout</span></div>
    <pre class="code-block">
D:\Kamlesh_September_Oct_Pro_Java\SupportDesk
├── pom.xml                                    # Maven Build Manifest &amp; Dependencies
├── mvnw.cmd / mvnw                            # Maven Wrapper Scripts
├── src
│   ├── main
│   │   ├── java/com/supportdesk
│   │   │   ├── SupportDeskApplication.java    # Application Entry Point (@SpringBootApplication)
│   │   │   ├── config/                        # Spring Configuration Classes
│   │   │   │   └── DevelopmentDataInitializer.java # Seeds test accounts in dev profile
│   │   │   ├── controller/                    # REST Web Layer (@RestController)
│   │   │   │   ├── AuthController.java        # Login &amp; Token Issuance (/api/auth)
│   │   │   │   ├── EmployeeController.java    # Employee Management (/api/employees)
│   │   │   │   ├── TicketController.java      # Ticket Lifecycle (/api/tickets)
│   │   │   │   ├── DashboardController.java   # Metrics &amp; Workload (/api/dashboard)
│   │   │   │   ├── NotificationController.java# In-App Notifications (/api/notifications)
│   │   │   │   ├── GlobalExceptionHandler.java# Centralized @RestControllerAdvice
│   │   │   │   └── [Request/Response DTOs]    # CreateTicketRequest, LoginResponse, etc.
│   │   │   ├── entity/                        # JPA Entities (@Entity, @Table)
│   │   │   │   ├── Employee.java              # Employee account with role and credentials
│   │   │   │   ├── Ticket.java                # Ticket entity with priority and SLA
│   │   │   │   ├── TicketComment.java         # Threaded ticket comments
│   │   │   │   ├── TicketActivity.java        # Immutable audit log entries
│   │   │   │   ├── Notification.java          # In-app notification alerts
│   │   │   │   ├── Role.java                  # ENUM: ADMIN, TECHNICIAN, EMPLOYEE
│   │   │   │   ├── TicketStatus.java          # ENUM: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │   ├── TicketPriority.java        # ENUM: CRITICAL, HIGH, MEDIUM, LOW
│   │   │   │   └── ActivityType.java          # ENUM: TICKET_CREATED, TECHNICIAN_ASSIGNED, etc.
│   │   │   ├── repository/                    # Spring Data JPA Interfaces (@Repository)
│   │   │   │   ├── EmployeeRepository.java
│   │   │   │   ├── TicketRepository.java
│   │   │   │   ├── TicketCommentRepository.java
│   │   │   │   ├── TicketActivityRepository.java
│   │   │   │   └── NotificationRepository.java
│   │   │   ├── security/                      # Spring Security &amp; JWT Engine
│   │   │   │   ├── SecurityConfig.java        # Filter chain, CORS, permitAll vs authenticated
│   │   │   │   ├── JwtUtil.java               # HMAC-SHA256 signing, validation &amp; fingerprinting
│   │   │   │   ├── JwtAuthenticationFilter.java# Intercepts Bearer tokens on each request
│   │   │   │   ├── CustomUserDetailsService.java # Loads UserPrincipal by email from MySQL
│   │   │   │   ├── SecurityUtils.java         # Helper to get current authenticated user
│   │   │   │   ├── CustomAuthenticationEntryPoint.java # Emits clean 401 Unauthorized JSON
│   │   │   │   └── CustomAccessDeniedHandler.java # Emits clean 403 Forbidden JSON
│   │   │   └── service/                       # Business Logic Layer (@Service)
│   │   │       ├── AuthService.java           # Login credential check &amp; token generation
│   │   │       ├── EmployeeService.java       # Employee CRUD, password hash preservation
│   │   │       ├── TicketService.java         # Ticket creation, SLA calculation, assignment
│   │   │       ├── DashboardService.java      # Workload aggregations &amp; summary counts
│   │   │       └── NotificationService.java   # Dispatches activity notifications
│   │   └── resources
│   │       ├── application.properties         # Baseline defaults (port 8082, SLA hours)
│   │       ├── application-dev.properties     # Development config (MySQL localhost, seed data)
│   │       ├── application-prod.properties    # Production config (Env vars, ddl-auto=validate)
│   │       └── static/                        # Single-Page Application Client
│   │           ├── index.html                 # Main SPA dashboard &amp; ticket workspace
│   │           ├── login.html                 # Dedicated corporate sign-in screen
│   │           ├── css/styles.css             # Navy/Blue responsive design system
│   │           └── js/                        # ES6 client modules (auth.js, api.js, app.js)
│   └── test/java/com/supportdesk              # 185 Automated Unit &amp; Integration Tests
</pre>
</div>

<h2>3.2 Application Entry Point (`SupportDeskApplication.java`)</h2>
<div class="code-container">
    <div class="code-header"><span>SupportDeskApplication.java</span><span>Java 17 / Spring Boot</span></div>
    <pre class="code-block">
package com.supportdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SupportDeskApplication {{
    public static void main(String[] args) {{
        SpringApplication.run(SupportDeskApplication.class, args);
    }}
}}
    </pre>
</div>

<div class="bilingual-box">
    <div class="bilingual-header">How Spring Boot Starts (@SpringBootApplication का महत्व)</div>
    <div class="english-part">
        <strong>English:</strong> `@SpringBootApplication` is a convenience meta-annotation that bundles three vital annotations:
        1. `@Configuration`: Marks the class as a source of bean definitions for the Spring Application Context.
        2. `@EnableAutoConfiguration`: Tells Spring Boot to automatically configure beans based on classpath dependencies (e.g., configuring HikariCP DataSource because MySQL driver is present).
        3. `@ComponentScan`: Scans `com.supportdesk` and all sub-packages for `@Component`, `@Service`, `@Repository`, and `@RestController` classes to instantiate them as Spring managed beans.
    </div>
    <div class="hindi-part">
        <strong>Hinglish:</strong> `@SpringBootApplication` तीन प्रमुख एनोटेशन्स का समूह है: `@Configuration` (बीन कॉन्फ़िगरेशन के लिए), `@EnableAutoConfiguration` (क्लासपाथ की जार फाइल्स देखकर स्वचालित सेटअप के लिए), और `@ComponentScan` (यह `com.supportdesk` के अंदर की सभी क्लासेज को स्कैन करके उनके ऑब्जेक्ट्स यानी स्प्रिंग बीन्स बनाता है)। जब `main` मेथड चलती है, तो यह एम्बेडेड टॉमकैट सर्वर शुरू करती है और एप्लिकेशन पोर्ट 8082 पर लाइव हो जाती है।
    </div>
</div>

<!-- ========================================== -->
<!-- CHAPTER 4: APPLICATION ARCHITECTURE -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 4 — Application Architecture &amp; Layered Flow</h1>

<h2>4.1 Layered Architecture Overview (स्तरीय वास्तुकला)</h2>
<p>SupportDesk enforces a strict <strong>Separation of Concerns (SoC)</strong> through a 4-tier layered architecture:</p>

<div class="figure-container">
    <img src="{d_arch}" class="figure-img" alt="SupportDesk Layered Architecture">
    <div class="figure-caption">Figure 4.1: SupportDesk 4-Tier Layered Architecture Diagram</div>
</div>

<h2>4.2 End-to-End Request Execution Flow (अनुरोध निष्पादन प्रवाह)</h2>
<p>When an employee clicks "Create Ticket", the request traverses through every layer as visualized below:</p>

<div class="figure-container">
    <img src="{d_flow}" class="figure-img" alt="Request Flow">
    <div class="figure-caption">Figure 4.2: Step-by-Step Request Lifecycle from Browser to MySQL Database</div>
</div>

<table class="line-explain-table">
    <thead>
        <tr>
            <th>Layer</th>
            <th>Primary Class / Component</th>
            <th>Responsibility in the Flow (प्रवाह में ज़िम्मेदारी)</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>1. Client / Browser</strong></td>
            <td>`api.js` (Fetch API)</td>
            <td>Submits HTTP POST `/api/tickets` with JSON body and `Authorization: Bearer &lt;JWT&gt;`.</td>
        </tr>
        <tr>
            <td><strong>2. Security Filter</strong></td>
            <td>`JwtAuthenticationFilter.java`</td>
            <td>Extracts token, validates cryptographic signature, checks expiration, verifies password fingerprint (`pwd`), and populates `SecurityContext`.</td>
        </tr>
        <tr>
            <td><strong>3. REST Controller</strong></td>
            <td>`TicketController.java`</td>
            <td>Validates input payload via `@Valid @RequestBody`, checks identity spoofing (`SecurityUtils`), and delegates to `TicketService`.</td>
        </tr>
        <tr>
            <td><strong>4. Service Layer</strong></td>
            <td>`TicketService.java`</td>
            <td>Begins transaction (`@Transactional`), computes SLA deadline via `calculateSlaDeadline()`, generates unique ticket number, and calls repositories.</td>
        </tr>
        <tr>
            <td><strong>5. Data Access Layer</strong></td>
            <td>`TicketRepository.java`</td>
            <td>Translates Java entity operations into parameterized SQL `INSERT` statements executed by Hibernate ORM engine.</td>
        </tr>
        <tr>
            <td><strong>6. Database Engine</strong></td>
            <td>MySQL 8.0 (`supportdesk_db`)</td>
            <td>Executes atomic SQL statements, assigns auto-incremented primary key, and commits transaction.</td>
        </tr>
    </tbody>
</table>

<!-- ========================================== -->
<!-- CHAPTER 5: DATABASE SCHEMA & SQL -->
<!-- ========================================== -->
<h1 class="chapter-title">Chapter 5 — Database Schema, ERD &amp; SQL Queries</h1>

<h2>5.1 Relational Entity-Relationship Diagram (ERD)</h2>
<p>SupportDesk's database structure comprises five core tables linked via relational foreign key constraints:</p>

<div class="figure-container">
    <img src="{d_erd}" class="figure-img" alt="SupportDesk ERD">
    <div class="figure-caption">Figure 5.1: Relational Entity-Relationship Diagram (ERD)</div>
</div>

<h2>5.2 Relational Tables Breakdown (टेबल्स का विस्तृत विवरण)</h2>
<table class="data-table">
    <thead>
        <tr>
            <th>Table Name</th>
            <th>Primary Key</th>
            <th>Foreign Keys</th>
            <th>Business Purpose (व्यावसायिक उद्देश्य)</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>`employees`</td>
            <td>`id` (BIGINT AUTO)</td>
            <td>None</td>
            <td>Stores user credentials (BCrypt password hash), corporate email (Unique), department, active status, and RBAC role (`ADMIN`, `TECHNICIAN`, `EMPLOYEE`).</td>
        </tr>
        <tr>
            <td>`tickets`</td>
            <td>`id` (BIGINT AUTO)</td>
            <td>`employee_id` &rarr; `employees(id)`<br>`assigned_technician_id` &rarr; `employees(id)`</td>
            <td>Stores ticket metadata, unique ticket number (`TCK-YYYYMMDD-XXXX`), status, priority, category, resolution notes, and SLA deadline timestamp.</td>
        </tr>
        <tr>
            <td>`ticket_comments`</td>
            <td>`id` (BIGINT AUTO)</td>
            <td>`ticket_id` &rarr; `tickets(id)`<br>`employee_id` &rarr; `employees(id)`</td>
            <td>Stores threaded discussions, internal technician notes, and communications related to a specific ticket.</td>
        </tr>
        <tr>
            <td>`ticket_activities`</td>
            <td>`id` (BIGINT AUTO)</td>
            <td>`ticket_id` &rarr; `tickets(id)`<br>`employee_id` &rarr; `employees(id)`</td>
            <td>Maintains an append-only, tamper-proof audit history recording every status change, technician assignment, and resolution event.</td>
        </tr>
        <tr>
            <td>`notifications`</td>
            <td>`id` (BIGINT AUTO)</td>
            <td>`recipient_id` &rarr; `employees(id)`<br>`ticket_id` &rarr; `tickets(id)`</td>
            <td>Stores in-app alerts dispatched to users when their tickets are assigned, commented upon, or updated.</td>
        </tr>
    </tbody>
</table>

<h2>5.3 Why Entities and DTOs Are Strictly Separated (Entity बनाम DTO)</h2>
<div class="bilingual-box">
    <div class="bilingual-header">Architectural Principle: Entity vs DTO</div>
    <div class="english-part">
        <strong>English:</strong> In SupportDesk, JPA Entities (`Employee.java`, `Ticket.java`) represent the exact relational database tables. They are NEVER directly exposed as API request or response bodies. Instead, we use dedicated Data Transfer Objects (DTOs) like `CreateTicketRequest.java`, `UpdateEmployeeRequest.java`, and `LoginResponse.java`.
        <br><br>
        <strong>Key Reasons:</strong>
        1. <strong>Security:</strong> Prevents Mass Assignment attacks (e.g., an employee sending `"role": "ADMIN"` in a profile update).
        2. <strong>Credential Protection:</strong> Ensures passwords and BCrypt hashes are NEVER serialized into JSON output.
        3. <strong>Decoupling:</strong> Allows database schemas to evolve independently without breaking frontend API contracts.
    </div>
    <div class="hindi-part">
        <strong>Hinglish:</strong> एंटिटी (Entity) और DTO को अलग रखना सॉफ्टवेयर आर्किटेक्चर का एक महत्वपूर्ण नियम है। एंटिटी सीधे डेटाबेस की टेबल से जुड़ी होती है। अगर हम एंटिटी को सीधे API में भेजेंगे, तो यूजर का पासवर्ड हैश गलती से JSON में लीक हो सकता है। DTO (Data Transfer Object) का उपयोग करके हम केवल वही डेटा क्लाइंट को भेजते हैं जिसकी उसे ज़रूरत होती है, जिससे हमारा एप्लिकेशन सुरक्षित और व्यवस्थित रहता है।
    </div>
</div>
"""
