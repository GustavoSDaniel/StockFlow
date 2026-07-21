package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.util.PdfFooterHelper;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductPdfReport {

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

    private static final float[] COLUMN_WIDTHS = {4f, 3f, 2f, 2f, 1.5f, 2f};

    private static final Color HEADER_BG_COLOR = new Color(235, 235, 235);

    public byte[] generateReport(List<ProductResponse> products) {

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()){

            Document document = new Document(PageSize.A4.rotate(),
                    MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);

            PdfWriter writer = PdfWriter.getInstance(document, output);

            writer.setPageEvent(new PdfFooterHelper());

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, TITLE_FONT_SIZE);
            Paragraph title = new Paragraph("StockFlow - Relatório de Produtos", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(TITLE_SPACING_AFTER);
            document.add(title);

            PdfPTable table = new PdfPTable(COLUMN_WIDTHS);
            table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);

            String[] headers = {"Nome", "SKU", "P. Custo", "P. Venda", "Unidade", "Status"};

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, HEADER_FONT_SIZE);

            for (String h : headers){
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(HEADER_BG_COLOR);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(HEADER_PADDING);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, CELL_FONT_SIZE);

            for (ProductResponse p : products) {

                addCell(table, p.name(), cellFont, Element.ALIGN_LEFT);
                addCell(table, p.sku(), cellFont, Element.ALIGN_LEFT);

                addCell(table, formatarMoeda(p.costPrice()), cellFont, Element.ALIGN_RIGHT);
                addCell(table, formatarMoeda(p.salePrice()), cellFont, Element.ALIGN_RIGHT);

                String unitStr = p.unitMeasure() != null ? p.unitMeasure().name() : "-";
                addCell(table, unitStr, cellFont, Element.ALIGN_CENTER);

                String statusStr = "-";
                if (p.status() != null) {
                    statusStr = switch (p.status()) {
                        case ACTIVE -> "Ativo";
                        case INACTIVE -> "Inativo";
                        case DISCONTINUED -> "Descontinuado";
                    };
                }
                addCell(table, statusStr, cellFont, Element.ALIGN_CENTER);
            }

            document.add(table);

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, SUMMARY_FONT_SIZE);
            Paragraph resumo = new Paragraph("Total de Produtos: " + products.size(), boldFont);
            resumo.setSpacingBefore(SUMMARY_SPACING_BEFORE);
            resumo.setAlignment(Element.ALIGN_RIGHT);
            document.add(resumo);

            document.close();
            return output.toByteArray();

        } catch (Exception e){
            throw new RuntimeException("Erro ao gerar o relatório de produtos em PDF", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment){
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(CELL_PADDING);
        table.addCell(cell);
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        return String.format("R$ %,.2f", valor);
    }
}
