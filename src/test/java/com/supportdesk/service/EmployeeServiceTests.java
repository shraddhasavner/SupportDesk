package com.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.controller.UpdateEmployeeRequest;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EmployeeServiceTests
{
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should create employee and hash password with BCrypt")
    void testCreateEmployeeHashedPassword()
    {
        Employee emp = new Employee("Raj Kumar", "raj.kumar@test.com", "mySecret123", "IT", Role.TECHNICIAN, "9998887776");
        Employee created = employeeService.createEmployee(emp);

        assertNotNull(created.getId());
        assertTrue(created.isActive());
        assertFalse(created.getPassword().equals("mySecret123"), "Password must not be stored in plain text");
        assertTrue(passwordEncoder.matches("mySecret123", created.getPassword()), "BCrypt must verify the hashed password");
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email is already taken")
    void testDuplicateEmailThrowsException()
    {
        Employee emp1 = new Employee("User One", "duplicate@test.com", "pass1234", "HR", Role.EMPLOYEE, "123");
        employeeService.createEmployee(emp1);

        Employee emp2 = new Employee("User Two", "duplicate@test.com", "pass5678", "Finance", Role.EMPLOYEE, "456");
        assertThrows(DuplicateEmailException.class, () -> employeeService.createEmployee(emp2));
    }

    @Test
    @DisplayName("Should deactivate an employee (soft delete)")
    void testDeactivateEmployee()
    {
        Employee emp = new Employee("Inactive Test", "inactive@test.com", "pass1234", "IT", Role.EMPLOYEE, "123");
        Employee created = employeeService.createEmployee(emp);

        Employee deactivated = employeeService.deactivateEmployee(created.getId());
        assertFalse(deactivated.isActive(), "Active status should be false after deactivation");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid ID")
    void testGetInvalidIdThrowsNotFound()
    {
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999999L));
    }

    @Test
    @DisplayName("Should preserve existing password hash when updating profile with null or blank password")
    void testUpdateEmployeeProfileOnlyPreservesPasswordHash()
    {
        Employee emp = new Employee("Service Profile", "serv.prof@test.com", "OriginalPass123", "IT", Role.TECHNICIAN, "999");
        Employee created = employeeService.createEmployee(emp);
        String initialHash = created.getPassword();

        UpdateEmployeeRequest req = new UpdateEmployeeRequest("Service Profile Updated", "serv.prof@test.com", "IT Core", Role.TECHNICIAN, "888", null);
        Employee updated = employeeService.updateEmployee(created.getId(), req);

        assertEquals("Service Profile Updated", updated.getName());
        assertEquals("IT Core", updated.getDepartment());
        assertEquals(initialHash, updated.getPassword(), "Password hash must be preserved when password is null");
        assertTrue(passwordEncoder.matches("OriginalPass123", updated.getPassword()), "Original password must still match hash");

        UpdateEmployeeRequest blankReq = new UpdateEmployeeRequest("Service Profile Updated 2", "serv.prof@test.com", "IT Ops", Role.TECHNICIAN, "777", "   ");
        Employee updated2 = employeeService.updateEmployee(created.getId(), blankReq);

        assertEquals(initialHash, updated2.getPassword(), "Password hash must be preserved when password is whitespace/blank");
        assertTrue(passwordEncoder.matches("OriginalPass123", updated2.getPassword()), "Original password must still match hash");
    }

    @Test
    @DisplayName("Should update and re-encode password hash when valid new password is provided")
    void testUpdateEmployeeWithNewPasswordEncodes()
    {
        Employee emp = new Employee("Service NewPwd", "serv.newpwd@test.com", "InitialPass123", "IT", Role.TECHNICIAN, "999");
        Employee created = employeeService.createEmployee(emp);
        String initialHash = created.getPassword();

        UpdateEmployeeRequest req = new UpdateEmployeeRequest("Service NewPwd", "serv.newpwd@test.com", "IT", Role.TECHNICIAN, "999", "NewSecretPass456");
        Employee updated = employeeService.updateEmployee(created.getId(), req);

        assertNotEquals(initialHash, updated.getPassword(), "Password hash must change when new password is provided");
        assertTrue(passwordEncoder.matches("NewSecretPass456", updated.getPassword()), "New password must match updated hash");
        assertFalse(passwordEncoder.matches("InitialPass123", updated.getPassword()), "Old password must no longer match");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with short invalid password")
    void testUpdateEmployeeWithShortPasswordThrows()
    {
        Employee emp = new Employee("Service Short", "serv.short@test.com", "ValidPass123", "IT", Role.TECHNICIAN, "999");
        Employee created = employeeService.createEmployee(emp);

        UpdateEmployeeRequest req = new UpdateEmployeeRequest("Service Short", "serv.short@test.com", "IT", Role.TECHNICIAN, "999", "123");
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(created.getId(), req));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating employee without a valid password")
    void testCreateEmployeeWithoutPasswordThrows()
    {
        Employee emp = new Employee("Service NoPwd", "serv.nopwd@test.com", "", "IT", Role.TECHNICIAN, "999");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }
}