package com.example.odontologia_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.odontologia_api.dto.AuthResponse;
import com.example.odontologia_api.dto.LoginRequest;
import com.example.odontologia_api.dto.RegisterPacienteRequest;
import com.example.odontologia_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldRegisterAndLoginPaciente() {
        AuthResponse registerResponse = authService.registerPaciente(new RegisterPacienteRequest(
                "Ana",
                "Prueba",
                "Login",
                "ana.integration@test.com",
                "70098765",
                "clave1234",
                "1234567",
                java.time.LocalDate.of(2000, 1, 1),
                "Zona Centro",
                null
        ));

        assertNotNull(registerResponse.token());
        assertEquals("PACIENTE", registerResponse.usuario().rol().name());

        AuthResponse loginResponse = authService.login(new LoginRequest(
                "ana.integration@test.com",
                "clave1234"
        ));

        assertNotNull(loginResponse.token());
        assertEquals(registerResponse.usuario().id(), loginResponse.usuario().id());
    }
}
