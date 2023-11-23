package payment.services;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.enums.PaymentStatus;
import payment.repository.InvoiceRepository;
import payment.models.Invoice;
import payment.util.FailureUtil;
import payment.util.SignatureUtil;
import payment.util.classes.InvoiceRequest;
import payment.util.classes.PaymentRequest;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
//    private final RabbitTemplate rabbitTemplate;

    public static String generateInvoiceNumber() {
        return "INV" + UUID.randomUUID().toString().toUpperCase();
    }
    @Autowired
    public PaymentService(InvoiceRepository invoiceRepository){
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("")
    public String gateway(){
        return "Payment Server is Running";
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody InvoiceRequest invoiceRequest){
        System.out.println("Received invoice request: " + invoiceRequest.getEmail() + " " + invoiceRequest.getTicketId());

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setTimestamp(Instant.now());
        invoice.setEmail(invoiceRequest.getEmail());
        invoice.setEventId(invoiceRequest.getEventId());
        invoice.setTicketId(invoiceRequest.getTicketId());
        invoice.setStatus(PaymentStatus.PENDING);

        String signature = SignatureUtil.generateSignature(invoice.getInvoiceNumber(), SignatureUtil.PaymentExpiry);
        if (signature == null){
            return ResponseEntity.internalServerError().build();
        }


        this.invoiceRepository.save(invoice);
        String url = "/api/payments/pay?signature=" + signature;
        String jsonResponse = String.format("{\"url\":\"%s\",\"invoiceNumber\":\"%s\"}", url, invoice.getInvoiceNumber());

        return ResponseEntity.ok().body(jsonResponse);
    }

    @PostMapping(value = "/pay", params = "signature")
    public ResponseEntity<?> download(@RequestParam String signature, @RequestBody PaymentRequest paymentRequest){
        System.out.println("Received payment request: " + paymentRequest.getInvoiceNumber());

        boolean valid;
        try {
            valid = SignatureUtil.verifySignature(signature);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to verify signature");
            return ResponseEntity.badRequest().build();
        }

        if(valid){
            String signInvoiceNumber;
            try {
                signInvoiceNumber = SignatureUtil.getIdentifier(signature);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to decode invoice number");
                return ResponseEntity.notFound().build();
            }

            if(signInvoiceNumber == null){
                return ResponseEntity.badRequest().build();
            }
            if(!Objects.equals(paymentRequest.getInvoiceNumber(), signInvoiceNumber)){
                System.out.println("Invoice number did not match");
                System.out.println("Signature: " + signInvoiceNumber);
                System.out.println("Body: " + paymentRequest.getInvoiceNumber());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            try {
                Invoice invoice = this.invoiceRepository.findByInvoiceNumber(signInvoiceNumber);

                if(invoice.getStatus() == PaymentStatus.DONE){
                    return ResponseEntity.badRequest().build();
                }

                boolean fail = FailureUtil.simulate();
                if(fail){
                    // TODO: Handle failure
                    invoice.setStatus(PaymentStatus.FAILED);
                } else{
                    // TODO: Handle success
                    invoice.setStatus(PaymentStatus.DONE);
                }
                invoice = this.invoiceRepository.save(invoice);

                String title = PDFService.generateInvoicePDF(invoice);
                String pdfSignature = SignatureUtil.generateSignature(title, SignatureUtil.PDFexpiry);
                String url = "/pdf/file?signature=" + pdfSignature;

                String jsonResponse = String.format("{\"url\":\"%s\",\"invoiceNumber\":\"%s\"}", url, invoice.getInvoiceNumber());

                return ResponseEntity.ok().body(jsonResponse);
            } catch (Exception e){
                System.out.println("Failed to execute query or generate pdf url");
                return ResponseEntity.internalServerError().build();
            }
        } else{
            System.out.println("Invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
