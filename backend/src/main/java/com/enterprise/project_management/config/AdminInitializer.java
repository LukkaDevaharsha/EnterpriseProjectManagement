package com.enterprise.project_management.config;

import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElse(null);

        if (admin == null) {

            admin = new User();

            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(
                    passwordEncoder.encode(adminPassword)
            );

            admin.setEmployeeId("ADMIN-001");
            admin.setDepartment("Administration");

            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailVerified(true);

            userRepository.save(admin);

            System.out.println("Default admin account created.");

        }
    }
}
