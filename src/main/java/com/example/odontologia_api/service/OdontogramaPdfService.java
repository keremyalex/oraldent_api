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
    private static final int[] SUPERIOR = {18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28};
    private static final int[] INFERIOR = {48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38};
    private static final Color INK = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(8, 116, 144);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color TOOTH_STROKE = new Color(100, 116, 139);

    private final OdontogramaRepository odontogramaRepository;

    public OdontogramaPdfService(OdontogramaRepository odontogramaRepository) {
        this.odontogramaRepository = odontogramaRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long odontogramaId) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawHeader(content, odontograma, page);
                drawHeaderDivider(content, 48, 458);
                drawLegend(content, 48, 439);
                drawArch(content, odontograma, "Arcada superior", SUPERIOR, 48, 332);
                drawArch(content, odontograma, "Arcada inferior", INFERIOR, 48, 183);
                drawObservations(content, odontograma, 48, 82);
                drawText(content, "Firma y sello: ______________________________________", 48, 44, 10, Color.BLACK);
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
        float top = page.getMediaBox().getHeight() - 38;
        drawText(content, "CLINICA ORALDENT", 48, top, 16, INK);
        drawText(content, "REPORTE VISUAL DE ODONTOGRAMA", 48, top - 19, 12, PRIMARY);
        drawText(content, "Paciente: " + patientName(paciente), 48, top - 43, 9, Color.DARK_GRAY);
        drawText(content, "Codigo: " + value(paciente.getCodigoPaciente()) + "    Documento: " + value(paciente.getDocumentoIdentidad()), 48, top - 57, 9, Color.DARK_GRAY);
        drawText(content, "Odontograma No " + odontograma.getId() + "    Ficha " + ficha.getId() + "    Fecha " + odontograma.getFechaCreacion().format(DATE_TIME_FORMAT), 48, top - 71, 9, Color.DARK_GRAY);
        drawText(content, "Nacimiento: " + (paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT)), 48, top - 85, 9, Color.DARK_GRAY);
    }

    private void drawLegend(PDPageContentStream content, float x, float y) throws IOException {
        drawColorBox(content, x, y, new Color(255, 225, 225));
        drawText(content, "Rojo: por realizar / patologia", x + 18, y + 2, 8, Color.DARK_GRAY);
        drawColorBox(content, x + 172, y, new Color(219, 234, 254));
        drawText(content, "Azul: realizado / restauracion", x + 190, y + 2, 8, Color.DARK_GRAY);
        drawText(content, "X ausente    I implante    C corona    E endodoncia", x + 368, y + 2, 8, Color.DARK_GRAY);
    }

    private void drawHeaderDivider(PDPageContentStream content, float x, float y) throws IOException {
        content.setStrokingColor(BORDER);
        content.setLineWidth(0.8f);
        content.moveTo(x, y);
        content.lineTo(x + 746, y);
        content.stroke();
    }

    private void drawArch(PDPageContentStream content, Odontograma odontograma, String title, int[] teeth, float x, float y) throws IOException {
        drawText(content, title, x, y + 94, 11, INK);
        drawText(content, "Derecho", x, y + 80, 8, PRIMARY);
        drawText(content, "Izquierdo", x + 678, y + 80, 8, PRIMARY);

        float tile = 42;
        float gap = 4;
        float centerGap = 10;
        float dividerX = x + 8 * (tile + gap) + centerGap / 2;
        content.setStrokingColor(PRIMARY);
        content.setLineWidth(1.2f);
        content.moveTo(dividerX, y - 7);
        content.lineTo(dividerX, y + 75);
        content.stroke();

        for (int i = 0; i < teeth.length; i++) {
            OdontogramaDiente diente = tooth(odontograma, teeth[i]);
            float toothX = x + i * (tile + gap) + (i >= 8 ? centerGap : 0);
            drawToothTile(content, diente, teeth[i], toothX, y, tile);
        }
    }

    // Mirrors the five-face layout used by Flutter's ToothDiagram painter.
    private void drawToothTile(PDPageContentStream content, OdontogramaDiente diente, int number, float x, float y, float tile) throws IOException {
        content.setNonStrokingColor(new Color(248, 250, 252));
        content.addRect(x, y, tile, 70);
        content.fill();
        content.setStrokingColor(BORDER);
        content.setLineWidth(0.8f);
        content.addRect(x, y, tile, 70);
        content.stroke();

        drawCenteredText(content, String.valueOf(number), x, y + 58, tile, 8, INK);
        drawToothDiagram(content, diente, x + 5, y + 14, tile - 10);
        String status = statusFor(diente);
        if (!status.isBlank()) {
            drawCenteredText(content, status, x, y + 4, tile, 8, status.equals("X") ? new Color(220, 38, 38) : PRIMARY);
        }
    }

    private void drawToothDiagram(PDPageContentStream content, OdontogramaDiente diente, float x, float y, float size) throws IOException {
        float innerOffset = size * 0.30f;
        float left = x;
        float right = x + size;
        float bottom = y;
        float top = y + size;
        float innerLeft = left + innerOffset;
        float innerRight = right - innerOffset;
        float innerBottom = bottom + innerOffset;
        float innerTop = top - innerOffset;

        drawPolygon(content, new float[][] {{left, top}, {right, top}, {innerRight, innerTop}, {innerLeft, innerTop}}, colorFor(diente, topFace(diente)));
        drawPolygon(content, new float[][] {{right, top}, {right, bottom}, {innerRight, innerBottom}, {innerRight, innerTop}}, colorFor(diente, rightFace(diente)));
        drawPolygon(content, new float[][] {{right, bottom}, {left, bottom}, {innerLeft, innerBottom}, {innerRight, innerBottom}}, colorFor(diente, bottomFace(diente)));
        drawPolygon(content, new float[][] {{left, bottom}, {left, top}, {innerLeft, innerTop}, {innerLeft, innerBottom}}, colorFor(diente, leftFace(diente)));

        content.setNonStrokingColor(colorFor(diente, "OCLUSAL"));
        content.addRect(innerLeft, innerBottom, innerRight - innerLeft, innerTop - innerBottom);
        content.fill();
        content.setStrokingColor(TOOTH_STROKE);
        content.setLineWidth(0.8f);
        content.addRect(innerLeft, innerBottom, innerRight - innerLeft, innerTop - innerBottom);
        content.stroke();
    }

    private void drawPolygon(PDPageContentStream content, float[][] points, Color color) throws IOException {
        content.moveTo(points[0][0], points[0][1]);
        for (int index = 1; index < points.length; index++) {
            content.lineTo(points[index][0], points[index][1]);
        }
        content.closePath();
        content.setNonStrokingColor(color);
        content.setStrokingColor(TOOTH_STROKE);
        content.setLineWidth(0.8f);
        content.fillAndStroke();
    }

    private void drawObservations(PDPageContentStream content, Odontograma odontograma, float x, float y) throws IOException {
        drawText(content, "Observaciones clinicas", x, y + 18, 10, INK);
        content.setNonStrokingColor(new Color(248, 250, 252));
        content.addRect(x, y - 8, 746, 20);
        content.fill();
        content.setStrokingColor(BORDER);
        content.addRect(x, y - 8, 746, 20);
        content.stroke();
        drawText(content, value(odontograma.getObservaciones()), x + 8, y - 1, 8, Color.DARK_GRAY);
    }

    private void drawColorBox(PDPageContentStream content, float x, float y, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, 12, 10);
        content.fill();
        content.setStrokingColor(TOOTH_STROKE);
        content.addRect(x, y, 12, 10);
        content.stroke();
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

    private String statusFor(OdontogramaDiente diente) {
        if (diente == null) return "";
        if (Boolean.TRUE.equals(diente.getAusente())) return "X";
        if (Boolean.TRUE.equals(diente.getImplante())) return "I";
        if (Boolean.TRUE.equals(diente.getCorona())) return "C";
        if (Boolean.TRUE.equals(diente.getEndodoncia())) return "E";
        return "";
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

    private void drawCenteredText(PDPageContentStream content, String text, float x, float y, float width, int size, Color color) throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float textWidth = font.getStringWidth(clean(text)) / 1000 * size;
        drawText(content, text, x + (width - textWidth) / 2, y, size, color);
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
