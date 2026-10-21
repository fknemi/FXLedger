package dev.fknemi.ledger.reversal;

import dev.fknemi.ledger.account.AccountService;
import dev.fknemi.ledger.ledger.LedgerEntry;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.transaction.Transaction;
import dev.fknemi.ledger.transaction.TransactionService;
import dev.fknemi.ledger.transaction.TransactionType;
import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReversalService {

    private final TransactionService transactionService;
    private final LedgerService      ledgerService;
    private final AccountService     accountService;

    @Transactional
    public TransactionResponse reverse(UUID originalTransactionId) {
        Transaction original = transactionService.findById(originalTransactionId);

        List<LedgerEntry> entries = ledgerService.getEntriesForTransaction(originalTransactionId);
        if (entries.isEmpty()) throw new RuntimeException("Transaction has no entries");

        Transaction reversal = transactionService.createRaw(TransactionType.REVERSAL, originalTransactionId);

        // Mirror every entry with its sign flipped — works for TRANSFER and CONVERSION alike
        for (LedgerEntry e : entries) {
            ledgerService.record(
                    e.getAccount(),
                    reversal,
                    e.getCurrency(),
                    e.getAmount().negate()
            );
        }

        return TransactionResponse.from(reversal);
    }
}
