package dev.fknemi.ledger.fx;

import dev.fknemi.ledger.fx.dto.RateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FxRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public ExchangeRate saveRate(RateRequest request) {
        return exchangeRateRepository.save(ExchangeRate.builder()
                .fromCurrency(request.fromCurrency())
                .toCurrency(request.toCurrency())
                .rate(request.rate())
                .build());
    }

    @Transactional(readOnly = true)
    public BigDecimal getRateAsOf(String from, String to, Instant asOf) {
        return exchangeRateRepository.findRateAsOf(from, to, asOf)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new RuntimeException(
                        "No exchange rate found for %s→%s at %s".formatted(from, to, asOf)));
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentRate(String from, String to) {
        return getRateAsOf(from, to, Instant.now());
    }
}
