package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Receta;
import com.example.odontologia_api.entity.RecetaDetalle;
import com.example.odontologia_api.entity.Usuario;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecetaPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RecetaService recetaService;
    private final SimplePdfService simplePdfService;

    public RecetaPdfService(RecetaService recetaService, SimplePdfService simplePdfService) {
        this.recetaService = recetaService;
        this.simplePdfService = simplePdfService;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long recetaId) {
        Receta receta = recetaService.buscarActiva(recetaId);
        return simplePdfService.crearDocumento(crearLineas(receta));
    }

    private List<String> crearLineas(Receta receta) {
        FichaClinica ficha = receta.getFichaClinica();
        Paciente paciente = ficha.getPaciente();
        Usuario usuario = receta.getUsuario();

        List<String> lineas = new ArrayList<>();
        lineas.add("CLINICAS ORALDENT");
        lineas.add("RECETA ODONTOLOGICA");
        lineas.add("");
        lineas.add("Receta No: " + receta.getId());
        lineas.add("Fecha de emision: " + receta.getFechaCreacion().format(DATE_TIME_FORMAT));
        lineas.add("Ficha clinica: " + ficha.getId());
        lineas.add("");
        lineas.add("DATOS DEL PACIENTE");
        lineas.add("Codigo paciente: " + valor(paciente.getCodigoPaciente()));
        lineas.add("Paciente: " + nombrePaciente(paciente));
        lineas.add("Celular: " + valor(paciente.getCelular()));
        lineas.add("Documento: " + valor(paciente.getDocumentoIdentidad()));
        lineas.add("Fecha de nacimiento: " + (paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT)));
        lineas.add("");
        lineas.add("DATOS CLINICOS");
        lineas.add("Motivo de consulta: " + valor(ficha.getMotivoConsulta()));
        lineas.add("Diagnostico: " + valor(ficha.getDiagnostico()));
        lineas.add("Tratamiento: " + valor(ficha.getTratamiento()));
        lineas.add("");
        lineas.add("MEDICAMENTOS");

        List<RecetaDetalle> detalles = receta.getDetalles().stream()
                .sorted(Comparator.comparing(RecetaDetalle::getOrden, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(detalle -> detalle.getId() == null ? 0L : detalle.getId()))
                .toList();
        if (detalles.isEmpty()) {
            lineas.add("Sin medicamentos registrados.");
        } else {
            int index = 1;
            for (RecetaDetalle detalle : detalles) {
                lineas.add(index + ". " + valor(detalle.getMedicamento()));
                lineas.add("   Dosis: " + valor(detalle.getDosis()));
                lineas.add("   Frecuencia: " + valor(detalle.getFrecuencia()));
                lineas.add("   Duracion: " + valor(detalle.getDuracion()));
                lineas.add("   Indicaciones: " + valor(detalle.getIndicaciones()));
                index++;
            }
        }

        lineas.add("");
        lineas.add("INDICACIONES GENERALES");
        lineas.add(valor(receta.getIndicacionesGenerales()));
        lineas.add("");
        lineas.add("OBSERVACIONES");
        lineas.add(valor(receta.getObservaciones()));
        lineas.add("");
        lineas.add("Emitido por: " + nombreUsuario(usuario));
        lineas.add("");
        lineas.add("________________________________");
        lineas.add("Firma y sello");
        return lineas;
    }

    private String nombrePaciente(Paciente paciente) {
        return (valor(paciente.getNombre()) + " "
                + valor(paciente.getApellidoPaterno()) + " "
                + valor(paciente.getApellidoMaterno())).trim();
    }

    private String nombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return "Sin registrar";
        }
        return (valor(usuario.getNombre()) + " "
                + valor(usuario.getApellidoPaterno()) + " "
                + valor(usuario.getApellidoMaterno())).trim();
    }

    private String valor(String value) {
        if (value == null || value.isBlank()) {
            return "Sin registrar";
        }
        return value.trim();
    }
}
