package payment.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import payment.models.Invoice;

import java.io.FileOutputStream;

public class PDFUtil {
    public static String generateInvoice(Invoice invoice){
        try{
            Document document = new Document();
            String basepath = "./src/main/resources/static/";
            String title = invoice.getEmail() + "_" +
                            invoice.getTimestamp().toString().replace(":", "-") +
                            ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(basepath + title));

            document.open();
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 36, BaseColor.BLACK);

            document.add(new Paragraph("\n"));
            Chunk chunk = new Chunk("Payment Invoice", font);
            document.add(chunk);

            document.add(new Paragraph("\n"));
            font = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
            chunk = new Chunk(
                    "\nEmail           : " + invoice.getEmail() +
                    "\nEvent           : " + invoice.getEvent_id() +
                    "\nTicket id       : " + invoice.getTicket_id() +
                    "\nTimestamp  : " + invoice.getTimestamp().toString() +
                    "\nStatus          : " + (invoice.getSuccess()? "SUCCESS" : "FAILED")
                    , font);
            document.add(chunk);

            document.close();

            System.out.println("PDF successfully created");
            return title;
        } catch (Exception e){
            System.out.println("Failed creating PDF");
            return null;
        }
    }
}
