package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.AnalisisRadiografia;
import com.example.odontologia_api.entity.Radiografia;
import com.example.odontologia_api.enums.EstadoAnalisisRadiografia;
import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RadiografiaIaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RadiografiaIaService(
            @Value("${ai.api-url:http://localhost:8000}") String aiApiUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(aiApiUrl.endsWith("/") ? aiApiUrl.substring(0, aiApiUrl.length() - 1) : aiApiUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public AnalisisRadiografia analizar(Radiografia radiografia) {
        if (radiografia.getImagenUrl() == null || radiografia.getImagenUrl().isBlank()) {
            throw new ReglaNegocioException("La radiografia no tiene una imagen para analizar.");
        }

        Map<String, Object> resultado = solicitarAnalisis(radiografia);
        AnalisisRadiografia analisis = new AnalisisRadiografia();
        analisis.setRadiografia(radiografia);
        analisis.setModelo("perdida-osea-yolo-pose");
        analisis.setEstado(EstadoAnalisisRadiografia.COMPLETADO);
        analisis.setResultadoJson(toJson(resultado));

        BigDecimal porcentaje = porcentajeMaximo(resultado);
        analisis.setPorcentajePerdidaOsea(porcentaje);
        analisis.setPerdidaOseaDetectada(porcentaje != null && porcentaje.compareTo(BigDecimal.TEN) >= 0);
        analisis.setSeveridad(severidadGlobal(resultado, porcentaje));
        analisis.setTipoPerdidaOsea(tipoGlobal(resultado));
        analisis.setConfianza(confianza(resultado));
        analisis.setRecomendacion(recomendacion(analisis));
        return analisis;
    }

    private Map<String, Object> solicitarAnalisis(Radiografia radiografia) {
        byte[] imagen;
        try {
            imagen = RestClient.create()
                    .get()
                    .uri(radiografia.getImagenUrl())
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("No se pudo descargar la imagen de la radiografia para analizarla.");
        }

        if (imagen == null || imagen.length == 0) {
            throw new ReglaNegocioException("La imagen de la radiografia esta vacia.");
        }

        String filename = radiografia.getNombreArchivo() == null || radiografia.getNombreArchivo().isBlank()
                ? "radiografia.jpg"
                : radiografia.getNombreArchivo();

        ByteArrayResource resource = new ByteArrayResource(imagen) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient
                    .post()
                    .uri("/api/vision/bone-loss")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new ReglaNegocioException("La IA no devolvio resultado para la radiografia.");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("No se pudo analizar la radiografia con IA.");
        }
    }

    private BigDecimal porcentajeMaximo(Map<String, Object> resultado) {
        Object teethValue = resultado.get("teeth");
        if (!(teethValue instanceof List<?> teeth)) {
            return null;
        }
        Double max = null;
        for (Object value : teeth) {
            if (!(value instanceof Map<?, ?> tooth)) {
                continue;
            }
            Object pctValue = tooth.get("bone_loss_avg_pct");
            if (!(pctValue instanceof Number number)) {
                continue;
            }
            double pct = number.doubleValue();
            max = max == null ? pct : Math.max(max, pct);
        }
        return max == null ? null : BigDecimal.valueOf(max).setScale(2, RoundingMode.HALF_UP);
    }

    private SeveridadPerdidaOsea severidadGlobal(Map<String, Object> resultado, BigDecimal porcentaje) {
        Object teethValue = resultado.get("teeth");
        String worst = null;
        if (teethValue instanceof List<?> teeth) {
            for (Object value : teeth) {
                if (value instanceof Map<?, ?> tooth && tooth.get("severity") instanceof String severity) {
                    worst = maxSeverity(worst, severity);
                }
            }
        }
        if (worst != null) {
            return mapSeverity(worst);
        }
        if (porcentaje == null) {
            return SeveridadPerdidaOsea.NO_EVALUABLE;
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(15)) < 0) {
            return SeveridadPerdidaOsea.LEVE;
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(33)) < 0) {
            return SeveridadPerdidaOsea.MODERADA;
        }
        return SeveridadPerdidaOsea.SEVERA;
    }

    private String maxSeverity(String current, String next) {
        if (current == null) {
            return next;
        }
        return severityRank(next) > severityRank(current) ? next : current;
    }

    private int severityRank(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "severa" -> 3;
            case "moderada" -> 2;
            case "leve" -> 1;
            default -> 0;
        };
    }

    private SeveridadPerdidaOsea mapSeverity(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "leve" -> SeveridadPerdidaOsea.LEVE;
            case "moderada" -> SeveridadPerdidaOsea.MODERADA;
            case "severa" -> SeveridadPerdidaOsea.SEVERA;
            default -> SeveridadPerdidaOsea.NO_EVALUABLE;
        };
    }

    private TipoPerdidaOsea tipoGlobal(Map<String, Object> resultado) {
        Object classification = resultado.get("global_classification");
        if (classification instanceof Map<?, ?> map && map.get("type") instanceof String type) {
            String normalized = type.toLowerCase(Locale.ROOT);
            if (normalized.contains("vertical")) {
                return TipoPerdidaOsea.VERTICAL;
            }
            if (normalized.contains("horizontal")) {
                return TipoPerdidaOsea.HORIZONTAL;
            }
            if (normalized.contains("sin pérdida") || normalized.contains("sin perdida")) {
                return TipoPerdidaOsea.NO_EVALUABLE;
            }
        }
        return TipoPerdidaOsea.NO_EVALUABLE;
    }

    private BigDecimal confianza(Map<String, Object> resultado) {
        Number nTeeth = resultado.get("n_teeth") instanceof Number value ? value : null;
        Number nReliable = resultado.get("n_reliable") instanceof Number value ? value : null;
        if (nTeeth == null || nTeeth.intValue() <= 0 || nReliable == null) {
            return null;
        }
        double ratio = Math.max(0, Math.min(1, nReliable.doubleValue() / nTeeth.doubleValue()));
        return BigDecimal.valueOf(ratio).setScale(4, RoundingMode.HALF_UP);
    }

    private String recomendacion(AnalisisRadiografia analisis) {
        if (analisis.getSeveridad() == SeveridadPerdidaOsea.NO_EVALUABLE) {
            return "Analisis no concluyente. Revise la calidad de la radiografia y valide clinicamente.";
        }
        if (Boolean.TRUE.equals(analisis.getPerdidaOseaDetectada())) {
            return "Se detectaron signos compatibles con perdida osea. Validar hallazgos con criterio clinico.";
        }
        return "No se detecta perdida osea significativa en el analisis automatico. Validar clinicamente.";
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
