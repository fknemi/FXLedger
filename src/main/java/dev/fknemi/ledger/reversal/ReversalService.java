package dev.fknemi.ledger.reversal;

import dev.fknemi.ledger.account.AccountService;
import dev.fknemi.ledger.ledger.LedgerEntry;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.transaction.Transaction;
import dev.fknemi.ledger.transaction.TransactionRepository;
import dev.fknemi.ledger.transaction.TransactionService;
import dev.fknemi.ledger.transaction.TransactionType;
import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import dev.fknemi.ledger.common.exception.AlreadyReversedException;
import dev.fknemi.ledger.transaction.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.common.util.LockOrdering;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReversalService {

    private final TransactionService transactionService;
    private final LedgerService ledgerService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse reverse(UUID originalTransactionId) {
        Transaction original = transactionService.findById(originalTransactionId);
        if (original.getStatus() == TransactionStatus.REVERSED) // check 1
            throw new AlreadyReversedException(originalTransactionId);
        if (transactionRepository.existsByReversalOfTransactionId(originalTransactionId))
            throw new AlreadyReversedException(originalTransactionId); // check 2
        List<LedgerEntry> entries = ledgerService.getEntriesForTransaction(originalTransactionId);
        if (entries.isEmpty())
            throw new RuntimeException("Transaction has no entries");

        List<UUID> ordered = LockOrdering.sort(
                entries.stream()
                        .map(e -> e.getAccount().getId())
                        .distinct()
                        .toArray(UUID[]::new)); // sort

        Map<UUID, Account> locked = new HashMap<>(ordered.size());
        // locks aquired
        for (UUID id : ordered) {
            locked.put(id, accountService.findByIdForUpdate(id));
        }

        Transaction reversal = transactionService.createRaw(TransactionType.REVERSAL, originalTransactionId);

        for (LedgerEntry e : entries) {
            Account account = locked.get(e.getAccount().getId());
            ledgerService.record(
                    account,
                    reversal,
                    e.getCurrency(),
                    e.getAmount().negate());
        }

        transactionService.markReversed(original.getId());

        return TransactionResponse.from(reversal);
    }
}
