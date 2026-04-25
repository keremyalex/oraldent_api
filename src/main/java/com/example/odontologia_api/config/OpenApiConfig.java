package com.example.odontologia_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Consultorio Odontologico",
                version = "1.0.0",
                description = "API REST para la gestion de pacientes, citas, horarios y perfil del doctor.",
                contact = @Contact(
                        name = "Equipo Odontologia"
                ),
                license = @License(
                        name = "Uso academico"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor local")
        }
)
public class OpenApiConfig {
}
