package dev.fknemi.ledger.account;

import dev.fknemi.ledger.account.dto.AccountResponse;
import dev.fknemi.ledger.account.dto.CreateAccountRequest;
import dev.fknemi.ledger.common.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        Account account = Account.builder()
                .name(request.name())
                .build();
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        return accountRepository.findById(id)
                .map(AccountResponse::from)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    // Package-visible: used by other services that need the entity
    @Transactional(readOnly = true)
    public Account findEntityById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
