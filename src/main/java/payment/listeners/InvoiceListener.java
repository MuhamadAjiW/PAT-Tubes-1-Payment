package payment.listeners;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import payment.services.PaymentService;
import payment.util.classes.InvoiceRequest;

@Component
public class InvoiceListener {
    @Autowired
    private PaymentService paymentService;

    @RabbitListener(queues = "incoming-invoice-queue")
    public void processIncomingInvoiceMessage(String message){
        try {
            JSONObject invoiceRequestJSON = new JSONObject(message);
            InvoiceRequest invoiceRequest = new InvoiceRequest();
            invoiceRequest.setEmail((String) invoiceRequestJSON.get("email"));
            invoiceRequest.setEventId((Integer) invoiceRequestJSON.get("eventId"));
            invoiceRequest.setTicketId((Integer) invoiceRequestJSON.get("ticketId"));

            paymentService.create(invoiceRequest);
        } catch (Exception e){
            System.out.println("Bad message received from incoming-invoice-queue: " + message);
        }
    }
}
