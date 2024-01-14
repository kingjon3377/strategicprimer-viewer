package common.map.fixtures.resources;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Possible status of fields (and meadows, and orchards ...) Fields should
 * rotate between these, at a rate determined by the kind of field.
 *
 * TODO: Implement that
 */
public enum FieldStatus {
    /**
     * Fallow: waiting to be planted, or waiting to start growing.
     */
    Fallow("fallow"),
    /**
     * Seeding: being planted, by human or animal activity.
     */
    Seeding("seeding"),
    /**
     * Growing.
     */
    Growing("growing"),
    /**
     * Bearing: ready to be harvested.
     */
    Bearing("bearing");
    private final String string;

    FieldStatus(final String str) {
        string = str;
    }

    @Override
    public String toString() {
        return string;
    }

    private static final class Cache implements Supplier<List<FieldStatus>> {
        private @Nullable List<FieldStatus> cache;

        public List<FieldStatus> get() {
            if (Objects.isNull(cache)) {
                cache = List.of(values());
            }
            return cache;
        }
    }

    private static final Supplier<List<FieldStatus>> FS_CACHE = new Cache();

    public static FieldStatus random(final long seed) {
        final List<FieldStatus> statuses = FS_CACHE.get();
        return statuses.get(new Random(seed).nextInt(statuses.size()));
    }

    public static FieldStatus parse(final String status) {
        // TODO: Have HashMap cache to speed this up?
        for (final FieldStatus val : FS_CACHE.get()) {
            if (status.equals(val.toString())) {
                return val;
            }
        }
        throw new IllegalArgumentException("Failed to parse FieldStatus from " + status);
    }
}
