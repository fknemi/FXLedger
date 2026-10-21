package dev.fknemi.ledger.common.exception;

import org.springframework.http.HttpStatus;
import java.util.UUID;

public class AccountNotFoundException extends ApiException {
    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }
    @Override public HttpStatus status() { return HttpStatus.NOT_FOUND; }
}
