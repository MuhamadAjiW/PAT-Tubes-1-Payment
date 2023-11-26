package payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import payment.models.WebhookToken;

public interface WebhookTokenRepository extends MongoRepository<WebhookToken, String> {
    void deleteByToken(String token);
    WebhookToken findByAddress(String address);
}

