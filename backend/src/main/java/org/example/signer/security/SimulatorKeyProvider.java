package org.example.signer.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Centralized key provider for simulator pseudo-bank signing and encryption.
 * Holds pre-configured shared keypair (999999.private.pem and NIBSS-999999.public.pem)
 * in memory, eliminating per-institution PKI onboarding friction for tenants.
 */
@Slf4j
@Component
public class SimulatorKeyProvider {

    private final ResourceLoader resourceLoader;

    @Value("${app.keys.private-path:src/main/java/org/example/signer/keys/999999.private.pem}")
    private String privateKeyPath;

    @Value("${app.keys.public-path:src/main/java/org/example/signer/keys/NIBSS-999999.public.pem}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private boolean initialized = false;

    public SimulatorKeyProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        loadKeys();
    }

    public synchronized void loadKeys() {
        try {
            log.info("Initializing SimulatorKeyProvider with privateKeyPath='{}', publicKeyPath='{}'", 
                    privateKeyPath, publicKeyPath);

            this.privateKey = resolvePrivateKey(privateKeyPath);
            this.publicKey = resolvePublicKey(publicKeyPath);

            this.initialized = (this.privateKey != null && this.publicKey != null);
            if (this.initialized) {
                log.info("SimulatorKeyProvider successfully initialized shared simulator keys: algorithm={}", 
                        privateKey.getAlgorithm());
            } else {
                log.warn("SimulatorKeyProvider could not load one or more simulator keys.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize SimulatorKeyProvider: {}", e.getMessage(), e);
        }
    }

    private PrivateKey resolvePrivateKey(String configuredPath) throws IOException {
        InputStream stream = openKeyStream(configuredPath, "999999.private.pem");
        if (stream == null) {
            throw new IOException("Could not locate private key: " + configuredPath);
        }
        try (stream) {
            return Signer.loadPrivateKey(stream);
        }
    }

    private PublicKey resolvePublicKey(String configuredPath) throws IOException {
        InputStream stream = openKeyStream(configuredPath, "NIBSS-999999.public.pem");
        if (stream == null) {
            throw new IOException("Could not locate public key: " + configuredPath);
        }
        try (stream) {
            return Signer.loadPublicKey(stream);
        }
    }

    private InputStream openKeyStream(String configuredPath, String fallbackFilename) {
        // 1. Try spring resource loader with configured path
        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            try {
                Resource resource = resourceLoader.getResource(configuredPath);
                if (resource.exists() && resource.isReadable()) {
                    return resource.getInputStream();
                }
            } catch (Exception ignored) {
            }

            // 2. Try raw file
            File file = new File(configuredPath);
            if (file.exists() && file.canRead()) {
                try {
                    return new FileInputStream(file);
                } catch (Exception ignored) {
                }
            }

            // 3. Try with "backend/" prefix if running from project root
            File backendFile = new File("backend/" + configuredPath);
            if (backendFile.exists() && backendFile.canRead()) {
                try {
                    return new FileInputStream(backendFile);
                } catch (Exception ignored) {
                }
            }

            // 4. Try stripping "backend/" prefix if running from backend folder
            if (configuredPath.startsWith("backend/")) {
                File strippedFile = new File(configuredPath.substring("backend/".length()));
                if (strippedFile.exists() && strippedFile.canRead()) {
                    try {
                        return new FileInputStream(strippedFile);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 5. Try classpath fallbacks
        String[] classpathCandidates = {
            "classpath:keys/" + fallbackFilename,
            "classpath:" + fallbackFilename,
            "/keys/" + fallbackFilename,
            "/" + fallbackFilename
        };

        for (String candidate : classpathCandidates) {
            try {
                if (candidate.startsWith("classpath:")) {
                    Resource resource = resourceLoader.getResource(candidate);
                    if (resource.exists() && resource.isReadable()) {
                        return resource.getInputStream();
                    }
                } else {
                    InputStream is = getClass().getResourceAsStream(candidate);
                    if (is != null) {
                        return is;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 6. Direct filesystem fallback search
        String[] fileCandidates = {
            "src/main/resources/keys/" + fallbackFilename,
            "backend/src/main/resources/keys/" + fallbackFilename,
            "src/main/java/org/example/signer/keys/" + fallbackFilename,
            "backend/src/main/java/org/example/signer/keys/" + fallbackFilename
        };

        for (String candidate : fileCandidates) {
            File f = new File(candidate);
            if (f.exists() && f.canRead()) {
                try {
                    return new FileInputStream(f);
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new IllegalStateException("Simulator private key is not initialized");
        }
        return privateKey;
    }

    public PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("Simulator public key is not initialized");
        }
        return publicKey;
    }

    public boolean isInitialized() {
        return initialized && privateKey != null && publicKey != null;
    }

    public String getKeyIdentifier() {
        return "NIBSS-999999";
    }

    public void sign(Document doc) throws Exception {
        Signer.sign(doc, getPrivateKey());
    }

    public void encrypt(Document doc, String tagName) throws Exception {
        Encrypter.encrypt(doc, getPublicKey(), tagName);
    }

    public String signXml(String xml) throws Exception {
        Document doc = XmlUtils.stringToDocument(xml);
        sign(doc);
        return XmlUtils.documentToString(doc);
    }

    public String encryptXml(String xml, String tagName) throws Exception {
        Document doc = XmlUtils.stringToDocument(xml);
        encrypt(doc, tagName);
        return XmlUtils.documentToString(doc);
    }

    public String signAndEncryptXml(String xml, String tagName) throws Exception {
        Document doc = XmlUtils.stringToDocument(xml);
        sign(doc);
        if (tagName != null && !tagName.trim().isEmpty()) {
            encrypt(doc, tagName);
        }
        return XmlUtils.documentToString(doc);
    }
}
