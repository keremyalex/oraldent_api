package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Receta;
import com.example.odontologia_api.entity.RecetaDetalle;
import com.example.odontologia_api.entity.Usuario;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecetaPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color INK = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(8, 116, 144);
    private static final Color MUTED = new Color(71, 85, 105);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final RecetaService recetaService;

    public RecetaPdfService(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long recetaId) {
        Receta receta = recetaService.buscarActiva(recetaId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                render(content, receta, page);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la receta.", ex);
        }
    }

    private void render(PDPageContentStream content, Receta receta, PDPage page) throws IOException {
        FichaClinica ficha = receta.getFichaClinica();
        Paciente paciente = ficha.getPaciente();
        Usuario usuario = receta.getUsuario();
        float y = page.getMediaBox().getHeight() - 48;

        drawText(content, "CLINICAS ORALDENT", 48, y, 17, BOLD, INK);
        drawText(content, "RECETA ODONTOLOGICA", 48, y - 20, 11, BOLD, PRIMARY);
        drawRightText(content, "RECETA No " + receta.getId(), 547, y - 2, 11, BOLD, INK);
        drawRightText(content, "Emitida: " + receta.getFechaCreacion().format(DATE_TIME_FORMAT), 547, y - 18, 8, REGULAR, MUTED);
        drawLine(content, 48, y - 34, 547, y - 34, PRIMARY, 1.2f);
        y -= 54;

        drawPatientCard(content, paciente, ficha, y);
        y -= 98;
        drawClinicalCard(content, ficha, y);
        y -= 94;

        drawSectionTitle(content, "MEDICAMENTOS", 48, y);
        y -= 14;
        List<RecetaDetalle> detalles = receta.getDetalles().stream()
                .sorted(Comparator.comparing(RecetaDetalle::getOrden, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(detalle -> detalle.getId() == null ? 0L : detalle.getId()))
                .toList();
        if (detalles.isEmpty()) {
            drawEmptyMedication(content, y);
            y -= 44;
        } else {
            int index = 1;
            for (RecetaDetalle detalle : detalles) {
                float height = 68;
                drawMedicationCard(content, detalle, index, y, height);
                y -= height + 9;
                index++;
            }
        }
        y -= 12;

        drawSectionTitle(content, "INDICACIONES GENERALES", 48, y);
        y -= 12;
        y = drawNoteBox(content, valor(receta.getIndicacionesGenerales()), y, 44);
        y -= 14;
        drawSectionTitle(content, "OBSERVACIONES", 48, y);
        y -= 12;
        y = drawNoteBox(content, valor(receta.getObservaciones()), y, 44);

        float signatureY = Math.max(36, y - 52);
        drawText(content, "Emitido por: " + nombreUsuario(usuario), 48, signatureY + 30, 9, REGULAR, MUTED);
        drawLine(content, 345, signatureY + 12, 527, signatureY + 12, INK, 0.8f);
        drawCenteredText(content, "Firma y sello", 345, signatureY, 182, 8, REGULAR, MUTED);
    }

    private void drawPatientCard(PDPageContentStream content, Paciente paciente, FichaClinica ficha, float top) throws IOException {
        drawCard(content, 48, top - 76, 499, 76);
        drawSectionTitle(content, "DATOS DEL PACIENTE", 60, top - 17);
        drawLabelValue(content, "Paciente", nombrePaciente(paciente), 60, top - 37, 225);
        drawLabelValue(content, "Codigo", valor(paciente.getCodigoPaciente()), 60, top - 55, 225);
        drawLabelValue(content, "Documento", valor(paciente.getDocumentoIdentidad()), 310, top - 37, 220);
        drawLabelValue(content, "Ficha clinica", String.valueOf(ficha.getId()), 310, top - 55, 220);
        String nacimiento = paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT);
        drawLabelValue(content, "Nacimiento", nacimiento, 60, top - 69, 225);
        drawLabelValue(content, "Celular", valor(paciente.getCelular()), 310, top - 69, 220);
    }

    private void drawClinicalCard(PDPageContentStream content, FichaClinica ficha, float top) throws IOException {
        drawCard(content, 48, top - 72, 499, 72);
        drawSectionTitle(content, "RESUMEN CLINICO", 60, top - 17);
        drawLabeledParagraph(content, "Motivo", valor(ficha.getMotivoConsulta()), 60, top - 36, 465);
        drawLabeledParagraph(content, "Diagnostico", valor(ficha.getDiagnostico()), 60, top - 51, 465);
        drawLabeledParagraph(content, "Tratamiento", valor(ficha.getTratamiento()), 60, top - 66, 465);
    }

    private void drawMedicationCard(PDPageContentStream content, RecetaDetalle detalle, int index, float top, float height) throws IOException {
        drawCard(content, 48, top - height, 499, height);
        content.setNonStrokingColor(PRIMARY);
        content.addRect(60, top - 27, 20, 20);
        content.fill();
        drawCenteredText(content, String.valueOf(index), 60, top - 22, 20, 9, BOLD, Color.WHITE);
        drawText(content, valor(detalle.getMedicamento()), 90, top - 20, 11, BOLD, INK);
        drawLabelValue(content, "Dosis", valor(detalle.getDosis()), 60, top - 41, 195);
        drawLabelValue(content, "Frecuencia", valor(detalle.getFrecuencia()), 300, top - 41, 225);
        drawLabeledParagraph(content, "Indicaciones", joinDetails(detalle.getDuracion(), detalle.getIndicaciones()), 60, top - 58, 465);
    }

    private void drawEmptyMedication(PDPageContentStream content, float top) throws IOException {
        drawCard(content, 48, top - 34, 499, 34);
        drawText(content, "No hay medicamentos registrados en esta receta.", 60, top - 21, 9, REGULAR, MUTED);
    }

    private float drawNoteBox(PDPageContentStream content, String value, float top, float height) throws IOException {
        drawCard(content, 48, top - height, 499, height);
        List<String> lines = wrap(value, 78);
        float textY = top - 17;
        for (int index = 0; index < Math.min(lines.size(), 3); index++) {
            drawText(content, lines.get(index), 60, textY - index * 11, 9, REGULAR, MUTED);
        }
        return top - height;
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
        String text = trimToWidth(value, maxWidth - labelWidth - 6, 8, REGULAR);
        drawText(content, text, x + labelWidth + 6, y, 8, REGULAR, INK);
    }

    private void drawLabeledParagraph(PDPageContentStream content, String label, String value, float x, float y, float maxWidth) throws IOException {
        drawText(content, label + ":", x, y, 8, BOLD, MUTED);
        float labelWidth = BOLD.getStringWidth(clean(label + ":")) / 1000 * 8;
        drawText(content, trimToWidth(value, maxWidth - labelWidth - 6, 8, REGULAR), x + labelWidth + 6, y, 8, REGULAR, INK);
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
        float width = font.getStringWidth(clean(text)) / 1000 * size;
        drawText(content, text, right - width, y, size, font, color);
    }

    private void drawCenteredText(PDPageContentStream content, String text, float x, float y, float width, int size, PDType1Font font, Color color) throws IOException {
        float textWidth = font.getStringWidth(clean(text)) / 1000 * size;
        drawText(content, text, x + (width - textWidth) / 2, y, size, font, color);
    }

    private void drawLine(PDPageContentStream content, float fromX, float fromY, float toX, float toY, Color color, float width) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(width);
        content.moveTo(fromX, fromY);
        content.lineTo(toX, toY);
        content.stroke();
    }

    private List<String> wrap(String value, int maxCharacters) {
        List<String> lines = new java.util.ArrayList<>();
        String remaining = value;
        while (remaining.length() > maxCharacters) {
            int breakAt = remaining.lastIndexOf(' ', maxCharacters);
            if (breakAt <= 0) breakAt = maxCharacters;
            lines.add(remaining.substring(0, breakAt).trim());
            remaining = remaining.substring(breakAt).trim();
        }
        if (!remaining.isBlank()) lines.add(remaining);
        return lines.isEmpty() ? List.of("Sin registrar") : lines;
    }

    private String trimToWidth(String value, float maxWidth, int size, PDType1Font font) throws IOException {
        String text = clean(value);
        if (font.getStringWidth(text) / 1000 * size <= maxWidth) return text;
        String suffix = "...";
        while (!text.isEmpty() && font.getStringWidth(text + suffix) / 1000 * size > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text.trim() + suffix;
    }

    private String joinDetails(String duracion, String indicaciones) {
        return "Duracion: " + valor(duracion) + " | " + valor(indicaciones);
    }

    private String nombrePaciente(Paciente paciente) {
        return (valor(paciente.getNombre()) + " " + valor(paciente.getApellidoPaterno()) + " " + valor(paciente.getApellidoMaterno())).trim();
    }

    private String nombreUsuario(Usuario usuario) {
        if (usuario == null) return "Sin registrar";
        return (valor(usuario.getNombre()) + " " + valor(usuario.getApellidoPaterno()) + " " + valor(usuario.getApellidoMaterno())).trim();
    }

    private String valor(String value) {
        return value == null || value.isBlank() ? "Sin registrar" : value.trim();
    }

    private String clean(String value) {
        return value == null ? "" : value
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N").replace("°", "o");
    }
}
