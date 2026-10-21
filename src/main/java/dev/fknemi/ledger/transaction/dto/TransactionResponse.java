package dev.fknemi.ledger.transaction.dto;

import dev.fknemi.ledger.transaction.Transaction;
import dev.fknemi.ledger.transaction.TransactionStatus;
import dev.fknemi.ledger.transaction.TransactionType;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        TransactionStatus status,
        UUID reversalOfTransactionId,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getType(), t.getStatus(),
                t.getReversalOfTransactionId(), t.getCreatedAt());
    }
}
