package dev.fknemi.ledger.common.util;

import java.util.List;
import java.util.UUID;

/** Returns two account IDs in a deterministic order to prevent deadlocks. */
public final class LockOrdering {

    private LockOrdering() {}

    public static List<UUID> sort(UUID a, UUID b) {
        return a.compareTo(b) <= 0 ? List.of(a, b) : List.of(b, a);
    }
}
