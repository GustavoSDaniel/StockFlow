package com.gustavosdaniel.stock_flow_api.util;

import org.openpdf.text.Document;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.FontFactory;
import org.openpdf.text.pdf.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * OpenPDF page event helper that renders a footer on every page of a PDF.
 * <p>
 * The footer displays "StockFlow - Generated on: dd/MM/yyyy HH:mm" on the left
 * and "Page X of Y" on the right. The total page count is filled in when the
 * document is closed.
 * </p>
 */
public class PdfFooterHelper extends PdfPageEventHelper {

    private static final float FOOTER_FONT_SIZE = 9f;
    private static final float FOOTER_MARGIN_BOTTOM = 20f;

    private static final float TEMPLATE_WIDTH = 30f;
    private static final float TEMPLATE_HEIGHT = 16f;

    private static final float TEXT_RIGHT_MARGIN = 30f;
    private static final float TEMPLATE_RIGHT_MARGIN = 28f;

    private PdfTemplate totalPagesTemplate;
    private BaseFont baseFont;
    private String dataGeracao;

    /**
     * Initializes the footer resources when the PDF document is opened:
     * base font, total-pages template, and the generation timestamp.
     *
     * @param writer   the PDF writer
     * @param document the PDF document
     */
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        try {

            baseFont = FontFactory.getFont(FontFactory.HELVETICA).getBaseFont();

            totalPagesTemplate = writer.getDirectContent().createTemplate(TEMPLATE_WIDTH, TEMPLATE_HEIGHT);

            dataGeracao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        } catch (Exception e) {

            throw new ExceptionConverter(e);
        }
    }

    /**
     * Draws the footer on the current page with generation date and page number.
     *
     * @param writer   the PDF writer
     * @param document the PDF document
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        PdfContentByte cb  = writer.getDirectContent();

        float textSize = FOOTER_FONT_SIZE;

        float textBase = document.bottom() - FOOTER_MARGIN_BOTTOM;

        cb.beginText();
        cb.setFontAndSize(baseFont, textSize);

        cb.setTextMatrix(document.left(), textBase);
        cb.showText("StockFlow - Gerado em: " + dataGeracao);

        String pageText     = "Página " + writer.getPageNumber() + " de ";

        float pageTextWidth = baseFont.getWidthPoint(pageText, textSize);

        cb.setTextMatrix(document.right() - pageTextWidth - TEXT_RIGHT_MARGIN, textBase);
        cb.showText(pageText);

        cb.endText();

        cb.addTemplate(totalPagesTemplate, document.right() - TEMPLATE_RIGHT_MARGIN, textBase);
    }

    /**
     * Fills the total-pages template with the final page count when the
     * document is closed.
     *
     * @param writer   the PDF writer
     * @param document the PDF document
     */
    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {

        totalPagesTemplate.beginText();

        totalPagesTemplate.setFontAndSize(baseFont, FOOTER_FONT_SIZE);

        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));

        totalPagesTemplate.endText();
    }
}
