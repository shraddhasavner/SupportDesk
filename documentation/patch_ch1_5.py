import re

with open("documentation/chapters_1_5.py", "r", encoding="utf-8") as f:
    c = f.read()

# Replace TOC
toc_old = """    <div class="toc-item"><span class="toc-chapter">Chapter 2 — Technology Stack &amp; Justifications (प्रौद्योगिकी स्टैक)</span><span class="toc-page">07</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 3 — Project Folder Structure &amp; Configuration (फ़ोल्डर संरचना)</span><span class="toc-page">11</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 4 — Application Architecture &amp; Layered Flow (एप्लिकेशन आर्किटेक्चर)</span><span class="toc-page">15</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 5 — Database Schema, ERD &amp; SQL Queries (डेटाबेस और संबंध)</span><span class="toc-page">20</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 6 — Employee Administration &amp; Access Boundaries (कर्मचारी प्रबंधन)</span><span class="toc-page">25</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 7 — Ticket Lifecycle &amp; Assignment Workflow (टिकट प्रबंधन)</span><span class="toc-page">30</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 8 — Priority-Based SLA &amp; Overdue Detection Engine (SLA ट्रैकिंग)</span><span class="toc-page">36</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 9 — Spring Security, JWT &amp; Password Revocation (सुरक्षा एवं RBAC)</span><span class="toc-page">41</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 10 — REST API Reference &amp; Request Contracts (REST एंडपॉइंट्स)</span><span class="toc-page">47</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 11 — Frontend SPA Architecture &amp; API Integration (फ़्रंटएंड एकीकरण)</span><span class="toc-page">52</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 12 — Input Validation &amp; Centralized Exception Handling (अपवाद प्रबंधन)</span><span class="toc-page">57</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 13 — Automated Testing &amp; Verification Suite (स्वचालित परीक्षण)</span><span class="toc-page">62</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 14 — Setup, Configuration &amp; Deployment Guide (सेटअप एवं निष्पादन)</span><span class="toc-page">67</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 15 — Production Troubleshooting &amp; Debugging (समस्या निवारण)</span><span class="toc-page">71</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 16 — Technical Limitations &amp; Future Roadmap (सीमाएँ एवं सुधार)</span><span class="toc-page">75</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 17 — Core Technical Interview Questions &amp; Answers (साक्षात्कार प्रश्नोत्तर)</span><span class="toc-page">79</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 18 — Practical Mock Interview: 80 Questions Bank (मॉक इंटरव्यू बैंक)</span><span class="toc-page">89</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 19 — Quick Revision Notes &amp; Last-Day Checklist (त्वरित पुनरावलोकन)</span><span class="toc-page">104</span></div>
    <div class="toc-item"><span class="toc-chapter">Chapter 20 — Personal 14-Day Practice &amp; Mastery Plan (14-दिवसीय अध्ययन योजना)</span><span class="toc-page">110</span></div>
    <div class="toc-item"><span class="toc-chapter">Appendix &amp; Technical Terms Glossary (शब्दावली एवं फ़ाइल सूची)</span><span class="toc-page">115</span></div>"""

toc_new = """    <div class="toc-item"><span class="toc-chapter">Chapter 2 — Technology Stack &amp; Justifications (प्रौद्योगिकी स्टैक)</span><span class="toc-page">05</span></div>
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
    <div class="toc-item"><span class="toc-chapter">Appendix &amp; Technical Terms Glossary (शब्दावली एवं फ़ाइल सूची)</span><span class="toc-page">48</span></div>"""

c = c.replace(toc_old.replace("\n", "\r\n"), toc_new.replace("\n", "\r\n"))
c = c.replace(toc_old, toc_new)

# Replace rightarrow
c = re.sub(r'\$\\rightarrow\$', '&rarr;', c)

with open("documentation/chapters_1_5.py", "w", encoding="utf-8") as f:
    f.write(c)

print("chapters_1_5.py updated successfully!")
