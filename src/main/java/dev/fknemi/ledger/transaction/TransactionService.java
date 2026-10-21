package dev.fknemi.ledger.transaction;

import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.account.AccountService;
import dev.fknemi.ledger.common.exception.InsufficientFundsException;
import dev.fknemi.ledger.common.util.LockOrdering;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import dev.fknemi.ledger.transaction.dto.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final LedgerService ledgerService;

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        List<UUID> ordered = LockOrdering.sort(request.fromAccountId(), request.toAccountId());
        // TODO: acquire DB-level advisory locks in ordered order
        Account from = accountService.findEntityById(ordered.get(0).equals(request.fromAccountId())
                ? request.fromAccountId() : request.toAccountId());
        Account to   = accountService.findEntityById(request.toAccountId().equals(from.getId())
                ? request.fromAccountId() : request.toAccountId());

        // Simpler: just load both
        Account sender   = accountService.findEntityById(request.fromAccountId());
        Account receiver = accountService.findEntityById(request.toAccountId());

        BigDecimal balance = ledgerService.getBalance(sender.getId(), request.currency());
        if (balance.compareTo(request.amount()) < 0) throw new InsufficientFundsException();

        Transaction tx = transactionRepository.save(Transaction.builder()
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .build());

        ledgerService.record(sender,   tx, request.currency(), request.amount().negate());
        ledgerService.record(receiver, tx, request.currency(), request.amount());

        return TransactionResponse.from(tx);
    }

    /** Used by ReversalService and ConversionService to persist a raw transaction shell. */
    @Transactional
    public Transaction createRaw(TransactionType type, UUID reversalOf) {
        return transactionRepository.save(Transaction.builder()
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .reversalOfTransactionId(reversalOf)
                .build());
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }
}
