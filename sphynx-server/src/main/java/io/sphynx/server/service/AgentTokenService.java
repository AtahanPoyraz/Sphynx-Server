package io.sphynx.server.service;

import io.sphynx.server.model.AgentModel;
import io.sphynx.server.repository.AgentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class AgentTokenService {
    @Value("${security.hmac.secret_key}")
    private String hmacSecretKey;

    @Value("${security.aes.secret_key}")
    private String aesSecretKey;

    private final AgentRepository agentRepository;

    @Autowired
    public AgentTokenService(
            AgentRepository agentRepository
    ) {
        this.agentRepository = agentRepository;
    }

    public String generateAgentToken(UUID agentId) {
        String id = agentId.toString();
        String signature = generateSignature(id);
        return this.encrypt(id + ":" + signature);
    }

    public boolean isAgentTokenValid(String agentToken) {
        try {
            String decrypted = this.decrypt(agentToken);
            String[] parts = decrypted.split(":");
            if (parts.length != 2) {
                return false;
            }

            String agentId = parts[0];
            String signature = parts[1];

            String expectedSignature = this.generateSignature(agentId);

            return MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes());

        } catch (Exception e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public AgentModel extractAgentFromToken(String agentToken) {
        String decrypted = this.decrypt(agentToken);
        String[] parts = decrypted.split(":");
        if (parts.length != 2) {
            return null;
        }

        String agentId = parts[0];
        String signature = parts[1];

        String expectedSignature = generateSignature(agentId);
        if (!MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes())) {
            return null;
        }

        UUID uuid = UUID.fromString(agentId);
        return this.agentRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found"));
    }

    private String generateSignature(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hmacSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (Exception e) {
            throw new RuntimeException("HMAC generation error: " + e.getMessage(), e);
        }
    }

    private String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] iv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = new SecretKeySpec(this.aesSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encryptedBytes.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("AES encryption error: " + e.getMessage(), e);
        }
    }

    private String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(aesSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedBytes);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES decryption error: " + e.getMessage(), e);
        }
    }
}
