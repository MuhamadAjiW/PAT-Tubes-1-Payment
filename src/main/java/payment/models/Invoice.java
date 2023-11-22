package payment.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Boolean success;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int event_id;

    @Column(nullable = false)
    private int ticket_id;
}
