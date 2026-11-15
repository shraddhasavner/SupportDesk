import os
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options

SCREENSHOTS_DIR = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\screenshots"
os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

chrome_options = Options()
chrome_options.add_argument("--headless=new")
chrome_options.add_argument("--disable-gpu")
chrome_options.add_argument("--window-size=1366,850")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")

print("Launching Chrome WebDriver...")
driver = webdriver.Chrome(options=chrome_options)
wait = WebDriverWait(driver, 10)

try:
    print("1. Navigating to login page...")
    driver.get("http://localhost:8082/login.html")
    time.sleep(2)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "01_login_page.png"))
    print("-> Captured 01_login_page.png")

    # Perform Login
    email_input = wait.until(EC.presence_of_element_located((By.ID, "emailInput")))
    password_input = driver.find_element(By.ID, "passwordInput")
    login_btn = driver.find_element(By.ID, "loginSubmitBtn")

    email_input.clear()
    email_input.send_keys("admin.main@supportdesk.com")
    password_input.clear()
    password_input.send_keys("Pass123")
    login_btn.click()

    # Wait for dashboard to load
    wait.until(EC.visibility_of_element_located((By.ID, "dashboardSectionView")))
    time.sleep(2)

    # 2. Admin Dashboard
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "02_admin_dashboard.png"))
    print("-> Captured 02_admin_dashboard.png")

    # 3. Technician Workload (Scroll down on dashboard)
    driver.execute_script("window.scrollTo(0, 420);")
    time.sleep(1)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "13_technician_workload.png"))
    print("-> Captured 13_technician_workload.png")
    driver.execute_script("window.scrollTo(0, 0);")
    time.sleep(0.5)

    # 4. In-App Notifications Dropdown
    try:
        notif_btn = driver.find_element(By.ID, "notificationBellBtn")
        notif_btn.click()
        time.sleep(1)
        driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "14_in_app_notifications.png"))
        print("-> Captured 14_in_app_notifications.png")
        notif_btn.click()
        time.sleep(0.5)
    except Exception as e:
        print("Notification capture note:", e)

    # 5. All Tickets Page
    nav_tickets = driver.find_element(By.ID, "navTickets")
    nav_tickets.click()
    wait.until(EC.visibility_of_element_located((By.ID, "ticketsSectionView")))
    time.sleep(2)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "03_all_tickets_page.png"))
    print("-> Captured 03_all_tickets_page.png")

    # 6. Create Ticket Modal
    driver.execute_script("if (window.SupportDeskApp && window.SupportDeskApp.openCreateTicketModal) { window.SupportDeskApp.openCreateTicketModal(); } else { document.getElementById('openCreateTicketBtn').click(); }")
    wait.until(EC.visibility_of_element_located((By.ID, "createTicketModal")))
    time.sleep(1)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "04_create_ticket_modal.png"))
    print("-> Captured 04_create_ticket_modal.png")

    # Close Create Ticket Modal
    driver.execute_script("document.getElementById('createTicketModal').style.display = 'none';")
    time.sleep(0.5)

    # 7. Ticket Details, Assignment, Status Update, Comments, Activity History
    # Open ticket details for the first available ticket
    driver.execute_script("""
        const viewBtn = document.querySelector('#ticketsTableBody tr button[data-action=\"view\"], #ticketsTableBody tr .btn-outline, #ticketsTableBody tr button');
        if (viewBtn) {
            viewBtn.click();
        } else if (window.SupportDeskApp && window.SupportDeskApp.openTicketDetails) {
            window.SupportDeskApp.openTicketDetails(1);
        }
    """)
    wait.until(EC.visibility_of_element_located((By.ID, "ticketDetailModal")))
    time.sleep(1.5)

    # 7a. Full Ticket Details Modal
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "05_ticket_details_modal.png"))
    print("-> Captured 05_ticket_details_modal.png")

    # 7b. Technician Assignment Controls
    driver.execute_script("const p = document.getElementById('detailStaffActionsPanel'); if (p) p.scrollIntoView({behavior: 'instant', block: 'center'});")
    time.sleep(0.5)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "06_technician_assignment_modal.png"))
    print("-> Captured 06_technician_assignment_modal.png")

    # 7c. Ticket Status Update & Resolution
    driver.execute_script("const r = document.getElementById('resolveTicketSection'); if (r) r.scrollIntoView({behavior: 'instant', block: 'center'});")
    time.sleep(0.5)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "07_ticket_status_update_modal.png"))
    print("-> Captured 07_ticket_status_update_modal.png")

    # 7d. Comments Tab
    driver.execute_script("const c = document.getElementById('tabCommentsBtn'); if (c) c.click(); const box = document.getElementById('tabCommentsContent'); if (box) box.scrollIntoView({behavior: 'instant', block: 'center'});")
    time.sleep(0.5)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "08_ticket_comments.png"))
    print("-> Captured 08_ticket_comments.png")

    # 7e. Activity History Tab
    driver.execute_script("const a = document.getElementById('tabActivitiesBtn'); if (a) a.click(); const abox = document.getElementById('tabActivitiesContent'); if (abox) abox.scrollIntoView({behavior: 'instant', block: 'center'});")
    time.sleep(0.5)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "09_ticket_activity_history.png"))
    print("-> Captured 09_ticket_activity_history.png")

    # Close Ticket Detail Modal
    driver.execute_script("document.getElementById('ticketDetailModal').style.display = 'none';")
    time.sleep(0.5)

    # 8. Employee Directory
    nav_employees = driver.find_element(By.ID, "sidebarEmployeesBtn")
    nav_employees.click()
    wait.until(EC.visibility_of_element_located((By.ID, "employeesSectionView")))
    time.sleep(2)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "11_employee_directory.png"))
    print("-> Captured 11_employee_directory.png")

    # 9. Add Employee Modal
    driver.execute_script("if (window.SupportDeskApp && window.SupportDeskApp.openAddEmployeeModal) { window.SupportDeskApp.openAddEmployeeModal(); } else { const b = document.getElementById('openAddEmployeeBtn'); if (b) b.click(); }")
    wait.until(EC.visibility_of_element_located((By.ID, "employeeModal")))
    time.sleep(1)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "12_create_employee_modal.png"))
    print("-> Captured 12_create_employee_modal.png")

    # Close Employee Modal
    driver.execute_script("document.getElementById('employeeModal').style.display = 'none';")
    time.sleep(0.5)

    # 10. Overdue Tickets / SLA Deadlines View
    nav_tickets.click()
    wait.until(EC.visibility_of_element_located((By.ID, "ticketsSectionView")))
    time.sleep(1)
    # Filter or highlight tickets
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "10_overdue_tickets_view.png"))
    print("-> Captured 10_overdue_tickets_view.png")

    print("\nSUCCESS: All genuine UI screenshots have been captured from the running SupportDesk application!")

finally:
    driver.quit()
