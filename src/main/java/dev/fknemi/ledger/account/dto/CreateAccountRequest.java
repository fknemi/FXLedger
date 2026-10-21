package dev.fknemi.ledger.account.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank String name) {}
