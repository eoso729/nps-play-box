package org.example.signer.config;

import jakarta.annotation.PostConstruct;
import org.example.signer.Utils.XmlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlConfig {

    @Value("${app.xml.output-dir}")
    private String xmlOutputDir;

    @PostConstruct
    public void init() {
        XmlUtils.setOutputDir(xmlOutputDir);
    }
}
