package com.example.hgh_assessment_prabesh_bhattarai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hgnOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("HGN SOS Alert Intake & Dispatch API")
                .version("v1")
                .description("Backend for SOS alert intake and dispatch coordination: "
                        + "device registration, orders and trekkers, time-bound device "
                        + "assignments, and alert ingest, dedup, claiming and escalation."));
    }
}
