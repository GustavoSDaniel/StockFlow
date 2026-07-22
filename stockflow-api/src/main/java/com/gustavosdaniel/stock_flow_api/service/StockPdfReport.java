package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;
import com.gustavosdaniel.stock_flow_api.util.PdfFooterHelper;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class StockPdfReport {

    private static final float MARGIN_LEFT = 30f;
    private static final float MARGIN_RIGHT = 30f;
    private static final float MARGIN_TOP = 40f;
    private static final float MARGIN_BOTTOM = 50f;

    private static final float TITLE_FONT_SIZE = 18f;
    private static final float HEADER_FONT_SIZE = 10f;
    private static final float CELL_FONT_SIZE = 9f;
    private static final float SUMMARY_FONT_SIZE = 12f;

    private static final float TITLE_SPACING_AFTER = 20f;
    private static final float SUMMARY_SPACING_BEFORE = 15f;
    private static final float TABLE_WIDTH_PERCENTAGE = 100f;
    private static final float HEADER_PADDING = 6f;
    private static final float CELL_PADDING = 5f;

    private static final float[] COLUMN_WIDTHS = {3f, 2.5f, 1.5f, 1.5f, 1f, 1f, 1f, 1.5f};

    private static final Color HEADER_BG_COLOR = new Color(235, 235, 235);

    private static final Color COLOR_OUT_OF_STOCK = new Color(255, 205, 210);
    private static final Color COLOR_LOW_STOCK    = new Color(255, 236, 179);
    private static final Color COLOR_OVER_STOCK   = new Color(187, 222, 251);

    public byte[] generateReport(List<StockResponse> stocks) {

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()){

            Document document = new Document(PageSize.A4.rotate(),
                    MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new PdfFooterHelper());

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.COURIER_BOLD, TITLE_FONT_SIZE);
            Paragraph title = new Paragraph("StockFlow - Relatório de Estoque", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(TITLE_SPACING_AFTER);
            document.add(title);

            if (stocks.isEmpty()){
                addEmptyMessage(document);
            } else {
                document.add(buildTable(stocks));
            }

            addSummary(document, stocks);

            document.close();

            return output.toByteArray();
        } catch (Exception e) {

            throw new RuntimeException("Erro ao gerar o relatório de estoque em PDF", e);
        }
    }

    private PdfPTable buildTable(List<StockResponse> stocks) throws DocumentException {

        PdfPTable table = new PdfPTable(COLUMN_WIDTHS);
        table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);

        String[] headers = { "Produto", "SKU", "Galpão", "Prateleira",
                "Qtd Atual", "Mínimo", "Máximo", "Status"};

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, HEADER_FONT_SIZE);

        for (String h : headers) {

            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(HEADER_PADDING);
            table.addCell(cell);
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, CELL_FONT_SIZE);

        for (StockResponse s : stocks) {

            addCell(table, s.productName(), cellFont, Element.ALIGN_LEFT, null);
            addCell(table, s.productSku(), cellFont, Element.ALIGN_LEFT, null);
            addCell(table, s.warehouseId(), cellFont, Element.ALIGN_CENTER, null);
            addCell(table, s.location(), cellFont, Element.ALIGN_CENTER, null);
            addCell(table, String.valueOf(s.currentQuantity()), cellFont, Element.ALIGN_CENTER, null);
            addCell(table, String.valueOf(s.minimumQuantity()), cellFont, Element.ALIGN_CENTER, null);
            addCell(table, String.valueOf(s.maximumQuantity()), cellFont, Element.ALIGN_CENTER, null);

            String statusStr = translateStatus(s.status());
            Color rowColor = getStatusColor(s.status());
            addCell(table, statusStr, cellFont, Element.ALIGN_CENTER, rowColor);
        }

        return table;
    }

    private String translateStatus(StockStatus status){

        if (status == null) return "-";

        return switch (status) {
            case OUT_OF_STOCK -> "Sem Estoque";
            case LOW -> "Baixo";
            case REORDER_POINT -> "Ponto de reposição";
            case NORMAL -> "Normal";
            case OVER_STOCKED -> "Excesso";
        };
    }

    private Color getStatusColor(StockStatus status) {

        if (status == null) return null;

        return switch (status){
            case OUT_OF_STOCK -> COLOR_OUT_OF_STOCK;
            case LOW -> COLOR_LOW_STOCK;
            case OVER_STOCKED -> COLOR_OVER_STOCK;
            default -> null;
        };
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment, Color bgColor){

        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(CELL_PADDING);

        if (bgColor != null){
            cell.setBackgroundColor(bgColor);
        }

        table.addCell(cell);
    }

    private void addEmptyMessage(Document document) throws DocumentException{

        Font emptyFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, SUMMARY_FONT_SIZE);
        Paragraph empty = new Paragraph("Nenhum item de estoque encontrado.", emptyFont);
        empty.setAlignment(Element.ALIGN_CENTER);
        empty.setSpacingBefore(TITLE_SPACING_AFTER);
        document.add(empty);
    }

    private void addSummary(Document document, List<StockResponse> stocks) throws DocumentException {

        long outOfStock = stocks.stream()
                .filter(s -> s.status() == StockStatus.OUT_OF_STOCK).count();

        long lowStock = stocks.stream().filter(s -> s.status() == StockStatus.LOW).count();

        long overStock = stocks.stream()
                .filter(s -> s.status() == StockStatus.OVER_STOCKED).count();

        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, SUMMARY_FONT_SIZE);

        Paragraph resumo = new Paragraph();
        resumo.setSpacingBefore(SUMMARY_SPACING_BEFORE);
        resumo.setAlignment(Element.ALIGN_RIGHT);
        resumo.add(new Chunk("Total: " + stocks.size() + " itens  |  ", boldFont));
        resumo.add(new Chunk("Sem estoque: " + outOfStock + "  |  ", boldFont));
        resumo.add(new Chunk("Baixo: " + lowStock + "  |  ", boldFont));
        resumo.add(new Chunk("Excesso: " + overStock, boldFont));

        document.add(resumo);
    }

}
