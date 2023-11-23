package payment.listeners;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import payment.services.PaymentService;
import payment.util.classes.InvoiceRequest;
import payment.util.classes.PaymentRequest;

public class PaymentListener {
//    TODO: Implement?
//    @Autowired
//    private PaymentService paymentService;
//
//    @RabbitListener(queues = "incoming-payment-queue")
//    public void processIncomingInvoiceMessage(String message){
//        try {
//            JSONObject paymentRequestJSON = new JSONObject(message);
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.setInvoiceNumber((String) paymentRequestJSON.get("invoiceNumber"));
//
//            paymentService.pay()
//        } catch (Exception e){
//            System.out.println("Bad message received from incoming-invoice-queue: " + message);
//        }
//    }
}
