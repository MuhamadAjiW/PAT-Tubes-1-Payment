package payment.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import payment.enums.PaymentStatus;
import payment.util.classes.InvoiceRequest;

import java.time.Instant;

@Data
@Document(collection = "Invoice")
public class Invoice {
    @Id
    private String invoiceNumber;
    private InvoiceRequest request;
    private Instant timestamp;
    private PaymentStatus status;
}
