package dev.fknemi.ledger.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Returns two account IDs in a deterministic order to prevent deadlocks. */
public final class LockOrdering {

    private LockOrdering() {
    }

    public static List<UUID> sort(UUID a, UUID b) {
        return a.compareTo(b) <= 0 ? List.of(a, b) : List.of(b, a);
    }

    /**
     * Deterministic order for an arbitrary number of account IDs.
     * Duplicates are removed so the same row is never locked twice.
     */
    public static List<UUID> sort(UUID... ids) {
        return Arrays.stream(ids)
                .distinct()
                .sorted()
                .toList();
    }

}
