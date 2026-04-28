package org.example.services;

import org.example.entities.Credential;
import org.example.entities.PasswordResetToken;
import org.example.repositories.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository resetTokenRepository;

    public PasswordResetService(PasswordResetTokenRepository resetTokenRepository) {
        this.resetTokenRepository = resetTokenRepository;
    }



    @Transactional
    public String createResetToken(Credential credential) {
        String token = UUID.randomUUID().toString();

        // Token-ul de resetare expiră mult mai repede decat RefreshToken-ul
        PasswordResetToken resetToken = new PasswordResetToken(
                credential,
                token,
                LocalDateTime.now().plusMinutes(30)
        );

        resetTokenRepository.save(resetToken);
        return token;
    }

    @Transactional
    public void useToken(String token){
        resetTokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            resetTokenRepository.save(t);
        });
    }

    public PasswordResetToken findByToken(String token) {
        if(resetTokenRepository.findByToken(token).isPresent())
            return resetTokenRepository.findByToken(token).get();
        else
            return null;
    }
}