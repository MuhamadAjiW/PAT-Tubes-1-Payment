package payment.services;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.enums.HttpRequestMethod;
import payment.enums.PaymentStatus;
import payment.repository.InvoiceRepository;
import payment.models.Invoice;
import payment.util.ApiUtil;
import payment.util.FailureUtil;
import payment.util.SignatureUtil;
import payment.util.classes.InvoiceRequest;
import payment.util.classes.PaymentRequest;
import payment.util.classes.Response;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentService {
    private final String paymentEndpoint = "/payment";
    private final String TicketServiceWebhookURL = ApiUtil.TicketServiceURL + "/webhook";
    private final String TicketServiceWebhookPaymentEndpoint = TicketServiceWebhookURL + paymentEndpoint;
    private final InvoiceRepository invoiceRepository;
    private final RabbitTemplate rabbitTemplate;

    private String invoiceWebhook;
    private String paymentWebhook;

    @Autowired
    public PaymentService(InvoiceRepository invoiceRepository, RabbitTemplate rabbitTemplate){
        this.invoiceRepository = invoiceRepository;
        this.rabbitTemplate = rabbitTemplate;

        try {
            Response response = ApiUtil.call(TicketServiceWebhookURL + "/clients", HttpRequestMethod.POST);

            System.out.println(response);
            if(response.isValid()){
                ApiUtil.TicketWebhookToken = (String) ((JSONObject)response.getData()).get("token");
                System.out.println(ApiUtil.TicketWebhookToken);

                JSONObject webhookdata = new JSONObject();
                webhookdata.put("eventName", "payment");
                webhookdata.put("endpoint", paymentEndpoint);

                response = ApiUtil.call(TicketServiceWebhookURL, HttpRequestMethod.POST, webhookdata, ApiUtil.TicketWebhookToken);
                System.out.println(response);
            } else{
                System.out.println("Webhook already registered");
                //TODO: Add special privilege method to reclaim token and recheck webhook addresses
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Payment Service failed to register its webhooks");
        }
    }

    public static String generateInvoiceNumber() {
        return "INV" + UUID.randomUUID().toString().toUpperCase();
    }

    @GetMapping("")
    public String gateway(){
        return "Payment Server is Running";
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody InvoiceRequest invoiceRequest){
        //TODO: This should trigger a webhook, not a rabbitMQ message

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
            String message = "Failed to generate signature";

            System.out.println(message);

            return ResponseEntity.internalServerError().build();
        }


        this.invoiceRepository.save(invoice);
        String url = "/api/payments/pay?signature=" + signature;

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("url", url);
        jsonResponse.put("invoiceNumber", invoice.getInvoiceNumber());

        try {
            System.out.println("Triggering invoice request webhook");
            Response response = ApiUtil.call(TicketServiceWebhookPaymentEndpoint, HttpRequestMethod.POST, jsonResponse, ApiUtil.TicketWebhookToken);
            System.out.println(response);
        } catch (Exception e){
            this.invoiceRepository.delete(invoice);
            e.printStackTrace();
        }

        return ResponseEntity.ok().body(new Response("Invoice Request Success", true, jsonResponse).toJsonString());
    }

    @PostMapping(value = "/pay", params = "signature")
    public ResponseEntity<?> pay(@RequestParam String signature, @RequestBody PaymentRequest paymentRequest){
        //TODO: This should trigger a webhook, not a rabbitMQ message

        System.out.println("Received payment request: " + paymentRequest.getInvoiceNumber());

        boolean valid;
        try {
            valid = SignatureUtil.verifySignature(signature);
        } catch (Exception e){
            e.printStackTrace();
            String message = "Failed to verify signature";
            System.out.println(message);

            return ResponseEntity.status(401).build();
        }

        if(valid){
            String signInvoiceNumber;
            try {
                signInvoiceNumber = SignatureUtil.getIdentifier(signature);
            } catch (Exception e){
                e.printStackTrace();
                String message = "Failed to decode invoice number";
                System.out.println(message);

                return ResponseEntity.status(400).build();
            }

            if(signInvoiceNumber == null){
                return ResponseEntity.badRequest().build();
            }
            if(!Objects.equals(paymentRequest.getInvoiceNumber(), signInvoiceNumber)){
                String message = "Invoice number did not match";

                System.out.println(message);
                System.out.println("Signature: " + signInvoiceNumber);
                System.out.println("Body: " + paymentRequest.getInvoiceNumber());

                return ResponseEntity.status(401).build();
            }

            try {
                Invoice invoice = this.invoiceRepository.findByInvoiceNumber(signInvoiceNumber);

                // Reject successfully paid invoices
                if(invoice.getStatus() == PaymentStatus.DONE){
                    String message = "Payment has already been done";
                    System.out.println(message);

                    return ResponseEntity.status(409).build();
                }

                boolean fail = FailureUtil.simulate();
                if(fail){
                    invoice.setStatus(PaymentStatus.FAILED);
                } else{
                    invoice.setStatus(PaymentStatus.DONE);
                }
                invoice = this.invoiceRepository.save(invoice);

                String message = "Payment success";
                System.out.println(message);

                return ResponseEntity.ok().build();
            } catch (Exception e){
                String message = "Failed to execute query or generate pdf url";
                System.out.println(message);

                return ResponseEntity.internalServerError().build();
            }
        } else{
            String message = "Invalid signature";
            System.out.println(message);

            return ResponseEntity.badRequest().build();
        }
    }


}
