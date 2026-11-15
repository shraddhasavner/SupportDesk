package com.supportdesk.service;

import com.supportdesk.controller.LoginRequest;
import com.supportdesk.controller.LoginResponse;
import com.supportdesk.entity.Employee;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService: Handles authentication logic for user login and JWT issuance.
 * 
 * [English]:
 * - Validates email and raw password against the BCrypt password hash.
 * - Rejects inactive employees with 401 Unauthorized.
 * - Issues signed JWT without logging or returning sensitive credentials.
 * 
 * [Hinglish]:
 * - User login aur JWT token generate karne ki service.
 * - Password match karta hai aur check karta hai ki user active hai ya nahi.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        Employee employee = employeeRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!employee.isActive()) {
            throw new BadCredentialsException("Account is deactivated. Please contact an administrator.");
        }

        String token = jwtUtil.generateToken(employee);

        return new LoginResponse(
                token,
                "Bearer",
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole()
        );
    }
}
