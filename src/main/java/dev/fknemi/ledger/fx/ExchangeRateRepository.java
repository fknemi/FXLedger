package dev.fknemi.ledger.fx;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    @Query("""
            SELECT r FROM ExchangeRate r
            WHERE r.fromCurrency = :from
              AND r.toCurrency   = :to
              AND r.effectiveAt <= :asOf
            ORDER BY r.effectiveAt DESC
            LIMIT 1
            """)
    Optional<ExchangeRate> findRateAsOf(@Param("from") String from,
                                        @Param("to")   String to,
                                        @Param("asOf") Instant asOf);
}
