import os

DIAGRAMS_DIR = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\diagrams"
os.makedirs(DIAGRAMS_DIR, exist_ok=True)

# 1. Layered Application Architecture (Clean flat vector styling, zero gradient/filter bugs)
arch_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 850 500" width="850" height="500">
  <rect width="850" height="500" fill="#f8fafc" rx="8" stroke="#cbd5e1" stroke-width="1"/>
  <text x="425" y="35" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-size="20" font-weight="bold" fill="#0f172a">SupportDesk Layered Architecture (स्तरीय वास्तुकला)</text>

  <!-- Presentation Layer -->
  <g>
    <rect x="50" y="65" width="750" height="70" rx="8" fill="#ffffff" stroke="#2563eb" stroke-width="2"/>
    <rect x="50" y="65" width="180" height="70" rx="8" fill="#1e40af"/>
    <text x="140" y="98" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#ffffff">Client / Presentation</text>
    <text x="140" y="118" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#bfdbfe">प्रस्तुति परत (SPA)</text>
    <text x="480" y="96" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="600" fill="#1e293b">Vanilla JS (ES6+) SPA • Fetch API Client (`api.js`) • HTML5 / Modern Responsive CSS</text>
    <text x="480" y="118" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#64748b">In-Memory JWT Auth Service • Dynamic DOM Rendering • Modals &amp; Real-time Event Handlers</text>
  </g>

  <!-- Arrow -->
  <path d="M 425 135 L 425 155" stroke="#2563eb" stroke-width="2.5" fill="none"/>

  <!-- Security & Controller Layer -->
  <g>
    <rect x="50" y="155" width="750" height="80" rx="8" fill="#ffffff" stroke="#7c3aed" stroke-width="2"/>
    <rect x="50" y="155" width="180" height="80" rx="8" fill="#6d28d9"/>
    <text x="140" y="190" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#ffffff">REST Controllers &amp;</text>
    <text x="140" y="210" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#ffffff">Spring Security</text>
    <text x="480" y="185" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="600" fill="#1e293b">AuthController • TicketController • EmployeeController • DashboardController</text>
    <text x="480" y="205" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11.5" fill="#475569">JwtAuthenticationFilter (HMAC-SHA256 Fingerprint) • CustomAccessDeniedHandler • GlobalExceptionHandler</text>
    <text x="480" y="223" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#7c3aed">Jakarta Bean Validation (@Valid, @NotBlank, @Size) • CORS Configuration</text>
  </g>

  <!-- Arrow -->
  <path d="M 425 235 L 425 255" stroke="#7c3aed" stroke-width="2.5" fill="none"/>

  <!-- Service Layer -->
  <g>
    <rect x="50" y="255" width="750" height="75" rx="8" fill="#ffffff" stroke="#059669" stroke-width="2"/>
    <rect x="50" y="255" width="180" height="75" rx="8" fill="#047857"/>
    <text x="140" y="290" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#ffffff">Business Logic Layer</text>
    <text x="140" y="310" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#a7f3d0">सेवा परत (Services)</text>
    <text x="480" y="285" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="600" fill="#1e293b">TicketService • EmployeeService • AuthService • DashboardService • NotificationService</text>
    <text x="480" y="305" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#475569">Transaction Boundaries (@Transactional) • SLA Calculation Engine • State Machine Transitions</text>
    <text x="480" y="322" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#047857">Audit Activity Logger • Password Hashing (BCrypt) • Admin Self-Lockout Protections</text>
  </g>

  <!-- Arrow -->
  <path d="M 425 330 L 425 350" stroke="#059669" stroke-width="2.5" fill="none"/>

  <!-- Repository Layer -->
  <g>
    <rect x="50" y="350" width="750" height="65" rx="8" fill="#ffffff" stroke="#ea580c" stroke-width="2"/>
    <rect x="50" y="350" width="180" height="65" rx="8" fill="#c2410c"/>
    <text x="140" y="380" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#ffffff">Data Access (Spring Data)</text>
    <text x="140" y="400" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#fed7aa">डेटा एक्सेस परत (JPA)</text>
    <text x="480" y="378" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="600" fill="#1e293b">TicketRepository • EmployeeRepository • TicketCommentRepository • TicketActivityRepository</text>
    <text x="480" y="398" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#64748b">Spring Data JPA • Derived Query Methods • Custom JPQL Queries • Hibernate ORM Engine</text>
  </g>

  <!-- Arrow -->
  <path d="M 425 415 L 425 435" stroke="#ea580c" stroke-width="2.5" fill="none"/>

  <!-- Database Layer -->
  <g>
    <rect x="250" y="435" width="350" height="50" rx="25" fill="#0f172a" stroke="#334155" stroke-width="2"/>
    <text x="425" y="465" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="bold" fill="#38bdf8">MySQL Relational Database (`supportdesk_db`)</text>
  </g>
