import os
import sys
import base64
import time
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

# Add documentation folder to sys.path
DOCS_DIR = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation"
sys.path.insert(0, DOCS_DIR)

from styles import BOOK_CSS
from chapters_1_5 import get_chapters_1_to_5_html
from chapters_6_10 import get_chapters_6_to_10_html
from chapters_11_16 import get_chapters_11_to_16_html
from chapters_17_20 import get_chapters_17_to_20_html

SCREENSHOTS_DIR = os.path.join(DOCS_DIR, "screenshots")
DIAGRAMS_DIR = os.path.join(DOCS_DIR, "diagrams")
PDF_OUTPUT_PATH = os.path.join(DOCS_DIR, "SupportDesk_Complete_Interview_Guide.pdf")
HTML_OUTPUT_PATH = os.path.join(DOCS_DIR, "SupportDesk_Complete_Interview_Guide.html")

def get_base64_image(file_path):
    if not os.path.exists(file_path):
        print(f"Warning: Image file not found: {file_path}")
        return ""
    with open(file_path, "rb") as f:
        data = f.read()
    ext = os.path.splitext(file_path)[1].lower().replace(".", "")
    mime = "image/svg+xml" if ext == "svg" else f"image/{ext}"
    return f"data:{mime};base64,{base64.b64encode(data).decode('utf-8')}"

print("Loading screenshot and diagram assets into base64...")

assets = {
    'd_arch': get_base64_image(os.path.join(DIAGRAMS_DIR, "01_layered_architecture.svg")),
    'd_life': get_base64_image(os.path.join(DIAGRAMS_DIR, "02_ticket_lifecycle.svg")),
    'd_jwt': get_base64_image(os.path.join(DIAGRAMS_DIR, "03_jwt_auth_flow.svg")),
    'd_erd': get_base64_image(os.path.join(DIAGRAMS_DIR, "04_database_erd.svg")),
    'd_flow': get_base64_image(os.path.join(DIAGRAMS_DIR, "05_controller_service_flow.svg")),
    's_login': get_base64_image(os.path.join(SCREENSHOTS_DIR, "01_login_page.png")),
    's_dash': get_base64_image(os.path.join(SCREENSHOTS_DIR, "02_admin_dashboard.png")),
    's_tickets': get_base64_image(os.path.join(SCREENSHOTS_DIR, "03_all_tickets_page.png")),
    's_create_t': get_base64_image(os.path.join(SCREENSHOTS_DIR, "04_create_ticket_modal.png")),
    's_details': get_base64_image(os.path.join(SCREENSHOTS_DIR, "05_ticket_details_modal.png")),
    's_assign': get_base64_image(os.path.join(SCREENSHOTS_DIR, "06_technician_assignment_modal.png")),
    's_status': get_base64_image(os.path.join(SCREENSHOTS_DIR, "07_ticket_status_update_modal.png")),
    's_comments': get_base64_image(os.path.join(SCREENSHOTS_DIR, "08_ticket_comments.png")),
    's_activity': get_base64_image(os.path.join(SCREENSHOTS_DIR, "09_ticket_activity_history.png")),
    's_overdue': get_base64_image(os.path.join(SCREENSHOTS_DIR, "10_overdue_tickets_view.png")),
    's_emp': get_base64_image(os.path.join(SCREENSHOTS_DIR, "11_employee_directory.png")),
    's_create_emp': get_base64_image(os.path.join(SCREENSHOTS_DIR, "12_create_employee_modal.png")),
    's_workload': get_base64_image(os.path.join(SCREENSHOTS_DIR, "13_technician_workload.png")),
    's_notif': get_base64_image(os.path.join(SCREENSHOTS_DIR, "14_in_app_notifications.png"))
}

print("Compiling HTML document...")

html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>SupportDesk – Complete Technical &amp; Interview Guide</title>
<style>
{BOOK_CSS}
</style>
</head>
<body>

{get_chapters_1_to_5_html(assets)}
{get_chapters_6_to_10_html(assets)}
{get_chapters_11_to_16_html(assets)}
{get_chapters_17_to_20_html()}

</body>
</html>
"""

with open(HTML_OUTPUT_PATH, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"HTML compilation complete: {HTML_OUTPUT_PATH} ({len(html_content)} characters)")

print("Launching Chrome Headless to generate vector PDF...")

chrome_options = Options()
chrome_options.add_argument("--headless=new")
chrome_options.add_argument("--disable-gpu")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")

driver = webdriver.Chrome(options=chrome_options)

try:
    file_uri = "file:///" + HTML_OUTPUT_PATH.replace("\\", "/")
    print(f"Loading {file_uri} in browser...")
    driver.get(file_uri)
    time.sleep(3) # Ensure all fonts, SVGs, and images are fully rendered

    print("Executing CDP Page.printToPDF with headers, footers, and margins...")
    pdf_params = {
        'printBackground': True,
        'displayHeaderFooter': True,
        'headerTemplate': '<div style="font-size:8pt; font-family: \'Segoe UI\', Arial, sans-serif; width:100%; text-align:right; padding-right:15mm; color:#94a3b8;">SupportDesk – IT Help Desk System | Technical &amp; Interview Guide</div>',
        'footerTemplate': '<div style="font-size:8pt; font-family: \'Segoe UI\', Arial, sans-serif; width:100%; display:flex; justify-content:space-between; padding:0 15mm; color:#94a3b8;"><span>Confidential &amp; Proprietary</span><span>Page <span class="pageNumber"></span> of <span class="totalPages"></span></span></div>',
        'marginTop': 0.5,
        'marginBottom': 0.5,
        'marginLeft': 0.5,
        'marginRight': 0.5,
        'paperWidth': 8.27,  # A4 width in inches
        'paperHeight': 11.69, # A4 height in inches
        'preferCSSPageSize': True
    }

    pdf_data = driver.execute_cdp_cmd('Page.printToPDF', pdf_params)
    
    with open(PDF_OUTPUT_PATH, "wb") as f:
        f.write(base64.b64decode(pdf_data['data']))

    file_size_mb = os.path.getsize(PDF_OUTPUT_PATH) / (1024 * 1024)
    print(f"\n=======================================================")
    print(f"SUCCESS: PDF BOOK GENERATED SUCCESSFULLY!")
    print(f"Path: {PDF_OUTPUT_PATH}")
    print(f"Size: {file_size_mb:.2f} MB")
    print(f"=======================================================")

finally:
    driver.quit()
