package payment.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import payment.models.Invoice;
import payment.util.SignatureUtil;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@RestController
@RequestMapping("/pdf")
public class PDFService {
    public static String basepath = "./src/main/resources/static/";
    @GetMapping("/gateway")
    public String gateway(){
        return "PDF Server is Running";
    }

    @GetMapping(value = "/file", params = "signature")
    public ResponseEntity<Resource> download(@RequestParam String signature){
        boolean valid;
        try {
            valid = SignatureUtil.verifySignature(signature);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to verify signature");
            return ResponseEntity.badRequest().build();
        }

        if(valid){
            String title;
            try {
                title = SignatureUtil.getIdentifier(signature);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to decode title");
                return ResponseEntity.notFound().build();
            }

            if(title == null){
                return ResponseEntity.notFound().build();
            }

            try {
                String filepath = basepath + title;
                Path path = Paths.get(filepath);
                Resource resource = new UrlResource(path.toUri());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + title);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to load resource");
                return ResponseEntity.notFound().build();
            }
        } else{
            System.out.println("Invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public static String generateInvoicePDF(Invoice invoice) throws Exception{
        try{
            Document document = new Document();
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
                            "\nEvent           : " + invoice.getEventId() +
                            "\nTicket id       : " + invoice.getTicketId() +
                            "\nTimestamp  : " + invoice.getTimestamp().toString() +
                            "\nStatus          : " + invoice.getStatus().getString()
                    , font);
            document.add(chunk);

            document.close();

            System.out.println("PDF successfully created");
            return title;
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
