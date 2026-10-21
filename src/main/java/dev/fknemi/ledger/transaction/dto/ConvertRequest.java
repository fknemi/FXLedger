package dev.fknemi.ledger.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ConvertRequest(
        @NotNull UUID fromAccountId,
        @NotNull UUID toAccountId,
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
