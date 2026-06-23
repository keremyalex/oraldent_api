package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.AnalisisRadiografia;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Radiografia;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.AnalisisRadiografiaRepository;
import com.example.odontologia_api.repository.RadiografiaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RadiografiaPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color INK = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(8, 116, 144);
    private static final Color MUTED = new Color(71, 85, 105);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final RadiografiaRepository radiografiaRepository;
    private final AnalisisRadiografiaRepository analisisRepository;
    private final ObjectMapper objectMapper;

    public RadiografiaPdfService(
            RadiografiaRepository radiografiaRepository,
            AnalisisRadiografiaRepository analisisRepository
    ) {
        this.radiografiaRepository = radiografiaRepository;
        this.analisisRepository = analisisRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long radiografiaId) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        AnalisisRadiografia analisis = analisisRepository
                .findByRadiografiaAndActivoTrueOrderByFechaCreacionDescIdDesc(radiografia)
                .stream()
                .findFirst()
                .orElse(null);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                render(content, document, page, radiografia, analisis);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la radiografia.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Radiografia buscarActiva(Long radiografiaId) {
        return radiografiaRepository.findById(radiografiaId)
                .filter(Radiografia::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Radiografia no encontrada."));
    }

    private void render(
            PDPageContentStream content,
            PDDocument document,
            PDPage page,
            Radiografia radiografia,
            AnalisisRadiografia analisis
    ) throws IOException {
        FichaClinica ficha = radiografia.getFichaClinica();
        Paciente paciente = ficha.getPaciente();
        float top = page.getMediaBox().getHeight() - 46;

        drawText(content, "CLINICA ORALDENT", 48, top, 17, BOLD, INK);
        drawText(content, "REPORTE RADIOGRAFICO", 48, top - 20, 11, BOLD, PRIMARY);
        drawRightText(content, "RADIOGRAFIA No " + radiografia.getId(), 547, top - 2, 10, BOLD, INK);
        drawRightText(content, "Fecha: " + radiografia.getFechaCreacion().format(DATE_TIME_FORMAT), 547, top - 18, 8, REGULAR, MUTED);
        drawLine(content, 48, top - 34, 547, top - 34, PRIMARY, 1.2f);

        drawPatientCard(content, paciente, ficha, 716);
        drawStudySummary(content, radiografia, 620);
        drawImages(content, document, radiografia, analisis, 570);
        drawAnalysisSummary(content, radiografia, analisis, 300);
        drawObservations(content, radiografia, 119);
        drawText(content, "El analisis automatizado es apoyo diagnostico y requiere validacion clinica profesional.", 48, 42, 8, REGULAR, MUTED);
        drawLine(content, 370, 55, 527, 55, INK, 0.8f);
        drawCenteredText(content, "Firma y sello", 370, 43, 157, 8, REGULAR, MUTED);
    }

    private void drawPatientCard(PDPageContentStream content, Paciente paciente, FichaClinica ficha, float top) throws IOException {
        drawCard(content, 48, top - 72, 499, 72);
        drawSectionTitle(content, "DATOS DEL PACIENTE", 60, top - 17);
        drawLabelValue(content, "Paciente", patientName(paciente), 60, top - 37, 225);
        drawLabelValue(content, "Codigo", value(paciente.getCodigoPaciente()), 60, top - 55, 225);
        drawLabelValue(content, "Documento", value(paciente.getDocumentoIdentidad()), 310, top - 37, 220);
        drawLabelValue(content, "Ficha clinica", String.valueOf(ficha.getId()), 310, top - 55, 220);
        String nacimiento = paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT);
        drawLabelValue(content, "Nacimiento", nacimiento, 60, top - 69, 225);
        drawLabelValue(content, "Celular", value(paciente.getCelular()), 310, top - 69, 220);
    }

    private void drawStudySummary(PDPageContentStream content, Radiografia radiografia, float top) throws IOException {
        drawSectionTitle(content, "DATOS DEL ESTUDIO", 48, top);
        drawLabelValue(content, "Titulo", value(radiografia.getTitulo()), 48, top - 18, 245);
        drawLabelValue(content, "Fecha", radiografia.getFechaEstudio() == null ? "Sin registrar" : radiografia.getFechaEstudio().format(DATE_FORMAT), 310, top - 18, 237);
        drawLabelValue(content, "Tipo", value(radiografia.getTipo()), 48, top - 34, 245);
        drawLabelValue(content, "Pieza / zona", studyLocation(radiografia), 310, top - 34, 237);
    }

    private void drawImages(PDPageContentStream content, PDDocument document, Radiografia radiografia, AnalisisRadiografia analisis, float top) throws IOException {
        drawSectionTitle(content, "IMAGENES DEL ESTUDIO", 48, top);
        PDImageXObject original = imageFromUrl(document, radiografia.getImagenUrl());
        PDImageXObject overlay = overlayImage(document, analisis);
        boolean hasOverlay = overlay != null;
        float gap = 12;
        float panelWidth = hasOverlay ? (499 - gap) / 2 : 499;
        float panelHeight = 214;
        float y = top - 232;

        drawImagePanel(content, original, "Radiografia original", 48, y, panelWidth, panelHeight);
        if (hasOverlay) {
            drawImagePanel(content, overlay, "Resultado visual IA", 48 + panelWidth + gap, y, panelWidth, panelHeight);
        }
    }

    private void drawImagePanel(PDPageContentStream content, PDImageXObject image, String label, float x, float y, float width, float height) throws IOException {
        drawCard(content, x, y, width, height);
        drawText(content, label, x + 10, y + height - 17, 9, BOLD, INK);
        if (image == null) {
            drawCenteredText(content, "Imagen no disponible", x, y + height / 2, width, 9, REGULAR, MUTED);
            return;
        }
        float maxWidth = width - 20;
        float maxHeight = height - 40;
        float scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
        float drawWidth = image.getWidth() * scale;
        float drawHeight = image.getHeight() * scale;
        float drawX = x + (width - drawWidth) / 2;
        float drawY = y + 10 + (maxHeight - drawHeight) / 2;
        content.drawImage(image, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawAnalysisSummary(PDPageContentStream content, Radiografia radiografia, AnalisisRadiografia analisis, float top) throws IOException {
        drawSectionTitle(content, "HALLAZGOS Y ANALISIS", 48, top);
        drawCard(content, 48, top - 150, 499, 138);
        if (analisis == null) {
            drawText(content, "No hay un analisis automatico registrado para esta radiografia.", 60, top - 34, 9, REGULAR, MUTED);
            drawParagraph(content, value(radiografia.getDiagnosticoRadiografico()), 60, top - 54, 475, 9, MUTED, 3);
            return;
        }

        String status = enumLabel(analisis.getEstado() == null ? null : analisis.getEstado().name());
        drawLabelValue(content, "Estado", status, 60, top - 34, 215);
        // drawLabelValue(content, "Modelo", value(analisis.getModelo()), 310, top - 34, 220);
        drawLabelValue(content, "Perdida estimada", percent(analisis.getPorcentajePerdidaOsea()), 60, top - 52, 215);
        drawLabelValue(content, "Confianza", confidence(analisis.getConfianza()), 310, top - 52, 220);
        drawLabelValue(content, "Tipo", enumLabel(finalType(analisis, radiografia)), 60, top - 70, 215);
        drawLabelValue(content, "Severidad", enumLabel(finalSeverity(analisis, radiografia)), 310, top - 70, 220);
        drawText(content, "Conclusion", 60, top - 91, 8, BOLD, MUTED);
        drawParagraph(content, value(analisis.getRecomendacion()), 60, top - 104, 475, 8, MUTED, 2);
        if (Boolean.TRUE.equals(analisis.getValidado())) {
            drawText(content, "Validado clinicamente", 60, top - 135, 8, BOLD, PRIMARY);
        }
    }

    private void drawObservations(PDPageContentStream content, Radiografia radiografia, float top) throws IOException {
        drawSectionTitle(content, "OBSERVACIONES CLINICAS", 48, top);
        drawCard(content, 48, top - 52, 499, 40);
        String text = radiografia.getObservacionesPeriodontales();
        if (text == null || text.isBlank()) text = radiografia.getDescripcion();
        drawParagraph(content, value(text), 60, top - 29, 475, 8, MUTED, 2);
    }

    private PDImageXObject imageFromUrl(PDDocument document, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        try {
            URLConnection connection = new URL(imageUrl).openConnection();
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(12000);
            try (InputStream input = connection.getInputStream()) {
                return imageFromBytes(document, input.readAllBytes());
            }
        } catch (IOException ex) {
            return null;
        }
    }

    private PDImageXObject overlayImage(PDDocument document, AnalisisRadiografia analisis) {
        if (analisis == null || analisis.getResultadoJson() == null || analisis.getResultadoJson().isBlank()) return null;
        try {
            JsonNode node = objectMapper.readTree(analisis.getResultadoJson());
            String overlay = node.path("overlay").asText(null);
            if (overlay == null || overlay.isBlank()) return null;
            String encoded = overlay.contains(",") ? overlay.substring(overlay.indexOf(',') + 1) : overlay;
            return imageFromBytes(document, Base64.getDecoder().decode(encoded));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private PDImageXObject imageFromBytes(PDDocument document, byte[] bytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        return image == null ? null : LosslessFactory.createFromImage(document, image);
    }

    private void drawCard(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.setNonStrokingColor(SURFACE);
        content.addRect(x, y, width, height);
        content.fill();
        content.setStrokingColor(BORDER);
        content.setLineWidth(0.7f);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawSectionTitle(PDPageContentStream content, String text, float x, float y) throws IOException {
        drawText(content, text, x, y, 10, BOLD, PRIMARY);
    }

    private void drawLabelValue(PDPageContentStream content, String label, String value, float x, float y, float maxWidth) throws IOException {
        drawText(content, label + ":", x, y, 8, BOLD, MUTED);
        float labelWidth = BOLD.getStringWidth(clean(label + ":")) / 1000 * 8;
        drawText(content, trimToWidth(value, maxWidth - labelWidth - 6, 8, REGULAR), x + labelWidth + 6, y, 8, REGULAR, INK);
    }

    private void drawParagraph(PDPageContentStream content, String text, float x, float y, float width, int size, Color color, int maxLines) throws IOException {
        List<String> lines = wrap(text, width, size, REGULAR);
        for (int index = 0; index < Math.min(lines.size(), maxLines); index++) {
            drawText(content, lines.get(index), x, y - index * (size + 3), size, REGULAR, color);
        }
    }

    private List<String> wrap(String value, float width, int size, PDType1Font font) throws IOException {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : clean(value).split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.getStringWidth(candidate) / 1000 * size > width) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines.isEmpty() ? List.of("Sin registrar") : lines;
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, int size, PDType1Font font, Color color) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(clean(text));
        content.endText();
    }

    private void drawRightText(PDPageContentStream content, String text, float right, float y, int size, PDType1Font font, Color color) throws IOException {
        float textWidth = font.getStringWidth(clean(text)) / 1000 * size;
        drawText(content, text, right - textWidth, y, size, font, color);
    }

    private void drawCenteredText(PDPageContentStream content, String text, float x, float y, float width, int size, PDType1Font font, Color color) throws IOException {
        float textWidth = font.getStringWidth(clean(text)) / 1000 * size;
        drawText(content, text, x + (width - textWidth) / 2, y, size, font, color);
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color, float width) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(width);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private String patientName(Paciente paciente) {
        return (value(paciente.getNombre()) + " " + value(paciente.getApellidoPaterno()) + " " + value(paciente.getApellidoMaterno())).trim();
    }

    private String studyLocation(Radiografia radiografia) {
        String piece = radiografia.getNumeroFdi() == null ? null : "Pieza " + radiografia.getNumeroFdi();
        String zone = radiografia.getZona();
        if (piece == null && (zone == null || zone.isBlank())) return "Sin registrar";
        if (piece == null) return zone;
        if (zone == null || zone.isBlank()) return piece;
        return piece + " - " + zone;
    }

    private String finalType(AnalisisRadiografia analisis, Radiografia radiografia) {
        if (analisis.getTipoPerdidaOseaFinal() != null) return analisis.getTipoPerdidaOseaFinal().name();
        if (analisis.getTipoPerdidaOsea() != null) return analisis.getTipoPerdidaOsea().name();
        return radiografia.getTipoPerdidaOsea() == null ? "NO_EVALUABLE" : radiografia.getTipoPerdidaOsea().name();
    }

    private String finalSeverity(AnalisisRadiografia analisis, Radiografia radiografia) {
        if (analisis.getSeveridadFinal() != null) return analisis.getSeveridadFinal().name();
        if (analisis.getSeveridad() != null) return analisis.getSeveridad().name();
        return radiografia.getSeveridadPerdidaOsea() == null ? "NO_EVALUABLE" : radiografia.getSeveridadPerdidaOsea().name();
    }

    private String percent(java.math.BigDecimal value) {
        return value == null ? "Sin estimacion" : value.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String confidence(java.math.BigDecimal value) {
        return value == null ? "Sin estimacion" : value.multiply(java.math.BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String enumLabel(String value) {
        return value == null || value.isBlank() ? "Sin registrar" : value.toLowerCase().replace('_', ' ');
    }

    private String trimToWidth(String value, float maxWidth, int size, PDType1Font font) throws IOException {
        String text = clean(value);
        if (font.getStringWidth(text) / 1000 * size <= maxWidth) return text;
        while (!text.isEmpty() && font.getStringWidth(text + "...") / 1000 * size > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text.trim() + "...";
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "Sin registrar" : text.trim();
    }

    private String clean(String text) {
        return text == null ? "" : text
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N").replace("°", "o");
    }
}
