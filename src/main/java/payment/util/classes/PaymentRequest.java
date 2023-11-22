package payment.util.classes;

import lombok.Data;

@Data
public class PaymentRequest {
    private String email;
    private int event_id;
    private int ticket_id;
}
