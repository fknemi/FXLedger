package dev.fknemi.ledger.fx;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 3)
    private String fromCurrency;

    @Column(nullable = false, length = 3)
    private String toCurrency;

    @Column(nullable = false, precision = 38, scale = 10)
    private BigDecimal rate;

    @Column(nullable = false, updatable = false)
    private Instant effectiveAt;

    @PrePersist
    void prePersist() { if (effectiveAt == null) this.effectiveAt = Instant.now(); }
}
