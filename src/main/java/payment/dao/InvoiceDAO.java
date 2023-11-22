package payment.dao;

import org.hibernate.SessionFactory;
import payment.models.Invoice;

public class InvoiceDAO extends DAO<Invoice>{
    public InvoiceDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
