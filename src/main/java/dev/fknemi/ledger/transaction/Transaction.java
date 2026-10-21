package dev.fknemi.ledger.transaction;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(name = "uk_transactions_reversal_of", columnNames = "reversal_of_transaction_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    /** For REVERSAL transactions: the original transaction being reversed. */
    @Column(name = "reversal_of_transaction_id")
    private UUID reversalOfTransactionId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
