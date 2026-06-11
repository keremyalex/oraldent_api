package com.example.odontologia_api.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SimplePdfService {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int TOP_MARGIN = 780;
    private static final int LEFT_MARGIN = 54;
    private static final int LINE_HEIGHT = 16;
    private static final int MAX_LINES_PER_PAGE = 44;

    public byte[] crearDocumento(List<String> lineas) {
        List<List<String>> paginas = paginar(lineas);
        List<String> objetos = new ArrayList<>();

        int catalogId = 1;
        int pagesId = 2;
        int fontId = 3;
        int firstPageId = 4;

        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");

        List<Integer> pageIds = new ArrayList<>();
        for (int i = 0; i < paginas.size(); i++) {
            pageIds.add(firstPageId + (i * 2));
        }
        StringBuilder kids = new StringBuilder();
        for (Integer pageId : pageIds) {
            kids.append(pageId).append(" 0 R ");
        }
        objetos.add("<< /Type /Pages /Kids [" + kids + "] /Count " + paginas.size() + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        for (int i = 0; i < paginas.size(); i++) {
            int pageId = firstPageId + (i * 2);
            int contentId = pageId + 1;
            objetos.add("<< /Type /Page /Parent " + pagesId + " 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT
                    + "] /Resources << /Font << /F1 " + fontId + " 0 R >> >> /Contents " + contentId + " 0 R >>");

            String contenido = crearContenidoPagina(paginas.get(i));
            int length = contenido.getBytes(StandardCharsets.ISO_8859_1).length;
            objetos.add("<< /Length " + length + " >>\nstream\n" + contenido + "\nendstream");
        }

        return escribirPdf(objetos);
    }

    private List<List<String>> paginar(List<String> lineas) {
        List<List<String>> paginas = new ArrayList<>();
        List<String> pagina = new ArrayList<>();

        for (String linea : lineas) {
            for (String fragmento : envolver(linea == null ? "" : linea, 92)) {
                if (pagina.size() >= MAX_LINES_PER_PAGE) {
                    paginas.add(pagina);
                    pagina = new ArrayList<>();
                }
                pagina.add(fragmento);
            }
        }

        if (pagina.isEmpty()) {
            pagina.add("");
        }
        paginas.add(pagina);
        return paginas;
    }

    private List<String> envolver(String texto, int maxLength) {
        List<String> resultado = new ArrayList<>();
        if (texto.length() <= maxLength) {
            resultado.add(texto);
            return resultado;
        }

        String restante = texto;
        while (restante.length() > maxLength) {
            int corte = restante.lastIndexOf(' ', maxLength);
            if (corte <= 0) {
                corte = maxLength;
            }
            resultado.add(restante.substring(0, corte).trim());
            restante = restante.substring(corte).trim();
        }
        if (!restante.isBlank()) {
            resultado.add(restante);
        }
        return resultado;
    }

    private String crearContenidoPagina(List<String> lineas) {
        StringBuilder contenido = new StringBuilder();
        contenido.append("BT\n");
        contenido.append("/F1 11 Tf\n");
        contenido.append(LEFT_MARGIN).append(' ').append(TOP_MARGIN).append(" Td\n");

        for (int i = 0; i < lineas.size(); i++) {
            String linea = normalizar(lineas.get(i));
            if (i == 0) {
                contenido.append("(").append(escapar(linea)).append(") Tj\n");
            } else {
                contenido.append("0 -").append(LINE_HEIGHT).append(" Td\n");
                contenido.append("(").append(escapar(linea)).append(") Tj\n");
            }
        }

        contenido.append("ET");
        return contenido.toString();
    }

    private byte[] escribirPdf(List<String> objetos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");

        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            write(out, objetos.get(i));
            write(out, "\nendobj\n");
        }

        int xrefOffset = out.size();
        write(out, "xref\n");
        write(out, "0 " + (objetos.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n");
        write(out, "<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\n");
        write(out, "startxref\n");
        write(out, xrefOffset + "\n");
        write(out, "%%EOF");
        return out.toByteArray();
    }

    private void write(ByteArrayOutputStream out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }

    private String escapar(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String normalizar(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ñ", "n")
                .replace("Ñ", "N")
                .replace("°", "o");
    }
}