</svg>"""

with open(os.path.join(DIAGRAMS_DIR, "01_layered_architecture.svg"), "w", encoding="utf-8") as f:
    f.write(arch_svg)

# 2. Ticket Lifecycle State Machine
lifecycle_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 850 320" width="850" height="320">
  <defs>
    <marker id="arr" viewBox="0 0 10 10" refX="7" refY="5" markerWidth="6" markerHeight="6" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="850" height="320" fill="#ffffff" rx="8" stroke="#cbd5e1" stroke-width="1"/>
  <text x="425" y="32" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-size="18" font-weight="bold" fill="#0f172a">SupportDesk Ticket Lifecycle State Machine (टिकट जीवनचक्र)</text>

  <!-- OPEN -->
  <g>
    <rect x="40" y="80" width="120" height="70" rx="10" fill="#dbeafe" stroke="#2563eb" stroke-width="2"/>
    <text x="100" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#1e40af">OPEN</text>
    <text x="100" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#3b82f6">टिकट दर्ज हुआ</text>
  </g>

  <!-- Arrow -->
  <path d="M 160 115 L 200 115" stroke="#475569" stroke-width="2" marker-end="url(#arr)"/>
  <text x="180" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Assign</text>

  <!-- ASSIGNED -->
  <g>
    <rect x="200" y="80" width="130" height="70" rx="10" fill="#fef3c7" stroke="#d97706" stroke-width="2"/>
    <text x="265" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#92400e">ASSIGNED</text>
    <text x="265" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#b45309">तकनीशियन नियुक्त</text>
  </g>

  <!-- Arrow -->
  <path d="M 330 115 L 370 115" stroke="#475569" stroke-width="2" marker-end="url(#arr)"/>
  <text x="350" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Start Work</text>

  <!-- IN_PROGRESS -->
  <g>
    <rect x="370" y="80" width="140" height="70" rx="10" fill="#ede9fe" stroke="#7c3aed" stroke-width="2"/>
    <text x="440" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="bold" fill="#5b21b6">IN_PROGRESS</text>
    <text x="440" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#7c3aed">कार्य प्रगति पर है</text>
  </g>

  <!-- Arrow -->
  <path d="M 510 115 L 550 115" stroke="#475569" stroke-width="2" marker-end="url(#arr)"/>
  <text x="530" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Remediate</text>

  <!-- RESOLVED -->
  <g>
    <rect x="550" y="80" width="130" height="70" rx="10" fill="#dcfce7" stroke="#16a34a" stroke-width="2"/>
    <text x="615" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#166534">RESOLVED</text>
    <text x="615" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#15803d">समस्या हल हुई</text>
  </g>

  <!-- Arrow -->
  <path d="M 680 115 L 720 115" stroke="#475569" stroke-width="2" marker-end="url(#arr)"/>
  <text x="700" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Confirm</text>

  <!-- CLOSED -->
  <g>
    <rect x="720" y="80" width="100" height="70" rx="10" fill="#f1f5f9" stroke="#64748b" stroke-width="2"/>
    <text x="770" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="14" font-weight="bold" fill="#334155">CLOSED</text>
    <text x="770" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#64748b">टिकट बंद</text>
  </g>

  <!-- SLA Timeline Box -->
  <g>
    <rect x="40" y="195" width="780" height="95" rx="8" fill="#f8fafc" stroke="#cbd5e1" stroke-width="1.5"/>
    <text x="60" y="222" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#0f172a">Priority-Based SLA Resolution Targets (प्राथमिकता अनुसार SLA सीमाएँ):</text>
    <text x="60" y="250" font-family="'Segoe UI', sans-serif" font-size="11.5" fill="#b91c1c"><tspan font-weight="bold">CRITICAL: 4 Hours</tspan> — Severely blocking operations (e.g., ERP/database outage)</text>
    <text x="60" y="275" font-family="'Segoe UI', sans-serif" font-size="11.5" fill="#c2410c"><tspan font-weight="bold">HIGH: 8 Hours</tspan> — Significant impact with no workaround (e.g., VPN failure)</text>
    <text x="440" y="250" font-family="'Segoe UI', sans-serif" font-size="11.5" fill="#d97706"><tspan font-weight="bold">MEDIUM: 24 Hours</tspan> — Standard operational issue (e.g., mouse/keyboard issue)</text>
    <text x="440" y="275" font-family="'Segoe UI', sans-serif" font-size="11.5" fill="#2563eb"><tspan font-weight="bold">LOW: 48 Hours</tspan> — Minor inquiry or non-blocking request</text>
  </g>
</svg>"""

