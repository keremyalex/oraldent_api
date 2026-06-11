package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.OdontogramaCara;
import com.example.odontologia_api.entity.OdontogramaDiente;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.OdontogramaRepository;
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
public class OdontogramaPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int[] Q1 = {18, 17, 16, 15, 14, 13, 12, 11};
    private static final int[] Q2 = {21, 22, 23, 24, 25, 26, 27, 28};
    private static final int[] Q4 = {48, 47, 46, 45, 44, 43, 42, 41};
    private static final int[] Q3 = {31, 32, 33, 34, 35, 36, 37, 38};

    private final OdontogramaRepository odontogramaRepository;

    public OdontogramaPdfService(OdontogramaRepository odontogramaRepository, SimplePdfService simplePdfService) {
        this.odontogramaRepository = odontogramaRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long odontogramaId) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawHeader(content, odontograma, page);
                drawLegend(content, 54, 668);
                drawQuadrant(content, odontograma, "Cuadrante 1 - Superior derecho", Q1, 54, 545);
                drawQuadrant(content, odontograma, "Cuadrante 2 - Superior izquierdo", Q2, 54, 415);
                drawQuadrant(content, odontograma, "Cuadrante 4 - Inferior derecho", Q4, 54, 285);
                drawQuadrant(content, odontograma, "Cuadrante 3 - Inferior izquierdo", Q3, 54, 155);
                drawText(content, "Observaciones: " + value(odontograma.getObservaciones()), 54, 72, 9, Color.DARK_GRAY);
                drawText(content, "Firma y sello: ________________________________", 54, 42, 10, Color.BLACK);
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF del odontograma.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Odontograma buscarActivo(Long odontogramaId) {
        return odontogramaRepository.findById(odontogramaId)
                .filter(Odontograma::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Odontograma no encontrado."));
    }

    private void drawHeader(PDPageContentStream content, Odontograma odontograma, PDPage page) throws IOException {
        FichaClinica ficha = odontograma.getFichaClinica();
        Paciente paciente = ficha.getPaciente();
        float top = page.getMediaBox().getHeight() - 44;
        drawText(content, "CLINICAS ORALDENT", 54, top, 16, new Color(15, 23, 42));
        drawText(content, "REPORTE VISUAL DE ODONTOGRAMA", 54, top - 20, 12, new Color(8, 116, 144));
        drawText(content, "Paciente: " + patientName(paciente), 54, top - 45, 9, Color.DARK_GRAY);
        drawText(content, "Codigo: " + value(paciente.getCodigoPaciente()) + "    Documento: " + value(paciente.getDocumentoIdentidad()), 54, top - 60, 9, Color.DARK_GRAY);
        drawText(content, "Odontograma No " + odontograma.getId() + "    Ficha " + ficha.getId() + "    Fecha " + odontograma.getFechaCreacion().format(DATE_TIME_FORMAT), 54, top - 75, 9, Color.DARK_GRAY);
        drawText(content, "Nacimiento: " + (paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT)), 54, top - 90, 9, Color.DARK_GRAY);
    }

    private void drawLegend(PDPageContentStream content, float x, float y) throws IOException {
        drawColorBox(content, x, y, new Color(255, 225, 225));
        drawText(content, "Rojo: por realizar / patologia", x + 18, y + 2, 8, Color.DARK_GRAY);
        drawColorBox(content, x + 170, y, new Color(219, 234, 254));
        drawText(content, "Azul: realizado / restauracion", x + 188, y + 2, 8, Color.DARK_GRAY);
        drawText(content, "X ausente    I implante    C corona    E endodoncia", x + 350, y + 2, 8, Color.DARK_GRAY);
    }

    private void drawQuadrant(PDPageContentStream content, Odontograma odontograma, String title, int[] teeth, float x, float y) throws IOException {
        drawText(content, title, x, y + 95, 10, new Color(15, 23, 42));
        float tile = 55;
        float gap = 8;
        for (int i = 0; i < teeth.length; i++) {
            OdontogramaDiente diente = tooth(odontograma, teeth[i]);
            drawTooth(content, diente, teeth[i], x + i * (tile + gap), y, tile);
        }
    }

    private void drawTooth(PDPageContentStream content, OdontogramaDiente diente, int number, float x, float y, float size) throws IOException {
        drawText(content, String.valueOf(number), x + 20, y + size + 22, 9, Color.BLACK);
        content.setStrokingColor(new Color(148, 163, 184));
        content.setLineWidth(0.8f);
        content.addRect(x, y, size, size);
        content.stroke();
        float inner = size * 0.30f;
        drawFace(content, x, y + size - inner, size, inner, colorFor(diente, topFace(diente)), true);
        drawFace(content, x + size - inner, y, inner, size, colorFor(diente, rightFace(diente)), true);
        drawFace(content, x, y, size, inner, colorFor(diente, bottomFace(diente)), true);
        drawFace(content, x, y, inner, size, colorFor(diente, leftFace(diente)), true);
        drawFace(content, x + inner, y + inner, size - (inner * 2), size - (inner * 2), colorFor(diente, "OCLUSAL"), true);
        content.setStrokingColor(new Color(100, 116, 139));
        content.addRect(x + inner, y + inner, size - (inner * 2), size - (inner * 2));
        content.stroke();
        if (diente != null) {
            String status = "";
            if (Boolean.TRUE.equals(diente.getAusente())) status = "X";
            else if (Boolean.TRUE.equals(diente.getImplante())) status = "I";
            else if (Boolean.TRUE.equals(diente.getCorona())) status = "C";
            else if (Boolean.TRUE.equals(diente.getEndodoncia())) status = "E";
            if (!status.isBlank()) {
                drawText(content, status, x + 23, y - 13, 9, Color.RED);
            }
        }
    }

    private void drawFace(PDPageContentStream content, float x, float y, float width, float height, Color color, boolean stroke) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, width, height);
        content.fill();
        if (stroke) {
            content.setStrokingColor(new Color(100, 116, 139));
            content.addRect(x, y, width, height);
            content.stroke();
        }
    }

    private void drawColorBox(PDPageContentStream content, float x, float y, Color color) throws IOException {
        drawFace(content, x, y, 12, 10, color, true);
    }

    private OdontogramaDiente tooth(Odontograma odontograma, int number) {
        return odontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi().equals(number))
                .findFirst()
                .orElse(null);
    }

    private Color colorFor(OdontogramaDiente diente, String face) {
        if (diente == null) return Color.WHITE;
        return diente.getCaras().stream()
                .filter(cara -> cara.getTipo().name().equals(face))
                .findFirst()
                .map(this::colorFor)
                .orElse(Color.WHITE);
    }

    private Color colorFor(OdontogramaCara cara) {
        if (cara.getColor() == null) return Color.WHITE;
        return switch (cara.getColor().name()) {
            case "ROJO" -> new Color(255, 225, 225);
            case "AZUL" -> new Color(219, 234, 254);
            default -> Color.WHITE;
        };
    }

    private String topFace(OdontogramaDiente diente) {
        int q = diente == null ? 1 : diente.getCuadrante();
        return q == 3 || q == 4 ? "PALATINO" : "VESTIBULAR";
    }

    private String bottomFace(OdontogramaDiente diente) {
        int q = diente == null ? 1 : diente.getCuadrante();
        return q == 3 || q == 4 ? "VESTIBULAR" : "PALATINO";
    }

    private String leftFace(OdontogramaDiente diente) {
        int q = diente == null ? 1 : diente.getCuadrante();
        return q == 1 || q == 4 ? "DISTAL" : "MESIAL";
    }

    private String rightFace(OdontogramaDiente diente) {
        int q = diente == null ? 1 : diente.getCuadrante();
        return q == 1 || q == 4 ? "MESIAL" : "DISTAL";
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, int size, Color color) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
        content.newLineAtOffset(x, y);
        content.showText(clean(text));
        content.endText();
    }

    private String patientName(Paciente paciente) {
        return (value(paciente.getNombre()) + " " + value(paciente.getApellidoPaterno()) + " " + value(paciente.getApellidoMaterno())).trim();
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "Sin registrar" : value.trim();
    }

    private String clean(String value) {
        return value == null ? "" : value
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N").replace("°", "o");
    }
}
