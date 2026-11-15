import os
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options

SCREENSHOTS_DIR = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\screenshots"

chrome_options = Options()
chrome_options.add_argument("--headless=new")
chrome_options.add_argument("--disable-gpu")
chrome_options.add_argument("--window-size=1366,850")

driver = webdriver.Chrome(options=chrome_options)
wait = WebDriverWait(driver, 10)

try:
    driver.get("http://localhost:8082/login.html")
    email_input = wait.until(EC.presence_of_element_located((By.ID, "emailInput")))
    driver.find_element(By.ID, "passwordInput").send_keys("Pass123")
    email_input.send_keys("admin.main@supportdesk.com")
    driver.find_element(By.ID, "loginSubmitBtn").click()

    wait.until(EC.visibility_of_element_located((By.ID, "dashboardSectionView")))
    time.sleep(2)

    # Capture 10_overdue_tickets_view.png (Click All Tickets and show Overdue card / filter)
    driver.find_element(By.ID, "navTickets").click()
    wait.until(EC.visibility_of_element_located((By.ID, "ticketsSectionView")))
    time.sleep(1.5)
    driver.execute_script("const overdueCard = document.querySelector('#ticketsStatsGridContainer .stat-card:last-child'); if (overdueCard) overdueCard.style.outline = '3px solid #ef4444';")
    time.sleep(0.5)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "10_overdue_tickets_view.png"))
    print("-> Captured 10_overdue_tickets_view.png")

    # Capture 12_create_employee_modal.png
    driver.find_element(By.ID, "sidebarEmployeesBtn").click()
    wait.until(EC.visibility_of_element_located((By.ID, "employeesSectionView")))
    time.sleep(1.5)
    driver.find_element(By.ID, "openCreateEmployeeBtn").click()
    wait.until(EC.visibility_of_element_located((By.ID, "employeeModal")))
    time.sleep(1)
    driver.save_screenshot(os.path.join(SCREENSHOTS_DIR, "12_create_employee_modal.png"))
    print("-> Captured 12_create_employee_modal.png")

finally:
    driver.quit()
