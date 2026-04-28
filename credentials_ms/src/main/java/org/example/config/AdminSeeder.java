package org.example.config;

import org.example.dtos.RegisterReqDTO;
import org.example.entities.AccountStatus;
import org.example.entities.Credential;
import org.example.repositories.CredentialRepository;
import org.example.services.UserSyncProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSeeder.class);

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSyncProducer userSyncProducer;

    public AdminSeeder(CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       UserSyncProducer userSyncProducer) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSyncProducer = userSyncProducer;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String adminEmail = "admin@example.com";

        // Verificăm dacă adminul există deja
        if (credentialRepository.findByEmail(adminEmail).isEmpty()) {
            LOGGER.info("Admin not found. Seeding default admin user...");

            //Creăm entitatea direct cu status ACTIVE și parola encodată
            Credential admin = new Credential(
                    adminEmail,
                    passwordEncoder.encode("admin"),
                    AccountStatus.ACTIVE
            );

            // Are rol exclusiv de admin
            admin.addRole("ADMIN");

            admin = credentialRepository.save(admin);

            // 4. Creăm un "Dummy DTO" pentru a-l trimite pe RabbitMQ,
            // astfel încât User-MS să îi creeze un profil de utilizator.
            RegisterReqDTO adminProfileInfo = new RegisterReqDTO(
                    adminEmail,
                    "admin",
                    "Super",
                    "Admin");

            try {
                // 5. Trimitem evenimentul către User-MS
                userSyncProducer.sendUserCreatedEvent(admin.getId(), adminProfileInfo);
                LOGGER.info("Default admin created successfully with ID: {}", admin.getId());
            } catch (Exception e) {
                LOGGER.error("Failed to sync admin to User MS. RabbitMQ might not be fully up yet.", e);
                // Nu dăm throw pentru a nu crăpa complet microserviciul la pornire
            }
        } else {
            LOGGER.info("Existing admin found. Skipping admin seeding.");
        }
    }
}