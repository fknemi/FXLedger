package dev.fknemi.ledger.fx;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Holds the mapping of currency code → suspense account ID, populated at startup. */
@Component
public class FxSuspenseLookup {

    private final Map<String, UUID> map = new ConcurrentHashMap<>();

    public void register(String currency, UUID accountId) {
        map.put(currency, accountId);
    }

    public UUID getAccountId(String currency) {
        UUID id = map.get(currency);
        if (id == null) throw new RuntimeException("No suspense account for currency: " + currency);
        return id;
    }
}
