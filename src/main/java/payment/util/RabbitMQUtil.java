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
    public Queue paymentQueue() {
        return new Queue("payment-queue");
    }

    @Bean
    public Binding binding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with("payment-queue");
    }
}
