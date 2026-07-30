package org.example.signer.Utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlUtils {

    private static String outputDir = "src/main/java/org/example/signer/xmlOutput";

    public static void setOutputDir(String dir) {
        outputDir = dir;
    }

    public static Document marshalToDocument(Object jaxbObject) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbObject.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(jaxbObject, sw);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        try (StringReader sr = new StringReader(sw.toString())) {
            return db.parse(new InputSource(sr));
        }
    }

    private static void saveDocument(Document doc, String filePath) throws Exception {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        try (OutputStream os = new FileOutputStream(filePath)) {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        }
    }

    public static String documentToString(Document doc) throws Exception {
        try (StringWriter sw = new StringWriter()) {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        }
    }

    public static Document stringToDocument(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringReader stringReader = new StringReader(xmlString);
        InputSource inputSource = new InputSource(stringReader);
        return builder.parse(inputSource);
    }

    public static void writeDocumentToFile(Document doc, String filename) throws Exception {
        saveDocument(doc, outputDir + "/" + filename);
    }
}
