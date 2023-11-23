package payment.util.classes;

import lombok.Data;

@Data
public class InvoiceRequest {
    private String email;
    private int eventId;
    private int ticketId;
}
