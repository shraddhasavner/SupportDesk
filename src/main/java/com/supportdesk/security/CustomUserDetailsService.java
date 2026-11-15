package com.supportdesk.security;

import com.supportdesk.entity.Employee;
import com.supportdesk.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService: Loads user credentials and roles from the database using email.
 * 
 * [English]:
 * Implements Spring Security's UserDetailsService interface.
 * 
 * [Hinglish]:
 * Database se email ke basis par employee ko search karke Spring Security ko return karta hai.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));

        return UserPrincipal.create(employee);
    }
}
