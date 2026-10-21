package dev.fknemi.ledger.ledger;

import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.ledger.dto.BalanceResponse;
import dev.fknemi.ledger.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId, String currency) {
        return ledgerEntryRepository.sumByAccountIdAndCurrency(accountId, currency);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalanceResponse(UUID accountId, String currency) {
        return new BalanceResponse(accountId, currency, getBalance(accountId, currency));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntriesForAccount(UUID accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntriesForTransaction(UUID transactionId) {
        return ledgerEntryRepository.findByTransactionId(transactionId);
    }

    /** Write a single credit (+) or debit (-) entry. Called only from TransactionService. */
    @Transactional
    public LedgerEntry record(Account account, Transaction transaction,
                              String currency, BigDecimal amount) {
        LedgerEntry entry = LedgerEntry.builder()
                .account(account)
                .transaction(transaction)
                .currency(currency)
                .amount(amount)
                .build();
        return ledgerEntryRepository.save(entry);
    }
}
