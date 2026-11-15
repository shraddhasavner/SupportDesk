package com.supportdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Entry point of the SupportDesk Spring Boot application.
 * 
 * [English]:
 * @SpringBootApplication marks this as the primary configuration class.
 * It combines three annotations:
 * 1. @Configuration: Allows defining beans.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to configure beans automatically based on dependencies.
 * 3. @ComponentScan: Scans packages (controller, service, repository, etc.) for Spring components.
 * 
 * [Hinglish]:
 * Yeh class hamare project ka starting point hai jahan se application run hona start hoti hai.
 * @SpringBootApplication annotation Spring Boot ko batati hai ki project ke saare components
 * (Controllers, Services, Repositories) ko automatically scan kare aur configure kare.
 */
@SpringBootApplication
public class SupportDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportDeskApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" SupportDesk Application Started Successfully!  ");
        System.out.println(" Server running on: http://localhost:8082       ");
        System.out.println("=================================================");
    }

    /**
     * BCrypt password encoder bean.
     * Uses one-way salt hashing so raw passwords are never stored in plain text.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
