package org.example.signer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "NPS Request Simulator",
                version = "1.0",
                description = "Documentation for my NPS Request Simulator"
        )
)
public class NpsRequestSimulator {

    private static final Logger log = LoggerFactory.getLogger(NpsRequestSimulator.class);

    public static void main(String[] args) {
        SpringApplication.run(NpsRequestSimulator.class, args);
        log.info("This is finally running ");
    }
}
