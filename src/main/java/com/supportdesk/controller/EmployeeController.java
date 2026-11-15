package com.supportdesk.controller;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * EmployeeController: REST API endpoints for Employee Management.
 * 
 * [English]:
 * - Base path: /api/employees
 * - Provides CRUD endpoints for employee registration, details, updates, and deactivation.
 * - Enforces role-based permissions: only ADMIN can create, delete, or list all employees.
 * - Non-admin employees can only view or update their own personal employee profile.
 * 
 * [Hinglish]:
 * - Yeh REST Controller employees aur technicians ke liye saare web APIs handle karta hai.
 * - Role-based authorization apply karta hai: sirf ADMIN naye employee add/delete kar sakta hai.
 * - Normal employee sirf apna khud ka profile dekh aur edit kar sakta hai.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SecurityUtils securityUtils;

    public EmployeeController(EmployeeService employeeService, SecurityUtils securityUtils) {
        this.employeeService = employeeService;
        this.securityUtils = securityUtils;
    }

    /**
     * Create / Register a new employee.
     * Accessible by: ADMIN
     * POST /api/employees
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can register new employees.");
        }
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * List all employees, with optional filter by role or department.
     * Accessible by: ADMIN
     * GET /api/employees
     * GET /api/employees?role=TECHNICIAN
     * GET /api/employees?department=IT
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String department) {

        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can view the full employee directory.");
        }

        if (role != null) {
            return ResponseEntity.ok(employeeService.getEmployeesByRole(role));
        }
        if (department != null && !department.isBlank()) {
            return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
        }
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Specialized endpoint to list all technicians.
     * Accessible by: ADMIN, TECHNICIAN
     * GET /api/employees/technicians
     */
    @GetMapping("/technicians")
    public ResponseEntity<List<Employee>> getTechnicians() {
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            throw new AccessDeniedException("Only administrators and technicians can list technician staff.");
        }
        return ResponseEntity.ok(employeeService.getEmployeesByRole(Role.TECHNICIAN));
    }

    /**
     * Get employee details by ID.
     * Accessible by: ADMIN or the employee themselves.
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Non-admins can only view their own profile
        if (!securityUtils.isAdmin() && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied: You can only view your own employee profile.");
        }

        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Update employee details.
     * Accessible by: ADMIN or the employee themselves.
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest updatedData) {
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Non-admins can only update their own profile
        if (!securityUtils.isAdmin() && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied: You can only update your own employee profile.");
        }

        // Non-admins cannot elevate their role
        if (!securityUtils.isAdmin()) {
            updatedData.setRole(currentUser.getRole());
        }

        Employee updated = employeeService.updateEmployee(id, updatedData);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivate an employee (soft delete).
     * Accessible by: ADMIN
     * PATCH /api/employees/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Employee> deactivateEmployee(@PathVariable Long id) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can deactivate employees.");
        }
        Employee deactivated = employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(deactivated);
    }

    /**
     * Reactivate an employee.
     * Accessible by: ADMIN
     * PATCH /api/employees/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Employee> activateEmployee(@PathVariable Long id) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can reactivate employees.");
        }
        Employee activated = employeeService.activateEmployee(id);
        return ResponseEntity.ok(activated);
    }

    /**
     * Soft delete endpoint (REST alternative to deactivate).
     * Accessible by: ADMIN
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable Long id) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can delete or deactivate employees.");
        }
        Employee deactivated = employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(deactivated);
    }
}
