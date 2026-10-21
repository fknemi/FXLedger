package dev.fknemi.ledger.fx;

import dev.fknemi.ledger.account.Account;
import dev.fknemi.ledger.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ensures FX suspense accounts exist for the currencies we care about.
 * Extend the CURRENCIES list as needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FxSuspenseAccountInitializer implements ApplicationRunner {

    private static final List<String> CURRENCIES = List.of("USD", "EUR", "GBP", "JPY");
    private static final String SUSPENSE_PREFIX  = "__FX_SUSPENSE_";

    private final AccountRepository accountRepository;
    private final FxSuspenseLookup  fxSuspenseLookup;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String currency : CURRENCIES) {
            String name = SUSPENSE_PREFIX + currency;
            accountRepository.findAll().stream()
                    .filter(a -> a.getName().equals(name))
                    .findFirst()
                    .ifPresentOrElse(
                            a -> fxSuspenseLookup.register(currency, a.getId()),
                            () -> {
                                Account created = accountRepository.save(
                                        Account.builder().name(name).build());
                                fxSuspenseLookup.register(currency, created.getId());
                                log.info("Created FX suspense account {} for {}", created.getId(), currency);
                            });
        }
    }
}
