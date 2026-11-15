package com.supportdesk.controller;

import com.supportdesk.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * UpdateEmployeeRequest: DTO payload for updating existing employee profiles.
 * 
 * [English]:
 * - Allows updating profile fields (name, email, department, role, phone).
 * - Password is OPTIONAL on update:
 *   * If null, omitted, or blank (""), the existing encoded password hash is preserved untouched.
 *   * If provided and non-blank, it must be at least 6 characters and will be encoded with BCrypt.
 * 
 * [Hinglish]:
 * - Employee details update karne ke liye request DTO.
 * - Password update ke waqt optional hai; agar blank ya null ho toh purana password safely preserve rehta hai.
 */
public class UpdateEmployeeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department must not exceed 50 characters")
    private String department;

    @NotNull(message = "Role is required (EMPLOYEE, TECHNICIAN, ADMIN)")
    private Role role;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Pattern(regexp = "^$|^.{6,}$", message = "Password must be at least 6 characters")
    private String password;

    public UpdateEmployeeRequest() {
    }

    public UpdateEmployeeRequest(String name, String email, String department, Role role, String phone, String password) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.phone = phone;
        setPassword(password);
    }

    public UpdateEmployeeRequest(String name, String email, String department, Role role, String phone) {
        this(name, email, department, role, phone, null);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && password.isBlank()) {
            this.password = "";
        } else {
            this.password = password;
        }
    }
}
