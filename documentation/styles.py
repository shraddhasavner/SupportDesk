BOOK_CSS = """
@page {
    size: A4;
    margin: 14mm 14mm 14mm 14mm;
}

* {
    box-sizing: border-box;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
}

body {
    font-family: 'Segoe UI', 'Nirmala UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;
    color: #1e293b;
    line-height: 1.6;
    font-size: 11pt;
    background-color: #ffffff;
    margin: 0;
    padding: 0;
}

/* Page Break Controls */
.page-break {
    page-break-before: always;
    break-before: page;
}

.avoid-break {
    page-break-inside: avoid;
    break-inside: avoid;
}

/* Cover Page - Solid background, strictly scoped within printable page boundary */
.cover-page {
    box-sizing: border-box;
    padding: 36px 28px;
    background-color: #0f172a;
    border: 2px solid #1e3a8a;
    color: #ffffff;
    border-radius: 8px;
}

.cover-header {
    border-bottom: 2px solid rgba(255, 255, 255, 0.2);
    padding-bottom: 18px;
}

.cover-badge {
    display: inline-block;
    padding: 6px 14px;
    background-color: #38bdf8;
    color: #0f172a;
    font-size: 11pt;
    font-weight: 700;
    border-radius: 20px;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 14px;
}

.cover-title {
    font-size: 32pt;
    font-weight: 800;
    margin: 0 0 10px 0;
    line-height: 1.15;
    color: #ffffff;
}

.cover-subtitle {
    font-size: 15pt;
    font-weight: 400;
    color: #bae6fd;
    margin: 0 0 18px 0;
    line-height: 1.4;
}

.cover-hindi {
    font-size: 12pt;
    color: #fef08a;
    background-color: #1e293b;
    padding: 12px 18px;
    border-radius: 8px;
    border-left: 4px solid #facc15;
    margin-top: 14px;
    line-height: 1.5;
}

.cover-features {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 14px;
    margin: 24px 0;
}

.cover-feature-item {
    background-color: #1e293b;
    border: 1px solid #334155;
    padding: 12px 16px;
    border-radius: 6px;
    font-size: 10pt;
    line-height: 1.45;
}

.cover-footer {
    border-top: 2px solid rgba(255, 255, 255, 0.2);
    padding-top: 18px;
    display: flex;
    justify-content: space-between;
    font-size: 9.5pt;
    color: #cbd5e1;
}

/* Headings */
h1.chapter-title {
    font-size: 21pt;
    font-weight: 800;
    color: #1e3a8a;
    border-bottom: 3px solid #3b82f6;
    padding-bottom: 8px;
    margin-top: 0;
    margin-bottom: 18px;
    page-break-before: always;
    break-before: page;
}

h2 {
    font-size: 14pt;
    font-weight: 700;
    color: #0f172a;
    margin-top: 22px;
    margin-bottom: 10px;
    border-left: 4px solid #2563eb;
    padding-left: 10px;
    page-break-after: avoid;
    break-after: avoid;
}

h3 {
    font-size: 12pt;
    font-weight: 600;
    color: #1e293b;
    margin-top: 16px;
    margin-bottom: 8px;
    page-break-after: avoid;
    break-after: avoid;
}

p {
    margin: 8px 0;
}

/* Callout Boxes */
.bilingual-box {
    margin: 14px 0;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #cbd5e1;
    page-break-inside: avoid;
    break-inside: avoid;
}

.bilingual-header {
    background-color: #f1f5f9;
    padding: 8px 14px;
    font-weight: 700;
    font-size: 10pt;
    color: #334155;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid #cbd5e1;
}

.english-part {
    padding: 10px 14px;
    background-color: #ffffff;
    font-size: 10pt;
    color: #1e293b;
    border-bottom: 1px dashed #e2e8f0;
}

.hindi-part {
    padding: 10px 14px;
    background-color: #f0fdf4;
    font-size: 10pt;
    color: #14532d;
    border-left: 4px solid #16a34a;
}

.real-life-box {
    margin: 12px 0;
    padding: 10px 14px;
    background-color: #eff6ff;
    border-left: 4px solid #3b82f6;
    border-radius: 0 6px 6px 0;
    font-size: 9.5pt;
    page-break-inside: avoid;
    break-inside: avoid;
}

.mistake-box {
    margin: 12px 0;
    padding: 10px 14px;
    background-color: #fff1f2;
    border-left: 4px solid #e11d48;
    border-radius: 0 6px 6px 0;
    font-size: 9.5pt;
    color: #881337;
    page-break-inside: avoid;
    break-inside: avoid;
}

.interview-tip-box {
    margin: 12px 0;
    padding: 10px 14px;
    background-color: #faf5ff;
    border-left: 4px solid #9333ea;
    border-radius: 0 6px 6px 0;
    font-size: 9.5pt;
    color: #581c87;
    page-break-inside: avoid;
    break-inside: avoid;
}

/* Code Blocks */
.code-container {
    margin: 14px 0;
    background-color: #0f172a;
    border-radius: 6px;
    overflow: hidden;
    page-break-inside: avoid;
    break-inside: avoid;
    border: 1px solid #334155;
}

.code-header {
    background-color: #1e293b;
    padding: 6px 14px;
    color: #94a3b8;
    font-size: 8.5pt;
    font-family: monospace;
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid #334155;
}

pre.code-block {
    margin: 0;
    padding: 10px 14px;
    color: #f8fafc;
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 8.5pt;
    line-height: 1.42;
    overflow-x: auto;
    white-space: pre-wrap;
    word-wrap: break-word;
}

/* Line-by-line explanation table */
.line-explain-table {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9pt;
}

.line-explain-table thead {
    display: table-header-group;
}

.line-explain-table tr {
    page-break-inside: avoid;
    break-inside: avoid;
}

.line-explain-table th {
    background-color: #f1f5f9;
    color: #0f172a;
    padding: 7px 10px;
    text-align: left;
    border: 1px solid #cbd5e1;
    font-weight: 700;
}

.line-explain-table td {
    padding: 6px 10px;
    border: 1px solid #e2e8f0;
    vertical-align: top;
}

.line-code {
    font-family: monospace;
    color: #0369a1;
    font-weight: 600;
    white-space: nowrap;
}

/* Image & Diagram Containers */
.figure-container {
    margin: 16px 0;
    text-align: center;
    page-break-inside: avoid;
    break-inside: avoid;
}

.figure-img {
    max-width: 95%;
    max-height: 400px;
    width: auto;
    height: auto;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    border: 1px solid #cbd5e1;
    display: block;
    margin: 0 auto;
}

.figure-caption {
    margin-top: 8px;
    font-size: 9pt;
    color: #475569;
    font-weight: 600;
}

/* Tables */
table.data-table {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9pt;
}

table.data-table thead {
    display: table-header-group;
}

table.data-table tr {
    page-break-inside: avoid;
    break-inside: avoid;
}

table.data-table th {
    background-color: #1e3a8a;
    color: #ffffff;
    padding: 8px 10px;
    text-align: left;
    font-weight: 600;
    border: 1px solid #1e3a8a;
}

table.data-table td {
    padding: 7px 10px;
    border: 1px solid #cbd5e1;
}

table.data-table tr:nth-child(even) {
    background-color: #f8fafc;
}

/* Badges */
.badge {
    display: inline-block;
    padding: 2px 7px;
    border-radius: 4px;
    font-size: 8pt;
    font-weight: 700;
    text-transform: uppercase;
}

.badge-admin { background-color: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
.badge-tech { background-color: #e0e7ff; color: #3730a3; border: 1px solid #c7d2fe; }
.badge-emp { background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; }
.badge-critical { background-color: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }

/* Table of Contents */
.toc-container {
    background-color: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 20px;
    margin: 16px 0;
}

.toc-item {
    display: flex;
    justify-content: space-between;
    padding: 5px 0;
    border-bottom: 1px dotted #cbd5e1;
    font-size: 10pt;
}

.toc-chapter {
    font-weight: 700;
    color: #1e3a8a;
}

.toc-page {
    font-family: monospace;
    color: #64748b;
}

/* Interview Q&A Cards */
.qa-card {
    margin: 14px 0;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    overflow: hidden;
    page-break-inside: avoid;
    break-inside: avoid;
}

.qa-question {
    background-color: #f8fafc;
    padding: 9px 12px;
    font-weight: 700;
    font-size: 10.5pt;
    color: #0f172a;
    border-bottom: 1px solid #cbd5e1;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.qa-answer-en {
    padding: 10px 14px;
    background-color: #ffffff;
    font-size: 9.5pt;
    border-bottom: 1px dashed #e2e8f0;
}

.qa-answer-hi {
    padding: 10px 14px;
    background-color: #f0fdf4;
    font-size: 9.5pt;
    color: #14532d;
    border-left: 4px solid #16a34a;
}

.qa-meta {
    padding: 7px 12px;
    background-color: #f1f5f9;
    font-size: 8.5pt;
    color: #475569;
    font-style: italic;
}
"""
