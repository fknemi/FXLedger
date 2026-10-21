package dev.fknemi.ledger.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsByReversalOfTransactionId(UUID reversalOfTransactionId);
}
