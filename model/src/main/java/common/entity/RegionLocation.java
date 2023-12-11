package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * An entity can be located at an unspecified location in a region.
 */
public record RegionLocation(@NotNull String world, int region) implements Location {
    public RegionLocation {
        if (region < -1) {
            throw new IllegalArgumentException("Region must be -1 if unspecified, or else non-negative");
        }
    }
}
