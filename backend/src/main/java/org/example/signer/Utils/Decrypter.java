package org.example.signer.Utils;

import org.apache.xml.security.encryption.XMLCipher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;

public final class Decrypter {

    static {
        org.apache.xml.security.Init.init();
    }

    private Decrypter() {
        // Private constructor to prevent instantiation of this utility class.
    }

    public static String decrypt(String encryptedXml, PrivateKey privateKey) throws Exception {
        // Parse the encrypted XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(encryptedXml.getBytes()));

        // Find the encrypted data element
        Element encryptedDataElement = (Element) doc.getElementsByTagNameNS(
                "http://www.w3.org/2001/04/xmlenc#", "EncryptedData").item(0);
        
        if (encryptedDataElement == null) {
            // If no encrypted data found, return original content
            return encryptedXml;
        }

        // Create XMLCipher for decryption
        XMLCipher xmlCipher = XMLCipher.getInstance();
        xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
        xmlCipher.setKEK(privateKey);

        // Decrypt the document
        xmlCipher.doFinal(doc, encryptedDataElement);

        // Convert back to string
        return documentToString(doc);
    }

    private static String documentToString(Document doc) throws Exception {
        javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
        javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }
}