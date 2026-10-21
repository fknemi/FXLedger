package dev.fknemi.ledger.common.exception;

import org.springframework.http.HttpStatus;

public class CurrencyMismatchException extends ApiException {
    public CurrencyMismatchException() {
        super("Currency mismatch");
    }
    @Override public HttpStatus status() { return HttpStatus.UNPROCESSABLE_ENTITY; }
}