with open(os.path.join(DIAGRAMS_DIR, "02_ticket_lifecycle.svg"), "w", encoding="utf-8") as f:
    f.write(lifecycle_svg)

# 3. JWT Authentication & Password Fingerprint Flow
jwt_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 850 420" width="850" height="420">
  <defs>
    <marker id="arr2" viewBox="0 0 10 10" refX="7" refY="5" markerWidth="6" markerHeight="6" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#2563eb"/>
    </marker>
  </defs>
  <rect width="850" height="420" fill="#ffffff" rx="8" stroke="#cbd5e1" stroke-width="1"/>
  <text x="425" y="30" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-size="18" font-weight="bold" fill="#0f172a">Stateless JWT Authentication &amp; Password Revocation Flow (प्रमाणीकरण प्रवाह)</text>

  <!-- Login Request -->
  <g>
    <rect x="40" y="65" width="230" height="90" rx="8" fill="#f0f9ff" stroke="#0284c7" stroke-width="2"/>
    <text x="155" y="90" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#0369a1">1. User Login (लॉगिन)</text>
    <text x="155" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" fill="#334155">POST /api/auth/login</text>
    <text x="155" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#64748b">{ email, password }</text>
  </g>

  <!-- Arrow -->
  <path d="M 270 110 L 320 110" stroke="#2563eb" stroke-width="2" marker-end="url(#arr2)"/>

  <!-- Server Authentication -->
  <g>
    <rect x="320" y="65" width="250" height="90" rx="8" fill="#f5f3ff" stroke="#7c3aed" stroke-width="2"/>
    <text x="445" y="88" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#6d28d9">2. AuthService &amp; JwtUtil</text>
    <text x="445" y="108" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#475569">BCrypt verifies raw password against DB hash</text>
    <text x="445" y="126" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#475569">Calculates HMAC-SHA256 fingerprint (`pwd`)</text>
    <text x="445" y="143" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#7c3aed">Generates signed JWT (Claims: sub, id, role, pwd)</text>
  </g>

  <!-- Arrow -->
  <path d="M 570 110 L 620 110" stroke="#2563eb" stroke-width="2" marker-end="url(#arr2)"/>

  <!-- JWT Response -->
  <g>
    <rect x="620" y="65" width="200" height="90" rx="8" fill="#ecfdf5" stroke="#10b981" stroke-width="2"/>
    <text x="720" y="90" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#047857">3. Secure In-Memory Token</text>
    <text x="720" y="112" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#334155">Client stores in JS closure</text>
    <text x="720" y="132" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10" fill="#059669">Never in cookies/storage</text>
  </g>

  <!-- Filter Verification Flow -->
  <g>
    <rect x="40" y="195" width="780" height="205" rx="8" fill="#f8fafc" stroke="#94a3b8" stroke-width="1.5"/>
    <text x="60" y="222" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="bold" fill="#0f172a">Subsequent Request Verification (`JwtAuthenticationFilter`):</text>
    
    <rect x="60" y="240" width="220" height="65" rx="6" fill="#ffffff" stroke="#cbd5e1"/>
    <text x="170" y="263" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#1e293b">A. Request with Header</text>
    <text x="170" y="283" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10" fill="#64748b">Authorization: Bearer &lt;JWT&gt;</text>

    <text x="300" y="278" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="18" fill="#2563eb">→</text>

    <rect x="320" y="240" width="240" height="65" rx="6" fill="#ffffff" stroke="#cbd5e1"/>
    <text x="440" y="260" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#1e293b">B. Filter Verifications</text>
    <text x="440" y="278" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9.5" fill="#475569">1. Valid cryptographic signature?</text>
    <text x="440" y="294" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9.5" fill="#475569">2. Expiration timestamp unexpired?</text>

    <text x="580" y="278" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="18" fill="#2563eb">→</text>

    <rect x="600" y="240" width="200" height="65" rx="6" fill="#ffffff" stroke="#cbd5e1"/>
    <text x="700" y="260" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#1e293b">C. Password Fingerprint</text>
    <text x="700" y="278" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9.5" fill="#475569">`isTokenValidForPassword`</text>
    <text x="700" y="294" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9.5" fill="#047857">Matches current BCrypt hash?</text>

    <!-- Outcomes -->
    <rect x="50" y="325" width="360" height="60" rx="6" fill="#ecfdf5" stroke="#10b981"/>
    <text x="65" y="347" font-family="'Segoe UI', sans-serif" font-size="10.5" font-weight="bold" fill="#065f46">Outcome 1: Password Unchanged (पासवर्ड वही है)</text>
    <text x="65" y="367" font-family="'Segoe UI', sans-serif" font-size="9" fill="#047857">Fingerprints match &#8594; User authenticated &#8594; 200 OK</text>

    <rect x="430" y="325" width="380" height="60" rx="6" fill="#fef2f2" stroke="#ef4444"/>
    <text x="445" y="347" font-family="'Segoe UI', sans-serif" font-size="10.5" font-weight="bold" fill="#991b1b">Outcome 2: Password Changed (पासवर्ड बदल गया)</text>
    <text x="445" y="367" font-family="'Segoe UI', sans-serif" font-size="9" fill="#b91c1c">New BCrypt hash mismatch &#8594; Token revoked &#8594; 401 Unauthorized</text>
  </g>
