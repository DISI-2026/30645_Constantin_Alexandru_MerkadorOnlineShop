package org.example.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.example.services.UserService;
import org.example.repositories.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    public AdminSeeder(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Check if an admin user already exists in the database
        long userCount = userRepository.count();

        if (userCount == 0) {
            System.out.println("Database is empty. Seeding admin user...");

            // Build the admin user
            String fullJsonPayload = new String(
                    "{\n" +
                            "    \"fullName\": \"Admin\",\n" +
                            "    \"address\": \"-\",\n" +
                            "    \"email\": \"admin@example.com\",\n" +
                            "    \"username\": \"admin\",\n" +
                            "    \"password\": \"admin\",\n" +
                            "    \"role\": \"ADMIN\"\n" +
                            "}"
            );

            // Save the admin user
            userService.create(fullJsonPayload);

            System.out.println("Default admin created successfully.");
        } else {
            System.out.println("Existing users found. Skipping admin seeding.");
        }
    }
}