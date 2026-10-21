package dev.fknemi.ledger.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public final class MoneyUtils {

    private MoneyUtils() {}

    /** Round to the default fraction digits for the given ISO 4217 currency code. */
    public static BigDecimal round(BigDecimal amount, String currencyCode) {
        int scale = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return amount.setScale(scale, RoundingMode.HALF_EVEN);
    }
}
