package com.supportdesk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Employee Entity: Maps directly to the 'employees' table in MySQL.
 * 
 * [English]:
 * @Entity: Tells JPA/Hibernate that this class represents a database table.
 * @Table(name = "employees"): Names the database table 'employees'.
 * @Id: Specifies the primary key of the table.
 * @GeneratedValue(strategy = GenerationType.IDENTITY): Uses MySQL AUTO_INCREMENT.
 * @Enumerated(EnumType.STRING): Saves the enum name (e.g. 'EMPLOYEE') as text rather than ordinal number.
 * @JsonProperty(access = WRITE_ONLY): Accepts password in POST/PUT requests, but NEVER exposes it in JSON responses.
 * 
 * [Hinglish]:
 * @Entity: Hibernate ko batata hai ki is class ke naam par MySQL me table banani hai.
 * @Table(name = "employees"): Database table ka naam 'employees' set karta hai.
 * @Id: Is field ko primary key banata hai.
 * @GeneratedValue: MySQL ko bolta hai ki ID apne aap 1, 2, 3... badhata jaye (auto-increment).
 * @Enumerated(EnumType.STRING): Role ko number (0, 1) ke bajaye text ("EMPLOYEE", "ADMIN") me save karta hai.
 * @JsonProperty(access = WRITE_ONLY): Password ko request me leta hai par API response me kabhi bahar nahi bhejta.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String department;

    @NotNull(message = "Role is required (EMPLOYEE, TECHNICIAN, ADMIN)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA/Hibernate.
     */
    public Employee() {
    }

    /**
     * Parameterized constructor for easy object creation.
     */
    public Employee(String name, String email, String password, String department, Role role, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.department = department;
        this.role = role;
        this.phone = phone;
        this.active = true;
    }

    /**
     * Automatically sets createdAt before saving to the database for the first time.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", role=" + role +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
