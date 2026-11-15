package com.supportdesk.security;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * UserPrincipal: Bridges our Employee entity with Spring Security's UserDetails model.
 * 
 * [English]:
 * - Implements UserDetails so Spring Security can authenticate and authorize employees.
 * - Attaches granted authority "ROLE_" + role.name() (e.g., ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_TECHNICIAN).
 * - Enforces account active flag via isEnabled().
 * 
 * [Hinglish]:
 * - Spring Security ko hamare Employee object ke sath connect karne wali class.
 * - Isme employee ka email, ID, name aur role rehta hai.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String name, String email, String password, Role role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static UserPrincipal create(Employee employee) {
        return new UserPrincipal(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPassword(),
                employee.getRole(),
                employee.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
