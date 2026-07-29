package org.example.signer.Utils;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.PublicKey;

public final class Encrypter {

    static {
        org.apache.xml.security.Init.init();
    }

    private Encrypter() {
        // Prevent instantiation
    }

    /**
     * Encrypts the first matching element with the given tag name.
     *
     * @param doc     the XML Document
     * @param pubKey  the RSA public key used for key wrapping
     * @param tagName the element tag name to encrypt
     */
    public static void encrypt(Document doc, PublicKey pubKey, String tagName) throws Exception {

        // Generate a session (AES) key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey sessionKey = keyGen.generateKey();

        // Prepare key cipher (RSA) to wrap AES key
        XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
        keyCipher.init(XMLCipher.WRAP_MODE, pubKey);
        EncryptedKey encryptedKey = keyCipher.encryptKey(doc, sessionKey);

        // Prepare data cipher (AES)
        XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_256_GCM);
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, sessionKey);

        // Add the encrypted AES key to <EncryptedData>
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
        KeyInfo keyInfo = new KeyInfo(doc);
        keyInfo.add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);

        // Find the first element with this tag
        NodeList nodes = doc.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            throw new Exception("No <" + tagName + "> element found to encrypt.");
        }

        Element element = (Element) nodes.item(0);
        xmlCipher.doFinal(doc, element, true);
    }
}