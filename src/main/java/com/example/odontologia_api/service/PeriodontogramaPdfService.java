package com.example.odontologia_api.service;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Periodontograma;
import com.example.odontologia_api.entity.PeriodontogramaDiente;
import com.example.odontologia_api.entity.PeriodontogramaSitio;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.PeriodontogramaRepository;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodontogramaPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float PAGE_WIDTH = PDRectangle.A4.getHeight();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getWidth();
    private static final float CHART_X = 92;
    private static final float CHART_TOP = 230;
    private static final float SCALE = 0.75f;
    private static final float BAND_HEIGHT = 162;
    private static final float BAND_GAP = 180;
    private static final float MM_SCALE = 6.45f;
    private static final int[] UPPER_TEETH = {18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28};
    private static final int[] LOWER_TEETH = {48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38};

    private final PeriodontogramaRepository periodontogramaRepository;

    public PeriodontogramaPdfService(PeriodontogramaRepository periodontogramaRepository, SimplePdfService simplePdfService) {
        this.periodontogramaRepository = periodontogramaRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long periodontogramaId) {
        Periodontograma periodontograma = buscarActivo(periodontogramaId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            drawPage(document, periodontograma, true);
            drawPage(document, periodontograma, false);
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF del periodontograma.", ex);
        }
    }

    @Transactional(readOnly = true)
    public Periodontograma buscarActivo(Long periodontogramaId) {
        return periodontogramaRepository.findById(periodontogramaId)
                .filter(Periodontograma::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodontograma no encontrado."));
    }

    private void drawPage(PDDocument document, Periodontograma periodontograma, boolean upper) throws IOException {
        PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
        document.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            drawHeader(content, periodontograma, upper);
            drawLegend(content);
            if (upper) {
                drawPeriodontalTable(content, periodontograma, UPPER_TEETH, false, 92, 457, "Vestibular superior");
                drawImage(document, content, "ok-teeth-01.jpg", 0);
                drawImage(document, content, "ok-teeth-02.jpg", BAND_GAP);
                drawImplants(document, content, periodontograma, UPPER_TEETH, "b", 0);
                drawImplants(document, content, periodontograma, UPPER_TEETH, "p", BAND_GAP);
                drawBand(content, periodontograma, UPPER_TEETH, new Band(0, 106, false, 1, -1, -1));
                drawBand(content, periodontograma, UPPER_TEETH, new Band(BAND_GAP, 53, true, -1, 1, 1));
                drawPeriodontalTable(content, periodontograma, UPPER_TEETH, true, 92, 100, "Palatino superior");
            } else {
                drawPeriodontalTable(content, periodontograma, LOWER_TEETH, true, 92, 457, "Lingual inferior");
                drawImage(document, content, "uk-teeth-01.jpg", 0);
                drawImage(document, content, "uk-teeth-02.jpg", BAND_GAP);
                drawImplants(document, content, periodontograma, LOWER_TEETH, "l", 0);
                drawImplants(document, content, periodontograma, LOWER_TEETH, "b", BAND_GAP);
                drawBand(content, periodontograma, LOWER_TEETH, new Band(0, 108, true, 1, -1, -1));
                drawBand(content, periodontograma, LOWER_TEETH, new Band(BAND_GAP, 61, false, -1, 1, 1));
                drawPeriodontalTable(content, periodontograma, LOWER_TEETH, false, 92, 100, "Vestibular inferior");
            }
            drawFooter(content, periodontograma, upper);
        }
    }

    private void drawHeader(PDPageContentStream content, Periodontograma periodontograma, boolean upper) throws IOException {
        FichaClinica ficha = periodontograma.getFichaClinica();
        Paciente paciente = ficha.getPaciente();
        drawText(content, "CLINICAS ORALDENT", 28, 558, 14, new Color(15, 23, 42));
        drawText(content, "REPORTE VISUAL DE PERIODONTOGRAMA - " + (upper ? "ARCADA SUPERIOR" : "ARCADA INFERIOR"), 28, 540, 11, new Color(8, 116, 144));
        drawText(content, "Paciente: " + patientName(paciente), 28, 519, 8, Color.DARK_GRAY);
        drawText(content, "Codigo: " + value(paciente.getCodigoPaciente()) + "    Documento: " + value(paciente.getDocumentoIdentidad()), 28, 506, 8, Color.DARK_GRAY);
        drawText(content, "Periodontograma No " + periodontograma.getId() + "    Ficha " + ficha.getId() + "    Fecha " + periodontograma.getFechaCreacion().format(DATE_TIME_FORMAT), 28, 493, 8, Color.DARK_GRAY);
        drawText(content, "Nacimiento: " + (paciente.getFechaNacimiento() == null ? "Sin registrar" : paciente.getFechaNacimiento().format(DATE_FORMAT)), 28, 480, 8, Color.DARK_GRAY);
    }

    private void drawLegend(PDPageContentStream content) throws IOException {
        content.setStrokingColor(new Color(220, 38, 38));
        content.setLineWidth(2);
        content.moveTo(604, 538);
        content.lineTo(646, 538);
        content.stroke();
        drawText(content, "Margen gingival", 652, 534, 8, Color.DARK_GRAY);
        content.setStrokingColor(new Color(37, 99, 235));
        content.moveTo(604, 518);
        content.lineTo(646, 518);
        content.stroke();
        drawText(content, "Sondaje", 652, 514, 8, Color.DARK_GRAY);
    }

    private void drawImage(PDDocument document, PDPageContentStream content, String fileName, float sourceTop) throws IOException {
        byte[] bytes = resourceBytes("/reports/periodontogram/chart/" + fileName);
        PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, fileName);
        float x = CHART_X - 9 * SCALE;
        float width = 810 * SCALE;
        float height = BAND_HEIGHT * SCALE;
        float y = pdfY(sourceTop + BAND_HEIGHT);
        content.drawImage(image, x, y, width, height);
    }

    private void drawImplants(PDDocument document, PDPageContentStream content, Periodontograma periodontograma, int[] teeth, String side, float sourceTop) throws IOException {
        for (int tooth : teeth) {
            PeriodontogramaDiente diente = tooth(periodontograma, tooth);
            if (diente == null || !Boolean.TRUE.equals(diente.getImplante())) {
                continue;
            }
            ImplantPoint point = implantPoint(tooth, side);
            if (point == null) {
                continue;
            }
            String resourceName = tooth + side + ".png";
            String path = "/reports/periodontogram/chart/implants/" + (tooth / 10) + "/" + resourceName;
            byte[] bytes = resourceBytes(path);
            PDImageXObject image = PDImageXObject.createFromByteArray(document, bytes, resourceName);
            float x = pdfX(point.x() - 35);
            float width = 70 * SCALE;
            float height = 132 * SCALE;
            float y = pdfY(sourceTop + point.y() - 66 + 132);
            content.drawImage(image, x, y, width, height);
        }
    }

    private ImplantPoint implantPoint(int tooth, String side) {
        return switch (tooth + side) {
            case "18b" -> new ImplantPoint(26, 57);
            case "18p" -> new ImplantPoint(26, 105);
            case "17b" -> new ImplantPoint(82.7f, 57);
            case "17p" -> new ImplantPoint(82.7f, 105);
            case "16b" -> new ImplantPoint(140.1f, 57);
            case "16p" -> new ImplantPoint(140.4f, 105);
            case "15b" -> new ImplantPoint(191.1f, 57);
            case "15p" -> new ImplantPoint(191.1f, 105);
            case "14b" -> new ImplantPoint(233, 57);
            case "14p" -> new ImplantPoint(233, 105);
            case "13b" -> new ImplantPoint(275.3f, 57);
            case "13p" -> new ImplantPoint(275.3f, 105);
            case "12b" -> new ImplantPoint(314.4f, 57);
            case "12p" -> new ImplantPoint(314.5f, 105.8f);
            case "11b" -> new ImplantPoint(363.9f, 57);
            case "11p" -> new ImplantPoint(363.9f, 106);
            case "21b" -> new ImplantPoint(424.9f, 57);
            case "21p" -> new ImplantPoint(424.9f, 106);
            case "22b" -> new ImplantPoint(473.6f, 57);
            case "22p" -> new ImplantPoint(473.4f, 105.8f);
            case "23b" -> new ImplantPoint(512.7f, 57);
            case "23p" -> new ImplantPoint(512.7f, 105);
            case "24b" -> new ImplantPoint(555, 57);
            case "24p" -> new ImplantPoint(554.4f, 105);
            case "25b" -> new ImplantPoint(596, 57);
            case "25p" -> new ImplantPoint(596, 105);
            case "26b" -> new ImplantPoint(646.8f, 57);
            case "26p" -> new ImplantPoint(646.8f, 105);
            case "27b" -> new ImplantPoint(704.8f, 57);
            case "27p" -> new ImplantPoint(704.8f, 105);
            case "28b" -> new ImplantPoint(762, 57);
            case "28p" -> new ImplantPoint(761.8f, 105);
            case "31b" -> new ImplantPoint(415, 112);
            case "31l" -> new ImplantPoint(415.9f, 54);
            case "32b" -> new ImplantPoint(454.5f, 112);
            case "32l" -> new ImplantPoint(455, 54);
            case "33b" -> new ImplantPoint(492, 112);
            case "33l" -> new ImplantPoint(491, 54);
            case "34b" -> new ImplantPoint(530, 112);
            case "34l" -> new ImplantPoint(529, 54);
            case "35b" -> new ImplantPoint(570.9f, 112);
            case "35l" -> new ImplantPoint(570.9f, 54);
            case "36b" -> new ImplantPoint(625, 112);
            case "36l" -> new ImplantPoint(623, 54);
            case "37b" -> new ImplantPoint(689.4f, 112);
            case "37l" -> new ImplantPoint(684.9f, 54);
            case "38b" -> new ImplantPoint(759.9f, 112);
            case "38l" -> new ImplantPoint(757.5f, 54);
            case "41b" -> new ImplantPoint(373.2f, 112);
            case "41l" -> new ImplantPoint(371.9f, 54);
            case "42b" -> new ImplantPoint(333, 112);
            case "42l" -> new ImplantPoint(331.9f, 54);
            case "43b" -> new ImplantPoint(294.9f, 112);
            case "43l" -> new ImplantPoint(296.6f, 54);
            case "44b" -> new ImplantPoint(257, 112);
            case "44l" -> new ImplantPoint(258.9f, 54);
            case "45b" -> new ImplantPoint(217.4f, 112);
            case "45l" -> new ImplantPoint(217.5f, 54);
            case "46b" -> new ImplantPoint(162.5f, 112);
            case "46l" -> new ImplantPoint(165.4f, 54);
            case "47b" -> new ImplantPoint(98.9f, 112);
            case "47l" -> new ImplantPoint(103.4f, 54);
            case "48b" -> new ImplantPoint(36, 112);
            case "48l" -> new ImplantPoint(38.4f, 54);
            default -> null;
        };
    }

    private void drawBand(PDPageContentStream content, Periodontograma periodontograma, int[] teeth, Band band) throws IOException {
        drawGrid(content, band);
        List<List<Point>> gmSegments = new ArrayList<>();
        List<List<Point>> pdSegments = new ArrayList<>();
        List<Point> currentGm = new ArrayList<>();
        List<Point> currentPd = new ArrayList<>();
        for (int tooth : teeth) {
            PeriodontogramaDiente diente = tooth(periodontograma, tooth);
            if (diente == null || Boolean.TRUE.equals(diente.getAusente())) {
                if (!currentGm.isEmpty()) {
                    gmSegments.add(currentGm);
                    pdSegments.add(currentPd);
                    currentGm = new ArrayList<>();
                    currentPd = new ArrayList<>();
                }
                continue;
            }
            Points points = pointsForTooth(diente, band);
            currentGm.addAll(points.gm());
            currentPd.addAll(points.pd());
        }
        if (!currentGm.isEmpty()) {
            gmSegments.add(currentGm);
            pdSegments.add(currentPd);
        }
        drawSegments(content, pdSegments, new Color(37, 99, 235));
        drawSegments(content, gmSegments, new Color(220, 38, 38));
    }

    private void drawGrid(PDPageContentStream content, Band band) throws IOException {
        content.setStrokingColor(new Color(17, 24, 39));
        content.setLineWidth(0.45f);
        float baselineY = band.top() + band.zeroLine();
        for (int i = 0; i <= 16; i++) {
            float sourceY = baselineY + i * MM_SCALE * band.gridDirection();
            if (sourceY >= band.top() && sourceY <= band.top() + BAND_HEIGHT) {
                content.moveTo(pdfX(12), pdfY(sourceY));
                content.lineTo(pdfX(780), pdfY(sourceY));
                content.stroke();
            }
        }
    }

    private Points pointsForTooth(PeriodontogramaDiente diente, Band band) {
        ToothX coords = ToothX.of(diente.getNumeroFdi());
        List<Point> gm = new ArrayList<>();
        List<Point> pd = new ArrayList<>();
        for (SiteOrder entry : orderedSites(diente.getNumeroFdi(), band.isOral())) {
            PeriodontogramaSitio sitio = site(diente, entry.site());
            float x = switch (entry.kind()) {
                case MESIAL -> band.isOral() ? coords.oralMesial() : coords.mb();
                case CENTER -> band.isOral() ? (coords.oralMesial() + coords.oralDistal()) / 2 : (coords.mb() + coords.db()) / 2;
                case DISTAL -> band.isOral() ? coords.oralDistal() : coords.db();
            };
            float gmY = band.top() + band.zeroLine() + sitio.getMargenGingivalMm() * MM_SCALE * band.gmDirection();
            float pdY = gmY + sitio.getProfundidadSondajeMm() * MM_SCALE * band.pdDirection();
            gm.add(new Point(pdfX(x), pdfY(gmY)));
            pd.add(new Point(pdfX(x), pdfY(pdY)));
        }
        return new Points(gm, pd);
    }

    private List<SiteOrder> orderedSites(int tooth, boolean oral) {
        boolean isRight = (tooth >= 11 && tooth <= 18) || (tooth >= 41 && tooth <= 48);
        String mesial = oral ? "MESIOPALATINO" : "MESIOVESTIBULAR";
        String center = oral ? "PALATINO" : "VESTIBULAR";
        String distal = oral ? "DISTOPALATINO" : "DISTOVESTIBULAR";
        return isRight
                ? List.of(new SiteOrder(distal, PointKind.DISTAL), new SiteOrder(center, PointKind.CENTER), new SiteOrder(mesial, PointKind.MESIAL))
                : List.of(new SiteOrder(mesial, PointKind.MESIAL), new SiteOrder(center, PointKind.CENTER), new SiteOrder(distal, PointKind.DISTAL));
    }

    private void drawSegments(PDPageContentStream content, List<List<Point>> segments, Color color) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(2.5f);
        for (List<Point> segment : segments) {
            if (segment.size() < 2) continue;
            Point first = segment.get(0);
            content.moveTo(first.x(), first.y());
            for (Point point : segment.subList(1, segment.size())) {
                content.lineTo(point.x(), point.y());
            }
            content.stroke();
        }
    }


    private void drawPeriodontalTable(
            PDPageContentStream content,
            Periodontograma periodontograma,
            int[] teeth,
            boolean oral,
            float x,
            float topY,
            String title
    ) throws IOException {
        final float labelX = 22;
        final float colW = 28;
        final float rowH = 9;
        final float headerH = 10;
        final String[] rows = {
                "Movilidad",
                "Implante",
                "Furcacion",
                "Sangrado al sondaje",
                "Placa",
                "Margen gingival",
                "Profundidad sondaje",
                "Nivel insercion"
        };

        float height = headerH + rowH * rows.length;
        drawText(content, title, labelX, topY + 5, 6, Color.DARK_GRAY);

        content.setStrokingColor(Color.BLACK);
        content.setLineWidth(0.35f);
        content.moveTo(labelX, topY);
        content.lineTo(pdfX(790), topY);
        content.stroke();

        for (int row = 0; row <= rows.length; row++) {
            float lineY = topY - headerH - row * rowH;
            content.moveTo(labelX, lineY);
            content.lineTo(pdfX(790), lineY);
            content.stroke();
        }

        for (int row = 0; row < rows.length; row++) {
            float rowTop = topY - headerH - row * rowH;
            drawText(content, shortRowLabel(rows[row]), labelX, rowTop - 6, 5, Color.DARK_GRAY);
        }

        for (int tooth : teeth) {
            PeriodontogramaDiente diente = tooth(periodontograma, tooth);
            float centerX = pdfX(controlX(tooth));
            float colX = centerX - colW / 2;

            content.setNonStrokingColor(Color.WHITE);
            content.addRect(colX, topY - height, colW, height);
            content.fill();

            content.setStrokingColor(Color.BLACK);
            content.setLineWidth(0.35f);
            content.addRect(colX, topY - height, colW, height);
            content.stroke();
            for (int row = 0; row <= rows.length; row++) {
                float lineY = topY - (row == 0 ? 0 : headerH + (row - 1) * rowH);
                content.moveTo(colX, lineY);
                content.lineTo(colX + colW, lineY);
                content.stroke();
            }

            drawText(content, String.valueOf(tooth), centerX - 4, topY - 7, 6, Color.BLACK);
            for (int row = 0; row < rows.length; row++) {
                float rowTop = topY - headerH - row * rowH;
                drawPeriodontalCell(content, diente, oral, rows[row], colX, rowTop, colW, rowH);
            }
        }
    }

    private String shortRowLabel(String row) {
        return switch (row) {
            case "Sangrado al sondaje" -> "Sangrado";
            case "Profundidad sondaje" -> "Prof. sondaje";
            case "Margen gingival" -> "Margen ging.";
            case "Nivel insercion" -> "Insercion";
            default -> row;
        };
    }
    private void drawPeriodontalCell(
            PDPageContentStream content,
            PeriodontogramaDiente diente,
            boolean oral,
            String row,
            float x,
            float topY,
            float width,
            float height
    ) throws IOException {
        if (diente == null) {
            return;
        }
        switch (row) {
            case "Movilidad" -> drawText(content, diente.getMovilidad() == null ? "0" : diente.getMovilidad().toString(), x + width / 2 - 2, topY - 6, 5, Color.BLACK);
            case "Implante" -> {
                if (Boolean.TRUE.equals(diente.getImplante())) drawText(content, "I", x + width / 2 - 2, topY - 6, 5, Color.BLACK);
            }
            case "Furcacion" -> drawText(content, furcationLabel(diente, oral), x + width / 2 - 5, topY - 6, 5, Color.BLACK);
            case "Sangrado al sondaje" -> drawBooleanTriplet(content, orderedSiteValues(diente, oral, "sangrado"), x, topY, width, height, new Color(248, 113, 113));
            case "Placa" -> drawBooleanTriplet(content, orderedSiteValues(diente, oral, "placa"), x, topY, width, height, new Color(148, 163, 184));
            case "Margen gingival" -> drawNumberTriplet(content, orderedSiteNumbers(diente, oral, "margen"), x, topY, width);
            case "Profundidad sondaje" -> drawNumberTriplet(content, orderedSiteNumbers(diente, oral, "profundidad"), x, topY, width);
            case "Nivel insercion" -> drawNumberTriplet(content, orderedSiteNumbers(diente, oral, "insercion"), x, topY, width);
            default -> {
            }
        }
    }

    private void drawBooleanTriplet(PDPageContentStream content, List<Boolean> values, float x, float topY, float width, float height, Color activeColor) throws IOException {
        float boxW = width / 3f;
        for (int i = 0; i < values.size(); i++) {
            content.setNonStrokingColor(values.get(i) ? activeColor : new Color(229, 231, 235));
            content.addRect(x + i * boxW + 1, topY - height + 1, boxW - 2, height - 2);
            content.fill();
        }
    }

    private void drawNumberTriplet(PDPageContentStream content, List<Integer> values, float x, float topY, float width) throws IOException {
        float cellW = width / 3f;
        for (int i = 0; i < values.size(); i++) {
            Color color = values.get(i) == 0 ? Color.BLACK : new Color(37, 99, 235);
            drawText(content, values.get(i).toString(), x + i * cellW + cellW / 2 - 2, topY - 6, 5, color);
        }
    }

    private List<Boolean> orderedSiteValues(PeriodontogramaDiente diente, boolean oral, String metric) {
        return orderedSites(diente.getNumeroFdi(), oral).stream()
                .map(order -> site(diente, order.site()))
                .map(site -> switch (metric) {
                    case "sangrado" -> Boolean.TRUE.equals(site.getSangradoSondaje());
                    case "placa" -> Boolean.TRUE.equals(site.getPlaca());
                    default -> false;
                })
                .toList();
    }

    private List<Integer> orderedSiteNumbers(PeriodontogramaDiente diente, boolean oral, String metric) {
        return orderedSites(diente.getNumeroFdi(), oral).stream()
                .map(order -> site(diente, order.site()))
                .map(site -> switch (metric) {
                    case "margen" -> site.getMargenGingivalMm();
                    case "profundidad" -> site.getProfundidadSondajeMm();
                    case "insercion" -> site.getProfundidadSondajeMm() - site.getMargenGingivalMm();
                    default -> 0;
                })
                .toList();
    }

    private String furcationLabel(PeriodontogramaDiente diente, boolean oral) {
        String value = oral
                ? diente.getFurcacionPalatinaLingual().name()
                : diente.getFurcacionVestibular().name();
        return switch (value) {
            case "GRADO_I" -> "I";
            case "GRADO_II" -> "II";
            case "GRADO_III" -> "III";
            default -> "";
        };
    }
    private void drawFooter(PDPageContentStream content, Periodontograma periodontograma, boolean upper) throws IOException {
        if (!upper) {
            drawText(content, "Observaciones: " + value(periodontograma.getObservaciones()), 28, 8, 7, Color.DARK_GRAY);
            drawText(content, "Firma y sello: ________________________________", 574, 8, 7, Color.BLACK);
        }
    }

    private float pdfX(float sourceX) {
        return CHART_X + sourceX * SCALE;
    }

    private float pdfY(float sourceY) {
        return PAGE_HEIGHT - CHART_TOP - sourceY * SCALE;
    }
    private float controlX(int tooth) {
        ToothX coords = ToothX.of(tooth);
        return (coords.mb() + coords.db() + coords.oralMesial() + coords.oralDistal()) / 4f;
    }

    private PeriodontogramaDiente tooth(Periodontograma periodontograma, int number) {
        return periodontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi().equals(number))
                .findFirst()
                .orElse(null);
    }

    private PeriodontogramaSitio site(PeriodontogramaDiente diente, String site) {
        return diente.getSitios().stream()
                .filter(registro -> registro.getSitio().name().equals(site))
                .findFirst()
                .orElseGet(() -> {
                    PeriodontogramaSitio empty = new PeriodontogramaSitio();
                    empty.setMargenGingivalMm(0);
                    empty.setProfundidadSondajeMm(0);
                    return empty;
                });
    }

    private byte[] resourceBytes(String path) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("No se encontro el recurso " + path);
            }
            return stream.readAllBytes();
        }
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

    private record Band(float top, float zeroLine, boolean isOral, float gmDirection, float pdDirection, float gridDirection) {}
    private record ImplantPoint(float x, float y) {}
    private record Point(float x, float y) {}
    private record Points(List<Point> gm, List<Point> pd) {}
    private record SiteOrder(String site, PointKind kind) {}
    private enum PointKind { MESIAL, CENTER, DISTAL }

    private record ToothX(float mb, float db, Float mp, Float dp, Float ml, Float dl) {
        float oralMesial() { return mp != null ? mp : ml != null ? ml : mb; }
        float oralDistal() { return dp != null ? dp : dl != null ? dl : db; }
        static ToothX of(int tooth) {
            return switch (tooth) {
                case 11 -> new ToothX(377, 345, 375f, 344f, null, null);
                case 12 -> new ToothX(329, 305, 329f, 303f, null, null);
                case 13 -> new ToothX(287, 261, 290f, 263f, null, null);
                case 14 -> new ToothX(245, 221, 246f, 221f, null, null);
                case 15 -> new ToothX(206, 180, 204f, 178f, null, null);
                case 16 -> new ToothX(168, 114, 162f, 116f, null, null);
                case 17 -> new ToothX(103, 63, 100f, 61f, null, null);
                case 18 -> new ToothX(49, 12, 48f, 8f, null, null);
                case 21 -> new ToothX(415, 446, 417f, 448f, null, null);
                case 22 -> new ToothX(463, 487, 463f, 489f, null, null);
                case 23 -> new ToothX(504, 532, 502f, 530f, null, null);
                case 24 -> new ToothX(546, 572, 546f, 571f, null, null);
                case 25 -> new ToothX(586, 612, 588f, 614f, null, null);
                case 26 -> new ToothX(624, 678, 629f, 677f, null, null);
                case 27 -> new ToothX(689, 728, 693f, 732f, null, null);
                case 28 -> new ToothX(743, 780, 745f, 784f, null, null);
                case 31 -> new ToothX(412, 431, null, null, 415f, 432f);
                case 32 -> new ToothX(445, 465, null, null, 450f, 468f);
                case 33 -> new ToothX(483, 504, null, null, 484f, 504f);
                case 34 -> new ToothX(522, 541, null, null, 521f, 544f);
                case 35 -> new ToothX(562, 581, null, null, 562f, 585f);
                case 36 -> new ToothX(604, 653, null, null, 604f, 649f);
                case 37 -> new ToothX(669, 716, null, null, 664f, 712f);
                case 38 -> new ToothX(733, 778, null, null, 729f, 778f);
                case 41 -> new ToothX(379, 360, null, null, 378f, 359f);
                case 42 -> new ToothX(346, 327, null, null, 342f, 324f);
                case 43 -> new ToothX(309, 287, null, null, 307f, 287f);
                case 44 -> new ToothX(270, 250, null, null, 270f, 248f);
                case 45 -> new ToothX(230, 209, null, null, 230f, 206f);
                case 46 -> new ToothX(186, 139, null, null, 186f, 142f);
                case 47 -> new ToothX(123, 75, null, null, 127f, 79f);
                case 48 -> new ToothX(59, 13, null, null, 63f, 14f);
                default -> new ToothX(0, 0, null, null, null, null);
            };
        }
    }
}






