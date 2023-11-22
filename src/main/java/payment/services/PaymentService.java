package payment.services;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import payment.dao.InvoiceDAO;
import payment.middleware.FailureMiddleware;
import payment.models.Invoice;
import payment.util.HibernateUtil;
import payment.util.classes.PaymentRequest;

import java.time.Instant;

@RestController
public class PaymentService {
    private final InvoiceDAO invoiceDAO;

    public PaymentService(){
        this.invoiceDAO = new InvoiceDAO(HibernateUtil.getSessionFactory());
    }

    @GetMapping("/api/payments")
    public String gateway(){
        return "Payment Server is Running";
    }

    @PostMapping("/api/payments")
    public String create(@RequestBody PaymentRequest paymentRequest){
        boolean fail = FailureMiddleware.simulate();

        System.out.println("Received payment data: " + paymentRequest.getEmail() + " " + paymentRequest.getTicket_id());

        Invoice invoice = new Invoice();
        invoice.setTimestamp(Instant.now());
        invoice.setEmail(paymentRequest.getEmail());
        invoice.setEvent_id(paymentRequest.getEvent_id());
        invoice.setTicket_id(paymentRequest.getTicket_id());
        invoice.setSuccess(!fail);
        this.invoiceDAO.insertData(invoice);

        if(fail){
            System.out.println("Payment failure!");
            // TODO: Simulate failure
        }
        else{
            System.out.println("Payment successful!");
            // TODO: Simulate success
        }

        return "Payment Server is Running";
    }
}
