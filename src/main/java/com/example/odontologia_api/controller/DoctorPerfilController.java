package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.DoctorPerfilRequest;
import com.example.odontologia_api.dto.DoctorPerfilResponse;
import com.example.odontologia_api.service.DoctorPerfilService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/doctor/perfiles")
public class DoctorPerfilController {

    private final DoctorPerfilService doctorPerfilService;

    public DoctorPerfilController(DoctorPerfilService doctorPerfilService) {
        this.doctorPerfilService = doctorPerfilService;
    }

    @GetMapping
    public List<DoctorPerfilResponse> listar() {
        return doctorPerfilService.listar();
    }

    @GetMapping("/{id}")
    public DoctorPerfilResponse obtener(@PathVariable Long id) {
        return doctorPerfilService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorPerfilResponse crear(@Valid @RequestBody DoctorPerfilRequest request) {
        return doctorPerfilService.crear(request);
    }

    @PutMapping("/{id}")
    public DoctorPerfilResponse actualizar(@PathVariable Long id, @Valid @RequestBody DoctorPerfilRequest request) {
        return doctorPerfilService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        doctorPerfilService.desactivar(id);
    }
}
