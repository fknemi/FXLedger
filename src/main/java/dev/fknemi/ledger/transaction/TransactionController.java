package dev.fknemi.ledger.transaction;

import dev.fknemi.ledger.fx.ConversionService;
import dev.fknemi.ledger.transaction.dto.ConvertRequest;
import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import dev.fknemi.ledger.transaction.dto.TransferRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final ConversionService conversionService;
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @PostMapping("/convert")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse convert(@Valid @RequestBody ConvertRequest request) {
        return conversionService.convert(request);
    }
}