</svg>"""

with open(os.path.join(DIAGRAMS_DIR, "03_jwt_auth_flow.svg"), "w", encoding="utf-8") as f:
    f.write(jwt_svg)

# 4. Database Entity Relationships (ER Diagram)
er_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 850 480" width="850" height="480">
  <rect width="850" height="480" fill="#ffffff" rx="8" stroke="#cbd5e1" stroke-width="1"/>
  <text x="425" y="30" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-size="18" font-weight="bold" fill="#0f172a">SupportDesk Relational Entity-Relationship Diagram (ERD)</text>

  <!-- EMPLOYEES Table -->
  <g>
    <rect x="40" y="60" width="220" height="230" rx="6" fill="#f8fafc" stroke="#2563eb" stroke-width="2"/>
    <rect x="40" y="60" width="220" height="32" rx="6" fill="#1e40af"/>
    <text x="150" y="82" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="bold" fill="#ffffff">EMPLOYEES (कर्मचारी)</text>
    <text x="55" y="115" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#b91c1c">PK</tspan> id: BIGINT (AUTO_INCREMENT)</text>
    <text x="55" y="135" font-family="monospace" font-size="11" fill="#0f172a">name: VARCHAR(100)</text>
    <text x="55" y="155" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#0369a1">UQ</tspan> email: VARCHAR(100)</text>
    <text x="55" y="175" font-family="monospace" font-size="11" fill="#0f172a">password: VARCHAR(255)</text>
    <text x="55" y="195" font-family="monospace" font-size="11" fill="#0f172a">department: VARCHAR(50)</text>
    <text x="55" y="215" font-family="monospace" font-size="11" fill="#0f172a">role: VARCHAR(20) [ENUM]</text>
    <text x="55" y="235" font-family="monospace" font-size="11" fill="#0f172a">phone: VARCHAR(20)</text>
    <text x="55" y="255" font-family="monospace" font-size="11" fill="#0f172a">active: BOOLEAN</text>
    <text x="55" y="275" font-family="monospace" font-size="11" fill="#0f172a">created_at: DATETIME</text>
  </g>

  <!-- TICKETS Table -->
  <g>
    <rect x="330" y="60" width="260" height="270" rx="6" fill="#f8fafc" stroke="#059669" stroke-width="2"/>
    <rect x="330" y="60" width="260" height="32" rx="6" fill="#047857"/>
    <text x="460" y="82" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="13" font-weight="bold" fill="#ffffff">TICKETS (समर्थन टिकट)</text>
    <text x="345" y="112" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#b91c1c">PK</tspan> id: BIGINT (AUTO_INCREMENT)</text>
    <text x="345" y="130" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#0369a1">UQ</tspan> ticket_number: VARCHAR(30)</text>
    <text x="345" y="148" font-family="monospace" font-size="11" fill="#0f172a">title: VARCHAR(150)</text>
    <text x="345" y="166" font-family="monospace" font-size="11" fill="#0f172a">description: TEXT</text>
    <text x="345" y="184" font-family="monospace" font-size="11" fill="#0f172a">category: VARCHAR(50)</text>
    <text x="345" y="202" font-family="monospace" font-size="11" fill="#0f172a">priority: VARCHAR(20) [ENUM]</text>
    <text x="345" y="220" font-family="monospace" font-size="11" fill="#0f172a">status: VARCHAR(20) [ENUM]</text>
    <text x="345" y="238" font-family="monospace" font-size="11" fill="#0f172a">sla_deadline: DATETIME</text>
    <text x="345" y="256" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> employee_id: BIGINT</text>
    <text x="345" y="274" font-family="monospace" font-size="11" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> assigned_technician_id: BIGINT</text>
    <text x="345" y="292" font-family="monospace" font-size="11" fill="#0f172a">resolution_notes: TEXT</text>
    <text x="345" y="310" font-family="monospace" font-size="11" fill="#0f172a">created_at, updated_at: DATETIME</text>
  </g>

  <!-- TICKET_COMMENTS Table -->
  <g>
    <rect x="640" y="60" width="180" height="150" rx="6" fill="#f8fafc" stroke="#d97706" stroke-width="2"/>
    <rect x="640" y="60" width="180" height="32" rx="6" fill="#b45309"/>
    <text x="730" y="82" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#ffffff">TICKET_COMMENTS</text>
    <text x="650" y="112" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b91c1c">PK</tspan> id: BIGINT</text>
    <text x="650" y="132" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> ticket_id: BIGINT</text>
    <text x="650" y="152" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> employee_id: BIGINT</text>
    <text x="650" y="172" font-family="monospace" font-size="10.5" fill="#0f172a">comment_text: TEXT</text>
    <text x="650" y="192" font-family="monospace" font-size="10.5" fill="#0f172a">created_at: DATETIME</text>
  </g>

  <!-- TICKET_ACTIVITIES Table -->
  <g>
    <rect x="640" y="240" width="180" height="150" rx="6" fill="#f8fafc" stroke="#7c3aed" stroke-width="2"/>
    <rect x="640" y="240" width="180" height="32" rx="6" fill="#6d28d9"/>
    <text x="730" y="262" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#ffffff">TICKET_ACTIVITIES</text>
    <text x="650" y="292" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b91c1c">PK</tspan> id: BIGINT</text>
    <text x="650" y="312" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> ticket_id: BIGINT</text>
    <text x="650" y="332" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b45309">FK</tspan> employee_id: BIGINT</text>
    <text x="650" y="352" font-family="monospace" font-size="10.5" fill="#0f172a">activity_type: VARCHAR</text>
    <text x="650" y="372" font-family="monospace" font-size="10.5" fill="#0f172a">description: VARCHAR(255)</text>
  </g>

  <!-- NOTIFICATIONS Table -->
  <g>
    <rect x="330" y="360" width="260" height="105" rx="6" fill="#f8fafc" stroke="#db2777" stroke-width="2"/>
    <rect x="330" y="360" width="260" height="28" rx="6" fill="#be185d"/>
    <text x="460" y="380" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="12" font-weight="bold" fill="#ffffff">NOTIFICATIONS (सूचनाएँ)</text>
    <text x="345" y="405" font-family="monospace" font-size="10.5" fill="#0f172a"><tspan font-weight="bold" fill="#b91c1c">PK</tspan> id: BIGINT, <tspan font-weight="bold" fill="#b45309">FK</tspan> recipient_id, <tspan font-weight="bold" fill="#b45309">FK</tspan> ticket_id</text>
    <text x="345" y="423" font-family="monospace" font-size="10.5" fill="#0f172a">title, message, notification_type: VARCHAR</text>
    <text x="345" y="441" font-family="monospace" font-size="10.5" fill="#0f172a">is_read: BOOLEAN, created_at: DATETIME</text>
  </g>

  <!-- Relationship Lines -->
  <!-- Employees to Tickets (1 to Many) -->
  <line x1="260" y1="170" x2="330" y2="170" stroke="#2563eb" stroke-width="2" stroke-dasharray="4"/>
  <text x="295" y="162" text-anchor="middle" font-family="monospace" font-size="10" fill="#2563eb">1 : N</text>

  <!-- Tickets to Comments (1 to Many) -->
  <line x1="590" y1="130" x2="640" y2="130" stroke="#059669" stroke-width="2" stroke-dasharray="4"/>
  <text x="615" y="122" text-anchor="middle" font-family="monospace" font-size="10" fill="#059669">1 : N</text>

  <!-- Tickets to Activities (1 to Many) -->
  <line x1="590" y1="280" x2="640" y2="280" stroke="#059669" stroke-width="2" stroke-dasharray="4"/>
  <text x="615" y="272" text-anchor="middle" font-family="monospace" font-size="10" fill="#059669">1 : N</text>
</svg>"""

