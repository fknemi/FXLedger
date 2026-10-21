package dev.fknemi.ledger.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
    @Override public HttpStatus status() { return HttpStatus.UNPROCESSABLE_ENTITY; }
}
