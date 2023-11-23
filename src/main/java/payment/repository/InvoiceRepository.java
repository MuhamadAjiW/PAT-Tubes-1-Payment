package payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import payment.models.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    void deleteByInvoiceNumber(String invoice_number);
    Invoice findByInvoiceNumber(String invoice_number);
}
