package payment.services;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.enums.HttpRequestMethod;
import payment.enums.PaymentStatus;
import payment.models.WebhookToken;
import payment.repository.InvoiceRepository;
import payment.models.Invoice;
import payment.repository.WebhookTokenRepository;
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
    private final WebhookTokenRepository webhookTokenRepository;
    private final RabbitTemplate rabbitTemplate;

    private String invoiceWebhook;
    private String paymentWebhook;

    @Autowired
    public PaymentService(InvoiceRepository invoiceRepository, WebhookTokenRepository webhookTokenRepository, RabbitTemplate rabbitTemplate){
        this.invoiceRepository = invoiceRepository;
        this.webhookTokenRepository = webhookTokenRepository;
        this.rabbitTemplate = rabbitTemplate;

        try {
            Response response = ApiUtil.call(TicketServiceWebhookURL + "/clients", HttpRequestMethod.POST);

            System.out.println(response);
            if(response.isValid()){
                String paymentToken = (String) ((JSONObject)response.getData()).get("token");
                ApiUtil.TicketWebhookToken = paymentToken;

                WebhookToken token = this.webhookTokenRepository.findByAddress(ApiUtil.TicketServiceURL);
                if(token != null){
                    this.webhookTokenRepository.deleteByToken(token.getToken());
                }

                System.out.println(paymentToken);

                token = new WebhookToken();
                token.setToken(paymentToken);
                token.setAddress(ApiUtil.TicketServiceURL);
                token.setDescription("Webhook for ticketing services");
                this.webhookTokenRepository.insert(token);

                JSONObject webhookData = new JSONObject();
                webhookData.put("eventName", "payment");
                webhookData.put("endpoint", paymentEndpoint);

                response = ApiUtil.call(TicketServiceWebhookURL, HttpRequestMethod.POST, webhookData, ApiUtil.TicketWebhookToken);
                System.out.println(response);
            } else{
                System.out.println("Webhook already registered");
                WebhookToken token = this.webhookTokenRepository.findByAddress(ApiUtil.TicketServiceURL);
                ApiUtil.TicketWebhookToken = token.getToken();

                //Re-register just in case
                JSONObject webhookData = new JSONObject();
                webhookData.put("eventName", "payment");
                webhookData.put("endpoint", paymentEndpoint);

                response = ApiUtil.call(TicketServiceWebhookURL, HttpRequestMethod.POST, webhookData, ApiUtil.TicketWebhookToken);
                System.out.println(response);
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
        System.out.println("Received invoice request: " + invoiceRequest.getEmail() + " " + invoiceRequest.getKursiId());

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setTimestamp(Instant.now());
        invoice.setRequest(invoiceRequest);
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

        return ResponseEntity.ok().body(new Response("Invoice Request Success", true, jsonResponse).toJsonString());
    }

    @PostMapping(value = "/pay", params = "signature")
    public ResponseEntity<?> pay(@RequestParam String signature, @RequestBody PaymentRequest paymentRequest){
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

                JSONObject jsonResponse = new JSONObject(invoice);
                System.out.println(jsonResponse);

                try{
                    System.out.println("Triggering invoice request webhook");
                    Response response = ApiUtil.call(TicketServiceWebhookPaymentEndpoint, HttpRequestMethod.POST, jsonResponse, ApiUtil.TicketWebhookToken);

                    System.out.println("Payment done");
                    return ResponseEntity.ok().build();
                } catch (Exception e){
                    invoice.setStatus(PaymentStatus.ERROR);
                    this.invoiceRepository.save(invoice);

                    System.out.println("Failed to call webhook");
                    return ResponseEntity.internalServerError().build();
                }
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
