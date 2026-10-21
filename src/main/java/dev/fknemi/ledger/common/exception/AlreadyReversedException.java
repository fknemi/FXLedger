package dev.fknemi.ledger.common.exception;

import java.util.UUID;

public class AlreadyReversedException extends RuntimeException {
    public AlreadyReversedException(UUID transactionId) {
        super("Transaction already reversed: " + transactionId);
    }
}
