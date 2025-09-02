package com.example.ticketing.system;

import com.example.ticketing.system.entity.Role;
import com.example.ticketing.system.entity.User;
import com.example.ticketing.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if no users exist
        if (userRepository.count() == 0) {
            User admin = new User(
                    "admin",
                    "admin@ticketing.com",
                    passwordEncoder.encode("admin123"),
                    "System",
                    "Administrator",
                    Role.ADMIN
            );
            userRepository.save(admin);

            User supportAgent = new User(
                    "support",
                    "support@ticketing.com",
                    passwordEncoder.encode("support123"),
                    "Support",
                    "Agent",
                    Role.SUPPORT_AGENT
            );
            userRepository.save(supportAgent);

            User testUser = new User(
                    "user",
                    "user@ticketing.com",
                    passwordEncoder.encode("user123"),
                    "Test",
                    "User",
                    Role.USER
            );
            userRepository.save(testUser);

            System.out.println("Default users created:");
            System.out.println("Admin: admin / admin123");
            System.out.println("Support Agent: support / support123");
            System.out.println("User: user / user123");
        }
    }
}