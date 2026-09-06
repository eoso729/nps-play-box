package org.example.signer.Utils;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.w3c.dom.Document;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Collections;

public final class Signer {

    static {
        Security.addProvider(new BouncyCastleProvider());
        org.apache.xml.security.Init.init();
    }

    private Signer() {
        // Private constructor to prevent instantiation of this utility class.
    }

    public static void sign(Document doc, PrivateKey privateKey) throws Exception {
        XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");

        Reference ref = sigFactory.newReference(
                "",
                sigFactory.newDigestMethod(DigestMethod.SHA256, null),
                Collections.singletonList(
                    sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                ),
                null,
                null
        );

        SignedInfo signedInfo = sigFactory.newSignedInfo(
                sigFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                Collections.singletonList(ref)
        );

        DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
        XMLSignature signature = sigFactory.newXMLSignature(signedInfo, null);
        signature.sign(dsc);
    }

    public static PrivateKey loadPrivateKey(String pemFilePath) throws IOException {
        try (Reader reader = new FileReader(pemFilePath)) {
            return loadPrivateKey(reader);
        }
    }

    public static PrivateKey loadPrivateKey(InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return loadPrivateKey(reader);
        }
    }

    public static PrivateKey loadPrivateKey(Reader reader) throws IOException {
        try (PEMParser pemParser = new PEMParser(reader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) object);
            }
            throw new IOException("PEM input does not contain a private key of type PrivateKeyInfo.");
        }
    }

    public static PublicKey loadPublicKey(String pemFilePath) throws IOException {
        try (Reader reader = new FileReader(pemFilePath)) {
            return loadPublicKey(reader);
        }
    }

    public static PublicKey loadPublicKey(InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return loadPublicKey(reader);
        }
    }

    public static PublicKey loadPublicKey(Reader reader) throws IOException {
        try (PEMParser pemParser = new PEMParser(reader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof SubjectPublicKeyInfo) {
                return converter.getPublicKey((SubjectPublicKeyInfo) object);
            }
            throw new IOException("PEM input does not contain a public key of type SubjectPublicKeyInfo.");
        }
    }
}