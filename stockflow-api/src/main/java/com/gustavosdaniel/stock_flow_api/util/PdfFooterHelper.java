package com.gustavosdaniel.stock_flow_api.util;

import org.openpdf.text.Document;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.FontFactory;
import org.openpdf.text.pdf.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {

        totalPagesTemplate.beginText();

        totalPagesTemplate.setFontAndSize(baseFont, FOOTER_FONT_SIZE);

        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));

        totalPagesTemplate.endText();
    }
}
