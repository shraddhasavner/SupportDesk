package com.supportdesk.service;

import com.supportdesk.controller.UpdateEmployeeRequest;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.security.SecurityUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EmployeeService: Contains business logic for managing employees and technicians.
 * 
 * [English]:
 * - Validates uniqueness of email addresses.
 * - Encrypts passwords using BCryptPasswordEncoder before persisting.
 * - Enforces administrator lockout protection (cannot deactivate self, cannot deactivate last active admin).
 * - Manages employee activation/deactivation (soft delete).
 * - Coordinates database queries via EmployeeRepository.
 * 
 * [Hinglish]:
 * - Email duplicate check karta hai taaki ek email se do accounts na banein.
 * - Password ko BCrypt se encrypt karta hai taaki plain-text password database me na jaye.
 * - Admin lockout protection enforce karta hai taaki system me hamesha kam se kam ek active admin rahe.
 * - Employee ko delete karne ke bajaye deactivate karta hai (soft delete) taaki ticket history bani rahe.
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, @Autowired(required = false) SecurityUtils securityUtils) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this(employeeRepository, passwordEncoder, null);
    }

    /**
     * Create a new employee with encrypted password and duplicate email prevention.
     */
    public Employee createEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee payload cannot be null.");
        }
        if (employee.getPassword() == null || employee.getPassword().isBlank() || employee.getPassword().trim().length() < 6) {
            throw new IllegalArgumentException("Password is required and must be at least 6 characters.");
        }

        String normalizedEmail = employee.getEmail() != null ? employee.getEmail().trim().toLowerCase() : "";
        if (employeeRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email '" + employee.getEmail() + "' is already registered in the system.");
        }

        employee.setEmail(normalizedEmail);
        if (employee.getName() != null) {
            employee.setName(employee.getName().trim());
        }
        if (employee.getDepartment() != null) {
            employee.setDepartment(employee.getDepartment().trim());
        }
        if (employee.getPhone() != null && !employee.getPhone().isBlank()) {
            employee.setPhone(employee.getPhone().trim());
        } else {
            employee.setPhone(null);
        }

        // Hash the password with BCrypt before saving to database
        employee.setPassword(passwordEncoder.encode(employee.getPassword().trim()));
        employee.setActive(true);
        return employeeRepository.save(employee);
    }

    /**
     * Retrieve all employees.
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Retrieve an employee by ID or throw ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
    }

    /**
     * Retrieve all employees with a specific role (e.g., all TECHNICIANs).
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByRole(Role role) {
        return employeeRepository.findByRole(role);
    }

    /**
     * Retrieve all employees in a specific department.
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    /**
     * Update employee details with duplicate email validation.
     * Preserves the existing encoded password if a new password is not provided or blank.
     */
    public Employee updateEmployee(Long id, UpdateEmployeeRequest updatedData) {
        if (updatedData == null) {
            throw new IllegalArgumentException("Updated employee data cannot be null.");
        }

        Employee existing = getEmployeeById(id);

        // Check if email is being changed and if new email is already in use by someone else
        String normalizedNewEmail = updatedData.getEmail() != null ? updatedData.getEmail().trim().toLowerCase() : "";
        if (!existing.getEmail().equalsIgnoreCase(normalizedNewEmail)) {
            if (employeeRepository.existsByEmail(normalizedNewEmail)) {
                throw new DuplicateEmailException("Email '" + updatedData.getEmail() + "' is already in use by another employee.");
            }
            existing.setEmail(normalizedNewEmail);
        }

        // Lockout Check: Prevent demoting the last active administrator or demoting oneself from ADMIN
        if (existing.getRole() == Role.ADMIN && updatedData.getRole() != null && updatedData.getRole() != Role.ADMIN && existing.isActive()) {
            Employee currentEmployee = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
            if (currentEmployee != null && currentEmployee.getId() != null && currentEmployee.getId().equals(id)) {
                throw new IllegalArgumentException("Administrators cannot demote their own account from the ADMIN role.");
            }
            List<Employee> activeAdmins = employeeRepository.findActiveEmployeesByRoleForUpdate(Role.ADMIN);
            if (activeAdmins.size() <= 1) {
                throw new IllegalStateException("Cannot demote the last active administrator account.");
            }
        }

        if (updatedData.getName() != null) {
            existing.setName(updatedData.getName().trim());
        }
        if (updatedData.getDepartment() != null) {
            existing.setDepartment(updatedData.getDepartment().trim());
        }
        if (updatedData.getRole() != null) {
            existing.setRole(updatedData.getRole());
        }
        if (updatedData.getPhone() != null && !updatedData.getPhone().isBlank()) {
            existing.setPhone(updatedData.getPhone().trim());
        } else {
            existing.setPhone(null);
        }

        // Update password only if a new non-blank password is provided
        if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {
            String rawPassword = updatedData.getPassword().trim();
            if (rawPassword.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            }
            existing.setPassword(passwordEncoder.encode(rawPassword));
        }

        return employeeRepository.save(existing);
    }

    /**
     * Backward compatibility overload for callers passing an Employee entity.
     */
    public Employee updateEmployee(Long id, Employee updatedData) {
        if (updatedData == null) {
            throw new IllegalArgumentException("Updated employee data cannot be null.");
        }
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                updatedData.getName(),
                updatedData.getEmail(),
                updatedData.getDepartment(),
                updatedData.getRole(),
                updatedData.getPhone(),
                updatedData.getPassword()
        );
        return updateEmployee(id, request);
    }

    /**
     * Deactivate an employee (soft delete).
     * Disables login/account while preserving tickets and history.
     * Enforces administrator lockout protection:
     * 1. Administrators cannot deactivate their own account.
     * 2. Cannot deactivate the last active administrator account.
     */
    public Employee deactivateEmployee(Long id) {
        Employee existing = getEmployeeById(id);

        if (!existing.isActive()) {
            return existing;
        }

        // Lockout Check 1: Administrators cannot deactivate their own account
        Employee currentEmployee = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
        if (currentEmployee != null && currentEmployee.getId() != null && currentEmployee.getId().equals(id)) {
            throw new IllegalArgumentException("Administrators cannot deactivate their own account.");
        }

        // Lockout Check 2: Cannot deactivate the last active administrator
        if (existing.getRole() == Role.ADMIN) {
            List<Employee> activeAdmins = employeeRepository.findActiveEmployeesByRoleForUpdate(Role.ADMIN);
            if (activeAdmins.size() <= 1) {
                throw new IllegalStateException("Cannot deactivate the last active administrator account.");
            }
        }

        existing.setActive(false);
        return employeeRepository.save(existing);
    }

    /**
     * Reactivate an employee.
     */
    public Employee activateEmployee(Long id) {
        Employee existing = getEmployeeById(id);
        existing.setActive(true);
        return employeeRepository.save(existing);
    }
}
