package org.example.signer.security;

import org.example.signer.Utils.XmlUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.w3c.dom.Document;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorKeyProviderTest {

    private SimulatorKeyProvider keyProvider;

    @BeforeEach
    void setUp() {
        keyProvider = new SimulatorKeyProvider(new DefaultResourceLoader());
        keyProvider.init();
    }

    @Test
    @DisplayName("Should initialize and load shared simulator keys")
    void testInitialization() {
        assertTrue(keyProvider.isInitialized(), "SimulatorKeyProvider should be initialized");
        assertNotNull(keyProvider.getPrivateKey(), "Private key must not be null");
        assertNotNull(keyProvider.getPublicKey(), "Public key must not be null");
        assertEquals("RSA", keyProvider.getPrivateKey().getAlgorithm());
        assertEquals("RSA", keyProvider.getPublicKey().getAlgorithm());
        assertEquals("NIBSS-999999", keyProvider.getKeyIdentifier());
    }

    @Test
    @DisplayName("Should sign XML document using simulator private key")
    void testSignXml() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Header>Test</Header></TestMessage>";
        String signedXml = keyProvider.signXml(xml);

        assertNotNull(signedXml);
        assertTrue(signedXml.contains("Signature"), "Signed XML must contain a Signature element");
        assertTrue(signedXml.contains("SignedInfo"), "Signed XML must contain SignedInfo");
    }

    @Test
    @DisplayName("Should sign and encrypt XML document")
    void testSignAndEncryptXml() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Header>Test</Header><Body>Secret</Body></TestMessage>";
        String resultXml = keyProvider.signAndEncryptXml(xml, "Body");

        assertNotNull(resultXml);
        assertTrue(resultXml.contains("Signature"), "Result XML must contain Signature");
        assertTrue(resultXml.contains("EncryptedData"), "Result XML must contain EncryptedData");
        assertFalse(resultXml.contains("<Body>Secret</Body>"), "Target element must be encrypted");
    }
}
