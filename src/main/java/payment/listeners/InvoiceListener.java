package payment.listeners;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import payment.services.PaymentService;
import payment.util.classes.InvoiceRequest;

//@Component
public class InvoiceListener {
//    @Autowired
//    private PaymentService paymentService;
//
//    @RabbitListener(queues = "incoming-invoice-queue")
//    public void processIncomingInvoiceMessage(String message){
//        try {
//            JSONObject invoiceRequestJSON = new JSONObject(message);
//            InvoiceRequest invoiceRequest = new InvoiceRequest();
//            invoiceRequest.setEmail((String) invoiceRequestJSON.get("email"));
//            invoiceRequest.setAcaraId((Integer) invoiceRequestJSON.get("acaraId"));
//            invoiceRequest.setKursiId((Integer) invoiceRequestJSON.get("kursiId"));
//            invoiceRequest.setUserId((Integer) invoiceRequestJSON.get("userId"));
//
//            paymentService.create(invoiceRequest);
//        } catch (Exception e){
//            System.out.println("Bad message received from incoming-invoice-queue: " + message);
//        }
//    }
}
