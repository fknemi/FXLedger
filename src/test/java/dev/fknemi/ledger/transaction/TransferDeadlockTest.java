package dev.fknemi.ledger.transaction;

import dev.fknemi.ledger.TestContainersConfiguration;
import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.account.AccountService;
import dev.fknemi.ledger.account.dto.CreateAccountRequest;
import dev.fknemi.ledger.common.exception.InsufficientFundsException;
import dev.fknemi.ledger.ledger.LedgerService;
import dev.fknemi.ledger.transaction.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class TransferDeadlockTest {

    @Autowired
    TransactionService transactionService;
    @Autowired
    AccountService accountService;

    @Autowired
    LedgerService ledgerService;
    @Autowired
    TransactionRepository transactionRepository;
    private static final int ITERATIONS = 100;
    private static final int PARALLELISM = 2;

    @Test
    void oppositeDirectionTransfersDoNotDeadlock() throws Exception {
        // Seed accounts with plenty of funds so InsufficientFunds never masks a
        // deadlock.
        UUID a = accountService.create(new CreateAccountRequest("A")).id();
        UUID b = accountService.create(new CreateAccountRequest("B")).id();
        seedFunds(a, "USD", new BigDecimal("1000000"));
        seedFunds(b, "USD", new BigDecimal("1000000"));

        ExecutorService pool = Executors.newFixedThreadPool(PARALLELISM);
        AtomicInteger deadlocks = new AtomicInteger();
        AtomicInteger otherFailures = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < ITERATIONS; i++) {
                // Adversarial ordering: A->B and B->A fired at the same time.
                futures.add(pool.submit(() -> runTransfer(a, b)));
                futures.add(pool.submit(() -> runTransfer(b, a)));
            }
            for (Future<?> f : futures) {
                try {
                    f.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (isDeadlock(cause)) {
                        deadlocks.incrementAndGet();
                    } else {
                        otherFailures.incrementAndGet();
                        cause.printStackTrace();
                    }
                } catch (TimeoutException e) {
                    // A hung lock that never times out would land here — that's also a failure.
                    otherFailures.incrementAndGet();
                    e.printStackTrace();
                }
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(deadlocks.get())
                .as("deadlocks observed — LockOrdering is not preventing them")
                .isZero();
        assertThat(otherFailures.get())
                .as("non-deadlock failures — investigate")
                .isZero();
    }

    private void runTransfer(UUID from, UUID to) {
        transactionService.transfer(new TransferRequest(
                from, to, "USD", new BigDecimal("1.00")));
    }

    private void seedFunds(UUID accountId, String currency, BigDecimal amount) {
        Account account = accountService.findEntityById(accountId);
        Transaction seed = transactionRepository.save(Transaction.builder()
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .build());
        ledgerService.record(account, seed, currency, amount);
    }

    private static boolean isDeadlock(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String msg = c.getMessage() == null ? "" : c.getMessage().toLowerCase();
            if (c instanceof CannotAcquireLockException
                    || c instanceof PessimisticLockingFailureException
                    || (c instanceof ObjectOptimisticLockingFailureException)
                    || msg.contains("deadlock")
                    || msg.contains("could not serialize")
                    || msg.contains("lock timeout")) {
                return true;
            }
        }
        return false;
    }

    @Test
    void concurrentWithdrawalsNeverOverdraw() throws Exception {
        UUID source = accountService.create(new CreateAccountRequest("SRC")).id();
        UUID sink1 = accountService.create(new CreateAccountRequest("S1")).id();
        UUID sink2 = accountService.create(new CreateAccountRequest("S2")).id();
        seedFunds(source, "USD", new BigDecimal("100.00")); // enough for exactly 10 x 10.00

        int attempts = 20; // 20 x 10.00 against 100.00
        ExecutorService pool = Executors.newFixedThreadPool(8);
        AtomicInteger succeeded = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            UUID to = (i % 2 == 0) ? sink1 : sink2;
            futures.add(pool.submit(() -> {
                try {
                    transactionService.transfer(new TransferRequest(source, to, "USD", new BigDecimal("10.00")));
                    succeeded.incrementAndGet();
                } catch (InsufficientFundsException expected) {
                    /* fine */ }
            }));
        }
        for (Future<?> f : futures)
            f.get(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        BigDecimal remaining = ledgerService.getBalance(source, "USD");
        BigDecimal debited = new BigDecimal("100.00").subtract(remaining);

        assertThat(succeeded.get()).isLessThanOrEqualTo(10); // never more than funded
        assertThat(debited).isEqualByComparingTo(new BigDecimal(succeeded.get() * 10)); // debits match successes
        assertThat(remaining).isGreaterThanOrEqualTo(BigDecimal.ZERO); // never negative
    }
}
