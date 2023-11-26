package payment.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "WebhookToken")
public class WebhookToken {
    @Id
    private String token;
    @Indexed(unique = true)
    private String address;
    private String description;

}
