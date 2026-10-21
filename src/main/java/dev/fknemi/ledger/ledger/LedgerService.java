package dev.fknemi.ledger.ledger;

import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.common.util.MoneyUtils;
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

    /**
     * Returns the balance for the given account and currency.
     *
     * <p>
     * <b>Locking invariant:</b> {@code getBalance()} is only safe to call after the
     * caller has locked the account row (via
     * {@code AccountService.findByIdForUpdate}).
     * Reading a balance without holding the row lock allows a concurrent transfer
     * or
     * conversion to debit between the read and the subsequent {@code record(...)},
     * which can overdraw the account. Every caller must therefore:
     *
     * <ol>
     * <li>acquire {@code PESSIMISTIC_WRITE} locks on all involved accounts, in the
     * deterministic order produced by
     * {@link dev.fknemi.ledger.common.util.LockOrdering},</li>
     * <li>then call {@code getBalance(...)}, and</li>
     * <li>post the corresponding {@code record(...)} calls before the transaction
     * commits.</li>
     * </ol>
     *
     * Callers that only display balances (e.g. read-only reporting) may call this
     * without the lock, accepting that the value is a point-in-time snapshot.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId, String currency) {
        return ledgerEntryRepository.sumByAccountIdAndCurrency(accountId, currency);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalanceResponse(UUID accountId, String currency) {
        return new BalanceResponse(
                accountId,
                currency,
                MoneyUtils.round(getBalance(accountId, currency), currency));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntriesForAccount(UUID accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntriesForTransaction(UUID transactionId) {
        return ledgerEntryRepository.findByTransactionId(transactionId);
    }

    /**
     * Write a single credit (+) or debit (-) entry. Called only from
     * TransactionService.
     */
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
