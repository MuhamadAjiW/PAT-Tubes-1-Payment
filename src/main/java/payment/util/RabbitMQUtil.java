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
    public Queue invoiceQueue() {
        return new Queue("invoice-queue");
    }

    @Bean
    public Binding invoicebinding(Queue invoiceQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(invoiceQueue).to(paymentExchange)
                .with("invoice-queue");
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue("payment-queue");
    }

    @Bean
    public Binding paymentbinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder
                .bind(paymentQueue).to(paymentExchange)
                .with("payment-queue");
    }
}
