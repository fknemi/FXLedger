package dev.fknemi.ledger.account.dto;

import dev.fknemi.ledger.account.Account;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID id, String name, Instant createdAt) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getCreatedAt());
    }
}
