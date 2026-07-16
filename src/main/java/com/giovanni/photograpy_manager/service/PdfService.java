package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.billing.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    /**
     * Generate an invoice/receipt PDF from a Thymeleaf template.
     * @param invoice the invoice to render
     * @param isReceipt true if this is a receipt (reçu) for a paid invoice
     * @return PDF bytes
     */
    public byte[] generateInvoicePdf(Invoice invoice, boolean isReceipt) {
        Context ctx = new Context(Locale.FRANCE);
        ctx.setVariable("invoice", invoice);
        ctx.setVariable("isReceipt", isReceipt);
        ctx.setVariable("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        ctx.setVariable("documentTitle", isReceipt ? "REÇU DE PAIEMENT" : "FACTURE");

        String html = templateEngine.process("billing/invoice/pdf-template", ctx);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }
}
