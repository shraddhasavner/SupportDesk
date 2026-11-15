package com.supportdesk.repository;

import com.supportdesk.controller.TechnicianWorkloadResponse;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * EmployeeRepository: Data Access Layer for the Employee entity.
 * 
 * [English]:
 * Extends Spring Data JPA's JpaRepository interface.
 * JpaRepository provides default implementations for:
 * - save(entity): Inserts or updates an employee.
 * - findById(id): Finds an employee by Primary Key.
 * - findAll(): Retrieves all employees.
 * - deleteById(id): Deletes an employee by ID.
 * - count(): Counts total employees.
 * 
 * Spring Data JPA also automatically generates SQL for custom method names (Derived Query Methods).
 * 
 * [Hinglish]:
 * Yeh interface Spring Data JPA ka `JpaRepository` extend karta hai.
 * Hume SQL likhne ki zaroorat nahi hoti; method ke naam ke hisab se Spring Boot khud SQL query bana leta hai.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find an employee by their unique email (used for login and verification).
     * Returns Optional to safely handle cases where the email is not found.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Find all employees belonging to a specific role (e.g., all TECHNICIANs for ticket assignment).
     */
    List<Employee> findByRole(Role role);

    /**
     * Find all employees belonging to a specific department (e.g., "IT", "HR").
     */
    List<Employee> findByDepartment(String department);

    /**
     * Check if an email already exists (used during registration to prevent duplicates).
     */
    boolean existsByEmail(String email);

    /**
     * Find all active employees by role with pessimistic write lock.
     * Prevents race conditions during administrative account deactivation / demotion.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.role = :role AND e.active = true")
    List<Employee> findActiveEmployeesByRoleForUpdate(@Param("role") Role role);

    /**
     * Efficiently calculates workload metrics for all active technicians using a single aggregated query.
     * Replaces the previous N+1 query pattern by joining technicians to their assigned tickets and
     * aggregating assigned, pending (ASSIGNED/IN_PROGRESS), and resolved (RESOLVED/CLOSED) counts in one pass.
     */
    @Query("SELECT new com.supportdesk.controller.TechnicianWorkloadResponse(" +
            "e.id, e.name, e.department, " +
            "COUNT(t), " +
            "COALESCE(SUM(CASE WHEN t.status IN (com.supportdesk.entity.TicketStatus.ASSIGNED, com.supportdesk.entity.TicketStatus.IN_PROGRESS) THEN 1L ELSE 0L END), 0L), " +
            "COALESCE(SUM(CASE WHEN t.status IN (com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED) THEN 1L ELSE 0L END), 0L)) " +
            "FROM Employee e LEFT JOIN Ticket t ON t.assignedTechnician = e " +
            "WHERE e.role = com.supportdesk.entity.Role.TECHNICIAN AND e.active = true " +
            "GROUP BY e.id, e.name, e.department " +
            "ORDER BY e.id ASC")
    List<TechnicianWorkloadResponse> findActiveTechnicianWorkloads();
}
