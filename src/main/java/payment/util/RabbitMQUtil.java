package payment.util;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQUtil {

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange("payment-exchange");
    }

    @Bean
    public Queue outgoingInvoiceQueue() {
        return new Queue("outgoing-invoice-queue");
    }

    @Bean
    public Binding outgoingInvoicebinding(Queue outgoingInvoiceQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(outgoingInvoiceQueue).to(paymentExchange)
                .with("outgoing-invoice-queue");
    }

    @Bean
    public Queue outgoingPaymentQueue() {
        return new Queue("outgoing-payment-queue");
    }

    @Bean
    public Binding outgoingPaymentbinding(Queue outgoingPaymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(outgoingPaymentQueue).to(paymentExchange)
                .with("outgoing-payment-queue");
    }

    @Bean
    public Queue incomingInvoiceQueue() {
        return new Queue("incoming-invoice-queue");
    }

    @Bean
    public Binding incomingInvoicebinding(Queue incomingInvoiceQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(incomingInvoiceQueue).to(paymentExchange)
                .with("incoming-invoice-queue");
    }

    @Bean
    public Queue incomingPaymentQueue() {
        return new Queue("incoming-payment-queue");
    }

    @Bean
    public Binding incomingPaymentbinding(Queue incomingPaymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(incomingPaymentQueue).to(paymentExchange)
                .with("incoming-payment-queue");
    }
}