with open(os.path.join(DIAGRAMS_DIR, "04_database_erd.svg"), "w", encoding="utf-8") as f:
    f.write(er_svg)

# 5. Controller -> Service -> Repository Flow
flow_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 850 240" width="850" height="240">
  <defs>
    <marker id="farr" viewBox="0 0 10 10" refX="7" refY="5" markerWidth="6" markerHeight="6" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#2563eb"/>
    </marker>
  </defs>
  <rect width="850" height="240" fill="#ffffff" rx="8" stroke="#cbd5e1" stroke-width="1"/>
  <text x="425" y="28" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-size="16" font-weight="bold" fill="#0f172a">End-to-End Request Execution Flow (अनुरोध निष्पादन प्रवाह)</text>

  <!-- Client -->
  <rect x="30" y="60" width="120" height="90" rx="8" fill="#eff6ff" stroke="#2563eb" stroke-width="1.5"/>
  <text x="90" y="85" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#1d4ed8">1. Client SPA</text>
  <text x="90" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9.5" fill="#475569">POST /api/tickets</text>
  <text x="90" y="123" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Bearer &lt;JWT&gt;</text>
  <text x="90" y="138" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">JSON Payload</text>

  <path d="M 150 105 L 185 105" stroke="#2563eb" stroke-width="2" marker-end="url(#farr)"/>

  <!-- Controller -->
  <rect x="185" y="60" width="140" height="90" rx="8" fill="#faf5ff" stroke="#7c3aed" stroke-width="1.5"/>
  <text x="255" y="85" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#7e22ce">2. TicketController</text>
  <text x="255" y="103" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#475569">`@Valid @RequestBody`</text>
  <text x="255" y="120" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Checks identity spoofing</text>
  <text x="255" y="137" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Calls ticketService</text>

  <path d="M 325 105 L 360 105" stroke="#2563eb" stroke-width="2" marker-end="url(#farr)"/>

  <!-- Service -->
  <rect x="360" y="60" width="155" height="90" rx="8" fill="#f0fdf4" stroke="#059669" stroke-width="1.5"/>
  <text x="437" y="85" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#15803d">3. TicketService</text>
  <text x="437" y="103" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#475569">`@Transactional`</text>
  <text x="437" y="118" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#475569">Calculates SLA deadline</text>
  <text x="437" y="133" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">Generates unique ticket #</text>

  <path d="M 515 105 L 550 105" stroke="#2563eb" stroke-width="2" marker-end="url(#farr)"/>

  <!-- Repository -->
  <rect x="550" y="60" width="135" height="90" rx="8" fill="#fff7ed" stroke="#ea580c" stroke-width="1.5"/>
  <text x="617" y="85" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#c2410c">4. TicketRepository</text>
  <text x="617" y="103" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#475569">Spring Data JPA</text>
  <text x="617" y="120" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#475569">Hibernate translates</text>
  <text x="617" y="137" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#64748b">to parameterized SQL</text>

  <path d="M 685 105 L 720 105" stroke="#2563eb" stroke-width="2" marker-end="url(#farr)"/>

  <!-- Database -->
  <rect x="720" y="60" width="105" height="90" rx="8" fill="#0f172a" stroke="#334155" stroke-width="1.5"/>
  <text x="772" y="85" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="bold" fill="#38bdf8">5. MySQL DB</text>
  <text x="772" y="105" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#94a3b8">INSERT into</text>
  <text x="772" y="120" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#94a3b8">`tickets` &amp;</text>
  <text x="772" y="135" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="9" fill="#94a3b8">`ticket_activities`</text>

  <!-- Return Flow Text -->
  <rect x="30" y="170" width="795" height="50" rx="6" fill="#f8fafc" stroke="#cbd5e1"/>
  <text x="425" y="192" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="11" font-weight="600" fill="#0f172a">Return Flow (वापसी प्रवाह): MySQL generates ID → Hibernate maps to Ticket entity → Service creates Activity &amp; Notification</text>
  <text x="425" y="208" text-anchor="middle" font-family="'Segoe UI', sans-serif" font-size="10.5" fill="#475569">→ Controller wraps in `ResponseEntity&lt;Ticket&gt;` (HTTP 201 Created) → JSON payload rendered in Client SPA</text>
</svg>"""

with open(os.path.join(DIAGRAMS_DIR, "05_controller_service_flow.svg"), "w", encoding="utf-8") as f:
    f.write(flow_svg)

print("All SVGs successfully re-written with clean, flat vector elements.")
