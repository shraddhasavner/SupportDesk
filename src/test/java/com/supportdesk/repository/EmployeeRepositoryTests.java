package com.supportdesk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for EmployeeRepository.
 *
 * @Transactional ensures test data is rolled back after each test method runs,
 * keeping the database clean for subsequent runs.
 */
@SpringBootTest
@Transactional
class EmployeeRepositoryTests
{
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Should save an employee and retrieve by email")
    void testSaveAndFindByEmail()
    {
        Employee employee = new Employee(
                "Kamlesh Sharma",
                "kamlesh@supportdesk.com",
                "password123",
                "IT Support",
                Role.TECHNICIAN,
                "9876543210"
        );

        Employee saved = employeeRepository.save(employee);

        assertNotNull(saved.getId(), "Generated ID should not be null");
        assertNotNull(saved.getCreatedAt(), "CreatedAt timestamp should be populated automatically");

        Optional<Employee> found = employeeRepository.findByEmail("kamlesh@supportdesk.com");
        assertTrue(found.isPresent(), "Employee should be found by email");
        assertEquals("Kamlesh Sharma", found.get().getName());
        assertEquals(Role.TECHNICIAN, found.get().getRole());
    }

    @Test
    @DisplayName("Should find employees by their role")
    void testFindByRole()
    {
        Employee tech1 = new Employee("Alex Tech", "alex@supportdesk.com", "password123", "IT", Role.TECHNICIAN, "111");
        Employee tech2 = new Employee("Bob Tech", "bob@supportdesk.com", "password123", "IT", Role.TECHNICIAN, "222");
        Employee user1 = new Employee("Sara User", "sara@supportdesk.com", "password123", "HR", Role.EMPLOYEE, "333");

        employeeRepository.save(tech1);
        employeeRepository.save(tech2);
        employeeRepository.save(user1);

        List<Employee> technicians = employeeRepository.findByRole(Role.TECHNICIAN);
        assertTrue(technicians.size() >= 2, "There should be at least 2 technicians");
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail()
    {
        Employee emp = new Employee("Jane Admin", "admin@supportdesk.com", "adminpass", "Management", Role.ADMIN, "999");
        employeeRepository.save(emp);

        assertTrue(employeeRepository.existsByEmail("admin@supportdesk.com"));
        assertEquals(false, employeeRepository.existsByEmail("nonexistent@supportdesk.com"));
    }
}