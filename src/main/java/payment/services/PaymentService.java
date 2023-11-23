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
    public ResponseEntity<?> create(@RequestBody InvoiceRequest paymentRequest){
        System.out.println("Received payment data: " + paymentRequest.getEmail() + " " + paymentRequest.getTicketId());

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setTimestamp(Instant.now());
        invoice.setEmail(paymentRequest.getEmail());
        invoice.setEventId(paymentRequest.getEventId());
        invoice.setTicketId(paymentRequest.getTicketId());
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
                boolean fail = FailureUtil.simulate();
                if(fail){
                    // TODO: Handle failure
                    return ResponseEntity.internalServerError().build();
                } else{
                    // TODO: Build and get PDF
                    Invoice invoice = this.invoiceRepository.findByInvoiceNumber(signInvoiceNumber);
                    invoice.setStatus(PaymentStatus.DONE);
                    invoice = this.invoiceRepository.save(invoice);
                    return ResponseEntity.ok().body(invoice);
                }

            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed to execute query");
                return ResponseEntity.notFound().build();
            }
        } else{
            System.out.println("Invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}