package com.supportdesk.config;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DevelopmentDataInitializer: Automatically seeds initial test accounts
 * (Admin, Technician, Staff) for local development environments.
 * 
 * [English]:
 * - Activated ONLY when property 'app.init.test-data.enabled=true' is present and active profile is NOT 'prod'.
 * - Idempotent: Checks if accounts already exist before inserting; never overwrites existing accounts.
 * - Passwords are encrypted with BCrypt; never stored in plaintext or logged.
 * - Does not print passwords, hashes, or database secrets.
 * - Safe for production: disabled by default (matchIfMissing = false) and strictly excluded in prod profile.
 * 
 * [Hinglish]:
 * - Local development ke liye initial test accounts (Admin, Tech, Staff) create karta hai.
 * - Production profile me strictly disable rehta hai.
 * - Agar account pehle se database me hai, to use overwrite nahi karta.
 * - Password ko BCrypt se encrypt karke save karta hai.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.init.test-data.enabled", havingValue = "true", matchIfMissing = false)
public class DevelopmentDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentDataInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentDataInitializer(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Checking development test accounts initialization (app.init.test-data.enabled=true)...");

        seedAccountIfNotExists(
                "Admin Main",
                "admin.main@supportdesk.com",
                "Pass123",
                "IT Administration",
                Role.ADMIN,
                "9870000001"
        );

        seedAccountIfNotExists(
                "Tech User",
                "tech.user@supportdesk.com",
                "Pass123",
                "IT Support",
                Role.TECHNICIAN,
                "9870000002"
        );

        seedAccountIfNotExists(
                "Staff User",
                "staff.user@supportdesk.com",
                "Pass123",
                "Operations",
                Role.EMPLOYEE,
                "9870000003"
        );

        logger.info("Development test data verification complete.");
    }

    /**
     * Seeds an employee account only if no account exists with the given email.
     * Preserves existing accounts without modifying their state or password.
     */
    @Transactional
    public Employee seedAccountIfNotExists(String name, String email, String rawPassword, String department, Role role, String phone) {
        String normalizedEmail = email.trim().toLowerCase();

        if (employeeRepository.existsByEmail(normalizedEmail)) {
            logger.info("Development account '{}' already exists. Preserving existing record without modification.", normalizedEmail);
            return employeeRepository.findByEmail(normalizedEmail).orElse(null);
        }

        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(normalizedEmail);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setDepartment(department);
        employee.setRole(role);
        employee.setPhone(phone);
        employee.setActive(true);

        Employee saved = employeeRepository.save(employee);
        logger.info("Successfully initialized development account: email={}, role={}", normalizedEmail, role);
        return saved;
    }
}
