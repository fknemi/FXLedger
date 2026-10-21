package dev.fknemi.ledger.fx;

import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.account.AccountService;
import dev.fknemi.ledger.common.exception.InsufficientFundsException;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.transaction.Transaction;
import dev.fknemi.ledger.transaction.TransactionService;
import dev.fknemi.ledger.transaction.TransactionType;
import dev.fknemi.ledger.transaction.dto.ConvertRequest;
import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ConversionService {

    private final AccountService     accountService;
    private final LedgerService      ledgerService;
    private final TransactionService transactionService;
    private final FxRateService      fxRateService;
    private final FxSuspenseLookup   fxSuspenseLookup;

    @Transactional
    public TransactionResponse convert(ConvertRequest request) {
        Account sender   = accountService.findEntityById(request.fromAccountId());
        Account receiver = accountService.findEntityById(request.toAccountId());

        BigDecimal balance = ledgerService.getBalance(sender.getId(), request.fromCurrency());
        if (balance.compareTo(request.amount()) < 0) throw new InsufficientFundsException();

        BigDecimal rate = fxRateService.getCurrentRate(request.fromCurrency(), request.toCurrency());
        // Destination-currency rounding (2 dp default; extend per currency as needed)
        BigDecimal converted = request.amount().multiply(rate).setScale(2, RoundingMode.HALF_EVEN);

        Account fromSuspense = accountService.findEntityById(
                fxSuspenseLookup.getAccountId(request.fromCurrency()));
        Account toSuspense   = accountService.findEntityById(
                fxSuspenseLookup.getAccountId(request.toCurrency()));

        Transaction tx = transactionService.createRaw(TransactionType.CONVERSION, null);

        // Step 1: sender → from-currency suspense
        ledgerService.record(sender,       tx, request.fromCurrency(), request.amount().negate());
        ledgerService.record(fromSuspense, tx, request.fromCurrency(), request.amount());

        // Step 2: to-currency suspense → receiver
        ledgerService.record(toSuspense, tx, request.toCurrency(), converted.negate());
        ledgerService.record(receiver,   tx, request.toCurrency(), converted);

        return TransactionResponse.from(tx);
    }
}
