package dev.fknemi.ledger.audit;

import dev.fknemi.ledger.ledger.LedgerEntry;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.ledger.dto.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts/{accountId}")
@RequiredArgsConstructor
public class AuditController {

    private final LedgerService ledgerService;

    @GetMapping("/balance")
    public BalanceResponse balance(@PathVariable UUID accountId,
                                   @RequestParam String currency) {
        return ledgerService.getBalanceResponse(accountId, currency);
    }

    @GetMapping("/entries")
    public List<LedgerEntry> entries(@PathVariable UUID accountId) {
        return ledgerService.getEntriesForAccount(accountId);
    }
}
