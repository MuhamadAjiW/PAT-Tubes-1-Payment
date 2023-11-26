package payment.util.classes;

import lombok.Data;

@Data
public class InvoiceRequest {
    private String email;
    private int acaraId;
    private int kursiId;
    private int userId;
    private int bookingId;
}
