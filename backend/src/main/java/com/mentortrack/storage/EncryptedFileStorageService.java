package com.mentortrack.storage;

import com.mentortrack.config.StorageProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Stores uploaded documents (marksheets, Aadhaar/PAN, etc.) encrypted at rest on local disk using
 * AES-CBC. The 16-byte IV is prepended to each stored file so decryption is self-contained.
 *
 * This satisfies the spec's "Local encrypted storage for uploaded documents" requirement using
 * only the JDK's built-in javax.crypto — no external dependency needed.
 */
@Service
public class EncryptedFileStorageService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private final Path rootDir;
    private final SecretKeySpec key;

    public EncryptedFileStorageService(StorageProperties storageProperties) throws IOException {
        this.rootDir = Path.of(storageProperties.getRootDir());
        Files.createDirectories(rootDir);
        this.key = deriveKey(storageProperties.getEncryptionKey());
    }

    private SecretKeySpec deriveKey(String rawKey) {
        byte[] bytes = rawKey.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[32]; // AES-256
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] = bytes[i % bytes.length];
        }
        return new SecretKeySpec(normalized, "AES");
    }

    /** Encrypts and stores the given file content, returning the relative storage path (safe to persist in the DB). */
    public String store(String studentRegNo, String documentType, InputStream content) throws IOException {
        String relativePath = studentRegNo + "/" + documentType + "-" + UUID.randomUUID() + ".enc";
        Path target = rootDir.resolve(relativePath);
        Files.createDirectories(target.getParent());

        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            try (OutputStream fileOut = Files.newOutputStream(target)) {
                fileOut.write(iv);
                try (CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
                    content.transferTo(cipherOut);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to encrypt/store file", e);
        }
        return relativePath;
    }

    /** Decrypts and returns the content of a previously stored file. */
    public InputStream retrieve(String relativePath) throws IOException {
        Path source = rootDir.resolve(relativePath);
        InputStream fileIn = Files.newInputStream(source);
        byte[] iv = new byte[IV_LENGTH];
        int read = fileIn.read(iv);
        if (read != IV_LENGTH) {
            throw new IOException("Corrupt encrypted file (missing IV): " + relativePath);
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return new CipherInputStream(fileIn, cipher);
        } catch (Exception e) {
            throw new IOException("Failed to decrypt file", e);
        }
    }
}
