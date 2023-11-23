package payment.repository;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import payment.enums.PaymentStatus;
import payment.models.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    void deleteByInvoiceNumber(String invoice_number);
    Invoice findByInvoiceNumber(String invoice_number);
}
