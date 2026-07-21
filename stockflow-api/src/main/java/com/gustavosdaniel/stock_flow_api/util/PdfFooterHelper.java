package com.gustavosdaniel.stock_flow_api.util;

import org.openpdf.text.Document;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.FontFactory;
import org.openpdf.text.pdf.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfFooterHelper extends PdfPageEventHelper {

    private PdfTemplate totalPagesTemplate;
    private BaseFont baseFont;
    private String dataGeracao;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document){

        try {
            baseFont = FontFactory.getFont(FontFactory.HELVETICA).getBaseFont();

            totalPagesTemplate = writer.getDirectContent().createTemplate(30, 16);

            dataGeracao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {

            throw new ExceptionConverter(e);
        }
    }

    public void onEndPage(PdfWriter writer, Document document){

        PdfContentByte pdfContentByte = writer.getDirectContent();
        float textSize = 9f;
        float textBase = document.bottom() - 20;

        pdfContentByte.beginText();
        pdfContentByte.setFontAndSize(baseFont, textSize);

        String textData = "StockFlow - Gerado em: " + dataGeracao;
        pdfContentByte.setTextMatrix(document.left(), textBase);
        pdfContentByte.showText(textData);

        String textPage = "Pagina " + writer.getPageNumber() + "de ";
        float textSizeWidth = baseFont.getWidthPoint(textPage, textSize);
        pdfContentByte.setTextMatrix(document.right() - textSizeWidth - 15, textBase);

        pdfContentByte.endText();

        pdfContentByte.addTemplate(totalPagesTemplate, document.right() - 15, textBase);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document){

        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(baseFont, 9f);
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber()));
        totalPagesTemplate.endText();
    }
}
