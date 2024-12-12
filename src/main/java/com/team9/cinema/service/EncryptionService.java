package com.team9.cinema.service;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private final TextEncryptor encryptor;

    public EncryptionService() {
        String encryptionPassword = "securePassword"; // replaced with secure password
        String encryptionSalt = "1234567890abcdef"; // 16 character hex string
        this.encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
    }

    public String encrypt(String data) {
        return encryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return encryptor.decrypt(encryptedData);
    }
}
